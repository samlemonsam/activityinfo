package org.activityinfo.core.client;

import com.google.common.collect.Lists;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.core.shared.criteria.NullCriteria;
import org.activityinfo.model.formTree.FieldPath;

import java.util.List;

/**
 * Describes a query for {@code FormInstances}
 */
public class InstanceQuery {

    private final static int FALLBACK_MAX_COUNT = 100;

    private List<FieldPath> fieldPaths;
    private Criteria criteria;
    private int offset;
    private int maxCount;

    public InstanceQuery() {
    }

    public InstanceQuery(List<FieldPath> fieldPaths, Criteria criteria) {
        this(fieldPaths, criteria, 0, FALLBACK_MAX_COUNT);
    }

    public InstanceQuery(List<FieldPath> fieldPaths, Criteria criteria, int offset, int maxCount) {
        assert criteria != null;
        this.criteria = criteria;
        this.fieldPaths = fieldPaths;
        this.offset = offset;
        this.maxCount = maxCount;
    }

    public List<FieldPath> getFieldPaths() {
        return fieldPaths;
    }

    public int getOffset() {
        return offset;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public InstanceQuery setFieldPaths(List<FieldPath> fieldPaths) {
        this.fieldPaths = fieldPaths;
        return this;
    }

    public InstanceQuery setCriteria(Criteria criteria) {
        this.criteria = criteria;
        return this;
    }

    public InstanceQuery setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public InstanceQuery incrementOffsetOn(int incrementedOn) {
        offset = offset + incrementedOn;
        if (offset < 0) {
            offset = 0;
        }
        return this;
    }

    public InstanceQuery setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    public static Builder select(ResourceId... fieldIds) {
        return new Builder().select(fieldIds);
    }

    public static class Builder {
        private List<FieldPath> paths = Lists.newArrayList();
        private Criteria criteria;
        private int offset = 0;
        private int maxCount = FALLBACK_MAX_COUNT;

        private Builder() {
        }

        public Builder where(Criteria criteria) {
            assert this.criteria == null : "Criteria already specified";
            this.criteria = criteria;
            return this;
        }

        public Builder select(ResourceId... fields) {
            for (ResourceId fieldId : fields) {
                paths.add(new FieldPath(fieldId));
            }
            return this;
        }

        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }

        public Builder maxCount(int maxCount) {
            this.maxCount = maxCount;
            return this;
        }

        public InstanceQuery build() {
            if (criteria == null) {
                criteria = NullCriteria.INSTANCE;
            }
            return new InstanceQuery(paths, criteria, offset, maxCount);
        }
    }
}
