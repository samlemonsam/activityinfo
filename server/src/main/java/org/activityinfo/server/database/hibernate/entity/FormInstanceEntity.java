package org.activityinfo.server.database.hibernate.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by yuriy on 3/1/2015.
 */
@Entity
@Table(name = "forminstance")
public class FormInstanceEntity implements Serializable, HasJson {

    private String id;
    private String classId;
    private String ownerId;
    private String json;
    private byte[] gzJson;

    public FormInstanceEntity() {
    }

    public FormInstanceEntity(String id) {
        this.id = id;
    }

    @Id
    @Column(name = "formInstanceId", unique = true, nullable = false)
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "formInstanceClassId", nullable = false)
    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    @Column(name = "formInstanceOwnerId", nullable = true)
    public String getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Lob
    @Column(name = "json")
    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @Column(name = "gzJson")
    public byte[] getGzJson() {
        return gzJson;
    }

    public void setGzJson(byte[] gzJson) {
        this.gzJson = gzJson;
    }

}
