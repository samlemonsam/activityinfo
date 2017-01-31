package org.activityinfo.ui.client.page.config;

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

import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.page.FrameId;
import org.activityinfo.ui.client.page.common.nav.NavigationPanel;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

public class ConfigFrameSet extends VSplitFrameSet {

    public static final FrameId PAGE_ID = new FrameId("config");

    @Inject
    public ConfigFrameSet(EventBus eventBus, Dispatcher dispatcher) {
        super(ConfigFrameSet.PAGE_ID,
                new NavigationPanel(eventBus, new ConfigNavigator(dispatcher, I18N.CONSTANTS, IconImageBundle.ICONS)));
    }

}
