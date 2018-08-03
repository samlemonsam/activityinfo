/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    public static final String RECORD_ID_SYMBOL = "_id";
    public static final String FORM_ID_SYMBOL = "_class";
    public static final String FORM_NAME_SYMBOL = "_class_label";
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
