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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.activityinfo.legacy.shared.command.AddPartner;
import org.activityinfo.legacy.shared.command.CloneDatabase;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.model.form.*;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.ParametrizedFieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.promise.Promise;
import org.activityinfo.server.database.hibernate.entity.*;
import org.activityinfo.service.store.FormCatalog;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.util.*;
import java.util.logging.Logger;

import static org.activityinfo.model.legacy.CuidAdapter.BUILTIN_FIELDS;
import static org.activityinfo.model.legacy.CuidAdapter.activityFormClass;

/**
 * @author yuriyz on 11/17/2014.
 */
public class CloneDatabaseHandler implements CommandHandler<CloneDatabase> {

    private static final Logger LOGGER = Logger.getLogger(CloneDatabaseHandler.class.getName());

    private final EntityManager em;
    private final PermissionOracle permissionOracle;
    private final KeyGenerator generator = new KeyGenerator();
    private final Provider<FormCatalog> formCatalog;

    // Mappings old id (source db) -> new id (target/newly created db)
    private final Map<Integer, Activity> activityMapping = Maps.newHashMap();
    private final Map<FormElement, FormElement> sourceIdToTargetFormElementMapping = Maps.newHashMap();
    private final Map<ResourceId, ResourceId> typeIdMapping = Maps.newHashMap();

    private CloneDatabase command;
    private UserDatabase targetDb;
    private UserDatabase sourceDb;

    @Inject
    public CloneDatabaseHandler(Injector injector, Provider<FormCatalog> formCatalog) {
        this.em = injector.getInstance(EntityManager.class);
        this.permissionOracle = injector.getInstance(PermissionOracle.class);
        this.formCatalog = formCatalog;
    }

    @Override
    public CommandResult execute(CloneDatabase command, User user) throws CommandException {
        
        this.command = command;
        this.targetDb = createDatabase(this.command, user);
        this.sourceDb = em.find(UserDatabase.class, this.command.getSourceDatabaseId());

        createDefaultPartner(user);

        if (!permissionOracle.isViewAllowed(sourceDb, user)) {
            throw new IllegalAccessCommandException();
        }

        // 1. copy partners and keep mapping between old and new partners
        if (this.command.isCopyPartners() || this.command.isCopyUserPermissions()) {
            copyPartners();
        }

        // 2. copy user permissions : without design privileges the user shouldn't be able to see the list of users.
        if (this.command.isCopyUserPermissions() && permissionOracle.isDesignAllowed(sourceDb, user)) {
            copyUserPermissions();
        }

        List<Promise<Void>> promises = new ArrayList<>();

        // 3. copy forms and form data
        copyFormData();
        
        return new CreateResult(targetDb.getId());
    }

    private void createDefaultPartner(User user) {
        Preconditions.checkNotNull(targetDb);
        Preconditions.checkState(targetDb.getId() > 0);

        PartnerDTO partner = new PartnerDTO();
        partner.setName("Default");

        new AddPartnerHandler(em).execute(new AddPartner(targetDb.getId(), partner), user);
    }

    private void copyUserPermissions() {

        for (UserPermission sourcePermission : sourceDb.getUserPermissions()) {
            UserPermission newPermission = new UserPermission(sourcePermission);
            newPermission.setDatabase(targetDb);
            newPermission.setLastSchemaUpdate(new Date());
            newPermission.setPartner(sourcePermission.getPartner());

            em.persist(newPermission);

        }
    }

    private void copyPartners() {
        for (Partner partner : sourceDb.getPartners()) {
            if (!partner.getName().equals("Default")) {
                targetDb.getPartners().add(partner);
            }
        }

        targetDb.setLastSchemaUpdate(new Date());
        em.persist(targetDb);
    }

    private void copyFormData() {

        // first copy all activities without payload (indicators, attributes)
        for (Activity activity : sourceDb.getActivities()) {
            copyActivity(activity);
        }

        final List<Promise<VoidResult>> copyPromises = new ArrayList<>();
        for (Activity activity : sourceDb.getActivities()) {
            final ResourceId sourceFormId = activityFormClass(activity.getId());
            final ResourceId targetFormId = activityFormClass(activityMapping.get(activity.getId()).getId());

            copyFormClass(sourceFormId, targetFormId);
        }
    }

    private void copyFormClass(final ResourceId sourceFormId,
                               final ResourceId targetFormId) {

        FormClass sourceFormClass = formCatalog.get().getFormClass(sourceFormId);
        FormClass targetFormClass = new FormClass(targetFormId);
        
        targetFormClass.setLabel(sourceFormClass.getLabel());
        targetFormClass.setDescription(sourceFormClass.getDescription());
        targetFormClass.setDatabaseId(CuidAdapter.databaseId(targetDb.getId()));

        sourceIdToTargetFormElementMapping.clear();
        copyFormElements(sourceFormClass, targetFormClass, sourceFormClass.getId(), targetFormClass.getId());
        correctRelevanceConditions(targetFormClass);
        
        formCatalog.get().getForm(targetFormId).get().updateFormClass(targetFormClass);
    }

    private void copyFormElements(FormElementContainer sourceContainer, FormElementContainer targetContainer, 
                                  ResourceId sourceFormId, ResourceId targetFormId) {
        for (FormElement element : sourceContainer.getElements()) {
            if (element instanceof FormSection) {
                FormSection sourceSection = (FormSection) element;
                FormSection targetSection = new FormSection(ResourceId.generateId());
                targetSection.setLabel(sourceSection.getLabel());

                targetContainer.addElement(targetSection);
                sourceIdToTargetFormElementMapping.put(sourceSection, targetSection);

                copyFormElements(sourceSection, targetSection, sourceFormId, targetFormId);
            } else if (element instanceof FormField) {
                FormField sourceField = (FormField) element;
                FormField targetField = new FormField(targetFieldId(sourceField, sourceFormId, targetFormId));

                targetField.setType(targetFieldType(sourceField));
                targetField.setCode(sourceField.getCode());
                targetField.setRelevanceConditionExpression(sourceField.getRelevanceConditionExpression());
                targetField.setLabel(sourceField.getLabel());
                targetField.setDescription(sourceField.getDescription());
                targetField.setRequired(sourceField.isRequired());
                targetField.setSuperProperties(sourceField.getSuperProperties());

                targetContainer.addElement(targetField);
                sourceIdToTargetFormElementMapping.put(sourceField, targetField);
            } else {
                throw new RuntimeException("Unsupported FormElement : " + element);
            }
        }
    }

    private void correctRelevanceConditions(FormClass targetFormClass) {
        for (FormField field : targetFormClass.getFields()) {
            field.setRelevanceConditionExpression(replaceSourceIdsToTargetIds(field.getRelevanceConditionExpression()));
        }
    }

    private String replaceSourceIdsToTargetIds(String expression) {
        if (!Strings.isNullOrEmpty(expression)) {

            // replace element ids
            for (Map.Entry<FormElement, FormElement> entry : sourceIdToTargetFormElementMapping.entrySet()) {
                expression = expression.replace(entry.getKey().getId().asString(), entry.getValue().getId().asString());
            }

            // replace type ids
            for (Map.Entry<ResourceId, ResourceId> entry :typeIdMapping.entrySet()) {
                expression = expression.replace(entry.getKey().asString(), entry.getValue().asString());
            }
        }
        return expression;
    }

    private FieldType targetFieldType(FormField sourceField) {
        FieldType fieldType = sourceField.getType();

        if (!(fieldType instanceof ParametrizedFieldType)) {
            return fieldType;
        }

        if (fieldType instanceof QuantityType ||
                fieldType instanceof CalculatedFieldType ||
                fieldType instanceof LocalDateType ||
                fieldType instanceof AttachmentType) {
            return fieldType;
        }

        if (fieldType instanceof EnumType) {
            if (sourceField.getId().getDomain() == CuidAdapter.ATTRIBUTE_GROUP_FIELD_DOMAIN) {
                EnumType sourceEnumType = (EnumType) fieldType;
                List<EnumItem> targetValues = Lists.newArrayList();

                for (EnumItem sourceValue : sourceEnumType.getValues()) {

                    ResourceId targetValueId = CuidAdapter.cuid(sourceValue.getId().getDomain(), generator.generateInt());
                    targetValues.add(new EnumItem(targetValueId, sourceValue.getLabel()));
                    typeIdMapping.put(sourceValue.getId(), targetValueId);
                }
                return new EnumType(sourceEnumType.getCardinality(), targetValues);
            }
        }

        if (fieldType instanceof ReferenceType) {
            ReferenceType sourceType = (ReferenceType) fieldType;

            Set<ResourceId> sourceRange = sourceType.getRange();
            Set<ResourceId> targetRange = new HashSet<>();

            ResourceId next = sourceRange.iterator().next();
            switch (next.getDomain()) {
                case CuidAdapter.PARTNER_FORM_CLASS_DOMAIN:
                    if (command.isCopyPartners()) {

                        // as defined in ActivityFormClassBuilder.build() in reference range we stick to db id
                        ResourceId partnerId = CuidAdapter.partnerFormId(targetDb.getId());
                        targetRange.add(partnerId);
                        typeIdMapping.put(next, partnerId);
                    }
                    break;
                case CuidAdapter.PROJECT_CLASS_DOMAIN:
                    ResourceId projectId = CuidAdapter.projectFormClass(targetDb.getId());
                    targetRange.add(projectId);
                    typeIdMapping.put(next, projectId);
                    break;
            }


            // fallback to source targetRange
            if (targetRange.isEmpty()) {
                targetRange = sourceRange;
            }

            return new ReferenceType()
                    .setCardinality(sourceType.getCardinality())
                    .setRange(targetRange);
        }

        throw new RuntimeException("Unable to generate field id for fieldType : " + fieldType);

    }

    private ResourceId targetFieldId(FormField sourceField, ResourceId sourceClassId, ResourceId targetClassId) {
        ResourceId sourceFieldId = sourceField.getId();
        for (int fieldIndex : BUILTIN_FIELDS) {
            if (sourceFieldId.equals(CuidAdapter.field(sourceClassId, fieldIndex))) {
                return CuidAdapter.field(targetClassId, fieldIndex);
            }
        }

        return CuidAdapter.cuid(sourceField.getId().getDomain(), generator.generateInt());
    }

    private Activity copyActivity(Activity sourceActivity) {
        Activity newActivity = new Activity(sourceActivity); // copy simple values : like name, category (but not Indicators, Attributes)
        newActivity.getAttributeGroups().clear();
        newActivity.getLockedPeriods().clear();
        newActivity.getIndicators().clear();

        // target db
        newActivity.setDatabase(targetDb);

        setLocationTypeForNewActivity(sourceActivity, newActivity);

        em.persist(newActivity); // persist to get id of new activity
        activityMapping.put(sourceActivity.getId(), newActivity);

        return newActivity;
    }

    private void setLocationTypeForNewActivity(Activity sourceActivity, Activity newActivity) {
        // location type -> change it only if sourceCountry != targetCountry
        if (sourceActivity.getLocationType() != null && sourceDb.getCountry().getId() != targetDb.getCountry().getId()) {

            boolean locationTypeCreated = false;

            //1. If there is a location type with the same name in the new country, use that location Type
            String sourceName = sourceActivity.getLocationType().getName();
            if (!Strings.isNullOrEmpty(sourceName)) {
                List<LocationType> locationTypes = em.createQuery("SELECT d FROM LocationType d WHERE Name = :activityName AND CountryId = :countryId")
                        .setParameter("activityName", sourceName)
                        .setParameter("countryId", targetDb.getCountry().getId())
                        .getResultList();
                if (!locationTypes.isEmpty()) {
                    newActivity.setLocationType(locationTypes.get(0));
                    locationTypeCreated = true;
                }
            }

            //2. if the source locationtype is bound to an adminlevel, choose the first root adminlevel in the new country
            if (!locationTypeCreated && sourceActivity.getLocationType().getBoundAdminLevel() != null) {
                List<LocationType> locationTypes = em.createQuery("SELECT d FROM LocationType d WHERE CountryId = :countryId")
                        .setParameter("countryId", targetDb.getCountry().getId())
                        .getResultList();
                if (!locationTypes.isEmpty()) {
                    newActivity.setLocationType(locationTypes.get(0));
                    locationTypeCreated = true;
                }
            }

            //3. Otherwise create new location type in the target country.
            if (!locationTypeCreated) {
                LocationType newLocationType = new LocationType();
                newLocationType.setName(sourceActivity.getLocationType().getName());
                newLocationType.setCountry(targetDb.getCountry());
                newLocationType.setWorkflowId(sourceActivity.getLocationType().getWorkflowId());
                newLocationType.setReuse(sourceActivity.getLocationType().isReuse());

                em.persist(newLocationType);

                newActivity.setLocationType(newLocationType);
            }
        }
    }

    private UserDatabase createDatabase(CloneDatabase command, User user) {
        UserDatabase db = new UserDatabase();
        db.setName(command.getName());
        db.setFullName(command.getDescription());
        db.setCountry(em.find(Country.class, command.getCountryId()));
        db.setOwner(user);

        em.persist(db);
        return db;
    }
}
