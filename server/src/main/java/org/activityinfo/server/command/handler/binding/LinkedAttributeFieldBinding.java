/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
