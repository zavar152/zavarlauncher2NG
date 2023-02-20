package com.zavar.zavarlauncher.auth.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStream;

public final class AuthHttpHandler implements HttpHandler {
    private final PropertyChangeSupport support;

    public AuthHttpHandler() {
        this.support = new PropertyChangeSupport(this);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        support.firePropertyChange("code", "", exchange.getRequestURI().getQuery().split("=")[1]);
    }

    public void addCodeReturnListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

}
