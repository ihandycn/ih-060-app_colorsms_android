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
package com.android.messaging.ui.conversation;

import android.app.FragmentManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.widget.EditText;

import com.android.messaging.R;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.binding.ImmutableBindingRef;
import com.android.messaging.datamodel.data.ConversationData;
import com.android.messaging.datamodel.data.ConversationData.ConversationDataListener;
import com.android.messaging.datamodel.data.ConversationData.SimpleConversationDataListener;
import com.android.messaging.datamodel.data.DraftMessageData;
import com.android.messaging.datamodel.data.DraftMessageData.DraftMessageSubscriptionDataProvider;
import com.android.messaging.datamodel.data.MediaPickerMessagePartData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.datamodel.data.PendingAttachmentData;
import com.android.messaging.datamodel.data.SubscriptionListData.SubscriptionListEntry;
import com.android.messaging.ui.emoji.EmojiPickerFragment;
import com.android.messaging.ui.emoji.EmojiType;
import com.android.messaging.ui.mediapicker.MediaPickerFragment;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.ImeUtil.ImeStateHost;
import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Manages showing/hiding/persisting different mutually exclusive UI components nested in
 * ConversationFragment that take user inputs, i.e. media picker, SIM selector and
 * IME keyboard (the IME keyboard is not owned by Bugle, but we try to model it the same way
 * as the other components).
 */
public class ConversationInputManager implements ConversationInput.ConversationInputBase {
    /**
     * The host component where all input components are contained. This is typically the
     * conversation fragment but may be mocked in test code.
     */
    public interface ConversationInputHost extends DraftMessageSubscriptionDataProvider {
        void invalidateActionBar();

        void setOptionsMenuVisibility(boolean visible);

        void dismissActionMode();

        void selectSim(SubscriptionListEntry subscriptionData);

        void onStartComposeMessage();

        SimSelectorView getSimSelectorView();

        MediaPickerFragment createMediaPicker();

        void showHideSimSelector(boolean show);

        int getSimSelectorItemLayoutId();

        EmojiPickerFragment createEmojiPicker();

        void showCamera();

        void showPhoto();
    }

    /**
     * The "sink" component where all inputs components will direct the user inputs to. This is
     * typically the ComposeMessageView but may be mocked in test code.
     */
    public interface ConversationInputSink {
        void onMediaItemsSelected(Collection<MessagePartData> items);

        boolean isContainMessagePartData(Uri uri);

        void onMediaItemsUnselected(MessagePartData item);

        void onPendingAttachmentAdded(PendingAttachmentData pendingItem);

        void resumeComposeMessage();

        EditText getComposeEditText();

        void setAccessibility(boolean enabled);

        void logMagicSticker(String name);

        void logEmoji(String code);

        void logSticker(String name);

        void hideMediaPickerView();

        void hideEmojiPickerView();
    }

    private final ConversationInputHost mHost;
    private final ConversationInputSink mSink;

    /**
     * Dependencies injected from the host during construction
     */
    private final FragmentManager mFragmentManager;
    private final Context mContext;
    private final ImeStateHost mImeStateHost;
    private final ImmutableBindingRef<ConversationData> mConversationDataModel;
    private final ImmutableBindingRef<DraftMessageData> mDraftDataModel;

    private final ConversationInput[] mInputs;
    private final ConversationMediaPicker mMediaInput;
    private final ConversationEmojiPicker mEmojiInput;
    private final ConversationSimSelector mSimInput;
    private final ConversationImeKeyboard mImeInput;
    private int mUpdateCount;

    private final ImeUtil.ImeStateObserver mImeStateObserver = new ImeUtil.ImeStateObserver() {
        @Override
        public void onImeStateChanged(final boolean imeOpen) {
            mImeInput.onVisibilityChanged(imeOpen);
        }
    };

    private final ConversationDataListener mDataListener = new SimpleConversationDataListener() {
        @Override
        public void onConversationParticipantDataLoaded(ConversationData data) {
            mConversationDataModel.ensureBound(data);
        }

        @Override
        public void onSubscriptionListDataLoaded(ConversationData data) {
            mConversationDataModel.ensureBound(data);
            mSimInput.onSubscriptionListDataLoaded(data.getSubscriptionListData());
        }
    };

    public ConversationInputManager(
            final Context context,
            final ConversationInputHost host,
            final ConversationInputSink sink,
            final ImeStateHost imeStateHost,
            final FragmentManager fm,
            final BindingBase<ConversationData> conversationDataModel,
            final BindingBase<DraftMessageData> draftDataModel,
            final Bundle savedState) {
        mHost = host;
        mSink = sink;
        mFragmentManager = fm;
        mContext = context;
        mImeStateHost = imeStateHost;
        mConversationDataModel = BindingBase.createBindingReference(conversationDataModel);
        mDraftDataModel = BindingBase.createBindingReference(draftDataModel);

        // Register listeners on dependencies.
        mImeStateHost.registerImeStateObserver(mImeStateObserver);
        mConversationDataModel.getData().addConversationDataListener(mDataListener);

        // Initialize the inputs
        mMediaInput = new ConversationMediaPicker(this);
        mEmojiInput = new ConversationEmojiPicker(this);
        mSimInput = new SimSelector(this);
        mImeInput = new ConversationImeKeyboard(this, mImeStateHost.isImeOpen());
        mInputs = new ConversationInput[]{mMediaInput, mSimInput, mImeInput, mEmojiInput};

        if (savedState != null) {
            for (int i = 0; i < mInputs.length; i++) {
                mInputs[i].restoreState(savedState);
            }
        }
        updateHostOptionsMenu();
    }

    public void onDetach() {
        mImeStateHost.unregisterImeStateObserver(mImeStateObserver);
        // Don't need to explicitly unregister for data model events. It will unregister all
        // listeners automagically on unbind.
    }

    public void onSaveInputState(final Bundle savedState) {
        for (int i = 0; i < mInputs.length; i++) {
            mInputs[i].saveState(savedState);
        }
    }

    @Override
    public String getInputStateKey(final ConversationInput input) {
        return input.getClass().getCanonicalName() + "_savedstate_";
    }

    public boolean onBackPressed() {
        for (int i = 0; i < mInputs.length; i++) {
            if (mInputs[i].onBackPressed()) {
                return true;
            }
        }
        return false;
    }

    public boolean onNavigationUpPressed() {
        for (int i = 0; i < mInputs.length; i++) {
            if (mInputs[i].onNavigationUpPressed()) {
                return true;
            }
        }
        return false;
    }

    public void showMediaPicker() {
        showHideInternal(mMediaInput, true, true);
    }

    public void hideMediaPicker() {
        showHideInternal(mMediaInput, false, false);
    }

    public void showEmojiPicker() {
        showHideInternal(mEmojiInput, true, false);
    }

    public void hideEmojiPicker() {
        showHideInternal(mEmojiInput, false, false);
    }

    /**
     * Show or hide the sim selector
     *
     * @param show    visibility
     * @param animate whether to animate the change in visibility
     * @return true if the state of the visibility was changed
     */
    public boolean showHideSimSelector(final boolean show, final boolean animate) {
        return showHideInternal(mSimInput, show, animate);
    }

    public void showHideImeKeyboard(final boolean show, final boolean animate) {
        showHideInternal(mImeInput, show, animate);
    }

    public void hideAllInputs(final boolean animate) {
        beginUpdate();
        for (int i = 0; i < mInputs.length; i++) {
            showHideInternal(mInputs[i], false, animate);
        }
        endUpdate();
    }

    /**
     * Toggle the visibility of the sim selector.
     *
     * @param animate
     * @param subEntry
     * @return true if the view is now shown, false if it now hidden
     */
    public boolean toggleSimSelector(final boolean animate, final SubscriptionListEntry subEntry) {
        mSimInput.setSelected(subEntry);
        return mSimInput.toggle(animate);
    }

    public boolean updateActionBar(final ActionBar actionBar) {
        for (int i = 0; i < mInputs.length; i++) {
            if (mInputs[i].mShowing) {
                return mInputs[i].updateActionBar(actionBar);
            }
        }
        return false;
    }

    @VisibleForTesting
    boolean isMediaPickerVisible() {
        return mMediaInput.mShowing;
    }

    @VisibleForTesting
    boolean isSimSelectorVisible() {
        return mSimInput.mShowing;
    }

    @VisibleForTesting
    boolean isImeKeyboardVisible() {
        return mImeInput.mShowing;
    }

    @VisibleForTesting
    void testNotifyImeStateChanged(final boolean imeOpen) {
        mImeStateObserver.onImeStateChanged(imeOpen);
    }

    /**
     * returns true if the state of the visibility was actually changed
     */
    @Override
    public boolean showHideInternal(final ConversationInput target, final boolean show,
                                    final boolean animate) {
        if (!mConversationDataModel.isBound()) {
            return false;
        }

        if (target.mShowing == show) {
            return false;
        }
        beginUpdate();
        boolean success;
        if (!show) {
            success = target.hide(animate);
        } else {
            success = target.show(animate);
        }

        if (success) {
            target.onVisibilityChanged(show);
        }
        endUpdate();
        return true;
    }

    @Override
    public void handleOnShow(final ConversationInput target) {
        if (!mConversationDataModel.isBound()) {
            return;
        }
        beginUpdate();

        // All inputs are mutually exclusive. Showing one will hide everything else.
        // The one exception, is that the keyboard and location media chooser can be open at the
        // time to enable searching within that chooser
        for (int i = 0; i < mInputs.length; i++) {
            final ConversationInput currInput = mInputs[i];
            if (currInput != target) {
                // TODO : If there's more exceptions we will want to make this more
                // generic
//                if (currInput instanceof ConversationMediaPicker &&
//                        target instanceof ConversationImeKeyboard &&
//                        mMediaInput.getExistingOrCreateMediaPicker() != null &&
//                        mMediaInput.getExistingOrCreateMediaPicker().canShowIme()) {
//                    // Allow the keyboard and location mediaPicker to be open at the same time,
//                    // but ensure the media picker is full screen to allow enough room
//                    mMediaInput.getExistingOrCreateMediaPicker().setFullScreen(true);
//                    continue;
//                }
                showHideInternal(currInput, false /* show */, false /* animate */);
            }
        }
        // Always dismiss action mode on show.
        mHost.dismissActionMode();
        // Invoking any non-keyboard input UI is treated as starting message compose.
        if (target != mImeInput) {
            mHost.onStartComposeMessage();
        }
        endUpdate();
    }

    @Override
    public void beginUpdate() {
        mUpdateCount++;
    }

    @Override
    public void endUpdate() {
        Assert.isTrue(mUpdateCount > 0);
        if (--mUpdateCount == 0) {
            // Always try to update the host action bar after every update cycle.
            mHost.invalidateActionBar();
        }
    }

    private void updateHostOptionsMenu() {
        mHost.setOptionsMenuVisibility(!mMediaInput.isOpen());
    }

    /**
     * Manages showing/hiding the media picker in conversation.
     */

    private class ConversationMediaPicker extends ConversationInput {
        public ConversationMediaPicker(ConversationInputBase baseHost) {
            super(baseHost, false);
        }

        private MediaPickerFragment mMediaPicker;

        @Override
        public boolean show(boolean animate) {
            if (!isAddedToFragmentManager()) {
                initMediaPicker();

                mFragmentManager.beginTransaction().replace(
                        R.id.media_picker_container,
                        mMediaPicker,
                        MediaPickerFragment.FRAGMENT_TAG).commit();
            }
            return true;
        }

        private void initMediaPicker() {
            if (mMediaPicker != null) {
                return;
            }
            mMediaPicker = mHost.createMediaPicker();
            mMediaPicker.setSubscriptionDataProvider(mHost);
            mMediaPicker.setOnMediaItemClickListener(new MediaPickerFragment.OnMediaItemListener() {
                @Override
                public void showCamera() {
                    BugleAnalytics.logEvent("SMS_DetailsPage_Plus_Camera", true);
                    mHost.showCamera();
                }

                @Override
                public void showPhoto() {
                    BugleAnalytics.logEvent("SMS_DetailsPage_Plus_Photo", true);
                    mHost.showPhoto();
                }

                @Override
                public void onAudioRecorded(MessagePartData item) {
                    final List<MessagePartData> items = new ArrayList<MessagePartData>(1);
                    items.add(item);
                    mSink.onMediaItemsSelected(items);

                    mHost.invalidateActionBar();
                    mSink.resumeComposeMessage();
                }
            });
        }

        private boolean isAddedToFragmentManager() {
            return mMediaPicker != null && mFragmentManager.findFragmentByTag(MediaPickerFragment.FRAGMENT_TAG) != null;
        }

        @Override
        public boolean hide(boolean animate) {
            mMediaPicker.hideAudioView();
            return true;
        }

        private boolean isOpen() {
            return (mMediaPicker != null);
        }

        @Override
        public boolean onBackPressed() {
            mSink.hideMediaPickerView();
            return super.onBackPressed();
        }
    }

    private class ConversationEmojiPicker extends ConversationInput {

        private EmojiPickerFragment mEmojiPickerFragment;

        ConversationEmojiPicker(ConversationInputBase baseHost) {
            super(baseHost, false);
        }

        @Override
        public boolean show(boolean animate) {
            if (!isAddedToFragmentManager()) {

                initEmojiPicker();

                mFragmentManager.beginTransaction().replace(
                        R.id.emoji_picker_container,
                        mEmojiPickerFragment,
                        EmojiPickerFragment.FRAGMENT_TAG).commitAllowingStateLoss();
            }
            return true;
        }

        @Override
        public boolean hide(boolean animate) {
            return true;
        }

        private void initEmojiPicker() {
            if (mEmojiPickerFragment != null) {
                return;
            }
            mEmojiPickerFragment = mHost.createEmojiPicker();
            mEmojiPickerFragment.setOnEmojiPickerListener(new EmojiPickerFragment.OnEmojiPickerListener() {
                @Override
                public void addEmoji(String emojiStr) {
                    if (mSink != null && mSink.getComposeEditText() != null) {
                        EditText editText = mSink.getComposeEditText();
                        int start = editText.getSelectionStart();
                        int end = editText.getSelectionEnd();
                        editText.getText().replace(start, end, emojiStr);
                        mSink.logEmoji(emojiStr);
                    }
                }

                @Override
                public void deleteEmoji() {
                    if (mSink != null && mSink.getComposeEditText() != null) {
                        mSink.getComposeEditText().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                    }
                }

                @Override
                public void prepareSendSticker(Collection<MessagePartData> items) {
                    mSink.onMediaItemsSelected(items);
                    mHost.invalidateActionBar();
                    for (MessagePartData data : items) {
                        if (data instanceof MediaPickerMessagePartData) {
                            MediaPickerMessagePartData mediaData = (MediaPickerMessagePartData) data;
                            if (mediaData.getEmojiType() != null) {
                                if (mediaData.getEmojiType() == EmojiType.STICKER_MAGIC) {
                                    mSink.logMagicSticker(mediaData.getName());
                                } else {
                                    mSink.logSticker(mediaData.getName());
                                }
                            }
                        }
                    }
                }

                @Override
                public boolean isContainMessagePartData(Uri uri) {
                    return mSink.isContainMessagePartData(uri);
                }
            });
        }

        @Override public boolean onBackPressed() {
            mSink.hideEmojiPickerView();
            return super.onBackPressed();
        }

        boolean isAddedToFragmentManager() {
            return mEmojiPickerFragment != null && mFragmentManager.findFragmentByTag(EmojiPickerFragment.FRAGMENT_TAG) != null;
        }
    }

    /**
     * Manages showing/hiding the SIM selector in conversation.
     */
    private class SimSelector extends ConversationSimSelector {
        public SimSelector(ConversationInputBase baseHost) {
            super(baseHost);
        }

        @Override
        protected SimSelectorView getSimSelectorView() {
            return mHost.getSimSelectorView();
        }

        @Override
        public int getSimSelectorItemLayoutId() {
            return mHost.getSimSelectorItemLayoutId();
        }

        @Override
        protected void selectSim(SubscriptionListEntry item) {
            mHost.selectSim(item);
        }

        @Override
        public boolean show(boolean animate) {
            final boolean result = super.show(animate);
            mHost.showHideSimSelector(true /*show*/);
            return result;
        }

        @Override
        public boolean hide(boolean animate) {
            final boolean result = super.hide(animate);
            mHost.showHideSimSelector(false /*show*/);
            return result;
        }
    }

    /**
     * Manages showing/hiding the IME keyboard in conversation.
     */
    private class ConversationImeKeyboard extends ConversationInput {
        public ConversationImeKeyboard(ConversationInputBase baseHost, final boolean isShowing) {
            super(baseHost, isShowing);
        }

        @Override
        public boolean show(boolean animate) {
            ImeUtil.get().showImeKeyboard(mContext, mSink.getComposeEditText());
            return true;
        }

        @Override
        public boolean hide(boolean animate) {
            ImeUtil.get().hideImeKeyboard(mContext, mSink.getComposeEditText());
            return true;
        }
    }
}
