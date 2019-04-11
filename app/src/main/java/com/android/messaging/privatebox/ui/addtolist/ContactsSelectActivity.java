package com.android.messaging.privatebox.ui.addtolist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactsSelectActivity extends HSAppCompatActivity {

    public final static String EXTRA_MODE_TYPE = "EXTRA_MODE_TYPE";

    public static final int MODE_CONTACTS_LIST_FOR_BLACKLIST = 0;
    public static final int MODE_WHITELIST = 4;

    private static final int WHITELIST_REQUEST_CODE = 1111;

    private ContactsSelectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_call_blocker);
        getWindow().setBackgroundDrawable(null);

        final int moduleType = getIntent().getIntExtra(EXTRA_MODE_TYPE, MODE_CONTACTS_LIST_FOR_BLACKLIST);

        startQueryData(moduleType);

        adapter = new ContactsSelectAdapter(moduleType == MODE_WHITELIST);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.contacts));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.fragment_missed_calls_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        adapter.setOnButtonClickListener(new ContactsSelectAdapter.OnButtonClickListener() {
            @Override
            public void onItemViewClicked(String name, final String number, String avatarUriStr) {

            }
        });

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
                for (CallAssistantUtils.ContactInfo contactInfo : adapter.getRecyclerDataList()) {
                    if (contactInfo.customInfo.equals(Boolean.TRUE) && !TextUtils.isEmpty(contactInfo.number)) {
                        addList.add(contactInfo.number);
                    }
                }

                switch (moduleType) {
                    case MODE_CONTACTS_LIST_FOR_BLACKLIST:
                        break;
                }

                onBackPressed();
            }
        });
    }

    private void startQueryData(final int moduleType) {
        Threads.postOnThreadPoolExecutor(() -> {
            final List<CallAssistantUtils.ContactInfo> list = new ArrayList<>();

            switch (moduleType) {
                case MODE_CONTACTS_LIST_FOR_BLACKLIST:
                    List<CallAssistantUtils.ContactInfo> contactsList = CallAssistantUtils.getAllContactsFromPhoneBook();
                    Collections.sort(contactsList, (o1, o2) -> Collator.getInstance().compare(o1.toString(), o2.toString()));
                    list.addAll(contactsList);
                    break;
            }

            Threads.postOnMainThread(() -> adapter.updateData(list));
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == WHITELIST_REQUEST_CODE) {
            startQueryData(MODE_WHITELIST);
        }
    }
}
