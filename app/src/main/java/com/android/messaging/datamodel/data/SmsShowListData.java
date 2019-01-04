package com.android.messaging.datamodel.data;

import android.util.SparseArray;

public class SmsShowListData {

    private SparseArray<SmsShowListItemData> mSet;

    public SmsShowListData() {

    }

    public SparseArray<SmsShowListItemData> getData() {
        return mSet;
    }

    public SmsShowListItemData findItemById(int id) {
        return mSet.get(id);
    }

}
