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
package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.EmbeddedEntity;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FormConverterTest {


    @Test
    public void deserializationEnumTypeNoChoices() {
        EnumType type = new EnumType(Cardinality.SINGLE, EnumType.Presentation.AUTOMATIC, Collections.<EnumItem>emptyList());
        JsonValue jsonObject = type.getParametersAsJson();

        EmbeddedEntity entity = FormConverter.toEmbeddedEntity(jsonObject);
        JsonValue fromEntity = FormConverter.fromEmbeddedEntity(entity);

        EnumType reType = EnumType.TYPE_CLASS.deserializeType(fromEntity);
        assertThat(reType.getCardinality(), equalTo(Cardinality.SINGLE));
        assertThat(reType.getPresentation(), equalTo(EnumType.Presentation.AUTOMATIC));
        assertThat(reType.getValues(), hasSize(0));
    }

}