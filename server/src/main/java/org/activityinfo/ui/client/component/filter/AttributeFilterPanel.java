package org.activityinfo.ui.client.component.filter;

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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.callback.SuccessCallback;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.ui.client.component.filter.FilterToolBar.RemoveFilterEvent;
import org.activityinfo.ui.client.component.filter.FilterToolBar.RemoveFilterHandler;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import static org.activityinfo.ui.client.component.filter.AttributeGroupFilterWidget.DIMENSION_TYPE;

public class AttributeFilterPanel extends ContentPanel implements FilterPanel {
    private FilterToolBar filterToolBar;

    private Filter value = new Filter();
    private AttributeGroupFilterWidgets widgets;

    @Inject
    public AttributeFilterPanel(Dispatcher service) {
        this.widgets = new AttributeGroupFilterWidgets(this, service, new ValueChangeHandler<Filter>() {
            @Override
            public void onValueChange(ValueChangeEvent<Filter> event) {
                applyFilter();
            }
        }, new SuccessCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                layout();
            }
        }
        );

        setLayout(new RowLayout(Orientation.VERTICAL));
        setScrollMode(Style.Scroll.AUTO);
        setHeadingText(I18N.CONSTANTS.filterByAttribute());
        setIcon(IconImageBundle.ICONS.filter());

        createFilterToolBar();
    }

    private void createFilterToolBar() {
        filterToolBar = new FilterToolBar(false, true);
        filterToolBar.addRemoveFilterHandler(new RemoveFilterHandler() {
            @Override
            public void onRemoveFilter(RemoveFilterEvent deleteEvent) {
                clearFilter();
                ValueChangeEvent.fire(AttributeFilterPanel.this, value);
            }
        });
        setTopComponent(filterToolBar);
    }

    @Override
    public void applyBaseFilter(Filter rawFilter) {
        widgets.draw(rawFilter);
    }

    protected void clearFilter() {
        value = new Filter();
        widgets.clearFilter();
        filterToolBar.setRemoveFilterEnabled(false);
    }

    private void applyFilter() {
        if (isRendered()) {
            value = widgets.getValue();
        }
        ValueChangeEvent.fire(this, value);

        filterToolBar.setRemoveFilterEnabled(true);
    }

    @Override
    public Filter getValue() {
        return value;
    }

    @Override
    public void setValue(Filter value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Filter value, boolean fireEvents) {
        this.value = new Filter();
        this.value.addRestriction(DIMENSION_TYPE, value.getRestrictions(DIMENSION_TYPE));

        widgets.setValue(this.value);

        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Filter> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
