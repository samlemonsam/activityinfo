package org.activityinfo.ui.client.component.formdesigner.event;
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

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.activityinfo.ui.client.component.formdesigner.container.WidgetContainer;

/**
 * @author yuriyz on 7/8/14.
 */
public class WidgetContainerSelectionEvent extends GwtEvent<WidgetContainerSelectionEvent.Handler> {

    public interface Handler extends EventHandler {
        void handle(WidgetContainerSelectionEvent event);
    }

    public static Type<Handler> TYPE = new Type<>();

    private final WidgetContainer selectedItem;

    public WidgetContainerSelectionEvent(WidgetContainer selectedItem) {
        this.selectedItem = selectedItem;
    }

    // The instance knows its BeforeSelectionHandler is of type I, but the TYPE
    // field itself does not, so we have to do an unsafe cast here.
    @SuppressWarnings("unchecked")
    @Override
    public final Type<Handler> getAssociatedType() {
        return (Type) TYPE;
    }

    /**
     * Gets the selected item.
     *
     * @return the selected item
     */
    public WidgetContainer getSelectedItem() {
        return selectedItem;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.handle(this);
    }
}