package com.android.messaging.privatebox.ui.view;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.util.Assert;
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

        TextView mainTitle, subTitle;
        ImageView removeButton, avatarImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mainTitle = itemView.findViewById(R.id.main_title);
            subTitle = itemView.findViewById(R.id.sub_title);
            removeButton = itemView.findViewById(R.id.call_assistant_sub_category_checked);
            avatarImageView = itemView.findViewById(R.id.missied_calls_sub_category_icon);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 0);

            subTitle.setLayoutParams(params);
        }
    }

    private List<ConversationListItemData> recyclerDataList = new ArrayList<>();

    public void updateData(List<ConversationListItemData> data) {
        this.recyclerDataList.clear();
        this.recyclerDataList.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public PrivateContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.private_contacts_sub_category, parent, false);
        return new PrivateContactsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PrivateContactsAdapter.ViewHolder holder, int position) {
        final ConversationListItemData contactInfo = recyclerDataList.get(position);

        if (!TextUtils.isEmpty(contactInfo.getName())) {
            if (ContactUtil.isValidContactId(contactInfo.getParticipantContactId())) {
                holder.mainTitle.setText(contactInfo.getName());
                holder.subTitle.setText(contactInfo.getOtherParticipantNormalizedDestination());
            } else {
                holder.mainTitle.setText(contactInfo.getName());
                holder.subTitle.setVisibility(View.GONE);
            }
        } else {
            holder.mainTitle.setText(contactInfo.getOtherParticipantNormalizedDestination());
            holder.subTitle.setVisibility(View.GONE);
        }

        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHost.onPrivateContactsRemoveButtonClick(contactInfo);
                Toasts.showToast(R.string.private_box_move_from_success);
                removeData(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recyclerDataList == null ? 0 : recyclerDataList.size();
    }

    public void removeData(int position){
        recyclerDataList.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void setHost(final PrivateContactsHost host) {
        Assert.isNull(mHost);
        mHost = host;
    }
}
