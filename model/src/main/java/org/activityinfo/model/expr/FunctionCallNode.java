package org.activityinfo.model.expr;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.activityinfo.model.expr.eval.EvalContext;
import org.activityinfo.model.expr.functions.ExprFunction;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FunctionCallNode extends ExprNode {

    @Nonnull
    private ExprFunction function;

    @Nonnull
    private List<ExprNode> arguments;

    public FunctionCallNode(ExprFunction function, List<ExprNode> arguments, SourceRange sourceRange) {
        super();
        this.function = function;
        this.arguments = arguments;
        this.sourceRange = sourceRange;
    }
    public FunctionCallNode(ExprFunction function, List<ExprNode> arguments) {
        this(function, arguments, null);
    }

    public FunctionCallNode(ExprFunction function, ExprNode... arguments) {
        this(function, Arrays.asList(arguments));
    }

    @Override
    public FieldValue evaluate(EvalContext context) {
        List<FieldValue> evaluatedArguments = Lists.newArrayList();
        for (ExprNode expr : arguments) {
            evaluatedArguments.add(expr.evaluate(context));
        }
        return function.apply(evaluatedArguments);
    }

    @Override
    public FieldType resolveType(EvalContext context) {
        List<FieldType> argumentTypes = Lists.newArrayList();
        for (ExprNode expr : arguments) {
            argumentTypes.add(expr.resolveType(context));
        }
        return function.resolveResultType(argumentTypes);
    }

    @Nonnull
    public ExprFunction getFunction() {
        return function;
    }

    @Nonnull
    public List<ExprNode> getArguments() {
        return arguments;
    }

    public int getArgumentCount() {
        return arguments.size();
    }


    public ExprNode getArgument(int i) {
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
        String argumentString = "";
        for (ExprNode arg : arguments) {
            argumentString += arg.asExpression();
            if (!arg.equals(arguments.get(arguments.size() - 1))) { // add comma if not last element
                argumentString += ", ";
            }
        }
        return function.getId() + "(" + argumentString + ")";
    }
    
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }

    @Override
    public ExprNode transform(Function<ExprNode, ExprNode> function) {
        List<ExprNode> transformedArgs = new ArrayList<>();
        for (ExprNode argument : arguments) {
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
