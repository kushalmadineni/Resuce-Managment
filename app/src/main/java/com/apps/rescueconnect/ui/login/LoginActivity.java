package com.apps.rescueconnect.ui.login;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;

import com.apps.rescueconnect.BuildConfig;
import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.FireBaseDatabaseConstants;
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.helper.encryption.AESHelper;
import com.apps.rescueconnect.helper.rescueConnectToast.RescueConnectToast;
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.ui.registration.RegistrationActivity;
import com.apps.rescueconnect.ui.roledetails.admin.AdminMainActivity;
import com.apps.rescueconnect.ui.roledetails.rescueTeamOfficer.RescueOfficerMainActivity;
import com.apps.rescueconnect.ui.roledetails.resqueAdmin.RescueAdminMainActivity;
import com.apps.rescueconnect.ui.roledetails.user.usermain.UserMainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RESULT_CODE = 111;
    private ProgressDialog progressDialog;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 999;

    private CoordinatorLayout loginCoordinatorLayout;
    private Button btnLogin;
    private TextView textSignup, textVersion;
    private EditText editMobileNumber;
    private TextInputEditText editMPIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);
        progressDialog = new ProgressDialog(LoginActivity.this);

        setUpViews();

        if (checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            // Otherwise, prompt user to get valid Play Services APK.
            if (!(Utils.checkLocationPermission(getApplicationContext()))) {
                requestPermission();
            }
        } else {
            Toast.makeText(LoginActivity.this, "Location not supported in this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpViews() {
        loginCoordinatorLayout = findViewById(R.id.login_coordinator_layout);

        btnLogin = findViewById(R.id.btn_login);
        textSignup = findViewById(R.id.text_signup);
        textVersion = findViewById(R.id.text_version);

        editMobileNumber = findViewById(R.id.edit_mobile_number);
        editMPIn = findViewById(R.id.edit_mPin);

        String version = "v." + BuildConfig.VERSION_NAME;
        textVersion.setText(version);

        textSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(LoginActivity.this, RegistrationActivity.class), RESULT_CODE);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideKeyboard(LoginActivity.this);
                if (validateFields())
                    checkLogin();
            }
        });

    }

    private boolean validateFields() {
        if (TextUtils.isEmpty(editMobileNumber.getText().toString())) {
            RescueConnectToast.showErrorToast(LoginActivity.this, getString(R.string.enter_phone_number), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        } else if (editMobileNumber.getText().toString().length() != 10) {
            RescueConnectToast.showErrorToast(LoginActivity.this, getString(R.string.phone_number_digits), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        } else if (TextUtils.isEmpty(editMPIn.getText().toString())) {
            RescueConnectToast.showErrorToast(LoginActivity.this, getString(R.string.mpin_enter), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        } else if (editMPIn.getText().toString().length() != 4) {
            RescueConnectToast.showErrorToast(LoginActivity.this, getString(R.string.mpin_digit_validation), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        }
        return true;
    }

    private void checkLogin() {
        try {
            String userMobileNumber = editMobileNumber.getText().toString().trim();
            String userMPin = editMPIn.getText().toString().trim();

            showProgressDialog("Verifying please wait.");

            verifyUserLogin(LoginActivity.this, userMobileNumber, userMPin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void verifyUserLogin(Context context, String userMobileNumber, String mPin) {
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.USERS_TABLE);

            databaseReference.child(userMobileNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String mobileNumber = snapshot.child(FireBaseDatabaseConstants.USER_MOBILE_NUMBER).getValue(String.class);
                        String mobilePin = snapshot.child(FireBaseDatabaseConstants.USER_M_PIN).getValue(String.class);
                        Log.d(TAG, "onDataChange: mobileNumber: " + mobileNumber);
                        Log.d(TAG, "onDataChange: mobileNumber: " + mobilePin);
                        if (mobileNumber != null && mobilePin != null) {
                            if (AESHelper.encryptData(userMobileNumber).equals(mobileNumber) && mobilePin.equals(AESHelper.encryptData(mPin))) {
                                String userMPin = snapshot.child(FireBaseDatabaseConstants.USER_M_PIN).getValue(String.class);
                                String userMainRole = snapshot.child(FireBaseDatabaseConstants.USER_MAIN_ROLE).getValue(String.class);
                                String userCity = snapshot.child(FireBaseDatabaseConstants.USER_CITY).getValue(String.class);
                                String userType = snapshot.child(FireBaseDatabaseConstants.USER_TYPE).getValue(String.class);
                                String userFullName = snapshot.child(FireBaseDatabaseConstants.USER_FULL_NAME).getValue(String.class);
                                String userGender = snapshot.child(FireBaseDatabaseConstants.USER_GENDER).getValue(String.class);
                                String userIsActive = snapshot.child(FireBaseDatabaseConstants.USER_IS_ACTIVE).getValue(String.class);
                                String userAgency = snapshot.child(FireBaseDatabaseConstants.USER_AGENCY).getValue(String.class);
                                String userUserAgencyPath = snapshot.child(FireBaseDatabaseConstants.USER_AGENCY_PATH).getValue(String.class);

                                Log.d(TAG, "onDataChange: role: " + userMainRole);
                                Log.d(TAG, "onDataChange: userIsActive: " + userIsActive);
                                if (userIsActive.equalsIgnoreCase(AppConstants.ACTIVE_USER)) {
                                    User user = new User();
                                    user.setMobileNumber(userMobileNumber);
                                    user.setmPin(userMPin);
                                    user.setMainRole(userMainRole);
                                    user.setUserCity(userCity);
                                    user.setUserType(userType);
                                    user.setUserAgency(userAgency);
                                    user.setUserAgencyPath(userUserAgencyPath);
                                    user.setFullName(userFullName);
                                    user.setGender(userGender);
                                    user.setIsActive(userIsActive);
                                    Utils.saveSharedPrefsString(context, AppConstants.LOGIN_TOKEN, userMobileNumber);
                                    Utils.saveSharedPrefsString(context, AppConstants.USER_ROLE, user.getMainRole());
                                    Utils.saveLoginUserDetails(context, user);
                                    hideProgressDialog();

                                    RescueConnectToast.showSuccessToastWithBottom(context, "Login success", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                                    navigateToDashboard(user.getMainRole());
                                } else {
                                    hideProgressDialog();
                                    RescueConnectToast.showErrorToastWithBottom(context, userMobileNumber + " is not activated or deActivated, Please contact admin.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
                                }
                            } else {
                                hideProgressDialog();
                                RescueConnectToast.showErrorToastWithBottom(context, "Credential mismatch.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                            }
                        } else {
                            hideProgressDialog();
                            RescueConnectToast.showErrorToastWithBottom(context, "Mobile number and mPin null", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                        }
                    } else {
                        hideProgressDialog();
                        RescueConnectToast.showErrorToastWithBottom(context, "Failed to login, verify credentials", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    hideProgressDialog();
                    RescueConnectToast.showErrorToastWithBottom(context, error.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                }
            });
        } catch (Exception e) {
            hideProgressDialog();
            e.printStackTrace();
            RescueConnectToast.showErrorToastWithBottom(context, e.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
        }
    }

    private void navigateToDashboard(String userMainRole) {
        try {
            if (userMainRole != null) {
                switch (userMainRole) {
                    case AppConstants.ROLE_ADMIN: {
                        startActivity(new Intent(LoginActivity.this, AdminMainActivity.class));
                        finish();
                        break;
                    }
                    case AppConstants.ROLE_AGENCY_ADMIN: {
                        startActivity(new Intent(LoginActivity.this, RescueAdminMainActivity.class));
                        finish();
                        break;
                    }
                    case AppConstants.ROLE_AGENCY_OFFICER: {
                        startActivity(new Intent(LoginActivity.this, RescueOfficerMainActivity.class));
                        finish();
                        break;
                    }
                    default: {
                        startActivity(new Intent(LoginActivity.this, UserMainActivity.class));
                        finish();
                        break;
                    }
                }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (RESULT_CODE): {
                if (resultCode == Activity.RESULT_OK) {
//                    String returnValue = data.getStringExtra("some_key");
                }
                break;
            }
        }
    }


    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(resultCode)) {
                googleAPI.getErrorDialog(LoginActivity.this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                LoginActivity.this.finish();
            }
            return false;
        }
        return true;
    }


    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Display a SnackBar with an explanation and a button to trigger the request.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Snackbar.make(loginCoordinatorLayout, "Some core functions of the app might not work correctly without these permissions.\nPlease ALLOW them in app settings",
                                Snackbar.LENGTH_INDEFINITE)
                        .setAction("ALLOW", view -> requestPermissions(AppConstants.PERMISSIONS_LOCATION, AppConstants.PERMISSION_REQUEST_LOCATION))
                        .show();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(AppConstants.PERMISSIONS_LOCATION, AppConstants.PERMISSION_REQUEST_LOCATION);
            }
        }
    }

}