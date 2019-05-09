package com.android.messaging.privatebox.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewGroupCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.annotation.VisibleForAnimation;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.binding.Binding;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.data.ConversationListData;
import com.android.messaging.datamodel.data.ConversationListData.ConversationListDataListener;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.privatebox.ui.addtolist.AddToListDialog;
import com.android.messaging.privatebox.ui.addtolist.ContactsSelectActivity;
import com.android.messaging.privatebox.ui.addtolist.ConversationSelectActivity;
import com.android.messaging.ui.BugleAnimationTags;
import com.android.messaging.ui.SnackBarInteraction;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.conversationlist.ConversationListAdapter;
import com.android.messaging.ui.conversationlist.ConversationListItemView;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.WallpaperDrawables;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ImeUtil;
import com.google.common.annotations.VisibleForTesting;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;

import java.util.ArrayList;
import java.util.List;

public class PrivateConversationListFragment extends Fragment
        implements ConversationListDataListener, ConversationListItemView.HostInterface {

    private static final String BUNDLE_FORWARD_MESSAGE_MODE = "forward_message_mode";

    //for multi op when long press
    public interface PrivateSelectModeHost {
        void onConversationClick(final ConversationListData listData,
                                 final ConversationListItemData conversationListItemData,
                                 final boolean isLongClick,
                                 final ConversationListItemView conversationView);

        boolean isConversationSelected(final String conversationId);

        boolean isSelectionMode();
    }

    private boolean mForwardMessageMode;

    private PrivateSelectModeHost mHost;
    private RecyclerView mRecyclerView;
    private View mEmptyListMessageView;
    private ConversationListAdapter mAdapter;

    // Saved Instance State Data - only for temporal data which is nice to maintain but not
    // critical for correctness.
    private static final String SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY =
            "conversationListViewState";
    private Parcelable mListState;

    @VisibleForTesting
    final Binding<ConversationListData> mListBinding = BindingBase.createBinding(this);

    /**
     * {@inheritDoc} from Fragment
     */
    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        mListBinding.getData().init(getLoaderManager(), mListBinding);
        mAdapter = new ConversationListAdapter(getActivity(), this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Assert.notNull(mHost);
        setScrolledToNewestConversationIfNeeded();
        updateUi();
    }

    public void setScrolledToNewestConversationIfNeeded() {
        if (!mForwardMessageMode
                && isScrolledToFirstConversation()
                && getActivity().hasWindowFocus()) {
            mListBinding.getData().setScrolledToNewestConversation(true);
        }
    }

    private boolean isScrolledToFirstConversation() {
        int firstItemPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                .findFirstCompletelyVisibleItemPosition();
        return firstItemPosition == 0;
    }

    /**
     * {@inheritDoc} from Fragment
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mListBinding.unbind();
        mHost = null;
    }

    /**
     * {@inheritDoc} from Fragment
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.private_conversation_list_fragment,
                container, false);
        ImageView conversationListBg = rootView.findViewById(R.id.private_conversation_list_bg);
        conversationListBg.setImageDrawable(WallpaperDrawables.getListWallpaperBg());
        mEmptyListMessageView = rootView.findViewById(R.id.private_box_empty_container);
        mRecyclerView = rootView.findViewById(android.R.id.list);
        mRecyclerView.setHasFixedSize(true);
        final Activity activity = getActivity();
        LinearLayoutManager manager = new LinearLayoutManager(activity) {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        };
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            int mCurrentState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

            private boolean isFirstConversationVisible = true;

            @Override
            public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                if (mCurrentState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
                        || mCurrentState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    ImeUtil.get().hideImeKeyboard(getActivity(), mRecyclerView);
                }

                isFirstConversationVisible = isScrolledToFirstConversation();
                if (isFirstConversationVisible) {
                    setScrolledToNewestConversationIfNeeded();
                } else {
                    mListBinding.getData().setScrolledToNewestConversation(false);
                }
            }

            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                mCurrentState = newState;
            }
        });

        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY);
        }

        ViewGroupCompat.setTransitionGroup(rootView, false);

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            mForwardMessageMode = arguments.getBoolean(BUNDLE_FORWARD_MESSAGE_MODE, false);
        }
        mListBinding.bind(DataModel.get().createConversationListData(activity, this, false));
    }


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mListState != null) {
            outState.putParcelable(SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY, mListState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mListState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        mListBinding.getData().setScrolledToNewestConversation(false);
    }

    /**
     * Call this immediately after attaching the fragment
     */
    public void setHost(final PrivateSelectModeHost host) {
        Assert.isNull(mHost);
        mHost = host;
    }

    @Override
    public void onConversationListCursorUpdated(final ConversationListData data,
                                                final Cursor cursor) {
        mListBinding.ensureBound(data);

        ArrayList<Object> dataList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ConversationListItemData itemData = new ConversationListItemData();
                itemData.bind(cursor);
                if (itemData.isPrivate()) {
                    dataList.add(itemData);
                }
            } while (cursor.moveToNext());
        }

        mAdapter.setDataList(dataList);
        updateEmptyListUi(dataList.isEmpty());
    }

    @Override
    public void setBlockedParticipantsAvailable(final boolean blockedAvailable) {

    }

    public void updateUi() {
        mAdapter.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc} from ConversationListItemView.HostInterface
     */
    @Override
    public void onConversationClicked(final ConversationListItemData conversationListItemData,
                                      final boolean isLongClick, final ConversationListItemView conversationView) {
        final ConversationListData listData = mListBinding.getData();
        mHost.onConversationClick(listData, conversationListItemData, isLongClick,
                conversationView);
    }

    /**
     * {@inheritDoc} from ConversationListItemView.HostInterface
     */
    @Override
    public boolean isConversationSelected(final String conversationId) {
        return mHost.isConversationSelected(conversationId);
    }

    @Override
    public boolean isSwipeAnimatable() {
        return mHost.isSelectionMode();
    }

    @Override
    public List<SnackBarInteraction> getSnackBarInteractions() {
        return null;
    }

    // Show and hide empty list UI as needed with appropriate text based on view specifics
    private void updateEmptyListUi(final boolean isEmpty) {
        if (isEmpty) {
            mEmptyListMessageView.setVisibility(View.VISIBLE);
            View addNowBtn = mEmptyListMessageView.findViewById(R.id.private_box_empty_view_add_btn);
            addNowBtn.setBackground(
                    BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                            Dimensions.pxFromDp(18), true));
            addNowBtn.setOnClickListener(v -> {
                BugleAnalytics.logEvent("PrivateBox_Homepage_AddContact_BtnClick");
                final AddToListDialog addToBlackListDialog = new AddToListDialog(getActivity());
                addToBlackListDialog.setOnButtonClickListener(new AddToListDialog.OnButtonClickListener() {
                    @Override
                    public void onFromConversationClick() {
                        BugleAnalytics.logEvent("PrivateBox_AddContactAlert_BtnClick", "type", "conversation");
                        Navigations.startActivitySafely(getActivity(),
                                new Intent(getActivity(), ConversationSelectActivity.class));
                        addToBlackListDialog.dismiss();
                    }

                    @Override
                    public void onFromContactsClick() {
                        BugleAnalytics.logEvent("PrivateBox_AddContactAlert_BtnClick", "type", "contact");
                        Navigations.startActivitySafely(getActivity(),
                                new Intent(getActivity(), ContactsSelectActivity.class));
                        addToBlackListDialog.dismiss();
                    }
                });

                addToBlackListDialog.show();
            });
            ((TextView)mEmptyListMessageView.findViewById(R.id.private_box_empty_view_description))
                    .setTextColor(ConversationColors.get().getListTitleColor());
        } else {
            mEmptyListMessageView.setVisibility(View.GONE);
        }
    }

    @VisibleForAnimation
    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public void startFullScreenPhotoViewer(
            final Uri initialPhoto, final Rect initialPhotoBounds, final Uri photosUri) {
        UIIntents.get().launchFullScreenPhotoViewer(
                getActivity(), initialPhoto, initialPhotoBounds, photosUri);
    }

    @Override
    public void startFullScreenVideoViewer(final Uri videoUri) {
        UIIntents.get().launchFullScreenVideoViewer(getActivity(), videoUri);
    }

    @Override
    public boolean isSelectionMode() {
        return mHost != null && mHost.isSelectionMode();
    }
}
