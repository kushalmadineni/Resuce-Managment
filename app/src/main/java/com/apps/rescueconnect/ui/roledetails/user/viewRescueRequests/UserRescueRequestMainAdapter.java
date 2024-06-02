package com.apps.rescueconnect.ui.roledetails.user.viewRescueRequests;

import android.content.Context;
import android.util.Log;
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

public class UserRescueRequestMainAdapter extends RecyclerView.Adapter<UserRescueRequestMainAdapter.UserRescueRequestAdapterViewHolder> {

    private static final String TAG = UserRescueRequestMainAdapter.class.getSimpleName();
    /**
     * ArrayList of type PlaceItem
     */
    private List<RescueRequestMaster> rescueRequestMasterList;
    private Context context;

    private UserRescueRequestItemClickListener listener;
    // endregion

    public UserRescueRequestMainAdapter(Context context, List<RescueRequestMaster> rescueRequestMasterList, UserRescueRequestItemClickListener listener) {
        this.context = context;
        this.rescueRequestMasterList = rescueRequestMasterList;
        this.listener = listener;
    }

    @Override
    public UserRescueRequestAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_rescue_request_main, parent, false);
        return new UserRescueRequestAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final UserRescueRequestAdapterViewHolder holder, int position) {
        try {
            holder.textAssign.setVisibility(View.GONE);
            int mainPosition = holder.getAdapterPosition();
            if (rescueRequestMasterList.size() > 0) {
                holder.setItem(rescueRequestMasterList.get(mainPosition));
                RescueRequestMaster rescueRequestMaster = rescueRequestMasterList.get(mainPosition);
                if (rescueRequestMaster != null) {
                    holder.textUserRescueRequestHeader.setText(rescueRequestMaster.getRescueRequestHeader());
                    if (rescueRequestMaster.getRescueRequestAccessType().equalsIgnoreCase(AppConstants.CALAMITY_ACCESS_TYPE_PUBLIC)) {
                        holder.textUserRescueRequestHeader.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_public_lock, 0, 0, 0);
                        holder.textUserRescueRequestHeader.setCompoundDrawablePadding((int) context.getResources().getDimension(R.dimen.std_10_dp));
                        holder.textUserRescueRequestHeader.getCompoundDrawables()[0].setTint(context.getResources().getColor(R.color.colorSuccess, null));
                    } else {
                        holder.textUserRescueRequestHeader.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_private_lock, 0, 0, 0);
                        holder.textUserRescueRequestHeader.setCompoundDrawablePadding((int) context.getResources().getDimension(R.dimen.std_10_dp));
                        holder.textUserRescueRequestHeader.getCompoundDrawables()[0].setTint(context.getResources().getColor(R.color.colorLightOrange, null));
                    }
                    holder.textUserRescueRequestNumber.setText(rescueRequestMaster.getRescueRequestType());
                    holder.textUserRescueRequestId.setText(rescueRequestMaster.getRescueRequestPlacePhotoId());
                    holder.textUserRescueRequestCity.setText(rescueRequestMaster.getRescueRequestCity());

                    int detailsLatest = rescueRequestMaster.getRescueRequestSubDetailsList().size() - 1;
                    RescueRequestSubDetails rescueRequestSubDetails = null;
                    if (detailsLatest >= 0) {
                        rescueRequestSubDetails = rescueRequestMaster.getRescueRequestSubDetailsList().get(detailsLatest);

                        if (rescueRequestSubDetails != null) {
                            holder.textUserRescueRequestStatus.setText(rescueRequestSubDetails.getRescueRequestStatus());
                            switch (rescueRequestSubDetails.getRescueRequestStatus()) {
                                case AppConstants
                                        .NEW_STATUS: {
                                    holder.textUserRescueRequestStatus.setTextColor(context.getResources().getColor(R.color.colorNewUxBlue, null));
                                    break;
                                }
                                case AppConstants
                                        .ACCEPTED_STATUS: {
                                    holder.textUserRescueRequestStatus.setTextColor(context.getResources().getColor(R.color.colorVeniceBlue, null));
                                    break;
                                }
                                case AppConstants
                                        .COMPLETED_STATUS: {
                                    holder.textUserRescueRequestStatus.setTextColor(context.getResources().getColor(R.color.colorSuccess, null));
                                    break;
                                }
                                case AppConstants
                                        .CANCELLED_STATUS: {
                                    holder.textUserRescueRequestStatus.setTextColor(context.getResources().getColor(R.color.colorError, null));
                                    break;
                                }
                                case AppConstants
                                        .REJECTED_STATUS: {
                                    holder.textUserRescueRequestStatus.setTextColor(context.getResources().getColor(R.color.colorError, null));
                                    break;
                                }
                                default: {
                                    holder.textUserRescueRequestStatus.setTextColor(context.getResources().getColor(R.color.colorNewUxBlue, null));
                                    break;
                                }
                            }
                        }
                    }

                    holder.textUpdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.userRescueRequestUpdateClicked(mainPosition, rescueRequestMaster, holder.textUserRescueRequestStatus.getText().toString());
                        }
                    });

                    holder.textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.userRescueRequestViewClicked(mainPosition, rescueRequestMaster, holder.userRescueRequestImage, holder.textUserRescueRequestHeader);
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
        return rescueRequestMasterList.size();
    }

    public interface UserRescueRequestItemClickListener {
        void userRescueRequestUpdateClicked(int position, RescueRequestMaster rescueRequestMaster, String userRescueRequestStatus);

        void userRescueRequestViewClicked(int position, RescueRequestMaster rescueRequestMaster, ImageView imageView, TextView textView);
    }

    static class UserRescueRequestAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        RescueRequestMaster userRescueRequest;
        ImageView userRescueRequestImage;
        TextView textUserRescueRequestNumber, textUserRescueRequestStatus, textUserRescueRequestId, textUserRescueRequestCity,
                textUserRescueRequestHeader, textView, textUpdate, textAssign;

        UserRescueRequestAdapterViewHolder(View itemView) {
            super(itemView);

            // Image View
            userRescueRequestImage = itemView.findViewById(R.id.user_rescue_request_image);
            // Text View
            textUserRescueRequestNumber = itemView.findViewById(R.id.text_user_rescue_request_number);
            textUserRescueRequestStatus = itemView.findViewById(R.id.text_user_rescue_request_status);
            textUserRescueRequestId = itemView.findViewById(R.id.text_user_rescue_request_id);
            textUserRescueRequestCity = itemView.findViewById(R.id.text_user_rescue_request_city);
            textUserRescueRequestHeader = itemView.findViewById(R.id.text_user_rescue_request_header);
            textView = itemView.findViewById(R.id.text_view);
            textUpdate = itemView.findViewById(R.id.text_update_status);
            textAssign = itemView.findViewById(R.id.text_assign);
        }

        public void setItem(RescueRequestMaster rescueRequestMaster) {
            userRescueRequest = rescueRequestMaster;
            Log.d(TAG, "setItem: " + rescueRequestMaster.getRescueRequestPlacePhotoPath());
            Log.d(TAG, "userRescueRequestImage: " + userRescueRequestImage);
            try {
                Glide.with(itemView)
                        .load(rescueRequestMaster.getRescueRequestPlacePhotoPath())
                        .fitCenter()
                        .centerCrop()
                        .into(userRescueRequestImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
        }
    }
}
