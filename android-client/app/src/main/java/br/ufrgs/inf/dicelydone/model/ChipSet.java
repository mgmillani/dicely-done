package br.ufrgs.inf.dicelydone.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * A set of betting chips.
 */
public class ChipSet implements Parcelable {

    private int[] mNumChips;
    private int mVal;

    /**
     * Creates an empty set of chips.
     */
    public ChipSet() {
        mNumChips = new int[Chip.values().length];
        clear();
        recalculateValue();
    }

    public ChipSet(ChipSet set) {
        mNumChips = set.mNumChips.clone();
        mVal = set.mVal;
    }

    public void clear() {
        for (Chip c : Chip.values()) {
            mNumChips[c.getIndex()] = 0;
        }
        recalculateValue();
    }

    /**
     * Obtain the total value of the chips in this set.
     */
    public int getValue() { return mVal; }

    public int getChips(Chip type) {
        return mNumChips[type.getIndex()];
    }

    /** Adds the given chips to this set. */
    public void add(@NonNull ChipSet chips) {
        for (Chip c : Chip.values()) {
            addChips(c, chips.getChips(c));
        }
    }

    /**
     * Adds the given number of chips of the given type.
     *
     * <p>
     * If given negative values, may leave a negative number
     * of chips on the set.
     */
    public void addChips(@NonNull Chip type, int x) {
        mNumChips[type.getIndex()] += x;
        mVal += type.getValue() * x;
    }

    /**
     * Removes the given number of chips of the given type, or
     * as many as possible.
     *
     * <p>
     * Ensures no more chips are taken as one already has, that
     * is, the resulting set has no negative number of chips.
     *
     * @return The number of chips actually taken.
     */
    public int takeChips(@NonNull Chip type, int desired) {
        int total = mNumChips[type.getIndex()];
        int taken = (total < desired)? total : desired;

        mNumChips[type.getIndex()] -= taken;
        mVal -= type.getValue() * taken;

        return taken;
    }

    public void add(int value) {
        ChipSet source = new ChipSet();
        for (Chip c : Chip.values()) {
            source.addChips(c, 10);
        }

        while (value > 0) {
            ChipSet set = new ChipSet(source);
            value -= set.giveUpTo(value, this);
        }
    }

    /**
     * @return Value actually transfered.
     */
    public int giveUpTo(int value, ChipSet receiver) {
        int toAdd = value;

        for (int i=Chip.values().length-1; i >= 0; i--) {
            if (toAdd <= 0) break;

            Chip c = Chip.values()[i];

            int taken = takeChips(c, toAdd/c.getValue());
            receiver.addChips(c, taken);
            toAdd -= taken * c.getValue();
        }

        return value - toAdd;
    }

    private void recalculateValue() {
        mVal = 0;

        for (Chip c : Chip.values()) {
            mVal += mNumChips[c.getIndex()] * c.getValue();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mVal);
        dest.writeIntArray(mNumChips);
    }

    public static final Parcelable.Creator<ChipSet> CREATOR = new Parcelable.Creator<ChipSet>() {
        @Override
        public ChipSet createFromParcel(Parcel source) {
            ChipSet result = new ChipSet();
            result.mVal = source.readInt();
            result.mNumChips = source.createIntArray();
            return result;
        }

        @Override
        public ChipSet[] newArray(int size) {
            return new ChipSet[size];
        }
    };
}
