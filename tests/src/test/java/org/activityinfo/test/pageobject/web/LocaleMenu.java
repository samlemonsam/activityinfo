package org.activityinfo.test.pageobject.web;

import org.activityinfo.test.pageobject.api.FluentElement;

import static org.activityinfo.test.pageobject.api.XPathBuilder.containingText;

/**
 * Created by yuriyz on 5/3/2016.
 */
public class LocaleMenu {

    private FluentElement menu;

    public LocaleMenu(FluentElement menu) {
        this.menu = menu;
    }

    public void selectLocale(String localeName) {
        menu.find().span(containingText(localeName)).clickWhenReady();
    }
}
