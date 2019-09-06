package com.android.messaging.ui.mediapicker.sendcontact;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEntry;
import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.binding.Binding;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.data.ContactListItemData;
import com.android.messaging.datamodel.data.ContactPickerData;
import com.android.messaging.ui.ContactsListViewWrapper;
import com.android.messaging.ui.contact.ContactDropdownLayouter;
import com.android.messaging.ui.contact.ContactListItemView;
import com.android.messaging.ui.contact.ContactRecipientAdapter;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ContactUtil;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.TextViewUtil;
import com.android.messaging.util.UiUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MediaContactPickerActivity extends BaseActivity
        implements ContactPickerData.ContactPickerDataListener, ContactListItemView.HostInterface {

    public static final String CONTACT_SEND_TYPE_TEXT = "text";
    public static final String CONTACT_SEND_TYPE_VCARD = "vcard";

    final Binding<ContactPickerData> mBinding = BindingBase.createBinding(this);
    private ContactsListViewWrapper mAllContactsListViewHolder;
    private AppCompatAutoCompleteTextView mRecipientTextView;
    private View mTypeSelectContainer;

    private Set<String> mSelectedPhoneNumbers = new HashSet<>();
    private List<RecipientEntry> mSelectEntry = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_select);

        UiUtils.setStatusBarColor(this, Color.WHITE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ContactSelectListAdapter adapter = new ContactSelectListAdapter(this);
        mAllContactsListViewHolder = new ContactsListViewWrapper(this, adapter);

        if (ContactUtil.hasReadContactsPermission()) {
            mBinding.bind(DataModel.get().createContactPickerData(this, this));
            mBinding.getData().init(getLoaderManager(), mBinding);
        }

        ListView listView = findViewById(R.id.all_contacts_list);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {
                if (scrollState != SCROLL_STATE_IDLE) {
                    ImeUtil.get().hideImeKeyboard(MediaContactPickerActivity.this, view);
                }
            }

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem,
                                 final int visibleItemCount, final int totalItemCount) {
            }
        });

        mRecipientTextView = findViewById(R.id.recipient_text_view);
        TextViewUtil.setCursorPointColor(mRecipientTextView, PrimaryColors.DEFAULT_PRIMARY_COLOR);
        mRecipientTextView.setThreshold(0);
        mRecipientTextView.setDropDownAnchor(R.id.compose_contact_divider);

        ContactRecipientAdapter adapter1 = new ContactRecipientAdapter(this, this, false);
        adapter1.setDropdownChipLayouter(new ContactDropdownLayouter(LayoutInflater.from(this), this, null));
        adapter1.registerUpdateObserver(entries -> {

        });
        mRecipientTextView.setAdapter(adapter1);
        mRecipientTextView.setOnItemClickListener((parent, view, position, id) -> {
            ContactListItemView itemView = ((ContactListItemView) view);
            RecipientEntry entry = itemView.getData().getRecipientEntry();
            if (entry != null && !mSelectedPhoneNumbers.contains(entry.getDestination())) {
                mSelectedPhoneNumbers.add(entry.getDestination());
                mSelectEntry.add(entry);
                adapter.notifyDataSetChanged();
            }
            mRecipientTextView.setText("");
            handleChooseTypeView();
        });

        mTypeSelectContainer = findViewById(R.id.contact_select_type_container);
        mTypeSelectContainer.setTranslationY(Dimensions.pxFromDp(80));

        Drawable leftCornerDrawable = BackgroundDrawables.createBackgroundDrawable(Color.WHITE,
                getResources().getColor(R.color.ripples_ripple_color),
                Dimensions.pxFromDp(24), 0, 0, Dimensions.pxFromDp(24),
                true, true);
        Drawable rightCornerDrawable = BackgroundDrawables.createBackgroundDrawable(Color.WHITE,
                getResources().getColor(R.color.ripples_ripple_color),
                0, Dimensions.pxFromDp(24), Dimensions.pxFromDp(24), 0,
                true, true);

        boolean isRtl = Dimensions.isRtl();
        View typeText = findViewById(R.id.contact_select_type_text_container);
        typeText.setBackground(isRtl ? rightCornerDrawable : leftCornerDrawable);
        typeText.setOnClickListener(v -> {
            setResult(CONTACT_SEND_TYPE_TEXT);
            BugleAnalytics.logEvent("Contact_SendTypeButton_Click", "type", "text");
        });

        View vcardText = findViewById(R.id.contact_select_type_vcard_container);
        vcardText.setBackground(isRtl ? leftCornerDrawable : rightCornerDrawable);
        vcardText.setOnClickListener(v -> {
            setResult(CONTACT_SEND_TYPE_VCARD);
            BugleAnalytics.logEvent("Contact_SendTypeButton_Click", "type", "vcard");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBinding.isBound()) {
            mBinding.unbind();
        }
    }

    private void setResult(String type) {
        Intent intent = new Intent();
        intent.putExtra("type", type);
        HashMap<String, String> contacts = new HashMap<>();
        for (int i = 0; i < mSelectEntry.size(); i++) {
            RecipientEntry entry = mSelectEntry.get(i);
            contacts.put(entry.getDestination(), entry.getDisplayName());
        }
        intent.putExtra("contacts", contacts);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAllContactsCursorUpdated(Cursor data) {
        mBinding.ensureBound();
        mAllContactsListViewHolder.onContactsCursorUpdated(data);
    }

    @Override
    public void onContactCustomColorLoaded(ContactPickerData data) {
        mBinding.ensureBound(data);
        invalidateContactLists();
    }

    private void invalidateContactLists() {
        mAllContactsListViewHolder.invalidateList();
    }

    @Override
    public void onContactListItemClicked(ContactListItemData item, View itemView) {
        if (mSelectedPhoneNumbers.contains(item.getRecipientEntry().getDestination())) {
            mSelectedPhoneNumbers.remove(item.getRecipientEntry().getDestination());
            mSelectEntry.remove(item.getRecipientEntry());
            ((ContactSelectListItemView) itemView).setSelectState(false);
        } else {
            mSelectedPhoneNumbers.add(item.getRecipientEntry().getDestination());
            mSelectEntry.add(item.getRecipientEntry());
            ((ContactSelectListItemView) itemView).setSelectState(true);
        }
        handleChooseTypeView();
    }

    @Override
    public boolean isContactSelected(ContactListItemData item) {
        return mSelectedPhoneNumbers.contains(item.getRecipientEntry().getDestination());
    }

    private void handleChooseTypeView() {
        if (mSelectedPhoneNumbers.size() == 0) {
            if (mTypeSelectContainer.getTranslationY() == Dimensions.pxFromDp(71)) {
                return;
            }
            ObjectAnimator hideAnimator = ObjectAnimator.ofFloat(mTypeSelectContainer, "translationY", Dimensions.pxFromDp(71));
            hideAnimator.setInterpolator(PathInterpolatorCompat.create(0.4f, 0, 0.68f, 0.06f));
            hideAnimator.setDuration(280);
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mTypeSelectContainer, "alpha", 0);
            alphaAnimator.setDuration(280);
            Threads.postOnMainThreadDelayed(() -> {
                hideAnimator.start();
                alphaAnimator.start();
            }, 50);
        } else {
            if (mTypeSelectContainer.getTranslationY() == 0) {
                return;
            }
            ObjectAnimator showAnimator = ObjectAnimator.ofFloat(mTypeSelectContainer,
                    "translationY", -Dimensions.pxFromDp(6));
            showAnimator.setInterpolator(PathInterpolatorCompat.create(0.32f, 0.94f, 0.6f, 1f));
            showAnimator.setDuration(240);
            showAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Animator animator = ObjectAnimator.ofFloat(mTypeSelectContainer, "translationY", 0);
                    animator.setDuration(80);
                    animator.start();
                }
            });

            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mTypeSelectContainer, "alpha", 0.4f, 1f);
            alphaAnimator.setDuration(360);
            Threads.postOnMainThreadDelayed(() -> {
                showAnimator.start();
                alphaAnimator.start();
            }, 50);
        }
    }
}
