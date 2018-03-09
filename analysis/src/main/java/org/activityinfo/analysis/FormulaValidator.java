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

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.*;
import org.activityinfo.model.formula.diagnostic.ArgumentException;
import org.activityinfo.model.formula.diagnostic.FormulaException;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
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

    public boolean validate(FormulaNode rootNode) {
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

    private FieldType validateExpr(FormulaNode formulaNode) {
        if(formulaNode instanceof ConstantNode) {
            return ((ConstantNode) formulaNode).getType();
        } else if(formulaNode instanceof GroupNode) {
            return validateExpr(((GroupNode) formulaNode).getExpr());
        } else if(formulaNode instanceof SymbolNode) {
            return validateReference(formulaNode);
        } else if(formulaNode instanceof CompoundExpr) {
            return validateReference(formulaNode);
        } else if(formulaNode instanceof FunctionCallNode) {
            return validateFunctionCall((FunctionCallNode) formulaNode);
        } else {
            throw new UnsupportedOperationException("type: " + formulaNode.getClass().getSimpleName());
        }
    }

    private FieldType validateReference(FormulaNode formulaNode) {
        Collection<NodeMatch> matches;
        try {
            if (formulaNode instanceof SymbolNode) {
                matches = nodeMatcher.resolveSymbol((SymbolNode) formulaNode);
            } else if (formulaNode instanceof CompoundExpr) {
                matches = nodeMatcher.resolveCompoundExpr((CompoundExpr) formulaNode);
            } else {
                throw new IllegalArgumentException();
            }
        } catch (FormulaException e) {
            errors.add(new FormulaError(formulaNode, e.getMessage()));
            throw new ValidationFailed();
        }

        if(matches.isEmpty()) {
            errors.add(new FormulaError(formulaNode, "Invalid field reference"));
            throw new ValidationFailed();
        }

        for (NodeMatch match : matches) {
            references.add(new FieldReference(formulaNode.getSourceRange(), match));
        }

        NodeMatch match = matches.iterator().next();

        if(match.isEnumBoolean()) {
            return BooleanType.INSTANCE;
        } else if(match.isCalculated()) {
            return findCalculatedFieldType(match.getFieldNode());
        } else if (match.isRootId()) {
            return TextType.SIMPLE;
        } else {
            return match.getFieldNode().getType();
        }
    }

    private FieldType findCalculatedFieldType(FormTree.Node fieldNode) {

        CalculatedFieldType fieldType = (CalculatedFieldType) fieldNode.getType();
        FormulaNode rootNode;
        try {
            rootNode = FormulaParser.parse(fieldType.getExpression());
        } catch (FormulaException e) {
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

        List<FormulaNode> arguments = call.getArguments();
        List<FieldType> argumentTypes = new ArrayList<>();
        boolean validationFailed = false;

        for (FormulaNode formulaNode : arguments) {
            try {
                argumentTypes.add(validateExpr(formulaNode));
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
        } catch(FormulaException e) {
            errors.add(new FormulaError(call, e.getMessage()));
            throw new ValidationFailed();
        }
    }

    public List<FormulaError> getErrors() {
        return errors;
    }


}
