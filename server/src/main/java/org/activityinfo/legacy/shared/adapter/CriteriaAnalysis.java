package org.activityinfo.legacy.shared.adapter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.activityinfo.core.shared.criteria.*;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;
import java.util.Set;

/**
 * Created by alex on 3/15/14.
 */
public class CriteriaAnalysis extends CriteriaVisitor {

    /**
     * Instances must be a subclass of all of these FormClasses
     */
    private final Set<ResourceId> classCriteria = Sets.newHashSet();

    private final Set<ParentCriteria.Parent> parentCriteria = Sets.newHashSet();

    private boolean rootOnly = false;
    private boolean classUnion = true;

    /**
     * Must be one of these ids
     */
    private final Multimap<Character, Integer> ids = HashMultimap.create();

    /**
     * List of instance ids not mapped to legacy model (saved in forminstance table)
     */
    private final List<String> idsWithoutLegacyModel = Lists.newArrayList();

    public ParentCriteria.Parent getParentCriteria() {
        return parentCriteria.iterator().next();
    }

    public boolean isRootOnly() {
        return rootOnly;
    }

    @Override
    public void visitClassCriteria(ClassCriteria criteria) {
        classCriteria.add(criteria.getClassId());
    }

    @Override
    public void visitInstanceIdCriteria(IdCriteria criteria) {
        // this is implicitly a union criteria
        // separate the instances out into domains
        for (ResourceId id : criteria.getInstanceIds()) {
            assert id != null : "ids cannot be null";
            if (criteria.isMappedToLegacyModel()) {
                if (id.getDomain() != CuidAdapter.ACTIVITY_CATEGORY_DOMAIN) {
                    ids.put(id.getDomain(), CuidAdapter.getLegacyIdFromCuid(id));
                }
            } else {
                if (id.getDomain() == ResourceId.GENERATED_ID_DOMAIN) {
                    idsWithoutLegacyModel.add(id.asString());
                }
            }
        }
    }

    @Override
    public void visitParentCriteria(ParentCriteria criteria) {
        if (criteria.selectsRoot()) {
            rootOnly = true;
        } else {
            parentCriteria.add(criteria.getParent());
        }
    }

    @Override
    public void visitIntersection(CriteriaIntersection intersection) {
        // A ∩ (B ∩ C) = A ∩ B ∩ C
        for (Criteria criteria : intersection) {
            criteria.accept(this);
        }
    }

    @Override
    public void visitUnion(CriteriaUnion criteriaUnion) {
        classUnion = true; // todo temp fix! - in general wrong approach, will work in flat case only!
        for (Criteria criteria : criteriaUnion.getElements()) {
            if (classUnion && !(criteria instanceof ClassCriteria)) {
                classUnion = false;
            }
            criteria.accept(this);
        }
    }

    public boolean isEmptySet() {
        if (classCriteria.size() > 1 && !classUnion) {
            // a single instance cannot (at this time) be a member of more than one
            // class, so the result of this query is logically the empty set
            return true;
        }

        if (parentCriteria.size() > 1 || (rootOnly && !parentCriteria.isEmpty())) {
            // likewise, a single instance cannot be a child of multiple parents, so
            // the result of this query is logically the empty set
            return true;
        }

        return false;
    }

    public boolean isRestrictedToSingleClass() {
        return classCriteria.size() == 1;
    }

    public boolean isRestrictedByUnionOfClasses() {
        return classUnion && !classCriteria.isEmpty();
    }

    public boolean isRestrictedByIdWithLegacyModel() {
        return !ids.isEmpty();
    }

    public boolean isRestrictedByIdWithoutLegacyModel() {
        return !idsWithoutLegacyModel.isEmpty();
    }

    public boolean isLocationQuery() {
        return isRestrictedToSingleClass() && getClassRestriction().getDomain() == CuidAdapter.LOCATION_TYPE_DOMAIN;
    }

    public boolean isSiteQuery() {
        return isRestrictedToSingleClass() && getClassRestriction().getDomain() == CuidAdapter.ACTIVITY_DOMAIN;
    }

    public boolean isAncestorQuery() {
        return rootOnly || !parentCriteria.isEmpty();
    }

    public ResourceId getClassRestriction() {
        return classCriteria.iterator().next();
    }

    public Set<ResourceId> getClassCriteria() {
        return classCriteria;
    }

    public Multimap<Character, Integer> getIds() {
        return ids;
    }

    public List<Integer> getIds(char domain) {
        return Lists.newArrayList(ids.get(domain));
    }

    public List<String> getIdsWithoutLegacyModel() {
        return idsWithoutLegacyModel;
    }

    public static CriteriaAnalysis analyze(Criteria criteria) {
        CriteriaAnalysis analysis = new CriteriaAnalysis();
        criteria.accept(analysis);
        return analysis;
    }
}
