package com.apps.rescueconnect.ui.roledetails.user.viewRescueRequests;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.FireBaseDatabaseConstants;
import com.apps.rescueconnect.helper.NetworkUtil;
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.helper.dataUtils.DataUtils;
import com.apps.rescueconnect.helper.rescueConnectToast.RescueConnectToast;
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.model.rescueRequest.RescueRequestMaster;
import com.apps.rescueconnect.model.rescueRequest.RescueRequestSubDetails;
import com.apps.rescueconnect.ui.roledetails.MainActivityInteractor;
import com.apps.rescueconnect.ui.roledetails.ViewRescueRequestActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.rxbinding.view.RxView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

public class ViewUserRescueRequests extends Fragment implements UserRescueRequestMainAdapter.UserRescueRequestItemClickListener {

    private static final String TAG = ViewUserRescueRequests.class.getSimpleName();
    private View rootView;
    private MainActivityInteractor mainActivityInteractor;
    private ProgressDialog progressDialog;
    private TextView textNoRescuesAvailable;

    private TabLayout rescueRequestTabLayout;
    BadgeDrawable badgePending, badgeCompleted;

    // Firebase Storage
    FirebaseDatabase firebaseDatabase;
    private DatabaseReference mUserReferenceRescue;

    private RecyclerView recyclerViewUserRescues;
    private UserRescueRequestMainAdapter userRescueRequestMainAdapter;

    private List<RescueRequestMaster> rescueRescueMasterList = new ArrayList<>();
    private List<RescueRequestMaster> rescueRescueMasterPendingList = new ArrayList<>();
    private List<RescueRequestMaster> rescueRequestMasterCompletedList = new ArrayList<>();

    private User loginUser;

    public ViewUserRescueRequests() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_view_user_rescues, container, false);
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInteractor = (MainActivityInteractor) requireActivity();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            mainActivityInteractor.setScreenTitle(getString(R.string.my_rescues_ttl));

            progressDialog = new ProgressDialog(requireContext());

            firebaseDatabase = FirebaseDatabase.getInstance();
            mUserReferenceRescue = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.RESQUE_REQUEST_LIST_TABLE);

            rescueRequestTabLayout = rootView.findViewById(R.id.user_rescues_tab);

            try {
                //set the icons
                rescueRequestTabLayout.addTab(rescueRequestTabLayout.newTab().setText(AppConstants.PENDING_BADGE), 0);
                rescueRequestTabLayout.addTab(rescueRequestTabLayout.newTab().setText(AppConstants.COMPLETED_BADGE), 1);

                //set the badge
                badgePending = rescueRequestTabLayout.getTabAt(0).getOrCreateBadge();
                badgeCompleted = rescueRequestTabLayout.getTabAt(1).getOrCreateBadge();

                badgePending.setBadgeTextColor(getResources().getColor(R.color.colorWhite, null));
                badgeCompleted.setBadgeTextColor(getResources().getColor(R.color.colorWhite, null));

                badgePending.setVisible(true);
                badgeCompleted.setVisible(true);

                badgePending.setNumber(0);
                badgeCompleted.setNumber(0);

            } catch (Exception e) {
                e.printStackTrace();
            }

            loginUser = Utils.getLoginUserDetails(requireContext());
            if (loginUser != null) {
                setUpViews();
                getUserRescueList(loginUser, true, true);
            } else {
                setUpViews();
                RescueConnectToast.showErrorToast(requireContext(), "Failed to fetch details", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpViews() {
        try {
            textNoRescuesAvailable = rootView.findViewById(R.id.no_rescues_available);
            recyclerViewUserRescues = rootView.findViewById(R.id.recycler_user_rescues);

            if (rescueRescueMasterPendingList.size() > 0) {
                textNoRescuesAvailable.setVisibility(View.GONE);
                recyclerViewUserRescues.setVisibility(View.VISIBLE);
            } else {
                recyclerViewUserRescues.setVisibility(View.GONE);
                textNoRescuesAvailable.setVisibility(View.VISIBLE);
                textNoRescuesAvailable.setText("No Requests Available");
            }

            rescueRequestTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 0:
                            showOrHideTabsBasedOnSelectedTab(AppConstants.PENDING_BADGE);
                            break;
                        case 1:
                            showOrHideTabsBasedOnSelectedTab(AppConstants.COMPLETED_BADGE);
                            break;
                        default:
                            showOrHideTabsBasedOnSelectedTab(AppConstants.PENDING_BADGE);
                            break;
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });
            showOrHideTabsBasedOnSelectedTab(AppConstants.PENDING_BADGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showOrHideTabsBasedOnSelectedTab(String selectedTab) {
        try {
            switch (selectedTab) {
                case AppConstants.PENDING_BADGE:
                    getUserRescueList(loginUser, true, true);
                    break;
                case AppConstants.COMPLETED_BADGE:
                    getUserRescueList(loginUser, true, false);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUserRescues() {
        badgePending.setBackgroundColor(getResources().getColor(R.color.colorNewUxOrange, null));
        badgeCompleted.setBackgroundColor(getResources().getColor(R.color.colorDarkGrayBorder, null));

        if (rescueRescueMasterPendingList.size() > 0) {
            textNoRescuesAvailable.setVisibility(View.GONE);
            recyclerViewUserRescues.setVisibility(View.VISIBLE);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
            recyclerViewUserRescues.setLayoutManager(linearLayoutManager);
            userRescueRequestMainAdapter = new UserRescueRequestMainAdapter(requireContext(), rescueRescueMasterPendingList, this);
            recyclerViewUserRescues.setAdapter(userRescueRequestMainAdapter);

            if (userRescueRequestMainAdapter != null) {
                userRescueRequestMainAdapter.notifyDataSetChanged();
            }
        } else {
            recyclerViewUserRescues.setVisibility(View.GONE);
            textNoRescuesAvailable.setVisibility(View.VISIBLE);
            textNoRescuesAvailable.setText("No Requests Available");
        }

        badgePending.setNumber(Math.max(rescueRescueMasterPendingList.size(), 0));
        badgeCompleted.setNumber(Math.max(rescueRescueMasterList.size(), 0));
    }

    private void loadUserRescues(boolean loadOnlyBadge, boolean isPending) {
        Log.d(TAG, "loadUserRescues: 1");
        if (loadOnlyBadge) {
            Log.d(TAG, "loadUserRescues: 2");
            badgePending.setBackgroundColor(getResources().getColor(R.color.colorNewUxOrange, null));
            badgeCompleted.setBackgroundColor(getResources().getColor(R.color.colorDarkGrayBorder, null));
        } else {
            Log.d(TAG, "loadUserRescues: 3");
            badgeCompleted.setBackgroundColor(getResources().getColor(R.color.colorNewUxOrange, null));
            badgePending.setBackgroundColor(getResources().getColor(R.color.colorDarkGrayBorder, null));
            Log.d(TAG, "loadUserRescues: " + rescueRescueMasterList);

            List<RescueRequestMaster> rescueRequestMasterList = getPendingOrCompletedRequestsList(isPending);

            if (rescueRequestMasterList.size() > 0) {
                Log.d(TAG, "loadUserRescues: 4");
                textNoRescuesAvailable.setVisibility(View.GONE);
                recyclerViewUserRescues.setVisibility(View.VISIBLE);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
                recyclerViewUserRescues.setLayoutManager(linearLayoutManager);
                userRescueRequestMainAdapter = new UserRescueRequestMainAdapter(requireContext(), rescueRequestMasterList, this);
                recyclerViewUserRescues.setAdapter(userRescueRequestMainAdapter);
            } else {
                Log.d(TAG, "loadUserRescues: 5");
                recyclerViewUserRescues.setVisibility(View.GONE);
                textNoRescuesAvailable.setVisibility(View.VISIBLE);
                textNoRescuesAvailable.setText("No Requests Available");
            }

            if (userRescueRequestMainAdapter != null) {
                userRescueRequestMainAdapter.notifyDataSetChanged();
            }
        }

        badgePending.setNumber(Math.max(rescueRescueMasterPendingList.size(), 0));
        badgeCompleted.setNumber(Math.max(rescueRequestMasterCompletedList.size(), 0));
    }

    public void getUserRescueList(User user, boolean isShowProgress, boolean isPending) {
        try {
            if (isShowProgress) {
                showProgressDialog("Fetching details, please wait");
            }

            mUserReferenceRescue.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    try {
                        if (snapshot.exists()) {
                            Log.d(TAG, "onDataChange: snapshot: " + snapshot);

                            DataSnapshot dataSnapshot = snapshot.child(user.getUserCity()).child(user.getMobileNumber());
                            Log.d(TAG, "onDataChange: dataSnapshot: " + dataSnapshot);
                            rescueRescueMasterList.clear();
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                RescueRequestMaster rescueRescueMaster = postSnapshot.getValue(RescueRequestMaster.class);
                                if (rescueRescueMaster != null) {
                                    rescueRescueMasterList.add(rescueRescueMaster);
                                }
                            }
                        }
                        Log.d(TAG, "onDataChange: rescueRescueMasterList:" + rescueRescueMasterList);

                        hideProgressDialog();

                        if (isShowProgress) {
                            loadUserRescues(false, isPending);
                        } else {
                            loadUserRescues(true, isPending);
                        }
                    } catch (Exception e) {
                        hideProgressDialog();
                        if (isShowProgress) {
                            loadUserRescues(false, isPending);
                        } else {
                            loadUserRescues(true, isPending);
                        }
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: error: " + error.getMessage());
                    if (isShowProgress) {
                        hideProgressDialog();
                        loadUserRescues(false, isPending);
                    } else {
                        loadUserRescues(true, isPending);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<RescueRequestMaster> getPendingOrCompletedRequestsList(boolean isPending) {
        try {
            rescueRescueMasterPendingList.clear();
            rescueRequestMasterCompletedList.clear();
            if (rescueRescueMasterList != null && rescueRescueMasterList.size() > 0) {
                for (RescueRequestMaster rescueRequestMaster : rescueRescueMasterList) {
                    if (rescueRequestMaster != null) {
                        List<RescueRequestSubDetails> rescueRequestSubDetailsList = rescueRequestMaster.getRescueRequestSubDetailsList();
                        if (rescueRequestSubDetailsList != null && rescueRequestSubDetailsList.size() > 0) {
                            int lastItem = rescueRequestSubDetailsList.size() - 1;
                            RescueRequestSubDetails rescueRequestSubDetails = rescueRequestSubDetailsList.get(lastItem);
                            if (rescueRequestSubDetails != null) {
                                String status = rescueRequestSubDetails.getRescueRequestStatus();
                                if (status.equalsIgnoreCase(AppConstants.COMPLETED_STATUS) || status.equalsIgnoreCase(AppConstants.CANCELLED_STATUS) || status.equalsIgnoreCase(AppConstants.REJECTED_STATUS)) {
                                    rescueRequestMasterCompletedList.add(rescueRequestMaster);
                                } else {
                                    rescueRescueMasterPendingList.add(rescueRequestMaster);
                                }
                            }
                        }
                    }
                }

                if (isPending) {
                    return rescueRescueMasterPendingList;
                } else {
                    return rescueRequestMasterCompletedList;
                }
            } else {
                return rescueRescueMasterList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rescueRescueMasterList;
    }

    @Override
    public void userRescueRequestUpdateClicked(int position, RescueRequestMaster rescueRescueMaster, String userRescueStatus) {
        try {
            if (userRescueStatus.equalsIgnoreCase(AppConstants.COMPLETED_STATUS)) {
                RescueConnectToast.showAlertToast(requireContext(), "Rescue already Completed.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            } else if (userRescueStatus.equalsIgnoreCase(AppConstants.ACCEPTED_STATUS)) {
                RescueConnectToast.showAlertToast(requireContext(), "Rescue already Accepted by Rescue Agency, can't Update.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            } else if (userRescueStatus.equalsIgnoreCase(AppConstants.REJECTED_STATUS)) {
                RescueConnectToast.showAlertToast(requireContext(), "Rescue rejected by Rescue Agency, can't Update.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            } else {
                showDialogForRescueStatusUpdate(requireContext(), position, userRescueStatus, rescueRescueMaster);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void userRescueRequestViewClicked(int position, RescueRequestMaster rescueRescueMaster, ImageView imageView, TextView textView) {
        try {
            if (NetworkUtil.getConnectivityStatus(requireContext())) {
                Intent intentView = new Intent(requireContext(), ViewRescueRequestActivity.class);
                intentView.putExtra(AppConstants.VIEW_RESCUE_REQUEST_DATA, rescueRescueMaster);

                Pair<View, String> transactionPairOne = Pair.create((View) imageView, requireContext().getResources().getString(R.string.transaction_rescue_details_photo));
                Pair<View, String> transactionPairTwo = Pair.create((View) textView, requireContext().getResources().getString(R.string.transaction_rescue_details_header));

           /*
           // Call single Shared Transaction
           ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(requireActivity(), (View) imagePlace, requireContext().getResources().getString(R.string.transaction_name));
            */

                // Call Multiple Shared Transaction using Pair Option
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(requireActivity(), transactionPairOne, transactionPairTwo);
                startActivityForResult(intentView, 3, options.toBundle());

            } else {
                RescueConnectToast.showErrorToast(requireContext(), getString(R.string.no_internet), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProgressDialog(String message) {
        try {
            if (progressDialog != null) {
                progressDialog.setMessage(message);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideProgressDialog() {
        try {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* @Override
     public void userRescueRequestUpdateClicked(int position, RescueRequestMaster rescueRequestMaster, String userIssueStatus) {
         try {
             if (userIssueStatus.equalsIgnoreCase(AppConstants.COMPLETED_STATUS)) {
                 RescueConnectToast.showAlertToast(requireContext(), "Issue already Completed.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
             } else if (userIssueStatus.equalsIgnoreCase(AppConstants.ACCEPTED_STATUS)) {
                 RescueConnectToast.showAlertToast(requireContext(), "Issue already Accepted by Municipal Officer, can't Update.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
             } else if (userIssueStatus.equalsIgnoreCase(AppConstants.REJECTED_STATUS)) {
                 RescueConnectToast.showAlertToast(requireContext(), "Issue rejected by Municipal Officer, can't Update.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
             } else {
                 showDialogForIssueStatusUpdate(requireContext(), position, userIssueStatus, rescueRequestMaster);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }

     @Override
     public void userRescueRequestViewClicked(int position, RescueRequestMaster rescueRequestMaster, ImageView imageView, TextView textView) {
         try {
             if (NetworkUtil.getConnectivityStatus(requireContext())) {
                 Intent intentView = new Intent(requireContext(), ViewRescueRequestActivity.class);
                 intentView.putExtra(AppConstants.VIEW_RESCUE_REQUEST_DATA, rescueRequestMaster);

                 Pair<View, String> transactionPairOne = Pair.create((View) imageView, requireContext().getResources().getString(R.string.transaction_rescue_details_photo));
                 Pair<View, String> transactionPairTwo = Pair.create((View) textView, requireContext().getResources().getString(R.string.transaction_rescue_details_header));

            *//*
           // Call single Shared Transaction
           ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(requireActivity(), (View) imagePlace, requireContext().getResources().getString(R.string.transaction_name));
            *//*

                // Call Multiple Shared Transaction using Pair Option
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(requireActivity(), transactionPairOne, transactionPairTwo);
                startActivityForResult(intentView, 3, options.toBundle());

            } else {
                RescueConnectToast.showErrorToast(requireContext(), getString(R.string.no_internet), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
    public void showDialogForIssueStatusUpdate(Context context, int position, String currentStatus, RescueRequestMaster rescueRequestMaster) {
        try {
            String lastStatus = "";
            String rescueAccessStatus = "";

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_rescue_status_update, null);
            builder.setView(dialogView);
            builder.setCancelable(false);

            // TextView and EditText Initialization
            TextView textMainHeader = dialogView.findViewById(R.id.text_rescue_status_update_main_header);
            TextView textRescueHeader = dialogView.findViewById(R.id.text_rescue_header);
            TextView textRescueAccessTypeHeader = dialogView.findViewById(R.id.text_rescue_access_type_header);
            TextView textRescueAccessTypeValue = dialogView.findViewById(R.id.text_rescue_access_type_value);
            TextView textRescueStatusHeader = dialogView.findViewById(R.id.text_rescue_status_header);
            TextView textRescueStatusValue = dialogView.findViewById(R.id.text_rescue_status_value);

            //Button Initialization
            Button btnUpdate = dialogView.findViewById(R.id.btn_update);
            Button btnClose = dialogView.findViewById(R.id.btn_close);

            List<String> rescueAccessTypeList = DataUtils.getCalamityAccessTypeList();

            RxView.touches(textRescueAccessTypeValue).subscribe(motionEvent -> {
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(requireContext());
                        builderSingle.setTitle(R.string.select_access_type);

                        final ArrayAdapter<String> taskStatusSelectionAdapter = new ArrayAdapter<String>(requireContext(),
                                android.R.layout.select_dialog_singlechoice, rescueAccessTypeList) {
                            @NonNull
                            @Override
                            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView text = view.findViewById(android.R.id.text1);
                                text.setTextColor(Color.BLACK);
                                return view;
                            }
                        };

                        builderSingle.setNegativeButton("Cancel", (dialog, subPosition) -> dialog.dismiss());

                        builderSingle.setAdapter(taskStatusSelectionAdapter, (dialog, subPosition) -> {
                            textRescueAccessTypeValue.setText(taskStatusSelectionAdapter.getItem(subPosition));
                        });
                        builderSingle.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            List<String> rescueRequestNextStatusList = DataUtils.getNextStatusBasedOnRole(currentStatus, loginUser.getMainRole());

            RxView.touches(textRescueStatusValue).subscribe(motionEvent -> {
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(requireContext());
                        builderSingle.setTitle(AppConstants.CALAMITY_STATUS);

                        final ArrayAdapter<String> taskStatusSelectionAdapter = new ArrayAdapter<String>(requireContext(),
                                android.R.layout.select_dialog_singlechoice, rescueRequestNextStatusList) {
                            @NonNull
                            @Override
                            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView text = view.findViewById(android.R.id.text1);
                                text.setTextColor(Color.BLACK);
                                return view;
                            }
                        };

                        builderSingle.setNegativeButton("Cancel", (dialog, subPosition) -> dialog.dismiss());

                        builderSingle.setAdapter(taskStatusSelectionAdapter, (dialog, subPosition) -> {
                            textRescueStatusValue.setText(taskStatusSelectionAdapter.getItem(subPosition));
                        });
                        builderSingle.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            textRescueAccessTypeHeader.setText(R.string.rescue_access_type_title);
            textRescueAccessTypeValue.setText(rescueRequestMaster.getRescueRequestAccessType());
            rescueAccessStatus = rescueRequestMaster.getRescueRequestAccessType();

            String headerMessage = rescueRequestMaster.getRescueRequestType() + " : " + rescueRequestMaster.getRescueRequestPlacePhotoId();
            textMainHeader.setText(headerMessage);
            textMainHeader.setTextColor(context.getResources().getColor(R.color.error_color, null));

            textRescueHeader.setText(rescueRequestMaster.getRescueRequestHeader());

            textRescueStatusHeader.setText(AppConstants.CALAMITY_STATUS);

            int lastPosition = (rescueRequestMaster.getRescueRequestSubDetailsList().size() - 1);
            Log.d(TAG, "lastPosition: " + lastPosition);

            if (lastPosition >= 0) {
                lastStatus = rescueRequestMaster.getRescueRequestSubDetailsList().get(lastPosition).getRescueRequestStatus();
                Log.d(TAG, "lastStatus: " + lastStatus);
                textRescueStatusValue.setText(lastStatus);
            }

            AlertDialog alert = builder.create();
            alert.show();

            String finalLastStatus = lastStatus;
            String finalAccessType = rescueAccessStatus;
            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (!(finalLastStatus.equalsIgnoreCase(textRescueStatusValue.getText().toString().trim())) || !(finalAccessType.equalsIgnoreCase(textRescueAccessTypeValue.getText().toString().trim()))) {
                            User loginUser = Utils.getLoginUserDetails(requireContext());

                            rescueRequestMaster.setRescueRequestAccessType(textRescueAccessTypeValue.getText().toString().trim());

                            RescueRequestSubDetails rescueRequestSubDetails = new RescueRequestSubDetails();
                            rescueRequestSubDetails.setRescueRequestId(rescueRequestMaster.getRescueRequestPlacePhotoId());
                            rescueRequestSubDetails.setRescueRequestAcceptedId(loginUser.getMobileNumber());
                            rescueRequestSubDetails.setRescueRequestStatus(textRescueStatusValue.getText().toString().trim());
                            rescueRequestSubDetails.setRescueRequestModifiedBy(loginUser.getMobileNumber());
                            rescueRequestSubDetails.setRescueRequestModifiedOn(Utils.getCurrentTimeStampWithSeconds());
                            rescueRequestSubDetails.setRescueRequestAcceptedRole(loginUser.getMainRole());
                            rescueRequestMaster.getRescueRequestSubDetailsList().add(rescueRequestSubDetails);

                            Log.d(TAG, "onClick: complaintMain: " + rescueRequestMaster);

                            updateRequestRequestDetails(position, rescueRequestMaster, alert);
                        } else {
                            RescueConnectToast.showInfoToast(requireContext(), "Nothing to Update.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        alert.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
/*
    public void updateRequestRequestDetails(int position, RescueRequestMaster rescueRequestMaster, AlertDialog alert) {
        try {
            showProgressDialog("Updating status please wait.");
            mUserReferenceIssue.child(rescueRequestMaster.getRescueRequestCity()).child(rescueRequestMaster.getRescueRequestType()).child(rescueRequestMaster.getRescueRequestAcceptedOfficerId()).child(rescueRequestMaster.getRescueRequestPlacePhotoId()).setValue(rescueRequestMaster)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressDialog();
                            RescueConnectToast.showSuccessToast(requireContext(), "Issue status updated successfully.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
                            if (alert != null) {
                                alert.dismiss();
                            }
                            if (userRescueRequestMainAdapter != null) {
                                userRescueRequestMainAdapter.notifyItemChanged(position);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            RescueConnectToast.showErrorToast(requireContext(), "Failed to update rescue.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                        }
                    });
        } catch (Exception e) {
            hideProgressDialog();
            RescueConnectToast.showErrorToast(requireContext(), e.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            e.printStackTrace();
        }
    }*/

    public void showDialogForRescueStatusUpdate(Context context, int position, String currentStatus, RescueRequestMaster rescueRescueMaster) {
        try {
            String lastStatus = "";

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_rescue_status_update, null);
            builder.setView(dialogView);
            builder.setCancelable(false);

            // TextView and EditText Initialization
            TextView textMainHeader = dialogView.findViewById(R.id.text_rescue_status_update_main_header);
            TextView textRescueHeader = dialogView.findViewById(R.id.text_rescue_header);
            TextView textRescueAccessTypeHeader = dialogView.findViewById(R.id.text_rescue_access_type_header);
            TextView textRescueAccessTypeValue = dialogView.findViewById(R.id.text_rescue_access_type_value);
            TextView textRescueStatusHeader = dialogView.findViewById(R.id.text_rescue_status_header);
            TextView textRescueStatusValue = dialogView.findViewById(R.id.text_rescue_status_value);

            //Button Initialization
            Button btnUpdate = dialogView.findViewById(R.id.btn_update);
            Button btnClose = dialogView.findViewById(R.id.btn_close);

            RxView.clicks(textRescueAccessTypeValue).subscribe(new Action1<Void>() {
                @Override
                public void call(Void unused) {
                    RescueConnectToast.showAlertToast(requireContext(), getString(R.string.rescue_access_type_not_allowed_to_update), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
                }
            });

            List<String> rescueRequestNextStatusList = DataUtils.getNextStatusBasedOnRole(currentStatus, loginUser.getMainRole());

            RxView.touches(textRescueStatusValue).subscribe(motionEvent -> {
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(requireContext());
                        builderSingle.setTitle(AppConstants.CALAMITY_STATUS);

                        final ArrayAdapter<String> taskStatusSelectionAdapter = new ArrayAdapter<String>(requireContext(),
                                android.R.layout.select_dialog_singlechoice, rescueRequestNextStatusList) {
                            @NonNull
                            @Override
                            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView text = view.findViewById(android.R.id.text1);
                                text.setTextColor(Color.BLACK);
                                return view;
                            }
                        };

                        builderSingle.setNegativeButton("Cancel", (dialog, subPosition) -> dialog.dismiss());

                        builderSingle.setAdapter(taskStatusSelectionAdapter, (dialog, subPosition) -> {
                            textRescueStatusValue.setText(taskStatusSelectionAdapter.getItem(subPosition));
                        });
                        builderSingle.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            textRescueAccessTypeHeader.setText(R.string.rescue_access_type_header);
            textRescueAccessTypeValue.setText(rescueRescueMaster.getRescueRequestAccessType());

            String headerMessage = rescueRescueMaster.getRescueRequestType() + " : " + rescueRescueMaster.getRescueRequestPlacePhotoId();
            textMainHeader.setText(headerMessage);
            textMainHeader.setTextColor(context.getResources().getColor(R.color.error_color, null));

            textRescueHeader.setText(rescueRescueMaster.getRescueRequestHeader());

            textRescueStatusHeader.setText(AppConstants.CALAMITY_STATUS);

            int lastPosition = (rescueRescueMaster.getRescueRequestSubDetailsList().size() - 1);
            Log.d(TAG, "lastPosition: " + lastPosition);

            if (lastPosition >= 0) {
                lastStatus = rescueRescueMaster.getRescueRequestSubDetailsList().get(lastPosition).getRescueRequestStatus();
                Log.d(TAG, "lastStatus: " + lastStatus);
                textRescueStatusValue.setText(lastStatus);
            }

            AlertDialog alert = builder.create();
            alert.show();

            String finalLastStatus = lastStatus;
            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (!(finalLastStatus.equalsIgnoreCase(textRescueStatusValue.getText().toString().trim()))) {
                            User loginUser = Utils.getLoginUserDetails(requireContext());

                            RescueRequestSubDetails rescueRequestSubDetails = new RescueRequestSubDetails();
                            rescueRequestSubDetails.setRescueRequestId(rescueRescueMaster.getRescueRequestPlacePhotoId());
                            rescueRequestSubDetails.setRescueRequestAcceptedId(loginUser.getMobileNumber());
                            rescueRequestSubDetails.setRescueRequestStatus(textRescueStatusValue.getText().toString().trim());
                            rescueRequestSubDetails.setRescueRequestModifiedBy(loginUser.getMobileNumber());
                            rescueRequestSubDetails.setRescueRequestModifiedOn(Utils.getCurrentTimeStampWithSeconds());
                            rescueRequestSubDetails.setRescueRequestAcceptedRole(loginUser.getMainRole());
                            rescueRescueMaster.getRescueRequestSubDetailsList().add(rescueRequestSubDetails);

                            Log.d(TAG, "onClick: complaintMain: " + rescueRescueMaster);

                            updateRequestRequestDetails(position, rescueRescueMaster, alert);
                        } else {
                            RescueConnectToast.showInfoToast(requireContext(), "Nothing to Update.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        alert.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateRequestRequestDetails(int position, RescueRequestMaster rescueRescueMaster, AlertDialog alert) {
        try {
            showProgressDialog("Updating status please wait.");
            mUserReferenceRescue.child(rescueRescueMaster.getRescueRequestCity()).child(rescueRescueMaster.getRescueRequestAcceptedOfficerId()).child(rescueRescueMaster.getRescueRequestPlacePhotoId()).setValue(rescueRescueMaster)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressDialog();
                            RescueConnectToast.showSuccessToast(requireContext(), "Rescue Request status updated successfully.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
                            if (alert != null) {
                                alert.dismiss();
                            }
                            if (userRescueRequestMainAdapter != null) {
                                userRescueRequestMainAdapter.notifyItemChanged(position);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            RescueConnectToast.showErrorToast(requireContext(), "Failed to update complaint", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                        }
                    });
        } catch (Exception e) {
            hideProgressDialog();
            RescueConnectToast.showErrorToast(requireContext(), e.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            e.printStackTrace();
        }
    }
}