package org.activityinfo.model.type.time;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class FortnightTypeTest {

    @Test
    public void test() {
        assertThat(parseSubFormKey("s0417565614-2017W46-47"), equalTo(new FortnightValue(2017, 46)));
        assertThat(parseSubFormKey("s0417565614-2017W2-3"), equalTo(new FortnightValue(2017, 2)));

    }


    private FortnightValue parseSubFormKey(String subFormId) {
        return FortnightType.INSTANCE.fromSubFormKey(
                new RecordRef(ResourceId.valueOf("FORM"), ResourceId.valueOf(subFormId)));
    }
}