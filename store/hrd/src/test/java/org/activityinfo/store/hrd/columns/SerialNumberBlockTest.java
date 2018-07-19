package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SerialNumberBlockTest {

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
    public void noPrefixes() {
        SerialNumberBlock block = new SerialNumberBlock(new SerialNumberType());
        Entity blockEntity = new Entity("Block", 1);

        block.update(blockEntity, 0, new SerialNumber(1));
        block.update(blockEntity, 2, new SerialNumber(2));
        block.update(blockEntity, 1, new SerialNumber(3));
        block.update(blockEntity, 8, new SerialNumber(4));

        FormEntity header = new FormEntity();
        header.setRecordCount(10);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header);

        ColumnView view = block.buildView(header, tombstoneIndex, Arrays.asList(blockEntity).iterator());

        assertThat(view.numRows(), equalTo(10));
        assertThat(view.getString(0), equalTo("00001"));
        assertThat(view.getString(1), equalTo("00003"));
        assertThat(view.getString(2), equalTo("00002"));
        assertThat(view.isMissing(3), equalTo(true));
        assertThat(view.isMissing(4), equalTo(true));
        assertThat(view.isMissing(5), equalTo(true));
        assertThat(view.getString(8), equalTo("00004"));
    }

    @Test
    public void prefixes() {

        SerialNumberBlock block = new SerialNumberBlock(new SerialNumberType());
        Entity blockEntity = new Entity("Block", 1);

        block.update(blockEntity, 0, new SerialNumber("ABC", 1));
        block.update(blockEntity, 1, new SerialNumber("XYZ", 1));
        block.update(blockEntity, 2, new SerialNumber("ABC", 2));
        block.update(blockEntity, 3, new SerialNumber("XYZ", 3));
        block.update(blockEntity, 4, new SerialNumber("ABC", 3));
        block.update(blockEntity, 5, new SerialNumber("ATC", 1));

        FormEntity header = new FormEntity();
        header.setRecordCount(6);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header);

        ColumnView view = block.buildView(header, tombstoneIndex, Arrays.asList(blockEntity).iterator());

        assertThat(view.numRows(), equalTo(6));
        assertThat(view.getString(0), equalTo("ABC-00001"));
        assertThat(view.getString(1), equalTo("XYZ-00001"));
        assertThat(view.getString(2), equalTo("ABC-00002"));
        assertThat(view.getString(3), equalTo("XYZ-00003"));
        assertThat(view.getString(4), equalTo("ABC-00003"));
        assertThat(view.getString(5), equalTo("ATC-00001"));
    }
}