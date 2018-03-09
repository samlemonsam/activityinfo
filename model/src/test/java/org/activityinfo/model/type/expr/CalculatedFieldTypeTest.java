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
package org.activityinfo.model.type.expr;

import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class CalculatedFieldTypeTest {
    
    @BeforeClass
    public static void setupI18N() {
        LocaleProxy.initialize();
    }

    @Test
    public void serialization() {

        FormField field = new FormField(ResourceId.generateId());
        field.setType(new CalculatedFieldType("A+B"));

        JsonValue record = field.toJsonObject();
        System.out.println(record.toJson());

        FormField read = FormField.fromJson(record);
        assertThat(read.getType(), instanceOf(CalculatedFieldType.class));

        CalculatedFieldType readType = (CalculatedFieldType) read.getType();
        assertThat(readType.getExpression(), equalTo("A+B"));
    }


    @Test
    public void emptySerialization() {

        FormField field = new FormField(ResourceId.generateId());
        field.setType(new CalculatedFieldType());

        JsonValue record = field.toJsonObject();
        System.out.println(record.toJson());

        FormField read = FormField.fromJson(record);
        assertThat(read.getType(), instanceOf(CalculatedFieldType.class));

        CalculatedFieldType readType = (CalculatedFieldType) read.getType();
        assertThat(readType.getExpression(), nullValue());
    }
}