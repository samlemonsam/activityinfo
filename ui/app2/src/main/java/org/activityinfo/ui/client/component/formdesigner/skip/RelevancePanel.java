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

import com.google.common.base.Optional;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formula.simple.Criteria;
import org.activityinfo.model.formula.simple.SimpleCondition;
import org.activityinfo.model.formula.simple.SimpleConditionList;
import org.activityinfo.model.formula.simple.SimpleOperators;
import org.activityinfo.ui.client.widget.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuriyz on 7/23/14.
 */
public class RelevancePanel implements IsWidget {

    private static final int ALL_INDEX = 0;
    private static final int ANY_INDEX = 1;

    private static final int MAX_CONDITIONS = 5;


    private static OurUiBinder uiBinder = GWT.create(OurUiBinder.class);

    interface OurUiBinder extends UiBinder<HTMLPanel, RelevancePanel> {
    }

    private List<FormField> selectableFields;


    HTMLPanel rootPanel;

    @UiField
    FlowPanel conditionPanel;

    @UiField
    ListBox criteriaListBox;

    @UiField
    Button addButton;

    private List<RelevanceRow> rows = new ArrayList<>();

    public RelevancePanel() {
        this.rootPanel = uiBinder.createAndBindUi(this);

        criteriaListBox.addItem(I18N.CONSTANTS.relevanceCriteriaAll());
        criteriaListBox.addItem(I18N.CONSTANTS.relevanceCriteriaAny());
    }

    public void init(List<FormField> fields, SimpleConditionList model) {
        assert rows.isEmpty();

        this.selectableFields = new ArrayList<>();
        for (FormField field : fields) {
            if(!SimpleOperators.forType(field.getType()).isEmpty()) {
                selectableFields.add(field);
            }
        }

        if(model.getCriteria() == Criteria.ALL_TRUE) {
            criteriaListBox.setSelectedIndex(ALL_INDEX);
        } else {
            criteriaListBox.setSelectedIndex(ANY_INDEX);
        }


        for (SimpleCondition condition : model.getConditions()) {
            addRow(Optional.of(condition));
        }
        if(rows.isEmpty()) {
            addRow(Optional.<SimpleCondition>absent());
        }
        onRowCountUpdated();
    }

    public Criteria getSelectedCriteria() {
        if(criteriaListBox.getSelectedIndex() == ANY_INDEX) {
            return Criteria.ANY_TRUE;
        } else {
            return Criteria.ALL_TRUE;
        }
    }


    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    @UiHandler("addButton")
    public void addButtonClick(ClickEvent event) {
        addRow(Optional.<SimpleCondition>absent());
        onRowCountUpdated();
    }

    private void addRow(Optional<SimpleCondition> condition) {
        final RelevanceRow row = new RelevanceRow(selectableFields, condition);
        row.addRemoveHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removeCondition(row);
            }

        });
        rows.add(row);
        conditionPanel.add(row);
    }


    private void removeCondition(RelevanceRow row) {
        if (rows.remove(row)) {
            conditionPanel.remove(row);
            onRowCountUpdated();
        }
    }



    private void onRowCountUpdated() {
        for (RelevanceRow row : rows) {
            row.setRemoveEnabled(rows.size() > 1);
        }
        addButton.setVisible(rows.size() < MAX_CONDITIONS);
    }


    public String build() {

        List<SimpleCondition> conditions = new ArrayList<>();
        for (RelevanceRow row : rows) {
            conditions.add(row.buildCondition());
        }

        return new SimpleConditionList(getSelectedCriteria(), conditions)
                .toFormula()
                .asExpression();
    }

}
