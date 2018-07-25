package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static java.util.Collections.emptyIterator;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RecordIdBlockTest {


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
    public void test() {
        RecordIdBlock block = new RecordIdBlock();

        Entity blockEntity = new Entity("Block", 1);

        block.update(blockEntity, 0, TextValue.valueOf("c143535xqw"));
        block.update(blockEntity, 1, TextValue.valueOf("c1adssdxqw"));
        block.update(blockEntity, 2, TextValue.valueOf("c3x5yxhxqw"));
        block.update(blockEntity, 3, TextValue.valueOf("cadsffdxqw"));
        block.update(blockEntity, 4, TextValue.valueOf("c1dfd3552w"));

        FormEntity header = new FormEntity();
        header.setNumberedRecordCount(5);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header, emptyIterator());

        ColumnView view = block.buildView(header, tombstoneIndex, Arrays.asList(blockEntity).iterator());
        assertThat(view.numRows(), equalTo(5));
        assertThat(view.getString(0), equalTo("c143535xqw"));
        assertThat(view.getString(1), equalTo("c1adssdxqw"));
        assertThat(view.getString(2), equalTo("c3x5yxhxqw"));
        assertThat(view.getString(3), equalTo("cadsffdxqw"));
        assertThat(view.getString(4), equalTo("c1dfd3552w"));

        // Delete two records

        Entity tombstone = new Entity("Tombstone", 1);
        TombstoneBlock.markDeleted(tombstone, 2);
        TombstoneBlock.markDeleted(tombstone, 4);

        header.setDeletedCount(2);

        tombstoneIndex = new TombstoneIndex(header, Arrays.asList(tombstone).iterator());

        view = block.buildView(header, tombstoneIndex, Arrays.asList(blockEntity).iterator());
        assertThat(view.numRows(), equalTo(3));
        assertThat(view.getString(0), equalTo("c143535xqw"));
        assertThat(view.getString(1), equalTo("c1adssdxqw"));
        assertThat(view.getString(2), equalTo("cadsffdxqw"));
    }

    @Test
    public void deleted() {
        RecordIdBlock block = new RecordIdBlock();

        Entity blockEntity1 = new Entity("Block", 1);
        for (int i = 0; i < block.getBlockSize(); i++) {
            block.update(blockEntity1, i, TextValue.valueOf("a" + i));
        }

        Entity blockEntity2 = new Entity("Block", 2);
        for (int i = 0; i < block.getBlockSize(); i++) {
            block.update(blockEntity2, i, TextValue.valueOf("b" + i));
        }

        FormEntity header = new FormEntity();
        header.setNumberedRecordCount(block.getBlockSize() * 2);
        header.setDeletedCount(1);

        Entity tombstone = new Entity("Tombstone", 1);
        TombstoneBlock.markDeleted(tombstone, 11);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header, Collections.singleton(tombstone).iterator());

        ColumnView view = block.buildView(header, tombstoneIndex, Arrays.asList(blockEntity1, blockEntity2).iterator());
        assertThat(view.numRows(), equalTo(block.getBlockSize() * 2 - 1));

    }

}