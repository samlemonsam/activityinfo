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
package org.activityinfo.server.database.hibernate.entity;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class IndicatorLinkEntityId implements Serializable {
    private static final long serialVersionUID = 6224197691532121470L;

    private Integer sourceIndicatorId;
    private Integer destinationIndicatorId;

    public IndicatorLinkEntityId() {
    }

    public IndicatorLinkEntityId(Integer sourceIndicatorId, Integer destinationIndicatorId) {
        this.sourceIndicatorId = sourceIndicatorId;
        this.destinationIndicatorId = destinationIndicatorId;
    }

    @Column(name = "SourceIndicatorId")
    public Integer getSourceIndicatorId() {
        return sourceIndicatorId;
    }

    public void setSourceIndicatorId(Integer sourceIndicatorId) {
        this.sourceIndicatorId = sourceIndicatorId;
    }

    @Column(name = "DestinationIndicatorId")
    public Integer getDestinationIndicatorId() {
        return destinationIndicatorId;
    }

    public void setDestinationIndicatorId(Integer destinationIndicatorId) {
        this.destinationIndicatorId = destinationIndicatorId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sourceIndicatorId, destinationIndicatorId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof IndicatorLinkEntityId)) {
            return false;
        }

        IndicatorLinkEntityId i = (IndicatorLinkEntityId) obj;
        return Objects.equal(i.getSourceIndicatorId(), this.getSourceIndicatorId()) &&
               Objects.equal(i.getDestinationIndicatorId(), this.getDestinationIndicatorId());
    }
}