package com.zavar.bootstrapper.controller;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.Notifications;
import com.github.plushaze.traynotification.notification.TrayNotification;
import com.zavar.bootstrapper.Bootstrapper;
import com.zavar.bootstrapper.config.BootstrapConfig;
import com.zavar.bootstrapper.download.JreDownloadTask;
import com.zavar.bootstrapper.launcher.LauncherStartTask;
import com.zavar.bootstrapper.launcher.LauncherUpdateTask;
import com.zavar.bootstrapper.java.JreManager;
import com.zavar.bootstrapper.util.Util;
import com.zavar.common.finder.JavaFinder;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
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
    private String availableIp;

    public BootstrapController() throws IOException {
        if(!launcherFolder.toFile().exists())
            FileUtils.forceMkdir(launcherFolder.toFile());
        config = new BootstrapConfig();
        executorService = Executors.newCachedThreadPool();
        javas = JavaFinder.find();
        System.out.println(javas);
        try {
            availableIp = config.getAvailableIp();
            jreManager = new JreManager(new URL(config.getJreDownloadUrl(availableIp)), jreFolder);
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
                FileUtils.forceDeleteOnExit(tempFolder.toFile());
            } catch (IOException e) {
                Util.showErrorDialog(e, e.getMessage());
                Platform.exit();
            }

            final Task<Void> jreDownloadTask = new JreDownloadTask(jreToInstall, tempFolder, jreFolder, jreManager, bar);

            info.textProperty().bind(jreDownloadTask.titleProperty());
            progressInfo.textProperty().bind(jreDownloadTask.messageProperty());
            bar.progressProperty().bind(jreDownloadTask.progressProperty());
            Bootstrapper.setOnCloseEvent((windowEvent) -> {
                if(!jreDownloadTask.isRunning()) {
                    Platform.exit();
                    System.exit(0);
                } else {
                    windowEvent.consume();
                }
            });

            jreDownloadTask.exceptionProperty().addListener((observableValue, throwable, t1) -> {
                Util.showErrorDialog(t1, observableValue.getValue().toString());
            });

            final Task<Void> launcherUpdateTask = new LauncherUpdateTask(launcherFolder, availableIp + config.getLauncherDownloadUrl());
            jreDownloadTask.setOnSucceeded(workerStateEvent -> {
                Bootstrapper.setOnCloseEvent((windowEvent) -> {
                    if(!launcherUpdateTask.isRunning()) {
                        Platform.exit();
                        System.exit(0);
                    } else {
                        windowEvent.consume();
                    }
                });
                info.textProperty().unbind();
                progressInfo.textProperty().unbind();
                bar.progressProperty().unbind();
                info.textProperty().bind(launcherUpdateTask.titleProperty());
                progressInfo.textProperty().bind(launcherUpdateTask.messageProperty());
                bar.progressProperty().bind(launcherUpdateTask.progressProperty());
                executorService.submit(launcherUpdateTask);
            });

            launcherUpdateTask.exceptionProperty().addListener((observableValue, throwable, t1) -> {
                Util.showErrorDialog(t1, observableValue.getValue().toString());
            });

            launcherUpdateTask.setOnSucceeded(workerStateEvent -> {
                launch();
            });

            executorService.submit(jreDownloadTask);
        } else {
            Bootstrapper.setOnCloseEvent((windowEvent) -> {
                Platform.exit();
                System.exit(0);
            });
            info.setText("Starting");
            bar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            launch();
        }
    }

    private void launch() {
        LauncherStartTask launcherStartTask = new LauncherStartTask(launcherFolder, javas, jreManager);
        launcherStartTask.exceptionProperty().addListener((observableValue, throwable, t1) -> {
            Util.showErrorDialog(t1, observableValue.getValue().toString());
        });
        launcherStartTask.setOnSucceeded(workerStateEvent -> {
            Platform.exit();
            System.exit(0);
        });
        executorService.submit(launcherStartTask);
    }

}
