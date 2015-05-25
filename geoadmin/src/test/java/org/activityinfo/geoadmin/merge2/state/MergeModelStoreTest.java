package org.activityinfo.geoadmin.merge2.state;

import org.activityinfo.geoadmin.merge2.MergeModelStore;
import org.activityinfo.geoadmin.merge2.view.model.FormMapping;
import org.activityinfo.geoadmin.merge2.view.model.SourceFieldMapping;
import org.junit.Test;

import java.io.IOException;

public class MergeModelStoreTest {

    @Test
    public void fieldMapping() throws IOException {

        ResourceStoreStub resourceStore = new ResourceStoreStub();
        MergeModelStore store = new MergeModelStore(resourceStore, 
                ResourceStoreStub.COMMUNE_SOURCE_ID, 
                ResourceStoreStub.COMMUNE_TARGET_ID);

        FormMapping formMapping = store.getFieldMapping().get();
        for (SourceFieldMapping mapping : formMapping.getMappings()) {
            System.out.println(mapping);
        }
    }
}