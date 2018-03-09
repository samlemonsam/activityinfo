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
package org.activityinfo.model.type.enumerated;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.Cardinality;
import org.junit.Test;

import java.util.Collections;

import static org.activityinfo.json.Json.createObject;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class EnumTypeTest {

    @Test
    public void deserializationCheckboxes() {

        JsonValue object = createObject();
        object.put("presentation", "CHECKBOX");
        object.put("cardinality", "SINGLE");
        object.put("values", Json.createArray());

        EnumType enumType = EnumType.TYPE_CLASS.deserializeType(object);
        assertThat(enumType.getPresentation(), equalTo(EnumType.Presentation.RADIO_BUTTON));
    }


    @Test
    public void deserializationEnumTypeNoChoices() {
        EnumType type = new EnumType(Cardinality.SINGLE, EnumType.Presentation.AUTOMATIC, Collections.<EnumItem>emptyList());
        JsonValue jsonObject = type.getParametersAsJson();

        EnumType reType = EnumType.TYPE_CLASS.deserializeType(jsonObject);
        assertThat(reType.getCardinality(), equalTo(Cardinality.SINGLE));
        assertThat(reType.getPresentation(), equalTo(EnumType.Presentation.AUTOMATIC));
        assertThat(reType.getValues(), hasSize(0));
    }
}