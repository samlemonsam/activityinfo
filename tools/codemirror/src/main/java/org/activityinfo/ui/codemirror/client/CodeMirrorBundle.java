package org.activityinfo.ui.codemirror.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

/**
 * Javascript and CSS resources
 */
public interface CodeMirrorBundle extends ClientBundle {

    public static CodeMirrorBundle INSTANCE = GWT.create(CodeMirrorBundle.class);

    @Source("codemirror.js")
    TextResource script();

    @Source("codemirror.css")
    TextResource styles();
}
