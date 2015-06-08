package br.ufrgs.inf.dicelydone.model;

import android.app.Activity;
import android.util.Log;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MockGame extends GameControl {

    private static final String TAG = "MockGame";

    public static int NUM_PLAYERS = 6;

    private boolean mJoined = false;
    private boolean mGameStarted = false;

    private int mPlayerOrder;
    private int mCurrentPlayer;
    private int mRound = 0;

    private ChipSet mSingleBet = new ChipSet();
    private ChipSet mTotalBet = new ChipSet();

    private Random mRand;
    private Timer mT;

    public MockGame(Activity context) {
        super(context);

        mRand = new Random(System.currentTimeMillis());
        mT = new Timer();
    }

    @Override
    public void join(String playerName) {

        mPlayerOrder = mRand.nextInt(NUM_PLAYERS);
        mJoined = true;
        fireJoined(mPlayerOrder);
        Log.i(TAG, "Joined the game as player " + mPlayerOrder);

        restart();
    }

    @Override
    public void roll() {
        final Hand hand = new Hand();
        randomizeDice(hand);
        Log.i(TAG, "Player " + mPlayerOrder + " rolled " + hand);

        delayed(3000, new Runnable() {
            @Override
            public void run() {
                fireDiceRolled(mPlayerOrder, hand);

                mRound = 2;
                mCurrentPlayer = 0;
                simulateRound2();
            }
        });
    }

    @Override
    public void bet(ChipSet bet) {
        mSingleBet = new ChipSet(bet);
        mTotalBet.add(bet);

        fireBetPlaced(mCurrentPlayer, mTotalBet.getValue(), mSingleBet.getValue());
        Log.i(TAG, "Player " + mCurrentPlayer + " placed bet of " + bet.getValue());

        if (mRound == 2) {
            mCurrentPlayer++;
            simulateRound2();

        } else if (mRound == 3) {
            delayed(3000, new Runnable() {
                @Override
                public void run() {
                    mRound = 4;
                    mCurrentPlayer = mPlayerOrder;
                    fireStartRollTurn(4);
                }
            });

        }
    }

    @Override
    public void fold() {
        Log.i(TAG, "Player " + mPlayerOrder + " folded.");
        restart();
    }

    @Override
    public void reroll(final Hand kept) {
        randomizeDice(kept);
        Log.i(TAG, "Player " + mPlayerOrder + " rolled " + kept);

        delayed(1500, new Runnable() {
            @Override
            public void run() {
                fireDiceRolled(mPlayerOrder, kept);
                mRound = 5;
                simulateConclusion();
            }
        });
    }

    private void randomizeDice(final Hand hand) {
        while (hand.size() < 5) {
            hand.add(Die.random(mRand));
        }
    }

    private void simulateRound2() {
        if (mCurrentPlayer == mPlayerOrder) {
            fireStartBetTurn(2, mSingleBet.getValue());

        } else if (mCurrentPlayer < NUM_PLAYERS) {
            for (Chip c : Chip.values()) {
                mSingleBet.addChips(c, mRand.nextInt(3));
            }

            mTotalBet.add(mSingleBet);
            fireBetPlaced(mCurrentPlayer, mTotalBet.getValue(), mSingleBet.getValue());
            Log.i(TAG, "Player " + mCurrentPlayer + " placed bet of " + mSingleBet.getValue());

            delayed(1500, new Runnable() {
                @Override
                public void run() {
                    mCurrentPlayer++;
                    simulateRound2();
                }
            });

        } else {
            mRound = 3;
            mCurrentPlayer = 0;
            simulateRound3();
        }
    }

    private void simulateRound3() {
        mCurrentPlayer = mPlayerOrder;
        fireStartBetTurn(3, mSingleBet.getValue());
    }

    private void simulateConclusion() {
        final int winner = mRand.nextInt(NUM_PLAYERS);
        Log.i(TAG, "Player " + winner + " won.");

        delayed(3000, new Runnable() {
            @Override
            public void run() {
                fireGameEnded(winner, mTotalBet.getValue());
                restart();
            }
        });
    }

    private void restart() {
        delayed(5000, new Runnable() {
            @Override
            public void run() {
                fireStartGame();
                mGameStarted = true;
                mRound = 1;

                delayed(1000, new Runnable() {
                    @Override
                    public void run() {
                        mCurrentPlayer = mPlayerOrder;
                        fireStartRollTurn(1);
                    }
                });
            }
        });
    }

    private void delayed(int milliseconds, final Runnable task) {
        mT.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(task);
            }
        }, milliseconds);
    }
}
