package br.ufrgs.inf.dicelydone.pokergame;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.ufrgs.inf.dicelydone.R;

/**
 * Fragment for the first round, where players roll their dice.
 *
 * <p>
 * Just displays the instructions for the player.
 */
public class Round1 extends Fragment {

    public Round1() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_round_1, container, false);
    }
}
