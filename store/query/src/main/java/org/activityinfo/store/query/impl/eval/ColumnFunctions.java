package org.activityinfo.store.query.impl.eval;

import com.google.common.collect.Sets;
import org.activityinfo.model.expr.functions.ExprFunction;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.util.List;
import java.util.Set;

public class ColumnFunctions {

    private static final Set<String> supportedFunctions = Sets.newHashSet("==", "!=", "+", "-", "&&", "||", "*", "/");

    public static boolean isSupported(ExprFunction fn) {
        return supportedFunctions.contains(fn.getId());
    }


    public static ColumnView create(ExprFunction fn, List<ColumnView> arguments) {
        String fnId = fn.getId();
                
        if(fnId.equals("+") ||
           fnId.equals("-") ||
           fnId.equals("*") ||
           fnId.equals("/")) {
           
            return new DoubleBinaryOpView(fnId, arguments.get(0), arguments.get(1));
            
        }  else if(fnId.equals("==")) {
            if (argsMatch(arguments, ColumnType.STRING, ColumnType.STRING)) {
                return new StringComparisonView(arguments.get(0), arguments.get(1), ComparisonOp.EQUALS);
            }
        } else if(fnId.equals("!=")) {
            if(argsMatch(arguments, ColumnType.STRING, ColumnType.STRING)) {
                return new StringComparisonView(arguments.get(0), arguments.get(1), ComparisonOp.NOT_EQUALS);
            }
        } else if(fnId.equals("&&")) {
            if (argsMatch(arguments, ColumnType.BOOLEAN, ColumnType.BOOLEAN)) {
                return new BooleanBinaryOp(BooleanBinaryOp.Operator.AND, arguments.get(0), arguments.get(1));
            }
        } else if(fnId.equals("||")) {
            if (argsMatch(arguments, ColumnType.BOOLEAN, ColumnType.BOOLEAN)) {
                return new BooleanBinaryOp(BooleanBinaryOp.Operator.OR, arguments.get(0), arguments.get(1));
            }
        }
        throw new UnsupportedOperationException("fn" + fn.getId());
    }

    private static boolean argsMatch(List<ColumnView> arguments, ColumnType... argumentTypes) {
        if(arguments.size() != argumentTypes.length) {
            return false;
        }
        for(int i=0;i!=argumentTypes.length;++i) {
            if(arguments.get(i).getType() != argumentTypes[i]) {
                return false;
            }
        }
        return true;
    }
}
