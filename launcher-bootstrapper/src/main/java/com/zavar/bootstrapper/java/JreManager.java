package com.zavar.bootstrapper.java;

import com.zavar.bootstrapper.util.Util;
import org.json.JSONArray;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class JreManager {

    private final URL downloadUrl;
    private final Path jreFolderPath;

    public  JreManager(URL downloadUrl, Path jreFolderPath) {
        this.downloadUrl = downloadUrl;
        this.jreFolderPath = jreFolderPath;
    }

    public List<Integer> getSupportedVersions() throws IOException {
        JSONArray jreVersions = Util.readJsonFromUrl(downloadUrl.toString() + "/jre.json").getJSONArray("versions");
        return jreVersions.toList().stream().map(o -> (Integer)o).toList();
    }

    //TODO only windows support
    public boolean isJreExists(Integer version) {
        return Files.exists(jreFolderPath.resolve(version.toString()).resolve("bin").resolve("java.exe"));
    }

    public URL getDownloadUrlForVersion(Integer version) throws MalformedURLException {
        return new URL(downloadUrl.toExternalForm() + "/" + version + ".zip");
    }

    public URL getDownloadUrl() {
        return downloadUrl;
    }

    public Path getJreFolderPath() {
        return jreFolderPath;
    }
}
