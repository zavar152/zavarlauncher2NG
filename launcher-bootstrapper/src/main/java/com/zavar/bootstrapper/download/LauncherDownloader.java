package com.zavar.bootstrapper.download;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class LauncherDownloader {

    public URL downloadUrl;
    public Path launcherPath;

    public LauncherDownloader(URL downloadUrl, Path launcherPath) {
        this.downloadUrl = downloadUrl;
        this.launcherPath = launcherPath;
    }

    public boolean isLauncherExists() {
        return Files.exists(launcherPath);
    }
}
