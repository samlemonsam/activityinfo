package org.activityinfo.test.pageobject.odk;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class FormListTest {

    @Test
    public void parse() throws IOException {
        PageSource page = new PageSource(Resources.toString(Resources.getResource("formList.xml"), Charsets.UTF_8));

        List<BlankForm> forms = FormList.parseFormList(page);

        assertThat(forms,
                contains(
                    hasProperty("name", equalTo("Birds")),
                    hasProperty("name", equalTo("Cascading Select Form")),
                    hasProperty("name", equalTo("Cascading Triple Select Form")),
                    hasProperty("name", equalTo("Forest Plot Survey")),
                    hasProperty("name", equalTo("Geo Tagger v2")),
                    hasProperty("name", equalTo("Hypertension Screening"))));
    }
}
