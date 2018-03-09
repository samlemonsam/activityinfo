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

import com.extjs.gxt.ui.client.data.RpcProxy;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetAdminEntities;
import org.activityinfo.legacy.shared.command.result.AdminEntityResult;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;

import java.util.ArrayList;
import java.util.List;

class AdminTreeProxy extends RpcProxy<List<AdminEntityDTO>> {

    private final Dispatcher service;
    private Filter filter;

    public AdminTreeProxy(Dispatcher service) {
        this.service = service;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * Load nodes in the tree
     *
     * @param parent
     * @param callback
     */
    @Override
    protected void load(final Object parent, final AsyncCallback<List<AdminEntityDTO>> callback) {

        if (filter == null) {
            callback.onSuccess(new ArrayList<AdminEntityDTO>());
            return;
        }

        if (!hasRestrictions()) {
            callback.onSuccess(new ArrayList<AdminEntityDTO>());

        } else {
            GetAdminEntities request = new GetAdminEntities();
            request.setFilter(filter);

            if (parent == null) {
                request.setParentId(GetAdminEntities.ROOT);
            } else {
                assert parent instanceof AdminEntityDTO : "expecting AdminEntityDTO";
                request.setParentId(((AdminEntityDTO) parent).getId());
            }

            service.execute(request, new AsyncCallback<AdminEntityResult>() {
                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(AdminEntityResult result) {
                    callback.onSuccess(result.getData());
                }
            });
        }
    }

    private boolean hasRestrictions() {

        return filter.isRestricted(DimensionType.Activity) ||
               filter.isRestricted(DimensionType.Database) ||
               filter.isRestricted(DimensionType.Indicator);

    }
}
