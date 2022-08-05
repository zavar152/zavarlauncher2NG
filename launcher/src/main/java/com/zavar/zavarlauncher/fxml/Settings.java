package com.zavar.zavarlauncher.fxml;

import com.zavar.common.finder.JavaFinder;
import com.zavar.zavarlauncher.Launcher;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.ToggleSwitch;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Settings implements Initializable {
    @FXML
    private ListSelectionView<String> jreSelection;
    @FXML
    private ToggleSwitch autoSwitch, consoleSwitch, animationSwitch;
    @FXML
    private ComboBox<String> langBox;
    @FXML
    private Button saveButton, backButton;
    private final HashMap<String, Locale> locales = new HashMap<>();
    private Locale selectedLocale;
    private Properties settings;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        jreSelection.disableProperty().bind(autoSwitch.selectedProperty());
        jreSelection.setSourceHeader(new Label(resourceBundle.getString("settings.java.availablelist")));
        jreSelection.setTargetHeader(new Label(resourceBundle.getString("settings.java.selectedlist")));
        //TODO
        jreSelection.getSourceItems().addAll("java18", "java17", "java8");
        saveButton.setOnMouseClicked(mouseEvent -> {
            String value = langBox.valueProperty().getValue();
            try {
                settings.setProperty("general.animation", String.valueOf(animationSwitch.isSelected()));
                settings.setProperty("general.console", String.valueOf(consoleSwitch.isSelected()));
                settings.setProperty("java.autojre", String.valueOf(autoSwitch.isSelected()));
                Launcher.saveSettings();
                if(!locales.get(value).equals(selectedLocale)) {
                    settings.setProperty("general.lang", locales.get(value).toString());
                    Launcher.loadMainFxmlWithSettings(locales.get(value));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void setLocalesList(Locale currentLocale, Set<ResourceBundle> resourceBundles) {
        selectedLocale = currentLocale;
        resourceBundles.forEach(resourceBundle -> {
            Locale temp = resourceBundle.getLocale();
            String name = temp.getDisplayLanguage(selectedLocale);
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            if(temp.equals(selectedLocale))
                langBox.valueProperty().setValue(name);
            locales.put(name, temp);
            langBox.getItems().add(name);
        });
    }

    public void setAvailableJavas(Set<JavaFinder.Java> javas) {

    }

    public void setupSettings(Properties settings) {
        this.settings = settings;
        consoleSwitch.setSelected(Boolean.parseBoolean(this.settings.getProperty("general.console")));
        animationSwitch.setSelected(Boolean.parseBoolean(this.settings.getProperty("general.animation")));
        autoSwitch.setSelected(Boolean.parseBoolean(this.settings.getProperty("java.autojre")));
    }
}
