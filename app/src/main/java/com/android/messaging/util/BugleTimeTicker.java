package com.android.messaging.util;

import android.provider.Telephony;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;
import com.superapps.util.TimeTicker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizhe on 2019/2/13.
 */

public class BugleTimeTicker extends TimeTicker {

    private boolean isDefaultSmsApp = false;

    private static List<String> sAppList = new ArrayList<>();

    static {
        sAppList.add("com.android.mms");
        sAppList.add("com.google.android.apps.messaging");
        sAppList.add("com.android.contacts/com.android.mms.ui.ConversationList");
        sAppList.add("com.android.contacts/.MmsConversationActivity");
        sAppList.add("com.sonyericsson.conversations");
        sAppList.add("com.motorola.messaging");
        sAppList.add("com.motorola.blur.conversations");
        sAppList.add("com.handcent.nextsms");
        sAppList.add("com.jb.gosms");
        sAppList.add("com.htc.sense.mms");
        sAppList.add("com.asus.message");
        sAppList.add("com.huawei.message");
        sAppList.add("com.lge.message");
        sAppList.add("com.lenovo.ideafriend/.alias.MmsActivity");
        sAppList.add("com.zui.mms");
        sAppList.add("com.google.android.talk");
        sAppList.add("com.android.messaging/.ui.conversationlist.ConversationListActivity");
        sAppList.add("com.samsung.android.messaging/com.android.mms.ui.ConversationComposer");
        sAppList.add("com.textra");
        sAppList.add("com.link.messages.sms");
        sAppList.add("com.calea.echo");
        sAppList.add("com.p1.chompsms");
        sAppList.add("xyz.klinker.messenger");
    }

    @Override public void onTick() {
        Threads.postOnThreadPoolExecutor(() -> {
            final String configuredApplication = Telephony.Sms.getDefaultSmsPackage(HSApplication.getContext());
            HSLog.d(BugleTimeTicker.class.getSimpleName(), "current default sms app : " + configuredApplication);

            if (!HSApplication.getContext().getPackageName().equals(configuredApplication) && isDefaultSmsApp) {
                BugleAnalytics.logEvent("SMS_DefaultSMS_Cleared", true, "App", String.valueOf(sAppList.indexOf(configuredApplication)));
            }

            isDefaultSmsApp = HSApplication.getContext().getPackageName().equals(configuredApplication);
        });
    }
}
