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

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.extjs.gxt.ui.client.data.BaseModelData;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonView;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.Set;

/**
 * A period where 'normal users' cannot add, update or remove data
 */
@JsonAutoDetect(JsonMethod.NONE)
public class LockedPeriodDTO extends BaseModelData implements EntityDTO {

    public static final String ENTITY_NAME = "LockedPeriod";
    public static final String START_DATE_PROPERTY = "fromDate";
    public static final String END_DATE_PROPERTY = "toDate";
    public static final String ENABLED_PROPERTY = "enabled";
    public static final String PARENT_NAME_PROPERTY = "parentName";
    public static final String PARENT_TYPE_PROPERTY = "parentType";

    /**
     * An object supporting locks
     */
    public interface HasLockedPeriod extends EntityDTO {
        Set<LockedPeriodDTO> getLockedPeriods();
    }

    private HasLockedPeriod parent;
    private Integer parentId;

    public LockedPeriodDTO() {
        super();
    }

    public void setName(String name) {
        set(NAME_PROPERTY, name);
    }

    @Override
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public String getName() {
        return get(NAME_PROPERTY);
    }

    public void setId(int id) {
        set(ID_PROPERTY, id);
    }

    @Override
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public int getId() {
        return (Integer) get(ID_PROPERTY);
    }

    @JsonIgnore
    public void setToDate(Date toDate) {
        if(toDate == null) {
            setToDate((LocalDate)null);
        } else {
            setToDate(new LocalDate(toDate));
        }
    }

    public void setToDate(LocalDate toDate) {
        set(END_DATE_PROPERTY, toDate);
    }

    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public LocalDate getToDate() {
        return get(END_DATE_PROPERTY);
    }

    @JsonIgnore
    public void setFromDate(Date fromDate) {
        if (fromDate == null) {
            setFromDate((LocalDate) null);
        } else {
            setFromDate(new LocalDate(fromDate));
        }
    }

    public void setFromDate(LocalDate fromDate) {
        set(START_DATE_PROPERTY, fromDate);
    }

    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public LocalDate getFromDate() {
        return get(START_DATE_PROPERTY);
    }

    /**
     * Returns true when startDate is before end date, and both startDate and
     * EndDate are non-null.
     */
    public boolean isValid() {
        return getFromDate() != null &&
                getToDate() != null &&
                getFromDate().before(getToDate());
    }

    public void setEnabled(boolean enabled) {
        set(ENABLED_PROPERTY, enabled);
    }

    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public boolean isEnabled() {
        return (Boolean) get(ENABLED_PROPERTY);
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    public HasLockedPeriod getParent() {
        return parent;
    }

    public void setParent(HasLockedPeriod hasLock) {
        this.parent = hasLock;
        this.parentId = hasLock.getId();
        set(PARENT_NAME_PROPERTY, hasLock.getName());
        set(PARENT_TYPE_PROPERTY, hasLock.getEntityName());
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean hasParentId() {
        return parentId != 0;
    }

    public void setParentId(int id) {
        this.parentId = id;
    }

    public int getParentId() {
        return parentId;
    }

    public String getParentType() {
        return get(PARENT_TYPE_PROPERTY);
    }

    /**
     * give meaning to the parentId by specifying the type of the parent.
     */
    public void setParentType(String type) {
        set(PARENT_TYPE_PROPERTY, type);
    }

    public boolean fallsWithinPeriod(@Nonnull LocalDate date) {
        LocalDate from = getFromDate();
        LocalDate to = getToDate();

        boolean isSameAsFrom = from.compareTo(date) == 0;
        boolean isSameAsTo = to.compareTo(date) == 0;
        boolean isBetween = from.before(date) && to.after(date);

        return isBetween || isSameAsFrom || isSameAsTo;
    }
}
