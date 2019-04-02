package com.android.messaging.sms;

import com.ihs.app.framework.HSApplication;

import me.leolin.shortcutbadger.ShortcutBadger;

public class UnreadMessageManager {
    private int mUnreadMessageCount;
    private static UnreadMessageManager mInstance = new UnreadMessageManager();

    public static UnreadMessageManager getInstance() {
        return mInstance;
    }

    private UnreadMessageManager() {

    }

    public void setUnreadMessageCount(int count) {
        if (mUnreadMessageCount != count) {
            mUnreadMessageCount = count;
            onUnreadMessageCountChanged();
        }
    }

    public void onUnreadMessageCountChanged() {
        if (ShortcutBadger.isBadgeCounterSupported(HSApplication.getContext())) {
            if (mUnreadMessageCount > 0) {
                ShortcutBadger.applyCount(HSApplication.getContext(), mUnreadMessageCount);
            } else {
                ShortcutBadger.removeCount(HSApplication.getContext());
            }
        }
    }
}
