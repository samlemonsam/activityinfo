package org.activityinfo.ui.style;


import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;

public class Styles {

    public static void ensureInjected() {
        StyleBundle bundle = GWT.create(StyleBundle.class);
        if(LocaleInfo.getCurrentLocale().isRTL()) {
            loadCss(bundle.baseStylesRtl().getSafeUri().asString());
        } else {
            loadCss(bundle.baseStyles().getSafeUri().asString());
        }
    }

    private static native void loadCss(String url)/*-{
        var fileref = document.createElement("link");
        fileref.setAttribute("rel","stylesheet");
        fileref.setAttribute("type","text/css");
        fileref.setAttribute("href",url);
        $doc.getElementsByTagName("head")[0].appendChild(fileref);
    }-*/;

}
