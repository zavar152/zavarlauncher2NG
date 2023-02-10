package com.zavar.bootstrapper.config;

import javafx.concurrent.Task;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class GetAvailableServerTask extends Task<String> {

    private final List<String> ips;
    private final String mainIp;
    private final Integer timeout;

    public GetAvailableServerTask(List<String> ips, String mainIp, Integer timeout) {
        this.ips = ips;
        this.mainIp = mainIp;
        this.timeout = timeout;
    }

    @Override
    protected String call() throws Exception {
        updateTitle("Pinging main server...");
        int size = 1 + ips.size();
        updateProgress(1, size);
        updateMessage(1 + "/" + size);
        if (pingHost(mainIp, timeout)) {
            updateMessage("");
            updateProgress(0.0, size);
            return mainIp;
        }


        int i = 2;
        for (String ip : ips) {
            updateTitle("Pinging " + ip + " ...");
            updateProgress(i, size);
            updateMessage(i + "/" + size);
            if (pingHost(ip, timeout)) {
                updateMessage("");
                updateProgress(0.0, size);
                return ip;
            }
            i++;
        }
        updateMessage("");
        updateProgress(0.0, size);
        return "";
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
}
