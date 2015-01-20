package org.activityinfo.legacy.shared.adapter.projection;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.legacy.shared.command.result.ListResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;


public class SiteProjector implements Function<ListResult<SiteDTO>, List<Projection>> {

    private final List<ProjectionUpdater<LocationDTO>> locationProjectors;
    private final List<ProjectionUpdater<PartnerDTO>> partnerProjectors = Lists.newArrayList();
    private final List<IndicatorProjectionUpdater> indicatorProjectors = Lists.newArrayList();
    private final List<ProjectionUpdater<SiteDTO>> siteProjectors = Lists.newArrayList();
    private final List<AttributeProjectionUpdater> attributeProjectors = Lists.newArrayList();
    private final List<ProjectionUpdater<ProjectDTO>> projectProjectors = Lists.newArrayList();

    private final ActivityFormDTO activity;
    private final Criteria criteria;

    public SiteProjector(ActivityFormDTO activity, Criteria criteria, List<FieldPath> fields) {
        this.activity = activity;
        this.criteria = criteria;
        locationProjectors = LocationProjector.createLocationUpdaters(fields);
        for (FieldPath path : fields) {
            ResourceId fieldId = path.getLeafId();

            if (fieldId.getDomain() == CuidAdapter.PARTNER_FORM_CLASS_DOMAIN) {
                int databaseId = CuidAdapter.getBlock(fieldId, 0);
                int fieldIndex = CuidAdapter.getBlock(fieldId, 1);
                partnerProjectors.add(new PartnerProjectionUpdater(path, databaseId, fieldIndex));
            } else if (fieldId.getDomain() == CuidAdapter.INDICATOR_DOMAIN) {
                int indicatorId = CuidAdapter.getLegacyIdFromCuid(fieldId);
                indicatorProjectors.add(new IndicatorProjectionUpdater(path, indicatorId));
            } else if (fieldId.getDomain() == CuidAdapter.ACTIVITY_DOMAIN) {
                int fieldIndex = CuidAdapter.getBlock(fieldId, 1);
                siteProjectors.add(new SiteProjectionUpdater(path, fieldIndex));
            } else if (fieldId.getDomain() == CuidAdapter.ATTRIBUTE_GROUP_FIELD_DOMAIN) {
                attributeProjectors.add(new AttributeProjectionUpdater(path));
            } else if (fieldId.getDomain() == CuidAdapter.PROJECT_CLASS_DOMAIN) {
                int fieldIndex = CuidAdapter.getBlock(fieldId, 1);
                projectProjectors.add(new ProjectProjectionUpdater<ProjectDTO>(path, fieldIndex));
            }
        }
    }

    @Override
    public List<Projection> apply(ListResult<SiteDTO> input) {
        List<Projection> projections = Lists.newArrayList();
        for (SiteDTO site : input.getData()) {
            Projection projection = new Projection(site.getInstanceId(), site.getFormClassId());
            for (ProjectionUpdater<PartnerDTO> projector : partnerProjectors) {
                projector.update(projection, site.getPartner());
            }
            for (ProjectionUpdater<LocationDTO> projector : locationProjectors) {
                projector.update(projection, site.getLocation());
            }
            for (ProjectionUpdater<ProjectDTO> projector : projectProjectors) {
                projector.update(projection, site.getProject());
            }

            for (String propertyName : site.getPropertyNames()) {
                if (propertyName.startsWith(IndicatorDTO.PROPERTY_PREFIX)) {
                    Object value = site.get(propertyName);

                    for (IndicatorProjectionUpdater projector : indicatorProjectors) {
                        if (projector.getIndicatorId() == IndicatorDTO.indicatorIdForPropertyName(propertyName)) {
                            if (value instanceof Number) {
                                final double doubleValue = ((Number) value).doubleValue();
                                projector.update(projection, doubleValue);
                            } else {
                                projector.update(projection, value);
                            }
                        }
                    }
                } else if (propertyName.startsWith(AttributeDTO.PROPERTY_PREFIX) &&
                        site.get(propertyName) == Boolean.TRUE) {
                    final AttributeDTO attributeById = activity.getAttributeById(AttributeDTO.idForPropertyName(
                            propertyName));
                    AttributeGroupDTO attributeGroup = activity.getAttributeGroupByAttributeId(attributeById.getId());
                    for (AttributeProjectionUpdater projector : attributeProjectors) {
                        if (CuidAdapter.getLegacyIdFromCuid(projector.getAttributeGroupId()) == attributeGroup.getId()) {
                            projector.update(projection, attributeById);
                        }
                    }
                }
            }

            for (ProjectionUpdater<SiteDTO> projector : siteProjectors) {
                projector.update(projection, site);
            }

            if (criteria.apply(projection)) {
                projections.add(projection);
            }
        }
        return projections;
    }
}
