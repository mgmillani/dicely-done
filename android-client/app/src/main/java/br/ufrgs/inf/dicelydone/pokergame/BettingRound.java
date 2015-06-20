package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import br.ufrgs.inf.dicelydone.R;

/**
 * Fragment for the betting rounds, where players place their bets.
 *
 * <p>
 * Activities using this fragment MUST implement {@link BettingRound.EventHandler}
 *
 * <p>
 * Displays instructions and buttons to place a bet or fold.
 */
public class BettingRound extends Fragment {

    public static final String ARG_MESSAGE = "br.ufrgs.inf.dicelydone.MESSAGE";

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

    private TextView mLbl;
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

        mLbl = (TextView) result.findViewById(R.id.instructionsView);

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

        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_MESSAGE)) {
            mLbl.setText(args.getCharSequence(ARG_MESSAGE));
        }
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
