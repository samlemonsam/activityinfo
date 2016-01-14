package org.activityinfo.model.query;

import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.expr.ExprValue;
import org.codehaus.jackson.annotate.JsonSetter;

/**
 * Defines a Column within a query
 */
public class ColumnModel {

    public static final String ID_SYMBOL = "_id";
    public static final String CLASS_SYMBOL = "_class";

    private String id;
    private ExprNode expression;

    /**
     *
     * @return a unique, machine-readable stable id for this column
     * that is used to ensure stable references to other fields or
     * elements in the analysis.
     */
    public String getId() {
        return id;
    }

    public ColumnModel setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the column's id
     *
     * @param id the new id of the column
     */
    public ColumnModel as(String id) {
        return setId(id);
    }

    public ExprNode getExpression() {
        return expression;
    }

    public ColumnModel setExpression(ExprNode expression) {
        this.expression = expression;
        return this;
    }

    public ColumnModel setExpression(ExprValue exprValue) {
        return setExpression(exprValue.getExpression());
    }

    @JsonSetter
    public ColumnModel setExpression(String expression) {
        this.expression = ExprParser.parse(expression);
        return this;
    }

    public ColumnModel setExpression(ResourceId resourceId) {
        return setExpression(new SymbolExpr(resourceId));
    }

    public ColumnModel setExpression(FieldPath path) {
        return setExpression(path.toExpr());
    }

}
