package com.pl.multicast.kraken;

/**
 * KrakenSample store a generated sound
 */
public class KrakenSample {

    private static int id = 1;
    private String name;
    private byte [] data;
    private int duration;

    public KrakenSample(byte[] bytes, int duration) {

        name = "sample #" + (id++);
        data = bytes;
        this.duration = duration;
    }

    public byte [] getData() {

        return data;
    }

    public int getDuration() {

        return duration;
    }

    public String toString() {

        return name;
    }
}
