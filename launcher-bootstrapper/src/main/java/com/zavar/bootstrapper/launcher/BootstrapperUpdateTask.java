package com.zavar.bootstrapper.launcher;

import com.vdurmont.semver4j.Semver;
import com.zavar.bootstrapper.util.ReadableByteChannelWrapper;
import com.zavar.bootstrapper.util.Util;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Locale;

public class BootstrapperUpdateTask extends Task<Void> {

    private final Semver localVersion;
    private final String downloadUrl;
    private final String latestUrl;
    private final Path tempFolder;

    public BootstrapperUpdateTask(Semver localVersion, String downloadUrl, String latestUrl, Path tempFolder) {
        this.localVersion = localVersion;
        this.downloadUrl = downloadUrl;
        this.latestUrl = latestUrl;
        this.tempFolder = tempFolder;
    }

    @Override
    protected Void call() throws Exception {
        updateTitle("Checking for bootstrapper update");
        JSONObject remoteBootstrapperInfo = Util.readJsonFromUrl(downloadUrl + latestUrl);
        Semver remoteVersion = new Semver((String) remoteBootstrapperInfo.get("version"));
        if (!tempFolder.toFile().exists())
            FileUtils.forceMkdir(tempFolder.toFile());
        else
            FileUtils.cleanDirectory(tempFolder.toFile());
        if(localVersion.isLowerThan(remoteVersion)) {
            updateTitle("Downloading bootstrapper");
            NumberFormat nf = NumberFormat.getPercentInstance(Locale.getDefault());
            ReadableByteChannelWrapper rbc = new ReadableByteChannelWrapper(Channels.newChannel(new URL(downloadUrl + remoteBootstrapperInfo.get("path")).openStream()), Util.contentLength(new URL(downloadUrl + remoteBootstrapperInfo.get("path"))));
            rbc.getProgressProperty().addListener((observableValue, number, t1) -> {
                updateProgress((double) observableValue.getValue(), 1);
                updateMessage(nf.format(observableValue.getValue()));
            });
            FileOutputStream fileOutputStream = new FileOutputStream(tempFolder + (String) remoteBootstrapperInfo.get("path"));
            fileOutputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fileOutputStream.close();
            updateTitle("Starting update");
            ProcessBuilder processBuilder = new ProcessBuilder(tempFolder + (String) remoteBootstrapperInfo.get("path"));
            processBuilder.start();
            Platform.exit();
            System.exit(0);
        }
        return null;
    }
}
