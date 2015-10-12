package org.activityinfo.store.mysql.metadata;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

public class Activity implements Serializable {

    public static final int REPORT_ONCE = 0;
    public static final int REPORT_MONTHLY = 1;

    private static final Logger LOGGER = Logger.getLogger(Activity.class.getName());

    private static final MemcacheService MEMCACHE = MemcacheServiceFactory.getMemcacheService();

    int activityId;
    int databaseId;
    int reportingFrequency;
    int locationTypeId;
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
}
