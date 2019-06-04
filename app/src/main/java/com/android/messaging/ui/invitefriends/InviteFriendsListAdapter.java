package com.android.messaging.ui.invitefriends;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.privatebox.ui.addtolist.CallAssistantUtils;
import com.android.messaging.util.BugleAnalytics;

import java.util.List;

import static com.android.messaging.ui.invitefriends.InviteFriendsActivity.REQUEST_CODE_ADD_FRIENDS;

public class InviteFriendsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity mActivity;

    private List<CallAssistantUtils.ContactInfo> mContactInfos;

    public InviteFriendsListAdapter(Activity activity) {
        mActivity = activity;
    }


    public String getRecipients() {
        StringBuilder recipients = new StringBuilder();
        if (mContactInfos != null) {
            for (CallAssistantUtils.ContactInfo contactInfo : mContactInfos) {
                recipients.append(contactInfo.number);
                recipients.append(",");
            }
            return recipients.toString();
        }
        return "";
    }

    public void initData(List<CallAssistantUtils.ContactInfo> contactInfos) {
        mContactInfos = contactInfos;
        notifyDataSetChanged();
    }

    public void addContact(List<CallAssistantUtils.ContactInfo> contactInfos) {
        if (contactInfos == null) {
            return;
        }
        int preCount = getItemCount();

        for (CallAssistantUtils.ContactInfo contactInfo : contactInfos) {
            if (!mContactInfos.contains(contactInfo)) {
                mContactInfos.add(contactInfo);
            }
        }

        int currentCount = getItemCount();
        notifyItemRangeInserted(preCount, currentCount - preCount);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mActivity).inflate(R.layout.invite_friends_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        viewHolder.mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int target = viewHolder.getAdapterPosition();
                mContactInfos.remove(target - 1);
                notifyItemRemoved(target);
                BugleAnalytics.logEvent("Invite_SendPage_Delete_Click");
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            if (position == 0) {
                ((ViewHolder) holder).mContactIcon.setImageResource(R.drawable.ic_add_gray);
                ((ViewHolder) holder).mContactIcon.setBackgroundResource(R.drawable.gray_circle_btn_bg_drawable);
                ((ViewHolder) holder).mContactIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(mActivity, SelectFriendsToInviteActivity.class);
                    mActivity.startActivityForResult(intent, REQUEST_CODE_ADD_FRIENDS);
                    BugleAnalytics.logEvent("Invite_SendPage_Add_Click");

                });
                ((ViewHolder) holder).mDeleteBtn.setVisibility(View.GONE);
            } else {
                position -= 1;
                GlideApp.with(mActivity).load(mContactInfos.get(position).avatarUriStr).placeholder(R.drawable.default_contact_avatar).circleCrop().into(((ViewHolder) holder).mContactIcon);
                ((ViewHolder) holder).mContactName.setText(mContactInfos.get(position).name);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (mContactInfos == null ? 0 : mContactInfos.size()) + 1;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView mContactIcon;
        TextView mContactName;
        ImageView mDeleteBtn;

        ViewHolder(View itemView) {
            super(itemView);
            mContactIcon = itemView.findViewById(R.id.contact_icon);
            mContactName = itemView.findViewById(R.id.contact_name);
            mDeleteBtn = itemView.findViewById(R.id.delete_friends);
        }
    }
}
