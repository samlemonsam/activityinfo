package org.activityinfo.model.query;

import com.google.gson.JsonObject;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.resource.ResourceId;
import org.codehaus.jackson.annotate.JsonProperty;
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

    @JsonProperty("expression")
    public String getExpressionAsString() {
        if(expression == null) {
            return null;
        } else {
            return expression.toString();
        }
    }

    public ColumnModel setExpression(ExprNode expression) {
        this.expression = expression;
        return this;
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


    public JsonObject toJsonElement() {
        JsonObject object = new JsonObject();
        object.addProperty("id", id);
        object.addProperty("expression", getExpressionAsString());
        return object;
    }

    public static ColumnModel fromJson(JsonObject object) {
        ColumnModel columnModel = new ColumnModel();
        if(!object.get("id").isJsonNull()) {
            columnModel.setId(object.get("id").getAsString());
        }
        columnModel.setExpression(object.get("expression").getAsString());
        return columnModel;
    }

}
