package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;

import java.util.Arrays;
import java.util.Collections;

import static java.util.Collections.emptyIterator;
import static org.activityinfo.store.testing.ColumnSetMatchers.hasValues;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class NumberBlockTest {

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
    public void integers() {
        FormField quantityField = new FormField(ResourceId.valueOf("F")).setType(new QuantityType("households"));
        NumberBlock block = (NumberBlock) BlockFactory.get(quantityField);

        Entity blockEntity = new Entity("Block", 1);

        block.update(blockEntity, 0, new Quantity(1000));
        block.update(blockEntity, 1, new Quantity(1001));
        block.update(blockEntity, 2, new Quantity(1002));
        block.update(blockEntity, 3, new Quantity(1003));
        block.update(blockEntity, 4, new Quantity(1004));
        block.update(blockEntity, 5, new Quantity(1005));

        FormEntity header = new FormEntity();
        header.setNumberedRecordCount(10);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header, emptyIterator());

        ColumnView view = block.buildView(header, tombstoneIndex, Arrays.asList(blockEntity).iterator());
        for (int i = 0; i < 6; i++) {
            assertThat(view.getDouble(i), equalTo(i + 1000d));
        }

        // Rebuild the view, but now with 2 records deleted

        header.setDeletedCount(2);
        header.setNumberedRecordCount(6);

        Entity tombstone = new Entity("Tombstone", 1);
        TombstoneBlock.markDeleted(tombstone, 2);
        TombstoneBlock.markDeleted(tombstone, 4);

        tombstoneIndex = new TombstoneIndex(header, Collections.singleton(tombstone).iterator());

        view = block.buildView(header, tombstoneIndex, Arrays.asList(blockEntity).iterator());

        assertThat(view, hasValues(1000, 1001, 1003, 1005));

    }


    @Test
    public void doubles() {
        FormField quantityField = new FormField(ResourceId.valueOf("F")).setType(new QuantityType("households"));
        NumberBlock block = (NumberBlock) BlockFactory.get(quantityField);

        if(block.getBlockSize() != (1024*10)) {
            throw new AssumptionViolatedException("Unit test expects block size of 10k, may require updating");
        }

        Entity block1 = new Entity("Block", 1);

        block.update(block1, 0, new Quantity(42));
        block.update(block1, 300, new Quantity(98));
        block.update(block1, 301, new Quantity(144));
        block.update(block1, 302, new Quantity(91));


        Entity block2 = new Entity("Block", 4);
        block.update(block2, 10, new Quantity(1.5));
        block.update(block2, 1024, new Quantity(1e25));

        Entity tombstone = new Entity("Tombstone", 1);
        TombstoneBlock.markDeleted(tombstone,100);
        TombstoneBlock.markDeleted(tombstone,301);
        TombstoneBlock.markDeleted(tombstone, 303);
        TombstoneBlock.markDeleted(tombstone, 11 * 1024);


        FormEntity header = new FormEntity();
        header.setNumberedRecordCount(1024*40);
        header.setDeletedCount(4);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header, ImmutableList.of(tombstone).iterator());

        ColumnView view = block.buildView(header, tombstoneIndex, Arrays.asList(block1, block2).iterator());

        assertThat(view.getDouble(0), equalTo(42d));
        assertThat(view.isMissing(1), equalTo(true));
        assertThat(view.getDouble(299), equalTo(98d));
        assertThat(view.getDouble(300), equalTo(91d));

        // Next values live on block 3, which starts at 10k * 3 = 30720, but 4 records prior
        // to this block have been deleted
        assertThat(view.getDouble(30726), equalTo(1.5d));


    }


}