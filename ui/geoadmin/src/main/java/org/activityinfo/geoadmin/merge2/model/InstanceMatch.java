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
package org.activityinfo.geoadmin.merge2.model;

import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;

/**
 * Explicit matching between a Resource in the source collection to an existing resource in the target collection.
 * 
 * <p>For example, if we are updating an <em>existing</em> (target) collection of provinces with a new (source) shapefile
 * containing corrections, than each {@code InstanceMatch} in the model would explicitly match the province in
 * the shapefile to the correct province in the existing collection of provinces.</p>
 * 
 * <p>{@code InstanceMatch}es complement or override the matching done automatically; an {@code InstanceMatch} would
 * be necessary to match a province in the source and target sets which have no obvious connection, for example,
 * the Egyptian governorate of al-Wādī l-Ǧadīd is sometimes referred to by its literal English translation, "New Valley".
 * </p>
 * 
 * <p>In this case, updating an existing (target) province called "New Valley" with a new (source) province
 * called "al Wadi Al Gadid" would require the user to add an explicit {@code InstanceMatch} through the UI.</p>
 */
public class InstanceMatch {
    
    private Optional<ResourceId> sourceId;
    private Optional<ResourceId> targetId;

    public InstanceMatch(ResourceId sourceId, ResourceId targetId) {
        this.sourceId = Optional.of(sourceId);
        this.targetId = Optional.of(targetId);
    }

    public InstanceMatch(Optional<ResourceId> sourceId, Optional<ResourceId> targetId) {
        this.sourceId = sourceId;
        this.targetId = targetId;
    }

    public Optional<ResourceId> getSourceId() {
        return sourceId;
    }

    public Optional<ResourceId> getTargetId() {
        return targetId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstanceMatch that = (InstanceMatch) o;

        if (!sourceId.equals(that.sourceId)) return false;
        if (!targetId.equals(that.targetId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sourceId.hashCode();
        result = 31 * result + targetId.hashCode();
        return result;
    }
}
