package br.ufrgs.inf.dicelydone.pokergame;

import android.animation.LayoutTransition;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.HashMap;

import br.ufrgs.inf.dicelydone.PlayerView;
import br.ufrgs.inf.dicelydone.R;
import br.ufrgs.inf.dicelydone.model.GameControl;
import br.ufrgs.inf.dicelydone.model.Player;

public class PlayersFragment extends Fragment implements GameControl.Handler {
    private static final String ARG_PLAYER = "PLAYER";

    private final HashMap<String, Player> mPlayers = new HashMap<>();
    private final HashMap<String, PlayerView> mPlayerViews = new HashMap<>();

    private LinearLayout mPlayerListView;
    private int mMinBet;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mainPlayer
     * @return A new instance of fragment PlayersFragment.
     */
    public static PlayersFragment newInstance(String mainPlayer) {
        PlayersFragment fragment = new PlayersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLAYER, mainPlayer);
        fragment.setArguments(args);
        return fragment;
    }

    public PlayersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPlayers.clear();

        if (getArguments() != null) {
            String mainPlayer = getArguments().getString(ARG_PLAYER);
            mPlayers.put(mainPlayer, new Player(mainPlayer));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_players, container, false);

        mPlayerListView = (LinearLayout) result.findViewById(R.id.playersList);

        LayoutTransition l = new LayoutTransition();
        l.enableTransitionType(LayoutTransition.CHANGING);
        mPlayerListView.setLayoutTransition(l);

        for (Player p : mPlayers.values()) {
            addPlayerView(p);
        }

        return result;
    }

    private PlayerView addPlayerView(Player p) {

        PlayerView v = new PlayerView(getActivity(), p);
        mPlayerViews.put(p.getName(), v);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mPlayerListView.addView(v, params);

        return v;
    }

    @Override
    public void handleMessage(GameControl.InMessage message) {
        switch(message.getType()) {
            case JOINED: {
                // TODO for other players
            }
            return;

            case STARTGAME: {
                GameControl.StartGameMessage msg = (GameControl.StartGameMessage) message;
                mMinBet = msg.getInitialBet();

                for (Player p : mPlayers.values()) {
                    p.setBet(mMinBet);
                }

                for (PlayerView v : mPlayerViews.values()) {
                    v.setBetVisible(true);
                    v.updateView();
                }
            }
            return;

            case STARTTURN:
                // TODO change icons and/or colors
                break;

            case DICE: {
                GameControl.DiceMessage msg = (GameControl.DiceMessage) message;

                Player p = mPlayers.get(msg.getPlayer());
                if (p == null) {
                    p = new Player(msg.getPlayer());
                    mPlayers.put(msg.getPlayer(), p);
                    p.setBet(mMinBet);
                }
                p.setHand(msg.getDice());

                PlayerView v = mPlayerViews.get(msg.getPlayer());
                if (v == null) {
                    v = addPlayerView(p);
                    v.setBetVisible(true);
                }
                v.updateView();
            }
            return;

            case BETPLACED: {
                GameControl.BetPlacedMessage msg = (GameControl.BetPlacedMessage) message;

                mPlayers.get(msg.getPlayer())
                    .setBet(msg.getIndividualBet());

                mPlayerViews.get(msg.getPlayer())
                    .updateView();
            }
            return;

            case DISCONNECTED:
            case FOLDED: {
                GameControl.FoldedMessage msg = (GameControl.FoldedMessage) message;

                mPlayers.get(msg.getPlayer())
                    .getHand().clear();

                PlayerView v = mPlayerViews.get(msg.getPlayer());
                v.setBetVisible(false);
                v.updateView();

                // TODO change icon/color
            }
            return;

            case ENDGAME: {
                for (Player p : mPlayers.values()) {
                    p.setBet(0);
                    p.getHand().clear();
                }

                for (PlayerView v : mPlayerViews.values()) {
                    v.setBetVisible(false);
                    v.updateView();
                }
            }
            return;

            case CLOSE:
                return; // Nothing to be done
        }
    }
}
