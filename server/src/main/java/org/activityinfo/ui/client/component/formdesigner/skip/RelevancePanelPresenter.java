package org.activityinfo.ui.client.component.formdesigner.skip;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;

import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 7/24/14.
 */
public class RelevancePanelPresenter {

    private final FieldWidgetContainer fieldWidgetContainer;
    private final RelevancePanel view = new RelevancePanel();
    private final Map<RelevanceRow, RelevanceRowPresenter> map = Maps.newHashMap();
    private final RowDataBuilder rowDataBuilder;

    public RelevancePanelPresenter(final FieldWidgetContainer fieldWidgetContainer) {
        this.fieldWidgetContainer = fieldWidgetContainer;
        this.rowDataBuilder = new RowDataBuilder(fieldWidgetContainer.getFormDesigner().getFormClass());

        if (fieldWidgetContainer.getFormField().hasRelevanceConditionExpression()) {
            List<RowData> build = rowDataBuilder.build(fieldWidgetContainer.getFormField().getRelevanceConditionExpression());
            for (RowData rowData : build) {
                RelevanceRowPresenter rowPresenter = addRow(fieldWidgetContainer);
                rowPresenter.updateWith(rowData);
            }
        }

        // add initial row if expression is not set
        if (view.getRootPanel().getWidgetCount() == 0) {
            addRow(fieldWidgetContainer);
        }
    }

    private RelevanceRowPresenter addRow(final FieldWidgetContainer fieldWidgetContainer) {
        final RelevanceRowPresenter rowPresenter = new RelevanceRowPresenter(fieldWidgetContainer);
        view.getRootPanel().add(rowPresenter.getView());
        map.put(rowPresenter.getView(), rowPresenter);

        rowPresenter.getView().getAddButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addRow(fieldWidgetContainer);
            }
        });
        rowPresenter.getView().getRemoveButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                view.getRootPanel().remove(rowPresenter.getView());
                map.remove(rowPresenter.getView());
                setFirstRowJoinFunctionVisible();
            }
        });

        setFirstRowJoinFunctionVisible();
        return rowPresenter;
    }

    private void setFirstRowJoinFunctionVisible() {
        if (view.getRootPanel().getWidgetCount() > 0) { // disable join function for first row
            RelevanceRow firstRow = (RelevanceRow) view.getRootPanel().getWidget(0);
            firstRow.getJoinFunction().setVisible(false);
        }
    }

    public RelevancePanel getView() {
        return view;
    }

    public void updateFormField() {
        fieldWidgetContainer.getFormField().setRelevanceConditionExpression(buildSkipExpression());
    }

    private String buildSkipExpression() {
        return new ExpressionBuilder(createRowDataList()).build();
    }

    private List<RowData> createRowDataList() {
        final List<RowData> result = Lists.newArrayList();
        final int widgetCount = view.getRootPanel().getWidgetCount();
        final FormClass formClass = fieldWidgetContainer.getFormDesigner().getFormClass();

        for (int i = 0; i < widgetCount; i++) {
            RelevanceRow row = (RelevanceRow) view.getRootPanel().getWidget(i);
            FieldValue value = map.get(row).getValue();
            if (value == null) {
                throw new NullPointerException("Null value is not allowed.");
            }
            result.add(RowDataFactory.create(row, value, formClass));
        }
        return result;
    }
}
