package com.zavar.bootstrapper.config;

import com.vdurmont.semver4j.Semver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class BootstrapConfig {

    private final List<String> ips;
    private final Properties bootstrapProperties = new Properties();
    private Semver version = null;
    private String launcherDownloadUrl = null;
    private String latestLauncherUrl = null;
    private String jreDownloadUrl = null;
    private String jreListUrl = null;
    private String bootstrapDownloadUrl = null;
    private String latestBootstrapUrl = null;
    private String mainIp = null;
    private String availableIp = null;

    public BootstrapConfig() throws IOException {
        ips = new ArrayList<>();
        bootstrapProperties.load(Objects.requireNonNull(getClass().getResourceAsStream("/config/bootstrap.properties")));
    }

    public List<String> getAllIps() {
        if(ips.size() == 0) {
            List<String> props = Objects.requireNonNull(bootstrapProperties.stringPropertyNames().stream().filter(s -> s.startsWith("serverName")).toList(), "Servers is missing");

            mainIp = bootstrapProperties.getProperty("serverNameMain");

            for(String key : props) {
                ips.add(requireNonEmpty(bootstrapProperties.getProperty(key), key + " is missing"));
            }
        }
        return ips;
    }

    public String getAvailableIp() throws NullPointerException {
        if(Objects.isNull(availableIp)) {
            int timeout = Integer.parseInt(requireNonEmpty(bootstrapProperties.getProperty("pingTimeout"), "pingTimeout is missing"));
            List<String> ips = getAllIps();
            ips.remove(mainIp);
            if(pingHost(mainIp, timeout)) {
                availableIp = mainIp;
                return mainIp;
            }

            for (String value : ips) {
                if (pingHost(value, timeout)) {
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
            con.setRequestMethod("HEAD");
            con.setConnectTimeout(timeout);
            con.connect();

            int code = con.getResponseCode();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public Semver getBootstrapVersion() throws IOException {
        if(Objects.isNull(version)) {
            Properties vProps = new Properties();
            vProps.load(Objects.requireNonNull(getClass().getResourceAsStream("/version.properties")));
            version = new Semver(requireNonEmpty(vProps.getProperty("bootstrapperVersion"), "Version is missing"));
        }
        return version;
    }

    public String getMainIp() {
        if(Objects.isNull(mainIp))
            mainIp = requireNonEmpty(bootstrapProperties.getProperty("serverNameMain"), "Main server name is missing");
        return mainIp;
    }

    public String getJreDownloadUrl(String serverUrl) {
        if(Objects.isNull(jreDownloadUrl))
            jreDownloadUrl = serverUrl + requireNonEmpty(bootstrapProperties.getProperty("jreDownloadUrl"), "Jre url is missing");
        return jreDownloadUrl;
    }

    public String getLauncherDownloadUrl() {
        if(Objects.isNull(launcherDownloadUrl))
            launcherDownloadUrl = requireNonEmpty(bootstrapProperties.getProperty("launcherDownloadUrl"), "Launcher url is missing");
        return launcherDownloadUrl;
    }

    public String getLatestLauncherUrl() {
        if(Objects.isNull(latestLauncherUrl))
            latestLauncherUrl = requireNonEmpty(bootstrapProperties.getProperty("latestLauncherUrl"), "Latest file url is missing");
        return latestLauncherUrl;
    }

    public String getBootstrapDownloadUrl() {
        if(Objects.isNull(bootstrapDownloadUrl))
            bootstrapDownloadUrl = requireNonEmpty(bootstrapProperties.getProperty("bootstrapDownloadUrl"), "Bootstrap url is missing");
        return bootstrapDownloadUrl;
    }

    public String getLatestBootstrapUrl() {
        if(Objects.isNull(latestBootstrapUrl))
            latestBootstrapUrl = requireNonEmpty(bootstrapProperties.getProperty("latestBootstrapUrl"), "Latest file url is missing");
        return latestBootstrapUrl;
    }

    public String getJreListUrl() {
        if(Objects.isNull(jreListUrl))
            jreListUrl = requireNonEmpty(bootstrapProperties.getProperty("jreListUrl"), "Jre list url is missing");
        return jreListUrl;
    }

    private static <T> T requireNonEmpty(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);

        if (((String)obj).isEmpty())
            throw new NullPointerException("Empty string");

        return obj;
    }
}
