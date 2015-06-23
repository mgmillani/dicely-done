package br.ufrgs.inf.dicelydone;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import br.ufrgs.inf.dicelydone.model.Die;


/**
 * A graphical component showing a face of a die.
 */
public class DieView extends View {

    private int mDotColor = R.color.bright_foreground_material_light;
    private Die mDie = Die.FIVE;

    private Paint mPaint;
    private GestureDetector mDetector;

    public DieView(Context context) {
        super(context);
        init(null, 0);
    }

    public DieView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DieView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.DieView, defStyle, 0);

        mDotColor = a.getInteger(R.styleable.DieView_dotColor, mDotColor);
        mSelected = false;

        setBackground(getResources().getDrawable(R.drawable.die_bg_rect));

        int dieVal = a.getInteger(R.styleable.DieView_dieValue, -1);
        mDie = (1 <= dieVal && dieVal <= 6)? Die.byVal(dieVal) : mDie;

        a.recycle();

        mPaint = new Paint();

        mDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });
    }

    private int mDieSize;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private int mPaddingBottom;

    private boolean mSelected;


    public Die getDie() { return mDie; }

    public void setDie(Die die) {
        mDie = die;
    }

    public int getDotColor() { return mDotColor; }

    public void setDotColor(int color) {
        mDotColor = color;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = mDetector.onTouchEvent(event);

        if (!result && event.getAction() == MotionEvent.ACTION_UP) {
            if (isEnabled()) {
                setSelected(!isSelected());
                updateBackground();
                performClick();
            }
        }
        return true ;
    }

    private void updateBackground() {
        setBackground(getResources().getDrawable(
                isSelected()? R.drawable.die_bg_selected : R.drawable.die_bg_rect
        ));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        mDieSize = contentWidth<contentHeight? contentWidth : contentHeight;

        mPaddingLeft = (getWidth() - mDieSize)/2;
        mPaddingRight = getWidth() - mDieSize - mPaddingLeft;
        mPaddingTop = (getHeight() - mDieSize)/2;
        mPaddingBottom = getHeight() - mDieSize - mPaddingRight;

        // Draw the die.
        //mPaint.setColor(getBackgroundColor());
        //RectF rect = new RectF(paddingLeft, paddingTop, paddingLeft+mDieSize, paddingTop+mDieSize);
        //canvas.drawRoundRect(rect, mDieSize / 10, mDieSize / 10, mPaint);

        mPaint.setColor(mDotColor);
        mPaint.setAntiAlias(true);
        switch(mDie) {
        case ONE:
            drawDot(canvas, 2, 2);
            break;
        case TWO:
            drawDot(canvas, 1, 1);
            drawDot(canvas, 3, 3);
            break;
        case THREE:
            drawDot(canvas, 1, 1);
            drawDot(canvas, 2, 2);
            drawDot(canvas, 3, 3);
            break;
        case FOUR:
            drawDot(canvas, 1, 1);
            drawDot(canvas, 1, 3);
            drawDot(canvas, 3, 1);
            drawDot(canvas, 3, 3);
            break;
        case FIVE:
            drawDot(canvas, 1, 1);
            drawDot(canvas, 1, 3);
            drawDot(canvas, 2, 2);
            drawDot(canvas, 3, 1);
            drawDot(canvas, 3, 3);
            break;
        case SIX:
            drawDot(canvas, 1, 1);
            drawDot(canvas, 1, 3);
            drawDot(canvas, 2, 1);
            drawDot(canvas, 2, 3);
            drawDot(canvas, 3, 1);
            drawDot(canvas, 3, 3);
            break;
        }

    }

    @Override
    protected void onMeasure(int x, int y) {
        if (MeasureSpec.getMode(x) == MeasureSpec.UNSPECIFIED) {
            x = Integer.MAX_VALUE;
        } else {
            x = MeasureSpec.getSize(x);
        }
        if (MeasureSpec.getMode(y) == MeasureSpec.UNSPECIFIED) {
            y = Integer.MAX_VALUE;
        } else {
            y = MeasureSpec.getSize(y);
        }

        int size = x<y? x : y;
        if (size == Integer.MAX_VALUE) {
            size = 100;
        }

        setMeasuredDimension(size, size);
    }

    private void drawDot(Canvas canvas, int x, int y) {
        canvas.drawCircle(mPaddingLeft+ x*mDieSize/4, mPaddingTop+y*mDieSize/4, mDieSize/10, mPaint);
    }

}
