package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SingleEnumBlockTest {
    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));

    @Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }


    @Test
    public void testDeleted() {

        EnumItem female = new EnumItem(ResourceId.valueOf("G1"), "Female");
        EnumItem male = new EnumItem(ResourceId.valueOf("G2"), "Male");
        FormField stringField = new FormField(ResourceId.valueOf("F")).setType(
                new EnumType(Cardinality.SINGLE, female, male));

        SingleEnumBlock block = (SingleEnumBlock) BlockFactory.get(stringField);

        Entity blockEntity = new Entity("Block", 1);

        block.update(blockEntity, 0, new EnumValue(female));
        block.update(blockEntity, 1, new EnumValue(male));

        FormEntity header = new FormEntity();
        header.setNumberedRecordCount(2);
        header.setDeletedCount(1);

        Entity tombstone = new Entity("Tombstone", 1);
        TombstoneBlock.markDeleted(tombstone, 1);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header, Arrays.asList(tombstone).iterator());

        ColumnView view = block.buildView(header, tombstoneIndex, Arrays.asList(blockEntity).iterator());
        assertThat(view.numRows(), equalTo(1));
        assertThat(view.getString(0), equalTo("Female"));
    }

}