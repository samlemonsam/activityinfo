package org.activityinfo.model.query;

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
}
