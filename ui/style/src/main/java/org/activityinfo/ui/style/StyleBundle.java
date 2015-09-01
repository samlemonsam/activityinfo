package org.activityinfo.ui.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ExternalTextResource;


public interface StyleBundle extends ClientBundle {
    
    interface BaseCssResource extends CssResource {
    }
    
    @Source("base.css")
    DataResource baseStyles();

    @Source("base-rtl.css")
    DataResource baseStylesRtl();
}
