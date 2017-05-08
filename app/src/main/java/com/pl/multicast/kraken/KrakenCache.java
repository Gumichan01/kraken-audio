package com.pl.multicast.kraken;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * KrakenCache is the class responsible of handling the memory cache
 */
public class KrakenCache {

    private static final int CACHE_SIZE = 8000;
    private List<Byte> buffer; // Cache memory

    public KrakenCache() {

        buffer = Collections.synchronizedList(new LinkedList<Byte>());
    }

    /**
     * Write a bloc of data into the cache memory
     *
     * @param array bloc to write
     * @param len   size of the bloc
     */
    public void write(byte[] array, int len) {

        Byte[] arr = toObjects(Arrays.copyOfRange(array, 0, len));
        buffer.addAll(Arrays.asList(arr));
    }

    /**
     * Returns the bloc of bytes from the cache
     *
     * @param sz maximum size of the bloc
     */
    public byte[] read(int sz) {

        int i = 0;
        int szread = sz > buffer.size() ? buffer.size() : sz;
        byte[] bytes = new byte[szread];

        for (; i < szread; i++) {

            bytes[i] = buffer.get(0);
            buffer.remove(0);
        }

        return bytes;
    }


    public byte [] readAll() {
        return read(buffer.size());
    }

    public boolean isEmpty() {
        return buffer.size() == 0;
    }

    public boolean isFull() {
        return buffer.size() >= CACHE_SIZE;
    }

    /**
     * Clear the cache
     */
    public void clear() {

        buffer.clear();
    }

    // byte[] to Byte[]
    private Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];

        int i = 0;
        for (byte b : bytesPrim)
            bytes[i++] = b; // Autoboxing

        return bytes;
    }

    // Byte[] to byte[]
    private byte[] toPrimitives(Byte[] oBytes) {
        byte[] bytes = new byte[oBytes.length];

        for (int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }

        return bytes;
    }

}
