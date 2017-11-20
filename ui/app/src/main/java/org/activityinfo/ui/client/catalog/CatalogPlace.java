package org.activityinfo.ui.client.catalog;


import com.google.common.base.Optional;
import com.google.gwt.place.shared.Place;

public class CatalogPlace extends Place {
    private Optional<String> parentId;

    public CatalogPlace(String parentId) {
        this.parentId = Optional.of(parentId);
    }

    public CatalogPlace(Optional<String> parentId) {
        this.parentId = parentId;
    }

    public Optional<String> getParentId() {
        return parentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CatalogPlace that = (CatalogPlace) o;

        return parentId.equals(that.parentId);
    }

    @Override
    public int hashCode() {
        return parentId.hashCode();
    }
}
