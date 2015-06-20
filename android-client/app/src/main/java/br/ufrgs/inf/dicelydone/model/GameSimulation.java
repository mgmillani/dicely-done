package br.ufrgs.inf.dicelydone.model;

import android.app.Activity;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameSimulation extends GameControl {

    private static final int MAX_BET = 40;

    private Game mGame;
    private Timer mT;
    private Random mRand;

    private String mActivePlayer;

    public GameSimulation(Activity context) {
        super(context);

        mGame = new Game();
        mGame.join("Dandelion");
        mGame.join("Zoltan");

        mT = new Timer();
        mRand = new Random(System.currentTimeMillis());
    }

    @Override
    public void join(String playerName) {
        mGame.join(playerName);
        mActivePlayer = playerName;

        resumeSimulation();
    }

    @Override
    public void roll() {
        Hand hand = new Hand();
        randomizeDice(hand);

        mGame.giveHand(hand);
        fireDiceRolled(mActivePlayer, hand);

        resumeSimulation();
    }

    @Override
    public void bet(ChipSet bet) {
        mGame.giveBet(bet.getValue());
        fireBetPlaced(mActivePlayer, mGame.getTotalBet(), mGame.getIndividualBet());

        resumeSimulation();
    }

    @Override
    public void fold() {
        mGame.fold();
        fireFolded(mActivePlayer);

        resumeSimulation();
    }

    @Override
    public void reroll(Hand kept) {
        randomizeDice(kept);

        mGame.giveHand(kept);
        fireDiceRolled(mActivePlayer, kept);

        resumeSimulation();
    }

    private void simulate() {
        Game.Info needed = mGame.getNeeded();

        if (mActivePlayer.equals(needed.player)) {
            startTurn();
            return;
        }

        switch (needed.type) {
            case NOTHING:
                if (mGame.getRound() == Game.Round.RESULT) {
                    fireGameEnded(mGame.getWinner(), mGame.getTotalBet());

                } else if (mGame.getRound() == Game.Round.INITIAL) {
                    fireStartGame(1);
                }
                break;

            case HAND:
                Hand hand = Hand.random(5, mRand);
                mGame.giveHand(hand);
                fireDiceRolled(needed.player, hand);
                break;

            case BET:
                if (mRand.nextInt(5) < 1) {
                    mGame.fold();
                    fireFolded(needed.player);

                } else {
                    int bet = mGame.getIndividualBet();

                    if (mGame.getRound() == Game.Round.BET) {
                        int other = mRand.nextInt(MAX_BET);
                        if (bet < other) {
                            bet = other;
                        }
                    }

                    mGame.giveBet(bet);
                    fireBetPlaced(needed.player, mGame.getTotalBet(), mGame.getIndividualBet());
                }
                break;
        }

        resumeSimulation();
    }

    private void startTurn() {
        switch (mGame.getRound()) {
            case CAST:
                fireStartRollTurn(1);
                break;
            case BET:
                fireStartBetTurn(2, mGame.getIndividualBet());
                break;
            case MATCH:
                fireStartBetTurn(3, mGame.getIndividualBet());
                break;
            case RECAST:
                fireStartRollTurn(4);
                break;
        }
    }

    private void delayed(int milliseconds, final Runnable task) {
        mT.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(task);
            }
        }, milliseconds);
    }

    private void resumeSimulation() {
        mGame.nextTurn();

        delayed(2000, new Runnable() {
            @Override
            public void run() {
                simulate();
            }
        });
    }

    private void randomizeDice(Hand hand) {
        while (hand.size() < 5) {
            hand.add(Die.random(mRand));
        }
    }
}
