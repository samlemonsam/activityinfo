package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.EmbeddedEntity;
import org.junit.Test;

import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class BlobBitSetTest {

    @Test
    public void test() {

        EmbeddedEntity entity = new EmbeddedEntity();

        BlobBitSet.update(entity, "bits", 1, true);
        BlobBitSet.update(entity, "bits", 3, true);
        BlobBitSet.update(entity, "bits", 50, false);
        BlobBitSet.update(entity, "bits", 230, true);
        BlobBitSet.update(entity, "bits", 10240, false);
        BlobBitSet.update(entity, "bits", 1, false);

        byte[] bytes = BlobBitSet.read(entity, "bits");

        assertThat(bytes.length, equalTo(29));

        assertThat(BlobBitSet.get(bytes, 0), equalTo(false));
        assertThat(BlobBitSet.get(bytes, 1), equalTo(false));
        assertThat(BlobBitSet.get(bytes, 3), equalTo(true));
        assertThat(BlobBitSet.get(bytes, 4), equalTo(false));
        assertThat(BlobBitSet.get(bytes, 50), equalTo(false));
        assertThat(BlobBitSet.get(bytes, 230), equalTo(true));
        assertThat(BlobBitSet.get(bytes, 10240), equalTo(false));

        assertThat(BlobBitSet.cardinality(bytes), equalTo(2));
    }

    @Test
    public void blocks() {

        int blockSize = 16;
        byte blocks[][] = new byte[][]{
                block("XX**X***X**X****"),
                block("*******X"),
                block(""),
                block("***************X")
        };


        // Make sure we've declared the blocks correctly
        assertThat(BlobBitSet.get(blocks[0], 0), equalTo(true));
        assertThat(BlobBitSet.get(blocks[0], 1), equalTo(true));
        assertThat(BlobBitSet.get(blocks[0], 2), equalTo(false));
        assertThat(BlobBitSet.get(blocks[0], 4), equalTo(true));
        assertThat(BlobBitSet.get(blocks[0], 8), equalTo(true));
        assertThat(BlobBitSet.get(blocks[0], 9), equalTo(false));
        assertThat(BlobBitSet.get(blocks[0], 10), equalTo(false));
        assertThat(BlobBitSet.get(blocks[0], 11), equalTo(true));


        assertThat(BlobBitSet.get(blocks[1], 0), equalTo(false));
        assertThat(BlobBitSet.get(blocks[1], 7), equalTo(true));

        assertThat(BlobBitSet.get(blocks[2], 15), equalTo(false));

        assertThat(BlobBitSet.get(blocks[3], 15), equalTo(true));

        // Now convert the whole thing to a bit set
        BitSet bitSet = BlobBitSet.toBitSet(blocks, blockSize, 0, 16 * 4);

        assertThat(toList(bitSet), contains(0, 1, 4, 8, 11, 23, 63));

        // Take a subset that straddles byte boundaries
        BitSet slice = BlobBitSet.toBitSet(blocks, blockSize, 10, 20);

        assertThat(toList(slice), contains(1, 13));
    }

    @Test
    public void cardinality() {
        byte[] a = new byte[] { 0b1001 };
        byte[] b = new byte[] {(byte) 0xFF, 0b1 };

        assertThat(BlobBitSet.cardinality(a), equalTo(2));
        assertThat(BlobBitSet.cardinality(a, 3), equalTo(1));
        assertThat(BlobBitSet.cardinality(a, 4), equalTo(2));
        assertThat(BlobBitSet.cardinality(a, 8), equalTo(2));
        assertThat(BlobBitSet.cardinality(a, 1047), equalTo(2));

        assertThat(BlobBitSet.cardinality(b, 4), equalTo(4));
        assertThat(BlobBitSet.cardinality(b, 8), equalTo(8));
        assertThat(BlobBitSet.cardinality(b, 9), equalTo(9));
        assertThat(BlobBitSet.cardinality(b, 10), equalTo(9));
        assertThat(BlobBitSet.cardinality(b, 16), equalTo(9));
        assertThat(BlobBitSet.cardinality(b, 18), equalTo(9));
    }

    private List<Integer> toList(BitSet bitSet) {
        return bitSet.stream().boxed().collect(Collectors.toList());
    }

    private byte[] block(String string) {
        assert string.length() <= 16;
        EmbeddedEntity entity = new EmbeddedEntity();
        for (int i = 0; i < string.length(); i++) {
            BlobBitSet.update(entity, "bits", i, string.charAt(i) == 'X');
        }
        return BlobBitSet.read(entity, "bits");
    }

}