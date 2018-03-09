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
package org.activityinfo.ui.client.component.filter;

import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;

class AdminTreeLoader extends BaseTreeLoader<AdminEntityDTO> {

    public AdminTreeLoader(Dispatcher service) {
        super(new AdminTreeProxy(service));
    }

    @Override
    public boolean hasChildren(AdminEntityDTO parent) {
        return true;
    }

    public void setFilter(Filter filter) {
        ((AdminTreeProxy) this.proxy).setFilter(filter);
    }
}
