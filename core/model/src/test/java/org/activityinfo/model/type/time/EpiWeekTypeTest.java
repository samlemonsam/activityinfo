package org.activityinfo.model.type.time;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class EpiWeekTypeTest {

    @Test
    public void testParse() {
        assertThat(parseSubFormKey("c342342342017-2017W3"), equalTo(new EpiWeek(2017, 3)));
        assertThat(parseSubFormKey("c342342342017-2017W34"), equalTo(new EpiWeek(2017, 3)));

    }

    private EpiWeek parseSubFormKey(String subFormId) {
        return EpiWeekType.INSTANCE.fromSubFormKey(
                new RecordRef(ResourceId.valueOf("FORM"), ResourceId.valueOf(subFormId)));
    }
}