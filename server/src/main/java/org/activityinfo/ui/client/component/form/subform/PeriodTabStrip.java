package org.activityinfo.ui.client.component.form.subform;
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

import com.gargoylesoftware.htmlunit.javascript.host.Event;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
    private int tabCount = 5;
    private PeriodCursor cursor;
    private Tab value;
    
    public PeriodTabStrip(SubFormKind kind) {
        super(SafeHtmlUtils.EMPTY_SAFE_HTML);

        Preconditions.checkNotNull(kind);

        setKind(kind);
        sinkEvents(Event.CLICK);
        
        addDomHandler(this, ClickEvent.getType());
        render();
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
            default:
                setVisible(false);
        }
        render();
    }

    private void render() {
        SafeHtmlBuilder list = new SafeHtmlBuilder();
        list.append(TEMPLATES.button(-5, "<<"));
        list.append(TEMPLATES.button(-1, "<"));

        for(int i=0;i<tabCount;++i) {
            Tab tab = cursor.get(i);
            String cssClass = tab.equals(getValue()) ? "active" : "";
            list.append(TEMPLATES.tab(tab.getId(), tab.getLabel(), cssClass));
        }
        list.append(TEMPLATES.button(+1, ">"));
        list.append(TEMPLATES.button(+5, ">>"));
        getElement().setInnerSafeHtml(TEMPLATES.tabList(list.toSafeHtml()));
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

    public SubFormKind getKind() {
        return kind;
    }
}
