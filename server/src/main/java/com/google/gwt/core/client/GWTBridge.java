package com.google.gwt.core.client;


import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Workaround for NoClassDefFoundErrors thrown by some usage of this class on the server.
 * Need to identify and remove.
 */
public class GWTBridge extends com.google.gwt.core.shared.GWTBridge {



    private static final Logger LOGGER = Logger.getLogger(GWTBridge.class.getName());

    static {
        LOGGER.log(Level.SEVERE, "com.google.gwt.core.client.GWTBridge class loaded", new RuntimeException());
    }

    @Override
    public <T> T create(Class<?> aClass) {
        throw new UnsupportedOperationException("Create " + aClass.getName() + " not supported in server code.");
    }

    @Override
    public String getVersion() {
        return "unknown";
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public void log(String s, Throwable throwable) {
        LOGGER.log(Level.FINE, s, throwable);
    }
}
