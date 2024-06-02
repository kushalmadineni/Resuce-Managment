package com.apps.rescueconnect.ui.roledetails.user.usermain;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.FireBaseDatabaseConstants;
import com.apps.rescueconnect.helper.SliderUtils;
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.model.rescueRequest.RescueRequestMaster;
import com.apps.rescueconnect.ui.roledetails.MainActivityInteractor;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserDashboardFragment extends Fragment implements UserDashboardMainAdapter.RescueRequestMasterClickListener {
    private static final String TAG = UserDashboardFragment.class.getSimpleName();
    private View rootView;
    private MainActivityInteractor mainActivityInteractor;

    private TextView textNoPublicRescueRequestAvailable;
    private RecyclerView recyclerPublicRescueRequest;
    private UserDashboardMainAdapter userDashboardIssueMainAdapter;

    private List<RescueRequestMaster> rescueRequestMasterList = new ArrayList<>();

    private ProgressDialog progressDialog;

    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE};

    final private int MULTIPLE_PERMISSIONS = 124;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_user_dashboard, container, false);
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
        mainActivityInteractor.setScreenTitle(getString(R.string.user_dashboard_title));
        progressDialog = new ProgressDialog(requireContext());

        ImageSlider imageSlider = rootView.findViewById(R.id.image_slider);
        List<SlideModel> slideModelList = SliderUtils.getUserDashboardSliderItemList();
        imageSlider.setImageList(slideModelList, ScaleTypes.FIT); // for all images
        imageSlider.startSliding(SliderUtils.SLIDER_TIME); // with new period

        checkPermissions();
//        setUpViews();
        loadAllRescueRequestStatusList();
    }

    private void setUpViews() {
        try {
            textNoPublicRescueRequestAvailable = rootView.findViewById(R.id.no_rescues_available);
            recyclerPublicRescueRequest = rootView.findViewById(R.id.recycler_public_issue);

            if (rescueRequestMasterList.size() > 0) {
                textNoPublicRescueRequestAvailable.setVisibility(View.GONE);
                recyclerPublicRescueRequest.setVisibility(View.VISIBLE);

                LinearLayoutManager linearLayoutManager = new GridLayoutManager(requireContext(), 1);
                recyclerPublicRescueRequest.setLayoutManager(linearLayoutManager);
                userDashboardIssueMainAdapter = new UserDashboardMainAdapter(requireContext(), rescueRequestMasterList, this);
                recyclerPublicRescueRequest.setAdapter(userDashboardIssueMainAdapter);

                if (userDashboardIssueMainAdapter != null) {
                    userDashboardIssueMainAdapter.notifyDataSetChanged();
                }
            } else {
                recyclerPublicRescueRequest.setVisibility(View.GONE);
                textNoPublicRescueRequestAvailable.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadAllRescueRequestStatusList() {
        try {
            showProgressDialog("All public rescueRequest loading..");

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.RESQUE_REQUEST_LIST_TABLE);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        hideProgressDialog();
                        rescueRequestMasterList.clear();
                        User loginUser = Utils.getLoginUserDetails(requireActivity());
                        for (DataSnapshot typeSnapshot : snapshot.getChildren()) {
                            for (DataSnapshot userSnapshot : typeSnapshot.getChildren()) {
                                for (DataSnapshot compSnapshot : userSnapshot.getChildren()) {
                                    RescueRequestMaster rescueRequestMaster = compSnapshot.getValue(RescueRequestMaster.class);
                                    if (rescueRequestMaster != null) {
                                        if (rescueRequestMaster.getRescueRequestAccessType().equalsIgnoreCase(AppConstants.CALAMITY_ACCESS_TYPE_PUBLIC)) {
                                            if (rescueRequestMaster.getRescueRequestCity().equalsIgnoreCase(loginUser.getUserCity())) {
                                                rescueRequestMasterList.add(rescueRequestMaster);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        setUpViews();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    hideProgressDialog();
                    setUpViews();
                    Log.d(TAG, "onCancelled: failed to load user details");
                }
            });
        } catch (
                Exception e) {
            hideProgressDialog();
            setUpViews();
            Log.d(TAG, "loadAllUsers: exception: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void rescueRequestMasterClicked(int position, ImageView imageIssue, TextView textIssueHeader, RescueRequestMaster rescueRequestMaster) {
        try {

            /*if (NetworkUtil.getConnectivityStatus(requireContext())) {
                Intent intentView = new Intent(requireContext(), ViewRescueRequestActivity.class);
                intentView.putExtra(AppConstants.VIEW_RESCUE_REQUEST_DATA, rescueRequestMaster);

                Pair<View, String> transactionPairOne = Pair.create((View) imageIssue, requireContext().getResources().getString(R.string.transaction_rescue_details_photo));
                Pair<View, String> transactionPairTwo = Pair.create((View) textIssueHeader, requireContext().getResources().getString(R.string.transaction_rescue_details_header));

                // Call Multiple Shared Transaction using Pair Option
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(requireActivity(), transactionPairOne, transactionPairTwo);
                startActivityForResult(intentView, 3, options.toBundle());

            } else {
                RescueConnectToast.showErrorToast(requireContext(), getString(R.string.no_internet), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
            }*/

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


    private boolean checkPermissions() {
        try {
            int result;
            List<String> listPermissionsNeeded = new ArrayList<>();
            for (String p : permissions) {
                result = ContextCompat.checkSelfPermission(requireContext(), p);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p);
                }
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(requireActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        try {
            switch (requestCode) {
                case MULTIPLE_PERMISSIONS: {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // permissions granted.
                    } else {
                        String perStr = "";
                        for (String per : permissions) {
                            perStr += "\n" + per;
                        }
                        // permissions list of don't granted permission
                    }
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}