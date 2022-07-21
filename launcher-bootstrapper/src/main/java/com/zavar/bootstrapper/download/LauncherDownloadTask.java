package com.zavar.bootstrapper.download;

import com.vdurmont.semver4j.Semver;
import com.zavar.bootstrapper.util.ReadableByteChannelWrapper;
import com.zavar.bootstrapper.util.Util;
import javafx.concurrent.Task;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.util.Properties;

public class LauncherDownloadTask extends Task<Void> {

    private final Path launcherFolder;
    private final String url;

    public LauncherDownloadTask(Path launcherFolder, String url) {
        this.launcherFolder = launcherFolder;
        this.url = url;
    }

    @Override
    protected Void call() throws Exception {
        URL versionPath = new URL("jar:file:" + launcherFolder + "/launcher.jar!/version.properties");
        Properties properties = new Properties();
        properties.load(versionPath.openStream());
        JSONObject remoteLauncherInfo = Util.readJsonFromUrl(url + "/latest.json");
        Semver localVersion = new Semver(properties.getProperty("launcherVersion"));
        Semver remoteVersion = new Semver((String) remoteLauncherInfo.get("version"));
        if(localVersion.isLowerThan(remoteVersion)) {
            ReadableByteChannelWrapper rbc = new ReadableByteChannelWrapper(Channels.newChannel(new URL(url + remoteLauncherInfo.get("path")).openStream()), Util.contentLength(new URL(url + remoteLauncherInfo.get("path"))));
            rbc.getProgressProperty().addListener((observableValue, number, t1) -> {
                updateProgress((double) observableValue.getValue(), 1);
            });
            FileOutputStream fileOutputStream = new FileOutputStream(launcherFolder + (String) remoteLauncherInfo.get("path"));
            fileOutputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fileOutputStream.close();
        }

        return null;
    }
}
