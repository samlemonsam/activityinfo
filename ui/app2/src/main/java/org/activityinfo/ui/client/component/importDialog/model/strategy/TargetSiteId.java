package org.activityinfo.ui.client.component.importDialog.model.strategy;


public class TargetSiteId {
    private final String id;

    public TargetSiteId(String id) {
        assert id != null;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TargetSiteId that = (TargetSiteId) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String asString() {
        return id;
    }

    @Override
    public String toString() {
        return "<" + id + ">";
    }
}
