package com.android.messaging.privatebox.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import com.android.messaging.R;
import com.android.messaging.privatebox.PrivateBoxSettings;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.ViewUtils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.List;

public class GestureLockView extends ViewGroup {

    private boolean isPathHide;

    public class CycleInterpolator implements Interpolator {
        private float mCycles;

        public CycleInterpolator(float cycles) {
            mCycles = cycles;
        }

        @Override
        public float getInterpolation(float input) {
            return (float) (Math.sin(2 * mCycles * Math.PI * input) * Math.exp(-2 * input));
        }
    }

    public CycleInterpolator nodeCycle;

    private class NodeView extends AppCompatImageView {

        private static final int STATE_NORMAL = 0;
        private static final int STATE_HIGHLIGHT = 1;
        private static final int STATE_FAILURE = 2;

        public static final float ANI_DURATION = 500;
        public long startTime;
        public boolean duringAni;

        private int num;
        private int state;

        public NodeView(Context context, int num) {
            super(context);
            this.num = num;
            this.startTime = -1;
            this.duringAni = false;
            setScaleType(ScaleType.CENTER_CROP);
            setImageDrawable(nodeDrawable);
        }

        public boolean isHighLighted() {
            return STATE_HIGHLIGHT == state;
        }

        public void setState(int state) {
            this.state = state;

            switch (state) {

                case STATE_NORMAL:
                    setImageDrawable(nodeDrawable);
                    break;

                case STATE_HIGHLIGHT:
                    setImageDrawable(nodeHighlightDrawable);
                    break;

                case STATE_FAILURE:
                    setImageDrawable(nodeFailureDrawable);
                    break;

                default:
                    break;
            }
        }

        public int getCenterX() {
            return (getLeft() + getRight()) / 2;
        }

        public int getCenterY() {
            return (getTop() + getBottom()) / 2;
        }

        public int getNum() {
            return num;
        }

    }

    private enum State {
        NORMAL,
        FAILURE,
        SUCCESS,
        DISABLE,
    }

    private static final int DURATION_SUCCESS_DISPLAY = 1000;
    private static final int DURATION_FAILURE_DISPLAY = 500;

    private List<Pair<NodeView, NodeView>> lineList = new ArrayList<Pair<NodeView, NodeView>>(); // 已经连线的节点链表
    private NodeView currentNode; // 最近一个点亮的节点，null表示还没有点亮任何节点

    private float x; // 当前手指坐标x
    private float y; // 当前手指坐标y
    private float downX; // 手指首次触摸坐标x
    private boolean isDrawPattern = true;

    private Drawable nodeDrawable;
    private Drawable nodeHighlightDrawable;
    private Drawable nodeFailureDrawable;

    private int lineColor;
    private int failureColor;

    private float lineWidth;
    private float padding; // 内边距
    private float spacing; // 节点间隔距离
    private Rect rect;

    private Paint normalPaint;
    private Paint failurePaint;
    private StringBuilder passwordBuilder = new StringBuilder();

    public interface OnGestureFinishListener {
        void onGestureLayoutFinished(int topMargin);

        void onSetPasswordFinished(String password);

        void onPasswordVerifyFinished(boolean result, String password);
    }

    private OnGestureFinishListener onGestureFinishListener;
    private String verifyPassword;
    private State state;
    private Vibrator vibrator;
    private boolean isSetPasswordOrAppUnLock;

    private Runnable resetRunnable = new Runnable() {
        @Override
        public void run() {
            resetNormal();
        }
    };

    private Runnable disableRunnable = new Runnable() {
        @Override
        public void run() {
            resetDisable();
        }
    };

    public GestureLockView(Context context) {
        this(context, null);
    }

    public GestureLockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GestureLockView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        nodeCycle = new CycleInterpolator(1);
        initFromAttributes(attrs, defStyleAttr);
    }

    public void setOnGestureFinishListener(OnGestureFinishListener onGestureFinishListener) {
        this.onGestureFinishListener = onGestureFinishListener;
    }

    public void setVerifyPassword(String password) {
        this.verifyPassword = password;
    }

    public void setPasswordOrAppUnLock(boolean isSetPasswordOrAppUnLock) {
        this.isSetPasswordOrAppUnLock = isSetPasswordOrAppUnLock;
    }

    private void initFromAttributes(AttributeSet attrs, int defStyleAttr) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.GestureLockView, defStyleAttr, 0);

        nodeDrawable = typedArray.getDrawable(R.styleable.GestureLockView_gesture_lock_node_image);
        nodeHighlightDrawable = typedArray.getDrawable(R.styleable.GestureLockView_gesture_lock_node_highlight_image);
        nodeHighlightDrawable.setColorFilter(new PorterDuffColorFilter(PrimaryColors.getPrimaryColor(), PorterDuff.Mode.MULTIPLY));
        nodeFailureDrawable = typedArray.getDrawable(R.styleable.GestureLockView_gesture_lock_node_failure_image);
        //lineColor = typedArray.getColor(R.styleable.GestureLockView_gesture_lock_normal_line_color, Color.argb(0, 0, 0, 0));
        lineColor = PrimaryColors.getPrimaryColor();
        failureColor = typedArray.getColor(R.styleable.GestureLockView_gesture_lock_failure_line_color, Color.argb(0, 0, 0, 0));
        lineWidth = typedArray.getDimension(R.styleable.GestureLockView_gesture_lock_line_width, 0);
        padding = typedArray.getDimension(R.styleable.GestureLockView_gesture_lock_padding, 0);
        spacing = typedArray.getDimension(R.styleable.GestureLockView_gesture_lock_spacing, 0);

        typedArray.recycle();

        normalPaint = new Paint(Paint.DITHER_FLAG);
        normalPaint.setStyle(Style.STROKE);
        normalPaint.setStrokeWidth(lineWidth);
        normalPaint.setColor(lineColor);
        normalPaint.setAntiAlias(true); // 抗锯齿

        failurePaint = new Paint(Paint.DITHER_FLAG);
        failurePaint.setStyle(Style.STROKE);
        failurePaint.setStrokeWidth(lineWidth);
        failurePaint.setColor(failureColor);
        failurePaint.setAntiAlias(true); // 抗锯齿

        // 构建node
        for (int i = 0; i < 9; ++i) {
            addView(new NodeView(getContext(), i));
        }

        // 清除FLAG，否则onDraw() 不会调用，原因是 ViewGroup 默认透明背景不需要调用 onDraw()
        setWillNotDraw(false);

        state = State.NORMAL;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!changed) {
            return;
        }

        int nodeWidth = (int) ((right - left - padding * 2 - spacing * 2) / 3);
        int viewHeight = getMeasuredHeight();
        int topMargin = (viewHeight - nodeWidth * 3 - (int) spacing * 2) / 2;

        for (int i = 0; i < 9; ++i) {
            NodeView node = (NodeView) getChildAt(i);

            // 获取3*3宫格内坐标
            int row = i / 3;
            int col = i % 3;

            // 计算实际的坐标，要包括内边距和分割边距
            int l = (int) (padding + col * (nodeWidth + spacing));
            int t = (int) (topMargin + row * (nodeWidth + spacing));
            int r = l + nodeWidth;
            int b = t + nodeWidth;
            node.layout(l, t, r, b);
        }

        int leftPadding = Dimensions.pxFromDp(42);
        int topPadding = Dimensions.pxFromDp(24);
        rect = new Rect(leftPadding, topPadding, right - left - leftPadding, bottom - top - topPadding);
        if (null != onGestureFinishListener) {
            onGestureFinishListener.onGestureLayoutFinished(topMargin);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (State.FAILURE == state || State.SUCCESS == state) {
            //            return true;
            post(resetRunnable);
        } else if (State.DISABLE == state) {
            return true;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                isDrawPattern = true;
                downX = event.getX();
                onTouchDown(event.getX());
            case MotionEvent.ACTION_MOVE:
                if (!isDrawPattern) {
                    break;
                }

                onTouchMove(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_UP:
                onTouchUp();
                break;

            default:
                break;
        }
        return true;
    }

    private void onTouchDown(float tX) {
        isDrawPattern = true;
        downX = tX;
    }

    private void onTouchMove(float tX, float tY) {
        x = tX;
        y = tY;
        NodeView nodeAt = getNodeAt(x, y);
        if (currentNode == null) { // 之前没有点
            if (nodeAt != null) { // 第一个点
                currentNode = nodeAt;

                currentNode.setState(NodeView.STATE_HIGHLIGHT);
                //动画设置
                currentNode.duringAni = true;
                currentNode.startTime = System.currentTimeMillis();
                invalidate(); // 通知重绘

                passwordBuilder.append(currentNode.getNum());
                vibrate(30);
            } else {
                double distance = Math.abs(downX - x);
                if (distance > spacing) {
                    isDrawPattern = false;
                }
            }
        } else { // 之前有点-所以怎么样都要重绘
            if (nodeAt != null && !nodeAt.isHighLighted()) { // 当前碰触了新点

                NodeView center = getCenterNode(currentNode.getNum(), nodeAt.getNum());
                if (null != center) {
                    center.setState(NodeView.STATE_HIGHLIGHT);
                    Pair<NodeView, NodeView> pair = new Pair<NodeView, NodeView>(currentNode, center);
                    lineList.add(pair);
                    currentNode = center;
                    passwordBuilder.append(currentNode.getNum());
                }

                nodeAt.setState(NodeView.STATE_HIGHLIGHT);
                Pair<NodeView, NodeView> pair = new Pair<NodeView, NodeView>(currentNode, nodeAt);
                lineList.add(pair);
                // 赋值当前的node
                currentNode = nodeAt;
                //设置动画
                currentNode.duringAni = true;
                currentNode.startTime = System.currentTimeMillis();
                passwordBuilder.append(currentNode.getNum());
                vibrate(30);
            }
            invalidate(); // 通知重绘
        }
    }

    private void onTouchUp() {
        isDrawPattern = false;
        if (passwordBuilder.length() <= 0) {
            return;
        }

        // 回调结果
        if (TextUtils.isEmpty(verifyPassword)) {
            String password = null;
            if (passwordBuilder.length() < 4) {
                state = State.FAILURE;
                setCurrentFailure();
                postDelayed(resetRunnable, DURATION_FAILURE_DISPLAY);
            } else {
                password = passwordBuilder.toString();
                if (isSetPasswordOrAppUnLock) {
                    state = State.SUCCESS;
                    verifyPassword = password;
                    postDelayed(resetRunnable, DURATION_SUCCESS_DISPLAY);
                    invalidate();
                } else {
                    if (password.equals(PrivateBoxSettings.getUnlockGesture())) {
                        state = State.SUCCESS;
                        postDelayed(resetRunnable, DURATION_SUCCESS_DISPLAY);
                        invalidate();
                    } else {
                        state = State.FAILURE;
                        setCurrentFailure();
                        postDelayed(resetRunnable, DURATION_FAILURE_DISPLAY);
                    }
                }
            }

            if (onGestureFinishListener != null) {
                onGestureFinishListener.onSetPasswordFinished(password);
            }

        } else {
            String password = passwordBuilder.toString();
            HSLog.i("GestureLockView", "ACTION_UP, password = " + password);
            boolean correct = password.equals(verifyPassword);

            if (isSetPasswordOrAppUnLock) {
                if (!correct) {
                    state = State.FAILURE;
                    setCurrentFailure();

                    postDelayed(resetRunnable, DURATION_FAILURE_DISPLAY);
                } else {
                    state = State.SUCCESS;
                    postDelayed(resetRunnable, DURATION_SUCCESS_DISPLAY);
                }
            } else {
                if (password.equals(PrivateBoxSettings.getUnlockGesture())) {
                    state = State.SUCCESS;
                    postDelayed(resetRunnable, DURATION_SUCCESS_DISPLAY);
                    invalidate();
                } else {
                    state = State.FAILURE;
                    setCurrentFailure();
                    postDelayed(resetRunnable, DURATION_FAILURE_DISPLAY);
                }
            }

            if (onGestureFinishListener != null) {
                onGestureFinishListener.onPasswordVerifyFinished(correct, password);
            }
        }
    }

    private void vibrate(long timeInMs) {
        if (null == vibrator) {
            vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        }
        try {
            vibrator.vibrate(timeInMs);
        } catch (SecurityException | NullPointerException exception) {
        }
    }

    private void resetInternal() {
        HSLog.i("GestureLockView", "reset");
        removeCallbacks(resetRunnable);

        // 清空状态
        lineList.clear();
        currentNode = null;
        passwordBuilder.setLength(0);

        // 清除高亮
        for (int i = 0; i < getChildCount(); i++) {
            NodeView node = (NodeView) getChildAt(i);
            node.setState(NodeView.STATE_NORMAL);
        }

        // 通知重绘
        invalidate();
    }

    public void setCurrentFailure() {
        for (int i = 0; i < passwordBuilder.length(); ++i) {
            int childId = Integer.valueOf(passwordBuilder.substring(i, i + 1));
            NodeView node = (NodeView) getChildAt(childId);
            if (null != node) {
                node.setState(NodeView.STATE_FAILURE);
            }
        }
        invalidate();
    }

    private void resetNormal() {
        if (State.DISABLE != state) {
            state = State.NORMAL;
        }
        resetInternal();
    }

    private void resetDisable() {
        state = State.DISABLE;
        resetInternal();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //首先判读那 node 的动画是否结束
        for (int i = 0; i < 9; ++i) {
            NodeView node = (NodeView) getChildAt(i);
            if (node.duringAni) {
                //检查时间
                long timeGoes = System.currentTimeMillis() - node.startTime;
                if (timeGoes >= NodeView.ANI_DURATION) {
                    ViewUtils.setViewScale(node, 1.0f);
                    node.duringAni = false;
                    node.startTime = -1;
                } else {
                    // 计算实际的坐标，要包括内边距和分割边距
                    float time = timeGoes / NodeView.ANI_DURATION;
                    float percent;
                    if (time > 0.5) {
                        percent = nodeCycle.getInterpolation(time) * 0.25f + 1;
                    } else {
                        percent = nodeCycle.getInterpolation(time) * 0.4f + 1;
                    }
                    ViewUtils.setViewScale(node, percent);
                }
            }
        }
        Paint paint = null;
        switch (state) {
            case NORMAL:
            case SUCCESS:
                paint = normalPaint;
                break;
            case DISABLE:
            case FAILURE:
                paint = failurePaint;
                break;
            default:
                break;
        }
        if (null == paint) {
            return;
        }

        if (!isPathHide) {
            // 先绘制已有的连线
            for (Pair<NodeView, NodeView> pair : lineList) {
                canvas.drawLine(pair.first.getCenterX(), pair.first.getCenterY(), pair.second.getCenterX(), pair.second.getCenterY(), paint);
            }
            // 如果已经有点亮的点，则在点亮点和手指位置之间绘制连线
            if (null != currentNode && State.NORMAL == state) {
                canvas.drawLine(currentNode.getCenterX(), currentNode.getCenterY(), x, y, paint);
            }
        }

        postInvalidate();
    }

    /**
     * 获取给定坐标点的Node，返回null表示当前手指在两个Node之间
     */
    private NodeView getNodeAt(float x, float y) {
        for (int n = 0; n < getChildCount(); n++) {
            NodeView node = (NodeView) getChildAt(n);
            if (!(x >= node.getLeft() && x < node.getRight())) {
                continue;
            }
            if (!(y >= node.getTop() && y < node.getBottom())) {
                continue;
            }
            return node;
        }
        return null;
    }

    /**
     * 获取当前点与之前点之间是否有其他未选中的点，null表示没有
     */
    private NodeView getCenterNode(int lastIndex, int curIndex) {
        NodeView node = null;
        switch (lastIndex) {
            case 0:
                switch (curIndex) {
                    case 2:
                        node = (NodeView) getChildAt(1);
                        break;
                    case 6:
                        node = (NodeView) getChildAt(3);
                        break;
                    case 8:
                        node = (NodeView) getChildAt(4);
                        break;
                    default:
                        break;
                }
                break;
            case 1:
                if (curIndex == 7) {
                    node = (NodeView) getChildAt(4);
                }
                break;
            case 2:
                switch (curIndex) {
                    case 0:
                        node = (NodeView) getChildAt(1);
                        break;
                    case 6:
                        node = (NodeView) getChildAt(4);
                        break;
                    case 8:
                        node = (NodeView) getChildAt(5);
                        break;
                    default:
                        break;
                }
                break;
            case 3:
                if (curIndex == 5) {
                    node = (NodeView) getChildAt(4);
                }
                break;
            case 4:
                // none
                break;
            case 5:
                if (curIndex == 3) {
                    node = (NodeView) getChildAt(4);
                }
                break;
            case 6:
                switch (curIndex) {
                    case 0:
                        node = (NodeView) getChildAt(3);
                        break;
                    case 2:
                        node = (NodeView) getChildAt(4);
                        break;
                    case 8:
                        node = (NodeView) getChildAt(7);
                        break;
                    default:
                        break;
                }
                break;
            case 7:
                if (curIndex == 1) {
                    node = (NodeView) getChildAt(4);
                }
                break;
            case 8:
                switch (curIndex) {
                    case 0:
                        node = (NodeView) getChildAt(4);
                        break;
                    case 2:
                        node = (NodeView) getChildAt(5);
                        break;
                    case 6:
                        node = (NodeView) getChildAt(7);
                        break;
                    default:
                        break;
                }
                break;

            default:
                break;
        }

        if (null != node && node.isHighLighted()) {
            node = null;
        }
        return node;
    }

    public void hidePath(boolean isHide) {
        this.isPathHide = isHide;
    }
}
