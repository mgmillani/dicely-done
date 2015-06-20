package br.ufrgs.inf.dicelydone.model;

import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class Game {

    private static final String TAG = "dicelydone.Game";

    public enum Round {
        INITIAL, CAST, BET, MATCH, RECAST, RESULT;

        public Round next() {
            switch (this) {
                case INITIAL:
                    return CAST;
                case CAST:
                    return BET;
                case BET:
                    return MATCH;
                case MATCH:
                    return RECAST;
                case RECAST:
                    return RESULT;
                case RESULT:
                    return INITIAL;
            }

            throw new RuntimeException("unreachable code reached");
        }
    }

    public static class Info {

        public enum Type {
            HAND, BET, NOTHING
        }

        public final Type type;
        public final String player;

        public Info(Type type, String player) {
            this.type = type;
            this.player = player;
        }

    }

    private static class PlayerData {
        public int bet;
        public Hand hand;

        public PlayerData(int bet, Hand hand) {
            this.bet = bet;
            this.hand = hand;
        }
    }

    private LinkedList<String> mPlayers = new LinkedList<>();
    private HashMap<String, PlayerData> mData = new HashMap<>();

    private Round mRound = Round.RESULT;
    private int mTotalBet;
    private int mIndividualBet;
    private String mWinner;

    private LinkedList<String> mActivePlayers;
    private Iterator<String> mPlayerIt;

    private Info mNeeded = new Info(Info.Type.NOTHING, null);

    private Random mRand = new Random(System.currentTimeMillis());


    public void join(String player) {
        mPlayers.add(player);
        Log.v(TAG, player + " joined");
    }

    public void giveHand(Hand hand) {
        if (mRound != Round.CAST && mRound !=  Round.RECAST)
            throw new RuntimeException("Wrong round.");

        Log.v(TAG, mNeeded.player + " rolled " + hand);

        mData.get(mNeeded.player).hand = hand;
    }

    public void giveBet(int bet) {
        if (mRound != Round.BET && mRound != Round.MATCH)
            throw new RuntimeException("Wrong round.");

        Log.v(TAG, mNeeded.player + " placed bet of " + bet);

        PlayerData data = mData.get(mNeeded.player);
        int added = bet - data.bet;

        data.bet = bet;
        mTotalBet += added;
        mIndividualBet = bet;
    }

    public void fold() {
        if (mRound != Round.BET && mRound != Round.MATCH)
            throw new RuntimeException("Wrong round.");

        Log.v(TAG, mNeeded.player + " folded");

        mPlayerIt.remove();
    }

    public void nextTurn() {
        if (mPlayers.isEmpty()) {
            return;
        }

        if (mActivePlayers != null && mActivePlayers.size() == 1) {
            // No one left playing, go straight to result round
            mRound = Round.RECAST;
            nextRound();

        } else if (mPlayerIt == null || !mPlayerIt.hasNext()) {
            // All players acted this round, proceed to the next
            nextRound();
        }

        nextPlayer();
    }

    private void nextPlayer() {
        switch (mRound) {
        case INITIAL:
            mNeeded = new Info(Info.Type.NOTHING, null);
            break;

        case CAST:
            mNeeded = new Info(Info.Type.HAND, mPlayerIt.next());
            break;

        case BET:
            mNeeded = new Info(Info.Type.BET, mPlayerIt.next());
            break;

        case MATCH:
            mNeeded = new Info(Info.Type.BET, mPlayerIt.next());
            break;

        case RECAST:
            mNeeded = new Info(Info.Type.HAND, mPlayerIt.next());
            break;

        case RESULT:
            mNeeded = new Info(Info.Type.NOTHING, null);
            break;
        }

    }

    private void nextRound() {
        mRound = mRound.next();
        Log.v(TAG, "Starting round: " + mRound);

        switch (mRound) {
            case INITIAL:
                mPlayerIt = null;
                mTotalBet = 0;
                mIndividualBet = 0;
                mWinner = null;
                break;

            case CAST:
                mActivePlayers = new LinkedList<>(mPlayers);
                mPlayerIt = mActivePlayers.iterator();

                mData.clear();
                for (String player : mPlayers) {
                    mData.put(player, new PlayerData(0, new Hand()));
                }
                break;

            case RESULT:
                int idx = mRand.nextInt(mActivePlayers.size());
                mWinner = mActivePlayers.get(idx);

                mPlayerIt = null;
                mActivePlayers = null;
                break;

            default:
                mPlayerIt = mActivePlayers.iterator();
        }
    }

    public Info getNeeded() {
        return mNeeded;
    }

    public int getTotalBet() {
        return mTotalBet;
    }

    public int getIndividualBet() {
        return mIndividualBet;
    }

    public Round getRound() {
        return mRound;
    }

    public String getWinner() {
        return mWinner;
    }

}
