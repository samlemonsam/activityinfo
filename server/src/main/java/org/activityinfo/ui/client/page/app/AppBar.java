package org.activityinfo.ui.client.page.app;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.ApplicationLocale;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.LocaleSwitcher;
import org.activityinfo.ui.client.local.LocalController;
import org.activityinfo.ui.client.page.NavigationEvent;
import org.activityinfo.ui.client.page.NavigationHandler;

public class AppBar extends Composite {

    private static AppBarUiBinder uiBinder = GWT.create(AppBarUiBinder.class);

    @UiField SectionTabStrip sectionTabStrip;

    @UiField Label logo;
    @UiField Label localeButton;

    private SettingsPopup settingsPopup;

    private EventBus eventBus;

    private LocalController offlineController;

    public static final int HEIGHT = 50;
    private Menu localeMenu;

    interface AppBarUiBinder extends UiBinder<Widget, AppBar> {
    }

    @Inject
    public AppBar(EventBus eventBus, LocalController offlineController) {
        this.eventBus = eventBus;
        this.offlineController = offlineController;

        initWidget(uiBinder.createAndBindUi(this));
        
        this.localeButton.setText(LocaleInfo.getCurrentLocale().getLocaleName().toUpperCase());
    }

    public SectionTabStrip getSectionTabStrip() {
        return sectionTabStrip;
    }

    @UiHandler("logo") 
    void handleLogoClick(ClickEvent e) {
        Window.open("http://about.activityinfo.org/", "_blank", null);
    }

    @UiHandler("settingsButton") 
    void handleSettingsClick(ClickEvent e) {
        if (settingsPopup == null) {
            settingsPopup = new SettingsPopup(eventBus, offlineController);
        }
        settingsPopup.setPopupPosition(Window.getClientWidth() - SettingsPopup.WIDTH, HEIGHT - 3);
        settingsPopup.show();
    }

    @UiHandler("helpButton")
    void helpClick(ClickEvent e) {
        Window.open("http://help.activityinfo.org", "_blank", null);
    }
    
    @UiHandler("localeButton")
    void localeClick(ClickEvent e) {
        if(localeMenu == null) {
            localeMenu = new Menu();
            for (final ApplicationLocale applicationLocale : ApplicationLocale.values()) {
                CheckMenuItem menuItem = new CheckMenuItem(applicationLocale.getLocalizedName());
                menuItem.setChecked(LocaleSwitcher.isCurrent(applicationLocale));
                menuItem.setGroup("lang");
                menuItem.addSelectionListener(new SelectionListener<MenuEvent>() {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        LocaleSwitcher.switchLocale(applicationLocale);
                    }
                });
                localeMenu.add(menuItem);
            }
            localeMenu.add(new SeparatorMenuItem());

            MenuItem preferenceItem = new MenuItem(I18N.CONSTANTS.language());
            preferenceItem.addSelectionListener(new SelectionListener<MenuEvent>() {
                @Override
                public void componentSelected(MenuEvent ce) {
                    eventBus.fireEvent(new NavigationEvent(NavigationHandler.NAVIGATION_REQUESTED, 
                            new UserProfilePage.State()));
                }
            });
            localeMenu.add(preferenceItem);
        }
        localeMenu.show(localeButton.getElement(), "?");
    }

}
