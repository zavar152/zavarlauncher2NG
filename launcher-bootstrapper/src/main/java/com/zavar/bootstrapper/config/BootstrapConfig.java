package com.zavar.bootstrapper.config;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class BootstrapConfig {

    private final List<String> ips;
    private final Properties properties = new Properties();
    private Long version = null;
    private String launcherDownloadUrl = null;
    private String jreDownloadUrl = null;
    private String mainIp = null;
    private String availableIp = null;

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

    public String getAvailableIp() throws NullPointerException {
        if(Objects.isNull(availableIp)) {
            List<String> ips = getAllIps();
            if(pingHost(mainIp,1500)) {
                availableIp = mainIp;
                return mainIp;
            }

            for (String value : ips) {
                if (pingHost(value, 1500)) {
                    availableIp = value;
                    return value;
                }
            }
            if(Objects.isNull(availableIp)) {
                throw new NullPointerException("Server is offline");
            }
        }
        return availableIp;
    }

    private boolean pingHost(String host, int timeout) {
        try {
            URL urlObj = new URL(host);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(timeout);
            con.connect();

            int code = con.getResponseCode();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
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
