package org.activityinfo.ui.client.component.table.filter;
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

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.activityinfo.ui.icons.Icons;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

/**
 * @param <C> the type that this Cell represents
 * @author yuriyz on 4/2/14.
 */
public class FilterCell<C> extends AbstractCell<C> {

    public interface Template extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<span>{0}</span>" +
                "<span class='{1} pull-right'/>")
        SafeHtml html(String headerName, String icon);
    }

    private static final Template TEMPLATE = GWT.create(Template.class);

    private final FilterCellAction cellAction;

    public FilterCell(FilterCellAction cellAction) {
        super(CLICK, KEYDOWN);

        this.cellAction = cellAction;
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, C value,
                               NativeEvent event, ValueUpdater<C> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        if (CLICK.equals(event.getType())) {
            EventTarget eventTarget = event.getEventTarget();
            if (!Element.is(eventTarget)) {
                return;
            }
            onEnterKeyDown(context, parent, value, event, valueUpdater);
        }
    }

    @Override
    public void render(Context context, C value, SafeHtmlBuilder sb) {
        final boolean hasCriteria = cellAction.getColumn().getCriteria() != null;
        final String icon = hasCriteria ? Icons.INSTANCE.filter() : Icons.INSTANCE.caretDown();
        sb.append(TEMPLATE.html(cellAction.getColumn().getHeader(), icon));
    }

    @Override
    protected void onEnterKeyDown(Context context, Element parent, C value,
                                  NativeEvent event, ValueUpdater<C> valueUpdater) {
        cellAction.execute(value);
    }
}
