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
package org.activityinfo.ui.client.page.config.mvp;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

@Deprecated
public interface CanRefresh<M> {
    interface RefreshHandler extends EventHandler {
        void onRefresh(RefreshEvent refreshEvent);
    }

    // The user wants to have the latest information from the server.
    // The refresh on the view is managed by calling the setValue and other
    // setData
    // methods.
    HandlerRegistration addRefreshHandler(RefreshHandler handler);

    // Is the refresh button enabled?
    void setRefreshEnabled(boolean canRefresh);

    public class RefreshEvent extends GwtEvent<RefreshHandler> {
        public static final Type TYPE = new Type<RefreshHandler>();

        @Override
        public Type<RefreshHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(RefreshHandler handler) {
            handler.onRefresh(this);
        }
    }
}
