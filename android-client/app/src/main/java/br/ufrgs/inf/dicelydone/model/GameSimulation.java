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
        fireMessage(new DiceMessage(mActivePlayer, hand));

        resumeSimulation();
    }

    @Override
    public void bet(ChipSet bet) {
        mGame.giveBet(bet.getValue());
        fireMessage(new BetPlacedMessage(mActivePlayer, mGame.getIndividualBet(), mGame.getTotalBet()));

        resumeSimulation();
    }

    @Override
    public void fold() {
        mGame.fold();
        fireMessage(new FoldedMessage(mActivePlayer));

        resumeSimulation();
    }

    @Override
    public void reroll(Hand kept) {
        randomizeDice(kept);

        mGame.giveHand(kept);
        fireMessage(new DiceMessage(mActivePlayer, kept));

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
                    fireMessage(new EndGameMessage(mGame.getWinner(), mGame.getTotalBet()));

                } else if (mGame.getRound() == Game.Round.INITIAL) {
                    fireMessage(new StartGameMessage(1));
                }
                break;

            case HAND:
                Hand hand = Hand.random(5, mRand);
                mGame.giveHand(hand);
                fireMessage(new DiceMessage(needed.player, hand));
                break;

            case BET:
                if (mRand.nextInt(5) < 1) {
                    mGame.fold();
                    fireMessage(new FoldedMessage(needed.player));

                } else {
                    int bet = mGame.getIndividualBet();

                    if (mGame.getRound() == Game.Round.BET) {
                        int other = mRand.nextInt(MAX_BET);
                        if (bet < other) {
                            bet = other;
                        }
                    }

                    mGame.giveBet(bet);
                    fireMessage(new BetPlacedMessage(needed.player, mGame.getIndividualBet(), mGame.getTotalBet()));
                }
                break;
        }

        resumeSimulation();
    }

    private void startTurn() {
        switch (mGame.getRound()) {
            case CAST:
                fireMessage(new StartTurnMessage(1));
                break;
            case BET:
                fireMessage(new StartTurnMessage(2, mGame.getIndividualBet()));
                break;
            case MATCH:
                fireMessage(new StartTurnMessage(3, mGame.getIndividualBet()));
                break;
            case RECAST:
                fireMessage(new StartTurnMessage(4));
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
