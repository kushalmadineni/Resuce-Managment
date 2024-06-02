package com.apps.rescueconnect.ui.registration;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.FireBaseDatabaseConstants;
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.helper.dataUtils.DataUtils;
import com.apps.rescueconnect.helper.encryption.AESHelper;
import com.apps.rescueconnect.helper.rescueConnectToast.RescueConnectToast;
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.model.citydetails.City;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.rxbinding.view.RxView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RegistrationActivity extends AppCompatActivity {
    private static final String TAG = RegistrationActivity.class.getSimpleName();
    private List<City> cityList = new ArrayList<>();
    private List<String> cityStringList = new ArrayList<>();

    private ProgressDialog progressDialog;
    // Firebase Storage
    FirebaseDatabase firebaseDatabase;
    private DatabaseReference mUserReference;

    private EditText editMobilePhone, editUserFullName;
    private TextInputEditText editMPin;
    private TextView textCityHeader, textCity, textUserFullNameHeader, textGender, textSelectUserTypeHeader;
    private Button btnSignup;
    private RadioGroup radioGroupUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        progressDialog = new ProgressDialog(RegistrationActivity.this);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mUserReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.USERS_TABLE);
        getCityList();
        setUpViews();
    }

    private void setUpViews() {
        btnSignup = findViewById(R.id.btn_signup);

        radioGroupUserType = findViewById(R.id.radio_group_user_type);

        editMobilePhone = findViewById(R.id.edit_mobile_phone);
        editMPin = findViewById(R.id.edit_mPin);
        editUserFullName = findViewById(R.id.edit_user_full_name);

        textCityHeader = findViewById(R.id.text_city_header);
        textCity = findViewById(R.id.text_city);
        textUserFullNameHeader = findViewById(R.id.text_user_full_name_header);
        textGender = findViewById(R.id.text_gender);
        textSelectUserTypeHeader = findViewById(R.id.text_user_type_header);

        // Radio Group of selecting User Type
        radioGroupUserType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int radioButtonId) {
                if (radioButtonId == R.id.radio_user_general) {
                    textUserFullNameHeader.setVisibility(View.VISIBLE);
                    editUserFullName.setVisibility(View.VISIBLE);
                } else if (radioButtonId == R.id.radio_user_anonymous) {
                    textUserFullNameHeader.setVisibility(View.GONE);
                    editUserFullName.setVisibility(View.GONE);
                }
            }
        });

        RxView.touches(textCity).subscribe(motionEvent -> {
            try {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(RegistrationActivity.this);
                    builderSingle.setTitle(RegistrationActivity.this.getString(R.string.select_city));
                    final ArrayAdapter<String> citySelectionAdapter = new ArrayAdapter<String>(RegistrationActivity.this,
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
                    });
                    builderSingle.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        List<String> genderTypeList = DataUtils.getGenderType();

        RxView.touches(textGender).subscribe(motionEvent -> {
            try {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(RegistrationActivity.this);
                    builderSingle.setTitle("Select Gender");

                    final ArrayAdapter<String> genderTypeSelectionAdapter = new ArrayAdapter<String>(RegistrationActivity.this,
                            android.R.layout.select_dialog_singlechoice, genderTypeList) {
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

                    builderSingle.setAdapter(genderTypeSelectionAdapter, (dialog, position) -> {
                        textGender.setText(genderTypeSelectionAdapter.getItem(position));
                    });
                    builderSingle.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideKeyboard(RegistrationActivity.this);
                if (validateFields()) {
                    buildValueAndSubmit();
                }
            }
        });


    }

    private boolean validateFields() {
        if (TextUtils.isEmpty(editMobilePhone.getText().toString())) {
            RescueConnectToast.showErrorToast(RegistrationActivity.this, getString(R.string.enter_phone_number), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        } else if (editMobilePhone.getText().toString().length() != 10) {
            RescueConnectToast.showErrorToast(RegistrationActivity.this, getString(R.string.phone_number_digits), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        } else if (TextUtils.isEmpty(editMPin.getText().toString())) {
            RescueConnectToast.showErrorToast(RegistrationActivity.this, getString(R.string.mpin_enter), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        } else if (editMPin.getText().toString().length() != 4) {
            RescueConnectToast.showErrorToast(RegistrationActivity.this, getString(R.string.mpin_digit_validation), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        }
        if (TextUtils.isEmpty(textCity.getText().toString())) {
            RescueConnectToast.showErrorToast(RegistrationActivity.this, getString(R.string.select_city_alert), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        } else if ((radioGroupUserType.getCheckedRadioButtonId() == R.id.radio_user_general) && TextUtils.isEmpty(editUserFullName.getText().toString())) {
            RescueConnectToast.showErrorToast(RegistrationActivity.this, getString(R.string.enter_name), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        } else if (TextUtils.isEmpty(textGender.getText().toString())) {
            RescueConnectToast.showErrorToast(RegistrationActivity.this, getString(R.string.select_gender), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        }
        return true;
    }

    private void buildValueAndSubmit() {
        User user = new User();
        user.setMobileNumber(editMobilePhone.getText().toString().trim());
        user.setmPin(editMPin.getText().toString().trim());

        user.setUserCity(textCity.getText().toString().trim());
        user.setMainRole(AppConstants.ROLE_USER);
        user.setIsActive(AppConstants.ACTIVE_USER);

        if (radioGroupUserType.getCheckedRadioButtonId() == R.id.radio_user_general) {
            user.setUserType(AppConstants.USER_TYPE_GENERAL);
            user.setFullName(editUserFullName.getText().toString().trim());
            user.setUserAgency("");
            user.setUserAgencyPath("");
        } else {
            user.setUserType(AppConstants.USER_TYPE_ANONYMOUS);
            user.setFullName(AppConstants.USER_TYPE_ANONYMOUS);
            user.setUserAgency("");
            user.setUserAgencyPath("");
        }

        user.setGender(textGender.getText().toString().trim());

        verifyUserRegistration(user);
    }

    public void signUpNewUser(User user) {
        try {
            String userMobileNumber = user.getMobileNumber();
            user.setMobileNumber(AESHelper.encryptData(userMobileNumber));
            user.setmPin(AESHelper.encryptData(user.getmPin()));
            showProgressDialog(getString(R.string.processing_wait));
            mUserReference.child(userMobileNumber).setValue(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressDialog();
                            String responseMessage = "User created successfully.";
                            if (user.getMainRole().equals(AppConstants.ROLE_USER)) {
                                responseMessage = "Hi " + user.getFullName() + "..! your role as a \"" + user.getMainRole() + "\" created successfully.";
                            } else {
                                responseMessage = "Hi " + user.getFullName() + "..! your role as a \"" + user.getMainRole() + "\" created successfully, Contact admin to activate.";
                            }
                            showSuccessAlertDialogForUserCreated(responseMessage);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            RescueConnectToast.showErrorToast(RegistrationActivity.this, "Failed to register", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                        }
                    });
        } catch (Exception e) {
            hideProgressDialog();
            RescueConnectToast.showErrorToast(RegistrationActivity.this, e.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            e.printStackTrace();
        }
    }

    public void verifyUserRegistration(User user) {
        try {
            showProgressDialog(getString(R.string.processing_wait));
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.USERS_TABLE);
            databaseReference.child(user.getMobileNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    hideProgressDialog();
                    if (snapshot.exists()) {
                        String mobileNumber = snapshot.child(FireBaseDatabaseConstants.USER_MOBILE_NUMBER).getValue(String.class);
                        Log.d(TAG, "onDataChange: userMobileNumber: " + user.getMobileNumber());
                        Log.d(TAG, "onDataChange: mobileNumber: " + mobileNumber);
                        if (mobileNumber != null) {
                            if (!(AESHelper.decryptData(mobileNumber).equalsIgnoreCase(user.getMobileNumber()))) {
                                signUpNewUser(user);
                            } else {
                                RescueConnectToast.showErrorToast(RegistrationActivity.this, mobileNumber + " Mobile number already exists", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
                            }
                        } else {
                            signUpNewUser(user);
                        }
                    } else {
                        signUpNewUser(user);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    hideProgressDialog();
                    signUpNewUser(user);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            hideProgressDialog();
            signUpNewUser(user);
        }
    }

    public void navigateToLogin() {
        try {
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
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

    public void showSuccessAlertDialogForUserCreated(String message) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alert_dialog_with_single_buttons, null);
            builder.setView(dialogView);
            builder.setCancelable(false);

            // TextView and EditText Initialization
            TextView textAlertHeader = dialogView.findViewById(R.id.dialog_message_header);
            TextView textAlertDesc = dialogView.findViewById(R.id.dialog_message_desc);

            TextView textBtn = dialogView.findViewById(R.id.text_button);

            textAlertHeader.setText("Success..!");
            textAlertHeader.setTextColor(getColor(R.color.colorSuccess));

            textAlertDesc.setText(message);
            textBtn.setText("Login");

            AlertDialog alert = builder.create();
            alert.show();

            textBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        alert.dismiss();
                        navigateToLogin();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}