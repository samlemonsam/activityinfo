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
