package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.PropertyContainer;

import java.util.Arrays;
import java.util.BitSet;

public class BlobBitSet {

    private final static byte[] EMPTY = new byte[0];

    private final static int ADDRESS_BITS_PER_WORD = 3;

    /**
     * A table which provides the number of bits set in each possible byte value.
     */
    private final static byte[] BIT_COUNT_LOOKUP = new byte[256];

    static {
        for (int i = 0; i < 256; i++) {
            BIT_COUNT_LOOKUP[i] = (byte)Integer.bitCount(i);
        }
    }

    public static boolean update(PropertyContainer blockEntity, String propertyName, int index, boolean value) {
        Blob blob = (Blob) blockEntity.getProperty(propertyName);
        if(blob == null && !value) {
            return false;
        }

        int capacity = 0;
        if(blob != null) {
            capacity = blob.getBytes().length * 8;
        }

        // Unallocated bits at the end of the blob are assumed to be false
        if(index >= capacity && !value) {
            return false;
        }

        byte[] bytes = ensureCapacity(blob, index);
        set(bytes, index, value);

        blockEntity.setProperty(propertyName, new Blob(bytes));

        return true;
    }


    private static byte[] ensureCapacity(Blob blob, int index) {
        int requiredCapacity = byteIndex(index) + 1;
        if(blob == null) {
            return new byte[requiredCapacity];
        }
        byte[] bytes = blob.getBytes();
        if(requiredCapacity <= bytes.length) {
            return bytes;
        } else {
            return Arrays.copyOf(bytes, requiredCapacity);
        }
    }

    /**
     * Given a bit index, return byte index containing it.
     */
    private static int byteIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }

    private static void set(byte[] bytes, int bitIndex, boolean value) {
        if(value) {
            set(bytes, bitIndex);
        } else {
            clear(bytes, bitIndex);
        }
    }

    private static void set(byte[] bytes, int bitIndex) {
        int byteIndex = byteIndex(bitIndex);
        int bitOffset = bitIndex % 8;
        bytes[byteIndex] |= (1 << bitOffset);
    }

    private static void clear(byte[] bytes, int bitIndex) {
        int byteIndex = byteIndex(bitIndex);
        int bitOffset = bitIndex % 8;
        if (byteIndex < bytes.length) {
            bytes[byteIndex] &= ~(1 << bitOffset);
        }
    }

    public static boolean get(byte[] bytes, int bitIndex) {
        int byteIndex = byteIndex(bitIndex);
        int bitOffset = bitIndex % 8;
        if (byteIndex < bytes.length) {
            return ((bytes[byteIndex] & (1 << bitOffset)) != 0);
        } else {
            return false;
        }
    }

    public static byte[] read(PropertyContainer entity, String propertyName) {
        Blob blob = (Blob) entity.getProperty(propertyName);
        if(blob == null) {
            return EMPTY;
        } else {
            return blob.getBytes();
        }
    }

    public static int cardinality(byte[] bytes) {
        return cardinality(bytes, 0, bytes.length);
    }

    public static int cardinality(byte[] bytes, int start, int length) {
        int count = 0;
        int n = start + Math.min(bytes.length, length);
        for (int i = start; i < n; i++) {
            count += BIT_COUNT_LOOKUP[bytes[i] & 0xFF];
        }
        return count;
    }

    /**
     *
     * @param blocks
     * @param blockSize the size of each block, in bits
     * @param startBitIndex the start bit index
     * @param length the number of bits to extract
     * @return
     */
    public static BitSet toBitSet(byte[][] blocks, int blockSize, int startBitIndex, int length) {
        BitSet bitSet = new BitSet();
        int remainingBits = length;
        int blockIndex = startBitIndex / blockSize;
        int bitOffset = startBitIndex % blockSize;
        int byteIndex = bitOffset / 8;
        int bitIndex = bitOffset % 8;
        int destBitIndex = 0;

        // ITERATE over BLOCKS
        while (remainingBits > 0) {
            byte[] block = blocks[blockIndex];

            // ITERATE over BYTES
            while (remainingBits > 0 && byteIndex < (blockSize / 8)) {
                if(byteIndex < block.length) {
                    int b = block[byteIndex];

                    // ITERATE over BITS
                    if (b != 0) {
                        int bn = Math.min(8, remainingBits);
                        for (int i = bitIndex; i < bn; i++) {
                            if ((b & (1 << i)) != 0) {
                                bitSet.set(destBitIndex + i - bitIndex);
                            }
                        }
                    }
                }
                // Advance to next byte
                bitIndex = 0;
                byteIndex++;
                remainingBits -= 8;
                destBitIndex += 8;
            }

            // Advance to next block
            byteIndex = 0;
            blockIndex++;
        }

        return bitSet;
    }
}
