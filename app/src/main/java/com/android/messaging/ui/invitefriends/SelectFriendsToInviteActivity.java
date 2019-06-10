package com.android.messaging.ui.invitefriends;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.privatebox.PrivateContactsManager;
import com.android.messaging.privatebox.ui.addtolist.CallAssistantUtils;
import com.android.messaging.privatebox.ui.addtolist.ContactsSelectAdapter;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.UiUtils;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectFriendsToInviteActivity extends BaseActivity {

    private ContactsSelectAdapter mAdapter;
    private TextView mActionButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends_to_select);

        startQueryData();

        mAdapter = new ContactsSelectAdapter();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.invite_friends_default_back_to_main_page_title));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        RecyclerView recyclerView = findViewById(R.id.fragment_missed_calls_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnSelectCountChangeListener(count -> mActionButton.setText(String.format(getString(R.string.invite_friends_add), count)));

        mActionButton = findViewById(R.id.private_box_add_btn);
        mActionButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                Dimensions.pxFromDp(3.3f), true));
        mActionButton.setText(String.format(getString(R.string.invite_friends_add), 0));

        mActionButton.setOnClickListener(v -> {
            List<CallAssistantUtils.ContactInfo> addList = new ArrayList<>();
            for (CallAssistantUtils.ContactInfo contactInfo : mAdapter.getRecyclerDataList()) {
                if (contactInfo.customInfo.equals(Boolean.TRUE) && !TextUtils.isEmpty(contactInfo.number)) {
                    addList.add(contactInfo);
                }
            }

            if (addList.size() <= 0) {
                return;
            }
            InviteFriendsList.setAddedInvitedFriendsList(addList);
            setResult(RESULT_OK);
            finish();
            BugleAnalytics.logEvent("Invite_AddList_Add_Click", "num", String.valueOf(addList.size()));

        });

    }

    private void startQueryData() {
        Threads.postOnThreadPoolExecutor(() -> {
            List<CallAssistantUtils.ContactInfo> contactsList = CallAssistantUtils.getAllContactsFromPhoneBook();
            // filter contact which is already private
            List<String> privateRecipients = PrivateContactsManager.getInstance().getPrivateRecipientList();
            List<CallAssistantUtils.ContactInfo> filterContactsList = new ArrayList<>();
            for (CallAssistantUtils.ContactInfo info : contactsList) {
                String recipient = PhoneUtils.getDefault().getCanonicalBySimLocale(info.number.trim());
                if (!privateRecipients.contains(recipient)) {
                    filterContactsList.add(info);
                }
            }
            Collections.sort(filterContactsList, (o1, o2) -> Collator.getInstance().compare(o1.toString(), o2.toString()));
            final List<CallAssistantUtils.ContactInfo> list = new ArrayList<>(filterContactsList);

            Threads.postOnMainThread(() -> {
                mAdapter.updateData(list);

                LottieAnimationView lottieAnimationView = findViewById(R.id.loading_image_hint);
                lottieAnimationView.cancelAnimation();
                lottieAnimationView.setVisibility(View.GONE);
            });
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
