package com.zavar.zavarlauncher.fxml;

import com.zavar.common.finder.JavaFinder;
import com.zavar.zavarlauncher.Launcher;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.ToggleSwitch;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

public class Settings implements Initializable {
    @FXML
    private ListSelectionView<String> jreSelection;
    @FXML
    private ToggleSwitch autoSwitch;
    @FXML
    private ComboBox<String> langBox;
    private final HashMap<String, Locale> locales = new HashMap<>();
    private Locale selectedLocale = Locale.getDefault();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        jreSelection.disableProperty().bind(autoSwitch.selectedProperty());
        jreSelection.setSourceHeader(new Label(resourceBundle.getString("settings.java.availablelist")));
        jreSelection.setTargetHeader(new Label(resourceBundle.getString("settings.java.selectedlist")));
        //TODO
        jreSelection.getSourceItems().addAll("java18", "java17", "java8");
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
        langBox.valueProperty().addListener((observableValue, s, t1) -> {
            try {
                Launcher.loadFxml(locales.get(t1));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void setAvailableJavas(Set<JavaFinder.Java> javas) {

    }
}
