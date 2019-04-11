package com.android.messaging.privatebox.ui.addtolist;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.privatebox.MessagesMoveManager;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.android.messaging.datamodel.data.ConversationListData.SORT_ORDER;


public class ConversationSelectActivity extends HSAppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CONVERSATION_LIST_LOADER = 1;

    private ConversationSelectAdapter mAdapter;
    private LoaderManager mLoaderManager;

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

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> addList = new ArrayList<>();
                HashMap<ConversationListItemData, Boolean> selectedMap = mAdapter.getSelectedMap();
                for (ConversationListItemData data : selectedMap.keySet()) {
                    if (selectedMap.get(data)) {
                        addList.add(data.getConversationId());
                    }
                }

                MessagesMoveManager.moveConversations(addList, false,
                        new MessagesMoveManager.MessagesMoveListener() {
                            @Override
                            public void onMoveStart() {

                            }

                            @Override
                            public void onMoveEnd() {

                            }
                        });

                onBackPressed();
            }
        });

        mLoaderManager = getLoaderManager();
        mLoaderManager.initLoader(CONVERSATION_LIST_LOADER, null, this);
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
