package org.activityinfo.model.job;

import com.google.gson.JsonObject;
import org.activityinfo.model.expr.ExprNode;

/**
 * Defines a column to be included in an export
 */
public class ExportColumn {
    private String formula;

    public ExportColumn(String formula) {
        this.formula = formula;
    }

    public ExportColumn(ExprNode exprNode) {
        this.formula = exprNode.asExpression();
    }

    public String getFormula() {
        return formula;
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("formula", formula);
        return object;
    }

    public static ExportColumn fromJson(JsonObject object) {
        return new ExportColumn(object.get("formula").getAsString());
    }
}
