package org.activityinfo.model.database;

import org.activityinfo.model.resource.ResourceId;
import org.junit.Test;

public class AutoJsonTest {

    @Test
    public void test() {

        GrantModelBuilder grant = new GrantModelBuilder();
        grant.setResourceId(ResourceId.valueOf("foo"));
        grant.addOperation(Operation.EDIT_RECORD);


        System.out.println(GrantModelJson.toJson(grant.build()).toJson());
    }
}
