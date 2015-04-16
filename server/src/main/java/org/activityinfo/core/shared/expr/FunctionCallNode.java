package org.activityinfo.core.shared.expr;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class FunctionCallNode extends ExprNode {

    @Nonnull
    private ExprFunction function;

    @Nonnull
    private List<ExprNode> arguments;

    public FunctionCallNode(ExprFunction function, List<ExprNode> arguments) {
        super();
        this.function = function;
        this.arguments = arguments;
    }

    public FunctionCallNode(ExprFunction function, ExprNode... arguments) {
        this(function, Arrays.asList(arguments));
    }

    @Nonnull
    public List<ExprNode> getArguments() {
        return arguments;
    }

    @Override
    public double evalReal() {
        return function.applyReal(arguments);
    }

    @Override
    public String toString() {
        return arguments.get(0) + " " + function.getName() + " " + arguments.get(1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((arguments == null) ? 0 : arguments.hashCode());
        result = prime * result
                + ((function == null) ? 0 : function.hashCode());
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
