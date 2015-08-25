package org.activityinfo.ui.client.frame;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.ApplicationLocale;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.constants.Toggle;

public class LanguageDropDown implements IsWidget {

    private final ButtonGroup buttonGroup;

    public LanguageDropDown() {
        String current = LocaleInfo.getCurrentLocale().getLocaleName().toUpperCase();
        Button button = new Button(current);
        button.setDataToggle(Toggle.DROPDOWN);

        DropDownMenu menu = new DropDownMenu();
        for (final ApplicationLocale locale : ApplicationLocale.values()) {
            AnchorListItem listItem = new AnchorListItem(locale.getLocalizedName());
            listItem.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    changeLocale(locale);
                }
            });
            menu.add(listItem);
        }

        buttonGroup = new ButtonGroup();
        buttonGroup.add(button);
        buttonGroup.add(menu);
    }

    private void changeLocale(ApplicationLocale locale) {
        UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        urlBuilder.setParameter("locale", locale.name().toLowerCase());
        Window.Location.assign(urlBuilder.buildString());
    }

    @Override
    public Widget asWidget() {
        return buttonGroup;
    }
}
