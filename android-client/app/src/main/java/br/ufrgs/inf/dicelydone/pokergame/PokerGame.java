package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Fragment;
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
    implements GameControl.Handler, Round1.EventHandler, BettingRound.EventHandler {

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

    private GameControl mGameCtrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poker_game);

        mRand = new Random(System.currentTimeMillis());
        mT = new Timer();
        mGameCtrl = new MockGame(this);
        mGameCtrl.addHandler(this);

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

    private void initGame() {
        mHand = new Hand();
        mTotalBet = 0;
        mIndividualBet = 0;
        mPlayerBet = new ChipSet();
        mPlayerChips = new ChipSet();

        for (Chip c : Chip.values()) {
            mPlayerChips.addChips(c, 10);
        }

        mGameCtrl.join("Foo");
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
        Round1 fragment = new Round1();
        fragment.setArguments(null);

        return fragment;
    }

    private Fragment openBettingRound(boolean canRaise) {
        BettingRound fragment = new BettingRound();

        Bundle args = assembleParams();
        args.putBoolean(BettingRound.ARG_CAN_RAISE, canRaise);

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
        replaceFragment(openWaitingScreen());
    }

    @Override
    public void onStartRollTurn(int turn) {
        if (turn == 1) {
            replaceFragment(openRound1());
        } else {
            Toast.makeText(this, "Round 4 started", Toast.LENGTH_LONG).show();
        }
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

        replaceFragment(openBettingRound(turn == 2));
    }

    @Override
    public void onDiceRolled(int player, Hand hand) {
        if (player != mPlayerNum) return;

        mHand = hand;
        replaceFragment(openWaitingScreen());
    }

    @Override
    public void onBetPlaced(int player, int totalBet) {
        mTotalBet = totalBet;
        replaceFragment(openWaitingScreen());
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
        replaceFragment(openWaitingScreen());
    }

    @Override
    public void onBetPlaced(ChipSet playerStash, ChipSet playerBet) {
        mPlayerChips = playerStash;
        mPlayerBet = playerBet;

        mGameCtrl.bet(playerBet);
        replaceFragment(openWaitingScreen());
    }

    @Override
    public void onFolded() {
        mGameCtrl.fold();
    }
}
