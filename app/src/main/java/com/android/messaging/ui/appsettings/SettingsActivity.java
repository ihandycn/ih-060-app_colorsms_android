package com.android.messaging.ui.appsettings;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.binding.Binding;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.data.SettingsData;
import com.android.messaging.datamodel.data.SettingsData.SettingsDataListener;
import com.android.messaging.datamodel.data.SettingsData.SettingsItem;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.PhoneUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the "master" settings activity that contains two parts, one for application-wide settings
 * (dubbed "General settings"), and one or more for per-subscription settings (dubbed "Messaging
 * settings" for single-SIM, and the actual SIM name for multi-SIM). Clicking on either item
 * (e.g. "General settings") will open the detail settings activity (ApplicationSettingsActivity
 * in this case).
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Directly open the detailed settings page as the top-level settings activity if this is
        // not a multi-SIM device.
        if (PhoneUtils.getDefault().getActiveSubscriptionCount() <= 1) {
            UIIntents.get().launchApplicationSettingsActivity(this, true /* topLevel */);
            finish();
        } else {
            setContentView(R.layout.activity_setting);

            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle("");
            TextView title = toolbar.findViewById(R.id.toolbar_title);
            title.setText(getString(R.string.settings_activity_title));
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            getFragmentManager().beginTransaction()
                    .replace(R.id.setting_fragment_container, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends Fragment implements SettingsDataListener {
        private ListView mListView;
        private SettingsListAdapter mAdapter;
        private final Binding<SettingsData> mBinding = BindingBase.createBinding(this);

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mBinding.bind(DataModel.get().createSettingsData(getActivity(), this));
            mBinding.getData().init(getLoaderManager(), mBinding);
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                 final Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.settings_fragment, container, false);
            mListView = view.findViewById(android.R.id.list);
            mListView.setDivider(null);
            mAdapter = new SettingsListAdapter(getActivity());
            mListView.setAdapter(mAdapter);
            return view;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mBinding.unbind();
        }

        @Override
        public void onSelfParticipantDataLoaded(SettingsData data) {
            mBinding.ensureBound(data);
            mAdapter.setSettingsItems(data.getSettingsItems());
        }

        /**
         * An adapter that displays a list of SettingsItem.
         */
        private class SettingsListAdapter extends ArrayAdapter<SettingsItem> {
            SettingsListAdapter(final Context context) {
                super(context, R.layout.settings_item_view, new ArrayList<>());
            }

            void setSettingsItems(final List<SettingsItem> newList) {
                clear();
                addAll(newList);
                notifyDataSetChanged();
            }

            @Override
            public @NonNull View getView(final int position, final View convertView, final ViewGroup parent) {
                SettingItemView itemView;
                if (convertView != null) {
                    itemView = (SettingItemView) convertView;
                } else {
                    final LayoutInflater inflater = LayoutInflater.from(getContext());
                    itemView = (SettingItemView) inflater.inflate(
                            R.layout.settings_item_view, parent, false);
                }
                final SettingsItem item = getItem(position);
                final String summaryText = item != null ? item.getDisplayDetail() : null;
                itemView.setTitle(item != null ? item.getDisplayName() : "");
                if (!TextUtils.isEmpty(summaryText)) {
                    itemView.setSummary(summaryText);
                }
                itemView.setViewType(SettingItemView.WITH_TRIANGLE);
                itemView.setOnItemClickListener(() -> {
                    if (item == null) {
                        return;
                    }
                    switch (item.getType()) {
                        case SettingsItem.TYPE_GENERAL_SETTINGS:
                            UIIntents.get().launchApplicationSettingsActivity(getActivity(),
                                    false /* topLevel */);
                            break;

                        case SettingsItem.TYPE_PER_SUBSCRIPTION_SETTINGS:
                            UIIntents.get().launchPerSubscriptionSettingsActivity(getActivity(),
                                    item.getSubId(), item.getActivityTitle());
                            BugleAnalytics.logEvent("SMS_Settings_Advanced_Click", true);
                            break;

                        default:
                            Assert.fail("unrecognized setting type!");
                            break;
                    }
                });
                return itemView;
            }
        }
    }
}
