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
package org.activityinfo.ui.client.component.formdesigner.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.activityinfo.ui.client.component.formdesigner.container.WidgetContainer;

/**
 * @author yuriyz on 11/13/2014.
 */
public class PanelUpdatedEvent extends GwtEvent<PanelUpdatedEvent.Handler> {

    public static enum EventType {
        ADDED
    }

    public static interface Handler extends EventHandler {
        void handle(PanelUpdatedEvent event);
    }

    public static Type<Handler> TYPE = new Type<>();

    private final WidgetContainer widgetContainer;
    private final EventType type;

    public PanelUpdatedEvent(WidgetContainer widgetContainer, EventType type) {
        this.widgetContainer = widgetContainer;
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Type<PanelUpdatedEvent.Handler> getAssociatedType() {
        return (Type) TYPE;
    }

    public EventType getType() {
        return type;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.handle(this);
    }
}