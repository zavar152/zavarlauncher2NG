module com.zavar.zavarlauncher {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires javafx.graphics;
    requires org.update4j;
    requires org.apache.commons.net;
    requires jmccc;
    requires jmccc.yggdrasil.authenticator;

    opens com.zavar.zavarlauncher to javafx.fxml;
    exports com.zavar.zavarlauncher;
}