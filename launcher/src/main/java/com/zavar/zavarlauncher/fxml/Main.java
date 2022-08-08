package com.zavar.zavarlauncher.fxml;

import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main implements Initializable {
    @FXML
    private Button playButton, settingsButton;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private AnchorPane mainPane, mainMenuControlsPane, settingsFxml, mainMenuBackgroundPane;
    @FXML
    private Settings settingsFxmlController;

    private final FadeTransition fadeSettingsTransition = new FadeTransition(Duration.millis(500));
    private final FadeTransition fadeMainControlsTransition = new FadeTransition(Duration.millis(500));
    private final FadeTransition fadeMainBackgroundTransition = new FadeTransition(Duration.millis(500));
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fadeMainBackgroundTransition.setNode(mainMenuBackgroundPane);
        fadeMainControlsTransition.setNode(mainMenuControlsPane);
        closeMain();
        settingsFxmlController.setLocalesList(resourceBundle.getLocale(), getAvailableLocales());
        backgroundImage.fitWidthProperty().bind(mainPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(mainPane.heightProperty());
        fadeSettingsTransition.setNode(settingsFxml);
        closeSettings();
        settingsButton.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if(fadeSettingsTransition.getToValue() == 1.0) {
                    closeSettings();
                } else {
                    openSettings();
                }
            }
        });
        ScheduledThreadPoolExecutor sch = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
        sch.scheduleWithFixedDelay(new Task<Void>() {
            @Override
            protected Void call() {
                openMain();
                return null;
            }
        }, 1, 1, TimeUnit.SECONDS);
        settingsFxmlController.setBackButtonHandler(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                closeSettings();
            }
        });
    }

    public void openSettings() {
        fadeSettingsTransition.stop();
        fadeSettingsTransition.setFromValue(0);
        fadeSettingsTransition.setToValue(1.0);
        fadeSettingsTransition.setOnFinished(actionEvent -> {});
        settingsFxml.setDisable(false);
        fadeSettingsTransition.play();
    }

    private void closeSettings() {
        fadeSettingsTransition.stop();
        fadeSettingsTransition.setFromValue(1.0);
        fadeSettingsTransition.setToValue(0);
        fadeSettingsTransition.setOnFinished(actionEvent -> {
            settingsFxml.setDisable(true);
            settingsFxmlController.resetSettingsFromFile();
        });
        fadeSettingsTransition.play();
    }

    private void openMain() {
        fadeMainBackgroundTransition.stop();
        fadeMainBackgroundTransition.setFromValue(0);
        fadeMainBackgroundTransition.setToValue(0.3);
        fadeMainControlsTransition.stop();
        fadeMainControlsTransition.setFromValue(0);
        fadeMainControlsTransition.setToValue(1.0);
        fadeMainControlsTransition.play();
        fadeMainBackgroundTransition.play();
    }

    private void closeMain() {
        fadeMainBackgroundTransition.stop();
        fadeMainBackgroundTransition.setFromValue(0.3);
        fadeMainBackgroundTransition.setToValue(0);
        fadeMainControlsTransition.stop();
        fadeMainControlsTransition.setFromValue(1.0);
        fadeMainControlsTransition.setToValue(0);
        fadeMainControlsTransition.play();
        fadeMainBackgroundTransition.play();
    }

    private Set<ResourceBundle> getAvailableLocales() {
        Set<ResourceBundle> resourceBundles = new HashSet<>();
        for (Locale locale : Locale.getAvailableLocales())
            resourceBundles.add(ResourceBundle.getBundle("com/zavar/zavarlauncher/lang/launcher", locale));
        return Collections.unmodifiableSet(resourceBundles);
    }

    public void setupSettings(Properties settings) {
        settingsFxmlController.setupSettings(settings);
    }
}
