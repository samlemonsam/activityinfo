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
package org.activityinfo.store.mysql.metadata;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.number.QuantityType;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class ActivityLoaderTest {

    @Test
    public void serializationTest() throws IOException, ClassNotFoundException {

        // Large form class
        FormClass formClass = new FormClass(ResourceId.generateId());
        formClass.setLabel("Widget survey");

        for (int i = 0; i < 1000; i++) {
            formClass.addField(ResourceId.generateId())
                    .setLabel("How many widgets?")
                    .setType(new QuantityType("people"));
        }

        assertTrue(formClass.toJsonString().length() > 0xFFFF);

        Activity activity = new Activity();
        activity.serializedFormClass = new Activity.FormClassHolder();
        activity.serializedFormClass.value = formClass;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(activity);

        // Re-read
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Activity reread = (Activity) ois.readObject();

        assertThat(reread.getSerializedFormClass().toJsonString(), equalTo(formClass.toJsonString()));


    }

}