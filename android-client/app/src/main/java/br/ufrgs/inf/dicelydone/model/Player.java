package br.ufrgs.inf.dicelydone.model;

public class Player {

    private String mName;
    private int mBet;
    private Hand mHand;

    public Player(String name) {
        mName = name;
        mBet = 0;
        mHand = new Hand();
    }

    public String getName() {
        return mName;
    }

    public int getBet() {
        return mBet;
    }

    public Hand getHand() {
        return mHand;
    }

    public void setBet(int mBet) {
        this.mBet = mBet;
    }

    public void setHand(Hand mHand) {
        this.mHand = mHand;
    }

}
