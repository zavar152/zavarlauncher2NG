package com.zavar.zavarlauncher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    //TODO Resources paths
    @Override
    public void start(Stage primaryStage) throws IOException {
        ResourceBundle bundle = ResourceBundle.getBundle("com/zavar/zavarlauncher/lang/launcher", new Locale("ru", "RU"));
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("fxml/main.fxml")), bundle);
        Scene scene = new Scene(parent);

        primaryStage.setResizable(false);
        primaryStage.setWidth(800);
        primaryStage.setHeight(500);
        primaryStage.setTitle(bundle.getString("main.title"));
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("img/icons/icon.png"))));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void init() throws Exception {
        super.init();
    }
}
