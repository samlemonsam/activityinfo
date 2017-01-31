package org.activityinfo.ui.client.component.table;
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

import com.google.common.base.Strings;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.user.client.Event;
import org.activityinfo.ui.client.component.table.action.TableHeaderAction;

import java.util.List;

/**
 * Handles table header action events.
 *
 * @author yuriyz on 4/8/14.
 */
public class TableHeaderActionBrowserEventHandler {

    private final InstanceTable instanceTable;

    public TableHeaderActionBrowserEventHandler(InstanceTable instanceTable) {
        this.instanceTable = instanceTable;
    }

    public void onBrowserEvent(Event event) {
        final String type = event.getType();
        if (BrowserEvents.CLICK.equals(type) || BrowserEvents.KEYDOWN.equals(type)) {
            handleHeaderActionClick(event);
        }
    }

    private void handleHeaderActionClick(Event event) {
        final EventTarget eventTarget = event.getEventTarget();
        if (!Element.is(eventTarget)) {
            return;
        }

        final Element target = event.getEventTarget().cast();
        final String uniqueId = getUniqueId(target);
        if (!Strings.isNullOrEmpty(uniqueId)) {
            final List<TableHeaderAction> headerActions = instanceTable.getHeaderActions();
            for (TableHeaderAction action : headerActions) {
                if (action.getUniqueId().equals(uniqueId)) {
                    action.execute();
                }
            }
        }
    }

    private String getUniqueId(Element target) {
        final String uniqueId = target.getAttribute(TableHeaderAction.ACTION_ATTRIBUTE);
        final Element parentElement = target.getParentElement();
        if (!Strings.isNullOrEmpty(uniqueId)) {
            return uniqueId;
        } else if (parentElement != null) {
            // try parent, trick is that sometimes target may be child of action element
            return parentElement.getAttribute(TableHeaderAction.ACTION_ATTRIBUTE);
        }
        return null;
    }
}
