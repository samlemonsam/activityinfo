package org.activityinfo.store.mysql.mapping;

import com.google.common.base.Preconditions;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

public class AttributeConverter implements FieldValueConverter {


    @Override
    public FieldValue toFieldValue(ResultSet rs, int index) throws SQLException {
        int id = rs.getInt(index);
        if (rs.wasNull()) {
            return null;
        } else {
            return new EnumValue(CuidAdapter.attributeId(id));
        }
    }

    @Override
    public Collection<?> toParameters(FieldValue value) {
        EnumValue enumValue = (EnumValue) value;
        ResourceId enumItemId = enumValue.getValueId();
        Preconditions.checkArgument(enumItemId.getDomain() == CuidAdapter.ATTRIBUTE_DOMAIN);

        return Collections.singleton(CuidAdapter.getLegacyIdFromCuid(enumItemId));
    }
}
