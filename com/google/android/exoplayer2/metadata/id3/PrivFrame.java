package com.google.android.exoplayer2.metadata.id3;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.exoplayer2.util.Util;
import java.util.Arrays;

public final class PrivFrame extends Id3Frame {
    public static final Creator<PrivFrame> CREATOR = new Creator<PrivFrame>() {
        public PrivFrame createFromParcel(Parcel in) {
            return new PrivFrame(in);
        }

        public PrivFrame[] newArray(int size) {
            return new PrivFrame[size];
        }
    };
    public static final String ID = "PRIV";
    public final String owner;
    public final byte[] privateData;

    public PrivFrame(String owner, byte[] privateData) {
        super(ID);
        this.owner = owner;
        this.privateData = privateData;
    }

    PrivFrame(Parcel in) {
        super(ID);
        this.owner = (String) Util.castNonNull(in.readString());
        this.privateData = (byte[]) Util.castNonNull(in.createByteArray());
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PrivFrame other = (PrivFrame) obj;
        if (Util.areEqual(this.owner, other.owner) && Arrays.equals(this.privateData, other.privateData)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (((this.owner != null ? this.owner.hashCode() : 0) + 527) * 31) + Arrays.hashCode(this.privateData);
    }

    public String toString() {
        return this.id + ": owner=" + this.owner;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.owner);
        dest.writeByteArray(this.privateData);
    }
}
