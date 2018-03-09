package org.activityinfo.model.query;

import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;
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
    public static final String PARENT_SYMBOL = "@parent";


    private String id;
    private FormulaNode formula;

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

    public FormulaNode getFormula() {
        return formula;
    }

    @JsonProperty("formula")
    public String getFormulaAsString() {
        if(formula == null) {
            return null;
        } else {
            return formula.asExpression();
        }
    }

    public ColumnModel setFormula(FormulaNode formula) {
        this.formula = formula;
        return this;
    }

    @JsonSetter
    public ColumnModel setFormula(String formula) {
        try {
            this.formula = FormulaParser.parse(formula);
        } catch (FormulaSyntaxException e) {
            throw new ColumnModelException("Invalid column expression '" + formula + "': " + e.getMessage(), e);
        }
        return this;
    }

    public ColumnModel setFormula(ResourceId resourceId) {
        return setFormula(new SymbolNode(resourceId));
    }

    public ColumnModel setFormula(FieldPath path) {
        return setFormula(path.toExpr());
    }

    @Deprecated
    public JsonValue toJsonElement() {
        return toJson();
    }

    public JsonValue toJson() {
        JsonValue object = createObject();
        object.put("id", id);
        object.put("expression", getFormulaAsString());
        return object;
    }

    public static ColumnModel fromJson(JsonValue object) {
        ColumnModel columnModel = new ColumnModel();
        if(!object.get("id").isJsonNull()) {
            columnModel.setId(object.get("id").asString());
        }
        if(object.hasKey("expression")) {
            columnModel.setFormula(object.getString("expression"));
        }
        if(object.hasKey("formula")) {
            columnModel.setFormula(object.getString("formula"));
        }
        return columnModel;
    }

}
