package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.model.type.time.YearType;
import org.activityinfo.model.type.time.YearValue;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyIterator;
import static org.activityinfo.store.testing.ColumnSetMatchers.hasValues;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
        NumberBlock block = quantityBlock();

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
    public void integersSetBlank() {

        NumberBlock block = quantityBlock();

        Entity blockEntity = new Entity("Block", 1);

        block.update(blockEntity, 0, new Quantity(1000));
        block.update(blockEntity, 1, new Quantity(1001));
        block.update(blockEntity, 0, null);

        FormEntity header = new FormEntity();
        header.setNumberedRecordCount(3);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header, emptyIterator());

        ColumnView view = block.buildView(header, tombstoneIndex, Arrays.asList(blockEntity).iterator());

        assertThat(view.isMissing(0), equalTo(true));
        assertThat(view.getDouble(1), equalTo(1001d));
        assertThat(view.isMissing(2), equalTo(true));
    }

    @Test
    public void years() {

        FormField quantityField = new FormField(ResourceId.valueOf("F")).setType(YearType.INSTANCE);
        NumberBlock block = (NumberBlock) BlockFactory.get(quantityField);

        Entity blockEntity = new Entity("Block", 1);

        block.update(blockEntity, 0, new YearValue(1992));
        block.update(blockEntity, 1, new YearValue(2004));
        block.update(blockEntity, 0, null);
        block.update(blockEntity, 2, new YearValue(2020));
        block.update(blockEntity, 4, new YearValue(1982));

        FormEntity header = new FormEntity();
        header.setNumberedRecordCount(5);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header, emptyIterator());

        ColumnView view = block.buildView(header, tombstoneIndex, Arrays.asList(blockEntity).iterator());

        assertThat(view.isMissing(0), equalTo(true));
        assertThat(view.getDouble(1), equalTo(2004d));
        assertThat(view.getDouble(2), equalTo(2020d));
        assertThat(view.isMissing(3), equalTo(true));
        assertThat(view.getDouble(4), equalTo(1982d));
    }


    @Test
    public void doubles() {
        NumberBlock block = quantityBlock();

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

    @Test
    public void doubleBlank() {

        NumberBlock block = quantityBlock();

        Entity blockEntity = new Entity("Block", 1);

        block.update(blockEntity, 0, new Quantity(3.14));
        block.update(blockEntity, 1, new Quantity(55));
        block.update(blockEntity, 0, null);
        block.update(blockEntity, 3, null);


        FormEntity header = new FormEntity();
        header.setNumberedRecordCount(4);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header, emptyIterator());

        ColumnView view = block.buildView(header, tombstoneIndex, Arrays.asList(blockEntity).iterator());

        assertThat(view.isMissing(0), equalTo(true));
        assertThat(view.getDouble(1), equalTo(55d));
        assertThat(view.isMissing(2), equalTo(true));
        assertThat(view.isMissing(3), equalTo(true));

    }

    @Test
    public void dates() {
        FormField quantityField = new FormField(ResourceId.valueOf("F")).setType(LocalDateType.INSTANCE);
        NumberBlock block = (NumberBlock) BlockFactory.get(quantityField);

        LocalDate startDate = new LocalDate(1980, 1, 1);
        LocalDate date = startDate;

        List<Entity> blocks = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Entity blockEntity = new Entity("Block", i + 1);
            for (int j = 0; j < block.getBlockSize(); j++) {
                block.update(blockEntity, j, date);
                date = date.plusDays(1);
            }
            blocks.add(blockEntity);
        }

        FormEntity header = new FormEntity();
        header.setNumberedRecordCount(block.getBlockSize() * 3);
        header.setDeletedCount(1);

        Entity tombstone = new Entity("Tombstone", 1);
        TombstoneBlock.markDeleted(tombstone, 2);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header, Collections.singleton(tombstone).iterator());

        ColumnView view = block.buildView(header, tombstoneIndex, blocks.iterator());
        assertThat(view.numRows(), equalTo(block.getBlockSize() * 3 - 1));
        assertThat(view.getString(0), equalTo(startDate.toString()));
        assertThat(view.getString(1), equalTo(startDate.plusDays(1).toString()));
        // Deleted record
        assertThat(view.getString(2), equalTo(startDate.plusDays(3).toString()));
        assertThat(view.getString(3), equalTo(startDate.plusDays(4).toString()));

        // Start of block 2
        assertThat(view.getString(block.getBlockSize()), equalTo(startDate.plusDays(block.getBlockSize() + 1).toString()));


        // Verify that filtering works...
        ColumnView selected = view.select(new int[]{1});
        assertThat(selected.numRows(), equalTo(1));
        assertThat(selected.getString(0), equalTo(startDate.plusDays(1).toString()));
    }

    @Test
    public void upgradeToDouble() {

        NumberBlock block = quantityBlock();

        Entity blockEntity = new Entity("Block", 1);

        // The first record is empty

        // The second two updates are integers
        block.update(blockEntity, 1, new Quantity(1000));
        block.update(blockEntity, 2, new Quantity(1001));

        // Now we have a double; the whole block needs to be migrated to double precision
        block.update(blockEntity, 3, new Quantity(1.5));


        // Verify that the values are correct

        FormEntity header = new FormEntity();
        header.setNumberedRecordCount(4);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header, emptyIterator());

        ColumnView view = block.buildView(header, tombstoneIndex, Arrays.asList(blockEntity).iterator());

        assertThat(view.isMissing(0), equalTo(true));
        assertThat(view.getDouble(1), equalTo(1000d));
        assertThat(view.getDouble(2), equalTo(1001d));
        assertThat(view.getDouble(3), equalTo(1.5));
    }


    @Test
    public void upgradeToDouble2() {

        NumberBlock block = quantityBlock();

        Entity blockEntity = new Entity("Block", 1);

        // The first record is empty

        // The second two updates are integers
        block.update(blockEntity, 1, new Quantity(1000));
        block.update(blockEntity, 2, new Quantity(1001));

        // Now we have a double; the whole block needs to be migrated to double precision
        block.update(blockEntity, 7, new Quantity(1.5));


        // Verify that the values are correct

        FormEntity header = new FormEntity();
        header.setNumberedRecordCount(10);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header, emptyIterator());

        ColumnView view = block.buildView(header, tombstoneIndex, Arrays.asList(blockEntity).iterator());

        assertThat(view.isMissing(0), equalTo(true));
        assertThat(view.getDouble(1), equalTo(1000d));
        assertThat(view.getDouble(2), equalTo(1001d));
        assertThat(view.isMissing(3), equalTo(true));
        assertThat(view.isMissing(4), equalTo(true));
        assertThat(view.isMissing(5), equalTo(true));
        assertThat(view.isMissing(6), equalTo(true));
        assertThat(view.getDouble(7), equalTo(1.5));
    }

    @Test
    public void noBlocks() {

        NumberBlock block = quantityBlock();

        // Verify that all values are missing

        FormEntity header = new FormEntity();
        header.setNumberedRecordCount(1000);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header, emptyIterator());

        ColumnView view = block.buildView(header, tombstoneIndex, Iterators.forArray());

        for (int i = 0; i < 1000; i++) {
            assertTrue(view.isMissing(i));
        }
    }


    private NumberBlock quantityBlock() {
        FormField quantityField = new FormField(ResourceId.valueOf("F")).setType(new QuantityType("households"));
        return (NumberBlock) BlockFactory.get(quantityField);
    }


    @Test(expected = UnsupportedOperationException.class)
    public void unsupportedStorageType() {

        NumberBlock block = quantityBlock();

        Entity blockEntity = new Entity("Block", 1);
        blockEntity.setUnindexedProperty(block.formatProperty, 9999);

        // Verify that the values are correct

        FormEntity header = new FormEntity();
        header.setNumberedRecordCount(10);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header, emptyIterator());

        ColumnView view = block.buildView(header, tombstoneIndex, Arrays.asList(blockEntity).iterator());
    }
}