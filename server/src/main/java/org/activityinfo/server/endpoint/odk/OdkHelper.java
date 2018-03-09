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
package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.activityinfo.model.legacy.CuidAdapter.LOCATION_FIELD;

public class OdkHelper {

    public static String extractText(Node node) {
        NodeList childNodes = node.getChildNodes();

        if (childNodes.getLength() == 1) {
            Node child = childNodes.item(0);
            if (child.getChildNodes().getLength() == 0 && "#text".equals(child.getNodeName())) {
                return child.getNodeValue();
            }
        }

        return null;
    }

    public static boolean isLocation(FormClass formClass, FormField formField) {
        ResourceId locationFieldId = CuidAdapter.field(formClass.getId(), LOCATION_FIELD);
        return formField.getId().equals(locationFieldId);
    }

    public static ResourceId extractLocationReference(FormField formField) {
        if (formField.getType() instanceof ReferenceType) {
            ReferenceType referenceType = (ReferenceType) formField.getType();
            for (ResourceId locationFormId : referenceType.getRange()) {
                // Check for non Admin Level location reference
                Character domain = locationFormId.getDomain();
                if (!domain.equals(CuidAdapter.ADMIN_LEVEL_DOMAIN)) {
                    return locationFormId;
                }
            }
            return null;
        } else {
            return formField.getId();
        }
    }

}
