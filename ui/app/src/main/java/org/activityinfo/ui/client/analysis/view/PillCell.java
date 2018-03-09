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
package org.activityinfo.ui.client.analysis.view;

import com.google.common.base.Function;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;


public class PillCell<T> extends AbstractCell<T> {


    private Function<T, String> labelFunction;
    private PillHandler<T> handler;

    public PillCell(Function<T, String> labelFunction, PillHandler<T> handler) {
        super(BrowserEvents.CLICK);
        this.labelFunction = labelFunction;
        this.handler = handler;
    }

    @Override
    public void render(Context context, T model, SafeHtmlBuilder sb) {
        sb.appendEscaped(labelFunction.apply(model));
        sb.appendHtmlConstant("<div class=\"" + AnalysisBundle.INSTANCE.getStyles().handle() + "\">");
        sb.appendHtmlConstant("&#8942;");
        sb.appendHtmlConstant("</div>");
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event, ValueUpdater<T> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        if (CLICK.equals(event.getType())) {
            EventTarget eventTarget = event.getEventTarget();
            if (!Element.is(eventTarget)) {
                return;
            }
            Element element = Element.as(eventTarget);
            if (parent.getFirstChildElement().isOrHasChild(element) &&
                   element.hasClassName(AnalysisBundle.INSTANCE.getStyles().handle()) ) {

                handler.showMenu(element, value);
            }
        }
    }
}
