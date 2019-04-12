package com.android.messaging.privatebox.ui.addtolist;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Choreographer;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.action.DeleteConversationAction;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.privatebox.MessagesMoveManager;
import com.android.messaging.privatebox.ui.PrivateMultiSelectActionModeCallback;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Toasts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.android.messaging.datamodel.data.ConversationListData.SORT_ORDER;


public class ConversationSelectActivity extends HSAppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CONVERSATION_LIST_LOADER = 1;
    public static final String PREF_KEY_ADD_PRIVATE_DIALOG_HAS_PROMPT = "pref_key_add_private_dialog_has_prompt";

    private ConversationSelectAdapter mAdapter;
    private LoaderManager mLoaderManager;

    private volatile boolean mIsMessageMoving;
    private View mProcessBarContainer;
    private ProgressBar mProgressBar;

    private long mStartTime;
    private Choreographer mChoreographer;
    private Choreographer.FrameCallback mFrameCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_call_blocker);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.conversation_list));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.fragment_missed_calls_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new ConversationSelectAdapter();
        recyclerView.setAdapter(mAdapter);

        View actionButton = findViewById(R.id.fragment_call_assistant_button);
        actionButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                Dimensions.pxFromDp(3.3f), true));
        actionButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                view.setPressed(true);
                return false;
            }
        });

        actionButton.setOnClickListener(v -> {
            if (Preferences.getDefault().getBoolean(PREF_KEY_ADD_PRIVATE_DIALOG_HAS_PROMPT, false)) {
                addAndMoveConversations();
            } else {
                new BaseAlertDialog.Builder(ConversationSelectActivity.this)
                        .setTitle(R.string.private_move_tip)
                        .setPositiveButton(R.string.welcome_set_default_button, (dialog, button) -> addAndMoveConversations())
                        .setNegativeButton(R.string.delete_conversation_decline_button, null)
                        .show();
                Preferences.getDefault().putBoolean(ConversationSelectActivity.PREF_KEY_ADD_PRIVATE_DIALOG_HAS_PROMPT, true);
            }
        });

        mLoaderManager = getLoaderManager();
        mLoaderManager.initLoader(CONVERSATION_LIST_LOADER, null, this);

        mProcessBarContainer = findViewById(R.id.private_progress_bar_container);
        mProgressBar = findViewById(R.id.private_move_progress_bar);

        mChoreographer = Choreographer.getInstance();
        mFrameCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (mIsMessageMoving) {
                    mProgressBar.setProgress((int) ((System.currentTimeMillis() - mStartTime) / 10));
                    mChoreographer.postFrameCallback(this);
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (mIsMessageMoving) {
            return;
        }
        super.onBackPressed();
    }

    private void addAndMoveConversations() {
        List<String> addList = new ArrayList<>();
        HashMap<ConversationListItemData, Boolean> selectedMap = mAdapter.getSelectedMap();
        for (ConversationListItemData data : selectedMap.keySet()) {
            if (selectedMap.get(data)) {
                addList.add(data.getConversationId());
            }
        }

        if (addList.size() > 0) {
            MessagesMoveManager.moveConversations(addList, false,
                    new MessagesMoveManager.MessagesMoveListener() {
                        @Override
                        public void onMoveStart() {
                            startMessagesMoveProgress();
                        }

                        @Override
                        public void onMoveEnd() {
                            stopMessageMoveProgress();
                            onBackPressed();
                        }
                    });
        }
    }

    private void startMessagesMoveProgress() {
        if (mIsMessageMoving) {
            return;
        }
        mStartTime = System.currentTimeMillis();
        mIsMessageMoving = true;
        mProcessBarContainer.setVisibility(View.VISIBLE);
        mChoreographer.postFrameCallback(mFrameCallback);
    }

    private void stopMessageMoveProgress() {
        if (!mIsMessageMoving) {
            return;
        }
        mIsMessageMoving = false;
        mChoreographer.removeFrameCallback(mFrameCallback);
        mProcessBarContainer.setVisibility(View.GONE);
        Toasts.showToast(R.string.private_box_add_success);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader = new CursorLoader(ConversationSelectActivity.this,
                MessagingContentProvider.CONVERSATIONS_URI,
                ConversationListItemData.PROJECTION,
                "",
                null,
                SORT_ORDER);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        List<ConversationListItemData> dataList = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ConversationListItemData itemData = new ConversationListItemData();
                itemData.bind(cursor);
                if (!itemData.isPrivate()) {
                    dataList.add(itemData);
                }
            } while (cursor.moveToNext());
        }

        mAdapter.updateData(dataList);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
