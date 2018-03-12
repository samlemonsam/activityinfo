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
package org.activityinfo.legacy.shared.model;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.common.base.Strings;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.TypeRegistry;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.time.Month;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonView;

/**
 * One-to-one DTO for the <code>Indicator</code> domain object.
 *
 * @author Alex Bertram
 */
@JsonAutoDetect(JsonMethod.NONE)
public final class IndicatorDTO extends BaseModelData implements EntityDTO, ProvidesKey, IsFormField {

    public static final String ENTITY_NAME = "Indicator";

    public static final int AGGREGATE_SUM = 0;
    public static final int AGGREGATE_AVG = 1;
    public static final int AGGREGATE_SITE_COUNT = 2;

    public static final String PROPERTY_PREFIX = "I";

    public static final int UNITS_MAX_LENGTH = 254;
    public static final int MAX_LIST_HEADER_LENGTH = 29;
    public static final int MAX_CATEGORY_LENGTH = 50;

    public static final String TYPE_PROPERTY = "type";
    public static final String UNITS_PROPERTY = "units";
    public static final String LIST_HEADER_PROPERTY = "listHeader";
    public static final String DESCRIPTION_PROPERTY = "description";
    public static final String EXPRESSION_PROPERTY = "expression";
    public static final String RELEVANCE_PROPERTY = "relevanceExpression";
    public static final String CODE_PROPERTY = "nameInExpression";
    public static final String CATEGORY_PROPERTY = "category";
    public static final String AGGREGATION_PROPERTY = "aggregation";
    public static final String MANDATORY_PROPERTY = "mandatory";
    public static final String VISIBLE_PROPERTY = "visible";

    private int databaseId;
    private String databaseName;

    public IndicatorDTO() {
        super();
        setVisible(true);
    }

    /**
     * @param name  the name of the Indicator
     * @param units string describing this Indicator's units
     */
    public IndicatorDTO(String name, String units) {
        super();
        set(NAME_PROPERTY, name);
        set(UNITS_PROPERTY, units);
        setVisible(true);
    }

    /**
     * Constructs a copy of the given IndicatorDTO
     */
    public IndicatorDTO(IndicatorDTO dto) {
        super(dto.getProperties());
    }

    /**
     * @return the id of the Indicator
     */
    @Override
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public int getId() {
        return (Integer) get(ID_PROPERTY);
    }

    /**
     * Sets the Indicator's id
     */
    public void setId(int id) {
        set(ID_PROPERTY, id);
    }


    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Sets the Indicator's name
     */
    public void setName(String name) {
        set(NAME_PROPERTY, name);
    }

    /**
     * @return the Indicator's name
     */
    @Override
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public String getName() {
        return get(NAME_PROPERTY);
    }

    /**
     * Sets the Indicator's units, for example, "household" or "%"
     */
    public void setUnits(String units) {
        set(UNITS_PROPERTY, units);
    }

    /**
     * @return the Indicator's units
     */
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public String getUnits() {
        return get(UNITS_PROPERTY);
    }

    /**
     * @return the short list header used when displaying this Indicator in a
     * grid
     */
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public String getListHeader() {
        return get(LIST_HEADER_PROPERTY);
    }

    /**
     * Sets the short list header that is used when this Indicator's values are
     * displayed in a grid.
     */
    public void setListHeader(String value) {
        set(LIST_HEADER_PROPERTY, value);
    }

    /**
     * Full description of this Indicator, used to aid users entering data.
     */

    public void setDescription(String description) {
        set(DESCRIPTION_PROPERTY, description);
    }

    /**
     * @return this Indicator's description, principally used to aid users
     * entering data
     */
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public String getDescription() {
        return get(DESCRIPTION_PROPERTY);
    }

    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public String getExpression() {
        return get(EXPRESSION_PROPERTY);
    }

    public void setExpression(String expression) {
        set(EXPRESSION_PROPERTY, expression);
    }

    public String getRelevanceExpression() {
        return get(RELEVANCE_PROPERTY);
    }

    public void setRelevanceExpression(String relevanceExpression) {
        set(RELEVANCE_PROPERTY, relevanceExpression);
    }

    @JsonProperty("code")
    @JsonView(DTOViews.Schema.class)
    public String getNameInExpression() {
        return get(CODE_PROPERTY);
    }

    public void setNameInExpression(String nameInExpression) {
        set(CODE_PROPERTY, nameInExpression);
    }

    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public Boolean getCalculatedAutomatically() {
        return get("calculatedAutomatically");
    }

    public void setCalculatedAutomatically(Boolean calculatedAutomatically) {
        set("calculatedAutomatically", calculatedAutomatically);
    }


    public boolean isCalculated() {
        return !Strings.isNullOrEmpty(getExpression());
    }


    public FieldTypeClass getType() {
        return TypeRegistry.get().getTypeClass(getTypeId());
    }

    public void setType(FieldTypeClass type) {
        set(IndicatorDTO.TYPE_PROPERTY, type.getId());
    }

    public void setTypeId(String typeId) {
        set(IndicatorDTO.TYPE_PROPERTY, typeId);
    }

    @JsonProperty(IndicatorDTO.TYPE_PROPERTY)
    @JsonView(DTOViews.Schema.class)
    private String getTypeId() {
        return get(IndicatorDTO.TYPE_PROPERTY, QuantityType.TYPE_CLASS.getId());
    }

    public void setMandatory(boolean mandatory) {
        set(MANDATORY_PROPERTY, mandatory);
    }

    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public boolean isMandatory() {
        return get(MANDATORY_PROPERTY, false);
    }

    public void setVisible(boolean visible) {
        set(VISIBLE_PROPERTY, visible);
    }

    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public boolean isVisible() {
        return get(VISIBLE_PROPERTY, true);
    }

    /**
     * Sets the aggregation method for this indicator
     */
    public void setAggregation(int aggregation) {
        set(AGGREGATION_PROPERTY, aggregation);
    }

    /**
     * @return the aggregation method for this indicator
     */
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public int getAggregation() {
        return get(AGGREGATION_PROPERTY, AGGREGATE_SUM);
    }

    /**
     * @return this Indicator's category
     */
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public String getCategory() {
        return get(CATEGORY_PROPERTY);
    }

    /**
     * Sets this Indicator's category
     */
    public void setCategory(String category) {
        if (category != null && category.trim().length() == 0) {
            category = null;
        }
        set(CATEGORY_PROPERTY, category);
    }

    public int getActivityId() {
        return get("activityId");
    }

    public void setActivityId(int id) {
        set("activityId", id);
    }

    /**
     * @return the name of the property in which values for this indicator are
     * stored, for example in the
     * {@link org.activityinfo.legacy.shared.model.SiteDTO} object.
     */
    public String getPropertyName() {
        return getPropertyName(this.getId());
    }

    /**
     * Returns the name of the property in which values for Indicators of this
     * id are stored, for example in the
     * {@link org.activityinfo.legacy.shared.model.SiteDTO} object.
     * <p/>
     * For example, an indicator with the id of 3 will be stored as I3 =>
     * 1432.32 in a SiteDTO.
     *
     * @param id
     * @return the property name for
     */
    public static String getPropertyName(int id) {
        return PROPERTY_PREFIX + id;
    }

    public static String getPropertyName(int id, Month m) {
        return getPropertyName(id) + m.getPropertyName();
    }

    /**
     * Parses an Indicator property name, for example "I432", "I565" or "I309M2013-7" (in case of a monthly reported
     * indicator that was stored in the sitehistory) for the referenced indicator Id.
     *
     * @return the id of referenced Indicator
     */
    public static int indicatorIdForPropertyName(String propertyName) {
        int monthInfix = propertyName.indexOf(Month.PROPERTY_PREFIX);
        if (monthInfix > -1) {
            return Integer.parseInt(propertyName.substring(PROPERTY_PREFIX.length(), monthInfix));
        } else {
            return Integer.parseInt(propertyName.substring(PROPERTY_PREFIX.length()));
        }
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public String getKey() {
        return "i" + getId();
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public int getSortOrder() {
        return get(SORT_ORDER_PROPERTY, 0);
    }

    @Override
    public FormField asFormField() {
        FormField field = new FormField(CuidAdapter.indicatorField(getId()));
        field.setLabel(getName());
        field.setDescription(getDescription());
        field.setRelevanceConditionExpression(getRelevanceExpression());
        field.setRequired(isMandatory());
        field.setVisible(isVisible());

        String code = getNameInExpression();
        if (!Strings.isNullOrEmpty(code)) {
            field.setCode(code);
        }

        if (isCalculated()) {
            field.setType(new CalculatedFieldType(getExpression()));

        } else if (Strings.isNullOrEmpty(getTypeId()) || getTypeId().equals(QuantityType.TYPE_CLASS.getId())) {
            String units = getUnits();
            if (units == null) {
                units = "";
            }
            field.setType(new QuantityType()
                    .setUnits(units)
                    .setAggregation(getAggregation()));

        } else {
            field.setType(getType().createType());

        }
        return field;
    }


    public void setSortOrder(int sortOrder) {
        set(SORT_ORDER_PROPERTY, sortOrder);
    }
}
