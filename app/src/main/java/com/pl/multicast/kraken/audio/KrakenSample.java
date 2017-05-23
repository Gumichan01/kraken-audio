package com.pl.multicast.kraken.audio;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * KrakenSample store a generated sound
 */
public class KrakenSample implements Parcelable {

    private static int id = 1;
    private String name;
    private byte[] data;
    private int duration;
    private int frequency;

    public KrakenSample(byte[] bytes, int duration, int freq) {

        name = "sample #" + (id++);
        data = bytes;
        this.duration = duration;
        frequency = freq;
    }

    protected KrakenSample(Parcel in) {
        name = in.readString();
        data = in.createByteArray();
        duration = in.readInt();
        frequency = in.readInt();
    }

    public static final Creator<KrakenSample> CREATOR = new Creator<KrakenSample>() {
        @Override
        public KrakenSample createFromParcel(Parcel in) {
            return new KrakenSample(in);
        }

        @Override
        public KrakenSample[] newArray(int size) {
            return new KrakenSample[size];
        }
    };

    public String getName() {

        return name;
    }

    public byte[] getData() {

        return data;
    }

    public int getDuration() {

        return duration;
    }

    public String toString() {

        return name + " - " +  duration + " s " + "@" + frequency + "Hz";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(name);
        dest.writeByteArray(data);
        dest.writeInt(duration);
        dest.writeInt(frequency);
    }
}
