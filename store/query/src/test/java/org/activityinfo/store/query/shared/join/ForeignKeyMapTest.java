package org.activityinfo.store.query.shared.join;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.shared.TableFilter;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertThat;

public class ForeignKeyMapTest {


    @Test
    public void filtering() {

        // Build the unfiltered foreign key map
        // That maps row indexes to foreign keys
        Multimap<Integer, ResourceId> unfilteredKeys = HashMultimap.create();
        unfilteredKeys.put(0, ResourceId.valueOf("s0272548382"));
        unfilteredKeys.put(1, ResourceId.valueOf("s0272548382"));
        unfilteredKeys.put(2, ResourceId.valueOf("s0362622291"));
        unfilteredKeys.put(3, ResourceId.valueOf("s0362622291"));
        unfilteredKeys.put(4, ResourceId.valueOf("s0890848243"));
        unfilteredKeys.put(5, ResourceId.valueOf("s0890848243"));
        ForeignKeyMap fkMap = new ForeignKeyMap(6, unfilteredKeys);


        // Now define a filter that includes only the 4th and 5th rows
        BitSet bitSet = new BitSet();
        bitSet.set(4);
        bitSet.set(5);
        TableFilter filter = new TableFilter(bitSet);


        // Apply the filter to the ForeignKey map and verify the results
        ForeignKeyMap filteredMap = filter.apply(fkMap);

        assertThat(filteredMap.getKeys(0), Matchers.hasItems(ResourceId.valueOf("s0890848243")));
        assertThat(filteredMap.getKeys(1), Matchers.hasItems(ResourceId.valueOf("s0890848243")));

    }

}