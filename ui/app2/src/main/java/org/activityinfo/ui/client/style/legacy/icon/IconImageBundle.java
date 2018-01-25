/**
 * Application icons
 */
package org.activityinfo.ui.client.style.legacy.icon;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General License for more details.
 * 
 * You should have received a copy of the GNU General 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * Provides access to the application's icons through GWT's magic ImageBundle
 * generator.
 */
@SuppressWarnings("deprecation")
public interface IconImageBundle extends ImageBundle {
    
    IconImageBundle ICONS = GWT.create(IconImageBundle.class);

    AbstractImagePrototype add();

    AbstractImagePrototype delete();

    AbstractImagePrototype editPage();

    AbstractImagePrototype save();

    @Resource("arrow_merge.png")
    AbstractImagePrototype importIcon();

    AbstractImagePrototype database();

    AbstractImagePrototype design();

    AbstractImagePrototype addDatabase();

    AbstractImagePrototype editDatabase();

    AbstractImagePrototype excel();

    AbstractImagePrototype activity();

    AbstractImagePrototype addActivity();

    AbstractImagePrototype user();

    AbstractImagePrototype addUser();

    AbstractImagePrototype deleteUser();

    /*
     * @return Icon for a user group
     */
    AbstractImagePrototype group();

    AbstractImagePrototype table();
    AbstractImagePrototype report();

    AbstractImagePrototype curveChart();

    AbstractImagePrototype map();

    AbstractImagePrototype filter();

    @Resource(value = "key.png")
    AbstractImagePrototype login();

    AbstractImagePrototype cancel();

    AbstractImagePrototype barChart();

    @Resource(value = "keyboard.png")
    AbstractImagePrototype dataEntry();

    @Resource(value = "ruler.png")
    AbstractImagePrototype indicator();

    AbstractImagePrototype attributeGroup();

    AbstractImagePrototype attribute();

    AbstractImagePrototype refresh();

    @Resource(value = "wrench_orange.png")
    AbstractImagePrototype setup();

    AbstractImagePrototype mapped();

    AbstractImagePrototype unmapped();

    AbstractImagePrototype image();

    AbstractImagePrototype msword();

    AbstractImagePrototype pdf();

    AbstractImagePrototype pieChart();

    AbstractImagePrototype unchecked();

    AbstractImagePrototype offline();

    AbstractImagePrototype sync();

    AbstractImagePrototype up();

    AbstractImagePrototype down();

    AbstractImagePrototype remove();

    @Resource(value = "cog.png")
    AbstractImagePrototype create();

    @Resource(value = "page_edit.png")
    AbstractImagePrototype rename();

    @Resource(value = "table_edit.png")
    AbstractImagePrototype select();

    @Resource(value = "information.png")
    AbstractImagePrototype info();

    AbstractImagePrototype collapse();

    AbstractImagePrototype project();

    @Resource(value = "LockedPeriod.png")
    AbstractImagePrototype lockedPeriod();

    @Resource(value = "lockSmall.png")
    AbstractImagePrototype lockedPeriodSmall();

    AbstractImagePrototype partner();

    AbstractImagePrototype empty();

    AbstractImagePrototype applyFilter();

    AbstractImagePrototype site();

    AbstractImagePrototype none();

    AbstractImagePrototype indicators();

    AbstractImagePrototype location();

    AbstractImagePrototype edit();

    AbstractImagePrototype time();

    AbstractImagePrototype note();

    AbstractImagePrototype csv();

    AbstractImagePrototype dashboard();

    AbstractImagePrototype marker();

    @Resource(value = "useLocation16.png")
    AbstractImagePrototype useLocation();

    AbstractImagePrototype list();

    AbstractImagePrototype folder();

    AbstractImagePrototype printer();

    AbstractImagePrototype email();

    AbstractImagePrototype star();

    AbstractImagePrototype starWhite();

    AbstractImagePrototype link();

    AbstractImagePrototype page();

    @Resource(value = "html_add.png")
    AbstractImagePrototype embed();

    AbstractImagePrototype text();

    @Resource(value = "application_form.png")
    AbstractImagePrototype form();

}
