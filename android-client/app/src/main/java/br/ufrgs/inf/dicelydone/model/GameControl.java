package br.ufrgs.inf.dicelydone.model;

import android.app.Activity;

import java.util.ArrayList;

public abstract class GameControl {

    public abstract void join(String playerName);

    public abstract void roll();

    public abstract void bet(ChipSet bet);

    public abstract void fold();

    public abstract void reroll(Hand kept);

    private ArrayList<Handler> mHandlers;
    private Activity mContext;

    public GameControl(Activity context) {
        mHandlers = new ArrayList<>();
        mContext = context;
    }

    public void addHandler(Handler h) {
        mHandlers.add(h);
    }

    public void removeHandler(Handler h) {
        mHandlers.remove(mHandlers.indexOf(h));
    }

    protected void runOnUiThread(Runnable task) {
        mContext.runOnUiThread(task);
    }

    protected void fireJoined() {
        for (Handler h: mHandlers) {
            h.onJoined();
        }
    }

    protected void fireStartGame(int bet) {
        for (Handler h: mHandlers) {
            h.onStartGame(bet);
        }
    }

    protected void fireStartRollTurn(int round) {
        for (Handler h: mHandlers) {
            h.onStartRollTurn(round);
        }
    }

    protected void fireStartBetTurn(int round, int minBet) {
        for (Handler h: mHandlers) {
            h.onStartBetTurn(round, minBet);
        }
    }

    protected void fireDiceRolled(String player, Hand rolled) {
        for (Handler h: mHandlers) {
            h.onDiceRolled(player, rolled);
        }
    }

    protected void fireBetPlaced(String player, int totalBet, int individualBet) {
        for (Handler h: mHandlers) {
            h.onBetPlaced(player, totalBet, individualBet);
        }
    }

    protected void fireGameEnded(String winner, int valueWon) {
        for (Handler h: mHandlers) {
            h.onGameEnded(winner, valueWon);
        }
    }

    protected void fireFolded(String player) {
        for (Handler h: mHandlers) {
            h.onFolded(player);
        }
    }

    protected void fireDisconnected() {
        for (Handler h : mHandlers) {
            h.onDisconnected();
        }
    }


    public interface Handler {

        void onJoined();

        void onStartGame(int bet);

        void onStartRollTurn(int turn);

        void onStartBetTurn(int turn, int minBet);

        void onDiceRolled(String player, Hand rolled);

        void onBetPlaced(String player, int totalBet, int individualBet);

        void onFolded(String player);

        void onGameEnded(String winner, int prize);

        void onDisconnected();

    }

}
