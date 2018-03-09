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
package org.activityinfo.model.type.quantity;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.number.QuantityType;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class QuantityTypeTest {

    /**
     * Ensure QuantityType is parsed correctly from JSON.
     * In particular, ensure that the "aggregation" field is correctly initialised from a legacy schema with no
     * aggregation field within Quantity JSON element.
     *
     * @throws IOException
     */
    @Test
    public void deserialization() throws IOException {
        FormClass formClass = parseResource();

        ResourceId quantityFieldId = ResourceId.valueOf("i0000006090");
        FormField quantityField = formClass.getField(quantityFieldId);

        // Quantity Checks
        assertThat(quantityField.getLabel(),equalTo("Number of water points constructed"));
        assertThat(quantityField.isRequired(),is(false));
        assertThat(quantityField.isVisible(),is(true));

        // QuantityType Checks
        assertThat(quantityField.getType(),instanceOf(QuantityType.class));
        QuantityType quantityType = (QuantityType) quantityField.getType();
        assertThat(quantityType.getUnits(),equalTo("Waterpoints"));
        assertThat(quantityType.getAggregation(),is(notNullValue()));
        assertThat(quantityType.getAggregation(),equalTo(QuantityType.Aggregation.SUM));
        assertThat(quantityType.getAggregation().ordinal(),equalTo(0));
    }


    private FormClass parseResource() throws IOException {
        URL resource = Resources.getResource(FormClass.class, "OldFormClass2.json");
        String json = Resources.toString(resource, Charsets.UTF_8);
        JsonValue element = Json.parse(json);

        return FormClass.fromJson(element);
    }

}
