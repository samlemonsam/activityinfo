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
package org.activityinfo.analysis;

import com.google.common.base.Strings;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.FormulaError;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.formula.diagnostic.FormulaException;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.store.query.shared.NodeMatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Formula parsed and validated
 */
public class ParsedFormula {

    private String formula;
    private boolean valid;
    private FormulaNode rootNode;
    private FieldType resultType;

    /**
     * True if this refers to a single field
     */
    private boolean simpleReference = true;


    private List<FormulaError> errors = new ArrayList<>();
    private List<FieldReference> references = new ArrayList<>();

    public ParsedFormula(FormTree tree, String formula) {
        this.formula = formula;
        valid = true;

        if (Strings.isNullOrEmpty(formula)) {
            errors.add(new FormulaError("The formula is empty."));
            valid = false;
        }
        try {
            rootNode = FormulaParser.parse(formula);
        } catch (FormulaException e) {
            errors.add(new FormulaError(e.getSourceRange(), e.getMessage()));
            valid = false;
        }

        if(valid) {
            FormulaValidator validator = new FormulaValidator(tree);
            validator.validate(rootNode);
            valid = validator.isValid();
            errors.addAll(validator.getErrors());
            this.resultType = validator.getResultType();
            this.references = validator.getReferences();
            this.simpleReference = validator.isSimpleReference();
        }
    }

    public boolean isValid() {
        return valid;
    }

    public String getFormula() {
        return formula;
    }

    public FormulaNode getRootNode() {
        return rootNode;
    }

    public List<FormulaError> getErrors() {
        return errors;
    }

    public FieldType getResultType() {
        assert resultType != null : "Formula " + formula + " is not valid, and has no type.";
        return resultType;
    }

    public String getErrorMessage() {
        assert !valid;
        if(errors.size() == 1) {
            return errors.get(0).getMessage();
        } else {
            return I18N.CONSTANTS.calculationExpressionIsInvalid();
        }
    }

    public List<FieldReference> getReferences() {
        return references;
    }

    public String getLabel() {
        // If this formula is just a field reference
        if(simpleReference && references.size() == 1) {
            NodeMatch ref = references.get(0).getMatch();
            if(ref.isEnumBoolean()) {
                return ref.getEnumItem().getLabel();
            } else if(ref.getType() == NodeMatch.Type.FIELD) {
                FormTree.Node field = ref.getFieldNode();
                if (field.isRoot()) {
                    return field.getField().getLabel();
                } else {
                    return field.getDefiningFormClass().getLabel() + " " + field.getField().getLabel();
                }
            }
        }
        return formula;
    }
}
