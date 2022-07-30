package com.zavar.zavarlauncher.fxml;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.controlsfx.control.ListSelectionView;

import java.net.URL;
import java.util.ResourceBundle;

public class Settings implements Initializable {
    @FXML
    private ListSelectionView<String> jreSelection;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        jreSelection.getSourceItems().addAll("java18", "java17", "java8");
    }
}
