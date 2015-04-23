package org.activityinfo.io.xform.form;

import javax.xml.bind.annotation.XmlElement;

public class Meta {
    private String instanceId;
    private String userId;

    @XmlElement(name = "instanceID", required = true)
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @XmlElement(name = "userID")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
