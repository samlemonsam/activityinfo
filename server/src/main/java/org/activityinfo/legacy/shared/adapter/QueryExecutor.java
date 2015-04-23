package org.activityinfo.legacy.shared.adapter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.activityinfo.core.shared.application.ApplicationProperties;
import org.activityinfo.core.shared.application.FolderClass;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.core.shared.criteria.CriteriaIntersection;
import org.activityinfo.core.shared.criteria.FieldCriteria;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.promise.ConcatList;
import org.activityinfo.promise.Promise;

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

    public Promise<List<FormInstance>> execute() {

        if (criteriaAnalysis.isEmptySet()) {
            return emptySet();
        }

        if (criteriaAnalysis.isRestrictedToSingleClass()) {
            return queryByClassId(criteriaAnalysis.getClassRestriction());
        } else if (criteriaAnalysis.isRestrictedByUnionOfClasses()) {
            return queryByClassIds();
        } else if (criteriaAnalysis.isRestrictedById()) {
            List<Promise<List<FormInstance>>> resultSets = Lists.newArrayList();
            for (Character domain : criteriaAnalysis.getIds().keySet()) {
                resultSets.add(queryByIds(domain, criteriaAnalysis.getIds().get(domain)));
            }
            return Promise.foldLeft(Collections.<FormInstance>emptyList(), new ConcatList<FormInstance>(), resultSets);

        } else if (criteriaAnalysis.isAncestorQuery()) {
            ResourceId parentId = criteriaAnalysis.getParentCriteria();

            if (parentId.equals(FolderListAdapter.HOME_ID) || parentId.getDomain() == DATABASE_DOMAIN ||
                parentId.getDomain() == ACTIVITY_CATEGORY_DOMAIN) {
                return folders();
            } else if (parentId.equals(FolderListAdapter.GEODB_ID)) {
                return countries();
            } else if (parentId.getDomain() == CuidAdapter.COUNTRY_DOMAIN) {
                return adminLevels(CuidAdapter.getLegacyIdFromCuid(parentId));
            } else {
                throw new UnsupportedOperationException("parentID " + parentId);
            }
        } else {
            throw new UnsupportedOperationException("queries must have either class criteria or parent criteria");
        }
    }

    private Promise<List<FormInstance>> adminLevels(int countryId) {
        GetAdminLevels query = new GetAdminLevels();
        query.setCountryId(countryId);

        return dispatcher.execute(query).then(new ListResultAdapter<>(new AdminLevelInstanceAdapter()));
    }

    private Promise<List<FormInstance>> queryByClassIds() {
        final Set<ResourceId> classCriteria = criteriaAnalysis.getClassCriteria();
        final List<Promise<List<FormInstance>>> resultSets = Lists.newArrayList();
        for (ResourceId classId : classCriteria) {
            resultSets.add(queryByClassId(classId));
        }
        return Promise.foldLeft(Collections.<FormInstance>emptyList(), new ConcatList<FormInstance>(), resultSets);
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

    private Promise<List<FormInstance>> queryByClassId(ResourceId formClassId) {
        if (formClassId.equals(FolderClass.CLASS_ID)) {
            return folders();
        } else if (formClassId.equals(ApplicationProperties.COUNTRY_CLASS)) {
            return countries();
        }

        switch (formClassId.getDomain()) {
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
                return Promise.rejected(new UnsupportedOperationException(
                        "domain not yet implemented: " + formClassId.getDomain()));
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
