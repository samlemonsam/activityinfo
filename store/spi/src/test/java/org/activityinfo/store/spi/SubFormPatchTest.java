package org.activityinfo.store.spi;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertFalse;

public class SubFormPatchTest {

    @Test
    public void removeExtraClonedField() throws IOException {
        URL resource = Resources.getResource(SubFormPatch.class, "subform.json");
        String json = Resources.toString(resource, Charsets.UTF_8);
        FormClass schema = FormClass.fromJson(json);

        FormClass patched = SubFormPatch.patch(schema);

        assertFalse(patched.getFieldIfPresent(ResourceId.valueOf("p1403648307")).isPresent());
    }
}
