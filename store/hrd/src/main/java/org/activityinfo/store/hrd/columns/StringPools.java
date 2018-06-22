package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.primitives.Chars;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Routines for a reading and manipulating string pools encoded as byte arrays. Strings present in the pool
 * can be referenced by their index.
 *
 * <p>String pools are prefixed with a two-byte unsigned integer indicating the length of the string pool.</p>
 *
 * <p>Strings present in the pool are limited to 65536 bytes (encoded).</p>
 */
public class StringPools {

    public static final int MAX_SIZE = Character.MAX_VALUE;

    private static final int POOL_SIZE_BYTES = 2;
    private static final int ELEMENT_LENGTH_BYTES = 2;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Search for a UTF-8 encoded string in a pool of length-prefixed UTF-8 strings.
     */
    @VisibleForTesting
    static int find(byte[] pool, byte[] newValue) {
        int pos = POOL_SIZE_BYTES;
        int index = 0;
        while(pos < pool.length) {
            int stringLength = Chars.fromBytes(pool[pos], pool[pos+1]);
            pos += ELEMENT_LENGTH_BYTES;
            if (stringLength == newValue.length && equal(pool, newValue, pos, stringLength)) {
                return index;
            }
        }
        return -1;
    }

    public static Blob appendString(Blob stringPool, String string) {
        if(stringPool == null) {
            return new Blob(newPool(string));
        } else {
            return new Blob(appendString(stringPool.getBytes(), string.getBytes(Charsets.UTF_8)));
        }
    }

    /**
     * Expands the pool to accomodate a new string
     *
     * @param stringPool
     * @param newValueBytes
     * @return
     */
    public static byte[] appendString(byte[] stringPool, byte[] newValueBytes) {
        int newPoolSizeBytes = stringPool.length + ELEMENT_LENGTH_BYTES + newValueBytes.length;
        byte[] expandedPool = Arrays.copyOf(stringPool, newPoolSizeBytes);

        // Read the current size of the pool
        int poolSize = size(expandedPool);

        // Update the size of the pool
        setLength(expandedPool, 0, (char)(poolSize + 1));

        // Append the new string
        setLength(expandedPool, stringPool.length, newValueBytes.length);
        System.arraycopy(newValueBytes, 0, expandedPool, stringPool.length + ELEMENT_LENGTH_BYTES,
                newValueBytes.length);

        return expandedPool;
    }

    public static int size(byte[] stringPool) {
        return (char)((stringPool[0] << 8) + stringPool[1]);
    }

    public static String[] toArray(byte[] pool) {
        String[] array = new String[size(pool)];
        toArray(pool, array, 0);
        return array;
    }

    public static void toArray(byte[] pool, String[] array, int offset) {
        int poolSize = size(pool);
        int pos = POOL_SIZE_BYTES;
        for (int i = 0; i < poolSize; i++) {
            int elementLength = Chars.fromBytes(pool[pos], pool[pos+1]);
            pos+=2;

            array[i + offset] = new String(pool, pos, elementLength, Charsets.UTF_8);
            pos+=elementLength;
        }
    }

    public static String[] toArray(Blob blob) {
        if(blob == null) {
            return EMPTY_STRING_ARRAY;
        } else {
            return toArray(blob.getBytes());
        }
    }

    public static void toArray(Blob blob, String[] array, int offset) {
        if(blob != null) {
            toArray(blob.getBytes(), array, offset);
        }
    }

    public static byte[] newPool(String... array) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(bytes);
            output.writeChar((char)array.length);
            for (int i = 0; i < array.length; i++) {
                output.writeUTF(array[i]);
            }
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setLength(byte[] pool, int offset, int value) {
        pool[offset] = (byte)(value >> 8);
        pool[offset+1] =  (byte) value;
    }

    private static boolean equal(byte[] pool, byte[] string, int pos, int length) {
        for (int i = 0; i < length; i++) {
            if(pool[pos + i] != string[i]) {
                return false;
            }
        }
        return true;
    }

    public static int size(Blob pool) {
        if(pool == null) {
            return 0;
        }
        return size(pool.getBytes());
    }
}
