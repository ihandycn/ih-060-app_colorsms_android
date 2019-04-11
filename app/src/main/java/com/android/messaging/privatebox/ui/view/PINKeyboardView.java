package com.android.messaging.privatebox.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.util.Typefaces;
import com.superapps.util.Dimensions;

import java.util.ArrayList;

/**
 * Custom Decimal Keyboard View
 */
public class PINKeyboardView extends TableLayout {

    public interface OnKeyboardClickListener {
        void onKeyboardClick(int i);
    }

    private static final int[] VIEW_NUMBER_ARRAY = {
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            0, -1,
    };

    private static final int TEXT_SIZE = 24; // dp

    private static final float STROKE_WIDTH = 2; // dp

    private int pinNumberColor;
    private int pinCircleColor;
    private Drawable pinBtnBack;
    private Paint circlePaint;

    private OnKeyboardClickListener listener;


    public PINKeyboardView(Context context) {
        super(context);
        init();
    }

    public PINKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PINKeyboardView, 0, 0);
        pinNumberColor = typedArray.getColor(R.styleable.PINKeyboardView_pin_number_color, -1);
        pinCircleColor = typedArray.getColor(R.styleable.PINKeyboardView_pin_circle_color, -1);
        pinBtnBack = typedArray.getDrawable(R.styleable.PINKeyboardView_pin_btn_back);
        typedArray.recycle();
        init();
    }

    private void init() {
        setStretchAllColumns(true);

        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        ArrayList<View> views = new ArrayList<>();

        Typeface typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        for (int i = 0; i < 3; ++i) {
            TableRow row = new TableRow(getContext());
            row.setGravity(Gravity.CENTER);
            row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));

            for (int j = 0; j < 3; ++j) {
                ClickableTextView textView = new ClickableTextView(getContext(), getCirclePaint());
                textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.0f));
                textView.setBackgroundResource(android.R.color.transparent);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
                textView.setTextColor(pinNumberColor);
                textView.setGravity(Gravity.CENTER);
                textView.setTypeface(typeface);
                textView.setText(String.valueOf(i * 3 + j + 1));
                row.addView(textView);

                views.add(textView);
            }

            addView(row);
        }

        TableRow lastRow = new TableRow(getContext());
        lastRow.setGravity(Gravity.CENTER);
        lastRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));

        TextView viewPlaceholder = new TextView(getContext());
        viewPlaceholder.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.0f));

        ClickableTextView textView = new ClickableTextView(getContext(), getCirclePaint());
        textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.0f));
        textView.setBackgroundResource(android.R.color.transparent);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
        textView.setTextColor(pinNumberColor);
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(typeface);
        textView.setText("0");

        ClickableImageView backImageView = new ClickableImageView(getContext(), getCirclePaint());
        backImageView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.0f));
        backImageView.setBackgroundResource(android.R.color.transparent);
        backImageView.setImageDrawable(pinBtnBack);
        backImageView.setScaleType(ImageView.ScaleType.CENTER);
        lastRow.addView(viewPlaceholder);
        lastRow.addView(textView);
        lastRow.addView(backImageView);
        addView(lastRow);
        views.add(textView);
        views.add(backImageView);

        for (int i = 0; i < views.size(); ++i) {
            final int number = VIEW_NUMBER_ARRAY[i];
            View view = views.get(i);

            if (view instanceof ClickableTextView) {
                final ClickableTextView clickableTextView = (ClickableTextView) view;
                clickableTextView.setTextSize(33);
                clickableTextView.setTypeface(Typefaces.getCustomRegular());
                views.get(i).setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        if (action == MotionEvent.ACTION_DOWN) {
                            clickableTextView.setTouching(true);
                        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_OUTSIDE) {
                            clickableTextView.setTouching(false);
                        }
                        return false;
                    }
                });

            } else { // aView instanceof ClickableImageView
                final ClickableImageView clickableImageView = (ClickableImageView) view;
                views.get(i).setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        if (action == MotionEvent.ACTION_DOWN) {
                            clickableImageView.setTouching(true);
                        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_OUTSIDE) {
                            clickableImageView.setTouching(false);
                        }
                        return false;
                    }
                });
            }
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener == null) return;
                    listener.onKeyboardClick(number);
                }
            });
        }
    }

    private Paint getCirclePaint() {
        if (circlePaint == null) {
            circlePaint = new Paint();
            circlePaint.setColor(pinCircleColor);
            circlePaint.setAntiAlias(true);
            circlePaint.setStyle(Paint.Style.FILL);
            circlePaint.setStrokeWidth(Dimensions.pxFromDp(STROKE_WIDTH));
        }
        return circlePaint;
    }

    public void setOnKeyboardClickListener(OnKeyboardClickListener listener) {
        this.listener = listener;
    }
}
