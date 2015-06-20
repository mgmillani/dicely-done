package br.ufrgs.inf.dicelydone.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Possible values for betting chips.
 */
public enum Chip implements Parcelable {
    ONE(0, 1), FIVE(1, 5), TEN(2, 10);

    private final int index;
    private final int value;

    Chip(int idx, int val) {
        this.index = idx;
        this.value = val;
    }

    public int getValue() { return value; }

    /** Gets the index for this chip in {@link Chip#values()}. */
    public int getIndex() { return index; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(index);
    }

    private static final Parcelable.Creator<Chip> CREATOR = new Parcelable.Creator<Chip>() {
        @Override
        public Chip createFromParcel(Parcel source) {
            return values()[source.readInt()];
        }

        @Override
        public Chip[] newArray(int size) {
            return new Chip[size];
        }
    };


}
