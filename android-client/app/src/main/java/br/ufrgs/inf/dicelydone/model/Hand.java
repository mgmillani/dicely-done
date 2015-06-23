package br.ufrgs.inf.dicelydone.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Contains the set of (at most five) dice that a player has.
 *
 * The dice are stored sorted by value.
 */
public class Hand implements Parcelable, Iterable<Die> {
    private List<Die> dice;

    /**
     * Creates a hand with the given dice (at most 5).
     *
     * @throws IllegalArgumentException when given more than 5 dice.
     */
    public Hand(Die... dice) {
        this(Arrays.asList(dice));
    }

    /**
     * Creates a hand with the given dice (at most 5).
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

    public Hand(Hand hand) {
        this(hand.dice);
    }

    /**
     * Creates a random hand with the given number of dice (at most 5) .
     *
     * @throws IllegalArgumentException when given a size larger than 5
     */
    public static Hand random(int size, Random generator) {
        if (size > 5) {
            throw new IllegalArgumentException("Cannot create hand with more than 5 dice.");
        }

        Hand created = new Hand();
        for (int i=0; i<size; i++) {
            created.add(Die.random(generator));
        }
        return created;
    }

    /** Creates a random hand with a random number of dice. */
    public static Hand random(Random generator) {
        return random(generator.nextInt(6), generator);
    }

    /** Number of dice present in the hand. */
    public int size() {
        return dice.size();
    }

    public boolean isEmpty() { return dice.isEmpty(); }

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

    public void remove(Die die) {
        dice.remove(die);
    }

    @Override
    public Iterator<Die> iterator() {
        return dice.iterator();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(dice);
    }

    public static final Parcelable.Creator<Hand> CREATOR = new Parcelable.Creator<Hand>() {
        @Override
        public Hand createFromParcel(Parcel in) {
            return new Hand(in.createTypedArrayList(Die.CREATOR));
        }

        @Override
        public Hand[] newArray(int size) {
            return new Hand[size];
        }
    };

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        boolean first = true;
        for (Die d : dice) {
            if (first) {
                first = false;
            } else {
                b.append(' ');
            }

            b.append(d.getValue());
        }

        return b.toString();
    }
}
