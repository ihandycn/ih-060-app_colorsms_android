package org.qcode.fontchange.impl;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.qcode.fontchange.FontManager;
import org.qcode.fontchange.FontSizeAttr;
import org.qcode.fontchange.IFontChangeListener;
import org.qcode.fontchange.base.observable.INotifyUpdate;
import org.qcode.fontchange.base.observable.Observable;
import org.qcode.fontchange.base.utils.Logging;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 字体大小调节加载管理类对外实现接口
 * qqliu
 * 2016/9/24.
 */
public class FontManagerImpl extends FontManager {

    private static final String TAG = "FontManagerImpl";

    //单例相关
    private static volatile FontManagerImpl mInstance;
    private String mFontPath;

    private FontManagerImpl() {
    }

    public static FontManagerImpl getInstance() {
        if (null == mInstance) {
            synchronized (FontManagerImpl.class) {
                if (null == mInstance) {
                    mInstance = new FontManagerImpl();
                }
            }
        }
        return mInstance;
    }

    private Context mContext;

    private float mScale = 1.0f;

    private Observable<ActivityFontEventHandlerImpl> mObservable;

    public void init(Context context) {
        mContext = context.getApplicationContext();
        mObservable = new Observable<ActivityFontEventHandlerImpl>();
    }

    public void changeFontSize(float scale, IFontChangeListener fontChangeListener) {
        if (null != fontChangeListener) {
            fontChangeListener.onLoadStart(scale);
        }

        float tmpScale = mScale;
        mScale = scale;

        try {
            refreshFont();
            if (fontChangeListener != null) {
                fontChangeListener.onLoadSuccess(scale);
            }
        } catch (Exception ex) {
            Logging.d(TAG, "changeFontSize()| error happened", ex);
            mScale = tmpScale;

            if (fontChangeListener != null) {
                fontChangeListener.onLoadFail(scale);
            }
        }
    }

    public void applyFont(TextView textView) {
        if (null == textView) {
            Logging.d(TAG, "applyFont()| view is null");
            return;
        }

        /*FontSizeAttr sizeAttr = ViewFontTagHelper.getFontAttr(textView);
        if (null != sizeAttr) {
            sizeAttr.apply(textView, mScale);
        }*/
        textView.setTextSize(mScale * 36);
    }

    public void applyFont(View view, boolean applyChild) {
        if (view instanceof TextView) {
            applyFont((TextView) view);
            changeTypeFaced((TextView) view);
        } else {
            if (view instanceof RecyclerView) {
                clearRecyclerView((RecyclerView) view);
            }

            if (applyChild) {
                if (view instanceof ViewGroup) {
                    //遍历子元素应用字体大小调节
                    ViewGroup viewGroup = (ViewGroup) view;
                    for (int i = 0; i < viewGroup.getChildCount(); i++) {
                        applyFont(viewGroup.getChildAt(i), true);
                    }
                }
            }
        }
    }

    public void changeTypeFaced(String fontPath, IFontChangeListener fontChangeListener) {
        if (null != fontChangeListener) {
            fontChangeListener.onLoadStart(fontPath);
        }

        mFontPath = fontPath;

        try {
            refreshFont();
            if (fontChangeListener != null) {
                fontChangeListener.onLoadSuccess(fontPath);
            }
        } catch (Exception ex) {
            Logging.d(TAG, "changeFontSize()| error happened", ex);

            if (fontChangeListener != null) {
                fontChangeListener.onLoadFail(fontPath);
            }
        }
    }

    private void changeTypeFaced(TextView textView) {
        if (mFontPath != null) {
            Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), mFontPath);
            textView.setTypeface(typeface);
        }
    }

    public float getFontScale() {
        return mScale;
    }

    public String getTypeFaced() {
        return mFontPath;
    }

    public void addObserver(ActivityFontEventHandlerImpl observer) {
        mObservable.addObserver(observer);
    }

    public void removeObserver(ActivityFontEventHandlerImpl observer) {
        mObservable.removeObserver(observer);
    }

    public void notifyUpdate(INotifyUpdate<ActivityFontEventHandlerImpl> callback, String identifier, Object... params) {
        mObservable.notifyUpdate(callback, identifier, params);
    }

    /***
     * 告知外部观察者当前字体大小发生了变化
     */
    private void refreshFont() {
        notifyUpdate(new INotifyUpdate<ActivityFontEventHandlerImpl>() {
            @Override
            public boolean notifyEvent(
                    ActivityFontEventHandlerImpl handler,
                    String identifier,
                    Object... params) {
                handler.handleFontUpdate();
                return false;
            }
        }, null);
    }

    private void clearRecyclerView(RecyclerView recyclerView) {
        Logging.d(TAG, "refreshRecyclerView()| clear recycler view");
        Class<RecyclerView> recyclerViewClass = RecyclerView.class;
        try {
            Field declaredField = recyclerViewClass.getDeclaredField("mRecycler");
            declaredField.setAccessible(true);
            Method declaredMethod = Class.forName(RecyclerView.Recycler.class.getName()).getDeclaredMethod("clear", (Class<?>[]) new Class[0]);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(declaredField.get(recyclerView), new Object[0]);
            RecyclerView.RecycledViewPool recycledViewPool = recyclerView.getRecycledViewPool();
            recycledViewPool.clear();

        } catch (Exception ex) {
            Logging.d(TAG, "refreshRecyclerView()| error happened", ex);
        }
    }
}
