package com.zavar.bootstrapper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class Bootstrapper extends Application {

    private static Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage splashStage) throws IOException {
        splashStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/icon.png"))));
        splashStage.initStyle(StageStyle.UNDECORATED);
        Scene scene = new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/bootstrap.fxml"))), 300, 50);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/bootstrap.css")).toExternalForm());
        splashStage.setScene(scene);
        splashStage.setTitle("ZL2 Bootstrapper");
        splashStage.show();
        stage = splashStage;
    }

    public static Stage getStage() {
        return stage;
    }
}
