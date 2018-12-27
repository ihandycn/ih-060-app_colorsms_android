package com.android.messaging.ui.smsshow;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;

public class SmsShowListFragment extends Fragment {
    public static final String FRAGMENT_TAG = "smsshow";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sms_show_list_fragment, container, false);
        return view;
    }
}
