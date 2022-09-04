package com.zavar.bootstrapper.controller;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.Notifications;
import com.github.plushaze.traynotification.notification.TrayNotification;
import com.zavar.bootstrapper.Bootstrapper;
import com.zavar.bootstrapper.config.BootstrapConfig;
import com.zavar.bootstrapper.java.JreDownloadTask;
import com.zavar.bootstrapper.java.JreManager;
import com.zavar.bootstrapper.launcher.BootstrapperUpdateTask;
import com.zavar.bootstrapper.launcher.LauncherStartTask;
import com.zavar.bootstrapper.launcher.LauncherUpdateTask;
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
import java.nio.file.Files;
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
    private final Set<Integer> installedJavas = new HashSet<>();
    private final BootstrapConfig config;
    private JreManager jreManager;
    private final ExecutorService executorService;
    private boolean isOffline = true;
    private String availableIp;

    public BootstrapController() throws IOException {
        if(!launcherFolder.toFile().exists())
            FileUtils.forceMkdir(launcherFolder.toFile());
        if(tempFolder.toFile().exists())
            FileUtils.deleteDirectory(tempFolder.toFile());
        if(!jreFolder.toFile().exists())
            FileUtils.forceMkdir(jreFolder.toFile());
        config = new BootstrapConfig();
        executorService = Executors.newCachedThreadPool();
        javas = JavaFinder.find();
        System.out.println(javas);
        try {
            availableIp = config.getAvailableIp();
            jreManager = new JreManager(new URL(config.getJreDownloadUrl(availableIp)), jreFolder, config.getJreListUrl());
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
            bar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

            Task<Void> bootstrapperUpdateTask = null;
            try {
                bootstrapperUpdateTask = new BootstrapperUpdateTask(config.getBootstrapVersion(), availableIp + config.getBootstrapDownloadUrl(), config.getLatestBootstrapUrl(), tempFolder);
            } catch (IOException e) {
                Util.showErrorDialog(e, e.getMessage());
                Platform.exit();
                System.exit(0);
            }

            bootstrapperUpdateTask.exceptionProperty().addListener((observableValue, throwable, t1) -> {
                Util.showErrorDialog(t1, t1.getMessage());
            });

            final List<Integer> jreToInstall = new ArrayList<>();
            info.textProperty().unbind();
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
                System.exit(0);
            }

            final Task<Void> jreDownloadTask = new JreDownloadTask(jreToInstall, tempFolder, jreFolder, jreManager);

            jreDownloadTask.exceptionProperty().addListener((observableValue, throwable, t1) -> {
                Util.showErrorDialog(t1, observableValue.getValue().toString());
            });

            final Task<Void> launcherUpdateTask = new LauncherUpdateTask(launcherFolder, availableIp + config.getLauncherDownloadUrl(), config.getLatestLauncherUrl());

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
                try {
                    jreManager.getSupportedVersions().forEach(v -> {
                        if (jreManager.isJreExists(v))
                            installedJavas.add(v);
                    });
                } catch (IOException e) {
                    Util.showErrorDialog(e, e.getMessage());
                    Platform.exit();
                    System.exit(0);
                }
                launch();
            });

            bootstrapperUpdateTask.setOnSucceeded(workerStateEvent -> {
                Bootstrapper.setOnCloseEvent((windowEvent) -> {
                    if(!jreDownloadTask.isRunning()) {
                        Platform.exit();
                        System.exit(0);
                    } else {
                        windowEvent.consume();
                    }
                });
                info.textProperty().bind(jreDownloadTask.titleProperty());
                progressInfo.textProperty().bind(jreDownloadTask.messageProperty());
                bar.progressProperty().bind(jreDownloadTask.progressProperty());
                executorService.submit(jreDownloadTask);
            });

            Task<Void> finalBootstrapperUpdateTask = bootstrapperUpdateTask;
            Bootstrapper.setOnCloseEvent((windowEvent) -> {
                if(!finalBootstrapperUpdateTask.isRunning()) {
                    Platform.exit();
                    System.exit(0);
                } else {
                    windowEvent.consume();
                }
            });
            info.textProperty().bind(bootstrapperUpdateTask.titleProperty());
            bar.progressProperty().bind(bootstrapperUpdateTask.progressProperty());
            progressInfo.textProperty().bind(bootstrapperUpdateTask.messageProperty());
            executorService.submit(bootstrapperUpdateTask);
        } else {
            Bootstrapper.setOnCloseEvent((windowEvent) -> {
                Platform.exit();
                System.exit(0);
            });
            info.setText("Starting");
            bar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            installedJavas.addAll(JreManager.getLocalJreVersionsList(jreFolder));
            launch();
        }
    }

    private void launch() {
        LauncherStartTask launcherStartTask = new LauncherStartTask(launcherFolder, javas, jreManager, installedJavas, jreFolder);
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
