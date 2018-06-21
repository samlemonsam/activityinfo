package org.activityinfo.store.hrd.columns;

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

}