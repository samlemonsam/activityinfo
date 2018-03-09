package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.SymbolNode;
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
        return new DimensionMapping(null, null, Type.FORM);
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
        return object;
    }

    public static DimensionMapping fromJson(JsonValue object) {
        ResourceId formId = null;
        if(object.hasKey("formId")) {
            formId = ResourceId.valueOf(object.getString("formId"));
        }
        String formula = object.getString("formula");
        return new DimensionMapping(formId, formula);
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
