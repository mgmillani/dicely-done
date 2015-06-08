package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.ufrgs.inf.dicelydone.HandView;
import br.ufrgs.inf.dicelydone.R;
import br.ufrgs.inf.dicelydone.model.Hand;

/**
 * Fragment for the waiting phase of the poker game.
 *
 * <p>
 * Displays the player's dice, chips and bet, as well
 * as the overall bet.
 */
public class WaitingScreen extends Fragment {

    private static final String TAG = "WaitingScreen";

    public static final String ARG_HAND = PokerGame.ARG_HAND;

    private HandView mHandView;
    private Hand mHand;

    public WaitingScreen() {
        // Required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mHand = savedInstanceState.getParcelable(ARG_HAND);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_HAND, mHand);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_waiting_screen, container, false);

        mHandView = (HandView) result.findViewById(R.id.handView);

        return result;
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getArguments();
        if (args != null) {
            mHand = args.getParcelable(ARG_HAND);
        }

        updateView();
    }

    private void updateView() {
        Log.d(TAG, "updating view");

        mHandView.setHand(mHand);
    }


}
