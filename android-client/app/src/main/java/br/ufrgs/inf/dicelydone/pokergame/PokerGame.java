package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import br.ufrgs.inf.dicelydone.R;
import br.ufrgs.inf.dicelydone.model.Chip;
import br.ufrgs.inf.dicelydone.model.ChipSet;
import br.ufrgs.inf.dicelydone.model.Hand;


public class PokerGame extends AppCompatActivity
    implements Round1.EventHandler, Round2.EventHandler {
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


    private int mTotalBet;
    private Hand mHand;
    private ChipSet mPlayerBet;
    private ChipSet mPlayerChips;

    private Random mRand;
    private Timer mT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poker_game);

        mRand = new Random(System.currentTimeMillis());
        mT = new Timer();
        initGame();

        if (findViewById(R.id.pokergame_fragment_container) != null) {

            // Don't create fragments when being restored to avoid overlapping fragments
            if (savedInstanceState != null) {
                return;
            }

            getFragmentManager().beginTransaction()
                    .add(R.id.pokergame_fragment_container, openWaitingScreen()).commit();

        }
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
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onDiceRolled(Hand hand) {
        mHand = hand;
        replaceFragment(openWaitingScreen());

        // Wait three seconds then open round 2
        delayed(3000, new Runnable() {
            @Override
            public void run() {
                replaceFragment(openRound2());
            }
        });
    }

    @Override
    public void onBetPlaced(ChipSet playerStash, ChipSet playerBet) {
        mPlayerChips = playerStash;

        mPlayerBet = playerBet;
        mTotalBet += playerBet.getValue();

        replaceFragment(openWaitingScreen());
    }

    @Override
    public void onFolded() {
        mHand = new Hand();

        replaceFragment(openWaitingScreen());
    }

    private void initGame() {
        mHand = new Hand();
        mTotalBet = 0;
        mPlayerBet = new ChipSet();
        mPlayerChips = new ChipSet();

        for (Chip c : Chip.values()) {
            mPlayerChips.addChips(c, 10);
        }

        // Wait for three seconds, then open round 1
        delayed(3000, new Runnable() {
            @Override
            public void run() {
                replaceFragment(openRound1());
            }
        });
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
                .replace(R.id.pokergame_fragment_container, frag)
                .commit();
    }

    private Fragment openRound1() {
        // Create the fragment for round 1
        Round1 fragment = new Round1();
        fragment.setArguments(null);

        return fragment;
    }

    private Fragment openRound2() {
        // Create the fragment for round 2
        Round2 fragment = new Round2();
        fragment.setArguments(assembleParams());

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
        args.putParcelable(WaitingScreen.ARG_HAND, mHand);
        args.putInt(WaitingScreen.ARG_TOTAL_BET, mTotalBet);
        args.putParcelable(WaitingScreen.ARG_PLAYER_BET, mPlayerBet);
        args.putParcelable(WaitingScreen.ARG_CHIPS, mPlayerChips);
        return args;
    }

}
