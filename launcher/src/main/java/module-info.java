module com.zavar.zavarlauncher {
    requires com.zavar.launcher.common;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires javafx.graphics;
    requires org.update4j;
    requires org.apache.commons.net;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires jmccc;
    requires jmccc.yggdrasil.authenticator;

    opens com.zavar.zavarlauncher to javafx.fxml;
    exports com.zavar.zavarlauncher;
    exports com.zavar.zavarlauncher.update.handler;
    opens com.zavar.zavarlauncher.update.handler to javafx.fxml;
    opens com.zavar.zavarlauncher.lang;
    opens com.zavar.zavarlauncher.css;
    opens com.zavar.zavarlauncher.fxml;
    opens com.zavar.zavarlauncher.img.graphics;
    opens com.zavar.zavarlauncher.img.background;
    opens com.zavar.zavarlauncher.img.icons;
}