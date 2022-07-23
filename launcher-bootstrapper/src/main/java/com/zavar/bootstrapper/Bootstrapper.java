package com.zavar.bootstrapper;

import com.zavar.bootstrapper.util.Util;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.IOException;
import java.util.Objects;

public class Bootstrapper extends Application {

    private static CloseEvent onCloseEvent;
    private static Stage stage;

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            Util.showErrorDialog(e, e.getMessage());
        });
        launch(args);
    }

    @Override
    public void start(Stage splashStage) throws IOException {
        stage = splashStage;
        splashStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/icon.png"))));
        splashStage.setResizable(false);
        Scene scene = new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/bootstrap.fxml"))), 300, 50);
        JMetro jMetro = new JMetro(Style.LIGHT);
        jMetro.setScene(scene);
        splashStage.setScene(scene);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/bootstrap.css")).toExternalForm());
        splashStage.setTitle("ZL2 Bootstrapper");
        splashStage.show();
        splashStage.setOnCloseRequest(windowEvent -> onCloseEvent.onCloseEvent(windowEvent));
    }

    public static void setOnCloseEvent(CloseEvent onCloseEvent) {
        Bootstrapper.onCloseEvent = onCloseEvent;
    }

    public static Stage getStage() {
        return stage;
    }

    @FunctionalInterface
    public interface CloseEvent {
        void onCloseEvent(WindowEvent event);
    }

}
