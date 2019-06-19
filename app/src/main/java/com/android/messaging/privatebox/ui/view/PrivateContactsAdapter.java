package com.android.messaging.privatebox.ui.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ContactUtil;
import com.superapps.util.Toasts;

import java.util.ArrayList;
import java.util.List;

public class PrivateContactsAdapter extends RecyclerView.Adapter<PrivateContactsAdapter.ViewHolder>{

    public interface PrivateContactsHost {
        void onPrivateContactsRemoveButtonClick(ConversationListItemData conversationListItemData);
    }
    private PrivateContactsHost mHost;
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mMainTitle, mSubTitle;
        ImageView mRemoveButton, mAvatarImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mMainTitle = itemView.findViewById(R.id.main_title);
            mSubTitle = itemView.findViewById(R.id.sub_title);
            mRemoveButton = itemView.findViewById(R.id.call_assistant_sub_category_checked);
            mAvatarImageView = itemView.findViewById(R.id.missied_calls_sub_category_icon);
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
        holder.mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                mHost.onPrivateContactsRemoveButtonClick(mRecyclerDataList.get(position));
                Toasts.showToast(R.string.private_box_move_from_success);
                removeData(position);
                BugleAnalytics.logEvent("PrivateBox_PrivateContacts_Move_Click");
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final PrivateContactsAdapter.ViewHolder holder, int position) {
        final ConversationListItemData contactInfo = mRecyclerDataList.get(position);
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
}
