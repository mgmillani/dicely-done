package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;

import br.ufrgs.inf.dicelydone.PlayerView;
import br.ufrgs.inf.dicelydone.R;
import br.ufrgs.inf.dicelydone.model.Player;

public class PlayersFragment extends Fragment {
    private static final String ARG_PLAYER = "PLAYER";

    private final ArrayList<Player> mPlayers = new ArrayList<>();
    private final HashMap<String, PlayerView> mPlayerViews = new HashMap<>();

    private LinearLayout mPlayerListView;
    private boolean mShowBet;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mainPlayer
     * @return A new instance of fragment PlayersFragment.
     */
    public static PlayersFragment newInstance(String mainPlayer, String param2) {
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
            mPlayers.add(new Player(mainPlayer));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_players, container, false);

        mPlayerListView = (LinearLayout) result.findViewById(R.id.playersList);

        for (Player p : mPlayers) {
            PlayerView v = new PlayerView(getActivity(), p);
            mPlayerViews.put(p.getName(), v);
            mPlayerListView.addView(v);
        }

        return result;
    }

}
