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

/**
 * Converts to and from a {@code ReferenceValue} and an integer foreign key column in
 * an SQL table.
 */
public class ReferenceConverter implements FieldValueConverter {
    private char domain;

    /**
     * 
     * @param domain the prefix to use in converting the integer id to a {@code ResourceId}
     */
    public ReferenceConverter(char domain) {
        this.domain = domain;
    }

    @Override
    public FieldValue toFieldValue(ResultSet rs, int index) throws SQLException {
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
