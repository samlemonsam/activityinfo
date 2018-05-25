package org.activityinfo.model.type.enumerated;

import org.activityinfo.model.resource.ResourceId;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class EnumValueTest {

    @Test
    public void equality() {
        //  [t0679737062, t2075376824] != [t2075376824, t0679737062]

        EnumValue a = new EnumValue(ResourceId.valueOf("t0679737062"), ResourceId.valueOf("t2075376824"));
        EnumValue b = new EnumValue(ResourceId.valueOf("t2075376824"), ResourceId.valueOf("t0679737062"));

        assertTrue(a.equals(b));
    }

}