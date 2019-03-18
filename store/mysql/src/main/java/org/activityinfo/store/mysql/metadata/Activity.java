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
package org.activityinfo.store.mysql.metadata;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import org.activityinfo.json.JsonParser;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.io.*;
import java.util.*;

public class Activity implements Serializable {

    public static final int REPORT_ONCE = 0;
    public static final int REPORT_MONTHLY = 1;

    int activityId;
    int databaseId;
    String databaseName;
    int reportingFrequency;
    int folderId;

    int sortOrder;

    /**
     * The current locationTypeId of the activity.
     */
    int locationTypeId;


    /**
     * The id of the adminLevel to which this activity's current location type is bound, or {@code null} if
     * this activity's current location type is not bound.
     */
    Integer adminLevelId;

    /**
     * Because it currently possible to change location type, it's possible that a single
     * activity references *multiple* location types
     */
    List<ResourceId> locationRange = new ArrayList<>();

    String category;
    String locationTypeName;

    String name;
    int ownerUserId;
    boolean published;
    long schemaVersion;
    long siteVersion;
    
    boolean deleted;

    boolean classicView;

    FormClassHolder serializedFormClass = new FormClassHolder();


    List<ActivityField> fields = Lists.newArrayList();
    
    
    Map<ResourceId, Integer> fieldsOrder = Maps.newHashMap();

    /**
     * Map from destination indicator to source indicators, within the same activity
     */
    Multimap<Integer, Integer> selfLinkedIndicators = HashMultimap.create();

    /**
     * Map from destination indicator to source indicators, from an external activity form
     */
    Map<Integer, LinkedActivity> linkedActivities = Maps.newHashMap();


    public boolean hrd;

    /**
     * True if this activity has the "nullary" location type.
     */
    public boolean nullLocationType;

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

    public boolean isClassicView() {
        return classicView;
    }

    public String getLocationTypeName() {
        return locationTypeName;
    }

    public Integer getAdminLevelId() {
        return adminLevelId;
    }

    public List<ActivityField> getFields() {
        return fields;
    }

    public Map<ResourceId, Integer> getFieldsOrder() {
        return fieldsOrder;
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
        return Math.max(siteVersion, schemaVersion);
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
        return !isNullLocationType();
    }

    public int getNullaryLocationId() {
        Preconditions.checkState(isNullLocationType(), "Only valid if this is a nullarly location type");
        // This is nasty hack to allow for activities without location types.
        // Each country has one "nullary" location type called "Country"
        // Each of these location types has exactly one location instance, with the same id.
        return locationTypeId;
    }


    private boolean isNullLocationType() {
        return nullLocationType;
    }

    public ResourceId getProjectFormClassId() {
        return CuidAdapter.projectFormClass(databaseId);
    }
    
    public ResourceId getPartnerFormClassId() {
        return CuidAdapter.partnerFormId(databaseId);
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
        // If the activity is *currently* mapped to a bound admin level, then
        // we maps this to a reference field only to the admin level. References to
        // old, non-bound locations can be mapped to the correct admin entity transparently.
        if(adminLevelId != null) {
            return Collections.singleton(CuidAdapter.adminLevelFormClass(adminLevelId));

        } else {
            // Otherwise, we need to explicitly model the location field as a reference
            // to one more location forms and/or admin levels
            return locationRange;
        }
    }

    public int getSortOrder() {
        return sortOrder;
    }


    public boolean hasCategory() {
        return !Strings.isNullOrEmpty(category);
    }

    public ResourceId getSiteFormClassId() {
        return CuidAdapter.activityFormClass(activityId);
    }

    public ResourceId getLeafFormClassId() {
        if(reportingFrequency == 0) {
            return getSiteFormClassId();
        } else {
            return CuidAdapter.reportingPeriodFormClass(activityId);
        }
    }

    public Collection<LinkedActivity> getLinkedActivities() {
        return linkedActivities.values();
    }

    public boolean isMonthly() {
        return reportingFrequency == 1;
    }

    public String getDatabaseName() {
        return databaseName;
    }


    public ActivityField getAttributeGroupField(Integer groupId) {
        for (ActivityField field : fields) {
            if(field.isAttributeGroup() && field.getId() == groupId) {
                return field;
            }
        }

        throw new IllegalArgumentException("No such attribute group " + groupId + " in activity " + activityId);
    }


    public ActivityVersion getActivityVersion() {
        return new ActivityVersion(this.getId(), schemaVersion, siteVersion, hrd);
    }
    
    void addLink(int destinationIndicatorId, int sourceActivityId, int sourceReportingFrequency, int sourceIndicatorId) {
        if(sourceActivityId == this.activityId) {
            selfLinkedIndicators.put(destinationIndicatorId, sourceIndicatorId);
        } else {
            LinkedActivity linkedActivity = linkedActivities.get(sourceActivityId);
            if(linkedActivity == null) {
                linkedActivity = new LinkedActivity();
                linkedActivity.activityId = sourceActivityId;
                linkedActivity.reportingFrequency = sourceReportingFrequency;
                linkedActivities.put(sourceActivityId, linkedActivity);
            }
            linkedActivity.linkMap.put(destinationIndicatorId, sourceIndicatorId);

        }
    }
    
    public boolean isDeleted() {
        return deleted;
    }

    public boolean isMigratedToHrd() {
        return hrd;
    }
    
    public LinkedActivity getSelfLink() {
        LinkedActivity linked = new LinkedActivity();
        linked.activityId = this.activityId;
        linked.reportingFrequency = this.reportingFrequency;
        for (ActivityField indicatorField : getIndicatorFields()) {
            linked.linkMap.put(indicatorField.getId(), indicatorField.getId());
        }
        linked.linkMap.putAll(selfLinkedIndicators);
        return linked;
    }

    public Multimap<Integer, Integer> getSelfLinkedIndicators() {
        return selfLinkedIndicators;
    }

    public FormClass getSerializedFormClass() {
        return serializedFormClass.value;
    }

    public int getFolderId() {
        assert inFolder();
        return folderId;
    }

    public boolean inFolder() {
        return folderId != 0;
    }

    /**
     * Helper class to allow the FormClass value to be serialized with JSON instead of Java Serialization.
     */
    static class FormClassHolder implements Serializable {

        FormClass value;

        private void writeObject(ObjectOutputStream out) throws IOException {
            if(value == null) {
                out.writeBoolean(false);
            } else {
                // Avoid writeUTF as it has a limit of 65k
                out.writeBoolean(true);
                byte[] bytes = value.toJsonString().getBytes(Charsets.UTF_8);
                out.writeInt(bytes.length);
                out.write(bytes);
            }
        }
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            if(in.readBoolean()) {
                int length = in.readInt();
                byte[] bytes = new byte[length];
                in.readFully(bytes);
                JsonParser parser = new JsonParser();
                try(InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(bytes), Charsets.UTF_8)) {
                    this.value = FormClass.fromJson(parser.parse(reader));
                }
            } else {
                this.value = null;
            }
        }
    }
}
