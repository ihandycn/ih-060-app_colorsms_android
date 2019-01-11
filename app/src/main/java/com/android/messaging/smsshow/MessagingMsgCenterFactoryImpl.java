package com.android.messaging.smsshow;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.PhoneUtils;
import com.messagecenter.customize.MessageCenterFactoryImpl;
import com.messagecenter.customize.SmsShowCallBack;
import com.messagecenter.notification.NotificationMessageAlertActivity;
import com.messagecenter.sms.SmsMessageAlertActivity;

public class MessagingMsgCenterFactoryImpl extends MessageCenterFactoryImpl {

    @Override
    public boolean isSMSAssistantOpenDefault() {
        return true;
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
                return !Factory.get().getIsForeground() && BugleNotifications.shouldNotify();
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
}
