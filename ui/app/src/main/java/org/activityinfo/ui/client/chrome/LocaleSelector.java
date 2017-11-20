package org.activityinfo.ui.client.chrome;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Menu;
import org.activityinfo.i18n.shared.ApplicationLocale;


public class LocaleSelector implements IsWidget {

    private HTML icon;

    public LocaleSelector() {


        Menu menu = new Menu();
        for (final ApplicationLocale applicationLocale : ApplicationLocale.values()) {
            CheckMenuItem menuItem = new CheckMenuItem(applicationLocale.getLocalizedName());
            menuItem.setChecked(isCurrent(applicationLocale));
            menuItem.setGroup("lang");
            menuItem.addSelectionHandler(selectionEvent -> switchLocale(applicationLocale));
            menu.add(menuItem);
        }

        String currentLocaleCode = LocaleInfo.getCurrentLocale().getLocaleName().toUpperCase();

        icon = new HTML();
        icon.addStyleName(ChromeBundle.BUNDLE.style().localeIcon());
        icon.addStyleName(ChromeBundle.BUNDLE.style().appBarButton());
        icon.setText(currentLocaleCode);
        icon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                 menu.show(icon.getElement(), new Style.AnchorAlignment(Style.Anchor.TOP, Style.Anchor.BOTTOM));
            }
        });
    }

    @Override
    public Widget asWidget() {
        return icon;
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
