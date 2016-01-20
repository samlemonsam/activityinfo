package org.activityinfo.store.mysql.metadata;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.collections.BETA;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Activity implements Serializable {

    public static final int REPORT_ONCE = 0;
    public static final int REPORT_MONTHLY = 1;

    int activityId;
    int databaseId;
    int reportingFrequency;
    int locationTypeId;

    /**
     * Because it currently possible to change location type, it's possible that a single
     * activity references *multiple* location types
     */
    Set<Integer> locationTypeIds = Sets.newHashSet();
    String category;
    String locationTypeName;
    int adminLevelId;
    String name;
    int ownerUserId;
    boolean published;
    long version;
    
    List<ActivityField> fields = Lists.newArrayList();
    
    public int getId() {
        return activityId;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public int getReportingFrequency() {
        return reportingFrequency;
    }

    public int getLocationTypeId() {
        return locationTypeId;
    }

    public String getCategory() {
        return category;
    }

    public String getLocationTypeName() {
        return locationTypeName;
    }

    public int getAdminLevelId() {
        return adminLevelId;
    }

    public List<ActivityField> getFields() {
        return fields;
    }

    public String getName() {
        return name;
    }

    public Iterable<ActivityField> getAttributeAndIndicatorFields() {
        if(reportingFrequency == REPORT_ONCE) {
            return fields;
        } else {
            return Iterables.filter(fields, new Predicate<ActivityField>() {
                @Override
                public boolean apply(ActivityField input) {
                    return input.isAttributeGroup();
                }
            });
        }
    }

    public long getVersion() {
        return version;
    }

    public Iterable<ActivityField> getIndicatorFields() {
        return Iterables.filter(fields, new Predicate<ActivityField>() {
            @Override
            public boolean apply(ActivityField input) {
                return !input.isAttributeGroup();
            }
        });
    }
    
    public boolean hasLocationType() {
        // hack!!
        return !isNullLocationType();
    }

    public int getNullaryLocationId() {
        // This is nasty hack to allow for activities without location types.
        // Each country has one "nullary" location type called "Country"
        // Each of these location types has exactly one location instance, with the same id.
        return locationTypeId;
    }


    private boolean isNullLocationType() {
        return "Country".equals(locationTypeName) && locationTypeId != 20301;
    }

    public ResourceId getProjectFormClassId() {
        return CuidAdapter.projectFormClass(databaseId);
    }
    
    public ResourceId getPartnerFormClassId() {
        return CuidAdapter.partnerFormClass(databaseId);
    }

    public ResourceId getLocationFormClassId() {
        return CuidAdapter.locationFormClass(locationTypeId);
    }


    public int getOwnerUserId() {
        return ownerUserId;
    }

    public boolean isPublished() {
        return published;
    }

    public Collection<ResourceId> getLocationFormClassIds() {
        if(BETA.ENABLE_LOCATION_UNION_FIELDS) {
            Set<ResourceId> locationFormClassIds = Sets.newHashSet();
            for (Integer typeId : locationTypeIds) {
                locationFormClassIds.add(CuidAdapter.locationFormClass(typeId));
            }
            return locationFormClassIds;
        } else {
            return Collections.singleton(CuidAdapter.locationFormClass(locationTypeId));
        }
    }
}
