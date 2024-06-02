package com.apps.rescueconnect.ui.settings;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.apps.rescueconnect.R;

import java.util.List;

public class SettingsMainAdapter extends RecyclerView.Adapter<SettingsMainAdapter.SettingsAdapterViewHolder> {

    private static final String TAG = SettingsMainAdapter.class.getSimpleName();
    /**
     * ArrayList of type DashboardItem
     */
    private List<String> settingOptionsList;
    private Context context;

    private SettingsItemClickListener listener;
    // endregion

    public SettingsMainAdapter(Context context, List<String> settingOptionsList, SettingsItemClickListener listener) {
        this.context = context;
        this.settingOptionsList = settingOptionsList;
        this.listener = listener;
    }

    @Override
    public SettingsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_settings_main, parent, false);
        return new SettingsAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final SettingsAdapterViewHolder holder, int position) {
        try {
            if (settingOptionsList.size() > 0) {
                holder.setItem(settingOptionsList.get(holder.getAbsoluteAdapterPosition()));

                String settingsItem = settingOptionsList.get(holder.getAbsoluteAdapterPosition());

                if (settingsItem != null) {
                    holder.textTitle.setText(settingsItem);

                    String itemTitleWithLower = settingsItem.replaceAll(" ", "_").toLowerCase();
                    String iconName = "ic_" + itemTitleWithLower;
                    Log.d(TAG, "getDashboardItemList: iconName: " + iconName);

                  /*  Glide.with(holder.itemView)
                            .load(Utils.getImageFromDrawable(context, iconName))
                            .fitCenter()
                            .centerCrop()
                            .into(holder.imageIcon);
*/
                    holder.imageIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.settingsItemClicked(holder.getAbsoluteAdapterPosition(), settingsItem);
                        }
                    });

                    holder.textTitle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.settingsItemClicked(holder.getAbsoluteAdapterPosition(), settingsItem);
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return settingOptionsList.size();
    }

    public interface SettingsItemClickListener {
        void settingsItemClicked(int position, String item);
    }

    static class SettingsAdapterViewHolder extends RecyclerView.ViewHolder {

        String item;
        ImageView imageIcon;
        TextView textTitle;

        SettingsAdapterViewHolder(View itemView) {
            super(itemView);

            // Image View
            imageIcon = itemView.findViewById(R.id.item_icon);
            // Text View
            textTitle = itemView.findViewById(R.id.item_title);
        }

        public void setItem(String item) {
            this.item = item;
        }
    }
}
