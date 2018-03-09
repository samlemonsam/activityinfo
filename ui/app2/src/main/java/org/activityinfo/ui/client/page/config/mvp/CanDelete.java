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
import org.activityinfo.legacy.shared.model.DTO;
import org.activityinfo.ui.client.dispatch.AsyncMonitor;

@Deprecated
public interface CanDelete<M extends DTO> {
    interface ConfirmDeleteHandler extends EventHandler {
        void onConfirmDelete(ConfirmDeleteEvent deleteEvent);
    }

    interface RequestDeleteHandler extends EventHandler {
        void onRequestDelete(RequestDeleteEvent deleteEvent);
    }

    // The user intends to remove an item
    HandlerRegistration addRequestDeleteHandler(RequestDeleteHandler handler);

    // The user confirmed his request to remove an item
    HandlerRegistration addConfirmDeleteHandler(ConfirmDeleteHandler handler);

    // Update the views' store with deletion information
    void delete(M item);

    // The presenter wants to know from the user if he really intends to remove
    // the item
    void askConfirmDelete(M item);

    // The user wants to exit the delete entity mode
    void cancelDelete();

    // If true, the delete button is enabled
    void setDeleteEnabled(boolean deleteEnabled);

    // Let the user know what's going on during deleting
    AsyncMonitor getDeletingMonitor();

    // Since View<T> extends TakesValue<T>, the value does not need to be
    // encapsulated
    class RequestDeleteEvent extends GwtEvent<RequestDeleteHandler> {
        public static final Type TYPE = new Type<RequestDeleteHandler>();

        @Override
        public Type<RequestDeleteHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(RequestDeleteHandler handler) {
            handler.onRequestDelete(this);
        }
    }

    // Since View<T> extends TakesValue<T>, the value does not need to be
    // encapsulated
    class ConfirmDeleteEvent extends GwtEvent<ConfirmDeleteHandler> {
        public static final Type<ConfirmDeleteHandler> TYPE = new Type<>();

        @Override
        public Type<ConfirmDeleteHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ConfirmDeleteHandler handler) {
            handler.onConfirmDelete(this);
        }
    }
}
