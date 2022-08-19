package com.zavar.zavarlauncher.fxml;

import com.zavar.common.finder.JavaFinder;
import com.zavar.zavarlauncher.Launcher;
import javafx.beans.value.ChangeListener;
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
    private ListSelectionView<JavaFinder.Java> jreSelection;
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
    private SettingsSavedListener settingsSavedListener = settings -> {};
    private boolean settingsChanged = false;
    private ChangeListener<? super Object> settingsChangedListener;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        settingsChangedListener = (observableValue, strings, t1) -> settingsChanged = true;
        ramSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1024, 16384, 1024,128));
        jreSelection.disableProperty().bind(autoSwitch.selectedProperty());
        jreSelection.setSourceHeader(new Label(resourceBundle.getString("settings.java.availablelist")));
        jreSelection.setTargetHeader(new Label(resourceBundle.getString("settings.java.selectedlist")));
        saveButton.setOnMouseClicked(mouseEvent -> {
            if(settingsChanged) {
                String value = langBox.valueProperty().getValue();
                try {
                    if(consoleSwitch.isSelected() && !Boolean.parseBoolean(settings.getProperty("general.console")))
                        Launcher.showConsole(resourceBundle);
                    else if(!consoleSwitch.isSelected() && Boolean.parseBoolean(settings.getProperty("general.console")))
                        Launcher.hideConsole();
                    settings.setProperty("general.animation", String.valueOf(animationSwitch.isSelected()));
                    settings.setProperty("general.console", String.valueOf(consoleSwitch.isSelected()));
                    settings.setProperty("java.autojre", String.valueOf(autoSwitch.isSelected()));
                    settings.setProperty("minecraft.maxram", String.valueOf(ramSpinner.getValue()));
                    settings.setProperty("minecraft.width", String.valueOf(widthField.getText()));
                    settings.setProperty("minecraft.height", String.valueOf(heightField.getText()));
                    if(!locales.get(value).equals(selectedLocale)) {
                        settings.setProperty("general.lang", locales.get(value).toString());
                        Launcher.saveSettings();
                        settingsSavedListener.onSettingsSaved(settings);
                        Launcher.loadMainFxmlWithSettings(locales.get(value));
                    } else {
                        Launcher.saveSettings();
                        settingsSavedListener.onSettingsSaved(settings);
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
        jreSelection.targetItemsProperty().addListener(settingsChangedListener);
        autoSwitch.selectedProperty().addListener(settingsChangedListener);
        autoSwitch.selectedProperty().addListener(settingsChangedListener);
        animationSwitch.selectedProperty().addListener(settingsChangedListener);
        consoleSwitch.selectedProperty().addListener(settingsChangedListener);
        langBox.valueProperty().addListener(settingsChangedListener);
        ramSpinner.valueProperty().addListener(settingsChangedListener);
        widthField.textProperty().addListener(settingsChangedListener);
        heightField.textProperty().addListener(settingsChangedListener);
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
        javas.forEach(java -> jreSelection.getSourceItems().add(java));
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
        ramSpinner.getValueFactory().setValue(Integer.parseInt(this.settings.getProperty("minecraft.maxram")));
        //ramSlider.setMin(1024);
        //ramSlider.setMax(16384);
        //ramSlider.setMajorTickUnit(1024);
        //ramSlider.setMinorTickCount(0);
//        ramSlider.setSnapToTicks(true);
//        ramSlider.setBlockIncrement(1024);
//        ramSlider.valueProperty().addListener((obs, oldval, newVal) ->
//                ramSlider.setValue(newVal.intValue()));
//        ramSlider.valueProperty().addListener((observableValue, number, t1) -> {
//            ramSpinner.getValueFactory().setValue(number.intValue());
//        });
        //ramSpinner.valueProperty().bind(ramSpinner.valueProperty());
    }

    public void setBackButtonHandler(EventHandler<? super MouseEvent> handler) {
        backButton.setOnMouseClicked(handler);
    }

    public void setOnSettingsSaved(SettingsSavedListener onSettingsSaved) {
        settingsSavedListener = onSettingsSaved;
    }

    @FunctionalInterface
    public interface SettingsSavedListener {
        void onSettingsSaved(Properties settings);
    }
}
