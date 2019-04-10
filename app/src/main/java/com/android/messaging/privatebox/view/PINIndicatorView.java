package com.android.messaging.privatebox.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.superapps.util.Dimensions;

import java.util.ArrayList;

/**
 * Custom view indicating the procedure of PIN inputs.
 */
public class PINIndicatorView extends LinearLayout {

    public interface OnPINFinishedListener {
        void onPINFinished(String decodedPIN);
    }

    private static final int MAX_PIN_LENGTH = 4;

    private String getDecodedPIN() {
        String decodedPIN = "";
        for (Integer each : pin) {
            decodedPIN = decodedPIN + each;
        }
        return decodedPIN;
    }

    private Context context;
    private ArrayList<ImageView> views;
    private ArrayList<Integer> pin;
    private OnPINFinishedListener listener;
    private Drawable shapePinCircle;
    private Drawable shapePinRing;

    public PINIndicatorView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public PINIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PINIndicatorView, 0, 0);
        shapePinCircle = typedArray.getDrawable(R.styleable.PINIndicatorView_pin_indicator_circle);
        shapePinCircle.setColorFilter(new PorterDuffColorFilter(PrimaryColors.getPrimaryColor(), PorterDuff.Mode.MULTIPLY));
        shapePinRing = typedArray.getDrawable(R.styleable.PINIndicatorView_pin_indicator_ring);
        int color = PrimaryColors.getPrimaryColor();
        int blendedRed = Color.red(color);
        int blendedGreen = Color.green(color);
        int blendedBlue = Color.blue(color);
        int ringColor = Color.rgb(255 - (255 - blendedRed) / 2, 255 - (255 - blendedGreen) / 2, 255 - (255 - blendedBlue) / 2);
        shapePinRing.setColorFilter(new PorterDuffColorFilter(ringColor, PorterDuff.Mode.MULTIPLY));
        typedArray.recycle();
        init();
    }

    private void init() {
        views = new ArrayList<>();
        pin = new ArrayList<>();
        for (int i = 0; i < MAX_PIN_LENGTH; ++i) {
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new LayoutParams(Dimensions.pxFromDp(18), Dimensions.pxFromDp(6)));
            imageView.setImageDrawable(shapePinRing);
            imageView.setPadding(Dimensions.pxFromDp(6), 0, Dimensions.pxFromDp(6), 0);
            views.add(imageView);
            addView(imageView);
        }
    }

    public void inc(int number) {
        if (pin.size() >= MAX_PIN_LENGTH) return;
        views.get(pin.size()).setImageDrawable(shapePinCircle);
        pin.add(number);
        if (pin.size() == MAX_PIN_LENGTH) {
            listener.onPINFinished(getDecodedPIN());
        }
    }

    public void dec() {
        if (pin.size() <= 0) return;
        views.get(pin.size() - 1).setImageDrawable(shapePinRing);
        pin.remove(pin.size() - 1);
    }

    public void clear() {
        if (pin.size() == 0) return;
        pin.clear();
        for (ImageView view : views) {
            view.setImageDrawable(shapePinRing);
        }
    }

    public void setOnPINFinishedListener(OnPINFinishedListener listener) {
        this.listener = listener;
    }
}
