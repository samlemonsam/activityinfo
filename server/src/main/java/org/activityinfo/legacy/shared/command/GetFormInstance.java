package org.activityinfo.legacy.shared.command;

import com.google.common.collect.Lists;
import org.activityinfo.legacy.shared.command.result.FormInstanceListResult;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.List;

/**
 * Created by yuriy on 3/1/2015.
 */
public class GetFormInstance implements Command<FormInstanceListResult> {

    // todo : code it via type, need criteria but a lot of model classes are not serializable
    // have to ask Alex why
    public enum Type {
        ID, OWNER, CLASS
    }

    // todo : revisit with Criteria
    private Type type = Type.ID;
    private List<String> instanceIds = Lists.newArrayList();
    private String classId = null;
    private String ownerId = null;

    public GetFormInstance() {
    }

    public GetFormInstance(Collection<String> instanceIds) {
        this.instanceIds = Lists.newArrayList(instanceIds);
    }

    public List<String> getInstanceIds() {
        return instanceIds;
    }

    public GetFormInstance setInstanceIds(List<String> instanceIds) {
        this.instanceIds = instanceIds;
        return this;
    }

    public GetFormInstance setInstanceIdList(List<ResourceId> instanceIdList) {
        instanceIds = Lists.newArrayList();
        for (ResourceId id : instanceIdList) {
            instanceIds.add(id.asString());
        }
        return this;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public GetFormInstance setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public String getClassId() {
        return classId;
    }

    public GetFormInstance setClassId(String classId) {
        this.classId = classId;
        return this;
    }

    public Type getType() {
        return type;
    }

    public GetFormInstance setType(Type type) {
        this.type = type;
        return this;
    }
}
