package com.android.messaging.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.android.messaging.R;
import com.android.messaging.font.FontStyleManager;
import com.android.messaging.font.FontUtils;
import com.android.messaging.font.TypefaceInfo;
import com.superapps.util.Fonts;
import com.superapps.view.DebuggableTextView;

import static com.android.messaging.font.FontUtils.BLACK;
import static com.android.messaging.font.FontUtils.BOLD;
import static com.android.messaging.font.FontUtils.LIGHT;
import static com.android.messaging.font.FontUtils.MEDIUM;
import static com.android.messaging.font.FontUtils.REGULAR;
import static com.android.messaging.font.FontUtils.SEMI_BOLD;
import static com.android.messaging.font.FontUtils.THIN;

public class MessagesTextView extends DebuggableTextView {

    private Drawable mTopDrawable;
    private int fontStyle;
    private boolean fontFamilyChangeable;
    private boolean fontSizeChangeable;
    private float mDefaultTextSize;
    private float mDefaultTypefaceSizeScale = 1f;
    private float mCurrentScale = 1;
    private int mFontType;

    public MessagesTextView(Context context) {
        this(context, null);
    }

    public MessagesTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public MessagesTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (isInEditMode()) {
            return;
        }

        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.MessagesTextView);
        mFontType = styledAttrs.getResourceId(R.styleable.MessagesTextView_typeface, R.string.roboto_regular);
        String fontTypeFileName = styledAttrs.getString(R.styleable.MessagesTextView_typefaceFileName);
        fontStyle = styledAttrs.getInt(R.styleable.MessagesTextView_font_style, 3);
        fontFamilyChangeable = styledAttrs.getBoolean(R.styleable.MessagesTextView_font_family_changeable, true);
        fontSizeChangeable = styledAttrs.getBoolean(R.styleable.MessagesTextView_font_size_changeable, false);
        int drawableWidth = styledAttrs.getDimensionPixelSize(R.styleable.MessagesTextView_drawable_width, -1);
        int drawableHeight = styledAttrs.getDimensionPixelSize(R.styleable.MessagesTextView_drawable_height, -1);

        styledAttrs.recycle();

        Typeface typeface = null;
        if (!TextUtils.isEmpty(fontTypeFileName)) {
            typeface = Fonts.getTypeface(fontTypeFileName);
            setTypeface(typeface);
        }

        if (typeface == null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && mFontType == R.string.roboto_black) {
                mFontType = R.string.roboto_medium;
            }

            if (fontFamilyChangeable && mFontType != R.string.roboto_regular) {
                TypefaceInfo info = FontUtils.getTypefaceInfo(mFontType, fontStyle);
                if (info != null) {
                    mDefaultTypefaceSizeScale = info.getDefaultSizeScale();
                    setTypeface(info.getTypeface());
                }
            } else {
                typeface = Fonts.getTypeface(Fonts.Font.ofFontResId(mFontType), fontStyle);
                if (typeface != null) {
                    setTypeface(typeface);
                }
            }
        }

        mDefaultTextSize = getTextSize();

        if (fontSizeChangeable) {
            float scale = FontStyleManager.getInstance().getFontScale();
            setTextScale(scale);
        } else {
            setTextScale(1);
        }

        if (drawableWidth > 0 && drawableHeight > 0) {
            Drawable[] drawables = getCompoundDrawables();
            for (Drawable drawable : drawables) {
                if (drawable == null) {
                    continue;
                }
                Rect bounds = new Rect(drawable.getBounds());
                bounds.set(bounds.left, bounds.top, bounds.left + drawableWidth, bounds.top + drawableHeight);
                drawable.setBounds(bounds);
            }
            setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        }

        mTopDrawable = getCompoundDrawables()[1];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mTopDrawable != null) {
            int height = (int) (mTopDrawable.getBounds().bottom - mTopDrawable.getBounds().top + 1.0f / getLineSpacingMultiplier() * getTextSize() +
                    getPaddingTop() + getPaddingBottom() + getCompoundDrawablePadding());
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setTypefaceFileName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return;
        }
        Typeface typeface = Fonts.getTypeface(fileName);
        if (typeface != null) {
            setTypeface(typeface);
        }
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        mDefaultTextSize = size;
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        mDefaultTextSize = getTextSize();
    }

    public void setTextScale(float scale) {
        mCurrentScale = scale;
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, scale * mDefaultTextSize * mDefaultTypefaceSizeScale);
    }

    public void setTypeface(TypefaceInfo typefaceInfo) {
        if (typefaceInfo != null) {
            setTypeface(typefaceInfo.getTypeface());
            mDefaultTypefaceSizeScale = typefaceInfo.getDefaultSizeScale();
        } else {
            super.setTypeface(null);
            mDefaultTypefaceSizeScale = 1;
        }
        setTextScale(mCurrentScale);
    }

    public int getFontStyle() {
        return fontStyle;
    }

    public boolean fontFamilyChangeable() {
        return fontFamilyChangeable;
    }

    public boolean fontSizeChangeable() {
        return fontSizeChangeable;
    }

    public int getFontWeight() {
        if (mFontType == R.string.custom_font_thin) {
            return THIN;
        } else if (mFontType == R.string.custom_font_light) {
            return LIGHT;
        } else if (mFontType == R.string.custom_font_regular || mFontType == R.string.custom_font_regular_condensed) {
            return REGULAR;
        } else if (mFontType == R.string.custom_font_medium) {
            return MEDIUM;
        } else if (mFontType == R.string.custom_font_semibold) {
            return SEMI_BOLD;
        } else if (mFontType == R.string.custom_font_bold) {
            return BOLD;
        } else if (mFontType == R.string.custom_font_black) {
            return BLACK;
        } else {
            return 0;
        }
    }
}
