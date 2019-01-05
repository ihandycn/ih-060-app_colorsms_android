package com.android.messaging.datamodel.data;

import com.android.messaging.datamodel.DataModel;

import java.util.ArrayList;

public class SmsShowListData {

    private ArrayList<SmsShowListItemData> mList;

    public SmsShowListData() {

    }

    public ArrayList<SmsShowListItemData> getData() {
        mList = new ArrayList<>(20);
        for (int i = 0; i < 18; i++) {
            String url = "http://a0.att.hudong.com/14/72/19300001138148138494729355525.jpg";
            SmsShowListItemData itemData = DataModel.get().createSmsShowListItemData(i, url, url);
            mList.add(itemData);
        }
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
