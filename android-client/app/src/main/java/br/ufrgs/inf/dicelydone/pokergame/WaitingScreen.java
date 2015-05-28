package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;

import br.ufrgs.inf.dicelydone.ChipSetView;
import br.ufrgs.inf.dicelydone.HandView;
import br.ufrgs.inf.dicelydone.R;
import br.ufrgs.inf.dicelydone.model.Chip;
import br.ufrgs.inf.dicelydone.model.ChipSet;
import br.ufrgs.inf.dicelydone.model.Die;
import br.ufrgs.inf.dicelydone.model.Hand;

/**
 * Fragment for the waiting phase of the poker game.
 *
 * Displays the player's dice, chips and bet, as well
 * as the overall bet.
 */
public class WaitingScreen extends Fragment {

    private static final String TAG = "WaitingScreen";

    public static final String ARG_HAND = PokerGame.ARG_HAND;
    public static final String ARG_CHIPS = PokerGame.ARG_CHIPS;
    public static final String ARG_PLAYER_BET = PokerGame.ARG_PLAYER_BET;
    public static final String ARG_TOTAL_BET = PokerGame.ARG_TOTAL_BET;

    private HandView mHandView;
    private ChipSetView mPlayerChipsView;
    private ChipSetView mPlayerBetView;
    private TextView mTotalBetView;

    private Hand mHand;
    private ChipSet mPlayerChips;
    private ChipSet mPlayerBet;
    private int mTotalBet;
    private Random mRand;

    public WaitingScreen() {
        // Required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRand = new Random(System.currentTimeMillis());

        if (savedInstanceState != null) {
            mHand = savedInstanceState.getParcelable(ARG_HAND);
            mTotalBet = savedInstanceState.getInt(ARG_TOTAL_BET);
            mPlayerBet = savedInstanceState.getParcelable(ARG_PLAYER_BET);
            mPlayerChips = savedInstanceState.getParcelable(ARG_CHIPS);
        } else {
            mHand = new Hand();
            mTotalBet = 0;
            mPlayerBet = new ChipSet();
            mPlayerChips = new ChipSet();

            for (Chip c : Chip.values()) {
                mPlayerChips.addChips(c, 10);
                Log.v(TAG, "player has " + mPlayerChips.getChips(c) + " chips of value " + c.toString());
            }

            randomizeHand();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_HAND, mHand);
        outState.putInt(ARG_TOTAL_BET, mTotalBet);
        outState.putParcelable(ARG_PLAYER_BET, mPlayerBet);
        outState.putParcelable(ARG_CHIPS, mPlayerChips);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_waiting_screen, container, false);

        mHandView = (HandView) result.findViewById(R.id.handView);
        mTotalBetView = (TextView) result.findViewById(R.id.totalBetView);
        mPlayerBetView = (ChipSetView) result.findViewById(R.id.playerBetView);
        mPlayerChipsView = (ChipSetView) result.findViewById(R.id.playerChipsView);

        updateView();

        return result;
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getArguments();
        if (args != null) {
            mHand = args.getParcelable(ARG_HAND);
            mTotalBet = args.getInt(ARG_TOTAL_BET);
            mPlayerBet = args.getParcelable(ARG_PLAYER_BET);
            mPlayerChips = args.getParcelable(ARG_CHIPS);
        }

        updateView();
    }

    private void randomizeHand() {
        mHand.clear();

        int numDice = 5;//mRand.nextInt(6);
        for (int i = 0; i < numDice; i++) {
            mHand.add(Die.byVal(mRand.nextInt(6) + 1));
        }
    }

    private void updateView() {
        Log.d(TAG, "updating view");

        if (mHandView != null) {
            mHandView.setHand(mHand);
        }

        if (mPlayerBetView != null) {
            mPlayerBetView.setChipSet(mPlayerBet);
        }

        if (mPlayerChipsView != null) {
            mPlayerChipsView.setChipSet(mPlayerChips);
            Log.v(TAG, "updating chips view");
        }

        if (mTotalBetView != null) {
            mTotalBetView.setText(Integer.toString(mTotalBet));
        }
    }


}
