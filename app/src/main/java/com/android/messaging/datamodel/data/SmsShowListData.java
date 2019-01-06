package com.android.messaging.datamodel.data;

import com.android.messaging.datamodel.DataModel;
import com.ihs.commons.config.HSConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SmsShowListData {
    private static SmsShowListData sInstance = new SmsShowListData();

    private static final String CONFIG_KEY_ID = "Id";
    private static final String CONFIG_KEY_PREVIEW_URL = "Mainpage";
    private static final String CONFIG_KEY_SMS_SHOW_URL = "WebpUrl";

    private ArrayList<SmsShowListItemData> mList;

    private SmsShowListData() {
        List<Map<String, ?>> items = (List<Map<String, ?>>) HSConfig.getList("Application", "SmsShow", "Themes");
        mList = new ArrayList<>(items.size());

        DataModel model = DataModel.get();
        try {
            for (Map<String, ?> item : items) {
                mList.add(model.createSmsShowListItemData((Integer) item.get(CONFIG_KEY_ID),
                        (String) item.get(CONFIG_KEY_PREVIEW_URL),
                        (String) item.get(CONFIG_KEY_SMS_SHOW_URL)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SmsShowListData getInstance() {
        return sInstance;
    }

    public ArrayList<SmsShowListItemData> getData() {
        return mList;
    }

    public SmsShowListItemData findItemById(int id) {
        for (SmsShowListItemData itemData : mList) {
            if (itemData.getId() == id) {
                return itemData;
            }
        }
        return null;
    }

}
