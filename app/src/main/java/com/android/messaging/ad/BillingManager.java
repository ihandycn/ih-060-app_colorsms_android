package com.android.messaging.ad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.android.messaging.BugleApplication;
import com.android.messaging.R;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleApplicationPrefs;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.Toasts;

import net.appcloudbox.service.iap.AcbIAPTransaction;
import net.appcloudbox.service.utils.AcbError;

import org.json.JSONObject;

import hugo.weaving.DebugLog;

public class BillingManager {

    public static final String BILLING_VERIFY_SUCCESS = "billing.verify.success";
    public static final String PRODUCT_ID = "com.color.sms.messages.emoji.pid.adfree.tier1";
    private static final String PREF_KEY_USER_HAS_VERIFIED_SUCCESS = "iap.user.has.verified.success";

    private static int sRetryCount = 0;

    public interface CheckIapStateCallBack {
        void onGetResult(boolean isPremiumUser);
    }

    @DebugLog
    public static boolean isPremiumUser() {
        AcbIAPTransaction.State state = AcbIAPTransaction.getState(PRODUCT_ID);
        return AcbIAPTransaction.State.VERIFIED.equals(state);
    }

    public static void init(BugleApplication application, CheckIapStateCallBack checkIapStateCallBack) {
        LocalBroadcastManager.getInstance(application).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AcbIAPTransaction.State state = AcbIAPTransaction.getState(PRODUCT_ID);
                if (AcbIAPTransaction.State.SHOULD_VERIFY.equals(state)) {
                    requestPurchase(checkIapStateCallBack);
                } else if (checkIapStateCallBack != null) {
                    checkIapStateCallBack.onGetResult(AcbIAPTransaction.State.VERIFIED.equals(state));
                }
            }

        }, new IntentFilter(AcbIAPTransaction.INVENTORY_DID_UPDATE_NOTIFICATION));
    }


    public static boolean hasUserEverVerifiedSuccessfully() {
        return BugleApplicationPrefs.getApplicationPrefs().getBoolean(PREF_KEY_USER_HAS_VERIFIED_SUCCESS, false);
    }

    public static void requestPurchase(final CheckIapStateCallBack callBack) {
        AcbIAPTransaction.ItemType type = AcbIAPTransaction.ItemType.NON_CONSUMABLE;
        // 附加信息（可为null）
        JSONObject userInfo = new JSONObject();
        new AcbIAPTransaction(PRODUCT_ID, type, userInfo, new AcbIAPTransaction.TransactionListener() {

            @Override
            public void onPurchaseSuccess() {
                // 购买完成，即将发起验证
            }

            @Override
            public void onPurchaseFailure(AcbError error) {
                // 购买失败，交易结束
            }

            @Override
            public void onUserCancel() {
                // 用户在应用商店的界面取消了购买，交易结束
            }

            @Override
            public void onVerificationSuccess() {
                // 验证通过，交易结束
                // 可在此处增加用户资产或资格
                AdConfig.deactiveAllAds();
                HSGlobalNotificationCenter.sendNotification(BILLING_VERIFY_SUCCESS);
                BugleApplicationPrefs.getApplicationPrefs().putBoolean(PREF_KEY_USER_HAS_VERIFIED_SUCCESS, true);

                BugleAnalytics.logEvent("SMS_Subscription_Purchase_Success", true);
                BugleAnalytics.logEvent("Subscription_Analysis", "Subscription_Purchase_Success", "true");
                BugleFirebaseAnalytics.logEvent("Subscription_Analysis", "Subscription_Purchase_Success", "true");
                if (callBack != null) {
                    callBack.onGetResult(true);
                }
            }

            @Override
            public void onVerificationFailure(AcbError error) {
                // 验证未通过（error.getKey() == AcbIAPErrorKey.InvalidReceipt，刚完成的购买会被消耗掉并从库中删除）
                // 或在验证时出现其它错误（购买变为待验证状态）
                // 交易结束

                Toasts.showToast(R.string.purchase_failed);
                if (++sRetryCount < 2) {
                    requestPurchase(callBack);
                }
            }
        }).start();
    }
}
