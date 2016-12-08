package org.activityinfo.model.query;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.resource.ResourceId;

public class RowSource {

    private ResourceId rootFormId;

    public RowSource() {
    }

    public RowSource(ResourceId rootFormId) {
        this.rootFormId = rootFormId;
    }

    public ResourceId getRootFormId() {
        return rootFormId;
    }

    public RowSource setRootFormId(ResourceId rootFormId) {
        this.rootFormId = rootFormId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RowSource rowSource = (RowSource) o;

        if (rootFormId != null ? !rootFormId.equals(rowSource.rootFormId) : rowSource.rootFormId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return rootFormId != null ? rootFormId.hashCode() : 0;
    }


    public JsonElement toJsonElement() {
        JsonObject object = new JsonObject();
        object.addProperty("rootFormId", rootFormId.asString());

        return object;
    }


    public static RowSource fromJson(JsonObject object) {
        RowSource source = new RowSource();
        source.setRootFormId(ResourceId.valueOf(object.get("rootFormId").getAsString()));
        return source;
    }

}
