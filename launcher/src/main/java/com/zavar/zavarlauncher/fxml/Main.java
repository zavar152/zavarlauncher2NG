package com.zavar.zavarlauncher.fxml;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.*;

public class Main implements Initializable {
    @FXML
    private Button playButton, settingsButton;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private AnchorPane mainPane;
    @FXML
    private Settings settingsFxmlController;
    @FXML
    private AnchorPane settingsFxml;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        settingsFxml.setVisible(false);
        settingsFxmlController.setLocalesList(resourceBundle.getLocale(), getAvailableLocales());
        backgroundImage.fitWidthProperty().bind(mainPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(mainPane.heightProperty());
        settingsButton.setOnMouseClicked(mouseEvent -> {
            if(settingsFxml.isVisible()) {
                closeSettings();
            } else {
                openSettings();
            }
        });
    }

    public void openSettings() {
        settingsFxml.setVisible(true);
    }

    private void closeSettings() {
        settingsFxml.setVisible(false);
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
