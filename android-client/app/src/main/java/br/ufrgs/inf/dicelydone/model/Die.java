package br.ufrgs.inf.dicelydone.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Random;

/**
 * Possible values resulting from throwing a die.
 */
public enum Die implements Parcelable {
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6);

    private int val;

    private Die(int val) {
        this.val = val;
    }

    /**
     * Creates a die by its numeric value.
     *
     * @throws IllegalArgumentException value not between 1 and 6.
     */
    public static Die byVal(int val) {
        switch (val) {
        case 1:
            return ONE;
        case 2:
            return TWO;
        case 3:
            return THREE;
        case 4:
            return FOUR;
        case 5:
            return FIVE;
        case 6:
            return SIX;
        default:
            throw new IllegalArgumentException("Dice values must be between one and six, inclusive.");
        }
    }

    /** Creates a die with a random value. */
    public static Die random(Random generator) {
        return byVal(generator.nextInt(6)+1);
    }

    /** Get the numeric value (between 1 and 6) */
    public int getValue() {
        return val;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(val);
    }

    public static final Parcelable.Creator<Die> CREATE = new Parcelable.Creator<Die>() {
        @Override
        public Die createFromParcel(Parcel in) {
            return byVal(in.readInt());
        }

        @Override
        public Die[] newArray(int size) {
            return new Die[size];
        }
    };
}
