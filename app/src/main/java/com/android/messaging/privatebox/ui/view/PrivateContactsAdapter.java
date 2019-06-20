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
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.ui.ContactIconView;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ContactUtil;
import com.android.messaging.util.UiUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.List;

public class PrivateContactsAdapter extends RecyclerView.Adapter<PrivateContactsAdapter.ViewHolder>{

    public interface PrivateContactsHost {
        void onPrivateContactsRemoveButtonClick(ConversationListItemData conversationListItemData, boolean isPrivateContactListEmpty);
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

    private List<ConversationListItemData> mRecyclerDataList = new ArrayList<>();

    public void updateData(List<ConversationListItemData> data) {
        mRecyclerDataList.clear();
        mRecyclerDataList.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public PrivateContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.private_contacts_sub_category, parent, false);
        PrivateContactsAdapter.ViewHolder holder = new PrivateContactsAdapter.ViewHolder(view);
        holder.mRemoveButton.setBackground(
                BackgroundDrawables.createBackgroundDrawable(Color.WHITE,
                        UiUtils.getColorDark(Color.WHITE), Dimensions.pxFromDp(25), false, true));
        holder.mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if(position != -1) {
                    ConversationListItemData conversationListItemData = mRecyclerDataList.get(position);
                    removeData(position);
                    mHost.onPrivateContactsRemoveButtonClick(conversationListItemData, getItemCount() == 0);
                    BugleAnalytics.logEvent("PrivateBox_PrivateContacts_Move_Click");
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final PrivateContactsAdapter.ViewHolder holder, int position) {
        final ConversationListItemData contactInfo = mRecyclerDataList.get(position);
        setContactImage(contactInfo, holder);
        if (ContactUtil.isValidContactId(contactInfo.getParticipantContactId())) {
            holder.mMainTitle.setText(contactInfo.getName());
            holder.mSubTitle.setText(contactInfo.getOtherParticipantNormalizedDestination());
        } else {
            holder.mMainTitle.setText(contactInfo.getName());
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

    private void setContactImage(ConversationListItemData data, final PrivateContactsAdapter.ViewHolder holder) {
        Uri iconUri = null;
        String imgUri = data.getIcon();
        if (!data.getIsRead()) {
            //unread
            if (!TextUtils.isEmpty(imgUri)) {
                imgUri = imgUri.concat("unread");
            }
        }
        if (!TextUtils.isEmpty(imgUri)) {
            iconUri = Uri.parse(imgUri);
        }
        holder.mContactIconView.setImageResourceUri(iconUri, data.getParticipantContactId(),
                data.getParticipantLookupKey(), data.getOtherParticipantNormalizedDestination(), Color.TRANSPARENT);
    }
}
