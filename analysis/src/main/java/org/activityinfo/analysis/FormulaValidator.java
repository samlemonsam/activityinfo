package org.activityinfo.analysis;

import org.activityinfo.model.expr.*;
import org.activityinfo.model.expr.diagnostic.ArgumentException;
import org.activityinfo.model.expr.diagnostic.ExprException;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.store.query.shared.NodeMatch;
import org.activityinfo.store.query.shared.NodeMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FormulaValidator {


    private class ValidationFailed extends RuntimeException {}

    private final NodeMatcher nodeMatcher;
    private FormTree formTree;

    private List<FormulaError> errors = new ArrayList<>();
    private List<FieldReference> references = new ArrayList<>();

    private boolean valid;
    private boolean simpleReference = true;

    private FieldType resultType;

    public FormulaValidator(FormTree formTree) {
        this.formTree = formTree;
        this.nodeMatcher = new NodeMatcher(formTree);
    }

    public boolean validate(ExprNode rootNode) {
        valid = true;
        try {
            resultType = validateExpr(rootNode);
        } catch (ValidationFailed e) {
            valid = false;
        }
        return valid;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isSimpleReference() {
        return simpleReference;
    }

    public FieldType getResultType() {
        return resultType;
    }

    public List<FieldReference> getReferences() {
        return references;
    }

    private FieldType validateExpr(ExprNode exprNode) {
        if(exprNode instanceof ConstantExpr) {
            return ((ConstantExpr) exprNode).getType();
        } else if(exprNode instanceof GroupExpr) {
            return validateExpr(exprNode);
        } else if(exprNode instanceof SymbolExpr) {
            return validateReference(exprNode);
        } else if(exprNode instanceof CompoundExpr) {
            return validateReference(exprNode);
        } else if(exprNode instanceof FunctionCallNode) {
            return validateFunctionCall((FunctionCallNode) exprNode);
        } else {
            throw new UnsupportedOperationException("type: " + exprNode.getClass().getSimpleName());
        }
    }


    private FieldType validateReference(ExprNode exprNode) {
        Collection<NodeMatch> matches;
        try {
            if (exprNode instanceof SymbolExpr) {
                matches = nodeMatcher.resolveSymbol((SymbolExpr) exprNode);
            } else if (exprNode instanceof CompoundExpr) {
                matches = nodeMatcher.resolveCompoundExpr((CompoundExpr) exprNode);
            } else {
                throw new IllegalArgumentException();
            }
        } catch (ExprException e) {
            errors.add(new FormulaError(exprNode, e.getMessage()));
            throw new ValidationFailed();
        }

        if(matches.isEmpty()) {
            errors.add(new FormulaError(exprNode, "Invalid field reference"));
            throw new ValidationFailed();
        }

        for (NodeMatch match : matches) {
            references.add(new FieldReference(exprNode.getSourceRange(), match));
        }

        NodeMatch match = matches.iterator().next();

        if(match.isEnumBoolean()) {
            return BooleanType.INSTANCE;
        } else if(match.getFieldNode().getType() instanceof CalculatedFieldType) {
            return findCalculatedFieldType(match.getFieldNode());
        } else {
            return match.getFieldNode().getType();
        }
    }

    private FieldType findCalculatedFieldType(FormTree.Node fieldNode) {

        CalculatedFieldType fieldType = (CalculatedFieldType) fieldNode.getType();
        ExprNode rootNode;
        try {
            rootNode = ExprParser.parse(fieldType.getExpression());
        } catch (ExprException e) {
            throw new ValidationFailed();
        }

        FormClass formClass = fieldNode.getDefiningFormClass();
        FormTree subTree = formTree.subTree(formClass.getId());
        FormulaValidator validator = new FormulaValidator(subTree);

        if (!validator.validate(rootNode)) {
            throw new ValidationFailed();
        }

        return validator.getResultType();
    }


    private FieldType validateFunctionCall(FunctionCallNode call) {

        // If function calls are involved, this is no longer
        // a simple field reference
        this.simpleReference = false;

        List<ExprNode> arguments = call.getArguments();
        List<FieldType> argumentTypes = new ArrayList<>();
        boolean validationFailed = false;

        for (ExprNode exprNode : arguments) {
            try {
                argumentTypes.add(validateExpr(exprNode));
            } catch (ValidationFailed e) {
                // Continue validating the other arguments.
                validationFailed = true;
            }
        }

        if(validationFailed) {
            throw new ValidationFailed();
        }

        try {
            return call.getFunction().resolveResultType(argumentTypes);
        } catch (ArgumentException e) {
            errors.add(new FormulaError(arguments.get(e.getArgumentIndex()).getSourceRange(), e.getMessage()));
            throw new ValidationFailed();
        } catch(ExprException e) {
            errors.add(new FormulaError(call, e.getMessage()));
            throw new ValidationFailed();
        }
    }

    public List<FormulaError> getErrors() {
        return errors;
    }


}
