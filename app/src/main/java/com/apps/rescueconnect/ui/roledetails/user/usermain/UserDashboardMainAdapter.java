package com.apps.rescueconnect.ui.roledetails.user.usermain;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.model.rescueRequest.RescueRequestMaster;
import com.bumptech.glide.Glide;

import java.util.List;

public class UserDashboardMainAdapter extends RecyclerView.Adapter<UserDashboardMainAdapter.RescueRequestsMainAdapterViewHolder> {

    private static final String TAG = UserDashboardMainAdapter.class.getSimpleName();
    /**
     * ArrayList of type ResqueMaster
     */
    private List<RescueRequestMaster> rescueRequestMasterMainItemList;
    private Context context;

    private RescueRequestMasterClickListener listener;
    // endregion

    public UserDashboardMainAdapter(Context context, List<RescueRequestMaster> rescueRequestMasterMainItemList, RescueRequestMasterClickListener listener) {
        this.context = context;
        this.rescueRequestMasterMainItemList = rescueRequestMasterMainItemList;
        this.listener = listener;
    }

    @Override
    public RescueRequestsMainAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rescue_main, parent, false);
        return new RescueRequestsMainAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RescueRequestsMainAdapterViewHolder holder, int position) {
        try {
            int olderPosition = holder.getAbsoluteAdapterPosition();
            if (rescueRequestMasterMainItemList.size() > 0) {
                holder.setItem(rescueRequestMasterMainItemList.get(olderPosition));
                RescueRequestMaster rescueRequestMaster = rescueRequestMasterMainItemList.get(olderPosition);
                if (rescueRequestMaster != null) {
                    String textIssueMainHeader = AppConstants.COMPLAINT_TYPE + " : " + rescueRequestMaster.getRescueRequestPlacePhotoId();
                    holder.textIssueHeader.setText(textIssueMainHeader);
                    holder.textTitle.setText(rescueRequestMaster.getRescueRequestHeader());
                    holder.textUserName.setText(rescueRequestMaster.getRescueRequestAcceptedOfficerName());
                    holder.textDescription.setText(rescueRequestMaster.getRescueRequestDescription());
                    holder.textAddress.setText(rescueRequestMaster.getRescueRequestPlaceAddress());

                    holder.imageIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.rescueRequestMasterClicked(olderPosition, holder.imageIcon, holder.textTitle, rescueRequestMaster);
                        }
                    });

                    holder.textTitle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.rescueRequestMasterClicked(olderPosition, holder.imageIcon, holder.textTitle, rescueRequestMaster);
                        }
                    });

                    holder.rescueRequestMasterCardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.rescueRequestMasterClicked(olderPosition, holder.imageIcon, holder.textTitle, rescueRequestMaster);
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
        return rescueRequestMasterMainItemList.size();
    }

    public interface RescueRequestMasterClickListener {
        void rescueRequestMasterClicked(int position, ImageView imageIssue, TextView textIssueHeader, RescueRequestMaster rescueRequestMaster);
    }

    static class RescueRequestsMainAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        RescueRequestMaster rescueRequestMaster;
        ImageView imageIcon;
        TextView textIssueHeader, textTitle, textUserName, textDescription, textAddress;
        CardView rescueRequestMasterCardView;

        RescueRequestsMainAdapterViewHolder(View itemView) {
            super(itemView);

            // Image View
            imageIcon = itemView.findViewById(R.id.item_icon);
            // Text View
            textIssueHeader = itemView.findViewById(R.id.item_header_main);
            textTitle = itemView.findViewById(R.id.item_title);
            textUserName = itemView.findViewById(R.id.item_user_name);
            textDescription = itemView.findViewById(R.id.item_description);
            textAddress = itemView.findViewById(R.id.item_address);
            // Card View
            rescueRequestMasterCardView = itemView.findViewById(R.id.places_item_cardview);
        }

        public void setItem(RescueRequestMaster item) {
            rescueRequestMaster = item;

            Glide.with(itemView)
                    .load(rescueRequestMaster.getRescueRequestPlacePhotoPath())
                    .fitCenter()
                    .centerInside()
                    .into(imageIcon);
        }

        @Override
        public void onClick(View v) {
        }
    }
}
