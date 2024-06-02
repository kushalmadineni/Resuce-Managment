package com.apps.rescueconnect.ui.roledetails;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.rescueConnectToast.RescueConnectToast;
import com.apps.rescueconnect.model.rescueRequest.RescueRequestMaster;
import com.bumptech.glide.Glide;

public class ViewRescueRequestActivity extends AppCompatActivity {

    private static final String TAG = ViewRescueRequestActivity.class.getSimpleName();
    private TextView textTitle, textRescueRequestType, textRescueRequestHeader, textRescueRequestDesc, textRescueRequestPlace, textRescueRequestAddress, textRescueRequestLocation, textNavigateToLocation;
    private RecyclerView recyclerSubDetails;
    private ImageView imageRescueRequestPhoto;
    private Button btnBack;

    private RescueRequestMaster rescueRequestMaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_rescue_request_viewer);

            if (getIntent() != null) {
                rescueRequestMaster = getIntent().getParcelableExtra(AppConstants.VIEW_RESCUE_REQUEST_DATA);
            }
            Log.d(TAG, "onCreate: rescueRequestMaster: " + rescueRequestMaster);

            setUpViews();

            loadRescueRequestDetails();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpViews() {
        textTitle = findViewById(R.id.title);
        textRescueRequestType = findViewById(R.id.text_rescue_request_type);
        textRescueRequestHeader = findViewById(R.id.text_rescue_request_title_value);
        textRescueRequestDesc = findViewById(R.id.text_rescue_request_desc);
        textRescueRequestPlace = findViewById(R.id.text_rescue_request_place);

        textRescueRequestAddress = findViewById(R.id.text_rescue_request_address);
        textRescueRequestLocation = findViewById(R.id.text_rescue_request_location);
        textNavigateToLocation = findViewById(R.id.text_navigate);

        imageRescueRequestPhoto = findViewById(R.id.photo_image);
        btnBack = findViewById(R.id.btn_back);

        textTitle.setText("View Rescue Request Details");

        textNavigateToLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (rescueRequestMaster != null && (rescueRequestMaster.getRescueRequestPlaceLatitude() != 0.0)) {
                        openGoogleMapDirections(rescueRequestMaster.getRescueRequestPlaceLatitude(), rescueRequestMaster.getRescueRequestPlaceLongitude());
                    } else {
                        RescueConnectToast.showAlertToast(ViewRescueRequestActivity.this, "Unable to fetch location", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    callBackResult();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void openGoogleMapDirections(double latitude, double longitude) {
        try {
            // By Default set Google Map
            String mapRequest = "google.navigation:q=" + latitude + "," + longitude + "&avoid=tf";
            Uri gmmIntentUri = Uri.parse(mapRequest);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRescueRequestDetails() {
        try {
            if (rescueRequestMaster != null) {
                String complaintType = rescueRequestMaster.getRescueRequestType() + " : " + rescueRequestMaster.getRescueRequestPlacePhotoId();
                textRescueRequestType.setText(complaintType);
                textRescueRequestHeader.setText(rescueRequestMaster.getRescueRequestHeader());
                textRescueRequestDesc.setText(rescueRequestMaster.getRescueRequestDescription());
                textRescueRequestPlace.setText(rescueRequestMaster.getRescueRequestCity());

                if (rescueRequestMaster.getRescueRequestPlaceAddress() == null || rescueRequestMaster.getRescueRequestPlaceAddress().isEmpty()) {
                    textRescueRequestAddress.setText("Address Not Available");
                    textRescueRequestAddress.setTextColor(getResources().getColor(R.color.colorError, null));
                } else {
                    textRescueRequestAddress.setText(rescueRequestMaster.getRescueRequestPlaceAddress());
                    textRescueRequestAddress.setTextColor(getResources().getColor(R.color.colorBlack, null));
                }

                String locationText = "Location Not Available";
                if (rescueRequestMaster.getRescueRequestPlaceLatitude() != 0.0) {
                    locationText = "Latitude: " + rescueRequestMaster.getRescueRequestPlaceLatitude() + ", Longitude: " + rescueRequestMaster.getRescueRequestPlaceLongitude();
                    textRescueRequestLocation.setTextColor(getResources().getColor(R.color.colorBlack, null));
                } else {
                    textRescueRequestLocation.setTextColor(getResources().getColor(R.color.colorError, null));
                }

                textRescueRequestLocation.setText(locationText);

                imageRescueRequestPhoto = findViewById(R.id.rescue_request_photo);

                Glide.with(imageRescueRequestPhoto)
                        .load(rescueRequestMaster.getRescueRequestPlacePhotoPath())
                        .fitCenter()
                        .into(imageRescueRequestPhoto);
            } else {
                RescueConnectToast.showErrorToast(ViewRescueRequestActivity.this, "Failed to load Rescue Request details", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callBackResult() {
        try {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}