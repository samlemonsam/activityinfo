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
package org.activityinfo.ui.client.page.common.grid;

import com.extjs.gxt.ui.client.data.ModelData;
import org.activityinfo.ui.client.dispatch.AsyncMonitor;
import org.activityinfo.ui.client.page.common.toolbar.ActionToolBar;

public interface GridView<P extends GridPresenter, M extends ModelData> {
    public void setActionEnabled(String actionId, boolean enabled);

    public void confirmDeleteSelected(ConfirmCallback callback);

    public M getSelection();

    public AsyncMonitor getDeletingMonitor();

    public AsyncMonitor getSavingMonitor();

    public void refresh();

    public ActionToolBar getToolBar();
}
