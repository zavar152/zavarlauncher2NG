package com.zavar.bootstrapper.controller;

import com.zavar.bootstrapper.config.BootstrapConfig;
import com.zavar.bootstrapper.java.JreManager;
import com.zavar.bootstrapper.util.ReadableByteChannelWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BootstrapController implements Initializable {

    @FXML
    public ProgressBar bar;

    @FXML
    public Label info;

    @FXML
    public Label progressInfo;

    private final Path userHomeFolder = Path.of(System.getProperty("user.home"));
    private final Path launcherFolder = userHomeFolder.resolve("zavarlauncher2");
    private final Path tempFolder = launcherFolder.resolve("temp");
    private final Path jreFolder = launcherFolder.resolve("jre");

    private final BootstrapConfig config = new BootstrapConfig();
    private JreManager jreManager;
    private final ExecutorService executorService = Executors.newCachedThreadPool();


    public BootstrapController() throws IOException {
        try {
            String host = config.getAvailableIp();
            jreManager = new JreManager(new URL(config.getJreDownloadUrl(host)), jreFolder);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final List<Integer> jreToInstall = new ArrayList<>();
        bar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        info.setText("Loading");

        try {
            jreManager.getSupportedVersions().forEach(v -> {
                if (!jreManager.isJreExists(v))
                    jreToInstall.add(v);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO urls and paths from config
        final Task<Void> jreDownloadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                NumberFormat nf = NumberFormat.getPercentInstance(Locale.getDefault());
                for(Integer i : jreToInstall) {
                    ReadableByteChannelWrapper rbc = new ReadableByteChannelWrapper(Channels.newChannel(jreManager.getDownloadUrlForVersion(i).openStream()), contentLength(jreManager.getDownloadUrlForVersion(i)));
                    bar.progressProperty().bind(rbc.getProgressProperty());
                    rbc.getProgressProperty().addListener((observableValue, number, t1) -> {
                        updateMessage(nf.format(observableValue.getValue()));
                    });
                    updateTitle("Downloading java " + i);
                    FileOutputStream fileOutputStream = new FileOutputStream(tempFolder + "/" + i + ".zip");
                    fileOutputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    fileOutputStream.close();
                    bar.progressProperty().unbind();
                }
                return null;
            }
        };

        info.textProperty().bind(jreDownloadTask.titleProperty());
        progressInfo.textProperty().bind(jreDownloadTask.messageProperty());

        executorService.submit(jreDownloadTask);

    }

    private int contentLength(URL url) {
        HttpURLConnection connection;
        int contentLength = -1;

        try {
            HttpURLConnection.setFollowRedirects(false);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");

            contentLength = connection.getContentLength();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contentLength;
    }

}
