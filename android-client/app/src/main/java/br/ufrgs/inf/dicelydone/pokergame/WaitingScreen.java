package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Random;

import br.ufrgs.inf.dicelydone.HandView;
import br.ufrgs.inf.dicelydone.R;
import br.ufrgs.inf.dicelydone.model.Die;
import br.ufrgs.inf.dicelydone.model.Hand;

/**
 * Fragment for the waiting phase of the poker game.
 *
 * Displays the player's dice, chips and bet, as well
 * as the overall bet.
 *
 * TODO display the chips
 * TODO display the bets
 *
 * TODO receive actual data
 */
public class WaitingScreen extends Fragment {

    public static final String ARG_HAND = PokerGame.ARG_HAND;
    public static final String ARG_CHIPS = PokerGame.ARG_CHIPS;
    public static final String ARG_PLAYER_BET = PokerGame.ARG_PLAYER_BET;
    public static final String ARG_OVERALL_BET = PokerGame.ARG_OVERALL_BET;

    private Hand mHand;
    private Random mRand;
    private HandView mHandView;

    public WaitingScreen() {
        // Required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHand = new Hand(Die.FOUR, Die.TWO, Die.TWO, Die.SIX, Die.FIVE);
        if (mHandView != null) mHandView.setHand(mHand);

        mRand = new Random(System.currentTimeMillis());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_waiting_screen, container, false);

        mHandView = (HandView) result.findViewById(R.id.handView);
        mHandView.setHand(this.mHand);


        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        randomizeHand();
        mHandView.onHandChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void randomizeHand() {
        synchronized (mHand) {
            mHand.clear();

            int numDice = 5;//mRand.nextInt(6);
            for (int i = 0; i < numDice; i++) {
                mHand.add(Die.byVal(mRand.nextInt(6) + 1));
            }
        }
    }


}
