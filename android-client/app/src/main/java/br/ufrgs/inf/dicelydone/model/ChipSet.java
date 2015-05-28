package br.ufrgs.inf.dicelydone.model;

/**
 * A set of betting chips.
 */
public class ChipSet {

    private int[] mNumChips;
    private int mVal;

    /**
     * Creates an empty set of chips.
     */
    public ChipSet() {
        mNumChips = new int[Chip.values().length];
        for (Chip c : Chip.values()) {
            mNumChips[c.getIndex()] = 0;
        }
        recalculateValue();
    }

    public ChipSet(ChipSet set) {
        mNumChips = set.mNumChips.clone();
        mVal = set.mVal;
    }

    /**
     * Obtain the total value of the chips in this set.
     */
    public int getValue() { return mVal; }

    public int getChips(Chip type) {
        return mNumChips[type.getIndex()];
    }

    public void addChips(Chip type, int x) {
        mNumChips[type.getIndex()] += x;
    }

    public void addChips5(int x) {
        mNumChips[1] += x;
    }

    public void addChips10(int x) {
        mNumChips[2] += x;
    }

    private void recalculateValue() {
        mVal = 0;

        for (Chip c : Chip.values()) {
            mVal += mNumChips[c.getIndex()] * c.getValue();
        }
    }

}
