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
package org.activityinfo.server.database.hibernate.entity;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldTypeClass;

import javax.persistence.*;
import java.util.Date;

/**
 * Defines an Indicator, a value (numeric or string) that can change over time.
 *
 * @author Alex Bertram
 */
@Entity @org.hibernate.annotations.Filter(
        name = "hideDeleted",
        condition = "DateDeleted is null")
public class Indicator implements java.io.Serializable, Orderable, Deleteable, FormFieldEntity {

    private int id;
    private Date dateDeleted;

    private String name;
    private String units;
    private String description;
    private boolean mandatory;
    private boolean visible;

    private String category;

    private Activity activity;

    private int aggregation;

    private int sortOrder;
    private String listHeader;
    private String type = FieldTypeClass.QUANTITY.getId();
    private String expression;
    private String relevanceExpression;
    private String nameInExpression;
    private boolean calculatedAutomatically;

    public Indicator() {
    }

    public Indicator(Indicator indicator) {
        this.dateDeleted = indicator.dateDeleted;

        this.name = indicator.name;
        this.units = indicator.units;
        this.description = indicator.description;
        this.mandatory = indicator.mandatory;
        this.visible = indicator.visible;

        this.category = indicator.category;
        this.activity = indicator.activity;
        this.aggregation = indicator.aggregation;

        this.sortOrder = indicator.sortOrder;
        this.listHeader = indicator.listHeader;
        this.type = indicator.type;
        this.expression = indicator.expression;
        this.relevanceExpression = indicator.relevanceExpression;
        this.nameInExpression = indicator.nameInExpression;
        this.calculatedAutomatically = indicator.calculatedAutomatically;
    }

    /**
     * @return the id of this Indicator
     */
    @Id
    @Column(name = "IndicatorId", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    @Transient
    @Override
    public ResourceId getFieldId() {
        return CuidAdapter.indicatorField(getId());
    }

    /**
     * Sets the id of this Indicator
     */
    public void setId(int indicatorId) {
        this.id = indicatorId;
    }

    @Column(name = "Expression", nullable = true, length = 999)
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Column(name = "skipExpression", nullable = true, length = 999)
    public String getRelevanceExpression() {
        return relevanceExpression;
    }

    public void setRelevanceExpression(String relevanceExpression) {
        this.relevanceExpression = relevanceExpression;
    }

    @Column(name = "nameInExpression", nullable = true, length = 999)
    public String getNameInExpression() {
        return nameInExpression;
    }

    public void setNameInExpression(String nameInExpression) {
        this.nameInExpression = nameInExpression;
    }

    @Column(name = "calculatedAutomatically", nullable = false)
    public boolean getCalculatedAutomatically() {
        return calculatedAutomatically;
    }

    public void setCalculatedAutomatically(boolean calculatedAutomatically) {
        this.calculatedAutomatically = calculatedAutomatically;
    }


    @Column(name = "Type", nullable = false, length = 30)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the name of this Indicator
     */
    @Column(name = "Name", nullable = false, length = 128)
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the Indicator
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets a description of the units in which this Indicator is expressed.
     * Note that this is for descriptive purpose only for the user, it does not
     * carry any semantics for our system.
     *
     * @return description of the units in which this indicator is expressed.
     * Examples: "households", "%" "cm"
     */
    @Column(name = "Units", nullable = true, length = 15)
    public String getUnits() {
        return this.units;
    }

    /**
     * Sets the description of the units in which this indicator is expressed.
     *
     * @param units a description of the units
     */
    public void setUnits(String units) {
        this.units = units;
    }

    /**
     * @return a full description of this indicator, containing perhaps detailed
     * instructions on how it is to be collected or calculated.
     */
    @Lob
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description of this Indicator.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Checks if the indicator is mandatory in the new and edit site forms.
     *
     * @return True if indicator is mandatory, false otherwise
     */
    @Column(name = "mandatory", nullable = false)
    public boolean isMandatory() {
        return this.mandatory;
    }

    /**
     * Sets the mandatory flag
     *
     * @param mandatory True if the indicator mandatory in the new and edit site forms.
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * Checks if the indicator is visible in the new and edit site forms.
     *
     * @return True if indicator is visible, false otherwise
     */
    @Column(name = "visible", nullable = false)
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * Sets the visible flag
     *
     * @param visible True if the indicator is visible in the new and edit site forms
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @return the Activity which is implemented at this Site
     */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ActivityId", nullable = false)
    public Activity getActivity() {
        return this.activity;
    }

    /**
     * Sets the Activity which is implemented at this Site
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * @return the method by which this Indicator is aggregated
     */
    @Column(name = "Aggregation", nullable = false)
    public int getAggregation() {
        return this.aggregation;
    }

    /**
     * Sets the method by which this Indicator is aggregated.
     */
    public void setAggregation(int aggregation) {
        this.aggregation = aggregation;
    }

    /**
     * @return the sort order of this Indicator within its Activity
     */
    @Override @Column(name = "SortOrder", nullable = false)
    public int getSortOrder() {
        return this.sortOrder;
    }

    /**
     * Sets the sort order of this Indicator within its Activity
     */
    @Override
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * @return a short list header that is used when this Indicator's values are
     * displayed in a grid
     */
    @Column(name = "ListHeader", length = 30)
    public String getListHeader() {
        return this.listHeader;
    }

    /**
     * Sets the short list header that is used when this Indicator's values are
     * displayed within a grid
     */
    public void setListHeader(String listHeader) {
        this.listHeader = listHeader;
    }

    /**
     * Gets this Indicator's category. Categories are just strings that are used
     * for organizing the display of Indicators in the user interface.
     *
     * @return the name of the category
     */
    @Column(name = "Category", length = 50)
    public String getCategory() {
        return this.category;
    }

    /**
     * Sets this Indicator's category.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return the time at which this Indicator was deleted
     */
    @Column @Temporal(value = TemporalType.TIMESTAMP)
    public Date getDateDeleted() {
        return this.dateDeleted;
    }

    /**
     * Sets the time at which this Indicator was deleted.
     */
    public void setDateDeleted(Date deleteTime) {
        this.dateDeleted = deleteTime;
    }

    /**
     * Marks this Indicator as deleted.
     */
    @Override
    public void delete() {
        setDateDeleted(new Date());
        getActivity().getDatabase().setLastSchemaUpdate(new Date());
        getActivity().incrementSchemaVersion();
    }

    /**
     * @return true if this Indicator has been deleted.
     */
    @Override @Transient
    public boolean isDeleted() {
        return getDateDeleted() != null;
    }

}
