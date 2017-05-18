package org.activityinfo.analysis;

import com.google.common.base.Strings;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.FormulaError;
import org.activityinfo.model.expr.diagnostic.ExprException;
import org.activityinfo.model.formTree.FormTree;
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
    private ExprNode rootNode;
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
            rootNode = ExprParser.parse(formula);
        } catch (ExprException e) {
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

    public ExprNode getRootNode() {
        return rootNode;
    }

    public List<FormulaError> getErrors() {
        return errors;
    }

    public FieldType getResultType() {
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
