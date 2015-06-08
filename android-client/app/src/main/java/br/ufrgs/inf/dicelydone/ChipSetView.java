package br.ufrgs.inf.dicelydone;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import java.util.ArrayList;

import br.ufrgs.inf.dicelydone.model.Chip;
import br.ufrgs.inf.dicelydone.model.ChipSet;

/**
 * A graphical component for showing sets of betting chips.
 *
 * <p>
 * Shows both the total value of all chips and the number of chips for each type.
 * The individual chips shown may be clicked, firing events that may be listened
 * to with {@link #setOnChipClickListener(OnChipClickListener)}.
 *
 */
public class ChipSetView extends GridLayout {

    public interface OnChipClickListener {

        /**
         * Called when the user clicked one of the displayed chips
         *
         * @param view The view whose chips were clicked
         * @param type The type of chip that was clicked
         */
        void onChipClick(ChipSetView view, Chip type);

    }

    private ArrayList<OnChipClickListener> mChipClickListeners = new ArrayList<>();

    private TextView mLblVal;
    private TextView[] mChipNums;

    private ChipSet mChips;

    public ChipSetView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public ChipSetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null, 0, 0);
    }

    public ChipSetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, null, defStyleAttr, 0);
    }

    // Only available for API 21
//    public ChipSetView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        init(context, null, defStyleAttr, defStyleRes);
//    }

    public ChipSet getChipSet() { return mChips; }

    public void setChipSet(ChipSet chips) {
        mChips = chips;
        onValueChanged();
    }

    public void onValueChanged() {
        mLblVal.setText(Integer.toString(mChips.getValue()));

        for (Chip c : Chip.values()) {
            int numChips = mChips.getChips(c);
            TextView lbl = mChipNums[c.getIndex()];
            Resources res = getContext().getResources();

            lbl.setText(Integer.toString(numChips) + "x");
            lbl.setTextColor(res.getColor(
                    numChips > 0 ?
                            R.color.dim_foreground_material_light
                            : R.color.dim_foreground_disabled_material_light));
        }
    }

    public void setOnChipClickListener(OnChipClickListener listener) {
        mChipClickListeners.add(listener);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setRowCount(2);
        setColumnCount(2 + Chip.values().length);

        Resources res = context.getResources();

        // Create and configure the label for the total value
        mLblVal = new TextView(context, null, defStyleAttr);
        mLblVal.setGravity(Gravity.CENTER_HORIZONTAL);
        mLblVal.setTextAppearance(context, R.style.PokerGame_TextAppearance_ChipSumVal);

        LayoutParams params = new LayoutParams(spec(0, 2), spec(0));
        params.setGravity(Gravity.CENTER);
        params.width = res.getDimensionPixelSize(R.dimen.chipsetview_totalval_width);
        params.height = LayoutParams.WRAP_CONTENT;
        addView(mLblVal, params);

        // Create the spacer
        Space spacer = new Space(context);

        params = new LayoutParams(spec(0, 2), spec(1));
        params.width = res.getDimensionPixelSize(R.dimen.chipsetview_spacer_width);
        addView(spacer, params);

        // Create the chips and their labels
        mChipNums = new TextView[Chip.values().length];

        for (Chip c : Chip.values()) {
            int col = 2 + c.getIndex();

            OnClickListener listener = new OnClickListener_Chip(c);

            // Create the chip image
            // TODO use some proper image
            TextView chip = new TextView(context);
            chip.setText(Integer.toString(c.getValue()));
            chip.setTextSize(28);
            chip.setGravity(Gravity.CENTER);
            //chip.setClipToOutline(true); // NEEDS API 21
            //chip.setOnClickListener(listener);

            ImageView imageView = new ImageView(context);
            imageView.setImageResource(R.drawable.abc_switch_thumb_material);
            imageView.setOnClickListener(listener);


            params = new LayoutParams(spec(1), spec(col));
            params.width = res.getDimensionPixelSize(R.dimen.bettingchip_size);
            params.height = res.getDimensionPixelSize(R.dimen.bettingchip_size);
            addView(imageView, params);
            addView(chip, params);

            // Create the label for the number of chips
            TextView lblNumChips = new TextView(context);
            lblNumChips.setTextSize(20);
            mChipNums[c.getIndex()] = lblNumChips;

            params = new LayoutParams(spec(0), spec(col));
            params.width = LayoutParams.WRAP_CONTENT;
            params.height = LayoutParams.WRAP_CONTENT;
            params.setGravity(Gravity.CENTER_HORIZONTAL);
            addView(lblNumChips, params);
        }

        mChips = new ChipSet();
        onValueChanged();
    }

    private void fireChipClick(Chip type) {
        for (OnChipClickListener listener : mChipClickListeners) {
            listener.onChipClick(this, type);
        }
    }

    private class OnClickListener_Chip implements OnClickListener {
        private final Chip c;

        public OnClickListener_Chip(Chip c) {
            this.c = c;
        }

        @Override
        public void onClick(View v) {
            if (isEnabled()) {
                fireChipClick(c);
            }
        }
    }

}
