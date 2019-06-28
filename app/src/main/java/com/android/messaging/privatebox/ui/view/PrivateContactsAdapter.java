package com.android.messaging.privatebox.ui.view;

import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.datamodel.data.PrivateContactItemData;
import com.android.messaging.ui.ContactIconView;
import com.android.messaging.util.Assert;
import com.android.messaging.util.AvatarUriUtil;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ContactUtil;
import com.android.messaging.util.UiUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.List;

public class PrivateContactsAdapter extends RecyclerView.Adapter<PrivateContactsAdapter.ViewHolder>{

    public interface PrivateContactsHost {
        void onPrivateContactsRemoveButtonClick(PrivateContactItemData conversationListItemData, boolean isPrivateContactListEmpty);
    }

    private PrivateContactsHost mHost;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mMainTitle, mSubTitle;
        ImageView mRemoveButton;
        ContactIconView mContactIconView;
        public ViewHolder(View itemView) {
            super(itemView);
            mMainTitle = itemView.findViewById(R.id.main_title);
            mSubTitle = itemView.findViewById(R.id.sub_title);
            mContactIconView = itemView.findViewById(R.id.private_contact_icon);
            mRemoveButton = itemView.findViewById(R.id.contact_remove_button);
        }
    }

    private List<PrivateContactItemData> mRecyclerDataList = new ArrayList<>();

    public void updateData(List<PrivateContactItemData> contactListItemData) {
        mRecyclerDataList = contactListItemData;
        notifyDataSetChanged();
    }

    @Override
    public PrivateContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.private_contacts_sub_category, parent, false);
        PrivateContactsAdapter.ViewHolder holder = new PrivateContactsAdapter.ViewHolder(view);
        holder.mRemoveButton.setBackground(
                BackgroundDrawables.createBackgroundDrawable(Color.WHITE,
                        UiUtils.getColorDark(Color.WHITE), Dimensions.pxFromDp(25), false, true));
        holder.mRemoveButton.setOnClickListener(view1 -> {
            int position = holder.getAdapterPosition();
            if (position != -1) {
                PrivateContactItemData privateContactItemData = mRecyclerDataList.get(position);
                removeData(position);
                mHost.onPrivateContactsRemoveButtonClick(privateContactItemData, getItemCount() == 0);
                BugleAnalytics.logEvent("PrivateBox_PrivateContacts_Move_Click");
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final PrivateContactsAdapter.ViewHolder holder, int position) {
        final PrivateContactItemData contactInfo = mRecyclerDataList.get(position);
        setContactImage(contactInfo, holder);
        if (contactInfo.getRecipientEntry() != null && ContactUtil.isValidContactId(contactInfo.getContactId()) ) {
            holder.mMainTitle.setText(contactInfo.getDisplayName());
            holder.mSubTitle.setText(contactInfo.getDestination());
        } else {
            holder.mMainTitle.setText(contactInfo.getDisplayName());
            holder.mSubTitle.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mRecyclerDataList.size();
    }

    private void removeData(int position) {
        mRecyclerDataList.remove(position);
        notifyItemRemoved(position);
    }

    public void setHost(final PrivateContactsHost host) {
        Assert.isNull(mHost);
        mHost = host;
    }

    private void setContactImage(PrivateContactItemData data, final PrivateContactsAdapter.ViewHolder holder) {
        if (data.getRecipientEntry() == null) {
            final Uri avatarUri = AvatarUriUtil.createAvatarUri(
                    null, data.getDisplayName(), data.getDisplayName().toString(), null);
            holder.mContactIconView.setImageResourceUri(avatarUri);
        } else {
            final Uri avatarUri = AvatarUriUtil.createAvatarUri(
                    ParticipantData.getFromRecipientEntry(data.getRecipientEntry()));
            holder.mContactIconView.setImageResourceUri(avatarUri, data.getContactId(),
                    data.getLookupKey(), data.getDestination().toString());
        }
    }
}
