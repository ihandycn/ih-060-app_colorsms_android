package com.android.messaging.smsshow;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.PhoneUtils;
import com.ihs.commons.utils.HSLog;
import com.messagecenter.customize.MessageCenterFactoryImpl;
import com.messagecenter.customize.SmsShowCallBack;
import com.messagecenter.notification.NotificationMessageAlertActivity;
import com.messagecenter.sms.SmsMessageAlertActivity;

public class MessagingMsgCenterFactoryImpl extends MessageCenterFactoryImpl {

    @Override
    public boolean isSMSAssistantOpenDefault() {
        return false;
    }

    @Override
    public SmsMessageAlertActivity.Config getSMSConfig() {
        return new SmsMessageAlertActivity.Config() {
            @Override
            public String getAdPlacement() {
                // todo to be replaced
                return "to be replaced";
            }

            /**
             * @return only open this feature when set sms default and notification switch opened.
             */

            @Override
            public boolean configEnabled() {
                return !Factory.get().getIsForeground()
                        && BugleNotifications.isNotificationSettingsSwitchOpenned()
                        && PhoneUtils.getDefault().isDefaultSmsApp();
            }

            @Override
            public boolean showAd() {
                return false;
            }

            @Override
            public boolean hideNotificationGuide() {
                return true;
            }
        };
    }

    @Override
    public SmsShowCallBack getSmsShowCallBack() {
        return new SmsShowCallBack() {
            @Override
            public long getMessageBoxShowDelayTime() {
                return 2000L;
            }

            @Override
            public void onMessageBoxPrepareToStart() {
                if (SmsShowUtils.getSmsShowAppliedId() > 0 && SmsShowUtils.isSmsShowEnabledByUser()) {
                    UIIntents.get().launchSmsShowActivity();
                }
            }
        };
    }

    @Override
    public NotificationMessageAlertActivity.Config getNotificationMessageConfig() {
        return new NotificationMessageAlertActivity.Config() {
            @Override
            public String getAdPlacement() {
                return null;
            }

            @Override
            public boolean enable() {
                return false;
            }

            @Override
            public boolean showDefaultSmsAppEntranceIcon() {
                return false;
            }
        };
    }

    @Override
    public NotificationMessageAlertActivity.Event getNotificationMessageEvent() {
        return new NotificationMessageAlertActivity.Event() {
            @Override
            public void onAdShow() {

            }

            @Override
            public void onAdClick() {

            }

            @Override
            public void onAdFlurryRecord(boolean isShown) {

            }

            @Override
            public void onShow() {
                BugleAnalytics.logEvent("SMS_PopUp_Show", true);
            }

            @Override
            public void onDismiss(NotificationMessageAlertActivity.DismissType type) {

                if (type == NotificationMessageAlertActivity.DismissType.MENU_CLOSE) {
                    BugleAnalytics.logEvent("SMS_PopUp_Disable", true);
                }
                BugleAnalytics.logEvent("SMS_PopUp_Close", true, "type", type.toString());
            }

            @Override
            public void onReplyClicked(String msgType) {
                BugleAnalytics.logEvent("SMS_PopUp_Reply_BtnClick", true);
            }

            @Override
            public void onNextClicked(String msgType) {
                BugleAnalytics.logEvent("SMS_PopUp_Next_BtnClick", true);
            }

            @Override
            public void onContentClick(String msgType) {
                BugleAnalytics.logEvent("SMS_PopUp_Click", true);
            }
        };
    }
}
