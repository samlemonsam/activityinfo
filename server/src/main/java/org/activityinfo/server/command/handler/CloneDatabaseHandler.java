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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.activityinfo.json.Json;
import org.activityinfo.legacy.shared.command.CloneDatabase;
import org.activityinfo.legacy.shared.command.UpdatePartner;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.database.UserPermissionModel;
import org.activityinfo.model.form.*;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.*;
import org.activityinfo.server.database.hibernate.entity.*;
import org.activityinfo.store.mysql.MySqlStorageProvider;
import org.activityinfo.store.spi.DatabaseProvider;
import org.activityinfo.store.spi.FormStorageProvider;

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
    private final DatabaseProvider databaseProvider;
    private final KeyGenerator generator = new KeyGenerator();
    private final Provider<FormStorageProvider> formCatalog;

    // Mappings old id (source db) -> new id (target/newly created db)
    private final Map<Integer, Activity> activityMapping = Maps.newHashMap();
    private final Map<ResourceId, ResourceId> typeIdMapping = Maps.newHashMap();

    // Maps old folder id -> new folder object
    private final Map<Integer, Folder> folderMapping = Maps.newHashMap();

    private Database targetDb;
    private Database sourceDb;
    private UserDatabaseMeta sourceDbMeta;

    @Inject
    public CloneDatabaseHandler(Provider<EntityManager> entityManager,
                                DatabaseProvider databaseProvider,
                                Provider<FormStorageProvider> formCatalog) {
        this.em = entityManager.get();
        this.databaseProvider = databaseProvider;
        this.formCatalog = formCatalog;
    }

    @Override
    public CommandResult execute(CloneDatabase command, User user) {

        this.targetDb = createDatabase(command, user);
        this.sourceDb = em.find(Database.class, command.getSourceDatabaseId());
        Optional<UserDatabaseMeta> sourceMetadata = databaseProvider.getDatabaseMetadata(command.getSourceDatabaseId(), user.getId());

        assert sourceDb != null && sourceMetadata.isPresent();
        this.sourceDbMeta = sourceMetadata.get();

        int defaultPartnerId = createDefaultPartner(user);

        if (!PermissionOracle.canView(sourceDbMeta)) {
            throw new IllegalAccessCommandException();
        }

        // 1. copy partners and keep mapping between old and new partners
        if (command.isCopyPartners() || command.isCopyUserPermissions()) {
            copyPartners();
        }

        // 2. copy user permissions
        if (command.isCopyUserPermissions() && PermissionOracle.canManageUsersOnWholeDatabase(sourceDbMeta)) {
            copyUserPermissions(defaultPartnerId);
        }

        // 3. copy forms and form data
        copyForms();

        // 4. Map old folder ids to new folder ids on permission model
        if (command.isCopyUserPermissions() && PermissionOracle.canManageUsersOnWholeDatabase(sourceDbMeta)) {
            mapFolderPermissions();
        }
        
        return new CreateResult(targetDb.getId());
    }

    private int createDefaultPartner(User user) {
        Preconditions.checkNotNull(targetDb);
        Preconditions.checkState(targetDb.getId() > 0);

        PartnerDTO partner = new PartnerDTO();
        partner.setName("Default");

        CommandResult newPartnerId = new UpdatePartnerHandler(em, databaseProvider).execute(new UpdatePartner(targetDb.getId(), partner), user);
        return ((CreateResult) newPartnerId).getNewId();
    }

    private void copyUserPermissions(int defaultPartnerId) {
        Partner defaultPartner = em.find(Partner.class, defaultPartnerId);
        for (UserPermission sourcePermission : sourceDb.getUserPermissions()) {
            UserPermission newPermission = new UserPermission(sourcePermission);
            newPermission.setDatabase(targetDb);
            newPermission.setLastSchemaUpdate(new Date());
            newPermission.setPartner(copyPartnerAssignment(sourcePermission.getPartner(), defaultPartner));

            em.persist(newPermission);
            targetDb.getUserPermissions().add(newPermission);
        }
    }

    private Partner copyPartnerAssignment(Partner partner, Partner defaultPartner) {
        if (PartnerDTO.DEFAULT_PARTNER_NAME.equals(partner.getName())) {
            return defaultPartner;
        }

        return partner;
    }

    private void mapFolderPermissions() {

        for (UserPermission permission : targetDb.getUserPermissions()) {
            if (Strings.isNullOrEmpty(permission.getModel())) {
                continue;
            }

            UserPermissionModel sourceModel = UserPermissionModel.fromJson(Json.parse(permission.getModel()));
            List<GrantModel> destinationGrants = new ArrayList<>();

            for (GrantModel sourceGrant : sourceModel.getGrants()) {
                if (sourceGrant.getResourceId().getDomain() != CuidAdapter.FOLDER_DOMAIN) {
                    continue;
                }
                int folderId = CuidAdapter.getLegacyIdFromCuid(sourceGrant.getResourceId());
                if (!folderMapping.containsKey(folderId)) {
                    // folder not copied - do not add grant to new permission model
                    continue;
                }
                Folder destinationFolder = folderMapping.get(folderId);
                destinationGrants.add(new GrantModel.Builder()
                    .setResourceId(CuidAdapter.folderId(destinationFolder.getId()))
                    .build());
            }

            UserPermissionModel destinationModel = new UserPermissionModel(
                    permission.getUser().getId(),
                    targetDb.getId(),
                    destinationGrants);
            permission.setModel(destinationModel.toJson().toJson());
        }
    }

    private void copyPartners() {
        for (Partner partner : sourceDb.getPartners()) {
            if (!PartnerDTO.DEFAULT_PARTNER_NAME.equals(partner.getName())) {
                targetDb.getPartners().add(partner);
            }
        }

        targetDb.setLastSchemaUpdate(new Date());
        em.persist(targetDb);
    }

    private void copyForms() {

        // first copy all activities without payload (indicators, attributes)
        for (Activity activity : sourceDb.getActivities()) {
            if (activity.isDeleted()) {
                // skip current activity if deleted...
                continue;
            }
            copyActivity(activity);
        }

        for (Activity activity : sourceDb.getActivities()) {
            if (activity.isDeleted()) {
                // skip current activity if deleted...
                continue;
            }
            final ResourceId sourceFormId = activityFormClass(activity.getId());
            final ResourceId targetFormId = activityFormClass(activityMapping.get(activity.getId()).getId());

            if (activity.isClassicView()) {
                copyClassicActivity(activity, activityMapping.get(activity.getId()));
            } else {
                FormClass targetFormClass = copyFormClass(sourceFormId, targetFormId);
                formCatalog.get().getForm(targetFormId).get().updateFormClass(targetFormClass);
            }
        }
    }

    private Activity copyClassicActivity(final Activity sourceActivity,
                                          final Activity targetActivity) {

        // copy attribute groups
        for (AttributeGroup sourceGroup : sourceActivity.getAttributeGroups()) {
            copyAttributeGroup(sourceGroup, targetActivity);
        }

        // copy indicators
        for (Indicator sourceIndicator : sourceActivity.getIndicators()) {
            copyIndicator(sourceIndicator, targetActivity);
        }

        return targetActivity;
    }

    private void copyAttributeGroup(AttributeGroup sourceGroup, Activity targetActivity) {
        AttributeGroup targetGroup = new AttributeGroup(sourceGroup);
        targetGroup.setId(generator.generateInt());

        em.persist(targetGroup);
        targetActivity.getAttributeGroups().add(targetGroup);

        for (Attribute sourceAttribute : sourceGroup.getAttributes()) {
            copyAttribute(sourceAttribute, targetGroup);
        }
    }

    private void copyAttribute(Attribute sourceAttribute, AttributeGroup targetGroup) {
        Attribute targetAttribute = new Attribute(sourceAttribute);
        targetAttribute.setId(generator.generateInt());
        targetAttribute.setGroup(targetGroup);

        em.persist(targetAttribute);
    }

    private void copyIndicator(Indicator sourceIndicator, Activity targetActivity) {
        Indicator targetIndicator = new Indicator(sourceIndicator);
        targetIndicator.setId(generator.generateInt());
        targetIndicator.setActivity(targetActivity);

        em.persist(targetIndicator);
    }

    private FormClass copyFormClass(final ResourceId sourceFormId,
                                    final ResourceId targetFormId) {

        FormClass sourceFormClass = formCatalog.get().getFormClass(sourceFormId);
        FormClass targetFormClass = new FormClass(targetFormId);

        if(sourceFormClass.isSubForm()) {
            targetFormClass.setSubFormKind(sourceFormClass.getSubFormKind());
            ResourceId targetParentFormId = this.typeIdMapping.get(sourceFormClass.getParentFormId().get());
            if(targetParentFormId == null) {
                LOGGER.severe(String.format("Parent (%s) of subform (%s) was not copied",
                    sourceFormClass.getParentFormId(),
                    sourceFormId));
                throw new IllegalStateException("Parent form has not been copied!");
            }
            targetFormClass.setParentFormId(targetParentFormId);
        }

        targetFormClass.setLabel(sourceFormClass.getLabel());
        targetFormClass.setDescription(sourceFormClass.getDescription());
        targetFormClass.setDatabaseId(CuidAdapter.databaseId(targetDb.getId()));

        Map<FormElement, FormElement> sourceIdToTargetFormElementMapping = Maps.newHashMap();
        copyFormElements(sourceFormClass, targetFormClass, sourceFormClass.getId(), targetFormClass.getId(),
            sourceIdToTargetFormElementMapping);
        correctRelevanceConditions(targetFormClass, sourceIdToTargetFormElementMapping);

        return targetFormClass;
    }

    private void copyFormElements(FormElementContainer sourceContainer, FormElementContainer targetContainer,
                                  ResourceId sourceFormId, ResourceId targetFormId, Map<FormElement, FormElement> sourceIdToTargetFormElementMapping) {
        for (FormElement element : sourceContainer.getElements()) {
            if (element instanceof FormSection) {
                FormSection sourceSection = (FormSection) element;
                FormSection targetSection = new FormSection(ResourceId.generateId());
                targetSection.setLabel(sourceSection.getLabel());

                targetContainer.addElement(targetSection);
                sourceIdToTargetFormElementMapping.put(sourceSection, targetSection);

                copyFormElements(sourceSection, targetSection, sourceFormId, targetFormId, sourceIdToTargetFormElementMapping);
            } else if (element instanceof FormField) {
                FormField sourceField = (FormField) element;
                FormField targetField = new FormField(targetFieldId(sourceField, sourceFormId, targetFormId));

                targetField.setType(copyType(sourceField));
                targetField.setCode(sourceField.getCode());
                targetField.setRelevanceConditionExpression(sourceField.getRelevanceConditionExpression());
                targetField.setLabel(sourceField.getLabel());
                targetField.setDescription(sourceField.getDescription());
                targetField.setRequired(sourceField.isRequired());
                targetField.setSuperProperties(sourceField.getSuperProperties());
                targetField.setVisible(sourceField.isVisible());
                targetField.setKey(sourceField.isKey());

                targetContainer.addElement(targetField);
                sourceIdToTargetFormElementMapping.put(sourceField, targetField);
            } else {
                throw new UnsupportedOperationException("Unsupported FormElement : " + element);
            }
        }
    }

    private void correctRelevanceConditions(FormClass targetFormClass, Map<FormElement, FormElement> sourceIdToTargetFormElementMapping) {
        for (FormField field : targetFormClass.getFields()) {
            field.setRelevanceConditionExpression(
                replaceSourceIdsToTargetIds(field.getRelevanceConditionExpression(),
                    sourceIdToTargetFormElementMapping));
        }
    }

    private String replaceSourceIdsToTargetIds(String expression, Map<FormElement, FormElement> sourceIdToTargetFormElementMapping) {
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

    /**
     * Copies a field type.
     *
     * Most field types can be copied as-is, but some field types, for example,
     * reference fields, require special handling.
     */
    private FieldType copyType(FormField sourceField) {
        return sourceField.getType().accept(new FieldTypeVisitor<FieldType>() {
            @Override
            public FieldType visitAttachment(AttachmentType attachmentType) {
                return attachmentType;
            }

            @Override
            public FieldType visitCalculated(CalculatedFieldType calculatedFieldType) {
                return calculatedFieldType;
            }

            @Override
            public FieldType visitReference(ReferenceType referenceType) {
                return copyReferenceType(referenceType);
            }

            @Override
            public FieldType visitNarrative(NarrativeType narrativeType) {
                return narrativeType;
            }

            @Override
            public FieldType visitBoolean(BooleanType booleanType) {
                return booleanType;
            }

            @Override
            public FieldType visitQuantity(QuantityType type) {
                return type;
            }

            @Override
            public FieldType visitGeoPoint(GeoPointType geoPointType) {
                return geoPointType;
            }

            @Override
            public FieldType visitGeoArea(GeoAreaType geoAreaType) {
                return geoAreaType;
            }

            @Override
            public FieldType visitEnum(EnumType enumType) {
                return cloneEnumType(enumType);
            }

            @Override
            public FieldType visitBarcode(BarcodeType barcodeType) {
                return barcodeType;
            }

            @Override
            public FieldType visitSubForm(SubFormReferenceType subFormReferenceType) {
                return copySubForm(subFormReferenceType);
            }

            @Override
            public FieldType visitLocalDate(LocalDateType localDateType) {
                return localDateType;
            }

            @Override
            public FieldType visitMonth(MonthType monthType) {
                return monthType;
            }

            @Override
            public FieldType visitWeek(EpiWeekType epiWeekType) {
                return epiWeekType;
            }

            @Override
            public FieldType visitYear(YearType yearType) {
                return yearType;
            }

            @Override
            public FieldType visitFortnight(FortnightType fortnightType) {
                return fortnightType;
            }

            @Override
            public FieldType visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
                return localDateIntervalType;
            }

            @Override
            public FieldType visitText(TextType textType) {
                return textType;
            }

            @Override
            public FieldType visitSerialNumber(SerialNumberType serialNumberType) {
                return serialNumberType;
            }
        });
    }


    /**
     * Copies a reference type, updating any references to forms within the source
     * database to the newly cloned forms in the target database.
     */
    private FieldType copyReferenceType(ReferenceType sourceType) {

        Collection<ResourceId> targetRange = new HashSet<>();
        for (ResourceId sourceFormId : sourceType.getRange()) {
            targetRange.add(copyReference(sourceFormId));
        }

        return new ReferenceType()
                .setCardinality(sourceType.getCardinality())
                .setRange(targetRange);
    }

    /**
     * Copies a reference, changing references to forms within the source database
     * to the cloned forms in the target database.
     */
    private ResourceId copyReference(ResourceId sourceFormId) {
        switch (sourceFormId.getDomain()) {

            // Reference the NEW database's partner form
            case CuidAdapter.PARTNER_FORM_CLASS_DOMAIN:
                ResourceId partnerId = CuidAdapter.partnerFormId(targetDb.getId());
                typeIdMapping.put(sourceFormId, partnerId);
                return partnerId;

            // Reference the NEW database's project form
            case CuidAdapter.PROJECT_CLASS_DOMAIN:
                ResourceId projectId = CuidAdapter.projectFormClass(targetDb.getId());
                typeIdMapping.put(sourceFormId, projectId);
                return projectId;

            // If this references a form in the source database, change
            // the reference to point to the clone of that form
            case CuidAdapter.ACTIVITY_DOMAIN:
                int sourceActivityId = CuidAdapter.getLegacyIdFromCuid(sourceFormId);
                Activity targetActivity = this.activityMapping.get(sourceActivityId);
                if(targetActivity != null) {
                    return CuidAdapter.activityFormClass(targetActivity.getId());
                } else {
                    // if this is an activity in another database, then keep the
                    // reference as-is
                    return sourceFormId;
                }

            // If this form doesn't live in the source database, for example, it
            // could be an administrative level form, then don't change the reference at all.
            default:
                return sourceFormId;
        }
    }

    /**
     * Copies an {@link EnumType}, generating a new unique identifier each
     * enumerated item, since the MySQL backend requires them to be globally unique.
     */
    private FieldType cloneEnumType(EnumType sourceEnumType) {

        List<EnumItem> targetValues = Lists.newArrayList();
        for (EnumItem sourceValue : sourceEnumType.getValues()) {
            ResourceId targetValueId = newEnumId(sourceValue);
            targetValues.add(new EnumItem(targetValueId, sourceValue.getLabel()));
            typeIdMapping.put(sourceValue.getId(), targetValueId);
        }
        return new EnumType(sourceEnumType.getCardinality(), targetValues);
    }

    private ResourceId newEnumId(EnumItem sourceValue) {
        return CuidAdapter.cuid(sourceValue.getId().getDomain(), generator.generateInt());
    }

    private ResourceId targetFieldId(FormField sourceField, ResourceId sourceClassId, ResourceId targetClassId) {
        ResourceId sourceFieldId = sourceField.getId();

        for (int fieldIndex : BUILTIN_FIELDS) {
            if (sourceFieldId.equals(CuidAdapter.field(sourceClassId, fieldIndex))) {
                return CuidAdapter.field(targetClassId, fieldIndex);
            }
        }

        // Fields stored in the indicator and attribute group tables need to
        // have a GLOBALLY unique identifier, otherwise they will clash with other forms
        if(sourceField.getId().getDomain() == CuidAdapter.INDICATOR_DOMAIN ||
           sourceField.getId().getDomain() == CuidAdapter.ATTRIBUTE_GROUP_FIELD_DOMAIN ) {

            return CuidAdapter.cuid(sourceField.getId().getDomain(), generator.generateInt());
        }

        // Otherwise, the field id doesn't need to be changed
        return sourceFieldId;
    }


    private FieldType copySubForm(SubFormReferenceType subFormType) {

        ResourceId oldSubFormId = subFormType.getClassId();
        ResourceId newSubFormId = ResourceId.generateId();

        typeIdMapping.put(oldSubFormId, newSubFormId);

        FormClass targetFormClass = copyFormClass(oldSubFormId, newSubFormId);

        MySqlStorageProvider mysqlStorage = (MySqlStorageProvider) this.formCatalog.get();
        mysqlStorage.createOrUpdateFormSchema(targetFormClass);

        return new SubFormReferenceType(newSubFormId);
    }

    private Activity copyActivity(Activity sourceActivity) {
        Activity newActivity = new Activity(sourceActivity); // copy simple values : like name, category (but not Indicators, Attributes)
        newActivity.getAttributeGroups().clear();
        newActivity.getLockedPeriods().clear();
        newActivity.getIndicators().clear();

        // target db
        newActivity.setDatabase(targetDb);

        copyFolder(sourceActivity, newActivity);
        setLocationTypeForNewActivity(sourceActivity, newActivity);

        em.persist(newActivity); // persist to get id of new activity
        activityMapping.put(sourceActivity.getId(), newActivity);
        typeIdMapping.put(
            CuidAdapter.activityFormClass(sourceActivity.getId()),
            CuidAdapter.activityFormClass(newActivity.getId()));

        return newActivity;
    }

    private void copyFolder(Activity sourceActivity, Activity newActivity) {
        Folder sourceFolder = sourceActivity.getFolder();
        Folder destinationFolder;

        if (sourceFolder == null) {
            return;
        }

        if (folderMapping.containsKey(sourceFolder.getId())) {
            destinationFolder = folderMapping.get(sourceFolder.getId());
        } else {
            destinationFolder = new Folder(sourceFolder);
            destinationFolder.setDatabase(targetDb);
            em.persist(destinationFolder);

            folderMapping.put(sourceFolder.getId(),destinationFolder);
        }

        newActivity.setFolder(destinationFolder);
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

    private Database createDatabase(CloneDatabase command, User user) {
        Database db = new Database();
        db.setName(command.getName());
        db.setFullName(command.getDescription());
        db.setCountry(em.find(Country.class, command.getCountryId()));
        db.setOwner(user);

        em.persist(db);
        return db;
    }
}
