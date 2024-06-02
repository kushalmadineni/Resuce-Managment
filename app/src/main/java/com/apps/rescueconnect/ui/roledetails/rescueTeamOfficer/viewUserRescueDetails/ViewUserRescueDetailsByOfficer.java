package com.apps.rescueconnect.ui.roledetails.rescueTeamOfficer.viewUserRescueDetails;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.FireBaseDatabaseConstants;
import com.apps.rescueconnect.helper.NetworkUtil;
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.helper.dataUtils.DataUtils;
import com.apps.rescueconnect.helper.encryption.AESHelper;
import com.apps.rescueconnect.helper.rescueConnectToast.RescueConnectToast;
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.model.citydetails.City;
import com.apps.rescueconnect.model.rescueRequest.RescueRequestMaster;
import com.apps.rescueconnect.model.rescueRequest.RescueRequestSubDetails;
import com.apps.rescueconnect.ui.roledetails.MainActivityInteractor;
import com.apps.rescueconnect.ui.roledetails.ViewRescueRequestActivity;
import com.apps.rescueconnect.ui.roledetails.rescueTeamOfficer.RescueOfficerDashboardFragment;
import com.apps.rescueconnect.ui.roledetails.rescueTeamOfficer.RescueOfficerMainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.rxbinding.view.RxView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.functions.Action1;

public class ViewUserRescueDetailsByOfficer extends Fragment implements UserRescueRequestByOfficerMainAdapter.UserRescueRequestItemClickListener {
    private static final String TAG = ViewUserRescueDetailsByOfficer.class.getSimpleName();
    private View rootView;
    private MainActivityInteractor mainActivityInteractor;
    private ProgressDialog progressDialog;
    private TextView textCityRescueHeader, textNoRescueAvailable;

    // Firebase Storage
    FirebaseDatabase firebaseDatabase;
    private DatabaseReference mUserReferenceRescueRequest;

    private RecyclerView recyclerViewUserRescueRequestByOfficer;
    private UserRescueRequestByOfficerMainAdapter userRescueRequestByOfficerMainAdapter;

    private List<RescueRequestMaster> rescueRequestMasterList = new ArrayList<>();
    private User loginUser;

    private List<City> cityList = new ArrayList<>();
    private List<String> cityStringList = new ArrayList<>();
    private List<User> mainUserList = new ArrayList<>();
    private HashMap<String, User> userMap = new HashMap<>();

    public ViewUserRescueDetailsByOfficer() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_view_user_rescue_requests_by_officer, container, false);
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
            mainActivityInteractor.setScreenTitle(getString(R.string.rescue_officer_btn_second_option));

            progressDialog = new ProgressDialog(requireContext());

            firebaseDatabase = FirebaseDatabase.getInstance();
            mUserReferenceRescueRequest = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.RESQUE_REQUEST_LIST_TABLE);

            loginUser = Utils.getLoginUserDetails(requireContext());
            getCityList();
            getUserList();
            if (loginUser != null) {
                getUserRescueRequestList(loginUser);
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
            recyclerViewUserRescueRequestByOfficer = rootView.findViewById(R.id.recycler_user_rescues_by_officer);
            textNoRescueAvailable = rootView.findViewById(R.id.no_rescues_available);
            textCityRescueHeader = rootView.findViewById(R.id.city_rescues_header);

            if (rescueRequestMasterList.size() > 0) {
                textNoRescueAvailable.setVisibility(View.GONE);
                textCityRescueHeader.setVisibility(View.VISIBLE);
                recyclerViewUserRescueRequestByOfficer.setVisibility(View.VISIBLE);
            } else {
                textCityRescueHeader.setVisibility(View.VISIBLE);
                recyclerViewUserRescueRequestByOfficer.setVisibility(View.GONE);
                textNoRescueAvailable.setVisibility(View.VISIBLE);
            }

            if (loginUser != null) {
//                String cityRescueRequestText = loginUser.getUserCity() + " City Requests";
                String cityRescueRequestText = "Assigned Rescue Requests";
                textCityRescueHeader.setText(cityRescueRequestText);
            }

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
            recyclerViewUserRescueRequestByOfficer.setLayoutManager(linearLayoutManager);
            userRescueRequestByOfficerMainAdapter = new UserRescueRequestByOfficerMainAdapter(requireContext(), rescueRequestMasterList, this);
            recyclerViewUserRescueRequestByOfficer.setAdapter(userRescueRequestByOfficerMainAdapter);

            if (userRescueRequestByOfficerMainAdapter != null) {
                userRescueRequestByOfficerMainAdapter.notifyDataSetChanged();
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

    public void getUserRescueRequestList(User user) {
        try {
            showProgressDialog("Fetching details, please wait");
            mUserReferenceRescueRequest.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    hideProgressDialog();
                    if (snapshot.exists()) {
                        Log.d(TAG, "onDataChange: snapshot: " + snapshot);
                        rescueRequestMasterList.clear();
                        String loginUserId = AESHelper.decryptData(loginUser.getMobileNumber());
                        String loginUserName = loginUser.getFullName();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Log.d(TAG, "onDataChange: dataSnapshot: " + dataSnapshot);
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                Log.d(TAG, "onDataChange: child: " + postSnapshot);
                                for (DataSnapshot childData : postSnapshot.getChildren()) {
                                    RescueRequestMaster rescueRequestMaster = childData.getValue(RescueRequestMaster.class);
                                    if (rescueRequestMaster != null) {
                                        String resqueAgencyName = rescueRequestMaster.getRescueRequestAssignAgencies();
                                        List<RescueRequestSubDetails> rescueRequestSubDetailsList = rescueRequestMaster.getRescueRequestSubDetailsList();
                                        if (rescueRequestSubDetailsList.size() > 0) {
                                            int lastPos = rescueRequestSubDetailsList.size() - 1;
                                            RescueRequestSubDetails rescueRequestSubDetails = rescueRequestSubDetailsList.get(lastPos);
                                            if (rescueRequestSubDetails != null) {
                                                String agencyId = rescueRequestSubDetails.getRescueRequestAcceptedId();
                                                if (agencyId.equalsIgnoreCase(loginUserId)) {
                                                    rescueRequestMasterList.add(rescueRequestMaster);
                                                } else if (resqueAgencyName.equalsIgnoreCase(loginUserName)) {
                                                    rescueRequestMasterList.add(rescueRequestMaster);
                                                }
                                            }
                                        }
                                       /* if (rescueRequestMaster.getRescueRequestAssignAgencies().equalsIgnoreCase(loginUser.getFullName())) {
                                            rescueRequestMasterList.add(rescueRequestMaster);
                                        }*/
                                    }
                                }
                            }
                        }
                    }
                    Log.d(TAG, "onDataChange: mnRescueRequestMasterList:" + rescueRequestMasterList);
                    setUpViews();
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    hideProgressDialog();
                    Log.d(TAG, "onCancelled: error: " + error.getMessage());
                    setUpViews();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void userRescueRequestUpdateClicked(int position, RescueRequestMaster rescueRequestMaster, String userRescueStatus) {
        try {
            if (userRescueStatus.equalsIgnoreCase(AppConstants.COMPLETED_STATUS)) {
                RescueConnectToast.showAlertToast(requireContext(), "Resque request already Completed.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            } else if (userRescueStatus.equalsIgnoreCase(AppConstants.CANCELLED_STATUS)) {
                RescueConnectToast.showAlertToast(requireContext(), "Rescue request cancelled by User, can't Update.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            } else {
                showDialogForRescueRequestStatusUpdate(requireContext(), position, userRescueStatus, rescueRequestMaster);
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

    @Override
    public void userRescueRequestAssignClicked(int position, RescueRequestMaster rescueRequestMaster, String userRescueStatus) {
        try {
            if (userRescueStatus.equalsIgnoreCase(AppConstants.COMPLETED_STATUS)) {
                RescueConnectToast.showAlertToast(requireContext(), "Resque request already Completed.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            } else if (userRescueStatus.equalsIgnoreCase(AppConstants.CANCELLED_STATUS)) {
                RescueConnectToast.showAlertToast(requireContext(), "Rescue request cancelled by User, can't Update.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            } else {
                showDialogForRescueRequestStatusAssign(requireContext(), position, userRescueStatus, rescueRequestMaster);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void userRescueRequestClicked(int position, RescueRequestMaster rescueRequestMaster, ImageView imageView, TextView textView) {
        try {
            if (NetworkUtil.getConnectivityStatus(requireContext())) {
                Intent intentView = new Intent(requireContext(), ViewRescueRequestActivity.class);
                intentView.putExtra(AppConstants.VIEW_RESCUE_REQUEST_DATA, rescueRequestMaster);

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

    public void showDialogForRescueRequestStatusUpdate(Context context, int position, String currentStatus, RescueRequestMaster rescueRequestMaster) {
        try {
            String lastStatus = "";

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_rescue_status_update, null);
            builder.setView(dialogView);
            builder.setCancelable(false);

            // TextView and EditText Initialization
            TextView textMainHeader = dialogView.findViewById(R.id.text_rescue_status_update_main_header);
            TextView textRescueRequestHeader = dialogView.findViewById(R.id.text_rescue_header);
            TextView textRescueRequestAccessTypeHeader = dialogView.findViewById(R.id.text_rescue_access_type_header);
            TextView textRescueRequestAccessTypeValue = dialogView.findViewById(R.id.text_rescue_access_type_value);
            TextView textRescueRequestStatusHeader = dialogView.findViewById(R.id.text_rescue_status_header);
            TextView textRescueRequestStatusValue = dialogView.findViewById(R.id.text_rescue_status_value);

            //Button Initialization
            Button btnUpdate = dialogView.findViewById(R.id.btn_update);
            Button btnClose = dialogView.findViewById(R.id.btn_close);

            RxView.clicks(textRescueRequestAccessTypeValue).subscribe(new Action1<Void>() {
                @Override
                public void call(Void unused) {
                    RescueConnectToast.showAlertToast(requireContext(), getString(R.string.dont_have_rescue_access_type), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
                }
            });

            List<String> complaintOrRescueRequestNextStatusList = DataUtils.getNextStatusBasedOnRole(currentStatus, loginUser.getMainRole());

            RxView.touches(textRescueRequestStatusValue).subscribe(motionEvent -> {
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(requireContext());
                        builderSingle.setTitle(AppConstants.CALAMITY_STATUS);

                        final ArrayAdapter<String> taskStatusSelectionAdapter = new ArrayAdapter<String>(requireContext(),
                                android.R.layout.select_dialog_singlechoice, complaintOrRescueRequestNextStatusList) {
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
                            textRescueRequestStatusValue.setText(taskStatusSelectionAdapter.getItem(subPosition));
                        });
                        builderSingle.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            textRescueRequestAccessTypeHeader.setText(R.string.rescue_access_type_header);
            textRescueRequestAccessTypeValue.setText(rescueRequestMaster.getRescueRequestAccessType());

            String headerMessage = rescueRequestMaster.getRescueRequestType() + " : " + rescueRequestMaster.getRescueRequestPlacePhotoId();
            textMainHeader.setText(headerMessage);
            textMainHeader.setTextColor(context.getResources().getColor(R.color.error_color, null));

            textRescueRequestHeader.setText(rescueRequestMaster.getRescueRequestHeader());

            textRescueRequestStatusHeader.setText(AppConstants.CALAMITY_STATUS);

            int lastPosition = (rescueRequestMaster.getRescueRequestSubDetailsList().size() - 1);
            Log.d(TAG, "lastPosition: " + lastPosition);

            if (lastPosition >= 0) {
                lastStatus = rescueRequestMaster.getRescueRequestSubDetailsList().get(lastPosition).getRescueRequestStatus();
                Log.d(TAG, "lastStatus: " + lastStatus);
                textRescueRequestStatusValue.setText(lastStatus);
            }

            AlertDialog alert = builder.create();
            alert.show();

            String finalLastStatus = lastStatus;
            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (!(finalLastStatus.equalsIgnoreCase(textRescueRequestStatusValue.getText().toString().trim()))) {
                            User loginUser = Utils.getLoginUserDetails(requireActivity());

                            RescueRequestSubDetails rescueRequestSubDetails = new RescueRequestSubDetails();
                            rescueRequestSubDetails.setRescueRequestId(rescueRequestMaster.getRescueRequestPlacePhotoId());
                            rescueRequestSubDetails.setRescueRequestAcceptedId(loginUser.getMobileNumber());
                            rescueRequestSubDetails.setRescueRequestStatus(textRescueRequestStatusValue.getText().toString().trim());
                            rescueRequestSubDetails.setRescueRequestModifiedBy(loginUser.getMobileNumber());
                            rescueRequestSubDetails.setRescueRequestModifiedOn(Utils.getCurrentTimeStampWithSeconds());
                            rescueRequestSubDetails.setRescueRequestAcceptedRole(loginUser.getMainRole());
                            rescueRequestMaster.getRescueRequestSubDetailsList().add(rescueRequestSubDetails);

                            Log.d(TAG, "onClick: rescueRequestMaster: " + rescueRequestMaster);

                            updateRescueDetails(position, rescueRequestMaster, alert);
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

    public void showDialogForRescueRequestStatusAssign(Context context, int position, String currentStatus, RescueRequestMaster rescueRequestMaster) {
        try {
            String lastStatus = "";

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_rescue_assign, null);
            builder.setView(dialogView);
            builder.setCancelable(false);

            // TextView and EditText Initialization
            TextView textMainHeader = dialogView.findViewById(R.id.text_rescue_assign_main_header);
            TextView textRescueRequestHeader = dialogView.findViewById(R.id.text_rescue_header);

            TextView textRescueSelectCityHeader = dialogView.findViewById(R.id.text_rescue_select_city_header);
            TextView textRescueSelectCityValue = dialogView.findViewById(R.id.text_rescue_select_city_value);

            TextView textRescueOtherAgenciesHeader = dialogView.findViewById(R.id.text_rescue_other_agencies_header);
            TextView textRescueOtherAgenciesValue = dialogView.findViewById(R.id.text_rescue_other_agencies_value);

            //Button Initialization
            Button btnAssign = dialogView.findViewById(R.id.btn_assign);
            Button btnClose = dialogView.findViewById(R.id.btn_close);

            String headerMessage = rescueRequestMaster.getRescueRequestType() + " : " + rescueRequestMaster.getRescueRequestPlacePhotoId();
            textMainHeader.setText(headerMessage);
            textMainHeader.setTextColor(context.getResources().getColor(R.color.error_color, null));

            textRescueSelectCityHeader.setText(getString(R.string.rescue_select_city_header));
            textRescueRequestHeader.setText(rescueRequestMaster.getRescueRequestHeader());

            textRescueSelectCityValue.setHint(getString(R.string.rescue_select_city_header));
            textRescueOtherAgenciesValue.setHint(getString(R.string.rescue_other_agency_header));

            textRescueOtherAgenciesHeader.setText(R.string.rescue_other_agency_header);

            RxView.touches(textRescueSelectCityValue).subscribe(motionEvent -> {
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(requireContext());
                        builderSingle.setTitle(getString(R.string.select_city));

                        final ArrayAdapter<String> citySelectionAdapter = new ArrayAdapter<String>(requireContext(),
                                android.R.layout.select_dialog_singlechoice, cityStringList) {
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

                        builderSingle.setAdapter(citySelectionAdapter, (dialog, subPosition) -> {
                            textRescueSelectCityValue.setText(citySelectionAdapter.getItem(subPosition));
                            textRescueOtherAgenciesValue.setText("");
                            textRescueOtherAgenciesValue.setHint(getString(R.string.rescue_other_agency_header));
                        });
                        builderSingle.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            RxView.touches(textRescueOtherAgenciesValue).subscribe(motionEvent -> {
                try {
                    String cityName = textRescueSelectCityValue.getText().toString().trim();
                    List<String> otherRescueAgenciesList = getCityWiseAgenciesList(cityName);
                    if (!cityName.isEmpty()) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            AlertDialog.Builder builderSingle = new AlertDialog.Builder(requireContext());
                            builderSingle.setTitle(getString(R.string.rescue_other_agency_header));

                            final ArrayAdapter<String> rescueOtherAgenciesSelectionAdapter = new ArrayAdapter<String>(requireContext(),
                                    android.R.layout.select_dialog_singlechoice, otherRescueAgenciesList) {
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

                            builderSingle.setAdapter(rescueOtherAgenciesSelectionAdapter, (dialog, subPosition) -> {
                                textRescueOtherAgenciesValue.setText(rescueOtherAgenciesSelectionAdapter.getItem(subPosition));
                            });
                            builderSingle.show();
                        }
                    } else {
                        RescueConnectToast.showAlertToast(requireContext(), "Please select city", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();

            btnAssign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (NetworkUtil.getConnectivityStatus(requireContext())) {
                            String selectedAgencyName = getAgenciesOfficerName(textRescueOtherAgenciesValue.getText().toString().trim());
                            User selectedUser = userMap.get(selectedAgencyName);
                            String assignAgencyCity = textRescueSelectCityValue.getText().toString().trim();
                            if (selectedUser != null) {
                                RescueRequestMaster updateRescueMaster = rescueRequestMaster;
                                Log.d(TAG, "onClick: rescueRequestMaster before: " + updateRescueMaster);
                                String mobileNumber = AESHelper.decryptData(selectedUser.getMobileNumber());
                                Log.d(TAG, "onClick: selectedUser:" + selectedUser);
                                updateRescueMaster.setRescueRequestAcceptedOfficerId(mobileNumber);
                                updateRescueMaster.setRescueRequestAcceptedOfficerName(selectedUser.getFullName());
                                updateRescueMaster.setRescueRequestAssignAgencies(selectedUser.getFullName());

                                RescueRequestSubDetails rescueRequestSubDetails = new RescueRequestSubDetails();
                                rescueRequestSubDetails.setRescueRequestId(updateRescueMaster.getRescueRequestPlacePhotoId());
                                rescueRequestSubDetails.setRescueRequestAcceptedId(mobileNumber);
                                rescueRequestSubDetails.setRescueRequestModifiedBy(mobileNumber);
                                rescueRequestSubDetails.setRescueRequestModifiedOn(Utils.getCurrentTimeStampWithSeconds());
                                rescueRequestSubDetails.setRescueRequestAcceptedRole(selectedUser.getMainRole());
                                rescueRequestSubDetails.setRescueRequestStatus(AppConstants.NEW_STATUS);
                                updateRescueMaster.getRescueRequestSubDetailsList().add(rescueRequestSubDetails);

                                Log.d(TAG, "onClick: rescueRequestMaster after: " + updateRescueMaster);
                                reAssignRescueDetails(position, updateRescueMaster, rescueRequestMaster, alert);
                            } else {
                                RescueConnectToast.showErrorToast(requireContext(), "Failed to load agency details", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                            }
                        } else {
                            RescueConnectToast.showErrorToast(requireContext(), getString(R.string.no_internet), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
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

    public void updateRescueDetails(int position, RescueRequestMaster rescueRequestMaster, AlertDialog alert) {
        try {
            showProgressDialog("Updating status please wait.");
            User loginUser = Utils.getLoginUserDetails(requireActivity());
            mUserReferenceRescueRequest.child(rescueRequestMaster.getRescueRequestCity()).child(rescueRequestMaster.getRescueRequestAcceptedOfficerId()).child(rescueRequestMaster.getRescueRequestPlacePhotoId()).setValue(rescueRequestMaster)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressDialog();
                            RescueConnectToast.showSuccessToast(requireContext(), "RescueRequest status updated successfully.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
                            if (alert != null) {
                                alert.dismiss();
                            }
                            if (userRescueRequestByOfficerMainAdapter != null) {
                                userRescueRequestByOfficerMainAdapter.notifyItemChanged(position);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            RescueConnectToast.showErrorToast(requireContext(), "Failed to update rescue request.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                        }
                    });
        } catch (Exception e) {
            hideProgressDialog();
            RescueConnectToast.showErrorToast(requireContext(), e.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            e.printStackTrace();
        }
    }

    public void reAssignRescueDetails(int position, RescueRequestMaster updatedRescueRequestMaster, RescueRequestMaster oldRescueRequestMaster, AlertDialog alert) {
        try {
            showProgressDialog("Reassigning.. please wait.");
            mUserReferenceRescueRequest.child(oldRescueRequestMaster.getRescueRequestCity()).child(oldRescueRequestMaster.getRescueRequestAcceptedOfficerId()).child(oldRescueRequestMaster.getRescueRequestPlacePhotoId()).setValue(updatedRescueRequestMaster)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressDialog();

                            RescueConnectToast.showSuccessToast(requireContext(), "RescueRequest assigned successfully.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
                            if (alert != null) {
                                alert.dismiss();
                            }
//                            if (userRescueRequestByOfficerMainAdapter != null) {
//                                userRescueRequestByOfficerMainAdapter.notifyItemChanged(position);
//                            }
                            if (requireActivity() != null) {
                                if(requireActivity().getSupportFragmentManager() != null){
 requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_content_main, new ViewUserRescueDetailsByOfficer()).commit();
                                }
                            }

                          /*  if (updatedRescueRequestMaster.getRescueRequestCity().equalsIgnoreCase(oldRescueRequestMaster.getRescueRequestCity())) {
                                RescueConnectToast.showSuccessToast(requireContext(), "RescueRequest status updated successfully.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
                                if (alert != null) {
                                    alert.dismiss();
                                }
                                if (userRescueRequestByOfficerMainAdapter != null) {
                                    userRescueRequestByOfficerMainAdapter.notifyItemChanged(position);
                                }
                            } else {
                                deleteOldDataIfOtherCitySelected(position, oldRescueRequestMaster, alert);
                            }*/
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            RescueConnectToast.showErrorToast(requireContext(), "Failed to update rescue request.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                        }
                    });
        } catch (Exception e) {
            hideProgressDialog();
            RescueConnectToast.showErrorToast(requireContext(), e.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            e.printStackTrace();
        }
    }

    private void refreshFragment(){
        try{
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager()
                    .beginTransaction();
            if (Build.VERSION.SDK_INT >= 26) {
                transaction.setReorderingAllowed(false);
            }
            transaction.detach(this).attach
                    (this).commit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void deleteOldDataIfOtherCitySelected(int position, RescueRequestMaster oldRescueRequestMaster, AlertDialog alert) {
        try {
            showProgressDialog("Updating.. please wait.");
            mUserReferenceRescueRequest.child(oldRescueRequestMaster.getRescueRequestCity()).child(oldRescueRequestMaster.getRescueRequestAcceptedOfficerId()).child(oldRescueRequestMaster.getRescueRequestPlacePhotoId()).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressDialog();
                            RescueConnectToast.showSuccessToast(requireContext(), "RescueRequest reassigned successfully.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
                            if (alert != null) {
                                alert.dismiss();
                            }
                            if (userRescueRequestByOfficerMainAdapter != null) {
                                userRescueRequestByOfficerMainAdapter.notifyItemChanged(position);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            RescueConnectToast.showSuccessToast(requireContext(), "RescueRequest reassigned successfully.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
                        }
                    });
        } catch (Exception e) {
            hideProgressDialog();
            RescueConnectToast.showSuccessToast(requireContext(), "RescueRequest reassigned successfully.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
            e.printStackTrace();
        }
    }

    public void getCityList() {
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.CITY_TABLE);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        cityList.clear();
                        cityStringList.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            City city = postSnapshot.getValue(City.class);
                            if (city != null) {
                                cityList.add(city);
                                cityStringList.add(city.getCityName());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: error: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getUserList() {
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.USERS_TABLE);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        mainUserList.clear();
                        userMap.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            User user = postSnapshot.getValue(User.class);
                            if (user != null) {
                                if (user.getMainRole().equalsIgnoreCase(AppConstants.ROLE_AGENCY_OFFICER)) {
                                    String userMobileNumber = AESHelper.decryptData(user.getMobileNumber());
                                    if (!(loginUser.getMobileNumber().equalsIgnoreCase(userMobileNumber))) {
                                        mainUserList.add(user);
                                        userMap.put(user.getFullName(), user);
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: error: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> getCityWiseAgenciesList(String cityName) {
        List<String> agenciesName = new ArrayList<>();
        try {
            if (mainUserList != null && mainUserList.size() > 0) {
                for (User user : mainUserList) {
                    if (user != null && user.getUserCity().equalsIgnoreCase(cityName)) {
                        String userMobileNumber = AESHelper.decryptData(user.getMobileNumber());
                        if (!(userMobileNumber.equalsIgnoreCase(loginUser.getMobileNumber()))) {
                            agenciesName.add(user.getFullName() + " : (" + user.getUserAgency() + ")");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agenciesName;
    }

    private String getAgenciesOfficerName(String selectedAgencies) {
        try {
            if (selectedAgencies != null) {
                String[] spittedName = selectedAgencies.split(" : ");
                if (spittedName.length > 1) {
                    return spittedName[0];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return selectedAgencies;
    }
}