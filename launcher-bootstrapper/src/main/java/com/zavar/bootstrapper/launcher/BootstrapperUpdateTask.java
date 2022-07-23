package com.zavar.bootstrapper.launcher;

import com.vdurmont.semver4j.Semver;
import com.zavar.bootstrapper.util.Util;
import javafx.concurrent.Task;
import org.json.JSONObject;

public class BootstrapperUpdateTask extends Task<Void> {

    private final Semver localVersion;
    private final String downloadUrl;
    private final String latestUrl;

    public BootstrapperUpdateTask(Semver localVersion, String downloadUrl, String latestUrl) {
        this.localVersion = localVersion;
        this.downloadUrl = downloadUrl;
        this.latestUrl = latestUrl;
    }

    @Override
    protected Void call() throws Exception {
        updateTitle("Checking for bootstrapper update");
        JSONObject remoteLauncherInfo = Util.readJsonFromUrl(downloadUrl + latestUrl);
        Semver remoteVersion = new Semver((String) remoteLauncherInfo.get("version"));
        if(localVersion.isLowerThan(remoteVersion)) {
            Runtime rt = Runtime.getRuntime();
            rt.exec( "rundll32 url.dll,FileProtocolHandler " + downloadUrl + remoteLauncherInfo.get("path"));
            throw new InstantiationException("Download new bootstrapper and try again");
        }
        return null;
    }
}
