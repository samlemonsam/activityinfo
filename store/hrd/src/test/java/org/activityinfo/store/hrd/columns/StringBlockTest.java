package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.store.hrd.entity.FormColumnStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class StringBlockTest {

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));

    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }


    @Test
    public void strings() {
        StringBlock block = (StringBlock) BlockFactory.get(TextType.SIMPLE);

        Entity blockEntity = new Entity("Block", 1);

        block.update(blockEntity, 0, TextValue.valueOf("Hello World"));
        block.update(blockEntity, 2, TextValue.valueOf(null));
        block.update(blockEntity, 1, TextValue.valueOf("Sue"));
        block.update(blockEntity, 8, TextValue.valueOf("Bob"));
        block.update(blockEntity, 9, TextValue.valueOf("Dan"));

        FormColumnStorage header = new FormColumnStorage();
        header.setRecordCount(10);

        ColumnView view = block.buildView(header, Arrays.asList(blockEntity).iterator());
        assertThat(view.getString(0), equalTo("Hello World"));
        assertThat(view.getString(1), equalTo("Sue"));
        assertThat(view.getString(2), nullValue());
        assertThat(view.getString(8), equalTo("Bob"));
        assertThat(view.getString(9), equalTo("Dan"));
    }
}