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
package org.activityinfo.store.hrd.entity;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;

import java.util.Date;

@Entity(name = "AnalysisSnapshot")
public class AnalysisSnapshotEntity {

    @Parent
    private Key<AnalysisEntity> analysis;

    @Id
    private long version;

    @Index
    private Date time;

    @Index
    private long userId;

    @Index
    private String type;

    @Index
    private String label;

    /**
     * The analysis model serialized as JSON
     */
    @Unindex
    private String model;

    public AnalysisSnapshotEntity() {
    }

    public Key<AnalysisEntity> getAnalysis() {
        return analysis;
    }

    public void setAnalysis(Key<AnalysisEntity> analysis) {
        this.analysis = analysis;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
