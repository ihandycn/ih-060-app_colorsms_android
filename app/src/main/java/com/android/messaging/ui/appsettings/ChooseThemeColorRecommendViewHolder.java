package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.ui.BasePagerViewHolder;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.customize.OnColorChangedListener;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.theme.ThemeInfo;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;


public class ChooseThemeColorRecommendViewHolder extends BasePagerViewHolder implements CustomPagerViewHolder {

    private static int[] COLOR_IMG_IDS = new int[]{
            R.id.iv_theme_select_1,
            R.id.iv_theme_select_2,
            R.id.iv_theme_select_3,
            R.id.iv_theme_select_4,
            R.id.iv_theme_select_5,
            R.id.iv_theme_select_6,
            R.id.iv_theme_select_7,
            R.id.iv_theme_select_8,
            R.id.iv_theme_select_9,
            R.id.iv_theme_select_10,
            R.id.iv_theme_select_11,
            R.id.iv_theme_select_12
    };

    public static int[] COLORS = new int[]{
            0xff37a63b,
            0xff338ee4,
            0xff0098a6,
            0xffd74315,
            0xff744fdc,
            0xffe54da7,
            0xffba43d4,
            0xffc62827,
            0xfff0ce0f,
            0xff315eaf,
            0xff87c932,
            0xffe8b437
    };

    private Context mContext;
    private OnColorChangedListener mListener;
    private View mRoot;

    public ChooseThemeColorRecommendViewHolder(final Context context) {
        mContext = context;
    }

    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(
                R.layout.choose_theme_color_recommend,
                container /* root */,
                false /* attachToRoot */);
        mRoot = view;

        for (int i = 0; i < COLORS.length; i++) {
            view.findViewById(COLOR_IMG_IDS[i]).setBackground(
                    BackgroundDrawables.createBackgroundDrawable(COLORS[i], Dimensions.pxFromDp(31), true));
            final int k = i;
            view.findViewById(COLOR_IMG_IDS[i]).setOnClickListener(v -> {
                mListener.onColorChanged(COLORS[k]);
                refreshSelectStatus();

            });
        }
        refreshSelectStatus();
        return view;
    }


    void setOnColorChangedListener(OnColorChangedListener listener) {
        mListener = listener;
    }


    void refreshSelectStatus() {
        int primaryColor = PrimaryColors.getPrimaryColor();
        for (int i = 0; i < COLORS.length; i++) {
            if (primaryColor == COLORS[i]) {
                ((ImageView) mRoot.findViewById(COLOR_IMG_IDS[i])).setImageResource(R.drawable.ic_theme_color_selected);
            } else {
                ((ImageView) mRoot.findViewById(COLOR_IMG_IDS[i])).setImageResource(android.R.color.transparent);
            }
        }
    }

    @Override
    protected void setHasOptionsMenu() {

    }

    @Override
    public CharSequence getPageTitle(Context context) {
        return context.getString(R.string.bubble_customize_color_recommend);
    }

    @Override
    public void onPageSelected() {

    }

    public static String getPrimaryColorType() {

        int primaryColor = PrimaryColors.getPrimaryColor();
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == primaryColor) {
                return String.valueOf(i);
            }
        }

        for (ThemeInfo themeInfo : ThemeInfo.getAllThemes()) {
            if (primaryColor == Color.parseColor(themeInfo.themeColor)) {
                return "theme";
            }
        }
        return "advance";
    }

    public boolean getIsPrimaryColorRecommendedColor() {
        int primaryColor = PrimaryColors.getPrimaryColor();
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == primaryColor) {
                return true;
            }
        }
        return false;
    }
}