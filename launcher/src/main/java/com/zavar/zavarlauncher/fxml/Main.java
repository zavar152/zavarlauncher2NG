package com.zavar.zavarlauncher.fxml;

import com.zavar.common.finder.JavaFinder;
import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main implements Initializable {
    @FXML
    private Button playButton, settingsButton, updateButton, folderButton;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private AnchorPane mainPane, mainMenuControlsPane, settingsFxml, mainMenuBackgroundPane;
    @FXML
    private Settings settingsFxmlController;

    private final FadeTransition fadeSettingsTransition = new FadeTransition(Duration.millis(500));
    private final FadeTransition fadeMainControlsTransition = new FadeTransition(Duration.millis(500));
    private final FadeTransition fadeMainBackgroundTransition = new FadeTransition(Duration.millis(500));
    private boolean animationEnable = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fadeMainBackgroundTransition.setNode(mainMenuBackgroundPane);
        fadeMainControlsTransition.setNode(mainMenuControlsPane);
        closeMain();
        settingsFxmlController.setLocalesList(resourceBundle.getLocale(), getAvailableLocales());
        try {
            settingsFxmlController.setAvailableJavas(JavaFinder.find());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        settingsFxmlController.setOnSettingsSaved(settings -> animationEnable = Boolean.parseBoolean(settings.getProperty("general.animation")));
        backgroundImage.fitWidthProperty().bind(mainPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(mainPane.heightProperty());
        fadeSettingsTransition.setNode(settingsFxml);
        closeSettings();
        settingsButton.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (fadeSettingsTransition.getToValue() == 1.0) {
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
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                closeSettings();
            }
        });
    }

    public void openSettings() {
        fadeSettingsTransition.stop();
        if (animationEnable)
            fadeSettingsTransition.setFromValue(0);
        else
            fadeSettingsTransition.setFromValue(1.0);
        fadeSettingsTransition.setToValue(1.0);
        fadeSettingsTransition.setOnFinished(actionEvent -> {
        });
        settingsFxml.setDisable(false);
        fadeSettingsTransition.play();
        playButton.setDisable(true);
        folderButton.setDisable(true);
        updateButton.setDisable(true);
    }

    private void closeSettings() {
        fadeSettingsTransition.stop();
        if (animationEnable)
            fadeSettingsTransition.setFromValue(1.0);
        else
            fadeSettingsTransition.setFromValue(0);
        fadeSettingsTransition.setToValue(0);
        fadeSettingsTransition.setOnFinished(actionEvent -> {
            settingsFxml.setDisable(true);
            try {
                settingsFxmlController.resetSettingsFromFile();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        fadeSettingsTransition.play();
        playButton.setDisable(false);
        folderButton.setDisable(false);
        updateButton.setDisable(false);
    }

    private void openMain() {
        fadeMainBackgroundTransition.stop();
        if(animationEnable)
            fadeMainBackgroundTransition.setFromValue(0);
        else
            fadeMainBackgroundTransition.setFromValue(0.3);
        fadeMainBackgroundTransition.setToValue(0.3);
        fadeMainControlsTransition.stop();
        if(animationEnable)
            fadeMainControlsTransition.setFromValue(0);
        else
            fadeMainControlsTransition.setFromValue(1.0);
        fadeMainControlsTransition.setToValue(1.0);
        fadeMainControlsTransition.play();
        fadeMainBackgroundTransition.play();
    }

    private void closeMain() {
        fadeMainBackgroundTransition.stop();
        if(animationEnable)
            fadeMainBackgroundTransition.setFromValue(0.3);
        else
            fadeMainBackgroundTransition.setFromValue(0);
        fadeMainBackgroundTransition.setToValue(0);
        fadeMainControlsTransition.stop();
        if(animationEnable)
            fadeMainControlsTransition.setFromValue(1.0);
        else
            fadeMainControlsTransition.setFromValue(0);
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

    public void setupSettings(Properties settings) throws FileNotFoundException {
        animationEnable = Boolean.parseBoolean(settings.getProperty("general.animation"));
        settingsFxmlController.setupSettings(settings);
    }
}
