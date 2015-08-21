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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.core.shared.criteria.HasCriteria;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.table.FieldColumn;
import org.activityinfo.ui.client.component.table.InstanceTable;
import org.activityinfo.ui.client.util.GwtUtil;
import org.activityinfo.ui.client.util.Rectangle;
import org.activityinfo.ui.client.widget.Button;
import org.activityinfo.ui.client.widget.DisplayWidget;
import org.activityinfo.ui.client.widget.LoadingPanel;

/**
 * @author yuriyz on 4/3/14.
 */
public class FilterPanel extends Composite implements HasCriteria {

    interface FilterPanelUiBinder extends UiBinder<HTMLPanel, FilterPanel> {
    }

    private static FilterPanelUiBinder uiBinder = GWT.create(FilterPanelUiBinder.class);

    private final InstanceTable table;
    private final FieldColumn column;
    private FilterContent filterContent;

    @UiField
    PopupPanel popup;
    @UiField
    LoadingPanel loadingPanel;
    @UiField
    Button okButton;

    public FilterPanel(final InstanceTable table, final FieldColumn column) {
        this.table = table;
        this.column = column;

        FilterDataGridResources.INSTANCE.dataGridStyle().ensureInjected();

        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public Criteria getCriteria() {
        return filterContent.getCriteria();
    }

    public void show(final PopupPanel.PositionCallback positionCallback) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                popup.setPopupPositionAndShow(positionCallback);

                forcePopupToBeVisible();

                filterContent = FilterContentFactory.create(column, table);
                loadingPanel.setDisplayWidget(new DisplayWidget() {
                    @Override
                    public Promise<Void> show(Object value) {
                        return Promise.done();
                    }

                    @Override
                    public Widget asWidget() {
                        return (Widget) filterContent;
                    }
                });

                loadingPanel.showWithoutLoad();

                filterContent.setChangeHandler(new ValueChangeHandler() {
                    @Override
                    public void onValueChange(ValueChangeEvent event) {
                        okButton.setEnabled(filterContent.isValid());
                    }
                });
                okButton.setEnabled(filterContent.isValid());
            }
        });
    }

    private void forcePopupToBeVisible() {
        final Rectangle bsContainerRectangle = GwtUtil.getBsContainerRectangle(okButton.getElement());
        final Rectangle elementRectangle = GwtUtil.getRectangle(okButton.getElement());

        //GWT.log("element: " + elementRectangle + ", bs container: " + bsContainerRectangle);

        boolean isInViewport = bsContainerRectangle.has(elementRectangle);

        if (!isInViewport) {
            popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                @Override
                public void setPosition(int offsetWidth, int offsetHeight) {
                    int bottomDifference = -(bsContainerRectangle.getBottom() - elementRectangle.getBottom());
                    popup.setPopupPosition(popup.getAbsoluteLeft(), popup.getPopupTop() - bottomDifference);
                }
            });
        }
    }

    public PopupPanel getPopup() {
        return popup;
    }

    @UiHandler("clearButton")
    public void onClear(ClickEvent event) {
        if (filterContent != null) { // may be null in case user is fast enough to click button before items loaded
            filterContent.clear();
        }
        column.setCriteria(null);
        table.getTable().redrawHeaders();
        table.reload();
        popup.hide();
    }

    @UiHandler("okButton")
    public void onOk(ClickEvent event) {
        column.setCriteria(getCriteria());
        table.getTable().redrawHeaders();
        table.reload();
        popup.hide();
    }

    @UiHandler("cancelButton")
    public void cancelButton(ClickEvent event) {
        popup.hide();
    }
}
