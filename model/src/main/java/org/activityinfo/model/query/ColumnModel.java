package org.activityinfo.model.query;

import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.expr.diagnostic.ExprSyntaxException;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.resource.ResourceId;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;

import static org.activityinfo.json.Json.createObject;

/**
 * Defines a Column within a query
 */
public class ColumnModel implements JsonSerializable {

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
            return expression.asExpression();
        }
    }

    public ColumnModel setExpression(ExprNode expression) {
        this.expression = expression;
        return this;
    }

    @JsonSetter
    public ColumnModel setExpression(String expression) {
        try {
            this.expression = ExprParser.parse(expression);
        } catch (ExprSyntaxException e) {
            throw new ColumnModelException("Invalid column expression '" + expression + "': " + e.getMessage(), e);
        }
        return this;
    }

    public ColumnModel setExpression(ResourceId resourceId) {
        return setExpression(new SymbolExpr(resourceId));
    }

    public ColumnModel setExpression(FieldPath path) {
        return setExpression(path.toExpr());
    }

    @Deprecated
    public JsonValue toJsonElement() {
        return toJson();
    }

    public JsonValue toJson() {
        JsonValue object = createObject();
        object.put("id", id);
        object.put("expression", getExpressionAsString());
        return object;
    }

    public static ColumnModel fromJson(JsonValue object) {
        ColumnModel columnModel = new ColumnModel();
        if(!object.get("id").isJsonNull()) {
            columnModel.setId(object.get("id").asString());
        }
        if(object.get("expression").isJsonPrimitive()) {
            columnModel.setExpression(object.get("expression").asString());
        }
        return columnModel;
    }

}
