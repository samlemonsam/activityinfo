package org.activityinfo.store.mysql.mapping;

import com.google.common.base.Preconditions;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

public class ForeignKeyMapping implements FieldValueMapping {
    private char domain;

    public ForeignKeyMapping(char domain) {
        this.domain = domain;
    }

    @Override
    public FieldValue extract(ResultSet rs, int index) throws SQLException {
        int id = rs.getInt(index);
        if (rs.wasNull()) {
            return null;
        } else {
            return new ReferenceValue(CuidAdapter.cuid(domain, id));
        }
    }

    @Override
    public Collection<Integer> toParameters(FieldValue value) {
        ReferenceValue referenceValue = (ReferenceValue) value;
        ResourceId resourceId = referenceValue.getResourceId();
        Preconditions.checkArgument(resourceId.getDomain() == domain);
        return Collections.singleton(CuidAdapter.getLegacyIdFromCuid(resourceId));
    }
}
