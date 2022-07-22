package com.zavar.bootstrapper.launcher;

import com.vdurmont.semver4j.Semver;
import com.zavar.bootstrapper.util.ReadableByteChannelWrapper;
import com.zavar.bootstrapper.util.Util;
import javafx.concurrent.Task;
import net.lingala.zip4j.core.ZipFile;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.jar.JarFile;

public class LauncherUpdateTask extends Task<Void> {

    private final Path launcherFolder;
    private final String url;

    public LauncherUpdateTask(Path launcherFolder, String url) {
        this.launcherFolder = launcherFolder;
        this.url = url;
    }

    @Override
    protected Void call() throws Exception {
        NumberFormat nf = NumberFormat.getPercentInstance(Locale.getDefault());
        File launcherFile = FileUtils.getFile(launcherFolder + "/launcher.jar");
        ZipFile launcherJarFile = new ZipFile(launcherFile);
        if (launcherFile.exists()) {
            updateTitle("Checking for update");
            JSONObject remoteLauncherInfo = Util.readJsonFromUrl(url + "/latest.json");
            Semver localVersion;
            Semver remoteVersion = new Semver((String) remoteLauncherInfo.get("version"));
            long remoteSize = Util.contentLength(new URL(url + remoteLauncherInfo.get("path")));
            if(!launcherJarFile.isValidZipFile()) {
                localVersion = new Semver("0.0.0");
            } else {
                URL versionPath = new URL("jar:file:" + launcherFolder + "/launcher.jar!/version.properties");
                Properties properties = new Properties();
                JarURLConnection jarConn = (JarURLConnection) versionPath.openConnection();
                jarConn.setUseCaches(false);
                JarFile jarFile = jarConn.getJarFile();
                properties.load(jarFile.getInputStream(jarConn.getJarEntry()));
                jarConn.getInputStream().close();
                localVersion = new Semver(properties.getProperty("launcherVersion"));
            }
            if (localVersion.isLowerThan(remoteVersion)) {
                FileUtils.moveFile(launcherFile, FileUtils.getFile(launcherFolder + "/old.jar"));
                updateTitle("Updating launcher");
                downloadLauncher(nf, launcherFile, remoteLauncherInfo, remoteSize);
                FileUtils.delete(FileUtils.getFile(launcherFolder + "/old.jar"));
            }
            if(launcherJarFile.isValidZipFile() && launcherFile.length() == remoteSize) {
                updateTitle("Launcher is ready");
            } else {
                updateMessage("");
                updateProgress(0.0, 1.0);
                updateTitle("Launcher is corrupted, try again");
                Thread.sleep(5);
                updateProgress(-1.0, 1.0);
            }

        } else {
            JSONObject remoteLauncherInfo = Util.readJsonFromUrl(url + "/latest.json");
            long remoteSize = Util.contentLength(new URL(url + remoteLauncherInfo.get("path")));
            updateTitle("Downloading launcher");
            downloadLauncher(nf, launcherFile, remoteLauncherInfo, remoteSize);
            if(launcherJarFile.isValidZipFile() && launcherFile.length() == remoteSize) {
                updateTitle("Launcher is ready");
            } else {
                updateMessage("");
                updateProgress(0.0, 1.0);
                updateTitle("Launcher is corrupted, try again");
                Thread.sleep(5);
                updateProgress(-1.0, 1.0);

            }
        }
        return null;
    }

    private void downloadLauncher(NumberFormat nf, File launcherFile, JSONObject remoteLauncherInfo, long remoteSize) throws IOException {
        ReadableByteChannelWrapper rbc = new ReadableByteChannelWrapper(Channels.newChannel(new URL(url + remoteLauncherInfo.get("path")).openStream()), remoteSize);
        rbc.getProgressProperty().addListener((observableValue, number, t1) -> {
            updateProgress((double) observableValue.getValue(), 1);
            updateMessage(nf.format(observableValue.getValue()));
        });
        FileOutputStream fileOutputStream = new FileOutputStream(launcherFolder + (String) remoteLauncherInfo.get("path"));
        fileOutputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fileOutputStream.close();
        FileUtils.moveFile(FileUtils.getFile(launcherFolder + (String) remoteLauncherInfo.get("path")), launcherFile);
    }
}
