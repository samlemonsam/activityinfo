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
package org.activityinfo.ui.client;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import org.activityinfo.i18n.shared.ApplicationLocale;

/**
 * Switches the application locale
 */
public class LocaleSwitcher {


    public static void switchLocale(ApplicationLocale applicationLocale) {
        if(!isCurrent(applicationLocale)) {
            String newUrl = localeUrl(applicationLocale);
            Window.Location.assign(newUrl);
        }
    }

    public static String localeUrl(ApplicationLocale applicationLocale) {
        // We use the sub-delim '+' extensively in fragment identifiers,
        // for example:
        //     #data-entry/Activity+53432
        // UrlBuilder will decode and re-encode this resulting in
        //     #data-entry/Activity%2053432 
        // Which will break navigation
        // So we will first construct the url without the hash,
        // and then append the already-encoded fragment identifier directly
        
        String url = Window.Location.createUrlBuilder()
                .setParameter("locale", applicationLocale.getCode())
                .setHash(null)
                .buildString();
        
        return url + Window.Location.getHash();
    }

    public static boolean isCurrent(ApplicationLocale applicationLocale) {
        return applicationLocale.getCode().equals(LocaleInfo.getCurrentLocale().getLocaleName());
    }
}
