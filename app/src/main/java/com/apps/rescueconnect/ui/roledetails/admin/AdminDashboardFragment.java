package com.apps.rescueconnect.ui.roledetails.admin;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.FireBaseDatabaseConstants;
import com.apps.rescueconnect.helper.SliderUtils;
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.ui.roledetails.MainActivityInteractor;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardFragment extends Fragment {
    private static final String TAG = AdminDashboardFragment.class.getSimpleName();
    private View rootView;
    private MainActivityInteractor mainActivityInteractor;

    private TextView textNoUsersAvailable, textAllUsersChartHeader;
    private PieChart pieChartAllUsers;

    private LinearLayout userPieChartLayout;

    private List<User> userList = new ArrayList<>();

    private ProgressDialog progressDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
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
            mainActivityInteractor.setScreenTitle(getString(R.string.admin_dashboard_title));
            progressDialog = new ProgressDialog(requireContext());

            ImageSlider imageSlider = rootView.findViewById(R.id.image_slider);
            List<SlideModel> slideModelList = SliderUtils.getAdminDashboardSliderItemList();
            imageSlider.setImageList(slideModelList, ScaleTypes.FIT); // for all images
            imageSlider.startSliding(SliderUtils.SLIDER_TIME); // with new period

            setUpViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpViews() {
        try {
            textAllUsersChartHeader = rootView.findViewById(R.id.all_city_users_header);
            textNoUsersAvailable = rootView.findViewById(R.id.no_users_available);

            userPieChartLayout = rootView.findViewById(R.id.user_chart_layout);
            pieChartAllUsers = rootView.findViewById(R.id.pie_chart_all_users);

            setPieChartOfUser();

            loadAllUsersList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setPieChartOfUser() {
        try {
            pieChartAllUsers.setUsePercentValues(false);
            pieChartAllUsers.setDrawEntryLabels(false);
            pieChartAllUsers.getDescription().setEnabled(false);
            pieChartAllUsers.setExtraOffsets(0, 0, 0, 10);

            pieChartAllUsers.setDragDecelerationFrictionCoef(0.95f);

            pieChartAllUsers.setDrawHoleEnabled(false);

            pieChartAllUsers.setTransparentCircleColor(Color.WHITE);
            pieChartAllUsers.setTransparentCircleAlpha(110);

            pieChartAllUsers.setHoleRadius(58f);
            pieChartAllUsers.setTransparentCircleRadius(61f);

            pieChartAllUsers.setDrawCenterText(false);

            pieChartAllUsers.setRotationAngle(0);
            // enable rotation of the chart by touch
            pieChartAllUsers.setRotationEnabled(true);
            pieChartAllUsers.setHighlightPerTapEnabled(true);

            pieChartAllUsers.animateY(500, Easing.EaseInOutQuad);
            // pieChartAllUsers.spin(2000, 0, 360);

            Legend l = pieChartAllUsers.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            l.setOrientation(Legend.LegendOrientation.VERTICAL);
            l.setDrawInside(false);
            l.setXEntrySpace(0f);
            l.setYEntrySpace(0f);
            l.setYOffset(0f);
            l.setXOffset(0f);
            l.setTextSize(8f);

            // entry label styling
            pieChartAllUsers.setEntryLabelColor(Color.WHITE);
            pieChartAllUsers.setEntryLabelTextSize(12f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void loadAllUsersList() {
        try {
            showProgressDialog("User details loading..");

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.USERS_TABLE);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    hideProgressDialog();
                    userList.clear();
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        User userMain = postSnapshot.getValue(User.class);
                        Log.d(TAG, "onDataChange: userMain: " + userMain);
                        if (userMain != null) {
                            if (userMain.getMainRole() != null) {
                                if (!(userMain.getMainRole().equalsIgnoreCase(AppConstants.ROLE_ADMIN))) {
                                    userList.add(userMain);
                                }
                            }
                        }
                    }
                    generatePieChartForUserDetails(userList);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    hideProgressDialog();
                    generatePieChartForUserDetails(userList);
                    Log.d(TAG, "onCancelled: failed to load user details");
                }
            });
        } catch (Exception e) {
            hideProgressDialog();
            generatePieChartForUserDetails(userList);
            Log.d(TAG, "loadAllUsers: exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generatePieChartForUserDetails(List<User> userList) {
        try {
            if (userList.size() > 0) {
                textNoUsersAvailable.setVisibility(View.GONE);
                userPieChartLayout.setVisibility(View.VISIBLE);

                int users = 0;
                int rescueOfficer = 0;
                int totalUsers = 0;

                for (User user : userList) {
                    if (user.getMainRole().equals(AppConstants.ROLE_AGENCY_OFFICER)) {
                        rescueOfficer = rescueOfficer + 1;
                        totalUsers = totalUsers + 1;
                    } else if (user.getMainRole().equals(AppConstants.ROLE_USER)) {
                        users = users + 1;
                        totalUsers = totalUsers + 1;
                    }
                }

                Log.d(TAG, "generatePieChartForUserDetails: users: " + users);
                Log.d(TAG, "generatePieChartForUserDetails: rescueOfficer: " + rescueOfficer);
                Log.d(TAG, "generatePieChartForUserDetails: totalUsers: " + totalUsers);

                ArrayList<PieEntry> entries = new ArrayList<>();


                entries.add(new PieEntry(users, "User  "));
                entries.add(new PieEntry(rescueOfficer, "Resque Agency"));

                String totalUsersTitle = "Total users : " + totalUsers;
                PieDataSet dataSet = new PieDataSet(entries, totalUsersTitle);
                dataSet.setSliceSpace(3f);
                dataSet.setSelectionShift(5f);

                // add a lot of colors
                ArrayList<Integer> colors = new ArrayList<>();

                colors.add(Color.rgb(51, 255, 0));
                colors.add(Color.rgb(255, 51, 255));

                dataSet.setColors(colors);

                PieData data = new PieData(dataSet);
                data.setValueFormatter(new LargeValueFormatter());
                data.setValueTextSize(11f);
                data.setValueTextColor(Color.WHITE);
                pieChartAllUsers.setData(data);

                // undo all highlights
                pieChartAllUsers.highlightValues(null);

                pieChartAllUsers.invalidate();
            } else {
                userPieChartLayout.setVisibility(View.GONE);
                textNoUsersAvailable.setVisibility(View.VISIBLE);
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
}