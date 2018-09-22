/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
