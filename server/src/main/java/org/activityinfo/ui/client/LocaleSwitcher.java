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
        return Window.Location.createUrlBuilder().setParameter("locale", applicationLocale.getCode()).buildString();
    }

    public static boolean isCurrent(ApplicationLocale applicationLocale) {
        return applicationLocale.getCode().equals(LocaleInfo.getCurrentLocale().getLocaleName());
    }
}
