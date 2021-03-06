package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import br.ufrgs.inf.dicelydone.HandView;
import br.ufrgs.inf.dicelydone.R;
import br.ufrgs.inf.dicelydone.model.ChipSet;
import br.ufrgs.inf.dicelydone.model.Hand;

/**
 * Fragment for the betting rounds, where players place their bets.
 *
 * <p>
 * Activities using this fragment MUST implement {@link BettingRound.EventHandler}
 *
 * <p>
 * Displays the player's dice, chips and bet, as well
 * as the overall bet. Allows the player to move chips
 * from his stash to the bet and vice versa, when given
 * true for {@link #ARG_CAN_RAISE}. Also allows the
 * player to place his bet or fold.
 */
public class BettingRound extends Fragment {

    public interface EventHandler {
        /**
         * Called when the player places a bet.
         */
        void onBetPlaced();

        /**
         * Called when the player folds.
         */
        void onFolded();
    }

    private EventHandler mCallback;
    private Button mBtnBet;

    public BettingRound() {
        // Required empty constructor
    }

    public void setBetEnabled(boolean enabled) {
        mBtnBet.setEnabled(enabled);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_betting_round, container, false);

        mBtnBet = (Button) result.findViewById(R.id.buttonOk);
        mBtnBet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeBet();
            }
        });

        result.findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fold();
            }
        });

        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (EventHandler) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement BettingRound.EventHandler");
        }
    }

    private void placeBet() {
        if (mCallback == null) return;

        mCallback.onBetPlaced();
    }

    private void fold() {
        if (mCallback == null) return;

        mCallback.onFolded();
    }

}
