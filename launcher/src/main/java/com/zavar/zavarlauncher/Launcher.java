package com.zavar.zavarlauncher;

import com.vdurmont.semver4j.Semver;
import com.zavar.zavarlauncher.fxml.Console;
import com.zavar.zavarlauncher.fxml.Main;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
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

    private static Stage primaryStage, consoleStage;
    private static URL mainUrl;
    private static Image icon;
    private static URL cssUrl;
    private static AnchorPane root;
    private static FXMLLoader loader;
    private static final Path userHomeFolder = Path.of(System.getProperty("user.home"));
    private static final Path launcherFolder = userHomeFolder.resolve("zavarlauncher2");
    private static final Path tempFolder = launcherFolder.resolve("temp");
    private static final Path jreFolder = launcherFolder.resolve("jre");
    private static final Path minecraftFolder = launcherFolder.resolve("minecraft");
    private static final Path settingsFile = launcherFolder.resolve("settings.properties");
    private static final Path accountFile = launcherFolder.resolve("account.dat");
    private static final Properties settings = new Properties();
    private static final Logger logger = LoggerContext.getContext().getLogger(Launcher.class.getName());
    private static TextArea consoleTextArea;
    private static Semver launcherVersion;
    private static int launcherJavaVersion;

    public static void main(String[] args) throws IOException {
        setupVersion();
        Platform.startup(Launcher::setupConsole);
        if (!Files.exists(launcherFolder))
            Files.createDirectory(launcherFolder);
        if (!Files.exists(settingsFile)) {
            logger.info("Settings file created");
            Files.copy(Objects.requireNonNull(Launcher.class.getResource("settings/default.properties")).openStream(), settingsFile);
        }
        settings.load(new FileReader(settingsFile.toFile()));
        logger.info("Settings loaded");

        mainUrl = Objects.requireNonNull(Launcher.class.getResource("fxml/main.fxml"));
        icon = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("img/icons/icon.png")));
        cssUrl = Objects.requireNonNull(Launcher.class.getResource("css/style.css"));
        launch(args);
    }

    public static Path getLauncherFolder() {
        return launcherFolder;
    }

    public static Path getMinecraftFolder() {
        return minecraftFolder;
    }

    public static Path getTempFolder() {
        return tempFolder;
    }

    public static Path getJreFolder() {
        return jreFolder;
    }

    public static Path getAccountFile() {
        return accountFile;
    }

    private static void setupVersion() throws IOException {
        Properties version = new Properties();
        version.load(Objects.requireNonNull(Launcher.class.getResource("/version.properties")).openStream());
        launcherVersion = new Semver(version.getProperty("launcherVersion"));
        launcherJavaVersion = Integer.parseInt(version.getProperty("launcherJavaVersion"));
    }

    public static Semver getLauncherVersion() {
        return launcherVersion;
    }

    public static int getLauncherJavaVersion() {
        return launcherJavaVersion;
    }

    public static void setupConsole() {
        consoleTextArea = new TextArea();
        consoleTextArea.setEditable(false);
        consoleTextArea.fontProperty().setValue(Font.font("Monospace", FontWeight.MEDIUM, 14));
        consoleTextArea.setWrapText(true);
        System.setOut(new PrintStream(new Console.StreamCapturer(System.out, consoleTextArea::appendText)));
        System.setErr(new PrintStream(new Console.StreamCapturer(System.err, consoleTextArea::appendText)));
    }

    public static void saveSettings() throws IOException {
        settings.store(new FileWriter(settingsFile.toFile()), null);
        logger.info("Saved settings: " + settings);
    }

    //TODO Resources paths
    @Override
    public void start(Stage primaryStage) throws IOException {
        logger.info("Starting launcher");
        ResourceBundle bundle = ResourceBundle.getBundle("com/zavar/zavarlauncher/lang/launcher", LocaleUtils.toLocale(settings.getProperty("general.lang")));
        if (Boolean.parseBoolean(settings.getProperty("general.console")))
            showConsole(bundle);
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
            primaryStage.setTitle(bundle.getString("main.title") + " v" + launcherVersion.toString());
            primaryStage.getIcons().add(icon);
            primaryStage.setScene(scene);
            primaryStage.requestFocus();
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        primaryStage.setOnCloseRequest(windowEvent -> {
            consoleStage.close();
            primaryStage.close();
        });
        Launcher.primaryStage = primaryStage;
    }

    public static void loadMainFxmlWithSettings(Locale locale) throws IOException {
        logger.info("Language changed - " + locale.toString());
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
        if (Boolean.parseBoolean(settings.getProperty("general.console")))
            showConsole(bundle);
        primaryStage.requestFocus();
        ScheduledThreadPoolExecutor sch = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
        sch.scheduleWithFixedDelay(new Task<Void>() {
            @Override
            protected Void call() {
                mainController.openSettings();
                return null;
            }
        }, 900, 900, TimeUnit.MILLISECONDS);
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
        if (Boolean.parseBoolean(settings.getProperty("general.console")))
            showConsole(bundle);
        primaryStage.requestFocus();
        System.gc();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        consoleStage.close();
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void init() throws Exception {
        super.init();
    }

    public static void showConsole(ResourceBundle resourceBundle) {
        logger.info("Starting console");
        if (Objects.nonNull(consoleStage))
            consoleStage.hide();
        else
            consoleStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(Launcher.class.getResource("fxml/console.fxml")), resourceBundle);
        AnchorPane consoleRoot = null;
        try {
            consoleRoot = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AnchorPane.setBottomAnchor(consoleTextArea, 0.0D);
        AnchorPane.setTopAnchor(consoleTextArea, 0.0D);
        AnchorPane.setRightAnchor(consoleTextArea, 0.0D);
        AnchorPane.setLeftAnchor(consoleTextArea, 0.0D);
        consoleRoot.getChildren().add(consoleTextArea);
        consoleStage.setMinHeight(400);
        consoleStage.setMinWidth(700);
        consoleStage.setWidth(700);
        consoleStage.setHeight(400);
        consoleStage.setScene(new Scene(consoleRoot, 700, 400));
        consoleStage.setTitle("ZL Console");
        consoleStage.getIcons().add(icon);
        consoleStage.setX(0);
        consoleStage.setY(5);
        consoleStage.resizableProperty().setValue(false);
        consoleStage.show();
    }

    public static void hideConsole() {
        logger.info("Hiding console");
        if (Objects.nonNull(consoleStage))
            consoleStage.hide();
    }

    public static boolean isConsoleShowing() {
        return Objects.nonNull(consoleStage) && consoleStage.isShowing();
    }
}
