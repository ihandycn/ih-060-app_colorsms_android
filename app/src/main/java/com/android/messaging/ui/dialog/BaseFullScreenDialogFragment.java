package com.android.messaging.ui.dialog;

import android.os.Bundle;

import com.android.messaging.R;
import com.android.messaging.util.CommonUtils;
import com.superapps.util.HomeKeyWatcher;

public class BaseFullScreenDialogFragment extends BaseDialogFragment {

    private HomeKeyWatcher mHomeWatcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.TransparentDialog);
    }

    @Override public void onStart() {
        super.onStart();

        mHomeWatcher = new HomeKeyWatcher(getActivity().getApplicationContext());
        mHomeWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                onHomeKeyPressed();
            }

            @Override
            public void onRecentsPressed() {

            }
        });
        mHomeWatcher.startWatch();
    }

    @Override public void onStop() {
        super.onStop();

        mHomeWatcher.stopWatch();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CommonUtils.immersiveStatusAndNavigationBar(getDialog().getWindow());
    }

    protected void onHomeKeyPressed() {
    }
}
