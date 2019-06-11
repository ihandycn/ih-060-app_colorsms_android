package com.android.messaging.ui.invitefriends;

import com.android.messaging.privatebox.ui.addtolist.CallAssistantUtils;

import java.util.List;

public class InviteFriendsList {

    private static List<CallAssistantUtils.ContactInfo> sAddedFriendsList;

    public static void setAddedInvitedFriendsList(List<CallAssistantUtils.ContactInfo> contactInfoList) {
        sAddedFriendsList = contactInfoList;
    }

    public static List<CallAssistantUtils.ContactInfo> getAddedInvitedFriendsList() {
        return sAddedFriendsList;
    }

    public static void clear() {
        if (sAddedFriendsList != null) {
            sAddedFriendsList.clear();
            sAddedFriendsList = null;
        }
    }
}
