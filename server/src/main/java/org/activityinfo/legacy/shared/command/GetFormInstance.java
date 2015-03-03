package org.activityinfo.legacy.shared.command;

import com.google.common.collect.Lists;
import org.activityinfo.legacy.shared.command.result.FormInstanceResult;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.List;

/**
 * Created by yuriy on 3/1/2015.
 */
public class GetFormInstance implements Command<FormInstanceResult> {

    // todo : code it via type, need criteria but a lot of model classes are not serializable
    // have to ask Alex why
    public static enum Type {
        ID, OWNER
    }

    // todo : revisit with Criteria
    private Type type = Type.ID;
    private List<String> instanceIds = Lists.newArrayList();
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

    public Type getType() {
        return type;
    }

    public GetFormInstance setType(Type type) {
        this.type = type;
        return this;
    }
}
