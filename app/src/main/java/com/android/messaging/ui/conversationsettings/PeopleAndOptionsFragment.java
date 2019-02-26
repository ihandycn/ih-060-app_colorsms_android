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
package com.android.messaging.ui.conversationsettings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.binding.Binding;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.datamodel.data.ParticipantListItemData;
import com.android.messaging.datamodel.data.PeopleAndOptionsData;
import com.android.messaging.datamodel.data.PeopleAndOptionsData.PeopleAndOptionsDataListener;
import com.android.messaging.datamodel.data.PeopleOptionsItemData;
import com.android.messaging.datamodel.data.PersonItemData;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.ui.CompositeAdapter;
import com.android.messaging.ui.PersonItemView;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.conversation.ConversationActivity;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.CustomBubblesActivity;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.OsUtil;
import com.android.messaging.ui.wallpaper.WallpaperManager;
import com.android.messaging.ui.wallpaper.WallpaperPreviewActivity;
import com.superapps.util.Navigations;

import org.qcode.fontchange.impl.FontManagerImpl;

import java.util.ArrayList;
import java.util.List;

import static com.android.messaging.datamodel.data.PeopleOptionsItemData.SETTING_NOTIFICATION_SOUND_URI;
import static com.android.messaging.datamodel.data.PeopleOptionsItemData.SETTING_NOTIFICATION_VIBRATION;

/**
 * Shows a list of participants of a conversation and displays options.
 */
public class PeopleAndOptionsFragment extends Fragment
        implements PeopleAndOptionsDataListener, PeopleOptionsItemView.HostInterface, WallpaperManager.WallpaperChangeListener {
    private ListView mListView;
    private OptionsListAdapter mOptionsListAdapter;
    private PeopleListAdapter mPeopleListAdapter;
    private final Binding<PeopleAndOptionsData> mBinding =
            BindingBase.createBinding(this);
    private String mConversationId;

    private static final int REQUEST_CODE_RINGTONE_PICKER = 1000;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding.getData().init(getLoaderManager(), mBinding);
        WallpaperManager.addWallpaperChangeListener(this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.people_and_options_fragment, container, false);
        mListView = view.findViewById(android.R.id.list);
        mPeopleListAdapter = new PeopleListAdapter(getActivity());
        mOptionsListAdapter = new OptionsListAdapter();
        CustomizeListAdapter adapter = new CustomizeListAdapter(getActivity());

        final CompositeAdapter compositeAdapter = new CompositeAdapter(getActivity());
        compositeAdapter.addPartition(new PeopleAndOptionsPartition(adapter, R.string.customize_title, false));
        compositeAdapter.addPartition(new PeopleAndOptionsPartition(mOptionsListAdapter,
                R.string.general_settings_title, false));
        compositeAdapter.addPartition(new PeopleAndOptionsPartition(mPeopleListAdapter,
                R.string.participant_list_title, false));
        mListView.setAdapter(compositeAdapter);
        FontManagerImpl.getInstance().applyFont(view, true);
        return view;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_RINGTONE_PICKER) {
            final Parcelable pick = data.getParcelableExtra(
                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            final String pickedUri = pick == null ? "" : pick.toString();
            mBinding.getData().setConversationNotificationSound(mBinding, pickedUri);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBinding.unbind();
        WallpaperManager.removeWallpaperChangeListener(this);
    }

    public void setConversationId(final String conversationId) {
        Assert.isTrue(getView() == null);
        Assert.notNull(conversationId);
        mBinding.bind(DataModel.get().createPeopleAndOptionsData(conversationId, getActivity(),
                this));
        mConversationId = conversationId;
    }

    @Override
    public void onOptionsCursorUpdated(final PeopleAndOptionsData data, final Cursor cursor) {
        Assert.isTrue(cursor == null || cursor.getCount() == 1);
        mBinding.ensureBound(data);
        mOptionsListAdapter.swapCursor(cursor);
    }

    @Override
    public void onParticipantsListLoaded(final PeopleAndOptionsData data,
                                         final List<ParticipantData> participants) {
        mBinding.ensureBound(data);
        mPeopleListAdapter.updateParticipants(participants);
        final ParticipantData otherParticipant = participants.size() == 1 ?
                participants.get(0) : null;
        mOptionsListAdapter.setOtherParticipant(otherParticipant);
    }

    @Override
    public void onOptionsItemViewClicked(final PeopleOptionsItemData item,
                                         final boolean isChecked) {
        switch (item.getItemId()) {
            case PeopleOptionsItemData.SETTING_NOTIFICATION_ENABLED:
                mBinding.getData().enableConversationNotifications(mBinding, isChecked);
                break;

            case SETTING_NOTIFICATION_SOUND_URI:
                final Intent ringtonePickerIntent = UIIntents.get().getRingtonePickerIntent(
                        getString(R.string.notification_sound_pref_title),
                        item.getRingtoneUri(), Settings.System.DEFAULT_NOTIFICATION_URI,
                        RingtoneManager.TYPE_NOTIFICATION);
                startActivityForResult(ringtonePickerIntent, REQUEST_CODE_RINGTONE_PICKER);
                break;

            case SETTING_NOTIFICATION_VIBRATION:
                mBinding.getData().enableConversationNotificationVibration(mBinding,
                        isChecked);
                break;

            case PeopleOptionsItemData.SETTING_BLOCKED:
                if (item.getOtherParticipant().isBlocked()) {
                    mBinding.getData().setDestinationBlocked(mBinding, false);
                    break;
                }
                final Resources res = getResources();
                final Activity activity = getActivity();

                new BaseAlertDialog.Builder(activity)
                        .setTitle(res.getString(R.string.block_confirmation_title,
                                item.getOtherParticipant().getDisplayDestination()))
                        .setMessage(res.getString(R.string.block_confirmation_message))
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                                (arg0, arg1) -> {
                                    mBinding.getData().setDestinationBlocked(mBinding, true);
                                    activity.setResult(ConversationActivity.FINISH_RESULT_CODE);
                                    activity.finish();
                                })
                        .show();
                break;
        }
    }

    @Override
    public void onWallpaperChanged() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onOnlineWallpaperChanged() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private class CustomizeListAdapter extends BaseAdapter {

        private Context mContext;

        CustomizeListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.conversation_option_customize, parent, false);


            itemView.findViewById(R.id.chat_background).setOnClickListener(v -> {
                WallpaperPreviewActivity.startWallpaperPreviewByThreadId(mContext, mConversationId);
            });

            itemView.findViewById(R.id.chat_bubble).setOnClickListener(v ->
                    UIIntents.get().launchCustomBubblesActivity(mContext, mConversationId));
            return itemView;
        }
    }

    /**
     * A simple adapter that takes a conversation metadata cursor and binds
     * PeopleAndOptionsItemViews to individual COLUMNS of the first cursor record. (Note
     * that this is not a CursorAdapter because it treats individual columns of the cursor as
     * separate options to display for the conversation, e.g. notification settings).
     */
    private class OptionsListAdapter extends BaseAdapter {
        private Cursor mOptionsCursor;
        private ParticipantData mOtherParticipantData;

        private boolean isRingtoneEnabled = isRingtoneEnabled();

        public Cursor swapCursor(final Cursor newCursor) {
            final Cursor oldCursor = mOptionsCursor;
            if (newCursor != oldCursor) {
                mOptionsCursor = newCursor;
                notifyDataSetChanged();
            }
            return oldCursor;
        }

        public void setOtherParticipant(final ParticipantData participantData) {
            if (mOtherParticipantData != participantData) {
                mOtherParticipantData = participantData;
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            int count = PeopleOptionsItemData.SETTINGS_COUNT;
            if (mOtherParticipantData == null) {
                count--;
            }

            if (!isRingtoneEnabled) {
                // remove SETTING_NOTIFICATION_SOUND_URI and SETTING_NOTIFICATION_VIBRATION
                count -= 2;
            }
            return mOptionsCursor == null ? 0 : count;
        }

        @Override
        public Object getItem(final int position) {
            return null;
        }

        @Override
        public long getItemId(final int position) {
            return 0;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final PeopleOptionsItemView itemView;
            if (convertView != null && convertView instanceof PeopleOptionsItemView) {
                itemView = (PeopleOptionsItemView) convertView;
            } else {
                final LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                itemView = (PeopleOptionsItemView)
                        inflater.inflate(R.layout.people_options_item_view, parent, false);
            }
            mOptionsCursor.moveToFirst();

            itemView.bind(mOptionsCursor, position, mOtherParticipantData,
                    PeopleAndOptionsFragment.this, isRingtoneEnabled);
            FontManagerImpl.getInstance().applyFont(itemView, true);
            return itemView;
        }

        private boolean isRingtoneEnabled() {
            if (OsUtil.isAtLeastO()) {
                return false;
            }
            String prefKey = getString(R.string.notification_sound_pref_key);
            final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();

            String ringtoneString = prefs.getString(prefKey, null);

            if (ringtoneString == null) {
                ringtoneString = Settings.System.DEFAULT_NOTIFICATION_URI.toString();
                prefs.putString(prefKey, ringtoneString);
            }

            try {
                if (!TextUtils.isEmpty(ringtoneString)) {
                    final Uri ringtoneUri = Uri.parse(ringtoneString);
                    final Ringtone tone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);

                    if (tone != null) {
                        tone.getTitle(getActivity());
                    }
                    return true;
                }
                return false;
            } catch (SecurityException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * An adapter that takes a list of ParticipantData and displays them as a list of
     * ParticipantListItemViews.
     */
    private class PeopleListAdapter extends ArrayAdapter<ParticipantData> {
        public PeopleListAdapter(final Context context) {
            super(context, R.layout.people_list_item_view, new ArrayList<>());
        }

        public void updateParticipants(final List<ParticipantData> newList) {
            clear();
            addAll(newList);
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            PersonItemView itemView;
            final ParticipantData item = getItem(position);
            if (convertView != null && convertView instanceof PersonItemView) {
                itemView = (PersonItemView) convertView;
            } else {
                final LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                itemView = (PersonItemView) inflater.inflate(R.layout.people_list_item_view, parent,
                        false);
            }
            final ParticipantListItemData itemData =
                    DataModel.get().createParticipantListItemData(item);
            itemView.bind(itemData);

            // Any click on the row should have the same effect as clicking the avatar icon
            final PersonItemView itemViewClosure = itemView;
            itemView.setListener(new PersonItemView.PersonItemViewListener() {
                @Override
                public void onPersonClicked(final PersonItemData data) {
                    itemViewClosure.performClickOnAvatar();
                }

                @Override
                public boolean onPersonLongClicked(PersonItemData data) {
                    return false;
                }
            });
            FontManagerImpl.getInstance().applyFont(itemView, true);
            return itemView;
        }
    }

    /**
     * Represents a partition/section in the People & Options list (e.g. "general options" and
     * "people in this conversation" sections).
     */
    private class PeopleAndOptionsPartition extends CompositeAdapter.Partition {
        private final int mHeaderResId;
        private final boolean mNeedDivider;

        public PeopleAndOptionsPartition(final BaseAdapter adapter, final int headerResId,
                                         final boolean needDivider) {
            super(true /* showIfEmpty */, true /* hasHeader */, adapter);
            mHeaderResId = headerResId;
            mNeedDivider = needDivider;
        }

        @Override
        public View getHeaderView(final View convertView, final ViewGroup parentView) {
            View view;
            if (convertView != null && convertView.getId() == R.id.people_and_options_header) {
                view = convertView;
            } else {
                view = LayoutInflater.from(getActivity()).inflate(
                        R.layout.people_and_options_section_header, parentView, false);
            }
            final TextView text = view.findViewById(R.id.header_text);
            final View divider = view.findViewById(R.id.divider);
            text.setText(mHeaderResId);
            divider.setVisibility(mNeedDivider ? View.VISIBLE : View.GONE);
            return view;
        }
    }
}
