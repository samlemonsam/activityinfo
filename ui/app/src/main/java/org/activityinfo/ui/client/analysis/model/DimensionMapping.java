package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
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

    public DimensionMapping(ExprNode formula) {
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
        this.formula = new SymbolExpr(fieldId).asExpression();
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
}
