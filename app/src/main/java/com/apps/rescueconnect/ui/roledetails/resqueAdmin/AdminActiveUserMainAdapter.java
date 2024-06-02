package com.apps.rescueconnect.ui.roledetails.resqueAdmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.helper.encryption.AESHelper;
import com.apps.rescueconnect.model.User;

import java.util.List;

public class AdminActiveUserMainAdapter extends RecyclerView.Adapter<AdminActiveUserMainAdapter.SettingsAdapterViewHolder> {

    private static final String TAG = AdminActiveUserMainAdapter.class.getSimpleName();
    /**
     * ArrayList of type DashboardItem
     */
    private List<User> userList;
    private Context context;

    private ActiveUserItemClickListener listener;
    // endregion

    public AdminActiveUserMainAdapter(Context context, List<User> userList, ActiveUserItemClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @Override
    public SettingsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_active_user, parent, false);
        return new SettingsAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final SettingsAdapterViewHolder holder, int position) {
        try {
            if (userList.size() > 0) {
                holder.setItem(userList.get(holder.getAbsoluteAdapterPosition()));
                User user = userList.get(holder.getAbsoluteAdapterPosition());

                holder.activeSwitch.setClickable(false);

                holder.textSwitch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.userSwitchChecked(holder.getAbsoluteAdapterPosition(), user);
                    }
                });

                holder.switchLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.userSwitchChecked(holder.getAbsoluteAdapterPosition(), user);
                    }
                });


                if (user != null) {
                    holder.textName.setText(user.getFullName());
                    holder.textRole.setText(user.getMainRole());

                    String maskedMobileNumber = Utils.applyMaskAndShowLastDigits(AESHelper.decryptData(user.getMobileNumber()), 4);
                    holder.textMobileNumber.setText(maskedMobileNumber);

                    if (user.getIsActive().equalsIgnoreCase(AppConstants.ACTIVE_USER)) {
                        holder.activeSwitch.setChecked(true);
                        holder.textSwitch.setText(AppConstants.ACTIVE_USER);
                        holder.textSwitch.setTextColor(context.getColor(R.color.colorSuccess));
                    } else {
                        holder.activeSwitch.setChecked(false);
                        holder.textSwitch.setText(AppConstants.IN_ACTIVE_USER);
                        holder.textSwitch.setTextColor(context.getColor(R.color.colorError));
                    }

                    holder.textName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.userItemClicked(holder.getAbsoluteAdapterPosition(), user);
                        }
                    });

                    holder.textMobileNumber.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.userItemClicked(holder.getAbsoluteAdapterPosition(), user);
                        }
                    });

                    holder.textRole.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.userItemClicked(holder.getAbsoluteAdapterPosition(), user);
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
        return userList.size();
    }

    public interface ActiveUserItemClickListener {
        void userItemClicked(int position, User user);

        void userSwitchChecked(int position, User user);
    }

    static class SettingsAdapterViewHolder extends RecyclerView.ViewHolder {
        User user;
        TextView textName, textMobileNumber, textRole, textSwitch;
        Switch activeSwitch;
        LinearLayout switchLayout;

        SettingsAdapterViewHolder(View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.text_name);
            textRole = itemView.findViewById(R.id.text_role);
            textMobileNumber = itemView.findViewById(R.id.text_mobile_number);
            textSwitch = itemView.findViewById(R.id.text_switch);
            switchLayout = itemView.findViewById(R.id.switch_layout);

            activeSwitch = itemView.findViewById(R.id.active_switch);
        }

        public void setItem(User User) {
            this.user = user;
        }
    }
}
