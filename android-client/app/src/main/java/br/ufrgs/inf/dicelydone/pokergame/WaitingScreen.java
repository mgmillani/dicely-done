package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private Hand mHand;

    public WaitingScreen() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_waiting_screen, container, false);
    }

}
