package com.android.messaging.ui.smsshow;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;

public class SmsShowListFragment extends Fragment implements INotificationObserver {
    public static final String FRAGMENT_TAG = "smsshow";

    public static final String NOTIFICATION_KEY_APPLIED_SMS_SHOW_CHANGED = "NOTIFICATION_KEY_APPLIED_SMS_SHOW_CHANGED";

    private SmsShowListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sms_show_list_fragment, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.sms_show_list);
        recyclerView.setHasFixedSize(true);
        mAdapter = new SmsShowListAdapter(getActivity());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_KEY_APPLIED_SMS_SHOW_CHANGED, this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (NOTIFICATION_KEY_APPLIED_SMS_SHOW_CHANGED.equals(s)) {
            mAdapter.updateSelectedTheme();
        }
    }
}
