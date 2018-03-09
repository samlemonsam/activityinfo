package org.activityinfo.model.formula;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.activityinfo.model.formula.eval.EvalContext;
import org.activityinfo.model.formula.functions.FormulaFunction;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FunctionCallNode extends FormulaNode {

    @Nonnull
    private FormulaFunction function;

    @Nonnull
    private List<FormulaNode> arguments;

    public FunctionCallNode(FormulaFunction function, List<FormulaNode> arguments, SourceRange sourceRange) {
        super();
        this.function = function;
        this.arguments = arguments;
        this.sourceRange = sourceRange;
    }
    public FunctionCallNode(FormulaFunction function, List<FormulaNode> arguments) {
        this(function, arguments, null);
    }

    public FunctionCallNode(FormulaFunction function, FormulaNode... arguments) {
        this(function, Arrays.asList(arguments));
    }

    @Override
    public FieldValue evaluate(EvalContext context) {
        List<FieldValue> evaluatedArguments = Lists.newArrayList();
        for (FormulaNode expr : arguments) {
            evaluatedArguments.add(expr.evaluate(context));
        }
        return function.apply(evaluatedArguments);
    }

    @Override
    public FieldType resolveType(EvalContext context) {
        List<FieldType> argumentTypes = Lists.newArrayList();
        for (FormulaNode expr : arguments) {
            argumentTypes.add(expr.resolveType(context));
        }
        return function.resolveResultType(argumentTypes);
    }

    @Nonnull
    public FormulaFunction getFunction() {
        return function;
    }

    @Nonnull
    public List<FormulaNode> getArguments() {
        return arguments;
    }

    public int getArgumentCount() {
        return arguments.size();
    }


    public FormulaNode getArgument(int i) {
        return arguments.get(i);
    }

    @Override
    public String toString() {
        return "(" + function.getId() + " " + Joiner.on(" ").join(arguments) + ")";
    }

    @Override
    public String asExpression() {
        if(function.isInfix() && arguments.size() == 2) {
                return arguments.get(0).asExpression() + " " + function.getId() + " " + arguments.get(1).asExpression();
        }
        StringBuilder expr = new StringBuilder();
        expr.append(function.getId());
        expr.append("(");

        boolean needsComma = false;
        for (FormulaNode arg : arguments) {
            if(needsComma) {
                expr.append(", ");
            }
            expr.append(arg.asExpression());
            needsComma = true;
        }
        expr.append(")");

        return expr.toString();
    }
    
    @Override
    public <T> T accept(FormulaVisitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }

    @Override
    public FormulaNode transform(Function<FormulaNode, FormulaNode> function) {
        List<FormulaNode> transformedArgs = new ArrayList<>();
        for (FormulaNode argument : arguments) {
            transformedArgs.add(argument.transform(function));
        }
        return function.apply(new FunctionCallNode(this.function, transformedArgs));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (arguments.hashCode());
        result = prime * result + (function.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FunctionCallNode other = (FunctionCallNode) obj;
        return other.function.equals(function) && other.arguments.equals(arguments);
    }


}
