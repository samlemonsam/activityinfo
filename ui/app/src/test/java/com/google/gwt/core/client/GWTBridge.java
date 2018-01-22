package com.google.gwt.core.client;

/**
 * Workaround for JRE unit tests that depend on Sencha class which reference
 * .client.GWT instead of .shared.GWT.
 */
public abstract class GWTBridge extends com.google.gwt.core.shared.GWTBridge {
}