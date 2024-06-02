package com.apps.rescueconnect.ui.roledetails.rescueTeamOfficer.viewUserRescueDetailsbyCity;

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
import com.apps.rescueconnect.helper.rescueConnectToast.RescueConnectToast;
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.model.citydetails.City;
import com.apps.rescueconnect.model.rescueRequest.RescueRequestMaster;
import com.apps.rescueconnect.ui.roledetails.MainActivityInteractor;
import com.apps.rescueconnect.ui.roledetails.ViewRescueRequestActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.rxbinding.view.RxView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ViewCityWiseUserRescueRequestByOfficer extends Fragment implements UserCityWiseRescuedetailsByOfficerMainAdapter.UserRescueItemClickListener {
    private static final String TAG = ViewCityWiseUserRescueRequestByOfficer.class.getSimpleName();
    private View rootView;
    private MainActivityInteractor mainActivityInteractor;
    private ProgressDialog progressDialog;
    private TextView textNoRescuesAvailable, textCity;

    // Firebase Storage
    FirebaseDatabase firebaseDatabase;
    private DatabaseReference mUserReferencerescue;

    private RecyclerView recyclerViewUserrescueByOfficer;
    private UserCityWiseRescuedetailsByOfficerMainAdapter userrescueByOfficerMainAdapter;

    private List<City> cityList = new ArrayList<>();
    private List<String> cityStringList = new ArrayList<>();

    private List<RescueRequestMaster> rescueRequestMasterList = new ArrayList<>();
    private User loginUser;

    public ViewCityWiseUserRescueRequestByOfficer() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_view_user_city_wise_rescue_request_by_officer, container, false);
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
            mainActivityInteractor.setScreenTitle(getString(R.string.rescue_officer_btn_third_option));

            progressDialog = new ProgressDialog(requireContext());

            firebaseDatabase = FirebaseDatabase.getInstance();
            mUserReferencerescue = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.RESQUE_REQUEST_LIST_TABLE);

            loginUser = Utils.getLoginUserDetails(requireContext());

            textCity = rootView.findViewById(R.id.text_city);

            getCityList();

            if (loginUser != null) {
                textCity.setText(loginUser.getUserCity());
                getUserRescueList(loginUser.getUserCity(), true);
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
            recyclerViewUserrescueByOfficer = rootView.findViewById(R.id.recycler_user_rescues_by_officer);
            textNoRescuesAvailable = rootView.findViewById(R.id.no_rescues_available);
//            textCityRescuesHeader = rootView.findViewById(R.id.city_rescues_header);

            if (rescueRequestMasterList.size() > 0) {
                textNoRescuesAvailable.setVisibility(View.GONE);
//                textCityRescuesHeader.setVisibility(View.VISIBLE);
                recyclerViewUserrescueByOfficer.setVisibility(View.VISIBLE);
            } else {
//                textCityRescuesHeader.setVisibility(View.VISIBLE);
                recyclerViewUserrescueByOfficer.setVisibility(View.GONE);
                textNoRescuesAvailable.setVisibility(View.VISIBLE);
            }

            if (loginUser != null) {
                String cityrescueText = loginUser.getUserCity() + " City Requests";
//                textCityRescuesHeader.setText(cityrescueText);
            }

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
            recyclerViewUserrescueByOfficer.setLayoutManager(linearLayoutManager);
            userrescueByOfficerMainAdapter = new UserCityWiseRescuedetailsByOfficerMainAdapter(requireContext(), rescueRequestMasterList, this);
            recyclerViewUserrescueByOfficer.setAdapter(userrescueByOfficerMainAdapter);

            if (userrescueByOfficerMainAdapter != null) {
                userrescueByOfficerMainAdapter.notifyDataSetChanged();
            }

            RxView.touches(textCity).subscribe(motionEvent -> {
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(requireContext());
                        builderSingle.setTitle(requireContext().getString(R.string.select_city));
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

                        builderSingle.setNegativeButton("Cancel", (dialog, position) -> dialog.dismiss());

                        builderSingle.setAdapter(citySelectionAdapter, (dialog, position) -> {
                            textCity.setText(citySelectionAdapter.getItem(position));

                            getUserRescueList(citySelectionAdapter.getItem(position), false);
                        });
                        builderSingle.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
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

    public void getUserRescueList(String cityName, boolean isSetupLoad) {
        try {
            showProgressDialog("Fetching details, please wait");
            mUserReferencerescue.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    hideProgressDialog();
                    if (snapshot.exists()) {
                        Log.d(TAG, "onDataChange: snapshot: " + snapshot);
                        DataSnapshot dataSnapshot = snapshot.child(cityName);
                        Log.d(TAG, "onDataChange: dataSnapshot: " + dataSnapshot);
                        rescueRequestMasterList.clear();
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Log.d(TAG, "onDataChange: child: " + postSnapshot);
                            for (DataSnapshot childData : postSnapshot.getChildren()) {
                                RescueRequestMaster rescueRequestMaster = childData.getValue(RescueRequestMaster.class);
                                if (rescueRequestMaster != null) {
                                    if (rescueRequestMaster.getRescueRequestAssignAgencies().equalsIgnoreCase(loginUser.getFullName())) {
                                        rescueRequestMasterList.add(rescueRequestMaster);
                                    }
                                }
                            }
                        }
                    }
                    Log.d(TAG, "onDataChange: rescueRequestMasterList:" + rescueRequestMasterList);

                    loadCityWiseDetails(cityName, isSetupLoad);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    hideProgressDialog();
                    Log.d(TAG, "onCancelled: error: " + error.getMessage());
                    loadCityWiseDetails(cityName, isSetupLoad);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCityWiseDetails(String cityName, boolean isSetupLoad) {
        try {
            if (isSetupLoad) {
                setUpViews();
            } else {
                if (rescueRequestMasterList.size() > 0) {
                    textNoRescuesAvailable.setVisibility(View.GONE);
                    recyclerViewUserrescueByOfficer.setVisibility(View.VISIBLE);
                } else {
                    recyclerViewUserrescueByOfficer.setVisibility(View.GONE);
                    textNoRescuesAvailable.setVisibility(View.VISIBLE);
                }

                if (cityName != null) {
//                    textCityRescuesHeader.setVisibility(View.VISIBLE);
                    String cityrescueText = cityName + " City Rescues";
//                    textCityRescuesHeader.setText(cityrescueText);
                }

                if (userrescueByOfficerMainAdapter != null) {
                    userrescueByOfficerMainAdapter.notifyDataSetChanged();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void userRescueViewClicked(int position, RescueRequestMaster rescueRequestMaster, ImageView imageView, TextView textView) {
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


}