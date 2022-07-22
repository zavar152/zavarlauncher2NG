package com.zavar.bootstrapper.util;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Util {
    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return new JSONObject(sb.toString());
        }
    }

    public static long contentLength(URL url) throws IOException {
        HttpURLConnection connection;

        HttpURLConnection.setFollowRedirects(false);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");

        return connection.getContentLengthLong();
    }

    public static void showErrorDialog(Throwable t1, String title) {
        t1.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR, title);
        alert.setTitle("Bootstrapper error");
        TextArea textArea = new TextArea(ExceptionUtils.getStackTrace(t1));
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    public static void showWarningDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.setTitle("Bootstrapper warning");
        alert.initStyle(StageStyle.UNDECORATED);
        alert.showAndWait();
    }

}
