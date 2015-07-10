package org.activityinfo.ui.icons;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;

public interface IconClientBundle extends ClientBundle {
    
    public static IconClientBundle INSTANCE = GWT.create(IconClientBundle.class);
    
    
    @Source("icons.ttf")
    @DataResource.MimeType("application/x-font-ttf")
    DataResource trueTypeFont();

    @Source("icons.eot")
    @DataResource.MimeType("application/vnd.ms-fontobject")
    DataResource embeddedOpenTypeFont();

    @Source("icons.woff")
    @DataResource.MimeType("application/font-woff")
    DataResource openWebFont();

    @Source("icons.css")
    @CssResource.NotStrict
    CssResource iconStyle();
    
}