package com.zavar.zavarlauncher;

import com.zavar.zavarlauncher.fxml.Main;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.LocaleUtils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Launcher extends Application {

    private static Stage primaryStage;
    private static URL mainUrl;
    private static Image icon;
    private static URL cssUrl;
    private static AnchorPane root;
    private static FXMLLoader loader;
    private static final Path userHomeFolder = Path.of(System.getProperty("user.home"));
    private static final Path launcherFolder = userHomeFolder.resolve("zavarlauncher2");
    private final Path tempFolder = launcherFolder.resolve("temp");
    private final Path jreFolder = launcherFolder.resolve("jre");
    private static final Path settingsFile = launcherFolder.resolve("settings.properties");
    private static final Properties settings = new Properties();

    public static void main(String[] args) throws IOException {
        if(!Files.exists(settingsFile)) {
            Files.copy(Objects.requireNonNull(Launcher.class.getResource("settings/default.properties")).openStream(), settingsFile);
        }
        settings.load(new FileReader(settingsFile.toFile()));

        mainUrl = Objects.requireNonNull(Launcher.class.getResource("fxml/main.fxml"));
        icon = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("img/icons/icon.png")));
        cssUrl = Objects.requireNonNull(Launcher.class.getResource("css/style.css"));
        launch(args);
    }

    public static void saveSettings() throws IOException {
        System.out.println(settings);
        settings.store(new FileWriter(settingsFile.toFile()), null);
    }

    //TODO Resources paths
    @Override
    public void start(Stage primaryStage)  {
        ResourceBundle bundle = ResourceBundle.getBundle("com/zavar/zavarlauncher/lang/launcher", LocaleUtils.toLocale(settings.getProperty("general.lang")));
        loader = new FXMLLoader(mainUrl);
        try {
            loader.setResources(bundle);
            root = loader.load();
            Main mainController = loader.getController();
            mainController.setupSettings(settings);
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

    public static void loadMainFxmlWithSettings(Locale locale) throws IOException {
        primaryStage.hide();
        ResourceBundle bundle = ResourceBundle.getBundle("com/zavar/zavarlauncher/lang/launcher", locale);
        loader = new FXMLLoader(mainUrl);
        loader.setResources(bundle);
        AnchorPane temp = loader.load();
        Main mainController = loader.getController();
        mainController.setupSettings(settings);
        Scene scene = new Scene(temp);
        scene.getStylesheets().add(cssUrl.toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
        ScheduledThreadPoolExecutor sch = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
        sch.scheduleWithFixedDelay(new Task<Void>() {
            @Override
            protected Void call() {
                mainController.openSettings();
                return null;
            }
        }, 1, 1, TimeUnit.SECONDS);
        System.gc();
    }

    public static void loadMainFxml(Locale locale) throws IOException {
        primaryStage.hide();
        ResourceBundle bundle = ResourceBundle.getBundle("com/zavar/zavarlauncher/lang/launcher", locale);
        loader = new FXMLLoader(mainUrl);
        loader.setResources(bundle);
        AnchorPane temp = loader.load();
        Main mainController = loader.getController();
        mainController.setupSettings(settings);
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
