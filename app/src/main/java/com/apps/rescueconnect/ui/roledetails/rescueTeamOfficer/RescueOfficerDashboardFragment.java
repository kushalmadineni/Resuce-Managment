package com.apps.rescueconnect.ui.roledetails.rescueTeamOfficer;

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
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.model.rescueRequest.RescueRequestMaster;
import com.apps.rescueconnect.model.rescueRequest.RescueRequestSubDetails;
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

public class RescueOfficerDashboardFragment extends Fragment {
    private static final String TAG = RescueOfficerDashboardFragment.class.getSimpleName();
    private View rootView;
    private MainActivityInteractor mainActivityInteractor;

    private TextView textNoRescuesAvailable, textAllRescuesStatusHeader;
    private PieChart pieChartAllRescuesStatus;

    private LinearLayout allRescuesStatusPieChartLayout;

    private List<RescueRequestMaster> rescuesMasterList = new ArrayList<>();

    private ProgressDialog progressDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_rescue_officer_dashboard, container, false);
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
        mainActivityInteractor.setScreenTitle(getString(R.string.rescue_officer_dashboard_title));
        progressDialog = new ProgressDialog(requireContext());

        ImageSlider imageSlider = rootView.findViewById(R.id.image_slider);
        List<SlideModel> slideModelList = SliderUtils.getAgencyOfficerDashboardSliderItemList();
        imageSlider.setImageList(slideModelList, ScaleTypes.FIT); // for all images
        imageSlider.startSliding(SliderUtils.SLIDER_TIME); // with new period

        setUpViews();
    }

    private void setUpViews() {
        try {
            textAllRescuesStatusHeader = rootView.findViewById(R.id.all_complaints_status_header);
            textNoRescuesAvailable = rootView.findViewById(R.id.no_rescues_available);

            allRescuesStatusPieChartLayout = rootView.findViewById(R.id.rescues_status_chart_layout);
            pieChartAllRescuesStatus = rootView.findViewById(R.id.pie_chart_all_rescues_status);

            setPieChartOfRescuesStatus();

            loadAllRescuesStatusList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPieChartOfRescuesStatus() {
        try {
            pieChartAllRescuesStatus.setUsePercentValues(false);
            pieChartAllRescuesStatus.setDrawEntryLabels(false);
            pieChartAllRescuesStatus.getDescription().setEnabled(false);
            pieChartAllRescuesStatus.setExtraOffsets(0, 0, 0, 10);

            pieChartAllRescuesStatus.setDragDecelerationFrictionCoef(0.95f);

            pieChartAllRescuesStatus.setDrawHoleEnabled(false);

            pieChartAllRescuesStatus.setTransparentCircleColor(Color.WHITE);
            pieChartAllRescuesStatus.setTransparentCircleAlpha(110);

            pieChartAllRescuesStatus.setHoleRadius(58f);
            pieChartAllRescuesStatus.setTransparentCircleRadius(61f);

            pieChartAllRescuesStatus.setDrawCenterText(false);

            pieChartAllRescuesStatus.setRotationAngle(0);
            // enable rotation of the chart by touch
            pieChartAllRescuesStatus.setRotationEnabled(true);
            pieChartAllRescuesStatus.setHighlightPerTapEnabled(true);

            pieChartAllRescuesStatus.animateY(500, Easing.EaseInOutQuad);
            // pieChartAllRescuesStatus.spin(2000, 0, 360);

            Legend l = pieChartAllRescuesStatus.getLegend();
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
            pieChartAllRescuesStatus.setEntryLabelColor(Color.WHITE);
            pieChartAllRescuesStatus.setEntryLabelTextSize(12f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void loadAllRescuesStatusList() {
        try {
            showProgressDialog("Rescue details loading..");

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.RESQUE_REQUEST_LIST_TABLE);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        hideProgressDialog();
                        rescuesMasterList.clear();
                        if (requireContext() != null) {
                            User loginUser = Utils.getLoginUserDetails(requireContext());
                            for (DataSnapshot typeSnapshot : snapshot.getChildren()) {
                                for (DataSnapshot userSnapshot : typeSnapshot.getChildren()) {
                                    for (DataSnapshot compSnapshot : userSnapshot.getChildren()) {
                                        RescueRequestMaster rescueRequestMaster = compSnapshot.getValue(RescueRequestMaster.class);
                                        if (rescueRequestMaster != null && rescueRequestMaster.getRescueRequestAssignAgencies().equalsIgnoreCase(loginUser.getFullName())) {
                                            rescuesMasterList.add(rescueRequestMaster);
                                        }
                                    }
                                }
                            }
                            generatePieChartForRescueStatusDetails(rescuesMasterList);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    hideProgressDialog();
                    generatePieChartForRescueStatusDetails(rescuesMasterList);
                    Log.d(TAG, "onCancelled: failed to load user details");
                }
            });
        } catch (Exception e) {
            hideProgressDialog();
            generatePieChartForRescueStatusDetails(rescuesMasterList);
            Log.d(TAG, "loadAllUsers: exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generatePieChartForRescueStatusDetails(List<RescueRequestMaster> rescuesMasterList) {
        try {
            if (rescuesMasterList.size() > 0) {
                textNoRescuesAvailable.setVisibility(View.GONE);
                allRescuesStatusPieChartLayout.setVisibility(View.VISIBLE);

                int newStatus = 0;
                int acceptedStatus = 0;
                int completedStatus = 0;
                int rejectedStatus = 0;
                int cancelledStatus = 0;
                int totalRescues = 0;

                for (RescueRequestMaster rescueRequestMaster : rescuesMasterList) {
                    if (rescueRequestMaster.getRescueRequestSubDetailsList().size() > 0) {
                        int lastPosition = rescueRequestMaster.getRescueRequestSubDetailsList().size() - 1;
                        RescueRequestSubDetails rescueRequestSubDetails = rescueRequestMaster.getRescueRequestSubDetailsList().get(lastPosition);
                        if (rescueRequestSubDetails.getRescueRequestStatus().equalsIgnoreCase(AppConstants.NEW_STATUS)) {
                            newStatus = newStatus + 1;
                            totalRescues = totalRescues + 1;
                        } else if (rescueRequestSubDetails.getRescueRequestStatus().equalsIgnoreCase(AppConstants.ACCEPTED_STATUS)) {
                            acceptedStatus = acceptedStatus + 1;
                            totalRescues = totalRescues + 1;
                        } else if (rescueRequestSubDetails.getRescueRequestStatus().equalsIgnoreCase(AppConstants.COMPLETED_STATUS)) {
                            completedStatus = completedStatus + 1;
                            totalRescues = totalRescues + 1;
                        } else if (rescueRequestSubDetails.getRescueRequestStatus().equalsIgnoreCase(AppConstants.REJECTED_STATUS)) {
                            rejectedStatus = rejectedStatus + 1;
                            totalRescues = totalRescues + 1;
                        } else if (rescueRequestSubDetails.getRescueRequestStatus().equalsIgnoreCase(AppConstants.CANCELLED_STATUS)) {
                            cancelledStatus = cancelledStatus + 1;
                            totalRescues = totalRescues + 1;
                        }
                    }
                }

                Log.d(TAG, "generatePieChartForRescueStatusDetails: newStatus: " + newStatus);
                Log.d(TAG, "generatePieChartForRescueStatusDetails: acceptedStatus: " + acceptedStatus);
                Log.d(TAG, "generatePieChartForRescueStatusDetails: completedStatus: " + completedStatus);
                Log.d(TAG, "generatePieChartForRescueStatusDetails: rejectedStatus: " + rejectedStatus);
                Log.d(TAG, "generatePieChartForRescueStatusDetails: cancelledStatus: " + cancelledStatus);
                Log.d(TAG, "generatePieChartForRescueStatusDetails: totalRescues: " + totalRescues);

                ArrayList<PieEntry> entries = new ArrayList<>();


                entries.add(new PieEntry(newStatus, "New  "));
                entries.add(new PieEntry(acceptedStatus, "Accepted  "));
                entries.add(new PieEntry(completedStatus, "Completed  "));
                entries.add(new PieEntry(rejectedStatus, "Rejected  "));
                entries.add(new PieEntry(cancelledStatus, "Cancelled  "));

                String totalRescuesText = "Total rescues : " + totalRescues;

                PieDataSet dataSet = new PieDataSet(entries, totalRescuesText);
                dataSet.setSliceSpace(3f);
                dataSet.setSelectionShift(5f);

                // add a lot of colors
                ArrayList<Integer> colors = new ArrayList<>();

                colors.add(Color.rgb(84, 153, 199));
                colors.add(Color.rgb(155, 89, 182));
                colors.add(Color.rgb(51, 255, 0));
                colors.add(Color.rgb(255, 110, 0));
                colors.add(Color.rgb(255, 87, 34));

                dataSet.setColors(colors);

                PieData data = new PieData(dataSet);
                data.setValueFormatter(new LargeValueFormatter());
                data.setValueTextSize(11f);
                data.setValueTextColor(Color.WHITE);
                pieChartAllRescuesStatus.setData(data);

                // undo all highlights
                pieChartAllRescuesStatus.highlightValues(null);

                pieChartAllRescuesStatus.invalidate();
            } else {
                allRescuesStatusPieChartLayout.setVisibility(View.GONE);
                textNoRescuesAvailable.setVisibility(View.VISIBLE);
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