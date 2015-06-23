package br.ufrgs.inf.dicelydone;

import android.animation.LayoutTransition;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import br.ufrgs.inf.dicelydone.model.Player;

public class PlayerView extends FrameLayout {

    private Player mPlayer;

    private TextView mBetLabel;
    private TextView mHandLabel;

    private ImageView mIcon;
    private TextView mNameView;
    private TextView mBetView;
    private HandView mHandView;

    private boolean mBetVisible;

    public PlayerView(Context context, Player p) {
        super(context);
        mPlayer = p;

        init();
    }

    public PlayerView(Context context) {
        super(context);
        init();
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayerView(Context context, AttributeSet attrs, Player player) {
        super(context, attrs);
        mPlayer = player;
    }

    private void init() {
        LayoutInflater vi = LayoutInflater.from(getContext());

        View inner = vi.inflate(R.layout.view_player, null);
        addView(inner);

        mBetLabel = (TextView) findViewById(R.id.label_bet);
        mHandLabel = (TextView) findViewById(R.id.label_hand);

        mIcon = (ImageView) findViewById(R.id.player_icon);
        mNameView = (TextView) findViewById(R.id.player_name);
        mBetView = (TextView) findViewById(R.id.player_bet);
        mHandView = (HandView) findViewById(R.id.player_hand);

        mHandView.setEnabled(false);

        LayoutTransition l = new LayoutTransition();
        l.enableTransitionType(LayoutTransition.CHANGING);
        ViewGroup group = (ViewGroup) findViewById(R.id.container_layout);
        group.setLayoutTransition(l);

        updateView();
        setBetVisible(false);
    }

    public void updateView() {
        if (mPlayer == null) return;

        mNameView.setText(mPlayer.getName());
        mBetView.setText(Integer.toString(mPlayer.getBet()));

        int handVisibility = !mPlayer.getHand().isEmpty() ? VISIBLE :
                mBetView.getVisibility() == VISIBLE? INVISIBLE : GONE;

        mHandView.setHand(mPlayer.getHand());
        mHandView.setVisibility(handVisibility);
        mHandLabel.setVisibility(handVisibility);
    }

    public void setBetVisible(boolean visible) {
        int visibility = visible? VISIBLE : GONE;

        mBetView.setVisibility(visibility);
        mBetLabel.setVisibility(visibility);

        if (visible && mHandView.getVisibility() == GONE) {
            mHandView.setVisibility(INVISIBLE);
            mHandLabel.setVisibility(INVISIBLE);

        } else if (!visible && mHandView.getVisibility() == INVISIBLE) {
            mHandView.setVisibility(GONE);
            mHandLabel.setVisibility(GONE);
        }
    }




}
