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

import org.activityinfo.model.form.FormField;
import org.activityinfo.io.xform.xpath.XPathBuilder;

public class OdkField {

    private FormField model;
    private OdkFormFieldBuilder builder;

    public OdkField(FormField model, OdkFormFieldBuilder builder) {
        this.model = model;
        this.builder = builder;
    }

    public FormField getModel() {
        return model;
    }

    public OdkFormFieldBuilder getBuilder() {
        return builder;
    }

    public String getAbsoluteFieldName() {
        return "/data/" + getRelativeFieldName();
    }

    public String getRelativeFieldName() {
        return XPathBuilder.fieldTagName(model.getId());
    }
}
