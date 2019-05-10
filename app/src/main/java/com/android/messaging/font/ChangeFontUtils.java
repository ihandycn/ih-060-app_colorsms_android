package com.android.messaging.font;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.Factory;
import com.android.messaging.util.CommonUtils;
import com.ihs.commons.utils.HSLog;
import com.superapps.font.FontUtils;
import com.superapps.view.MessagesTextView;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.android.messaging.font.FontDownloadManager.LOCAL_DIRECTORY;
import static com.superapps.font.FontUtils.sSupportGoogleFonts;

public class ChangeFontUtils {
    public static Map<String, Typeface> sTypefaceMap = new HashMap<>();

    public static void changeFontSize(View view, float scale) {
        if (view instanceof MessagesTextView) {
            changeTextSize((MessagesTextView) view, scale);
        } else {
            if (view instanceof RecyclerView) {
                clearRecyclerView((RecyclerView) view);
            }

            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    changeFontSize(viewGroup.getChildAt(i), scale);
                }
            }
        }
    }

    private static void changeTextSize(MessagesTextView textView, float mScale) {
        if (null == textView) {
            return;
        }
        if (!textView.fontSizeChangeable()) {
            return;
        }
        textView.setTextScale(mScale);
    }

    public static void changeFontTypeface(View view, String typename) {
        sTypefaceMap.clear();
        loadAndChangeFontTypeface(view, typename);
        sTypefaceMap.clear();
    }

    private static void loadAndChangeFontTypeface(View view, String typeName) {
        if (view instanceof MessagesTextView) {
            if (((MessagesTextView) view).getText().length() > 0) {
                changeTypeface((MessagesTextView) view, typeName);
            }
        } else {
            if (view instanceof RecyclerView) {
                clearRecyclerView((RecyclerView) view);
            }

            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    loadAndChangeFontTypeface(viewGroup.getChildAt(i), typeName);
                }
            }
        }
    }

    private static void changeTypeface(MessagesTextView textView, String typeName) {
        if (textView.fontFamilyChangeable()) {
            int weight = textView.getFontWeight();
            String weightStr;
            switch (weight) {
                case FontUtils.MEDIUM:
                    weightStr = "Medium";
                    break;
                case FontUtils.SEMI_BOLD:
                case FontUtils.BOLD:
                case FontUtils.BLACK:
                    weightStr = "Semibold";
                    break;
                default:
                    weightStr = "Regular";
            }

            if (sTypefaceMap.containsKey(weightStr)) {
                if (sTypefaceMap.get(weightStr) != null) {
                    textView.setTypeface(sTypefaceMap.get(weightStr));
                }
                return;
            }

            Typeface tp = FontUtils.loadTypeface(typeName, weightStr);
            if (tp != null) {
                sTypefaceMap.put(weightStr, tp);
            }
            textView.setTypeface(tp);
        }
    }

    private static void clearRecyclerView(RecyclerView recyclerView) {
        HSLog.d("-->>", "refreshRecyclerView()| clear recycler view");
        Class<RecyclerView> recyclerViewClass = RecyclerView.class;
        try {
            Field declaredField = recyclerViewClass.getDeclaredField("mRecycler");
            declaredField.setAccessible(true);
            Method declaredMethod = Class.forName(RecyclerView.Recycler.class.getName()).getDeclaredMethod("clear", (Class<?>[]) new Class[0]);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(declaredField.get(recyclerView));
            RecyclerView.RecycledViewPool recycledViewPool = recyclerView.getRecycledViewPool();
            recycledViewPool.clear();

        } catch (Exception ex) {
            HSLog.d("-->>", "refreshRecyclerView()| error happened:\n" + ex);
        }
    }
}
