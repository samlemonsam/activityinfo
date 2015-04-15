package org.activityinfo.model.query;

import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.expr.ExprValue;

/**
 * Defines a Column within a query
 */
public class ColumnModel {

    public static final String ID_SYMBOL = "_id";
    public static final String CLASS_SYMBOL = "_class";

    private String id;
    private ExprValue expression;

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

    public ExprValue getExpression() {
        return expression;
    }

    public ColumnModel setExpression(ExprValue exprValue) {
        this.expression = exprValue;
        return this;
    }

    public ColumnModel setExpression(String expression) {
        this.expression = new ExprValue(expression);
        return this;
    }

    public ColumnModel setExpression(ResourceId resourceId) {
        return setExpression(resourceId.asString());
    }

    public ColumnModel setExpression(FieldPath expression) {
        StringBuilder sb = new StringBuilder();
        for (ResourceId fieldId : expression.getPath()) {
            if(sb.length() > 0) {
                sb.append(".");
            }
            sb.append(fieldId.asString());
        }
        return setExpression(sb.toString());
    }

}
