package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import br.ufrgs.inf.dicelydone.R;
import br.ufrgs.inf.dicelydone.model.Chip;
import br.ufrgs.inf.dicelydone.model.ChipSet;
import br.ufrgs.inf.dicelydone.model.GameControl;
import br.ufrgs.inf.dicelydone.model.Hand;
import br.ufrgs.inf.dicelydone.model.MockGame;


public class PokerGame extends AppCompatActivity
    implements GameControl.Handler, Round1.EventHandler, BettingRound.EventHandler, ChipInfoFragment.EventHandler {

    /**
     * This argument must be an instance of {@link Hand} containing the player's dice.
     */
    public static final String ARG_HAND = "br.ufrgs.inf.dicelydone.HAND";

    private static final String TAG = "PokerGame";


    private String mPlayer = "Baz";
    private Hand mHand;

    private MockGame mGameCtrl;

    private ChipInfoFragment mChipInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poker_game);

        mGameCtrl = new MockGame(this);
        mGameCtrl.addHandler(this);

        mChipInfo = new ChipInfoFragment();
        mChipInfo.setGameControl(mGameCtrl);

        initGame();

        if (findViewById(R.id.pokergame_fragment_container) != null) {

            // Don't create fragments when being restored to avoid overlapping fragments
            if (savedInstanceState != null) {
                return;
            }

            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, openWaitingScreen())
                    .add(R.id.chipinfo_container, mChipInfo)
                    .commit();

        }

        mChipInfo.initGame();
        mGameCtrl.join(mPlayer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_poker_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case R.id.action_restart:
            initGame();
            replaceFragment(openWaitingScreen());
            mGameCtrl.join("Foo");
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }

    }

    private void initGame() {
        mHand = new Hand();
    }

    private void replaceFragment(Fragment frag) {
        getFragmentManager().beginTransaction()
                .disallowAddToBackStack()
                .replace(R.id.fragment_container, frag)
                .commit();
    }

    private Fragment openRound1() {
        Round1 fragment = new Round1();
        fragment.setArguments(null);

        return fragment;
    }

    private Fragment openWaitingScreen() {
        // Create the fragment for the waiting screen
        WaitingScreen fragment = new WaitingScreen();
        fragment.setArguments(assembleParams());

        return fragment;
    }

    private Bundle assembleParams() {
        Bundle args = new Bundle();
        args.putParcelable(ARG_HAND, mHand);
        return args;
    }

    @Override
    public void onJoined() {
        Toast.makeText(this, R.string.toast_joined_server, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStartGame() {
        getFragmentManager().beginTransaction()
                .disallowAddToBackStack()
                .replace(R.id.fragment_container, openWaitingScreen())
                .commit();
    }

    @Override
    public void onStartRollTurn(int turn) {
        FragmentTransaction t = getFragmentManager().beginTransaction();

        t.hide(mChipInfo);

        if (turn == 1) {
            t.replace(R.id.fragment_container, openRound1());
        } else {
            Toast.makeText(this, "Round 4 started", Toast.LENGTH_LONG).show();
        }

        t.commit();
    }

    @Override
    public void onStartBetTurn(int turn, int minBet) {
        Fragment roundFragment = new BettingRound();
        roundFragment.setArguments(assembleParams());

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, roundFragment, "BettingRound")
                .show(mChipInfo)
                .commit();

        mChipInfo.setReadOnly(turn != 2);
    }

    @Override
    public void onDiceRolled(String player, Hand hand) {
        if (!player.equals(mPlayer)) return;

        mHand = hand;

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, openWaitingScreen())
                .show(mChipInfo)
                .commit();
    }

    @Override
    public void onFolded(String player) {
        if (!player.equals(mPlayer)) {
            Toast.makeText(this, player + " folded.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBetPlaced(String player, int totalBet, int individualBet) {
        if (!player.equals(mPlayer)) {
            replaceFragment(openWaitingScreen());
            Toast.makeText(this, "Player " + player + " placed a bet.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGameEnded(String winner, int valueWon) {
        if (!winner.equals(mPlayer)) {
            Toast.makeText(this, "You lost...", Toast.LENGTH_LONG).show();
            return;
        }

        ChipSet chipsWon = new ChipSet();
        for (int i=Chip.values().length-1; i >= 0; i--) {
            Chip c = Chip.values()[i];

            chipsWon.addChips(c, valueWon/c.getValue());
            valueWon = valueWon%c.getValue();
        }
        Toast.makeText(this, "You won!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void rollDice() {
        mGameCtrl.roll();
    }

    @Override
    public void onBetPlaced() {
        mGameCtrl.bet(mChipInfo.getPlayerBet());
        replaceFragment(openWaitingScreen());
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
