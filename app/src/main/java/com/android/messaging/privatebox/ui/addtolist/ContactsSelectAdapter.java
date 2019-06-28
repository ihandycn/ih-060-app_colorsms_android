package com.android.messaging.privatebox.ui.addtolist;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.invitefriends.SelectFriendsToInviteActivity;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.PhoneUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactsSelectAdapter extends RecyclerView.Adapter<ContactsSelectAdapter.ViewHolder> {

    public interface OnSelectCountChangeListener {
        void onChange(int count);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mainTitle, subTitle;
        ImageView checkBoxView, avatarImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mainTitle = itemView.findViewById(R.id.main_title);
            subTitle = itemView.findViewById(R.id.sub_title);
            checkBoxView = itemView.findViewById(R.id.call_assistant_sub_category_checked);
            avatarImageView = itemView.findViewById(R.id.missied_calls_sub_category_icon);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 0);

            subTitle.setLayoutParams(params);
        }
    }

    private int selectedCount;
    private OnSelectCountChangeListener mOnSelectCountChangeListener;

    public void setOnSelectCountChangeListener(OnSelectCountChangeListener onSelectCountChangeListener) {
        mOnSelectCountChangeListener = onSelectCountChangeListener;
    }

    private List<CallAssistantUtils.ContactInfo> recyclerDataList = new ArrayList<>();

    public void updateData(List<CallAssistantUtils.ContactInfo> data) {
        this.recyclerDataList.clear();
        Map<String, String> dataMap = new HashMap<>();
        for (CallAssistantUtils.ContactInfo info : data) {
            String address = PhoneUtils.getDefault().getCanonicalBySimLocale(info.number);
            if (!dataMap.containsKey(address) || !dataMap.get(address).equals(info.name)) {
                dataMap.put(address, info.name);
                this.recyclerDataList.add(info);
            }
        }
        notifyDataSetChanged();
    }

    public List<CallAssistantUtils.ContactInfo> getRecyclerDataList() {
        return new ArrayList<>(recyclerDataList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.call_assistant_sub_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final CallAssistantUtils.ContactInfo contactInfo = recyclerDataList.get(position);

        if (TextUtils.isEmpty(contactInfo.name)) {
            holder.mainTitle.setText(contactInfo.number);
            holder.subTitle.setVisibility(View.GONE);
        } else {
            holder.mainTitle.setText(contactInfo.name);
            holder.subTitle.setText(contactInfo.number);
            holder.subTitle.setVisibility(View.VISIBLE);
        }

        ImageView checkbox = holder.checkBoxView;

        if (contactInfo.customInfo.equals(Boolean.TRUE)) {
            checkbox.setImageResource(R.drawable.conversation_check);
            checkbox.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    PrimaryColors.getPrimaryColor(), Dimensions.pxFromDp(20), false));
        } else {
            checkbox.setImageDrawable(null);
            checkbox.setBackground(BackgroundDrawables.createBackgroundDrawable(0, 0, 4,
                    0xffbdc2c9, Dimensions.pxFromDp(20), false, false));
        }

        holder.itemView.setOnClickListener(view -> {
            if (contactInfo.customInfo.equals(Boolean.TRUE)) {
                contactInfo.customInfo = Boolean.FALSE;
                checkbox.setImageDrawable(null);
                checkbox.setBackground(BackgroundDrawables.createBackgroundDrawable(0, 0, 4,
                        0xffbdc2c9, Dimensions.pxFromDp(20), false, false));
                selectedCount--;
            } else {
                contactInfo.customInfo = Boolean.TRUE;
                checkbox.setImageResource(R.drawable.conversation_check);
                checkbox.setBackground(BackgroundDrawables.createBackgroundDrawable(
                        PrimaryColors.getPrimaryColor(), Dimensions.pxFromDp(20), false));
                selectedCount++;
            }

            if (mOnSelectCountChangeListener != null) {
                mOnSelectCountChangeListener.onChange(selectedCount);
            }

            if (holder.itemView.getContext() instanceof SelectFriendsToInviteActivity) {
                BugleAnalytics.logEvent("Invite_AddList_Contact_Click");
            }
        });
    }

    @Override
    public int getItemCount() {
        return recyclerDataList == null ? 0 : recyclerDataList.size();
    }
}