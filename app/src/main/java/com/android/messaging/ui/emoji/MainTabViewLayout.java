package com.android.messaging.ui.emoji;

import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;

import java.util.List;

public class MainTabViewLayout extends LinearLayout {
    private List<EmojiPackageInfo> mInfoList;
    private View[] mViewArr;
    private int mLastSelected = -1;
    private int mPrimaryColors;
    private Context mContext;
    private OnTabSelectListener mListener;

    public MainTabViewLayout(Context context) {
        super(context);
        initView(context);
    }

    public MainTabViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MainTabViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mPrimaryColors = PrimaryColors.getPrimaryColor();
        mContext = context;
    }

    public void setListener(OnTabSelectListener listener) {
        this.mListener = listener;
    }

    public void setInfoList(List<EmojiPackageInfo> infoList) {
        this.mInfoList = infoList;
        this.mViewArr = new View[infoList.size()];
        LayoutInflater inflater = LayoutInflater.from(mContext);
        for (int i = 0; i < mViewArr.length; i++) {
            mViewArr[i] = inflater.inflate(R.layout.emoji_tab_item_layout, this, false);
            int finalI = i;
            mViewArr[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTabSelected(finalI);
                }
            });
            addView(mViewArr[i]);
            onTabUnselected(i);
        }
    }

    public void select(int pos) {
        onTabSelected(pos);
    }

    private void onTabSelected(int pos) {
        if (pos >= mViewArr.length || pos < 0) {
            return;
        }
        if(pos == mLastSelected){
            return ;
        }
        View view = mViewArr[pos];
        if (view == null) {
            return;
        }
        ImageView tabIconView = view.findViewById(R.id.tab_icon_view);
        if (tabIconView == null)
            return;
        tabIconView.setImageURI(Uri.parse(mInfoList.get(pos).mTabIconSelectedUrl));
        tabIconView.getDrawable().setColorFilter(mPrimaryColors, PorterDuff.Mode.SRC_ATOP);
        if (mLastSelected != -1) {
            onTabUnselected(mLastSelected);
        }
        mLastSelected = pos;
        if(mListener != null) {
            mListener.onTabSelected(pos);
        }
    }

    private void onTabUnselected(int pos) {
        if (pos >= mViewArr.length || pos < 0) {
            return;
        }
        View view = mViewArr[pos];
        if(view == null){
            return ;
        }
        ImageView tabIconView = view.findViewById(R.id.tab_icon_view);
        if (tabIconView == null)
            return;
        tabIconView.setImageURI(Uri.parse(mInfoList.get(pos).mTabIconUrl));
        if(mListener != null) {
            mListener.onTabUnselected(pos);
        }
    }

    interface OnTabSelectListener {
        void onTabSelected(int pos);

        void onTabUnselected(int pos);
    }
}
