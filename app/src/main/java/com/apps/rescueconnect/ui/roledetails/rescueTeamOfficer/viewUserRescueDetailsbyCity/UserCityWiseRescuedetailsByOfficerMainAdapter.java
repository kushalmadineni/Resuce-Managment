package com.apps.rescueconnect.ui.roledetails.rescueTeamOfficer.viewUserRescueDetailsbyCity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.model.rescueRequest.RescueRequestMaster;
import com.apps.rescueconnect.model.rescueRequest.RescueRequestSubDetails;
import com.bumptech.glide.Glide;

import java.util.List;

public class UserCityWiseRescuedetailsByOfficerMainAdapter extends RecyclerView.Adapter<UserCityWiseRescuedetailsByOfficerMainAdapter.UserRescueAdapterViewHolder> {

    private static final String TAG = UserCityWiseRescuedetailsByOfficerMainAdapter.class.getSimpleName();
    /**
     * ArrayList of type PlaceItem
     */
    private List<RescueRequestMaster> rescueRequestMasterList;
    private Context context;
    // endregion

    private UserRescueItemClickListener listener;

    public UserCityWiseRescuedetailsByOfficerMainAdapter(Context context, List<RescueRequestMaster> rescueRequestMasterList, UserRescueItemClickListener listener) {
        this.context = context;
        this.rescueRequestMasterList = rescueRequestMasterList;
        this.listener = listener;
    }

    @Override
    public UserRescueAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_rescue_only_view_main, parent, false);
        return new UserRescueAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final UserRescueAdapterViewHolder holder, int position) {
        try {
            int mainPosition = holder.getAdapterPosition();
            if (rescueRequestMasterList.size() > 0) {
                holder.setItem(rescueRequestMasterList.get(mainPosition));
                RescueRequestMaster rescueRequestMaster = rescueRequestMasterList.get(mainPosition);
                if (rescueRequestMaster != null) {
                    holder.textUserRescueHeader.setText(rescueRequestMaster.getRescueRequestHeader());
                    if (rescueRequestMaster.getRescueRequestAccessType().equalsIgnoreCase(AppConstants.CALAMITY_ACCESS_TYPE_PUBLIC)) {
                        holder.textUserRescueHeader.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_public_lock, 0, 0, 0);
                        holder.textUserRescueHeader.setCompoundDrawablePadding((int) context.getResources().getDimension(R.dimen.std_10_dp));
                        holder.textUserRescueHeader.getCompoundDrawables()[0].setTint(context.getResources().getColor(R.color.colorSuccess, null));
                    } else {
                        holder.textUserRescueHeader.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_private_lock, 0, 0, 0);
                        holder.textUserRescueHeader.setCompoundDrawablePadding((int) context.getResources().getDimension(R.dimen.std_10_dp));
                        holder.textUserRescueHeader.getCompoundDrawables()[0].setTint(context.getResources().getColor(R.color.colorLightOrange, null));
                    }
                    holder.textUserRescueNumber.setText(rescueRequestMaster.getRescueRequestType());
                    holder.textUserRescueId.setText(rescueRequestMaster.getRescueRequestPlacePhotoId());
                    holder.textUserRescueCity.setText(rescueRequestMaster.getRescueRequestCity());

                    int detailsLatest = rescueRequestMaster.getRescueRequestSubDetailsList().size() - 1;
                    RescueRequestSubDetails rescueRequestSubDetails = null;
                    if (detailsLatest >= 0) {
                        rescueRequestSubDetails = rescueRequestMaster.getRescueRequestSubDetailsList().get(detailsLatest);

                        if (rescueRequestSubDetails != null) {
                            holder.textUserRescueStatus.setText(rescueRequestSubDetails.getRescueRequestStatus());
                            switch (rescueRequestSubDetails.getRescueRequestStatus()) {
                                case AppConstants
                                        .NEW_STATUS: {
                                    holder.textUserRescueStatus.setTextColor(context.getResources().getColor(R.color.colorNewUxBlue, null));
                                    break;
                                }
                                case AppConstants
                                        .ACCEPTED_STATUS: {
                                    holder.textUserRescueStatus.setTextColor(context.getResources().getColor(R.color.colorVeniceBlue, null));
                                    break;
                                }
                                case AppConstants
                                        .COMPLETED_STATUS: {
                                    holder.textUserRescueStatus.setTextColor(context.getResources().getColor(R.color.colorSuccess, null));
                                    break;
                                }
                                case AppConstants
                                        .CANCELLED_STATUS: {
                                    holder.textUserRescueStatus.setTextColor(context.getResources().getColor(R.color.colorError, null));
                                    break;
                                }
                                case AppConstants
                                        .REJECTED_STATUS: {
                                    holder.textUserRescueStatus.setTextColor(context.getResources().getColor(R.color.colorError, null));
                                    break;
                                }
                                default: {
                                    holder.textUserRescueStatus.setTextColor(context.getResources().getColor(R.color.colorNewUxBlue, null));
                                    break;
                                }
                            }
                        }
                    }

                    holder.textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.userRescueViewClicked(mainPosition, rescueRequestMaster, holder.userRescueImage, holder.textUserRescueHeader);
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

    public interface UserRescueItemClickListener {
        void userRescueViewClicked(int position, RescueRequestMaster rescueRequestMaster, ImageView imageView, TextView textView);
    }


    @Override
    public int getItemCount() {
        return rescueRequestMasterList.size();
    }

    static class UserRescueAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        RescueRequestMaster userRescue;
        ImageView userRescueImage;
        TextView textUserRescueNumber, textUserRescueStatus, textUserRescueId, textUserRescueCity,
                textUserRescueHeader, textView;

        UserRescueAdapterViewHolder(View itemView) {
            super(itemView);

            // Image View
            userRescueImage = itemView.findViewById(R.id.user_rescue_image);
            // Text View
            textUserRescueNumber = itemView.findViewById(R.id.text_user_rescue_number);
            textUserRescueStatus = itemView.findViewById(R.id.text_user_rescue_status);
            textUserRescueId = itemView.findViewById(R.id.text_user_rescue_id);
            textUserRescueCity = itemView.findViewById(R.id.text_user_rescue_city);
            textUserRescueHeader = itemView.findViewById(R.id.text_user_rescue_header);
            textView = itemView.findViewById(R.id.text_view);
        }

        public void setItem(RescueRequestMaster rescueRequestMaster) {
            userRescue = rescueRequestMaster;

            try {
                Glide.with(itemView)
                        .load(rescueRequestMaster.getRescueRequestPlacePhotoPath())
                        .fitCenter()
                        .centerCrop()
                        .into(userRescueImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
        }
    }
}
