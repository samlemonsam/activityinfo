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
package org.activityinfo.server.command.handler;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.util.Providers;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.legacy.shared.model.AttributeGroupDTO;
import org.activityinfo.legacy.shared.model.EntityDTO;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.legacy.shared.model.LockedPeriodDTO;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.TypeRegistry;
import org.activityinfo.server.command.handler.crud.PropertyMap;
import org.activityinfo.server.database.hibernate.entity.*;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.activityinfo.legacy.shared.model.EntityDTO.NAME_PROPERTY;
import static org.activityinfo.legacy.shared.model.EntityDTO.SORT_ORDER_PROPERTY;
import static org.activityinfo.legacy.shared.model.IndicatorDTO.*;
import static org.activityinfo.legacy.shared.util.StringUtil.truncate;

/**
 * Provides functionality common to CreateEntityHandler and UpdateEntityHandler
 *
 * @author Alex Bertram (akbertram@gmail.com)
 */
public class BaseEntityHandler {

    protected final EntityManager em;
    protected final PermissionOracle permissionsOracle;


    public BaseEntityHandler(EntityManager em) {
        this.em = em;
        this.permissionsOracle = new PermissionOracle(Providers.of(em));
    }

    protected void updateIndicatorProperties(Indicator indicator, Map<String, Object> changeMap) {
        PropertyMap changes = new PropertyMap(changeMap);

        if (changes.containsKey(NAME_PROPERTY)) {
            indicator.setName(trimAndTruncate(changes.get(NAME_PROPERTY)));
        }

        if (changes.containsKey(TYPE_PROPERTY)) {
            updateType(indicator, changes);
        }

        if (changes.containsKey(EXPRESSION_PROPERTY)) {
            indicator.setExpression(trimAndTruncate(changes.get(EXPRESSION_PROPERTY)));
        }

        if (changes.containsKey(RELEVANCE_PROPERTY)) {
            indicator.setRelevanceExpression(trimAndTruncate(changes.get(RELEVANCE_PROPERTY)));
        }

        if (changes.containsKey(CODE_PROPERTY)) {
            indicator.setNameInExpression(trimAndTruncate(changes.get(CODE_PROPERTY)));
        }
        
        // Allow "code" as an alias from the JSON API
        if (changes.containsKey("code")) {
            indicator.setNameInExpression(trimAndTruncate(changes.get("code")));
        }

        if (changes.containsKey("calculatedAutomatically")) {
            indicator.setCalculatedAutomatically(changes.get("calculatedAutomatically"));
        }

        if (changes.containsKey(AGGREGATION_PROPERTY)) {
            indicator.setAggregation(changes.get(AGGREGATION_PROPERTY));
        }

        if (changes.containsKey(CATEGORY_PROPERTY)) {
            indicator.setCategory(trimAndTruncate(changes.get(CATEGORY_PROPERTY)));
        }

        if (changes.containsKey(LIST_HEADER_PROPERTY)) {
            indicator.setListHeader(trimAndTruncate(changes.get(LIST_HEADER_PROPERTY)));
        }

        if (changes.containsKey(IndicatorDTO.DESCRIPTION_PROPERTY)) {
            indicator.setDescription(trimAndTruncate(changes.get(DESCRIPTION_PROPERTY)));
        }

        if (changes.containsKey(UNITS_PROPERTY)) {
            indicator.setUnits(trimAndTruncate(changes.get(UNITS_PROPERTY)));
        }

        if (changes.containsKey(SORT_ORDER_PROPERTY)) {
            indicator.setSortOrder(changes.get(SORT_ORDER_PROPERTY));
        }

        if (changes.containsKey(MANDATORY_PROPERTY)) {
            indicator.setMandatory(changes.get(MANDATORY_PROPERTY));
        }

        if (changes.containsKey(VISIBLE_PROPERTY)) {
            indicator.setVisible(changes.get(VISIBLE_PROPERTY));
        }

        indicator.getActivity().incrementSchemaVersion();
        indicator.getActivity().getDatabase().setLastSchemaUpdate(new Date());
    }

    private void updateType(Indicator indicator, PropertyMap changes) {
        FieldTypeClass type = parseType(changes.get(TYPE_PROPERTY));
        indicator.setType(type.getId());
        if ((type != FieldTypeClass.QUANTITY) && !changes.containsKey(UNITS_PROPERTY)) {
            indicator.setUnits("");
        }
    }

    private FieldTypeClass parseType(Object type) {
        List<String> registeredTypes = Lists.newArrayList();
        if(type instanceof FieldTypeClass) {
            return (FieldTypeClass) type;
        } else if(type instanceof String) {
            String typeName = (String) type;
            for (FieldTypeClass fieldTypeClass : TypeRegistry.get().getTypeClasses()) {
                if(fieldTypeClass.getId().equalsIgnoreCase(typeName)) {
                    return fieldTypeClass;
                }
                registeredTypes.add(fieldTypeClass.getId());
            }
        }
        throw new CommandException(String.format("Invalid 'type' property value '%s'. Expected: %s",
                type.toString(), registeredTypes.toString()));
    }

    protected void updateAttributeProperties(Map<String, Object> changes, Attribute attribute) {
        if (changes.containsKey(NAME_PROPERTY)) {
            attribute.setName(trimAndTruncate(changes.get(NAME_PROPERTY)));
        }
        if (changes.containsKey(SORT_ORDER_PROPERTY)) {
            attribute.setSortOrder((Integer) changes.get(SORT_ORDER_PROPERTY));
        }

        for (Activity activity : attribute.getGroup().getActivities()) {
            activity.getDatabase().updateVersion();
        }
    }

    protected void updateAttributeGroupProperties(AttributeGroup group, Map<String, Object> changes) {
        if (changes.containsKey(EntityDTO.NAME_PROPERTY)) {
            group.setName(trimAndTruncate(changes.get(NAME_PROPERTY)));
        }

        if (changes.containsKey(AttributeGroupDTO.MULTIPLE_ALLOWED_PROPERTY)) {
            group.setMultipleAllowed((Boolean) changes.get(AttributeGroupDTO.MULTIPLE_ALLOWED_PROPERTY));
        }
        if (changes.containsKey(SORT_ORDER_PROPERTY)) {
            group.setSortOrder((Integer) changes.get(SORT_ORDER_PROPERTY));
        }
        if (changes.containsKey(IndicatorDTO.MANDATORY_PROPERTY)) {
            group.setMandatory((Boolean) changes.get(IndicatorDTO.MANDATORY_PROPERTY));
        }
        if (changes.containsKey(AttributeGroupDTO.DEFAULT_VALUE_PROPERTY)) {
            group.setDefaultValue((Integer) changes.get(AttributeGroupDTO.DEFAULT_VALUE_PROPERTY));
        }
        if (changes.containsKey(AttributeGroupDTO.WORKFLOW_PROPERTY)) {
            group.setWorkflow((Boolean) changes.get(AttributeGroupDTO.WORKFLOW_PROPERTY));
        }
    }

    protected void updateLockedPeriodProperties(LockedPeriod lockedPeriod, Map<String, Object> changes) {
        if (changes.containsKey(EntityDTO.NAME_PROPERTY)) {
            lockedPeriod.setName(trimAndTruncate(changes.get(EntityDTO.NAME_PROPERTY)));
        }
        if (changes.containsKey(LockedPeriodDTO.END_DATE_PROPERTY)) {
            lockedPeriod.setToDate((LocalDate) changes.get(LockedPeriodDTO.END_DATE_PROPERTY));
        }
        if (changes.containsKey(LockedPeriodDTO.START_DATE_PROPERTY)) {
            lockedPeriod.setFromDate((LocalDate) changes.get(LockedPeriodDTO.START_DATE_PROPERTY));
        }
        if (changes.containsKey(LockedPeriodDTO.ENABLED_PROPERTY)) {
            lockedPeriod.setEnabled((Boolean) changes.get(LockedPeriodDTO.ENABLED_PROPERTY));
        }

        if (!lockedPeriod.getFromDate().before(lockedPeriod.getToDate()) && !lockedPeriod.getFromDate().equals(lockedPeriod.getToDate())) {
            throw new IllegalArgumentException("From date is not before To date. Refuse lock object persistence: " + lockedPeriod);
        }

        lockedPeriod.getParentDatabase().setLastSchemaUpdate(new Date());
        entityManager().merge(lockedPeriod);
    }

    private String trimAndTruncate(Object value) {
        if (value instanceof String) {
            String stringValue = (String)value;
            return truncate(Strings.emptyToNull(stringValue.trim()));
        } else {
            return null;
        }
    }

    protected void updateTargetProperties(Target target, Map<String, Object> changes) {
        if (changes.containsKey(NAME_PROPERTY)) {
            target.setName(trimAndTruncate(changes.get(NAME_PROPERTY)));
        }

        if (changes.containsKey(LockedPeriodDTO.START_DATE_PROPERTY)) {
            target.setDate1(((LocalDate) changes.get(LockedPeriodDTO.START_DATE_PROPERTY)).atMidnightInMyTimezone());
        }

        if (changes.containsKey(LockedPeriodDTO.END_DATE_PROPERTY)) {
            target.setDate2(((LocalDate) changes.get(LockedPeriodDTO.END_DATE_PROPERTY)).atMidnightInMyTimezone());
        }

        if (changes.containsKey("projectId")) {
            Object projectId = changes.get("projectId");
            if (projectId != null) {
                target.setProject(entityManager().getReference(Project.class, projectId));
            } else {
                target.setProject(null);
            }
        }

        if (changes.containsKey("partnerId")) {
            Object partnerId = changes.get("partnerId");
            if (partnerId != null) {
                target.setPartner(entityManager().getReference(Partner.class, partnerId));
            } else {
                target.setPartner(null);
            }
        }

        if (changes.containsKey("AdminEntityId")) {
            target.setAdminEntity(entityManager().getReference(AdminEntity.class, changes.get("AdminEntityId")));
        }

    }

    /**
     * Asserts that the user has permission to modify the structure of the given
     * database.
     *
     * @param user     THe user for whom to check permissions
     * @param database The database the user is trying to modify
     * @throws IllegalAccessCommandException If the user does not have permission
     */
    protected void assertDesignPrivileges(User user, Database database) {

        if (!permissionsOracle.isDesignAllowed(database, user)) {
            throw new IllegalAccessCommandException();
        }
    }

    public EntityManager entityManager() {
        return em;
    }

}
