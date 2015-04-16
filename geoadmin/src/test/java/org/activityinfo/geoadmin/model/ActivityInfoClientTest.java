package org.activityinfo.geoadmin.model;

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreePrettyPrinter;
import org.junit.Ignore;
import org.junit.Test;

public class ActivityInfoClientTest {
    
    @Test
    @Ignore
    public void test() {
        
        ActivityInfoClient client = new ActivityInfoClient("http://localhost:8898/resources", "test@test.org", "testing123");

        FormTree formTree = client.getFormTree(1);
        FormTreePrettyPrinter.print(formTree);
    }

}