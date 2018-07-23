package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.PropertyContainer;

import java.util.Arrays;
import java.util.BitSet;

public class BlobBitSet {

    public final static byte[] EMPTY = new byte[0];

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

        Blob updated = update(blob, index, value);

        if(blob == updated) {
            return false;

        } else {
            blockEntity.setProperty(propertyName, updated);
            return true;
        }
    }

    public static Blob update(Blob blob, int index, boolean value) {
        if(blob == null && !value) {
            return null;
        }

        int capacity = 0;
        if(blob != null) {
            capacity = blob.getBytes().length * 8;
        }

        // Unallocated bits at the end of the blob are assumed to be false
        if(index >= capacity && !value) {
            return blob;
        }

        byte[] bytes = ensureCapacity(blob, index);
        set(bytes, index, value);

        return new Blob(bytes);
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

    /**
     * Counts the number of bits set in the byte-backed BitSet.
     */
    public static int cardinality(byte[] bytes) {
        int count = 0;
        int n = bytes.length;
        for (int i = 0; i < n; i++) {
            count += BIT_COUNT_LOOKUP[bytes[i] & 0xFF];
        }
        return count;
    }

    /**
     * Counts the number of bits set in the first {@code bitCount}-bits.
     *
     * @param bytes a BitSet encoded as an array of bytes
     * @param bitCount the number of bits to inspect.
     * @return the number of bits set in the first bitCount bits.
     */
    public static int cardinality(byte[] bytes, int bitCount) {
        int count = 0;

        // Use the lookup table for the first
        int byteCount = bitCount / 8;
        int bytesToRead = Math.min(byteCount, bytes.length);
        for (int i = 0; i < bytesToRead; i++) {
            count += BIT_COUNT_LOOKUP[bytes[i] & 0xFF];
        }
        // Count any remaining bits one-by-one
        if(bytes.length > byteCount) {
            int bitIndex = (byteCount * 8);
            while (bitIndex < bitCount) {
                if (get(bytes, bitIndex)) {
                    count++;
                }
                bitIndex++;
            }
        }
        return count;
    }

    /**
     * Converts a subset of a partitioned BlobBitSet to a {@link BitSet}, which is backed by {@code long}s.
     *
     * Having this representation is useful when looping over a relatively smaller block of rows to see whether
     * they are deleted.
     *
     * @param blocks an array of byte[] ar
     * @param blockSize the size of each block, in bits
     * @param startBitIndex the start bit index
     * @param length the number of bits to extract
     * @return
     */
    public static BitSet toBitSet(byte[][] blocks, int blockSize, int startBitIndex, int length) {
        BitSet bitSet = new BitSet(length);
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

                int lastBit = Math.min(8, remainingBits);
                int bitCount = lastBit - bitIndex;

                if(byteIndex < block.length) {
                    int b = block[byteIndex];

                    // ITERATE over BITS
                    if (b != 0) {
                        for (int i = bitIndex; i < lastBit; i++) {
                            if ((b & (1 << i)) != 0) {
                                bitSet.set(destBitIndex + i - bitIndex);
                            }
                        }
                    }
                }

                remainingBits -= bitCount;
                destBitIndex += bitCount;

                // Advance to next byte
                bitIndex = 0;
                byteIndex++;
            }

            // Advance to next block
            byteIndex = 0;
            blockIndex++;
        }

        return bitSet;
    }
}
