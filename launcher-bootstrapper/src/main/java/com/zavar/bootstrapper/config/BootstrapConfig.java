package com.zavar.bootstrapper.config;

import java.io.IOException;
import java.util.*;

public class BootstrapConfig {

    private final List<String> ips;
    private final Properties properties = new Properties();
    private Long version = null;
    private String launcherDownloadUrl = null;
    private String jreDownloadUrl = null;
    private String mainIp = null;

    public BootstrapConfig() throws IOException {
        ips = new ArrayList<>();
        properties.load(Objects.requireNonNull(getClass().getResourceAsStream("/config/bootstrap.properties")));
    }

    public List<String> getAllIps() {
        if(ips.size() == 0) {
            List<String> props = Objects.requireNonNull(properties.stringPropertyNames().stream().filter(s -> s.startsWith("serverName")).toList(), "Servers is missing");

            mainIp = properties.getProperty("serverNameMain");

            for(String key : props) {
                ips.add(requireNonEmpty(properties.getProperty(key), key + " is missing"));
            }
        }
        return ips;
    }

    public Long getBootstrapVersion() {
        if(Objects.isNull(version))
            version = Long.parseLong(requireNonEmpty(properties.getProperty("version"), "Version is missing"));
        return version;
    }

    public String getMainIp() {
        if(Objects.isNull(mainIp))
            mainIp = requireNonEmpty(properties.getProperty("serverNameMain"), "Main server name is missing");
        return mainIp;
    }

    public String getJreDownloadUrl(String serverUrl) {
        if(Objects.isNull(jreDownloadUrl))
            jreDownloadUrl = serverUrl + requireNonEmpty(properties.getProperty("jreDownloadUrl"), "Jre url is missing");
        return jreDownloadUrl;
    }

    public String getLauncherDownloadUrl() {
        if(Objects.isNull(launcherDownloadUrl))
            launcherDownloadUrl = requireNonEmpty(properties.getProperty("launcherDownloadUrl"), "Launcher url is missing");
        return launcherDownloadUrl;
    }

    private static <T> T requireNonEmpty(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);

        if (((String)obj).isEmpty())
            throw new NullPointerException("Empty string");

        return obj;
    }
}
