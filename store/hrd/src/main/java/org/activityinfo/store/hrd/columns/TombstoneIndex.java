package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.store.hrd.entity.FormColumnStorage;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;

public class TombstoneIndex {

    private final int blockCount;
    private final byte[][] blocks;

    /**
     * For each block _i_, deletedBefore[i] is the number of records that have been deleted that fall
     * in blocks 0..i-1
     */
    private final int[] deletedBefore;

    public TombstoneIndex(FormColumnStorage columnStorage, Iterator<Entity> tombstoneBlocks) {
        this.blockCount = (columnStorage.getRecordCount() / TombstoneBlock.BLOCK_SIZE) + 1;
        this.blocks = new byte[blockCount][];
        this.deletedBefore = new int[blockCount];

        Arrays.fill(this.blocks, BlobBitSet.EMPTY);

        int counts[] = new int[blockCount];

        while(tombstoneBlocks.hasNext()) {
            Entity tombstone = tombstoneBlocks.next();
            int blockIndex = (int)(tombstone.getKey().getId() - 1);
            byte[] bitset = TombstoneBlock.getBitset(tombstone);

            blocks[blockIndex] = bitset;
            counts[blockIndex] = BlobBitSet.cardinality(bitset);
        }

        for (int i = 1; i < blockCount; i++) {
            deletedBefore[i] = deletedBefore[i - 1] + counts[i - 1];
        }
    }

    public TombstoneIndex(FormColumnStorage header) {
        this(header, Collections.emptyIterator());
    }

    /**
     * Counts the number of numbered records that have been deleted, prior to the zero-based {@code recordIndex}
     */
    public int countDeletedBefore(int recordIndex) {
        int blockIndex = blockIndex(recordIndex);
        int recordOffset = recordIndex % TombstoneBlock.BLOCK_SIZE;
        byte[] block = blocks[blockIndex];

        return deletedBefore[blockIndex] + BlobBitSet.cardinality(block, recordOffset);
    }

    private int blockIndex(int recordIndex) {
        return recordIndex / TombstoneBlock.BLOCK_SIZE;
    }

    public BitSet getDeletedBitSet(int startIndex, int length) {
        return BlobBitSet.toBitSet(blocks, TombstoneBlock.BLOCK_SIZE, startIndex, length);
    }

    public boolean isDeleted(int recordIndex) {
        int blockIndex = blockIndex(recordIndex);
        int recordOffset = recordIndex % TombstoneBlock.BLOCK_SIZE;

        return BlobBitSet.get(blocks[blockIndex], recordOffset);
    }
}
