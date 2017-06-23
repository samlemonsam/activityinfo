package org.activityinfo.model.job;

import org.activityinfo.json.JsonObject;
import org.activityinfo.model.expr.ExprNode;

import static org.activityinfo.json.Json.createObject;

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

    public org.activityinfo.json.JsonObject toJsonObject() {
        JsonObject object = createObject();
        object.put("formula", formula);
        return object;
    }

    public static ExportColumn fromJson(org.activityinfo.json.JsonObject object) {
        return new ExportColumn(object.get("formula").asString());
    }
}
