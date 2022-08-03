package com.zavar.zavarlauncher;

import com.zavar.zavarlauncher.fxml.Main;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class Launcher extends Application {

    private static Stage primaryStage;
    private static URL mainUrl;
    private static Image icon;
    private static URL cssUrl;
    private static AnchorPane root;
    private static FXMLLoader loader;

    public static void main(String[] args) {
        mainUrl = Objects.requireNonNull(Launcher.class.getResource("fxml/main.fxml"));
        icon = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("img/icons/icon.png")));
        cssUrl = Objects.requireNonNull(Launcher.class.getResource("css/style.css"));
        launch(args);
    }

    //TODO Resources paths
    @Override
    public void start(Stage primaryStage)  {
        ResourceBundle bundle = ResourceBundle.getBundle("com/zavar/zavarlauncher/lang/launcher", new Locale("ru", "RU"));
        loader = new FXMLLoader(mainUrl);
        try {
            loader.setResources(bundle);
            root = loader.load();
            Main mainController = loader.getController();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(cssUrl.toExternalForm());
            primaryStage.setWidth(800);
            primaryStage.setHeight(500);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(500);
            primaryStage.setTitle(bundle.getString("main.title"));
            primaryStage.getIcons().add(icon);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Launcher.primaryStage = primaryStage;
    }

    public static void loadFxml(Locale locale) throws IOException {
        primaryStage.hide();
        ResourceBundle bundle = ResourceBundle.getBundle("com/zavar/zavarlauncher/lang/launcher", locale);
        loader = new FXMLLoader(mainUrl);
        loader.setResources(bundle);
        AnchorPane temp = loader.load();
        Scene scene = new Scene(temp);
        scene.getStylesheets().add(cssUrl.toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
        System.gc();
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
