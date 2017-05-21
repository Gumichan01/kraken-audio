package com.pl.multicast.kraken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * KrakenCache is the class responsible of handling the memory cache
 */
public class KrakenCache {

    private static final int CACHE_SIZE = 64;
    private List<Byte> buffer; // Cache memory
    private int mcsize;   // maximum cache size

    public KrakenCache() {
        this(CACHE_SIZE);
    }

    public KrakenCache(int size) {

        buffer = new ArrayList<>();
        mcsize = size;
    }

    /**
     * Write a bloc of data into the cache memory
     *
     * @param array bloc to write
     * @param len   size of the bloc
     */
    public synchronized void write(byte[] array, int len) {

        Byte[] arr = toObjects(Arrays.copyOfRange(array, 0, len));
        buffer.addAll(Arrays.asList(arr));
    }

    /**
     * Returns the bloc of bytes from the cache
     *
     * @param sz maximum size of the bloc
     */
    private byte[] read(int sz) {

        int szread = sz > buffer.size() ? buffer.size() : sz;
        byte[] bytes = new byte[szread];

        for (int i = 0; i < szread; i++) {

            bytes[i] = buffer.get(i);
        }
        buffer.clear();

        return bytes;
    }


    public synchronized byte[] readAll() {
        return read(buffer.size());
    }

    public synchronized boolean isFull() {
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
}
