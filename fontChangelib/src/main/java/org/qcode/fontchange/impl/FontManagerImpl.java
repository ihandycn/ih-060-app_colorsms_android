package org.qcode.fontchange.impl;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.provider.FontRequest;
import android.support.v4.provider.FontsContractCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.iflytek.android_font_loader_lib.R;
import com.superapps.view.TypefacedTextView;

import org.qcode.fontchange.FontManager;
import org.qcode.fontchange.IFontChangeListener;
import org.qcode.fontchange.base.observable.INotifyUpdate;
import org.qcode.fontchange.base.observable.Observable;
import org.qcode.fontchange.base.utils.Logging;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 字体大小调节加载管理类对外实现接口
 */
public class FontManagerImpl extends FontManager {

    private static final String TAG = "FontManagerImpl";

    //单例相关
    private static volatile FontManagerImpl mInstance;
    private String mFontPath;
    private Handler mHandler = null;
    private Typeface mTypeface_thin = null, mTypeface_light = null,
            mTypeface_regular = null, mTypeface_medium = null,
            mTypeface_bold = null, mTypeface_black = null;
    private int weights[] = {100,300,400,500,700,900};
    private boolean isTypefaceRetrieved = false;

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

    public void applyFont(TypefacedTextView textView) {
        if (null == textView) {
            Logging.d(TAG, "applyFont()| view is null");
            return;
        }

        if (!textView.fontChangeable()){
            return;
        }
        /*FontSizeAttr sizeAttr = ViewFontTagHelper.getFontAttr(textView);
        if (null != sizeAttr) {
            sizeAttr.apply(textView, mScale);
        }*/
        textView.setTextSize(mScale * 36);
    }

    public void applyFont(View view, boolean applyChild) {
        if (view instanceof TypefacedTextView) {
            applyFont((TypefacedTextView) view);
            changeTypeFaced((TypefacedTextView) view);
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
            if (!fontPath.isEmpty()){
                requestDownload(fontPath);
            }
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

    private void changeTypeFaced(TypefacedTextView textView) {
        // use google font replace local font
        // fontPath is changed to fontName
        if (isTypefaceRetrieved && textView.fontChangeable()){
            int weight = textView.getFontStyle() * 100;
            switch (weight) {
                case 100:
                    textView.setTypeface(mTypeface_thin);
                    break;
                case 300:
                    textView.setTypeface(mTypeface_light);
                    break;
                case 400:
                    textView.setTypeface(mTypeface_regular);
                    break;
                case 500:
                    textView.setTypeface(mTypeface_medium);
                    break;
                case 700:
                    textView.setTypeface(mTypeface_bold);
                    break;
                case 900:
                    textView.setTypeface(mTypeface_black);
                    break;
            }
        }
    }

    private void requestDownload(String familyName) {
        for (final int weight : weights) {
            QueryBuilder queryBuilder = new QueryBuilder(familyName)
                    .withWidth(100f)
                    .withWeight(weight)
                    .withItalic(0.0f)
                    .withBestEffort(true);
            final String query = queryBuilder.build();

            Log.d(TAG, "Requesting a font. Query: " + query);
            FontRequest request = new FontRequest(
                    "com.google.android.gms.fonts",
                    "com.google.android.gms",
                    query,
                    R.array.com_google_android_gms_fonts_certs);


            FontsContractCompat.FontRequestCallback callback = new FontsContractCompat
                    .FontRequestCallback() {
                @Override
                public void onTypefaceRetrieved(Typeface typeface) {
                    Log.i(TAG, "onTypefaceRetrieved: "+ query);
                    // save typeface to local
                    isTypefaceRetrieved = true;
                    switch (weight) {
                        case 100:
                            mTypeface_thin = typeface;
                            break;
                        case 300:
                            mTypeface_light = typeface;
                            break;
                        case 400:
                            mTypeface_regular = typeface;
                            break;
                        case 500:
                            mTypeface_medium = typeface;
                            break;
                        case 700:
                            mTypeface_bold = typeface;
                            break;
                        case 900:
                            mTypeface_black = typeface;
                            break;
                    }
                    refreshFont();
                }

                @Override
                public void onTypefaceRequestFailed(int reason) {
                    Toast.makeText(mContext,
                            mContext.getString(R.string.request_failed, reason), Toast.LENGTH_LONG)
                            .show();
                }
            };
            FontsContractCompat
                    .requestFont(mContext, request, callback,
                            getHandlerThreadHandler());
        }
    }

    private Handler getHandlerThreadHandler() {
        if (mHandler == null) {
            HandlerThread handlerThread = new HandlerThread("fonts");
            handlerThread.start();
            mHandler = new Handler(handlerThread.getLooper());
        }
        return mHandler;
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
