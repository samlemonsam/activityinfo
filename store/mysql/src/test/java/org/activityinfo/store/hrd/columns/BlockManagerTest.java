package org.activityinfo.store.hrd.columns;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class BlockManagerTest {

    @Test
    public void test() {

        StringBlock block = new StringBlock("myfield", x -> "");

        assertThat(block.getBlockIndex(1), equalTo(0));
    }


}