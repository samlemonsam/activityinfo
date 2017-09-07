package org.activityinfo.server.command.handler;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.util.Providers;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.TypeRegistry;
import org.activityinfo.server.command.handler.crud.PropertyMap;
import org.activityinfo.server.database.hibernate.entity.*;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.activityinfo.legacy.shared.util.StringUtil.truncate;

/**
 * Provides functionality common to CreateEntityHandler and UpdateEntityHandler
 *
 * @author Alex Bertram (akbertram@gmail.com)
 */
public class BaseEntityHandler {

    private final EntityManager em;
    private final PermissionOracle permissionsOracle;


    public BaseEntityHandler(EntityManager em, Injector injector) {
        this.em = em;
        this.permissionsOracle = new PermissionOracle(Providers.of(em));
    }

    protected void updateIndicatorProperties(Indicator indicator, Map<String, Object> changeMap) {
        PropertyMap changes = new PropertyMap(changeMap);
        
        if (changes.containsKey("name")) {
            indicator.setName(trimAndTruncate(changes.get("name")));
        }

        if (changes.containsKey("type")) {
            FieldTypeClass type = parseType(changes.get("type"));
            indicator.setType(type != null ? type.getId() : null);
            if (type != FieldTypeClass.QUANTITY && !changes.containsKey("units")) {
                indicator.setUnits("");
            }
        }

        if (changes.containsKey("expression")) {
            indicator.setExpression(trimAndTruncate(changes.get("expression")));
        }

        if (changes.containsKey("relevanceExpression")) {
            indicator.setRelevanceExpression(trimAndTruncate(changes.get("relevanceExpression")));
        }

        if (changes.containsKey("nameInExpression")) {
            indicator.setNameInExpression(trimAndTruncate(changes.get("nameInExpression")));
        }
        
        // Allow "code" as an alias from the JSON API
        if (changes.containsKey("code")) {
            indicator.setNameInExpression(trimAndTruncate(changes.get("code")));
        }

        if (changes.containsKey("calculatedAutomatically")) {
            indicator.setCalculatedAutomatically((Boolean) changes.get("calculatedAutomatically"));
        }

        if (changes.containsKey("aggregation")) {
            indicator.setAggregation((Integer) changes.get("aggregation"));
        }

        if (changes.containsKey("category")) {
            indicator.setCategory(trimAndTruncate(changes.get("category")));
        }

        if (changes.containsKey("listHeader")) {
            indicator.setListHeader(trimAndTruncate(changes.get("listHeader")));
        }

        if (changes.containsKey("description")) {
            indicator.setDescription(trimAndTruncate(changes.get("description")));
        }

        if (changes.containsKey("units")) {
            indicator.setUnits(trimAndTruncate(changes.get("units")));
        }

        if (changes.containsKey("sortOrder")) {
            indicator.setSortOrder((Integer) changes.get("sortOrder"));
        }

        if (changes.containsKey("mandatory")) {
            indicator.setMandatory((Boolean) changes.get("mandatory"));
        }

        if (changes.containsKey("visible")) {
            indicator.setVisible((Boolean) changes.get("visible"));
        }

        indicator.getActivity().incrementSchemaVersion();
        indicator.getActivity().getDatabase().setLastSchemaUpdate(new Date());
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
        if (changes.containsKey("name")) {
            attribute.setName(trimAndTruncate(changes.get("name")));
        }
        if (changes.containsKey("sortOrder")) {
            attribute.setSortOrder((Integer) changes.get("sortOrder"));
        }
        // TODO: update lastSchemaUpdate
    }

    protected void updateAttributeGroupProperties(AttributeGroup group, Map<String, Object> changes) {
        if (changes.containsKey("name")) {
            group.setName(trimAndTruncate(changes.get("name")));
        }

        if (changes.containsKey("multipleAllowed")) {
            group.setMultipleAllowed((Boolean) changes.get("multipleAllowed"));
        }
        if (changes.containsKey("sortOrder")) {
            group.setSortOrder((Integer) changes.get("sortOrder"));
        }
        if (changes.containsKey("mandatory")) {
            group.setMandatory((Boolean) changes.get("mandatory"));
        }
        if (changes.containsKey("defaultValue")) {
            group.setDefaultValue((Integer) changes.get("defaultValue"));
        }
        if (changes.containsKey("workflow")) {
            group.setWorkflow((Boolean) changes.get("workflow"));
        }
    }

    protected void updateLockedPeriodProperties(LockedPeriod lockedPeriod, Map<String, Object> changes) {
        if (changes.containsKey("name")) {
            lockedPeriod.setName(trimAndTruncate(changes.get("name")));
        }
        if (changes.containsKey("toDate")) {
            lockedPeriod.setToDate((LocalDate) changes.get("toDate"));
        }
        if (changes.containsKey("fromDate")) {
            lockedPeriod.setFromDate((LocalDate) changes.get("fromDate"));
        }
        if (changes.containsKey("enabled")) {
            lockedPeriod.setEnabled((Boolean) changes.get("enabled"));
        }

        if (!lockedPeriod.getFromDate().before(lockedPeriod.getToDate()) && !lockedPeriod.getFromDate().equals(lockedPeriod.getToDate())) {
            throw new RuntimeException("From date is not before To date. Refuse lock object persistence: " + lockedPeriod);
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
        if (changes.containsKey("name")) {
            target.setName(trimAndTruncate(changes.get("name")));
        }

        if (changes.containsKey("fromDate")) {
            target.setDate1(((LocalDate) changes.get("fromDate")).atMidnightInMyTimezone());
        }

        if (changes.containsKey("toDate")) {
            target.setDate2(((LocalDate) changes.get("toDate")).atMidnightInMyTimezone());
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
    protected void assertDesignPrivileges(User user, UserDatabase database) throws IllegalAccessCommandException {

        if (!permissionsOracle.isDesignAllowed(database, user)) {
            throw new IllegalAccessCommandException();
        }
    }

    public EntityManager entityManager() {
        return em;
    }

}
