package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.ufrgs.inf.dicelydone.HandView;
import br.ufrgs.inf.dicelydone.R;
import br.ufrgs.inf.dicelydone.model.GameControl;
import br.ufrgs.inf.dicelydone.model.Hand;

public class HandInfoFragment extends Fragment implements GameControl.Handler {

    public static final String ARG_HAND = "br.ufrgs.inf.dicelydone.HAND";

    public static final String ARG_PLAYER = "br.ufrgs.inf.dicelydone.PLAYER";

    private HandView mHandView;
    private Hand mHand;
    private String mPlayer;

    private GameControl mGameCtrl;

    public HandInfoFragment() {
        // Required empty constructor
        mHand = new Hand();
    }

    public Hand getHand() {
        return mHand;
    }

    public void setPlayer(String player) {
        mPlayer = player;
    }

    public void setGameControl(GameControl gameCtrl) {
        if (mGameCtrl != null) {
            mGameCtrl.removeHandler(this);
        }

        mGameCtrl = gameCtrl;
        mGameCtrl.addHandler(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            readBundle(savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_HAND, mHand);
        outState.putString(ARG_PLAYER, mPlayer);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_hand_info, container, false);
        mHandView = (HandView) result.findViewById(R.id.handView);
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
    public void onJoined() {
        // Nothing needs to be done
    }

    @Override
    public void onStartGame() {
        // Nothing needs to be done
    }

    @Override
    public void onStartRollTurn(int turn) {
        // Nothing needs to be done
    }

    @Override
    public void onStartBetTurn(int turn, int minBet) {
        // Nothing needs to be done
    }

    @Override
    public void onDiceRolled(String player, Hand rolled) {
        if (player.equals(mPlayer)) {
            mHand = rolled;
            updateView();
        }
    }

    @Override
    public void onBetPlaced(String player, int totalBet, int individualBet) {
        // Nothing needs to be done
    }

    @Override
    public void onFolded(String player) {
        // Nothing needs to be done
    }

    @Override
    public void onGameEnded(String winner, int prize) {
        mHand = new Hand();
        updateView();
    }

    private void updateView() {
        mHandView.setHand(mHand);
    }

    private void readBundle(Bundle savedInstanceState) {
        mHand = savedInstanceState.getParcelable(ARG_HAND);
        mPlayer = savedInstanceState.getString(ARG_PLAYER);
    }


}
