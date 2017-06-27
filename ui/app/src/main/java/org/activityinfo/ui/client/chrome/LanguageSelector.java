package org.activityinfo.ui.client.chrome;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Menu;
import org.activityinfo.i18n.shared.ApplicationLocale;


public class LanguageSelector implements IsWidget {

    private TextButton button;

    public LanguageSelector() {

        Menu localeMenu = new Menu();
        for (final ApplicationLocale applicationLocale : ApplicationLocale.values()) {
            CheckMenuItem menuItem = new CheckMenuItem(applicationLocale.getLocalizedName());
            menuItem.setChecked(isCurrent(applicationLocale));
            menuItem.setGroup("lang");
            menuItem.addSelectionHandler(selectionEvent -> switchLocale(applicationLocale));
            localeMenu.add(menuItem);
        }

        String currentLocaleCode = LocaleInfo.getCurrentLocale().getLocaleName().toUpperCase();

        button = new TextButton(currentLocaleCode);
        button.setMenu(localeMenu);
    }

    @Override
    public Widget asWidget() {
        return button;
    }


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
