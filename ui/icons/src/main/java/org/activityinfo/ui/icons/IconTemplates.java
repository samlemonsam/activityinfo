package org.activityinfo.ui.icons;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

interface IconTemplates extends SafeHtmlTemplates {
    
    public static final IconTemplates INSTANCE = GWT.create(IconTemplates.class);

    @Template("<span class=\"icon icon_{0}\"></span>")
    SafeHtml icon(String name);
}
