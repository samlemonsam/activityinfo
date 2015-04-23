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
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;

import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 7/24/14.
 */
public class SkipPanelPresenter {

    private final FieldWidgetContainer fieldWidgetContainer;
    private final SkipPanel view = new SkipPanel();
    private final Map<SkipRow, SkipRowPresenter> map = Maps.newHashMap();
    private final RowDataBuilder rowDataBuilder;

    public SkipPanelPresenter(final FieldWidgetContainer fieldWidgetContainer) {
        this.fieldWidgetContainer = fieldWidgetContainer;
        this.rowDataBuilder = new RowDataBuilder(fieldWidgetContainer.getFormDesigner().getFormClass());

        if (fieldWidgetContainer.getFormField().hasRelevanceConditionExpression()) {
            List<RowData> build = rowDataBuilder.build(fieldWidgetContainer.getFormField().getRelevanceConditionExpression());
            for (RowData rowData : build) {
                SkipRowPresenter skipRowPresenter = addRow(fieldWidgetContainer);
                skipRowPresenter.updateWith(rowData);
            }
        }

        // add initial row if expression is not set
        if (view.getRootPanel().getWidgetCount() == 0) {
            addRow(fieldWidgetContainer);
        }
    }

    private SkipRowPresenter addRow(final FieldWidgetContainer fieldWidgetContainer) {
        final SkipRowPresenter skipRowPresenter = new SkipRowPresenter(fieldWidgetContainer);
        view.getRootPanel().add(skipRowPresenter.getView());
        map.put(skipRowPresenter.getView(), skipRowPresenter);

        skipRowPresenter.getView().getAddButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addRow(fieldWidgetContainer);
            }
        });
        skipRowPresenter.getView().getRemoveButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                view.getRootPanel().remove(skipRowPresenter.getView());
                map.remove(skipRowPresenter.getView());
                setFirstRowJoinFunctionVisible();
            }
        });

        setFirstRowJoinFunctionVisible();
        return skipRowPresenter;
    }

    private void setFirstRowJoinFunctionVisible() {
        if (view.getRootPanel().getWidgetCount() > 0) { // disable join function for first row
            SkipRow firstSkipRow = (SkipRow) view.getRootPanel().getWidget(0);
            firstSkipRow.getJoinFunction().setVisible(false);
        }
    }

    public SkipPanel getView() {
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
            SkipRow skipRow = (SkipRow) view.getRootPanel().getWidget(i);
            result.add(RowDataFactory.create(skipRow, map.get(skipRow).getValue(), formClass));
        }
        return result;
    }
}
