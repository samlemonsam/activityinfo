package org.activityinfo.ui.client.page.entry.form;

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
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.legacy.shared.model.IndicatorGroup;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.ui.client.dispatch.type.IndicatorNumberFormat;

import java.util.List;
import java.util.Set;

public class IndicatorSection extends LayoutContainer implements FormSection<SiteDTO> {

    public static final int NUMBER_FIELD_WIDTH = 50;
    public static final int TEXT_FIELD_WIDTH = 300;

    public static final int UNITS_FIELD_WIDTH = 80;

    private List<Field> indicatorFields = Lists.newArrayList();
    private Set<IndicatorDTO> calculatedIndicators = Sets.newHashSet();

    public IndicatorSection(ActivityFormDTO activity) {

        FlowLayout layout = new FlowLayout();
        layout.setMargins(new Margins(5, 5, 5, 5));

        setLayout(layout);
        setScrollMode(Scroll.AUTOY);

        for (IndicatorGroup group : activity.groupIndicators()) {

            if (group.getName() != null) {
                addGroupHeader(group.getName());
            }

            if(allQuantities(group.getIndicators())) {
                addQuantityTable(group);

            } else {
                addMixedGroup(group);
            }
        }

        for (IndicatorDTO indicator : activity.getIndicators()) {
            if(indicator.isCalculated() && indicator.isVisible()) {
                calculatedIndicators.add(indicator);
            }
        }
    }

    private boolean allQuantities(List<IndicatorDTO> indicators) {
        for (IndicatorDTO indicator : indicators) {
            if(!indicator.isCalculated()) {
                if (indicator.getType() != FieldTypeClass.QUANTITY) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addGroupHeader(String name) {

        Text header = new Text(name);
        header.setStyleAttribute("fontSize", "9pt");
        header.setStyleAttribute("fontWeight", "bold");
        header.setStyleAttribute("marginTop", "6pt");

        add(header);
    }

    private void addQuantityTable(IndicatorGroup group) {
        // Layout in three columns
        // Label | Field | Units

        TableData fieldLayout = new TableData();
        fieldLayout.setWidth(NUMBER_FIELD_WIDTH + "px");
        fieldLayout.setVerticalAlign(Style.VerticalAlignment.TOP);

        TableData unitLayout = new TableData();
        unitLayout.setWidth(UNITS_FIELD_WIDTH + "px");
        unitLayout.setVerticalAlign(Style.VerticalAlignment.TOP);

        TableLayout layout = new TableLayout();
        layout.setWidth("100%");
        layout.setColumns(3);
        layout.setCellPadding(5);

        LayoutContainer table = new LayoutContainer();
        table.setLayout(layout);
        table.setAutoHeight(true);

        for (IndicatorDTO indicator : group.getIndicators()) {
            if(!indicator.isCalculated() && indicator.isVisible()) {
                Text fieldLabel = createLabel(indicator);

                Field field = createField(indicator);
                field.setWidth(NUMBER_FIELD_WIDTH);

                Text unitLabel = new Text(indicator.getUnits());
                unitLabel.setWidth(UNITS_FIELD_WIDTH);
                unitLabel.setStyleAttribute("fontSize", "9pt");

                table.add(fieldLabel);
                table.add(field, fieldLayout);
                table.add(unitLabel, unitLayout);
            }
        }

        add(table);
    }


    private void addMixedGroup(IndicatorGroup group) {

        // Layout in two rows
        // Field Label
        // Field Widget

        for (IndicatorDTO indicator : group.getIndicators()) {
            if(!indicator.isCalculated() && indicator.isVisible()) {

                Text fieldLabel = createLabel(indicator);
                fieldLabel.setStyleAttribute("marginTop", "8px");
                fieldLabel.setStyleAttribute("marginBottom", "3px");
                add(fieldLabel);

                TextField field = createField(indicator);

                if(indicator.getType() == FieldTypeClass.QUANTITY) {

                    HBoxLayout rowLayout = new HBoxLayout();
                    rowLayout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);

                    Text unitsLabel = new Text(indicator.getUnits());
                    unitsLabel.setStyleAttribute("paddingLeft", "5px");

                    LayoutContainer row = new LayoutContainer();
                    row.setLayout(rowLayout);
                    row.add(field);
                    row.add(unitsLabel);
                    add(row);
                } else {
                    field.setWidth(TEXT_FIELD_WIDTH);
                    add(field);
                }
            }
        }
    }


    private TextField createField(IndicatorDTO indicator) {
        TextField field;
        if(indicator.getType() == FieldTypeClass.NARRATIVE) {
            field = new TextArea();
        } else if(indicator.getType() == FieldTypeClass.QUANTITY) {
            field = createQuantityField();
        } else {
            field = new TextField();
        }
        field.setName(indicator.getPropertyName());
        field.setAllowBlank(!indicator.isMandatory());
        field.setToolTip(toolTip(indicator));

        indicatorFields.add(field);

        return field;
    }


    private NumberField createQuantityField() {
        NumberField numberField = new NumberField();
        numberField.setFormat(IndicatorNumberFormat.INSTANCE);
        numberField.setWidth(NUMBER_FIELD_WIDTH);
        numberField.setStyleAttribute("textAlign", "right");
        return numberField;
    }

    private Text createLabel(IndicatorDTO indicator) {
        String name = indicator.getName();
        if (indicator.isMandatory()) {
            name += " *";
        }
        Text indicatorLabel = new Text(name);
        indicatorLabel.setStyleAttribute("fontSize", "9pt");
        return indicatorLabel;
    }


    private ToolTipConfig toolTip(IndicatorDTO indicator) {
        if (Strings.isNullOrEmpty(indicator.getDescription())) {
            return null;
        }

        ToolTipConfig tip = new ToolTipConfig();
        tip.setDismissDelay(0);
        tip.setShowDelay(100);
        tip.setText(indicator.getDescription());
        return tip;
    }

    @Override
    public boolean validate() {
        boolean valid = true;
        for (Field field : indicatorFields) {
            valid &= field.validate();
        }
        return valid;
    }

    @Override
    public void updateModel(SiteDTO m) {
        for (Field field : indicatorFields) {
            m.set(field.getName(), field.getValue());
        }
        for (IndicatorDTO indicator : calculatedIndicators) {
            m.remove(indicator.getPropertyName());
        }
    }

    @Override
    public void updateForm(SiteDTO m, boolean isNew) {
        for (Field field : indicatorFields) {
            field.setValue(m.get(field.getName()));
        }
    }

    @Override
    public Component asComponent() {
        return this;
    }
}
