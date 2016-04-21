package org.activityinfo.legacy.shared.adapter;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.activityinfo.core.client.QueryResult;
import org.activityinfo.core.shared.application.ApplicationProperties;
import org.activityinfo.core.shared.application.FolderClass;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.core.shared.criteria.CriteriaIntersection;
import org.activityinfo.core.shared.criteria.FieldCriteria;
import org.activityinfo.core.shared.criteria.ParentCriteria;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.adapter.bindings.SiteBinding;
import org.activityinfo.legacy.shared.adapter.bindings.SiteBindingFactory;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.FormInstanceListResult;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormInstanceLabeler;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.ClassType;
import org.activityinfo.promise.ConcatList;
import org.activityinfo.promise.Promise;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.activityinfo.model.legacy.CuidAdapter.*;
import static org.activityinfo.promise.BiFunctions.concatMap;

/**
 * Given an intersection of Criteria, fetch the corresponding entities
 */
public class QueryExecutor {

    private final Dispatcher dispatcher;
    private final Criteria criteria;

    private CriteriaAnalysis criteriaAnalysis;


    public QueryExecutor(Dispatcher dispatcher, Criteria rootCriteria) {
        this.dispatcher = dispatcher;
        this.criteria = rootCriteria;
        this.criteriaAnalysis = CriteriaAnalysis.analyze(rootCriteria);
    }

    public Promise<QueryResult<FormInstance>> executeQuery() {
        return execute().then(new Function<List<FormInstance>, QueryResult<FormInstance>>() {
            @Nullable
            @Override
            public QueryResult<FormInstance> apply(List<FormInstance> input) {
                return QueryResult.fullResult(input);
            }
        });
    }

    public Promise<List<FormInstance>> execute() {

        if (criteriaAnalysis.isEmptySet()) {
            return emptySet();
        }

        if (criteriaAnalysis.isRestrictedToSingleClass()) {
            return queryByClassId(criteriaAnalysis.getClassRestriction());
        } else if (criteriaAnalysis.isRestrictedByUnionOfClasses()) {
            return queryByClassIds();
        } else if (criteriaAnalysis.isRestrictedByIdWithoutLegacyModel()) { // WITHOUT legacy model

            return queryByIds(criteriaAnalysis.getIdsWithoutLegacyModel());

        } else if (criteriaAnalysis.isRestrictedByIdWithLegacyModel()) { // WITH legacy model
            List<Promise<List<FormInstance>>> resultSets = Lists.newArrayList();
            for (Character domain : criteriaAnalysis.getIds().keySet()) {
                resultSets.add(queryByIds(domain, criteriaAnalysis.getIds().get(domain)));
            }
            return Promise.foldLeft(Collections.<FormInstance>emptyList(), new ConcatList<FormInstance>(), resultSets);

        } else if (criteriaAnalysis.isAncestorQuery()) {
            ParentCriteria.Parent parent = criteriaAnalysis.getParentCriteria();
            ResourceId parentId = parent.getParentId();

            if (parentId.equals(FolderListAdapter.HOME_ID) || parentId.getDomain() == DATABASE_DOMAIN ||
                    parentId.getDomain() == ACTIVITY_CATEGORY_DOMAIN) {
                return folders();
            } if (parentId.equals(FolderListAdapter.MY_DATABASES)) {
                return myDatabases();
            } else if (parentId.equals(FolderListAdapter.SHARED_DATABASES)) {
                return sharedDatabases();
            } else if (parentId.equals(FolderListAdapter.GEODB_ID)) {
                return countries();
            } else if (parentId.getDomain() == CuidAdapter.COUNTRY_DOMAIN) {
                return adminLevels(CuidAdapter.getLegacyIdFromCuid(parentId));
            } else if (ClassType.isClassType(parentId)) {
                return instancesByClassType(parent);
            } else {
                return queryByOwner(parent);
            }
        } else {
            throw new UnsupportedOperationException("queries must have either class criteria or parent criteria");
        }
    }

    private Promise<List<FormInstance>> instancesByClassType(ParentCriteria.Parent parent) {
        ResourceId parentId = parent.getParentId();

        if (parentId.equals(ClassType.LOCATION_TYPE.getResourceId())) {
            return locationTypes(parent);
        } else if (parentId.equals(ClassType.PROJECT.getResourceId())) {
            return projects(parent);
        } else if (parentId.equals(ClassType.PARTNER.getResourceId())) {
            return partner(parent);
        } else if (parentId.equals(ClassType.REPEATING.getResourceId())) {
            return queryByOwner(parent);
        } else {
            throw new UnsupportedOperationException("ClassType is not supported, classType: " + parentId);
        }
    }

    private Promise<List<FormInstance>> queryByOwner(final ParentCriteria.Parent parent) {
        GetFormInstance command = new GetFormInstance()
                .setOwnerIds(Lists.newArrayList(parent.getRestrictedBy().asString()))
                .setType(GetFormInstance.Type.OWNER)
                .setClassId(parent.getClassId().asString());
        return dispatcher.execute(command).then(FormInstanceListAdapter.getInstance());
    }

    private Promise<List<FormInstance>> partner(final ParentCriteria.Parent parent) {
        return dispatcher.execute(new GetSchema()).then(new Function<SchemaDTO, List<PartnerDTO>>() {
            @Override
            public List<PartnerDTO> apply(SchemaDTO schema) {
                int dbId = getDbIdFromRestrictedById(schema, parent.getRestrictedBy());
                return schema.getDatabaseById(dbId).getPartners();
            }
        }).then(new ListAdapter<>(new PartnerInstanceAdapter(parent.getParentId())));
    }

    private Promise<List<FormInstance>> projects(final ParentCriteria.Parent parent) {
        return dispatcher.execute(new GetSchema()).then(new Function<SchemaDTO, List<ProjectDTO>>() {
            @Override
            public List<ProjectDTO> apply(SchemaDTO schema) {
                int dbId = getDbIdFromRestrictedById(schema, parent.getRestrictedBy());
                return schema.getDatabaseById(dbId).getProjects();
            }
        }).then(new ListAdapter<>(new ProjectInstanceAdapter(parent.getParentId())));
    }

    private Promise<List<FormInstance>> locationTypes(final ParentCriteria.Parent parent) {
        return dispatcher.execute(new GetSchema()).then(new Function<SchemaDTO, List<LocationTypeDTO>>() {
            @Override
            public List<LocationTypeDTO> apply(SchemaDTO schema) {
                int dbId = getDbIdFromRestrictedById(schema, parent.getRestrictedBy());
                return schema.getDatabaseById(dbId).getCountry().getLocationTypes();
            }
        }).then(new ListAdapter<>(new LocationTypeInstanceAdapter()));
    }

    private static int getDbIdFromRestrictedById(SchemaDTO schema, ResourceId restrictedById) {
        char domain = restrictedById.getDomain();
        int legacyId = CuidAdapter.getLegacyIdFromCuid(restrictedById);

        if (domain == CuidAdapter.DATABASE_DOMAIN) {
            return legacyId;

        } else if (domain == ACTIVITY_DOMAIN) {
            return schema.getActivityById(legacyId).getDatabaseId();

        }
        throw new UnsupportedOperationException("Id is not supported: " + restrictedById);
    }

    private Promise<List<FormInstance>> adminLevels(int countryId) {
        return dispatcher.execute(new GetAdminLevelsByCountry(countryId)).then(new ListResultAdapter<>(new AdminLevelInstanceAdapter()));
    }

    private Promise<List<FormInstance>> queryByClassIds() {
        final Set<ResourceId> classCriteria = criteriaAnalysis.getClassCriteria();
        final List<Promise<List<FormInstance>>> resultSets = Lists.newArrayList();
        for (ResourceId classId : classCriteria) {
            resultSets.add(queryByClassId(classId));
        }
        return Promise.foldLeft(Collections.<FormInstance>emptyList(), new ConcatList<FormInstance>(), resultSets);
    }

    private Promise<List<FormInstance>> queryByIds(Collection<String> ids) {
        return dispatcher.execute(new GetFormInstance(ids)).then(new Function<FormInstanceListResult, List<FormInstance>>() {
            @Nullable
            @Override
            public List<FormInstance> apply(FormInstanceListResult formInstanceResult) {
                return formInstanceResult.getFormInstanceList();
            }
        });
    }

    private Promise<List<FormInstance>> queryByIds(char domain, Collection<Integer> ids) {
        switch (domain) {
            case ADMIN_ENTITY_DOMAIN:
                GetAdminEntities entityQuery = new GetAdminEntities();
                if (!ids.isEmpty()) {
                    entityQuery.setEntityIds(ids);
                }
                return dispatcher.execute(entityQuery).then(new ListResultAdapter<>(new AdminEntityInstanceAdapter()));

            case LOCATION_DOMAIN:
                return dispatcher.execute(new GetLocations(Lists.newArrayList(ids)))
                        .then(new ListResultAdapter<>(new LocationInstanceAdapter()));

            case COUNTRY_DOMAIN:
                return countries();

            case '_': // system objects
            case 'h': // home
            case DATABASE_DOMAIN:
            case ACTIVITY_CATEGORY_DOMAIN:
            case ACTIVITY_DOMAIN:
            case LOCATION_TYPE_DOMAIN:
                return folders();
        }
        throw new UnsupportedOperationException("unrecognized domain: " + domain);
    }

    private Promise<List<FormInstance>> countries() {
        return dispatcher.execute(new GetCountries()).then(new ListResultAdapter<>(new CountryInstanceAdapter()));
    }

    private static String labelFromFormInstance(FormInstance instance, ActivityFormDTO activityFormDTO) {

        ResourceId labelId = ResourceId.valueOf("label");
        FormClass formClass = new ActivityFormClassBuilder(activityFormDTO).build();

        // 1. If the FormClass has a field with the code "label" use that field as the label
        if (formClass.getField(labelId) != null) {

            FieldValue label = instance.get(labelId);
            if (label != null && !Strings.isNullOrEmpty(label.toString())) {
                return label.toString();
            }
        }

        // 2. IF the FormClass has a field with the super-property of "label", use that field as a label
        for (FormField field : formClass.getFields()) {
            if (field.isSubPropertyOf(labelId)) {
                FieldValue label = instance.get(field.getId());

                if (label != null && !Strings.isNullOrEmpty(label.toString())) {
                    return label.toString();
                }
            }
        }

        // 3. If the FormClass has any text fields, use the first as the label
        for (FormField field : formClass.getFields()) {
            if (field.getType() instanceof TextType) {
                FieldValue label = instance.get(field.getId());
                if (label != null && !Strings.isNullOrEmpty(label.toString())) {
                    return label.toString();
                }
            }
        }

        // 4. Otherwise use the form's ID.
        return instance.getId().asString();
    }

    private Promise<List<FormInstance>> queryByClassId(ResourceId formClassId) {
        if (formClassId.equals(FolderClass.CLASS_ID)) {
            return folders();
        } else if (formClassId.equals(ApplicationProperties.COUNTRY_CLASS)) {
            return countries();
        }

        switch (formClassId.getDomain()) {
            case ACTIVITY_DOMAIN:
                int activityId = CuidAdapter.getLegacyIdFromCuid(formClassId);
                final Promise<SiteResult> site = dispatcher.execute(GetSites.byActivity(activityId));
                final Promise<ActivityFormDTO> activityForm = dispatcher.execute(new GetActivityForm(activityId));

                return Promise.waitAll(site, activityForm).then(new Function<Void, List<FormInstance>>() {
                    @Override
                    public List<FormInstance> apply(Void input) {
                        SiteBinding binding = new SiteBindingFactory().apply(activityForm.get());

                        List<FormInstance> result = Lists.newArrayList();
                        if (site.get().getData() != null && !site.get().getData().isEmpty()) {
                            for (SiteDTO siteDTO : site.get().getData()) {
                                FormInstance instance = binding.newInstance(siteDTO);

                                FormInstanceLabeler.setLabel(instance, labelFromFormInstance(instance, activityForm.get()));
                                result.add(instance);
                            }
                        }
                        return result;
                    }
                });
            case ADMIN_LEVEL_DOMAIN:
                return dispatcher.execute(adminQuery(formClassId))
                        .then(new ListResultAdapter<>(new AdminEntityInstanceAdapter()));

            case LOCATION_TYPE_DOMAIN:
                return dispatcher.execute(composeLocationQuery(formClassId))
                        .then(new ListResultAdapter<>(new LocationInstanceAdapter()));

            case PARTNER_FORM_CLASS_DOMAIN:
                return dispatcher.execute(new GetSchema())
                        .then(new PartnerListExtractor(criteria))
                        .then(concatMap(new PartnerInstanceAdapter(formClassId)));
            case PROJECT_CLASS_DOMAIN:
                return dispatcher.execute(new GetSchema())
                        .then(new ProjectListExtractor(criteria))
                        .then(concatMap(new ProjectInstanceAdapter(formClassId)));
            default:
                GetFormInstance command = new GetFormInstance()
                        .setClassId(formClassId.asString())
                        .setType(GetFormInstance.Type.CLASS);
                return dispatcher.execute(command).then(FormInstanceListAdapter.getInstance());
        }
    }

    private GetAdminEntities adminQuery(ResourceId formClassId) {
        GetAdminEntities query = new GetAdminEntities();
        query.setLevelId(CuidAdapter.getLegacyIdFromCuid(formClassId));

        Multimap<Character, Integer> ids = criteriaAnalysis.getIds();
        if (!ids.get(ADMIN_ENTITY_DOMAIN).isEmpty()) {
            query.setEntityIds(ids.get(ADMIN_ENTITY_DOMAIN));
        }
        if (criteria instanceof CriteriaIntersection) {
            for (Criteria element : ((CriteriaIntersection) criteria).getElements()) {
                if (element instanceof FieldCriteria) {
                    FieldCriteria fieldCriteria = (FieldCriteria) element;
                    if (fieldCriteria.getFieldId().equals(CuidAdapter.field(formClassId, ADMIN_PARENT_FIELD))) {
                        ReferenceValue id = (ReferenceValue) fieldCriteria.getValue();

                        query.setParentId(CuidAdapter.getLegacyIdFromCuid(Iterables.getOnlyElement(id.getResourceIds())));
                    }
                }
            }
        }

        return query;
    }

    private Promise<List<FormInstance>> folders() {
        return dispatcher.execute(new GetSchema()).then(new FolderListAdapter(criteria));
    }

    private Promise<List<FormInstance>> myDatabases() {
        return dispatcher.execute(new GetSchema()).then(new MyDatabasesAdapter());
    }

    private Promise<List<FormInstance>> sharedDatabases() {
        return dispatcher.execute(new GetSchema()).then(new SharedDatabasesAdapter());
    }

    private GetLocations composeLocationQuery(ResourceId formClassId) {
        int locationTypeId = CuidAdapter.getLegacyIdFromCuid(formClassId);
        GetLocations searchLocations = new GetLocations();
        searchLocations.setLocationTypeId(locationTypeId);
        searchLocations.setLocationIds(criteriaAnalysis.getIds(CuidAdapter.LOCATION_DOMAIN));
        return searchLocations;
    }

    private Promise<List<FormInstance>> emptySet() {
        return Promise.resolved(Collections.<FormInstance>emptyList());
    }

}
