package org.activityinfo.server.command.handler.binding;

import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.BitSetColumnView;
import org.activityinfo.model.type.enumerated.EnumItem;

public class LinkedAttributeFieldBinding extends AttributeFieldBinding {

    private final int destinationAttribute;

    public LinkedAttributeFieldBinding(int destinationAttribute, FormField attrField) {
        super(attrField);
        this.destinationAttribute = destinationAttribute;
    }

    public LinkedAttributeFieldBinding(FormField attrField) {
        super(attrField);
        this.destinationAttribute = CuidAdapter.getLegacyIdFromCuid(attrField.getId());
    }

    @Override
    protected void setAttributeValues(SiteDTO[] dataArray, EnumItem item, BitSetColumnView attrColumn) {
        int attrId = CuidAdapter.getLegacyIdFromCuid(item.getId());

        for (int i=0; i<attrColumn.numRows(); i++) {
            boolean selected = attrColumn.getBoolean(i) == BitSetColumnView.TRUE;
            dataArray[i].setAttributeValue(attrId, selected);
            if (selected) {
                dataArray[i].addDisplayAttribute(CuidAdapter.attributeGroupField(destinationAttribute).asString(),
                        item.getId().asString());
            }
        }
    }
}
