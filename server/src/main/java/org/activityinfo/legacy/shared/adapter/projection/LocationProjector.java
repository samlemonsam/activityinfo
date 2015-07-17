package org.activityinfo.legacy.shared.adapter.projection;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.legacy.shared.command.result.ListResult;
import org.activityinfo.legacy.shared.model.LocationDTO;

import java.util.List;

/**
 * The LocationDTO is actually already a projection, so we have a special case adapter which relies on
 * GetLocations for the heavy lifting.
 */
public class LocationProjector implements Function<ListResult<LocationDTO>, List<Projection>> {

    private final List<ProjectionUpdater<LocationDTO>> projectors;
    private Criteria criteria;

    public LocationProjector(Criteria criteria, List<FieldPath> fields) {
        this.criteria = criteria;
        projectors = createLocationUpdaters(fields);
    }

    public static List<ProjectionUpdater<LocationDTO>> createLocationUpdaters(List<FieldPath> fields) {
        List<ProjectionUpdater<LocationDTO>> projectors = Lists.newArrayList();
        for (FieldPath path : fields) {
            ResourceId fieldId = path.getLeafId();
            if (fieldId.getDomain() == CuidAdapter.ADMIN_LEVEL_DOMAIN) {
                int levelId = CuidAdapter.getBlock(fieldId, 0);
                int fieldIndex = CuidAdapter.getBlock(fieldId, 1);
                projectors.add(new AdminNameProjectionUpdater(path, levelId, fieldIndex));

            } else if (fieldId.getDomain() == CuidAdapter.LOCATION_TYPE_DOMAIN) {
                projectors.add(new LocationFieldProjectionUpdater(path, CuidAdapter.getBlock(fieldId, 1)));
            }
        }
        return projectors;
    }

    @Override
    public List<Projection> apply(ListResult<LocationDTO> input) {
        List<Projection> projections = Lists.newArrayList();
        for (LocationDTO location : input.getData()) {
            Projection projection = new Projection(CuidAdapter.locationInstanceId(location.getId()),
                    CuidAdapter.locationFormClass(location.getLocationTypeId()));
            for (ProjectionUpdater projector : projectors) {
                projector.update(projection, location);
            }
            if (criteria.apply(projection)) {
                projections.add(projection);
            }
        }
        return projections;
    }

}
