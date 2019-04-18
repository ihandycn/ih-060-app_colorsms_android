package com.android.messaging.ui.customize;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.android.messaging.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 取色器
 * <p>
 * 所有注释单位为dp的全局变量，初始都是dp值，在使用之前会乘上屏幕像素(mDensity)称为px值
 * https://github.com/relish-wang/ColorPicker
 * @author Relish Wang
 * @since 2017/08/02
 */
public class PaletteView extends View {

    @IntDef({PANEL.SAT_VAL, PANEL.HUE})
    @Retention(RetentionPolicy.SOURCE)
    @interface PANEL {
        int SAT_VAL = 0;
        int HUE = 1;
    }

    private static final float DEFAULT_HUE = 360f;
    private static final float DEFAULT_SAT = 0f;
    private static final float DEFAULT_VAL = 0f;

    /**
     * 显示H、SV的矩形的边框粗细（单位：dp）
     */
    private final static float BORDER_WIDTH = 1;
    /**
     * H矩形的宽度（单位：dp）
     */
    private float mHuePanelWidth = 6.7f;
    /**
     * H、SV矩形间的间距（单位：dp）
     */
    private float mPanelSpacing = 29f;
    /**
     * 当mode为MeasureSpec.UNSPECIFIED时的首选高度（单位：dp）
     */
    private float mPreferredHeight = 200;
    /**
     * 当mode为MeasureSpec.UNSPECIFIED时的首选宽度（单位：dp）
     */
    private float mPreferredWidth = mPreferredHeight + mHuePanelWidth + mPanelSpacing;
    /**
     * SV指示器的半径（单位：dp）
     */
    private float mSVTrackerRadius = 7.3f;
    /**
     * H指示器的宽高（单位：dp）
     */
    private float mHTrackerSize = 23.5f;
    /**
     * H、SV矩形与父布局的边距（单位：dp）
     */
    private float mRectOffset = 2f;

    /**
     * SV 矩形的圆角
     */
    private float mRadius = 6.7f;

    /**
     * 屏幕密度
     */
    private float mDensity = 1f;
    /**
     * 绘制SV的画笔
     */
    private Paint mSatValPaint;
    /**
     * 绘制SV指示器的画笔
     */
    private Paint mSatValTrackerPaint;

    /**
     * 绘制H的画笔
     */
    private Paint mHuePaint;
    /**
     * 绘制H指示器的画笔
     */
    private Paint mHueTrackerPaint;

    /**
     * 绘制Hue指示器的图形
     */
    private Bitmap mHueTrackerBitmap;

    //H、V着色器
    private Shader mHueShader;
    private Shader mValShader;

    //HSV的默认值
    private float mHue = DEFAULT_HUE;
    private float mSat = DEFAULT_SAT;
    private float mVal = DEFAULT_VAL;

    /**
     * 用于显示被选择H的位置的指示器的颜色
     */
    private int mSliderTrackerColor = Color.WHITE;

    /**
     * 记录上一次被点击的颜色板
     */
    @PANEL
    private int mLastTouchedPanel = PANEL.SAT_VAL;
    /**
     * 边距
     */
    private float mDrawingOffset;
    /**
     * H指示器
     */
    private RectF mDrawingRect;
    /**
     * 用于选择SV的矩形
     */
    private RectF mSatValRect;
    /**
     * 用于选择H的矩形
     */
    private RectF mHueRect;
    /**
     * SV指示器
     */
    private Point mStartTouchPoint = null;

    private OnColorChangedListener mListener;

    public PaletteView(Context context) {
        this(context, null);
    }

    public PaletteView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaletteView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mDensity = getContext().getResources().getDisplayMetrics().density;//获取屏幕密度
        mSVTrackerRadius *= mDensity;//灰度饱和度指示器的半径
        mHTrackerSize *= mDensity;//色相指示器高度
        mRectOffset *= mDensity;//H、SV矩形与父布局的边距
        mHuePanelWidth *= mDensity;//H矩形的宽度
        mPanelSpacing *= mDensity;//H、SV矩形间的间距
        mPreferredHeight *= mDensity;//当mode为MeasureSpec.UNSPECIFIED时的首选高度
        mPreferredWidth *= mDensity;//当mode为MeasureSpec.UNSPECIFIED时的首选宽度
        mRadius *= mDensity;//H、SV矩形间的圆角弧度

        mDrawingOffset = calculateRequiredOffset();//计算所需位移

        mHueTrackerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_hue_tracker);

        initPaintTools();//初始化画笔、画布

        setFocusable(true);//设置可获取焦点
        setFocusableInTouchMode(true);//设置在被触摸时会获取焦点
    }

    /**
     * mSVTrackerRadius、
     * mRectOffset、
     * BORDER_WIDTH * mDensity
     * 三者的最大值
     * 的1.5倍
     *
     * @return 边距
     */
    private float calculateRequiredOffset() {
        float offset = Math.max(mSVTrackerRadius, mRectOffset);
        offset = Math.max(offset, BORDER_WIDTH * mDensity);
        return offset * 1.5f;
    }

    private void initPaintTools() {
        mSatValPaint = new Paint();
        mSatValTrackerPaint = new Paint();
        mHuePaint = new Paint();
        mHueTrackerPaint = new Paint();

        mSatValPaint.setAntiAlias(true);

        mHuePaint.setAntiAlias(true);

        mSatValTrackerPaint.setStyle(Style.STROKE);
        mSatValTrackerPaint.setStrokeWidth(2f * mDensity);
        mSatValTrackerPaint.setColor(Color.WHITE);
        mSatValTrackerPaint.setAntiAlias(true);

        mHueTrackerPaint.setColor(mSliderTrackerColor);
        mHueTrackerPaint.setShadowLayer(4.0f, 0.0f, 2.0f, Color.BLACK);
        mHueTrackerPaint.setStyle(Style.FILL);
        mHueTrackerPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthAllowed = MeasureSpec.getSize(widthMeasureSpec);
        int heightAllowed = MeasureSpec.getSize(heightMeasureSpec);

        widthAllowed = isUnspecified(widthMode) ? (int) mPreferredWidth : widthAllowed;
        heightAllowed = isUnspecified(heightMode) ? (int) mPreferredHeight : heightAllowed;

        // 85% 的宽度
        widthAllowed = (widthAllowed * 108) >> 7;

        int width = widthAllowed;
        int height = (int) (widthAllowed - mPanelSpacing - mHuePanelWidth);
        //当根据宽度计算出来的高度大于可允许的最大高度时 或 当前是横屏
        if (height > heightAllowed || "landscape".equals(getTag())) {
            height = heightAllowed;
            width = (int) (height + mPanelSpacing + mHuePanelWidth);
        }
        setMeasuredDimension(width, height);
    }

    private static boolean isUnspecified(int mode) {
        return !(mode == MeasureSpec.EXACTLY || mode == MeasureSpec.AT_MOST);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawingRect.width() <= 0 || mDrawingRect.height() <= 0) return;
        drawSatValPanel(canvas);//绘制SV选择区域
        drawHuePanel(canvas);//绘制右侧H选择区域
    }

    /**
     * 绘制S、V选择区域（矩形）
     *
     * @param canvas 画布
     */
    private void drawSatValPanel(Canvas canvas) {

        //组合着色器 = 明度线性着色器 + 饱和度线性着色器
        ComposeShader mShader = generateSVShader();
        mSatValPaint.setShader(mShader);

        canvas.drawRoundRect(mSatValRect, mRadius, mRadius, mSatValPaint);

        //初始化选择器的位置
        Point p = satValToPoint(mSat, mVal);
        canvas.drawCircle(p.x, p.y, mSVTrackerRadius, mSatValTrackerPaint);
    }

    /**
     * 创建SV着色器(明度线性着色器 + 饱和度线性着色器)
     *
     * @return 着色器
     */
    private ComposeShader generateSVShader() {
        //明度线性着色器
        if (mValShader == null) {
            mValShader = new LinearGradient(mSatValRect.left, mSatValRect.top, mSatValRect.left, mSatValRect.bottom,
                    0xffffffff, 0xff000000, TileMode.CLAMP);
        }
        //HSV转化为RGB
        int rgb = Color.HSVToColor(new float[]{mHue, 1f, 1f});
        //饱和线性着色器
        Shader satShader = new LinearGradient(mSatValRect.left, mSatValRect.top, mSatValRect.right, mSatValRect.top,
                0xffffffff, rgb, TileMode.CLAMP);
        //组合着色器 = 明度线性着色器 + 饱和度线性着色器
        return new ComposeShader(mValShader, satShader, PorterDuff.Mode.MULTIPLY);
    }

    /**
     * 绘制右侧H选择区域
     *
     * @param canvas 画布
     */
    private void drawHuePanel(Canvas canvas) {
        final RectF rect = mHueRect;

        //初始化H线性着色器
        if (mHueShader == null) {
            int[] hue = new int[361];
            int count = 0;
            for (int i = hue.length - 1; i >= 0; i--, count++) {
                hue[count] = Color.HSVToColor(new float[]{i, 1f, 1f});
            }
            mHueShader = new LinearGradient(
                    rect.left,
                    rect.top,
                    rect.left,
                    rect.bottom,
                    hue,
                    null,
                    TileMode.CLAMP);
            mHuePaint.setShader(mHueShader);
        }

        canvas.drawRoundRect(rect, mRadius, mRadius, mHuePaint);

        // 选择条
        Point p = hueToPoint(mHue);
        canvas.drawBitmap(mHueTrackerBitmap,
                p.x - mHTrackerSize / 2,
                p.y - mHTrackerSize / 2,
                mSatValTrackerPaint);

    }

    private Point hueToPoint(float hue) {
        final RectF rect = mHueRect;
        final float height = rect.height();

        Point p = new Point();
        p.y = (int) (height - (hue * height / 360f) + rect.top);
        p.x = (int) (rect.left + mHuePanelWidth / 2);
        return p;
    }

    private Point satValToPoint(float sat, float val) {
        final float height = mSatValRect.height();
        final float width = mSatValRect.width();

        Point p = new Point();
        p.x = (int) (sat * width + mSatValRect.left);
        p.y = (int) ((1f - val) * height + mSatValRect.top);
        return p;
    }

    private float[] pointToSatVal(float x, float y) {
        final RectF rect = mSatValRect;
        float[] result = new float[2];

        float width = rect.width();
        float height = rect.height();

        if (x < rect.left) {
            x = 0f;
        } else if (x > rect.right) {
            x = width;
        } else {
            x = x - rect.left;
        }

        if (y < rect.top) {
            y = 0f;
        } else if (y > rect.bottom) {
            y = height;
        } else {
            y = y - rect.top;
        }
        result[0] = 1.f / width * x;
        result[1] = 1.f - (1.f / height * y);
        return result;
    }

    private float pointToHue(float y) {
        final RectF rect = mHueRect;
        float height = rect.height();
        if (y < rect.top) {
            y = 0f;
        } else if (y > rect.bottom) {
            y = height;
        } else {
            y = y - rect.top;
        }
        return 360f - (y * 360f / height);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean isUpdated = false;
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            switch (mLastTouchedPanel) {
                case PANEL.SAT_VAL:
                    float sat, val;
                    sat = mSat + x / 50f;
                    val = mVal - y / 50f;
                    if (sat < 0f) {
                        sat = 0f;
                    } else if (sat > 1f) {
                        sat = 1f;
                    }
                    if (val < 0f) {
                        val = 0f;
                    } else if (val > 1f) {
                        val = 1f;
                    }
                    mSat = sat;
                    mVal = val;
                    isUpdated = true;
                    break;
                case PANEL.HUE:
                    float hue = mHue - y * 10f;
                    if (hue < 0f) {
                        hue = 0f;
                    } else if (hue > 360f) {
                        hue = 360f;
                    }
                    mHue = hue;
                    isUpdated = true;
                    break;
            }
        }
        if (isUpdated) {
            if (mListener != null) {
                mListener.onColorChanged(Color.HSVToColor(new float[]{mHue, mSat, mVal}));
            }
            invalidate();
            return true;
        }
        return super.onTrackballEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isUpdated = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartTouchPoint = new Point((int) event.getX(), (int) event.getY());
                isUpdated = moveTrackersIfNeeded(event);
                break;
            case MotionEvent.ACTION_MOVE:
                isUpdated = moveTrackersIfNeeded(event);
                break;
            case MotionEvent.ACTION_UP:
                mStartTouchPoint = null;
                isUpdated = moveTrackersIfNeeded(event);
                break;
        }
        if (isUpdated) {
            if (mListener != null) {
                mListener.onColorChanged(Color.HSVToColor(new float[]{mHue, mSat, mVal}));
            }
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private boolean moveTrackersIfNeeded(MotionEvent event) {
        if (mStartTouchPoint == null) return false;
        boolean update = false;
        int startX = mStartTouchPoint.x;
        int startY = mStartTouchPoint.y;

        if (isValidHueTouchEvent(startX, startY)) {
            mLastTouchedPanel = PANEL.HUE;
            mHue = pointToHue(event.getY());
            update = true;
        } else if (mSatValRect.contains(startX, startY)) {
            mLastTouchedPanel = PANEL.SAT_VAL;
            float[] result = pointToSatVal(event.getX(), event.getY());
            mSat = result[0];
            mVal = result[1];
            update = true;
        }
        return update;
    }

    private boolean isValidHueTouchEvent(int x, int y) {
        return mHueRect.left < mHueRect.right && mHueRect.top < mHueRect.bottom  // check for empty first
                && x >= mHueRect.left - 10f * mDensity
                && x < mHueRect.right +  10f * mDensity
                && y >= mHueRect.top - 10f * mDensity
                && y < mHueRect.bottom + 10f * mDensity;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawingRect = new RectF();
        mDrawingRect.left = mDrawingOffset + getPaddingLeft();
        mDrawingRect.right = w - mDrawingOffset - getPaddingRight();
        mDrawingRect.top = mDrawingOffset + getPaddingTop();
        mDrawingRect.bottom = h - mDrawingOffset - getPaddingBottom();
        setUpSatValRect();
        setUpHueRect();
    }

    private void setUpSatValRect() {
        final RectF dRect = mDrawingRect;
        float panelSide = dRect.height() - BORDER_WIDTH * 2;
        float left = dRect.left + BORDER_WIDTH;
        float top = dRect.top + BORDER_WIDTH;
        float bottom = top + panelSide;
        float right = left + panelSide;
        mSatValRect = new RectF(left, top, right, bottom);
    }

    private void setUpHueRect() {
        final RectF dRect = mDrawingRect;
        float left = dRect.right - mHuePanelWidth ;
        float top = dRect.top;
        float bottom = dRect.bottom;
        float right = dRect.right ;
        mHueRect = new RectF(left, top, right, bottom);
    }

    /**
     * 设置颜色改变监听器
     *
     * @param listener 颜色改变监听器
     */
    public void setOnColorChangedListener(OnColorChangedListener listener) {
        mListener = listener;
    }

    /**
     * 获取当前颜色
     *
     * @return 当前颜色
     */
    public int getColor() {
        return Color.HSVToColor(new float[]{mHue, mSat, mVal});
    }

    /**
     * 设置选择的颜色
     *
     * @param color 被选择的颜色
     */
    public void setColor(@ColorInt int color) {
        setColor(color, false);
    }

    public void reset() {
        setColor(Color.HSVToColor(new float[]{DEFAULT_HUE, DEFAULT_SAT, DEFAULT_VAL}));
    }

    /**
     * 设置被选择的颜色
     *
     * @param color    被选择的颜色
     * @param callback 是否触发OnColorChangedListener
     */
    public void setColor(@ColorInt int color, boolean callback) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        mHue = hsv[0];
        mSat = hsv[1];
        mVal = hsv[2];
        if (callback && mListener != null) {
            mListener.onColorChanged(Color.HSVToColor(new float[]{mHue, mSat, mVal}));
        }
        invalidate();
    }

    /**
     * ColorPickerView的padding
     *
     * @return padding（单位：px）
     */
    public float getDrawingOffset() {
        return mDrawingOffset;
    }

    public void setSliderTrackerColor(int color) {
        mSliderTrackerColor = color;
        mHueTrackerPaint.setColor(mSliderTrackerColor);
        invalidate();
    }

    public int getSliderTrackerColor() {
        return mSliderTrackerColor;
    }
}
