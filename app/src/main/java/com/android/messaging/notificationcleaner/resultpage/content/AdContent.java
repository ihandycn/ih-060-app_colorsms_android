package com.android.messaging.notificationcleaner.resultpage.content;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.notificationcleaner.resultpage.ResultPageActivity;
import com.android.messaging.notificationcleaner.resultpage.data.ResultConstants;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.ViewUtils;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdIconView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdPrimaryView;

import static com.ihs.app.framework.HSApplication.getContext;

public class AdContent implements IContent {

    private Context context;

    private View adView;
    private Button button;
    private TextView title;
    private TextView subTitle;
    private ViewGroup mAdChoice;
    private AcbNativeAdIconView mAdIconView;
    private AcbNativeAdContainerView mAdContainer;
    private AcbNativeAdPrimaryView mAdImageContainer;
    private View resultView;
    private ViewGroup container;

    private AcbNativeAd ad;

    public AdContent(AcbNativeAd ad) {
        this.ad = ad;
    }

    @Override public void initView(Context context) {
        this.context = context;
        container = ViewUtils.findViewById((Activity) context, R.id.ad_view_container);
        resultView = ViewUtils.findViewById((Activity) context, R.id.result_view);
        container.setVisibility(View.GONE);
        adView = LayoutInflater.from(context).inflate(R.layout.result_page_content_ad, container, false);

        button = ViewUtils.findViewById(adView, R.id.promote_ad_button);
        button.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(((ResultPageActivity) context).getBackgroundColor(), 0, false));
        title = ViewUtils.findViewById(adView, R.id.promote_ad_title);
        subTitle = ViewUtils.findViewById(adView, R.id.promote_ad_body);
        mAdImageContainer = ViewUtils.findViewById(adView, R.id.promote_ad_primary_view);
        mAdIconView = ViewUtils.findViewById(adView, R.id.ad_icon_view_promote_ad_icon);
        mAdChoice = ViewUtils.findViewById(adView, R.id.promote_ad_choice);

        AcbNativeAdContainerView adContainer = new AcbNativeAdContainerView(getContext());
        adContainer.addContentView(adView);
        adContainer.setAdTitleView(title);
        adContainer.setAdBodyView(subTitle);
        adContainer.setAdActionView(button);
        adContainer.setAdPrimaryView(mAdImageContainer);
        adContainer.setAdChoiceView(mAdChoice);
        adContainer.setAdIconView(mAdIconView);

        container.addView(adContainer);
        mAdContainer = adContainer;
        if (mAdContainer != null) {
            mAdContainer.fillNativeAd(ad,null);
        }

        ad.setNativeClickListener(acbAd -> {
            BugleAnalytics.logEvent("ResultPage_Click", "Type", ResultConstants.AD);
            if (CommonUtils.isNewUser()) {
                BugleAnalytics.logEvent("New_User_BoostDone_Ad_Clicked", "version_name", HSApplication.getCurrentLaunchInfo().appVersionName);
            } else {
                BugleAnalytics.logEvent("Old_User_BoostDone_Ad_Clicked", "version_name", HSApplication.getCurrentLaunchInfo().appVersionName);
            }
        });
    }

    @Override public void startAnimation() {
        resultView.setVisibility(View.VISIBLE);
        container.setVisibility(View.VISIBLE);

        container.setAlpha(0);
        container.setTranslationY(Dimensions.pxFromDp(750));
        container.setVisibility(View.VISIBLE);
        container.animate()
                .setDuration(450)
                .alpha(1)
                .translationY(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                        ((ResultPageActivity) context).onAdContentShown();
                    }
                })
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    @Override public void onActivityDestroy() {

    }
}
