package org.activityinfo.legacy.shared.command;

import com.google.common.collect.Lists;
import org.activityinfo.legacy.shared.command.result.FormInstanceListResult;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.List;

/**
 * TODO Revise this class! Implementation must be based on Criteria and Cloud Storage
 *
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
    private Collection<String> ownerIds = Lists.newArrayList();

    public GetFormInstance() {
    }

    public GetFormInstance(Collection<String> instanceIds) {
        this.instanceIds = Lists.newArrayList(instanceIds);
    }


    public GetFormInstance(ResourceId instanceId) {
        this(Lists.newArrayList(instanceId.asString()));
    }
    
    public List<String> getInstanceIds() {
        return instanceIds;
    }

    public GetFormInstance setInstanceIds(List<String> instanceIds) {
        this.instanceIds = instanceIds;
        return this;
    }

    public GetFormInstance setInstanceIdList(List<ResourceId> instanceIdList) {
        instanceIds = asStrings(instanceIdList);
        return this;
    }

    public static List<String> asStrings(Collection<ResourceId> instanceIdList) {
        List<String> result = Lists.newArrayList();
        for (ResourceId id : instanceIdList) {
            result.add(id.asString());
        }
        return result;
    }

    public Collection<String> getOwnerIds() {
        return ownerIds;
    }

    public GetFormInstance setOwnerIds(Collection<String> ownerIds) {
        this.ownerIds = ownerIds;
        return this;
    }

    public GetFormInstance setOwnerIdList(Collection<ResourceId> ownerIds) {
        this.ownerIds = asStrings(ownerIds);
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
