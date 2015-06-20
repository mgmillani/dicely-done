package br.ufrgs.inf.dicelydone.model;

import android.app.Activity;

import java.util.ArrayList;

public abstract class GameControl {

    public interface Handler {
        void handleMessage(InMessage message);
    }

    public static abstract class InMessage {
        public enum Type { JOINED, STARTGAME, STARTTURN, DICE, BETPLACED, FOLDED, ENDGAME, DISCONNECTED, CLOSE}

        public abstract Type getType();
    }

    public static class JoinedMessage extends InMessage {
        @Override
        public Type getType() {
            return Type.JOINED;
        }
    }

    public static class StartGameMessage extends InMessage {
        private int initialBet;

        public StartGameMessage(int initialBet) {
            this.initialBet = initialBet;
        }

        @Override
        public Type getType() {
            return Type.STARTGAME;
        }

        public int getInitialBet() {
            return initialBet;
        }
    }

    public static class StartTurnMessage extends InMessage {
        private int round;
        private int minBet;

        public StartTurnMessage(int round, int minBet) {
            this.round = round;
            this.minBet = minBet;
        }

        public StartTurnMessage(int round) {
            this.round = round;
        }

        @Override
        public Type getType() {
            return Type.STARTTURN;
        }

        public int getRound() {
            return round;
        }

        public int getMinBet() {
            return minBet;
        }
    }

    public static class DiceMessage extends InMessage {
        private String player;
        private Hand dice;

        public DiceMessage(String player, Hand dice) {
            this.player = player;
            this.dice = dice;
        }

        @Override
        public Type getType() {
            return Type.DICE;
        }

        public String getPlayer() {
            return player;
        }

        public Hand getDice() {
            return dice;
        }
    }

    public static class BetPlacedMessage extends InMessage {
        private String player;
        private int individualBet;
        private int totalBet;

        public BetPlacedMessage(String player, int individualBet, int totalBet) {
            this.player = player;
            this.individualBet = individualBet;
            this.totalBet = totalBet;
        }

        @Override
        public Type getType() {
            return Type.BETPLACED;
        }

        public String getPlayer() {
            return player;
        }

        public int getIndividualBet() {
            return individualBet;
        }

        public int getTotalBet() {
            return totalBet;
        }
    }

    public static class FoldedMessage extends InMessage {
        private String player;

        public FoldedMessage(String player) {
            this.player = player;
        }

        @Override
        public Type getType() {
            return Type.FOLDED;
        }

        public String getPlayer() {
            return player;
        }
    }

    public static class EndGameMessage extends InMessage {
        private String winner;
        private int prize;

        public EndGameMessage(String winner, int prize) {
            this.winner = winner;
            this.prize = prize;
        }

        @Override
        public Type getType() {
            return Type.ENDGAME;
        }

        public String getWinner() {
            return winner;
        }

        public int getPrize() {
            return prize;
        }
    }

    public static class DisconnectedMessage extends InMessage {
        private String player;

        public DisconnectedMessage(String player) {
            this.player = player;
        }

        @Override
        public Type getType() {
            return Type.DISCONNECTED;
        }

        public String getPlayer() {
            return player;
        }
    }

    public static class CloseMessage extends InMessage {
        @Override
        public Type getType() {
            return Type.CLOSE;
        }
    }


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

    protected void fireMessage(InMessage message) {
        for (Handler h : mHandlers) {
            h.handleMessage(message);
        }
    }


}
