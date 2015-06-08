package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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

    /**
     * This argument must contain the player's chips.
     */
    public static final String ARG_CHIPS = "br.ufrgs.inf.dicelydone.CHIPS";

    /**
     * This argument must contain the player's current bet.
     */
    public static final String ARG_PLAYER_BET = "br.ufrgs.inf.dicelydone.PLAYER_BET";

    /**
     * This argument must contain the added bet of all players.
     */
    public static final String ARG_TOTAL_BET = "br.ufrgs.inf.dicelydone.TOTAL_BET";

    /**
     * This argument must contain the minimum individual bet to continue playing.
     */
    public static final String ARG_INDIVIDUAL_BET = "br.ufrgs.inf.dicelydone.INDIVIDUAL_BET";

    private static final String TAG = "PokerGame";


    private int mPlayerNum;
    private int mTotalBet;
    private int mIndividualBet;
    private Hand mHand;
    private ChipSet mPlayerBet;
    private ChipSet mPlayerChips;

    private Random mRand;
    private Timer mT;

    private MockGame mGameCtrl;

    private ChipInfoFragment mChipInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poker_game);

        mRand = new Random(System.currentTimeMillis());
        mT = new Timer();
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

            Bundle args = assembleParams();

            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, openWaitingScreen())
                    .add(R.id.chipinfo_container, mChipInfo)
                    .commit();

        }

        mGameCtrl.join("Foo");
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
        mTotalBet = 0;
        mIndividualBet = 0;
        mPlayerBet = new ChipSet();
        mPlayerChips = new ChipSet();

        for (Chip c : Chip.values()) {
            mPlayerChips.addChips(c, 10);
        }
    }

    private void delayed(long delay, final Runnable task) {
        mT.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(task);
            }
        }, delay);
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

    private Fragment openBettingRound(boolean canRaise) {
        BettingRound fragment = new BettingRound();
        Bundle args = assembleParams();
        fragment.setArguments(args);

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
        args.putInt(ARG_TOTAL_BET, mTotalBet);
        args.putInt(ARG_INDIVIDUAL_BET, mIndividualBet);
        args.putParcelable(ARG_PLAYER_BET, mPlayerBet);
        args.putParcelable(ARG_CHIPS, mPlayerChips);
        return args;
    }

    @Override
    public void onJoined(int playerNum) {
        mPlayerNum = playerNum;
        Toast.makeText(this, R.string.toast_joined_server, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStartGame() {
        Bundle args = assembleParams();

        getFragmentManager().beginTransaction()
                .disallowAddToBackStack()
                .replace(R.id.fragment_container, openWaitingScreen())
                //.add(R.id.pokergame_fragment_container, createChipInfo())
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
        mIndividualBet = minBet;

        for (int i=Chip.values().length-1; i >= 0; i--) {
            Chip c = Chip.values()[i];

            int taken = mPlayerChips.takeChips(c, minBet/c.getValue());
            mPlayerBet.addChips(c, taken);
            minBet -= taken * c.getValue();
        }

        Fragment roundFragment = new BettingRound();
        roundFragment.setArguments(assembleParams());

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, roundFragment, "BettingRound")
                .show(mChipInfo)
                .commit();

        mChipInfo.setReadOnly(turn != 2);
    }

    @Override
    public void onDiceRolled(int player, Hand hand) {
        if (player != mPlayerNum) return;

        mHand = hand;

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, openWaitingScreen())
                .show(mChipInfo)
                .commit();
    }

    @Override
    public void onBetPlaced(int player, int totalBet, int individualBet) {
        mTotalBet = totalBet;
        if (player != mPlayerNum) {
            replaceFragment(openWaitingScreen());
        }
    }

    @Override
    public void onGameEnded(int winner, int valueWon) {
        mPlayerBet = new ChipSet();
        mTotalBet = 0;

        if (winner != mPlayerNum) {
            Toast.makeText(this, "You lost...", Toast.LENGTH_LONG).show();
            return;
        }

        ChipSet chipsWon = new ChipSet();
        for (int i=Chip.values().length-1; i >= 0; i--) {
            Chip c = Chip.values()[i];

            chipsWon.addChips(c, valueWon/c.getValue());
            valueWon = valueWon%c.getValue();
        }

        mPlayerChips.add(chipsWon);
        Toast.makeText(this, "You won!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void rollDice() {
        mGameCtrl.roll();
    }

    @Override
    public void onBetPlaced() {
        mPlayerChips = mChipInfo.getPlayerStash();
        mPlayerBet = mChipInfo.getPlayerBet();

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

    private Fragment createChipInfo() {
        ChipInfoFragment fragment = new ChipInfoFragment();
        fragment.setGameControl(mGameCtrl);

        Bundle args =  new Bundle();
        args.putParcelable(ChipInfoFragment.ARG_STASH, mPlayerChips);
        args.putParcelable(ChipInfoFragment.ARG_PLAYER_BET, mPlayerBet);
        args.putInt(ChipInfoFragment.ARG_INDIVIDUAL_BET, mIndividualBet);
        args.putInt(ChipInfoFragment.ARG_TOTAL_BET, mTotalBet);

        fragment.setArguments(args);
        return fragment;
    }
}
