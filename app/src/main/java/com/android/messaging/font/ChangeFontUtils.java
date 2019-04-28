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

import static com.android.messaging.font.FontDownloadManager.LOCAL_DIRECTORY;
import static com.superapps.font.FontUtils.sSupportGoogleFonts;

public class ChangeFontUtils {

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


    public static void changeFontTypeface(View view, String typeName) {
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
                    changeFontTypeface(viewGroup.getChildAt(i), typeName);
                }
            }
        }
    }

    private static void changeTypeface(MessagesTextView textView, String typeName) {
        if (textView.fontFamilyChangeable()) {
            int weight = textView.getFontWeight();
            boolean isLocalFont = false;
            for (String s : sSupportGoogleFonts) {
                if (s.equals(typeName)) {
                    isLocalFont = true;
                    break;
                }
            }
            //local fonts
            if (isLocalFont) {
                if (typeName.equals(FontUtils.MESSAGE_FONT_FAMILY_DEFAULT_VALUE)) {
                    typeName = "Custom";
                }
                switch (weight) {
                    case 100:
                    case 300:
                    case 400:
                        Typeface tp = Typeface.createFromAsset(Factory.get().getApplicationContext().getAssets(),
                                "fonts/" + typeName + "-Regular.ttf");
                        textView.setTypeface(tp);
                        break;
                    case 500:
                        Typeface tp1 = Typeface.createFromAsset(Factory.get().getApplicationContext().getAssets(),
                                "fonts/" + typeName + "-Medium.ttf");
                        textView.setTypeface(tp1);
                        break;
                    case 600:
                    case 700:
                    case 900:
                        Typeface tp2 = Typeface.createFromAsset(Factory.get().getApplicationContext().getAssets(),
                                "fonts/" + typeName + "-Semibold.ttf");
                        textView.setTypeface(tp2);
                        break;
                }
            } else {
                switch (weight) {
                    case FontUtils.MEDIUM:
                        File mediumFile = new File(CommonUtils.getDirectory(LOCAL_DIRECTORY + typeName), "Medium.ttf");
                        if (mediumFile.exists()) {
                            textView.setTypeface(Typeface.createFromFile(mediumFile));
                            break;
                        }
                    case FontUtils.SEMI_BOLD:
                    case FontUtils.BOLD:
                    case FontUtils.BLACK:
                        File semiBoldFile = new File(CommonUtils.getDirectory(LOCAL_DIRECTORY + typeName), "SemiBold.ttf");
                        if (semiBoldFile.exists()) {
                            textView.setTypeface(Typeface.createFromFile(semiBoldFile));
                            break;
                        }
                    case FontUtils.THIN:
                    case FontUtils.EXTRA_LIGHT:
                    case FontUtils.LIGHT:
                    case FontUtils.REGULAR:
                        File regularFile = new File(CommonUtils.getDirectory(LOCAL_DIRECTORY + typeName), "Regular.ttf");
                        textView.setTypeface(Typeface.createFromFile(regularFile));
                        break;
                }
            }
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
