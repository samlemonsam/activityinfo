package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;

import java.util.List;

/**
 * Extracts a list a given database's Partners from the SchemaDTO.
 */
public class PartnerListExtractor implements Function<SchemaDTO, List<PartnerDTO>> {

    private final Predicate<ResourceId> formClassCriteria;

    public PartnerListExtractor(Criteria criteria) {
        this.formClassCriteria = CriteriaEvaluation.evaluatePartiallyOnClassId(criteria);
    }

    @Override
    public List<PartnerDTO> apply(SchemaDTO input) {
        List<PartnerDTO> results = Lists.newArrayList();
        for (UserDatabaseDTO db : input.getDatabases()) {
            ResourceId formClassId = CuidAdapter.partnerFormClass(db.getId());
            if (formClassCriteria.apply(formClassId)) {
                results.addAll(db.getPartners());
            }
        }
        return results;
    }
}
