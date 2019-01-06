package com.android.messaging.smsshow;

import com.android.messaging.ui.UIIntents;
import com.messagecenter.customize.MessageCenterFactoryImpl;
import com.messagecenter.customize.SmsShowCallBack;
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
             * @return true sms show is an important feature for product, so MessageBox is only controlled by user.
             */

            @Override
            public boolean configEnabled() {
                return true;
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
                UIIntents.get().launchSmsShowActivity();
            }
        };
    }
}
