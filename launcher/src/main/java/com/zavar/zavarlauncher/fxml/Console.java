package com.zavar.zavarlauncher.fxml;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

public final class Console implements Initializable {
    @FXML
    private TextArea textArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.setOut(new PrintStream(new StreamCapturer(System.out, text -> {
            textArea.appendText(text);
        })));
    }
    @FunctionalInterface
    private interface Consumer {
        void appendText(String text);
    }

    private static class StreamCapturer extends OutputStream {
        private final StringBuilder buffer;
        private final Consumer consumer;
        private final PrintStream old;

        public StreamCapturer(PrintStream old, Consumer consumer) {
            this.buffer = new StringBuilder(128);
            this.old = old;
            this.consumer = consumer;
        }

        @Override
        public void write(int b) {
            char c = (char) b;
            String value = Character.toString(c);
            buffer.append(value);
            if (value.equals("\n")) {
                consumer.appendText(buffer.toString());
                buffer.delete(0, buffer.length());
            }
            old.print(c);
        }
    }
}
