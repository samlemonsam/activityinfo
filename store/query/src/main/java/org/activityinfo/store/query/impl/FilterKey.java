package org.activityinfo.store.query.impl;

import org.activityinfo.model.resource.ResourceId;

class FilterKey {
    private ResourceId formId;
    private FilterLevel filterLevel;

    public FilterKey(ResourceId formId, FilterLevel filterLevel) {
        this.formId = formId;
        this.filterLevel = filterLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterKey filterKey = (FilterKey) o;

        if (!formId.equals(filterKey.formId)) return false;
        return filterLevel == filterKey.filterLevel;

    }

    @Override
    public int hashCode() {
        int result = formId.hashCode();
        result = 31 * result + filterLevel.hashCode();
        return result;
    }
}
