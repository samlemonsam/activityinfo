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
package org.activityinfo.model.analysis.pivot;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;

/**
 * Maps Measures to a Dimension or Dimension Category.
 *
 * <p>When tables are constructed from many data sources, the user needs to specify a way
 * to map values from these different data sources to the dimension categories. This can be done
 * in several ways:
 *
 * <ul>
 *     <li>The dimension category may be provided by a single field or formula in a given Form</li>
 *     <li>All quantities from a given Form may be assigned to a specific Dimension Category. For example, all
 *     beneficiary counts from a "School Construction" form might be mapped to the "Education" category of a </li>
 *     "Sector" dimension.</li>
 *     <li>All quantities from a specific field may be assigned to a specific Dimension Category. For example,
 *     all counts from a "Number of girls provided with a textbook" might be mapped to the "Female" category
 *     of a "Gender" dimension.</li>
 * </ul>
 *
 */
public class DimensionMapping {


    public enum Type {
        /**
         * Maps the source Form's label to the dimension category
         */
        FORM,

        /**
         * Maps the source field's label to the dimension category
         */
        FIELD,

        /**
         * Maps a value in the Record to the dimension category.
         */
        VALUE
    }

    private final ResourceId formId;
    private final String formula;
    private final Type type;

    public DimensionMapping(FormulaNode formula) {
        this.formId = null;
        this.formula = formula.asExpression();
        this.type = Type.VALUE;
    }

    public DimensionMapping(ResourceId formId, String formula) {
        this.formId = formId;
        this.formula = formula;
        this.type = Type.VALUE;
    }

    public DimensionMapping(ResourceId formId, ResourceId fieldId) {
        this.formId = formId;
        this.formula = new SymbolNode(fieldId).asExpression();
        this.type = Type.VALUE;
    }


    public DimensionMapping(ResourceId formId, String formula, Type type) {
        this.formId = formId;
        this.formula = formula;
        this.type = type;
    }

    public static DimensionMapping formMapping() {
        return new DimensionMapping(null, ColumnModel.FORM_NAME_SYMBOL, Type.FORM);
    }

    public ResourceId getFormId() {
        return formId;
    }

    public String getFormula() {
        return formula;
    }


    public JsonValue toJson() {

        JsonValue object = Json.createObject();
        if(formId != null) {
            object.put("formId", formId.asString());
        }
        object.put("formula", formula);
        object.put("type", type.toString());
        return object;
    }

    public static DimensionMapping fromJson(JsonValue object) {
        ResourceId formId = null;
        if(object.hasKey("formId")) {
            formId = ResourceId.valueOf(object.getString("formId"));
        }
        String formula = object.getString("formula");
        Type type = Type.valueOf(object.getString("type"));
        return new DimensionMapping(formId, formula, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DimensionMapping mapping = (DimensionMapping) o;

        if (formId != null ? !formId.equals(mapping.formId) : mapping.formId != null) return false;
        if (formula != null ? !formula.equals(mapping.formula) : mapping.formula != null) return false;
        return type == mapping.type;

    }

    @Override
    public int hashCode() {
        int result = formId != null ? formId.hashCode() : 0;
        result = 31 * result + (formula != null ? formula.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
