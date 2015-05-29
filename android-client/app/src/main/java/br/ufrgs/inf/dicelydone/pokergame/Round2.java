package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import br.ufrgs.inf.dicelydone.ChipSetView;
import br.ufrgs.inf.dicelydone.HandView;
import br.ufrgs.inf.dicelydone.R;
import br.ufrgs.inf.dicelydone.model.Chip;
import br.ufrgs.inf.dicelydone.model.ChipSet;
import br.ufrgs.inf.dicelydone.model.Hand;

/**
 * Fragment for the round 2, where players place their bets.
 */
public class Round2 extends Fragment {

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

    public Round2() {
        // Required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mHand = savedInstanceState.getParcelable(ARG_HAND);
            mPlayerChips = savedInstanceState.getParcelable(ARG_CHIPS);
            mPlayerBet = savedInstanceState.getParcelable(ARG_PLAYER_BET);
            mTotalBet = savedInstanceState.getInt(ARG_TOTAL_BET);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_HAND, mHand);
        outState.putParcelable(ARG_CHIPS, mPlayerChips);
        outState.putParcelable(ARG_PLAYER_BET, mPlayerBet);
        outState.putInt(ARG_TOTAL_BET, mTotalBet);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_round_2, container, false);

        mHandView = (HandView) result.findViewById(R.id.handView);
        mTotalBetView = (TextView) result.findViewById(R.id.totalBetView);
        mPlayerBetView = (ChipSetView) result.findViewById(R.id.playerBetView);
        mPlayerChipsView = (ChipSetView) result.findViewById(R.id.playerChipsView);

        // When a player's chip is clicked, it is added to the bet
        mPlayerChipsView.setOnChipClickListener(new ChipSetView.OnChipClickListener() {
            @Override
            public void onChipClick(ChipSetView view, Chip type) {
                int taken = mPlayerChips.takeChips(type, 1);
                if (taken > 0) {
                    mPlayerBet.addChips(type, taken);
                    mTotalBet += type.getValue() * taken;
                    updateView();
                }
            }
        });

        // When a bet's chip is clicked, it is returned to the player
        mPlayerBetView.setOnChipClickListener(new ChipSetView.OnChipClickListener() {
            @Override
            public void onChipClick(ChipSetView view, Chip type) {
                int taken = mPlayerBet.takeChips(type, 1);
                if (taken > 0) {
                    mPlayerChips.addChips(type, taken);
                    mTotalBet -= type.getValue() * taken;
                    updateView();
                }
            }
        });

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

    private void updateView() {
        mHandView.setHand(mHand);
        mPlayerBetView.setChipSet(mPlayerBet);
        mPlayerChipsView.setChipSet(mPlayerChips);

        if (mTotalBetView != null) {
            mTotalBetView.setText(Integer.toString(mTotalBet));
        }
    }

}
