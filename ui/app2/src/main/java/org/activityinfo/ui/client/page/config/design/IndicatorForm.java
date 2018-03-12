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
package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.form.*;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.i18n.shared.UiConstants;
import org.activityinfo.legacy.shared.model.EntityDTO;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formula.*;
import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.TypeRegistry;
import org.activityinfo.ui.client.page.config.form.AbstractDesignForm;
import org.activityinfo.ui.client.widget.legacy.MappingComboBox;
import org.activityinfo.ui.client.widget.legacy.MappingComboBoxBinding;
import org.activityinfo.ui.client.widget.legacy.OnlyValidFieldBinding;

import java.util.List;

import static org.activityinfo.legacy.shared.model.EntityDTO.NAME_PROPERTY;

@SuppressWarnings("squid:MaximumInheritanceDepth")
class IndicatorForm extends AbstractDesignForm {

    private final UiConstants constants = GWT.create(UiConstants.class);

    private final FormBinding binding;
    private final MappingComboBox<String> typeCombo;
    private final TextField<String> unitsField;
    private final MappingComboBox aggregationCombo;
    private final TextField<String> expressionField;
    private final TextField<String> codeField;
    private final NumberField idField;
    private final LabelField calculatedExpressionLabelDesc;
    private final CheckBox calculateAutomatically;

    public IndicatorForm() {
        super();

        binding = new FormBinding(this);


        this.setLabelWidth(150);
        this.setFieldWidth(200);

        idField = new NumberField();
        idField.setFieldLabel("ID");
        idField.setReadOnly(true);
        binding.addFieldBinding(new FieldBinding(idField, EntityDTO.ID_PROPERTY));
        add(idField);

        codeField = new TextField<>();
        codeField.setFieldLabel(constants.codeFieldLabel());
        codeField.setToolTip(constants.codeFieldLabel());
        codeField.setValidator((field, value) -> {
            if (!Strings.isNullOrEmpty(value) && !FormField.isValidCode(value)) {
                return constants.invalidCodeMessage();
            }
            return null;
        });
        binding.addFieldBinding(new OnlyValidFieldBinding(codeField, IndicatorDTO.CODE_PROPERTY));
        this.add(codeField);

        this.add(new LabelField(constants.nameInExpressionTooltip()));

        TextField<String> nameField = new TextField<>();
        nameField.setFieldLabel(constants.name());
        nameField.setAllowBlank(false);
        nameField.setValidator(new BlankValidator());

        binding.addFieldBinding(new OnlyValidFieldBinding(nameField, NAME_PROPERTY));
        this.add(nameField);

        typeCombo = new MappingComboBox<>();
        typeCombo.setFieldLabel(constants.type());
        typeCombo.add(FieldTypeClass.QUANTITY.getId(), I18N.CONSTANTS.fieldTypeQuantity());
        typeCombo.add(FieldTypeClass.FREE_TEXT.getId(), I18N.CONSTANTS.fieldTypeText());
        typeCombo.add(FieldTypeClass.NARRATIVE.getId(), I18N.CONSTANTS.fieldTypeNarrative());

        binding.addFieldBinding(new MappingComboBoxBinding(typeCombo, IndicatorDTO.TYPE_PROPERTY));
        this.add(typeCombo);

        unitsField = new TextField<>();
        unitsField.setName("units");
        unitsField.setFieldLabel(constants.units());
        unitsField.setAllowBlank(false);
        unitsField.setMaxLength(IndicatorDTO.UNITS_MAX_LENGTH);
        binding.addFieldBinding(new OnlyValidFieldBinding(unitsField, IndicatorDTO.UNITS_PROPERTY));
        this.add(unitsField);

        aggregationCombo = new MappingComboBox<Integer>();
        aggregationCombo.setFieldLabel(constants.aggregationMethod());
        aggregationCombo.add(IndicatorDTO.AGGREGATE_SUM, constants.sum());
        aggregationCombo.add(IndicatorDTO.AGGREGATE_AVG, constants.average());
        aggregationCombo.add(IndicatorDTO.AGGREGATE_SITE_COUNT, constants.siteCount());
        binding.addFieldBinding(new MappingComboBoxBinding(aggregationCombo, IndicatorDTO.AGGREGATION_PROPERTY));
        this.add(aggregationCombo);

        this.add(new LabelField("Please note: text and narrative indicators are not yet " +
                                "available for activities with monthly reporting. " +
                                "(We're working on it!)"));

        typeCombo.addSelectionChangedListener(new SelectionChangedListener<MappingComboBox.Wrapper<String>>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<MappingComboBox.Wrapper<String>> wrapperSelectionChangedEvent) {
                setState();
            }
        });

        calculateAutomatically = new CheckBox();
        calculateAutomatically.setFieldLabel(constants.calculateAutomatically());
        binding.addFieldBinding(new FieldBinding(calculateAutomatically, "calculatedAutomatically"));
        this.add(calculateAutomatically);

        expressionField = new TextField<>();
        expressionField.addKeyListener(new KeyListener() {
            @Override
            public void componentKeyUp(ComponentEvent event) {
                expressionField.validate();
            }
        });
        expressionField.setFieldLabel(constants.calculation());
        expressionField.setToolTip(constants.calculatedIndicatorExplanation());
        expressionField.setValidator((field, value) -> validateExpression(value));
        binding.addFieldBinding(new OnlyValidFieldBinding(expressionField, IndicatorDTO.EXPRESSION_PROPERTY));
        this.add(expressionField);

        calculateAutomatically.addListener(Events.Change,
                event -> expressionField.setEnabled(calculateAutomatically.getValue()));

        calculatedExpressionLabelDesc = new LabelField(constants.calculatedIndicatorExplanation());
        this.add(calculatedExpressionLabelDesc);


        TextField<String> categoryField = new TextField<>();
        categoryField.setName("category");
        categoryField.setFieldLabel(constants.category());
        categoryField.setMaxLength(IndicatorDTO.MAX_CATEGORY_LENGTH);
        binding.addFieldBinding(new OnlyValidFieldBinding(categoryField, IndicatorDTO.CATEGORY_PROPERTY));
        this.add(categoryField);


        TextField<String> listHeaderField = new TextField<>();
        listHeaderField.setFieldLabel(constants.listHeader());
        listHeaderField.setMaxLength(IndicatorDTO.MAX_LIST_HEADER_LENGTH);
        binding.addFieldBinding(new OnlyValidFieldBinding(listHeaderField, IndicatorDTO.LIST_HEADER_PROPERTY));
        this.add(listHeaderField);

        TextArea descField = new TextArea();
        descField.setFieldLabel(constants.description());
        binding.addFieldBinding(new OnlyValidFieldBinding(descField, IndicatorDTO.DESCRIPTION_PROPERTY));
        this.add(descField);

        CheckBox mandatoryCB = new CheckBox();
        mandatoryCB.setFieldLabel(constants.mandatory());
        binding.addFieldBinding(new FieldBinding(mandatoryCB, IndicatorDTO.MANDATORY_PROPERTY));
        this.add(mandatoryCB);

        CheckBox visibleCB = new CheckBox();
        visibleCB.setFieldLabel(constants.showInDataEntry());
        binding.addFieldBinding(new FieldBinding(visibleCB, IndicatorDTO.VISIBLE_PROPERTY));
        this.add(visibleCB);

        hideFieldWhenNull(idField);

        binding.addListener(Events.Bind, be -> setState());

    }

    private String validateExpression(String value) {
        try {
            FormulaLexer lexer = new FormulaLexer(value);
            FormulaParser parser = new FormulaParser(lexer);
            FormulaNode expr = parser.parse();

            // expr node is created, expression is parsable
            // try to check variable names
            List<SymbolNode> placeholderExprList = Lists.newArrayList();
            gatherPlaceholderExprs(expr, placeholderExprList);
            List<String> existingIndicatorCodes = existingIndicatorCodes();
            for (SymbolNode placeholderExpr : placeholderExprList) {
                if (!existingIndicatorCodes.contains(placeholderExpr.getName())) {
                    return I18N.MESSAGES.doesNotExist(placeholderExpr.getName());
                }
            }
            return null;
        } catch (FormulaSyntaxException e) {
            return e.getMessage();
        } catch (Exception ignored) {
            return constants.calculationExpressionIsInvalid();
        }
    }

    private void gatherPlaceholderExprs(FormulaNode node, List<SymbolNode> placeholderExprList) {
        if (node instanceof SymbolNode) {
            placeholderExprList.add((SymbolNode) node);
        } else if (node instanceof FunctionCallNode) {
            FunctionCallNode functionCallNode = (FunctionCallNode) node;
            List<FormulaNode> arguments = functionCallNode.getArguments();
            for (FormulaNode arg : arguments) {
                gatherPlaceholderExprs(arg, placeholderExprList);
            }
        }
    }

    private List<String> existingIndicatorCodes() {
        final List<String> result = Lists.newArrayList();
        List models = IndicatorForm.this.getBinding().getStore().getModels();
        for (Object model : models) {
            if (model instanceof IndicatorDTO) {
                IndicatorDTO indicatorDTO = (IndicatorDTO) model;
                result.add(CuidAdapter.indicatorField(indicatorDTO.getId()).asString());

                String nameInExpression = indicatorDTO.getNameInExpression();
                if (!Strings.isNullOrEmpty(nameInExpression)) {
                    result.add(nameInExpression);
                }
            }
        }
        return result;
    }

    private void setState() {
        if (typeCombo.getValue() != null) {
            FieldTypeClass selectedType = TypeRegistry.get().getTypeClass(typeCombo.getValue().getWrappedValue());

            unitsField.setVisible(selectedType == FieldTypeClass.QUANTITY);
            unitsField.setAllowBlank(selectedType != FieldTypeClass.QUANTITY);
            if (selectedType != FieldTypeClass.QUANTITY) {
                unitsField.setValue("");
            }

            calculatedExpressionLabelDesc.setEnabled(selectedType == FieldTypeClass.QUANTITY);
            calculateAutomatically.setEnabled(selectedType == FieldTypeClass.QUANTITY);
            expressionField.setEnabled(selectedType == FieldTypeClass.QUANTITY && calculateAutomatically.getValue());

            aggregationCombo.setVisible(selectedType == FieldTypeClass.QUANTITY);

        }
    }

    @Override
    public FormBinding getBinding() {
        return binding;
    }
}
