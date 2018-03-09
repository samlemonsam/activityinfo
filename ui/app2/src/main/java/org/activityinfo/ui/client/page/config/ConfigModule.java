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
/**
 * The set of pages comprising the configuration rubrique, including definition and creation
 * of databases, user settings, etc.
 */
package org.activityinfo.ui.client.page.config;

import com.google.gwt.inject.client.AbstractGinModule;
import org.activityinfo.ui.client.page.config.LockedPeriodsPresenter.LockedPeriodListEditor;

/**
 * @author Alex Bertram
 */
public class ConfigModule extends AbstractGinModule {

    @Override
    protected void configure() {

        // binds the view components
        bind(DbListPresenter.View.class).to(DbListPage.class);
        bind(DbPartnerEditor.View.class).to(DbPartnerGrid.class);
        bind(DbProjectEditor.View.class).to(DbProjectGrid.class);
        bind(LockedPeriodListEditor.class).to(LockedPeriodGrid.class);
        bind(DbTargetEditor.View.class).to(DbTargetGrid.class);
    }
}