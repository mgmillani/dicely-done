package br.ufrgs.inf.dicelydone;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;

import br.ufrgs.inf.dicelydone.model.Player;

public class PlayerView extends CardView {

    private Player mPlayer;

    public PlayerView(Context context, Player p) {
        super(context);
        mPlayer = p;

        init();
    }

    private void init() {
        LayoutInflater vi = LayoutInflater.from(getContext());

        View inner = vi.inflate(R.layout.view_player, null);
        addView(inner);
    }

}
