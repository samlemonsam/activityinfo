package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.common.base.Charsets;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class StringPoolsTest {

    @Test
    public void test() {

        byte[] pool = StringPools.newPool("Hello world");
        assertThat(StringPools.size(pool), equalTo(1));

        assertThat(StringPools.toArray(pool), Matchers.arrayContaining("Hello world"));

        pool = StringPools.appendString(pool, "Goodbye cruel world".getBytes(Charsets.UTF_8));

        assertThat(StringPools.toArray(pool), Matchers.arrayContaining("Hello world", "Goodbye cruel world"));

    }

    @Test
    public void findOrInsert() {

        EmbeddedEntity entity = new EmbeddedEntity();

        int index1 = StringPools.findOrInsertStringInPool(entity, "pool", "HH1");
        int index2 = StringPools.findOrInsertStringInPool(entity, "pool", "HH2");
        int index3 = StringPools.findOrInsertStringInPool(entity, "pool", "HH2");
        int index4 = StringPools.findOrInsertStringInPool(entity, "pool", "HHH2");

        Blob pool = (Blob) entity.getProperty("pool");

        assertThat(StringPools.toArray(pool), Matchers.arrayContaining("HH1", "HH2", "HHH2"));
        assertThat(index1, equalTo(1));
        assertThat(index2, equalTo(2));
        assertThat(index3, equalTo(2));
        assertThat(index4, equalTo(3));


    }

}
