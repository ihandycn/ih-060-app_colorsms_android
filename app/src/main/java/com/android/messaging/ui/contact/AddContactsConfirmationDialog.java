/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.messaging.ui.contact;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.messaging.BaseDialog;
import com.android.messaging.R;
import com.android.messaging.ui.ContactIconView;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.AccessibilityUtil;

public class AddContactsConfirmationDialog extends BaseDialog {

    private static final String BUNDLE_KEY_AVATAR_URI = "BUNDLE_KEY_AVATAR_URI";
    private static final String BUNDLE_KEY_DESTINATION = "BUNDLE_KEY_DESTINATION";

    private String mAvatarUri;
    private String mNormalizedDestination;

    public static AddContactsConfirmationDialog newInstance(Uri avatarUri, String normalizedDestination) {
        AddContactsConfirmationDialog dialog = new AddContactsConfirmationDialog();

        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_AVATAR_URI, avatarUri == null ? "" : avatarUri.toString());
        bundle.putString(BUNDLE_KEY_DESTINATION, normalizedDestination);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAvatarUri = getArguments() != null ? getArguments().getString(BUNDLE_KEY_AVATAR_URI) : null;
        mNormalizedDestination = getArguments() != null ? getArguments().getString(BUNDLE_KEY_DESTINATION) : null;
    }

    @Override
    protected CharSequence getMessages() {
        return null;
    }

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.add_contact_confirmation_dialog_title);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setOnNegativeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
            }
        });

        setOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIIntents.get().launchAddContactActivity(getActivity(), mNormalizedDestination);

            }
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected CharSequence getNegativeButtonText() {
        return getString(android.R.string.cancel);
    }

    @Override
    protected CharSequence getPositiveButtonText() {
        return getString(R.string.dialog_add_contact_confirmation);
    }

    @Override
    protected View getContentView() {
        return createBodyView();
    }

    private View createBodyView() {
        final View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.add_contacts_confirmation_dialog_body, null);
        final ContactIconView iconView = (ContactIconView) view.findViewById(R.id.contact_icon);
        if (TextUtils.isEmpty(mAvatarUri)) {
            iconView.setImageResourceUri(null);
        } else {
            iconView.setImageResourceUri(Uri.parse(mAvatarUri));
        }

        final TextView textView = (TextView) view.findViewById(R.id.participant_name);
        textView.setText(mNormalizedDestination);
        // Accessibility reason : in case phone numbers are mixed in the display name,
        // we need to vocalize it for talkback.
        final String vocalizedDisplayName = AccessibilityUtil.getVocalizedPhoneNumber(
                getResources(), mNormalizedDestination);
        textView.setContentDescription(vocalizedDisplayName);
        return view;
    }
}
