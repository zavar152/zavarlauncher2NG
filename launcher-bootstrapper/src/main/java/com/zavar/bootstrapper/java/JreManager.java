package com.zavar.bootstrapper.java;

import com.zavar.bootstrapper.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class JreManager {

    private final URL downloadUrl;
    private final Path jreFolderPath;
    private final String jreListUrl;

    public JreManager(URL downloadUrl, Path jreFolderPath, String jreListUrl) {
        this.downloadUrl = downloadUrl;
        this.jreFolderPath = jreFolderPath;
        this.jreListUrl = jreListUrl;
    }

    public List<Integer> getSupportedVersions() throws IOException {
        JSONArray jreVersions = Util.readJsonFromUrl(downloadUrl.toString() + "/jre.json").getJSONArray("versions");
        return jreVersions.toList().stream().map(o -> (Integer)o).toList();
    }

    public boolean isJreExists(Integer version) {
        if(SystemUtils.IS_OS_WINDOWS)
            return Files.exists(jreFolderPath.resolve(version.toString()).resolve("bin").resolve("java.exe"));
        else
            return Files.exists(jreFolderPath.resolve(version.toString()).resolve("bin").resolve("java"));
    }

    //TODO only windows support
    public static Set<Integer> getLocalJreVersionsList(Path jreFolderPath) {
        Set<Integer> versions = new HashSet<>();
        try(Stream<Path> paths = Files.list(jreFolderPath)) {
            paths.forEach(path -> {
                File file = path.toFile();
                if(Files.isDirectory(path) && StringUtils.isNumeric(file.getName())) {
                    if(Files.exists(jreFolderPath.resolve(file.getName()).resolve("bin").resolve("java.exe"))) {
                        versions.add(Integer.parseInt(file.getName()));
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return versions;
    }

    //TODO only windows support
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
