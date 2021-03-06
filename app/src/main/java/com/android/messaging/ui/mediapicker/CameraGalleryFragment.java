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

package com.android.messaging.ui.mediapicker;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.binding.Binding;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.binding.ImmutableBindingRef;
import com.android.messaging.datamodel.data.DraftMessageData;
import com.android.messaging.datamodel.data.DraftMessageData.DraftMessageSubscriptionDataProvider;
import com.android.messaging.datamodel.data.MediaPickerData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.datamodel.data.PendingAttachmentData;
import com.android.messaging.ui.BugleActionBarActivity;
import com.android.messaging.ui.FixedViewPagerAdapter;
import com.android.messaging.ui.conversation.ConversationFragment;
import com.android.messaging.ui.mediapicker.DocumentImagePicker.SelectionListener;
import com.android.messaging.util.Assert;
import com.android.messaging.util.UiUtils;
import com.google.common.annotations.VisibleForTesting;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Fragment used to select or capture media to be added to the message
 */
public class CameraGalleryFragment extends Fragment implements DraftMessageSubscriptionDataProvider {
    /**
     * The listener interface for events from the media picker
     */
    public interface MediaPickerListener {
        /**
         * Called when the user selects one or more items
         *
         * @param items The list of items which were selected
         */
        void onItemsSelected(Collection<MessagePartData> items, boolean dismissMediaPicker);

        /**
         * Called when the user unselects one item.
         */
        void onItemUnselected(MessagePartData item);

        /**
         * Called when the media picker is closed.  Always called immediately after onItemsSelected
         */
        void onDismissed();

        /**
         * Called when media item selection is confirmed in a multi-select action.
         */
        void onConfirmItemSelection();

        /**
         * Called when a pending attachment is added.
         *
         * @param pendingItem the pending attachment data being loaded.
         */
        void onPendingItemAdded(PendingAttachmentData pendingItem);

        /**
         * Called when a new media chooser is selected.
         */
        void onChooserSelected(final int chooserIndex);
    }

    /**
     * The tag used when registering and finding this fragment
     */
    public static final String FRAGMENT_TAG = "mediapicker";

    // Media type constants that the media picker supports
    public static final int MEDIA_TYPE_DEFAULT = 0x0000;
    public static final int MEDIA_TYPE_NONE = 0x0000;
    public static final int MEDIA_TYPE_IMAGE = 0x0001;
    public static final int MEDIA_TYPE_VIDEO = 0x0002;
    public static final int MEDIA_TYPE_AUDIO = 0x0004;
    public static final int MEDIA_TYPE_VCARD = 0x0008;
    public static final int MEDIA_TYPE_LOCATION = 0x0010;
    private static final int MEDA_TYPE_INVALID = 0x0020;
    public static final int MEDIA_TYPE_ALL = 0xFFFF;

    /**
     * The listener to call when events occur
     */
    private MediaPickerListener mListener;

    /**
     * The handler used to dispatch calls to the listener
     */
    private Handler mListenerHandler;

    /**
     * The bit flags of media types supported
     */
    private int mSupportedMediaTypes;

    /**
     * The list of choosers which could be within the media picker
     */
    private final MediaChooser[] mChoosers;

    /**
     * The list of currently enabled choosers
     */
    private final ArrayList<MediaChooser> mEnabledChoosers;

    /**
     * The currently selected chooser
     */
    private MediaChooser mSelectedChooser;

    /**
     * The view pager to swap between choosers
     */
    private ViewPager mViewPager;

    /**
     * The current pager adapter for the view pager
     */
    private FixedViewPagerAdapter<MediaChooser> mPagerAdapter;

    /**
     * The theme color to use to make the media picker match the rest of the UI
     */
    private int mThemeColor;

    @VisibleForTesting final Binding<MediaPickerData> mBinding = BindingBase.createBinding(this);

    /**
     * Handles picking image from the document picker
     */
    private DocumentImagePicker mDocumentImagePicker;

    /**
     * Provides subscription-related data to access per-subscription configurations.
     */
    private DraftMessageSubscriptionDataProvider mSubscriptionDataProvider;

    /**
     * Provides access to DraftMessageData associated with the current conversation
     */
    private ImmutableBindingRef<DraftMessageData> mDraftMessageDataModel;

    public CameraGalleryFragment() {
        this(Factory.get().getApplicationContext(), true);
    }

    public CameraGalleryFragment(final Context context, boolean isCamera) {
        mBinding.bind(DataModel.get().createMediaPickerData(context));
        mEnabledChoosers = new ArrayList<MediaChooser>();
        if (isCamera) {
            mSelectedChooser = new CameraMediaChooser(this);
        } else {
            mSelectedChooser = new GalleryMediaChooser(this);
        }

        mChoosers = new MediaChooser[]{
                mSelectedChooser
        };

        setSupportedMediaTypes(MEDIA_TYPE_ALL);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding.getData().init(getLoaderManager());
        mDocumentImagePicker = new DocumentImagePicker(this,
                new SelectionListener() {
                    @Override
                    public void onDocumentSelected(final PendingAttachmentData data) {
                        if (mBinding.isBound()) {
                            dispatchPendingItemAdded(data);
                        }
                    }
                });
    }

    @Override
    public View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mediapicker_fragment,
                container,
                false);

        mViewPager = view.findViewById(R.id.mediapicker_view_pager);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(
                    final int position,
                    final float positionOffset,
                    final int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                // The position returned is relative to if we are in RtL mode. This class never
                // switches the indices of the elements if we are in RtL mode so we need to
                // translate the index back. For example, if the user clicked the item most to the
                // right in RtL mode we would want the index to appear as 0 here, however the
                // position returned would the last possible index.
                if (UiUtils.isRtlMode()) {
                    position = mEnabledChoosers.size() - 1 - position;
                }
                selectChooser(mEnabledChoosers.get(position));
            }

            @Override
            public void onPageScrollStateChanged(final int state) {
            }
        });
        // Camera initialization is expensive, so don't realize offscreen pages if not needed.
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setAdapter(mPagerAdapter);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        CameraManager.get().onPause();
        for (final MediaChooser chooser : mEnabledChoosers) {
            chooser.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        CameraManager.get().onResume();

        for (final MediaChooser chooser : mEnabledChoosers) {
            chooser.onResume();
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mDocumentImagePicker.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBinding.unbind();
    }

    /**
     * Sets the theme color to make the media picker match the surrounding UI
     *
     * @param themeColor The new theme color
     */
    public void setConversationThemeColor(final int themeColor) {
        mThemeColor = themeColor;

        for (final MediaChooser chooser : mEnabledChoosers) {
            chooser.setThemeColor(mThemeColor);
        }
    }

    /**
     * Gets the current conversation theme color.
     */
    public int getConversationThemeColor() {
        return mThemeColor;
    }

    public void setDraftMessageDataModel(final BindingBase<DraftMessageData> draftBinding) {
        mDraftMessageDataModel = Binding.createBindingReference(draftBinding);
    }

    public ImmutableBindingRef<DraftMessageData> getDraftMessageDataModel() {
        return mDraftMessageDataModel;
    }

    public void setSubscriptionDataProvider(final DraftMessageSubscriptionDataProvider provider) {
        mSubscriptionDataProvider = provider;
    }

    @Override
    public int getConversationSelfSubId() {
        return mSubscriptionDataProvider.getConversationSelfSubId();
    }

    /**
     * Sets the list of media types to allow the user to select
     *
     * @param mediaTypes The bit flags of media types to allow.  Can be any combination of the
     *                   MEDIA_TYPE_* values
     */
    void setSupportedMediaTypes(final int mediaTypes) {
        mSupportedMediaTypes = mediaTypes;
        mEnabledChoosers.clear();
        boolean selectNextChooser = false;
        for (final MediaChooser chooser : mChoosers) {
            final boolean enabled = (chooser.getSupportedMediaTypes() & mSupportedMediaTypes) !=
                    MEDIA_TYPE_NONE;
            if (enabled) {
                // TODO Add a way to inform the chooser which media types are supported
                mEnabledChoosers.add(chooser);
                if (selectNextChooser) {
                    selectChooser(chooser);
                    selectNextChooser = false;
                }
            } else if (mSelectedChooser == chooser) {
                selectNextChooser = true;
            }
            final ImageButton tabButton = chooser.getTabButton();
            if (tabButton != null) {
                tabButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
            }
        }

        if (selectNextChooser && mEnabledChoosers.size() > 0) {
            selectChooser(mEnabledChoosers.get(0));
        }
        final MediaChooser[] enabledChoosers = new MediaChooser[mEnabledChoosers.size()];
        mEnabledChoosers.toArray(enabledChoosers);
        mPagerAdapter = new FixedViewPagerAdapter<MediaChooser>(enabledChoosers);
        if (mViewPager != null) {
            mViewPager.setAdapter(mPagerAdapter);
        }

        // Only rebind data if we are currently bound. Otherwise, we must have not
        // bound to any data yet and should wait until onCreate() to bind data.
        if (mBinding.isBound() && getActivity() != null) {
            mBinding.unbind();
            mBinding.bind(DataModel.get().createMediaPickerData(getActivity()));
            mBinding.getData().init(getLoaderManager());
        }
    }

    ViewPager getViewPager() {
        return mViewPager;
    }

    /**
     * Hides the media picker, and frees up any resources it’s using
     */
    public void dismiss(final boolean animate) {
        releaseView();
        dispatchDismissed();
        mSelectedChooser = null;
        HSGlobalNotificationCenter.sendNotification(ConversationFragment.EVENT_SHOW_OPTION_MENU);
    }

    /**
     * Sets the listener for the media picker events
     *
     * @param listener The listener which will receive events
     */
    public void setListener(final MediaPickerListener listener) {
        Assert.isMainThread();
        mListener = listener;
        mListenerHandler = listener != null ? new Handler() : null;
    }

    public void updateActionBar(final ActionBar actionBar) {
        if (getActivity() == null) {
            return;
        }
        if (mSelectedChooser != null) {
            mSelectedChooser.updateActionBar(actionBar);
        }
    }

    /**
     * Selects a new chooser
     *
     * @param newSelectedChooser The newly selected chooser
     */
    void selectChooser(final MediaChooser newSelectedChooser) {
        if (mSelectedChooser == newSelectedChooser) {
            return;
        }

        if (mSelectedChooser != null) {
            mSelectedChooser.setSelected(false);
        }
        mSelectedChooser = newSelectedChooser;
        if (mSelectedChooser != null) {
            mSelectedChooser.setSelected(true);
        }

        final int chooserIndex = mEnabledChoosers.indexOf(mSelectedChooser);
        if (mViewPager != null) {
            mViewPager.setCurrentItem(chooserIndex, true /* smoothScroll */);
        }

        invalidateOptionsMenu();

        // Save the newly selected chooser's index so we may directly switch to it the
        // next time user opens the media picker.
        mBinding.getData().saveSelectedChooserIndex(chooserIndex);
        dispatchChooserSelected(chooserIndex);
    }

    public void releaseView() {
        if (mSelectedChooser != null) {
            mSelectedChooser.destroyView();
        }
    }

    void invalidateOptionsMenu() {
        Activity activity = getActivity();
        if (activity instanceof BugleActionBarActivity) {
            ((BugleActionBarActivity) activity).supportInvalidateOptionsMenu();
        }
    }


    void dispatchDismissed() {
        setHasOptionsMenu(false);
        if (mListener != null) {
            mListenerHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onDismissed();
                }
            });
        }
        if (mSelectedChooser != null) {
            mSelectedChooser.onOpenedChanged(false);
        }
    }

    void dispatchItemsSelected(final MessagePartData item, final boolean dismissMediaPicker) {
        final List<MessagePartData> items = new ArrayList<MessagePartData>(1);
        items.add(item);
        dispatchItemsSelected(items, dismissMediaPicker);
    }

    void dispatchItemsSelected(final Collection<MessagePartData> items,
                               final boolean dismissMediaPicker) {
        if (mListener != null) {
            mListenerHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onItemsSelected(items, dismissMediaPicker);
                }
            });
        }

        if (!dismissMediaPicker) {
            invalidateOptionsMenu();
        }
    }

    void dispatchItemUnselected(final MessagePartData item) {
        if (mListener != null) {
            mListenerHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onItemUnselected(item);
                }
            });
        }
        invalidateOptionsMenu();
    }

    void dispatchConfirmItemSelection() {
        if (mListener != null) {
            mListenerHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onConfirmItemSelection();
                }
            });
        }
    }

    void dispatchPendingItemAdded(final PendingAttachmentData pendingItem) {
        if (mListener != null) {
            mListenerHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onPendingItemAdded(pendingItem);
                }
            });
        }

        invalidateOptionsMenu();
    }

    void dispatchChooserSelected(final int chooserIndex) {
        if (mListener != null) {
            mListenerHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onChooserSelected(chooserIndex);
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if (mSelectedChooser != null) {
            mSelectedChooser.onCreateOptionsMenu(inflater, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        return (mSelectedChooser != null && mSelectedChooser.onOptionsItemSelected(item)) ||
                super.onOptionsItemSelected(item);
    }

    /**
     * Launch an external picker to pick item from document picker as attachment.
     */
    public void launchDocumentPicker() {
        mDocumentImagePicker.launchPicker();
    }

    public ImmutableBindingRef<MediaPickerData> getMediaPickerDataBinding() {
        return BindingBase.createBindingReference(mBinding);
    }

    protected static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    protected static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    protected static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 3;
    protected static final int GALLERY_PERMISSION_REQUEST_CODE = 4;

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String permissions[], final int[] grantResults) {
        if (mSelectedChooser != null) {
            mSelectedChooser.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
