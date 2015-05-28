package br.ufrgs.inf.dicelydone;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import br.ufrgs.inf.dicelydone.model.Chip;
import br.ufrgs.inf.dicelydone.model.ChipSet;

/**
 * A graphical component for showing sets of betting chips
 */
public class ChipSetView extends GridLayout {

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

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setRowCount(2);
        setColumnCount(2 + Chip.values().length);

        Resources res = context.getResources();

        // Create and configure the label for the total value
        mLblVal = new TextView(context, null, defStyleAttr);
        mLblVal.setGravity(Gravity.CENTER_HORIZONTAL);
        mLblVal.setTextSize(32);
        mLblVal.setTextColor(res.getColor(R.color.bright_foreground_material_light));

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

            // Create the chip image
            // TODO use some actual image
            TextView chip = new TextView(context);
            chip.setText(Integer.toString(c.getValue()));
            chip.setTextSize(28);
            chip.setGravity(Gravity.CENTER);
            //chip.setClipToOutline(true); // NEEDS API 21

            ImageView imageView = new ImageView(context);
            imageView.setImageResource(R.drawable.abc_switch_thumb_material);

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

}
