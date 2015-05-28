package br.ufrgs.inf.dicelydone.model;

/**
 * Possible values for betting chips.
 */
public enum Chip {
    ONE(0, 1), FIVE(1, 5), TEN(2, 10);

    private final int index;
    private final int value;

    private Chip(int idx, int val) {
        this.index = idx;
        this.value = val;
    }

    public int getValue() { return value; }

    /** Gets the index for this chip in {@link Chip#values()}. */
    public int getIndex() { return index; }

}
