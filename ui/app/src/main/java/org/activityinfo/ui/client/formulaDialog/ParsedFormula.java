package org.activityinfo.ui.client.formulaDialog;

import com.google.common.base.Strings;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.SourceRange;
import org.activityinfo.model.expr.diagnostic.ExprException;
import org.activityinfo.model.formTree.FormTree;

/**
 * Formula parsed and validated
 */
public class ParsedFormula {

    private String formula;
    private boolean valid;
    private ExprNode rootNode;

    private String errorMessage;
    private SourceRange errorRange;

    public ParsedFormula(FormTree tree, String formula) {
        this.formula = formula;
        valid = true;

        if (Strings.isNullOrEmpty(formula)) {
            errorMessage = "The formula is empty.";
            valid = false;
        }
        try {
            rootNode = ExprParser.parse(formula);
        } catch (ExprException e) {
            errorMessage = e.getMessage();
            errorRange = e.getSourceRange();
            valid = false;
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isValid() {
        return valid;
    }

    public ExprNode getRootNode() {
        return rootNode;
    }

    public SourceRange getErrorRange() {
        return errorRange;
    }
}
