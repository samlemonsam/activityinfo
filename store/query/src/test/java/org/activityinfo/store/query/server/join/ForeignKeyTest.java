package org.activityinfo.store.query.server.join;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.store.query.server.join.ForeignKeyBuilder;
import org.activityinfo.store.query.shared.columns.ForeignKey;
import org.activityinfo.store.query.shared.PendingSlot;
import org.activityinfo.store.query.shared.TableFilter;
import org.junit.Test;

import java.util.BitSet;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ForeignKeyTest {


    @Test
    public void filtering() {

        // Build the unfiltered foreign key map
        // That maps row indexes to foreign keys
        ResourceId formId = ResourceId.valueOf("a00001");
        ForeignKeyBuilder builder = new ForeignKeyBuilder(formId, new PendingSlot<ForeignKey>());
        builder.onNext(new ReferenceValue(new RecordRef(formId, ResourceId.valueOf("s0272548382"))));
        builder.onNext(new ReferenceValue(new RecordRef(formId, ResourceId.valueOf("s0272548382"))));
        builder.onNext(new ReferenceValue(new RecordRef(formId, ResourceId.valueOf("s0362622291"))));
        builder.onNext(new ReferenceValue(new RecordRef(formId, ResourceId.valueOf("s0362622291"))));
        builder.onNext(new ReferenceValue(new RecordRef(formId, ResourceId.valueOf("s0890848243"))));
        builder.onNext(new ReferenceValue(new RecordRef(formId, ResourceId.valueOf("s0890848243"))));

        ForeignKey fkMap = builder.build();


        // Now define a filter that includes only the 4th and 5th rows
        BitSet bitSet = new BitSet();
        bitSet.set(4);
        bitSet.set(5);
        TableFilter filter = new TableFilter(bitSet);


        // Apply the filter to the ForeignKey map and verify the results
        ForeignKey filteredKey = filter.apply(fkMap);

        assertThat(filteredKey.getKey(0), equalTo("s0890848243"));
        assertThat(filteredKey.getKey(1), equalTo("s0890848243"));

    }


    @Test
    public void emptyValues() {

        // Build the unfiltered foreign key map
        // That maps row indexes to foreign keys
        ResourceId formId = ResourceId.valueOf("a00001");
        ForeignKeyBuilder builder = new ForeignKeyBuilder(formId, new PendingSlot<ForeignKey>());
        builder.onNext(null);
        builder.onNext(new ReferenceValue(new RecordRef(formId, ResourceId.valueOf("s0272548382"))));
        builder.onNext(null);
        builder.onNext(null);

        ForeignKey fkMap = builder.build();

        assertThat(fkMap.numRows(), equalTo(4));

    }

    @Test
    public void multipleReferencedForms() {

        // Build the unfiltered foreign key map
        // That maps row indexes to foreign keys
        ResourceId province = ResourceId.valueOf("g00001");
        ResourceId territory = ResourceId.valueOf("g00002");

        ForeignKeyBuilder builder = new ForeignKeyBuilder(province, new PendingSlot<ForeignKey>());

        // Row 0: Missing
        builder.onNext(null);

        // Row 1: Multiple Provinces
        builder.onNext(new ReferenceValue(
            new RecordRef(province, ResourceId.valueOf("P1")),
            new RecordRef(province, ResourceId.valueOf("P2"))));

        // Row 2: One province, one territory
        builder.onNext(new ReferenceValue(
            new RecordRef(province, ResourceId.valueOf("P1")),
            new RecordRef(territory, ResourceId.valueOf("T1"))));

        // Row 3: Two territories
        builder.onNext(new ReferenceValue(
            new RecordRef(territory, ResourceId.valueOf("T1")),
            new RecordRef(territory, ResourceId.valueOf("T2"))));

        // Row 4: Missing
        builder.onNext(null);

        // Row 5: Missing
        builder.onNext(null);

        ForeignKey fkMap = builder.build();

        assertThat(fkMap.numRows(), equalTo(6));
        assertThat(fkMap.getKey(0), nullValue());
        assertThat(fkMap.getKey(1), nullValue());
        assertThat(fkMap.getKey(2), equalTo("P1"));
        assertThat(fkMap.getKey(3), nullValue());
        assertThat(fkMap.getKey(4), nullValue());
        assertThat(fkMap.getKey(5), nullValue());
    }
}