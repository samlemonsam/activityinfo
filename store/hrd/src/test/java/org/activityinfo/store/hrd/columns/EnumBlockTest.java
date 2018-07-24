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
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class EnumBlockTest {

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
    public void singleEnum() {

        EnumItem blue = new EnumItem(ResourceId.valueOf("c342fjsfjkdd"), "Blue");
        EnumItem green = new EnumItem(ResourceId.valueOf("df2345253xkd"), "Green");
        EnumItem violet = new EnumItem(ResourceId.valueOf("abcdef344444"), "Violet");
        EnumType enumType = new EnumType(Cardinality.SINGLE, blue, green, violet);
        FormField enumField = new FormField(ResourceId.valueOf("FF")).setType(enumType);

        SingleEnumBlock block = (SingleEnumBlock) BlockFactory.get(enumField);

        Entity blockEntity = new Entity("Block", 1);

        block.update(blockEntity, 0, new EnumValue(blue.getId()));
        block.update(blockEntity, 2, new EnumValue(green.getId()));
        block.update(blockEntity, 2, new EnumValue(blue.getId()));
        block.update(blockEntity, 2, new EnumValue(violet.getId()));
        block.update(blockEntity, 2, new EnumValue(green.getId()));
        block.update(blockEntity, 1, new EnumValue(violet.getId()));
        block.update(blockEntity, 3, null);
        block.update(blockEntity, 4, new EnumValue(blue.getId()));

        FormEntity header = new FormEntity();
        header.setNumberedRecordCount(5);

        TombstoneIndex tombstoneIndex = new TombstoneIndex(header, Collections.emptyIterator());

        ColumnView view = block.buildView(header, tombstoneIndex, Arrays.asList(blockEntity).iterator());
        assertThat(view.getString(0), equalTo("Blue"));
        assertThat(view.getString(1), equalTo("Violet"));
        assertThat(view.getString(2), equalTo("Green"));
        assertThat(view.getString(3), nullValue());
        assertThat(view.getString(4), equalTo("Blue"));
    }
}