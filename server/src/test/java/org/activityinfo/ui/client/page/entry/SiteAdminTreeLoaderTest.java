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
package org.activityinfo.ui.client.page.entry;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.TreeStore;
import org.activityinfo.legacy.shared.command.GetAdminEntities;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.DTOs;
import org.activityinfo.ui.client.dispatch.DispatcherStub;
import org.activityinfo.ui.client.page.entry.grouping.AdminGroupingModel;
import org.junit.Test;

public class SiteAdminTreeLoaderTest {

    @Test
    public void load() {

        DispatcherStub dispatcher = new DispatcherStub();
        dispatcher.setResult(new GetSchema(), DTOs.PEAR.SCHEMA);
        dispatcher.setResult(new GetAdminEntities(1), DTOs.PROVINCES);

        SiteAdminTreeLoader loader = new SiteAdminTreeLoader(dispatcher,
                new AdminGroupingModel(1));

        new TreeStore<ModelData>(loader);

        loader.load();
    }

}
