package org.activityinfo.ui.client.local;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import org.activityinfo.i18n.shared.I18N;

/**
 * Informs the user that offline mode is unsupported.
 */
public class UnsupportedDialog {

    public interface Template extends SafeHtmlTemplates {
        
        @Template("<p>{0}</p><p><a href=\"{1}\" target=\"_blank\">{2}</a></p>")
        SafeHtml message(String explanation, SafeUri safeUri, String downloadLabel);
        
    }

    public static void show() {
        
        Template template = GWT.create(Template.class);
        SafeUri downloadUri = UriUtils.fromSafeConstant("https://www.google.com/chrome/browser/desktop/?hl=" +
                LocaleInfo.getCurrentLocale().getLocaleName());

        SafeHtml message = template.message(
                I18N.CONSTANTS.offlineNotSupported(), 
                downloadUri, 
                I18N.CONSTANTS.downloadGoogleChrome());


        MessageBox.alert(I18N.CONSTANTS.offlineNotSupportedTitle(), message.asString(), null);

    }
    
}
