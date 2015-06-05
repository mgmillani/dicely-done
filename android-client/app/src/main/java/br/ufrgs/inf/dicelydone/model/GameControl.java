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

    protected void runOnUiThread(Runnable task) {
        mContext.runOnUiThread(task);
    }

    protected void fireJoined(int playerNum) {
        for (Handler h: mHandlers) {
            h.onJoined(playerNum);
        }
    }

    protected void fireStartGame() {
        for (Handler h: mHandlers) {
            h.onStartGame();
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

    protected void fireDiceRolled(int player, Hand rolled) {
        for (Handler h: mHandlers) {
            h.onDiceRolled(player, rolled);
        }
    }

    protected void fireBetPlaced(int player, int totalBet) {
        for (Handler h: mHandlers) {
            h.onBetPlaced(player, totalBet);
        }
    }

    protected void fireGameEnded(int winner, int valueWon) {
        for (Handler h: mHandlers) {
            h.onGameEnded(winner, valueWon);
        }
    }


    public interface Handler {

        void onJoined(int playerNum);

        void onStartGame();

        void onStartRollTurn(int turn);

        void onStartBetTurn(int turn, int minBet);

        void onDiceRolled(int player, Hand rolled);

        void onBetPlaced(int player, int totalBet);

        void onGameEnded(int winner, int valueWon);

    }

}
