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
package org.activityinfo.ui.client.component.form.subform;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import org.activityinfo.model.form.SubFormKind;


public class PeriodTabStrip extends HTMLPanel implements ClickHandler, HasValue<Tab> {

    public interface Templates extends SafeHtmlTemplates {

        @Template("<li class=\"{2}\"><a href=\"javascript:\" data-period=\"{0}\" >{1}</a></li>")
        SafeHtml tab(String periodId, String label, String cssClass);

        @Template("<li><a href=\"javascript:\" data-cursor=\"{0}\">{1}</a></li>")
        SafeHtml button(int cursorChange, String label);

        @Template("<ul class=\"nav nav-pills\">{0}</ul>")
        SafeHtml tabList(SafeHtml tabs);

    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);

    private SubFormKind kind;
    private int tabCount;
    private PeriodCursor cursor;
    private Tab value;

    public PeriodTabStrip(SubFormKind kind) {
        super(SafeHtmlUtils.EMPTY_SAFE_HTML);

        Preconditions.checkNotNull(kind);

        tabCount = initialTabCount(kind);

        setKind(kind);
        sinkEvents(Event.ONCLICK);

        addDomHandler(this, ClickEvent.getType());
        render();
    }

    private int initialTabCount(SubFormKind kind) {
        switch (kind) {
            case DAILY:
            case BIWEEKLY:
                return 3;
            case WEEKLY:
            case MONTHLY:
                return 4;
        }

        return 5;
    }

    public void setKind(SubFormKind kind) {
        this.kind = kind;
        switch (kind) {
            case MONTHLY:
                setVisible(true);
                cursor = new MonthCursor();
                break;
            case DAILY:
                setVisible(true);
                cursor = new DailyCursor();
                break;
            case WEEKLY:
                setVisible(true);
                cursor = new WeeklyCursor();
                break;
            case BIWEEKLY:
                setVisible(true);
                cursor = new BiWeeklyCursor();
                break;
            default:
                setVisible(false);
        }
        if (cursor != null) { // avoid state when tab is not selected (data entered on UI is not bound to any key)
            setValue(cursor.getCurrentPeriod().toString(), false);
        }
        render();
    }

    private void render() {
        SafeHtmlBuilder list = new SafeHtmlBuilder();
        list.append(TEMPLATES.button(-tabCount, "<<"));
        list.append(TEMPLATES.button(-1, "<"));

        for (int i = 0; i < tabCount; ++i) {
            Tab tab = cursor.get(i);
            String cssClass = tab.equals(getValue()) ? "active" : "";
            list.append(TEMPLATES.tab(tab.getId(), tab.getLabel(), cssClass));
        }
        list.append(TEMPLATES.button(+1, ">"));
        list.append(TEMPLATES.button(+tabCount, ">>"));
        getElement().setInnerSafeHtml(TEMPLATES.tabList(list.toSafeHtml()));

        recalculateTabCount();
    }

    private void recalculateTabCount() {
        final int[] counter = new int[]{0};
        Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                counter[0]++;
                NodeList<Element> buttons = getElement().getElementsByTagName("li");
                if (buttons.getLength() > 0) {
                    Element firstButton = buttons.getItem(0);
                    Element lastButton = buttons.getItem(buttons.getLength() - 1);

                    if (firstButton.getAbsoluteTop() != lastButton.getAbsoluteTop()) {
                        tabCount--;
                        render();
                    }
                    return false;
                }
                if (counter[0] > 10) { // safe exit
                    return false;
                }

                return true;
            }
        }, 500);
    }

    @Override
    public Tab getValue() {
        return value;
    }

    @Override
    public void setValue(Tab value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Tab value, boolean fireEvents) {
        this.value = value;

        cursor.setCurrentPeriod(cursor.getValue(value.getId()));
        render();

        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    public void setValue(String dataPeriod, boolean fireEvent) {
        setValue(cursor.get(dataPeriod), fireEvent);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Tab> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void onClick(ClickEvent event) {
        Element element = event.getNativeEvent().getEventTarget().cast();
        String dataCursor = element.getAttribute("data-cursor");
        if (!Strings.isNullOrEmpty(dataCursor)) {
            int change = Integer.parseInt(dataCursor);
            cursor.advance(change);
            render();
            return;
        }

        String dataPeriod = element.getAttribute("data-period");
        if (!Strings.isNullOrEmpty(dataPeriod)) {
            this.value = cursor.get(dataPeriod);
            render();
            ValueChangeEvent.fire(this, value);
        }
    }

}
