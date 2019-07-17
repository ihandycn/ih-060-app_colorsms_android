package com.android.messaging.util;

import java.util.HashMap;

/**
 * Created by jelly on 2019/5/21.
 */

class FireBaseEventIdMap {

    private static final HashMap<String, Boolean> EVENTS_MAP = new HashMap<>(30);

    static {
        EVENTS_MAP.put("Device_High_Retention", true);
        EVENTS_MAP.put("Device_ExtraHigh_Retention", true);
        EVENTS_MAP.put("Device_HUAWEI", true);

        EVENTS_MAP.put("SMS_Messages_Create", true);
        EVENTS_MAP.put("SMS_Messages_Show_Corrected", true);
        EVENTS_MAP.put("SMS_Messages_First_Click", true);
        EVENTS_MAP.put("SMS_Messages_Message_Click", true);
        EVENTS_MAP.put("SMS_Messages_BannerAd_Click", true);
        EVENTS_MAP.put("SMS_Messages_BannerAd_Show", true);
        EVENTS_MAP.put("SMS_Messages_BannerAd_Should_Show", true);
        EVENTS_MAP.put("SMS_CreateMessage_ButtonClick", true);

        EVENTS_MAP.put("SMSEmoji_ChatEmoji_Emoji_Send", true);
        EVENTS_MAP.put("SMSEmoji_ChatEmoji_Tab_Send", true);
        EVENTS_MAP.put("SMSEmoji_ChatEmoji_Magic_Send", true);
        EVENTS_MAP.put("SMSEmoji_ChatEmoji_LittleEmoji_Send", true);
        EVENTS_MAP.put("SMSEmoji_ChatEmoji_Store_Click", true);
        EVENTS_MAP.put("SMSEmoji_ChatEmoji_StoreList_Get", true);
        EVENTS_MAP.put("SMSEmoji_Chat_Emoji_Click", true);

        EVENTS_MAP.put("SMS_PopUp_Show", true);
        EVENTS_MAP.put("SMS_PopUp_MultiUser_Show", true);
        EVENTS_MAP.put("SMS_PopUp_Open_Click", true);
        EVENTS_MAP.put("SMS_PopUp_Close_Multifunction_MultiUser", true);
        EVENTS_MAP.put("SMS_PopUp_Close_Multifunction_SingleUser", true);
        EVENTS_MAP.put("SMS_PopUp_Show_Multifunction", true);
        EVENTS_MAP.put("SMS_PopUp_Reply_BtnClick_Multifunction", true);

        EVENTS_MAP.put("SMS_ChatBackground_CutPage_Applied", true);
        EVENTS_MAP.put("SMS_ChatBackground_Change", true);
        EVENTS_MAP.put("SMS_ChatBackground_Show", true);
        EVENTS_MAP.put("SMS_ChatBackground_AddPhotos_Clicked", true);
        EVENTS_MAP.put("SMS_ChatBackground_Backgrounds_Clicked", true);
        EVENTS_MAP.put("SMS_ChatBackground_Backgrounds_Applied", true);

        EVENTS_MAP.put("SMS_DetailsPage_IconSettings_Click", true);
        EVENTS_MAP.put("SMS_DetailsPage_IconCall_Click", true);
        EVENTS_MAP.put("SMS_DetailsPage_IconSend_Click", true);
        EVENTS_MAP.put("SMS_DetailsPage_LongPress_Close", true);
        EVENTS_MAP.put("SMS_DetailsPage_Show", true);
        EVENTS_MAP.put("SMS_DetailsPage_DialogBox_Click", true);
        EVENTS_MAP.put("SMS_DetailsPage_IconPlus_Click", true);

        EVENTS_MAP.put("SMS_Received_Default", true);
        EVENTS_MAP.put("SMS_Received_NoDefault", true);
        EVENTS_MAP.put("SMS_Received", true);

        EVENTS_MAP.put("SMS_Notifications_Pushed", true);
        EVENTS_MAP.put("SMS_Notifications_Clicked", true);
        EVENTS_MAP.put("SMS_PrivacyNotifications_Pushed", true);

        EVENTS_MAP.put("Menu_Show", true);
        EVENTS_MAP.put("Menu_Bubble_Click", true);
        EVENTS_MAP.put("Menu_ChatBackground_Click", true);
        EVENTS_MAP.put("Menu_Settings_Click", true);
        EVENTS_MAP.put("Menu_FiveStart_Click", true);

        EVENTS_MAP.put("SMS_EditMode_Show", true);
        EVENTS_MAP.put("SMS_Shortcut_Creat_Success", true);
        EVENTS_MAP.put("SMS_Send_Failed", true);
        EVENTS_MAP.put("SMS_SendPeopleAmount_Statistics", true);

        EVENTS_MAP.put("Customize_Font_Show", true);
        EVENTS_MAP.put("Customize_Bubble_Color_Show", true);
        EVENTS_MAP.put("Customize_ThemeCenter_Show", true);
        EVENTS_MAP.put("Customize_TextSize_Change", true);
        EVENTS_MAP.put("Customize_TextFont_Change", true);
        EVENTS_MAP.put("Customize_Bubble_Show", true);
        EVENTS_MAP.put("Customize_Bubble_Style_Change", true);
        EVENTS_MAP.put("Customize_Bubble_Change", true);
        EVENTS_MAP.put("Customize_Bubble_Color_Change", true);
        EVENTS_MAP.put("Customize_Notification_Sound_Change", true);
        EVENTS_MAP.put("Customize_ThemeColor_Show", true);
        EVENTS_MAP.put("Customize_ThemeCenter_Theme_Apply", true);

        EVENTS_MAP.put("Detailspage_FullAd_Click", true);
        EVENTS_MAP.put("Detailspage_FullAd_Show", true);
        EVENTS_MAP.put("Detailspage_FullAd_Should_Show", true);
        EVENTS_MAP.put("Detailspage_TopAd_Should_Show", true);
        EVENTS_MAP.put("Detailspage_TopAd_Show", true);
        EVENTS_MAP.put("Detailpage_BtnSend_Click", true);
        EVENTS_MAP.put("Detailspage_Show_Interval", true);
        EVENTS_MAP.put("Detailspage_Back", true);
        EVENTS_MAP.put("Detailspage_Show_Details", true);

        EVENTS_MAP.put("Start_ChooseTheme_Show", true);
        EVENTS_MAP.put("Start_SetAsDefault_Click", true);
        EVENTS_MAP.put("Start_SetAsDefault_Show", true);
        EVENTS_MAP.put("Start_WelcomePage_Show", true);
        EVENTS_MAP.put("Start_DetailPage_Show", true);
        EVENTS_MAP.put("Start_ChooseTheme_Apply", true);
        EVENTS_MAP.put("Start_SetAsDefault_Success", true);
        EVENTS_MAP.put("Start_DetailPage_Click", true);

        EVENTS_MAP.put("Alert_FiveStar_Submit_BtnClicked", true);
        EVENTS_MAP.put("Alert_FiveStar_Star_Clicked", true);
        EVENTS_MAP.put("Alert_FiveStar_ViewedFrom", true);

        EVENTS_MAP.put("SetAsDefault_GuidePage_Success", true);
        EVENTS_MAP.put("SetAsDefault_GuidePage_Click", true);
        EVENTS_MAP.put("SetAsDefault_GuidePage_Show", true);

        EVENTS_MAP.put("Subscription_Analysis", true);
        EVENTS_MAP.put("process_start_daily", true);
        EVENTS_MAP.put("Feature_BackupRestore", true);
        EVENTS_MAP.put("Send_Mms_Analytics", true);
    }

    static boolean getIsValidEventId(String eventId) {
        Boolean valid = EVENTS_MAP.get(eventId);
        return valid != null && valid;
    }
}
