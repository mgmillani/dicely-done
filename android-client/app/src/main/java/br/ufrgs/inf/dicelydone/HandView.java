package br.ufrgs.inf.dicelydone;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import br.ufrgs.inf.dicelydone.model.Die;
import br.ufrgs.inf.dicelydone.model.Hand;

/**
 * A graphical component for showing a set of (at most five) dice.
 */
public class HandView extends LinearLayout {

    private @NonNull Hand mHand = new Hand(Die.ONE, Die.TWO, Die.THREE, Die.FOUR, Die.FIVE);
    private final DieView[] dieViews = new DieView[5];

    public HandView(Context context) {
        super(context);
        init(null, 0, 0);
    }

    public HandView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, 0);
    }

    public HandView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

//    Needs API 21
//    public HandView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        init(attrs, defStyleAttr, defStyleRes);
//    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // Default attributes
        int margin = 10;
        float dieElevation = 0;//getElevation();

        // Obtain the attributes
        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.HandView, defStyleAttr, defStyleRes);

            margin = a.getDimensionPixelSize(R.styleable.HandView_dieMargin, margin);
            dieElevation = a.getDimension(R.styleable.HandView_dieElevation, dieElevation);

            a.recycle();
        }

        // Initialize the views
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        params.setMargins(margin, margin, margin, margin);

        for (int i=0; i<5; i++) {
            dieViews[i] = new DieView(getContext());
            dieViews[i].setDotColor(getResources().getColor(R.color.bright_foreground_material_light));
            dieViews[i].setBackground(getResources().getDrawable(R.drawable.die_bg_rect));
            dieViews[i].setLayoutParams(params);
            //dieViews[i].setElevation(dieElevation);

            addView(dieViews[i], params);
        }

        onHandChanged();
    }

    public @NonNull Hand getHand() { return mHand; }

    public void setHand(@NonNull Hand hand) {
        mHand = hand;
        onHandChanged();
    }

    public void onHandChanged() {
        int i;
        for (i=0; i<mHand.size(); i++) {
            dieViews[i].setDie(mHand.get(i));
            dieViews[i].setVisibility(VISIBLE);
        }
        for (;i<5; i++) {
            dieViews[i].setVisibility(INVISIBLE);
        }
    }
}
