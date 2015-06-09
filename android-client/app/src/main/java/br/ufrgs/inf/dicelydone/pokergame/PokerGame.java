package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import br.ufrgs.inf.dicelydone.R;
import br.ufrgs.inf.dicelydone.model.ChipSet;
import br.ufrgs.inf.dicelydone.model.GameControl;
import br.ufrgs.inf.dicelydone.model.GameSimulation;
import br.ufrgs.inf.dicelydone.model.Hand;


public class PokerGame extends AppCompatActivity
    implements GameControl.Handler, RollingRound.EventHandler, BettingRound.EventHandler, ChipInfoFragment.EventHandler {

    /**
     * This argument must be an instance of {@link Hand} containing the player's dice.
     */
    public static final String ARG_HAND = "br.ufrgs.inf.dicelydone.HAND";

    private static final String TAG = "PokerGame";


    private String mPlayer = "Baz";
    private int mRound = 0;
    private boolean mFolded = false;

    private GameSimulation mGameCtrl;

    private ChipInfoFragment mChipInfo;
    private HandInfoFragment mHandInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poker_game);

        mGameCtrl = new GameSimulation(this);
        mGameCtrl.addHandler(this);

        mChipInfo = new ChipInfoFragment();
        mChipInfo.setPlayer(mPlayer);
        mChipInfo.setGameControl(mGameCtrl);

        mHandInfo = new HandInfoFragment();
        mHandInfo.setPlayer(mPlayer);
        mHandInfo.setGameControl(mGameCtrl);

        if (savedInstanceState == null) {

            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new WaitingScreen())
                    .add(R.id.chipinfo_container, mChipInfo)
                    .add(R.id.handinfo_container, mHandInfo)
                    .hide(mHandInfo)
                    .commit();

        }

        mChipInfo.initGame();
        mGameCtrl.join(mPlayer);
    }

    @Override
    public void onJoined() {
        Toast.makeText(this, R.string.toast_joined_server, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStartGame() {
        mFolded = false;

        Toast.makeText(this, "Game started.", Toast.LENGTH_LONG).show();

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new WaitingScreen())
                .hide(mHandInfo)
                .show(mChipInfo)
                .commit();
    }

    @Override
    public void onStartRollTurn(int turn) {
        if (mFolded) return;

        mRound = turn;

        Fragment fragment = new RollingRound();

        Bundle args = new Bundle();
        args.putInt(RollingRound.ARG_ROUND, turn);
        fragment.setArguments(args);

        getFragmentManager().beginTransaction()
                .hide(mChipInfo)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void onStartBetTurn(int turn, int minBet) {
        if (mFolded) return;

        mRound = turn;

        Fragment roundFragment = new BettingRound();

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, roundFragment, "BettingRound")
                .show(mChipInfo)
                .commit();

        mChipInfo.setReadOnly(turn != 2);
    }

    @Override
    public void onDiceRolled(String player, Hand hand) {
        if (!player.equals(mPlayer)) return;

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new WaitingScreen())
                .show(mChipInfo)
                .show(mHandInfo)
                .commit();
    }

    @Override
    public void onFolded(String player) {
        if (!player.equals(mPlayer)) {
            Toast.makeText(this, player + " folded.", Toast.LENGTH_SHORT).show();

        } else {
            mFolded = true;

            getFragmentManager().beginTransaction()
                    .hide(mChipInfo)
                    .hide(mHandInfo)
                    .replace(R.id.fragment_container, new WaitingScreen())
                    .commit();
        }
    }

    @Override
    public void onBetPlaced(String player, int totalBet, int individualBet) {
        if (!player.equals(mPlayer)) {
            Toast.makeText(this, player + " placed a bet.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGameEnded(String winner, int valueWon) {
        if (winner.equals(mPlayer)) {
            Toast.makeText(this, "You won!", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, "You lost...", Toast.LENGTH_LONG).show();
        }

        getFragmentManager().beginTransaction()
                .hide(mHandInfo)
                .commit();
    }

    @Override
    public void rollDice() {
        if (mRound == 1) {
            mGameCtrl.roll();
        } else if (mRound == 4) {
            mGameCtrl.reroll(new Hand()); // TODO actually choose the dice
        }
    }

    @Override
    public void onBetPlaced() {
        mGameCtrl.bet(mChipInfo.getPlayerBet());

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new WaitingScreen())
                .commit();
    }

    @Override
    public void onFolded() {
        mGameCtrl.fold();
    }

    @Override
    public void onBetAndStashChanged(ChipSet stash, ChipSet bet) {
        BettingRound fragment;
        try {
            fragment = (BettingRound) getFragmentManager().findFragmentById(R.id.fragment_container);
        } catch (ClassCastException e) {
            fragment = null;
        }

        if (fragment != null) {
            fragment.setBetEnabled(bet.getValue() >= mChipInfo.getIndividualBet());
        }
    }
}
