package org.activityinfo.geoadmin.merge2.state;

import org.activityinfo.geoadmin.merge2.MergeModelStore;
import org.activityinfo.geoadmin.merge2.view.model.FormMapping;
import org.activityinfo.geoadmin.merge2.view.model.SourceFieldMapping;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MergeModelStoreTest {

    @Test
    public void fieldMapping() throws IOException {

        ResourceStoreStub resourceStore = new ResourceStoreStub();
        MergeModelStore store = new MergeModelStore(resourceStore, 
                ResourceStoreStub.COMMUNE_SOURCE_ID, 
                ResourceStoreStub.COMMUNE_TARGET_ID);

        // Verify that the columns are properly matched

        FormMapping formMapping = store.getFieldMapping().get();
        assertThat(formMapping.getMappingForSource("COMMUNE").getTargetField().get().getLabel(), equalTo("Name"));
        assertThat(formMapping.getMappingForSource("DISTRICT").getTargetField().get().getLabel(), equalTo("District.Name"));
        assertThat(formMapping.getMappingForSource("REGION").getTargetField().get().getLabel(), equalTo("Region.Name"));
        assertThat(formMapping.getMappingForSource("REG_PCODE").getTargetField().get().getLabel(), equalTo("Region.Code"));


        // Verify that the rows get matched based on available data




    }
}