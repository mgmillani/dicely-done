package br.ufrgs.inf.dicelydone.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains the set of (at most five) dice that a player has.
 *
 * The dice are stored sorted by value.
 */
public class Hand implements Parcelable {
    private List<Die> dice;

    /**
     * Creates a hand with the given (at most 5) dice.
     *
     * @throws IllegalArgumentException when given more than 5 dice.
     */
    public Hand(Die... dice) {
        this(Arrays.asList(dice));
    }

    /**
     * Creates a hand with the given (at most 5) dice.
     *
     * @throws IllegalArgumentException when given more than 5 dice.
     */
    public Hand(@NonNull List<Die> dice) {
        if (dice.size() > 5) {
            throw new IllegalArgumentException("Cannot create hand with more than 5 dice.");
        }
        this.dice = new LinkedList<>(dice);
        Collections.sort(this.dice);
    }

    /** Number of dice present in the hand. */
    public int size() {
        return dice.size();
    }

    public Die get(int i) {
        return dice.get(i);
    }

    public void clear() {
        dice.clear();
    }

    public void add(Die die) {
        if (dice.size() >= 5) {
            throw new IllegalStateException("Cannot add a sixth die to the hand.");
        }
        dice.add(die);
        Collections.sort(dice);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(dice);
    }

    public static final Parcelable.Creator<Hand> CREATE = new Parcelable.Creator<Hand>() {
        @Override
        public Hand createFromParcel(Parcel in) {
            return new Hand(in.createTypedArrayList(Die.CREATE));
        }

        @Override
        public Hand[] newArray(int size) {
            return new Hand[size];
        }
    };
}
