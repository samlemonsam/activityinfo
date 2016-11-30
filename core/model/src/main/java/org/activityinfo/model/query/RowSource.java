package org.activityinfo.model.query;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.resource.ResourceId;

public class RowSource {

    private ResourceId rootFormClass;

    public RowSource() {
    }

    public RowSource(ResourceId rootFormClass) {
        this.rootFormClass = rootFormClass;
    }

    public ResourceId getRootFormClass() {
        return rootFormClass;
    }

    public RowSource setRootFormClass(ResourceId rootFormClass) {
        this.rootFormClass = rootFormClass;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RowSource rowSource = (RowSource) o;

        if (rootFormClass != null ? !rootFormClass.equals(rowSource.rootFormClass) : rowSource.rootFormClass != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return rootFormClass != null ? rootFormClass.hashCode() : 0;
    }


    public JsonElement toJsonElement() {
        JsonObject object = new JsonObject();
        object.addProperty("rootFormId", rootFormClass.asString());

        return object;
    }


    public static RowSource fromJson(JsonObject object) {
        RowSource source = new RowSource();
        source.setRootFormClass(ResourceId.valueOf(object.get("rootFormId").getAsString()));
        return source;
    }

}
