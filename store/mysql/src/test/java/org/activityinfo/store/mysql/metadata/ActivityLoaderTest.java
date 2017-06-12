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