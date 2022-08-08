package com.zavar.zavarlauncher.fxml;

import com.zavar.common.finder.JavaFinder;
import com.zavar.zavarlauncher.Launcher;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
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
    @FXML
    private TextField heightField, widthField;
    @FXML
    private Spinner<Integer> ramSpinner;
    @FXML
    private Slider ramSlider;
    private final HashMap<String, Locale> locales = new HashMap<>();
    private Locale selectedLocale;
    private Properties settings;
    private boolean settingsChanged = false;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        jreSelection.disableProperty().bind(autoSwitch.selectedProperty());
        jreSelection.setSourceHeader(new Label(resourceBundle.getString("settings.java.availablelist")));
        jreSelection.setTargetHeader(new Label(resourceBundle.getString("settings.java.selectedlist")));
        //TODO
        jreSelection.getSourceItems().addAll("java18", "java17", "java8");
        saveButton.setOnMouseClicked(mouseEvent -> {
            if(settingsChanged) {
                String value = langBox.valueProperty().getValue();
                try {
                    settings.setProperty("general.animation", String.valueOf(animationSwitch.isSelected()));
                    settings.setProperty("general.console", String.valueOf(consoleSwitch.isSelected()));
                    settings.setProperty("java.autojre", String.valueOf(autoSwitch.isSelected()));
                    if(!locales.get(value).equals(selectedLocale)) {
                        settings.setProperty("general.lang", locales.get(value).toString());
                        Launcher.saveSettings();
                        Launcher.loadMainFxmlWithSettings(locales.get(value));
                    } else {
                        Launcher.saveSettings();
                    }
                    settingsChanged = false;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        setSettingsChangedListeners();
        settingsChanged = false;
    }

    private void setSettingsChangedListeners() {
        jreSelection.targetItemsProperty().addListener((observableValue, strings, t1) -> settingsChanged = true);
        autoSwitch.selectedProperty().addListener((observableValue, strings, t1) -> settingsChanged = true);
        autoSwitch.selectedProperty().addListener((observableValue, strings, t1) -> settingsChanged = true);
        animationSwitch.selectedProperty().addListener((observableValue, strings, t1) -> settingsChanged = true);
        consoleSwitch.selectedProperty().addListener((observableValue, strings, t1) -> settingsChanged = true);
        langBox.valueProperty().addListener((observableValue, strings, t1) -> settingsChanged = true);
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

    public void resetSettingsFromFile() {
        if(settingsChanged) {
            setupSettings(settings);
            String name = selectedLocale.getDisplayLanguage(selectedLocale);
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            langBox.valueProperty().setValue(name);
            settingsChanged = false;
        }
    }

    public void setupSettings(Properties settings) {
        this.settings = settings;
        consoleSwitch.setSelected(Boolean.parseBoolean(this.settings.getProperty("general.console")));
        animationSwitch.setSelected(Boolean.parseBoolean(this.settings.getProperty("general.animation")));
        autoSwitch.setSelected(Boolean.parseBoolean(this.settings.getProperty("java.autojre")));
        heightField.setText(this.settings.getProperty("minecraft.height"));
        widthField.setText(this.settings.getProperty("minecraft.width"));
        ramSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1024, 16384, Integer.parseInt(this.settings.getProperty("minecraft.maxram"))));
        ramSlider.setMin(1024);
        ramSlider.setMax(16384);
        ramSlider.setMajorTickUnit(1024);
        //ramSpinner.valueProperty().bind(ramSpinner.valueProperty());
    }

    public void setBackButtonHandler(EventHandler<? super MouseEvent> handler) {
        backButton.setOnMouseClicked(handler);
    }
}
