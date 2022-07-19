package com.zavar.bootstrapper.controller;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.Notifications;
import com.github.plushaze.traynotification.notification.TrayNotification;
import com.zavar.bootstrapper.config.BootstrapConfig;
import com.zavar.bootstrapper.java.JreManager;
import com.zavar.bootstrapper.util.ReadableByteChannelWrapper;
import com.zavar.common.finder.JavaFinder;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.progress.ProgressMonitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.*;
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
    private final Set<JavaFinder.Java> javas;

    private final BootstrapConfig config;
    private JreManager jreManager;
    private final ExecutorService executorService;
    private boolean isOffline = true;

    public BootstrapController() throws IOException {
        if(!launcherFolder.toFile().exists())
            launcherFolder.toFile().mkdir();
        config = new BootstrapConfig();
        executorService = Executors.newCachedThreadPool();
        javas = JavaFinder.find();
        System.out.println(javas);
        try {
            jreManager = new JreManager(new URL(config.getJreDownloadUrl(config.getAvailableIp())), jreFolder);
            isOffline = false;
        } catch (NullPointerException | MalformedURLException e) {
            TrayNotification tray = new TrayNotification("Bootstrapper warning", e.getMessage(), Notifications.WARNING);
            tray.setTrayIcon(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/icon.png"))));
            tray.setAnimation(Animations.POPUP);
            tray.showAndDismiss(Duration.millis(3500));
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!isOffline) {
            final List<Integer> jreToInstall = new ArrayList<>();
            bar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            info.setText("Loading");

            try {
                jreManager.getSupportedVersions().forEach(v -> {
                    if (!jreManager.isJreExists(v) && javas.stream().noneMatch(java -> java.version() == v))
                        jreToInstall.add(v);
                });
                System.out.println(jreToInstall);
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING, e.getMessage());
                alert.setTitle("Bootstrapper error");
                alert.initStyle(StageStyle.UNDECORATED);
                alert.showAndWait();
                Platform.exit();
            }

            final Task<Void> jreDownloadTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    NumberFormat nf = NumberFormat.getPercentInstance(Locale.getDefault());
                    for (Integer i : jreToInstall) {
                        File zipFile = new File(tempFolder + "/" + i + ".zip");
                        if(!zipFile.exists()) {
                            ReadableByteChannelWrapper rbc = new ReadableByteChannelWrapper(Channels.newChannel(jreManager.getDownloadUrlForVersion(i).openStream()), contentLength(jreManager.getDownloadUrlForVersion(i)));
                            bar.progressProperty().bind(rbc.getProgressProperty());
                            rbc.getProgressProperty().addListener((observableValue, number, t1) -> {
                                updateMessage(nf.format(observableValue.getValue()));
                            });
                            updateTitle("Downloading java " + i);
                            if(!tempFolder.toFile().exists())
                                tempFolder.toFile().mkdir();

                            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
                            fileOutputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                            fileOutputStream.close();
                            bar.progressProperty().unbind();
                        }
                        updateTitle("Unzipping java " + i);
                        ZipFile toUnzipFile = new ZipFile(zipFile);
                        toUnzipFile.setRunInThread(true);
                        ProgressMonitor progressMonitor = toUnzipFile.getProgressMonitor();
                        toUnzipFile.extractAll(jreFolder + "/" + i);
                        while (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
                            updateMessage(nf.format(progressMonitor.getPercentDone()/100.0));
                            bar.setProgress(progressMonitor.getPercentDone()/100.0);
                        }
                        zipFile.delete();
                    }
                    updateTitle("Java is ready");
                    return null;
                }
            };

            info.textProperty().bind(jreDownloadTask.titleProperty());
            progressInfo.textProperty().bind(jreDownloadTask.messageProperty());

            executorService.submit(jreDownloadTask);
        } else {
            bar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            info.setText("Loading launcher...");
            if(javas.stream().anyMatch(java -> java.version() == Integer.parseInt(config.getLauncherJavaVersion()))) {

            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Java " + config.getLauncherJavaVersion() + " couldn't be found");
                alert.setTitle("Bootstrapper error");
                alert.initStyle(StageStyle.UNDECORATED);
                alert.showAndWait();
                Platform.exit();
            }
        }
    }

    private int contentLength(URL url) throws IOException {
        HttpURLConnection connection;
        int contentLength = -1;

        HttpURLConnection.setFollowRedirects(false);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        contentLength = connection.getContentLength();

        return contentLength;
    }

}
