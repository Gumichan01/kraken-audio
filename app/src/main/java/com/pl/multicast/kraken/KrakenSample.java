package com.pl.multicast.kraken;

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

    public KrakenSample(byte[] bytes, int duration) {

        name = "sample #" + (id++);
        data = bytes;
        this.duration = duration;
    }

    protected KrakenSample(Parcel in) {
        name = in.readString();
        data = in.createByteArray();
        duration = in.readInt();
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

    public byte[] getData() {

        return data;
    }

    public int getDuration() {

        return duration;
    }

    public String toString() {

        return name;
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
    }
}
