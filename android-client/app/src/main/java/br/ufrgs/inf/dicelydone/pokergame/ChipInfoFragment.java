package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import br.ufrgs.inf.dicelydone.ChipSetView;
import br.ufrgs.inf.dicelydone.R;
import br.ufrgs.inf.dicelydone.model.Chip;
import br.ufrgs.inf.dicelydone.model.ChipSet;
import br.ufrgs.inf.dicelydone.model.GameControl;

/**
 * Fragment for informing about the current bets and the player's chip stash.
 *
 * <p>
 * Activities using this fragment MUST implement {@link ChipInfoFragment.EventHandler}
 *
 * <p>
 * Displays the current total bet and the current individual bet,
 * as well as the the player's bet and stash. May allow the player
 * to manipulate their bet and stash (see {@link #setReadOnly(boolean)}
 * and {@link #ARG_READ_ONLY}).
 */
public class ChipInfoFragment extends Fragment implements GameControl.Handler {

    public interface EventHandler {

        /** Called when the player changes its bet or stash.
         *
         * @param stash Chips remaining in possession of the player
         * @param bet   Chips the player has used for betting
         */
        void onBetAndStashChanged(ChipSet stash, ChipSet bet);

    }

    /**
     * This argument must contain the player's chips.
     */
    public static final String ARG_STASH = "br.ufrgs.inf.dicelydone.STASH";

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

    /**
     * This argument indicates if the player may change their bet.
     */
    public static final String ARG_READ_ONLY = "br.ufrgs.inf.dicelydone.READ_ONLY";

    public static final String ARG_PLAYER = "br.ufrgs.inf.dicelydone.PLAYER";

    private EventHandler mCallback;
    private GameControl mGameCtrl;

    private ChipSetView mPlayerChipsView;
    private ChipSetView mPlayerBetView;
    private TextView mTotalBetView;
    private TextView mMinBetView;

    private ChipSet mPlayerStash;
    private ChipSet mPlayerBet;
    private ChipSet mAddedBet;

    private int mTotalBet;
    private int mIndividualBet;
    private String mPlayer;

    public static ChipInfoFragment createInstance(Bundle args) {
        ChipInfoFragment result = new ChipInfoFragment();
        result.setArguments(args);
        return result;
    }

    public ChipInfoFragment() {
        // Required empty constructor
    }

    public ChipSet getPlayerStash() {
        return mPlayerStash;
    }

    public ChipSet getPlayerBet() {
        return mPlayerBet;
    }

    public ChipSet getAddedBet() {
        return mAddedBet;
    }

    public int getIndividualBet() {
        return mIndividualBet;
    }

    public int getTotalBet() {
        return mTotalBet;
    }

    public void setPlayer(String player) {
        mPlayer = player;
    }

    public void initGame() {
        mTotalBet = 0;
        mIndividualBet = 0;
        mPlayerBet = new ChipSet();
        mPlayerStash = new ChipSet();

        for (Chip c : Chip.values()) {
            mPlayerStash.addChips(c, 10);
        }
    }

    public void setReadOnly(boolean readOnly) {
        if (!isAdded()) return;

        mPlayerBetView.setEnabled(!readOnly);
        mPlayerChipsView.setEnabled(!readOnly);
    }

    public void setGameControl(GameControl gameCtrl) {
        if (mGameCtrl != null) {
            mGameCtrl.removeHandler(this);
        }

        mGameCtrl = gameCtrl;

        if (isVisible()) {
            mGameCtrl.addHandler(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            readBundle(savedInstanceState);

        } else {
            mPlayerStash = new ChipSet();
            mPlayerBet = new ChipSet();
            mTotalBet = 0;
            mIndividualBet = 0;

            for (Chip c : Chip.values()) {
                mPlayerStash.addChips(c, 10);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_STASH, mPlayerStash);
        outState.putParcelable(ARG_PLAYER_BET, mPlayerBet);
        outState.putInt(ARG_TOTAL_BET, mTotalBet);
        outState.putInt(ARG_INDIVIDUAL_BET, mIndividualBet);
        outState.putString(ARG_PLAYER, mPlayer);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (EventHandler) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ChipInfoFragment.EventHandler");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_chip_info, container, false);

        mTotalBetView = (TextView) result.findViewById(R.id.totalBetView);
        mMinBetView = (TextView) result.findViewById(R.id.minBetView);
        mPlayerBetView = (ChipSetView) result.findViewById(R.id.playerBetView);
        mPlayerChipsView = (ChipSetView) result.findViewById(R.id.playerChipsView);

        // When a player's chip is clicked, it is added to the bet
        mPlayerChipsView.setOnChipClickListener(new ChipSetView.OnChipClickListener() {
            @Override
            public void onChipClick(ChipSetView view, Chip type) {
                int taken = mPlayerStash.takeChips(type, 1);
                if (taken > 0) {
                    mPlayerBet.addChips(type, taken);
                    mAddedBet.addChips(type, taken);

                    mTotalBet += type.getValue() * taken;
                    updateView();
                    mCallback.onBetAndStashChanged(mPlayerStash, mPlayerBet);
                }
            }
        });

        // When a bet's chip is clicked, it is returned to the player
        mPlayerBetView.setOnChipClickListener(new ChipSetView.OnChipClickListener() {
            @Override
            public void onChipClick(ChipSetView view, Chip type) {
                int taken = mPlayerBet.takeChips(type, 1);
                if (taken > 0) {
                    mPlayerStash.addChips(type, taken);
                    mAddedBet.addChips(type, -taken);

                    mTotalBet -= type.getValue() * taken;
                    updateView();
                    mCallback.onBetAndStashChanged(mPlayerStash, mPlayerBet);
                }
            }
        });

        return result;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getArguments() != null) {
            readBundle(getArguments());
        }

        if (mGameCtrl != null) {
            mGameCtrl.addHandler(this);
        }

        updateView();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGameCtrl != null) {
            mGameCtrl.removeHandler(this);
        }
    }

    private void readBundle(Bundle bundle) {
        mPlayerStash = bundle.getParcelable(ARG_STASH);
        mPlayerBet = bundle.getParcelable(ARG_PLAYER_BET);
        mTotalBet = bundle.getInt(ARG_TOTAL_BET);
        mIndividualBet = bundle.getInt(ARG_INDIVIDUAL_BET, 0);
        mPlayer = bundle.getString(ARG_PLAYER);
        mAddedBet = new ChipSet();

        setReadOnly(bundle.getBoolean(ARG_READ_ONLY));
    }

    private void updateView() {
        if (!isAdded()) return;

        mPlayerBetView.setChipSet(mPlayerBet);
        mPlayerChipsView.setChipSet(mPlayerStash);
        mTotalBetView.setText(Integer.toString(mTotalBet));
        mMinBetView.setText(Integer.toString(mIndividualBet));
    }

    @Override
    public void handleMessage(GameControl.InMessage message) {
        switch (message.getType()) {
            case STARTGAME: {
                GameControl.StartGameMessage msg = (GameControl.StartGameMessage) message;

                mTotalBet = 0;
                mIndividualBet = msg.getInitialBet();
                mAddedBet = new ChipSet();

                increaseBetTo(mIndividualBet);
                break;
            }

            case STARTTURN: {
                GameControl.StartTurnMessage msg = (GameControl.StartTurnMessage) message;

                setReadOnly(msg.getRound() != 2);

                if (msg.getRound() == 2 || msg.getRound() == 3) {
                    mAddedBet.clear();
                    mIndividualBet = msg.getMinBet();
                    increaseBetTo(mIndividualBet);

                    int added = mIndividualBet - mPlayerBet.getValue();
                    mTotalBet += added;

                    updateView();
                }

                break;
            }

            case BETPLACED: {
                GameControl.BetPlacedMessage msg = (GameControl.BetPlacedMessage) message;
                mTotalBet = msg.getTotalBet();
                mIndividualBet = msg.getIndividualBet();
                updateView();

                break;
            }

            case FOLDED: {
                GameControl.FoldedMessage msg = (GameControl.FoldedMessage) message;
                if (msg.getPlayer().equals(mPlayer)) {
                    // Undo the temporary bet
                    mTotalBet -= mAddedBet.getValue();
                    for (Chip c : Chip.values()) {
                        mPlayerStash.addChips(c, mAddedBet.getChips(c));
                    }

                    mPlayerBet.clear();
                    mAddedBet.clear();
                }

                break;
            }

            case ENDGAME: {
                GameControl.EndGameMessage msg = (GameControl.EndGameMessage) message;

                if (msg.getWinner().equals(mPlayer)) {
                    mPlayerStash.add(msg.getPrize());
                }

                mTotalBet = 0;
                mIndividualBet = 0;
                mPlayerBet.clear();
                updateView();
            }

            case JOINED:
            case DICE:
            case DISCONNECTED:
            case CLOSE:
                break; // Doesn't need to handle
        }
    }

    private void increaseBetTo(int minBet) {
        // Increase the player's bet to the minimum value
        int toAdd = minBet - mPlayerBet.getValue();
        for (int i = Chip.values().length-1; i >= 0 && toAdd > 0; i--) {
            Chip c = Chip.values()[i];

            int taken = mPlayerStash.takeChips(c, toAdd / c.getValue());
            mPlayerBet.addChips(c, taken);
            mAddedBet.addChips(c, taken);

            toAdd -= taken * c.getValue();
        }
    }
}
