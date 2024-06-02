package com.apps.rescueconnect.ui.roledetails.resqueAdmin.rescueTypedetails;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.FireBaseDatabaseConstants;
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.helper.dataUtils.DataUtils;
import com.apps.rescueconnect.helper.encryption.AESHelper;
import com.apps.rescueconnect.helper.rescueConnectToast.RescueConnectToast;
import com.apps.rescueconnect.model.Agencies;
import com.apps.rescueconnect.model.RescueType;
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.model.citydetails.City;
import com.apps.rescueconnect.ui.roledetails.MainActivityInteractor;
import com.apps.rescueconnect.ui.roledetails.resqueAdmin.RescueAdminDashboardFragment;
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
import java.util.HashMap;
import java.util.List;

public class CreateAgencyFragment extends Fragment {

    private static final String TAG = CreateAgencyFragment.class.getSimpleName();

    private View rootView;

    private MainActivityInteractor mainActivityInteractor;
    private List<City> cityList = new ArrayList<>();
    private List<String> cityStringList = new ArrayList<>();
    private List<RescueType> rescueTypeList = new ArrayList<>();
    private HashMap<String, String> agenciesMap = new HashMap<>();
    private List<String> rescueTypeStringList = new ArrayList<>();
    private List<String> agenciesStringList = new ArrayList<>();

    private ProgressDialog progressDialog;
    // Firebase Storage
    FirebaseDatabase firebaseDatabase;
    private DatabaseReference mUserReference;

    private EditText editMobilePhone, editAgencyOfficerFullName;
    private TextInputEditText editMPin;
    private TextView textCityHeader, textCity, textGender, textSelectAgency;
    private Button btnCreateAgency;

    public CreateAgencyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_create_agency, container, false);
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mainActivityInteractor = (MainActivityInteractor) requireActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            mainActivityInteractor.setScreenTitle(getString(R.string.create_agency));
            progressDialog = new ProgressDialog(requireContext());

            firebaseDatabase = FirebaseDatabase.getInstance();
            mUserReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.USERS_TABLE);
            getAgencyList();
            getCityList();
//            getAgencyTypeList();
            setUpViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpViews() {
        try {
            editMobilePhone = rootView.findViewById(R.id.edit_mobile_phone);
            editAgencyOfficerFullName = rootView.findViewById(R.id.edit_user_full_name);
            editMPin = rootView.findViewById(R.id.edit_mPin);
            textCity = rootView.findViewById(R.id.text_city);
            textGender = rootView.findViewById(R.id.text_gender);
            textSelectAgency = rootView.findViewById(R.id.text_agency);
            btnCreateAgency = rootView.findViewById(R.id.btn_create_agency);

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
                        });
                        builderSingle.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            RxView.touches(textSelectAgency).subscribe(motionEvent -> {
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(requireContext());
                        builderSingle.setTitle(requireContext().getString(R.string.select_agency));
                        final ArrayAdapter<String> rescueAgencySelectionAdapter = new ArrayAdapter<String>(requireContext(),
                                android.R.layout.select_dialog_singlechoice, agenciesStringList) {
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

                        builderSingle.setAdapter(rescueAgencySelectionAdapter, (dialog, position) -> {
                            textSelectAgency.setText(rescueAgencySelectionAdapter.getItem(position));
                        });
                        builderSingle.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            /*RxView.touches(textSelectAgency).subscribe(motionEvent -> {
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(requireContext());
                        builderSingle.setTitle(requireContext().getString(R.string.select_rescue_type));
                        final ArrayAdapter<String> rescueTypeSelectionAdapter = new ArrayAdapter<String>(requireContext(),
                                android.R.layout.select_dialog_singlechoice, rescueTypeStringList) {
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

                        builderSingle.setAdapter(rescueTypeSelectionAdapter, (dialog, position) -> {
                            textSelectAgency.setText(rescueTypeSelectionAdapter.getItem(position));
                        });
                        builderSingle.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });*/

            List<String> genderTypeList = DataUtils.getGenderType();

            RxView.touches(textGender).subscribe(motionEvent -> {
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(requireContext());
                        builderSingle.setTitle("Select Gender");

                        final ArrayAdapter<String> genderTypeSelectionAdapter = new ArrayAdapter<String>(requireContext(),
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

            btnCreateAgency.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.hideKeyboard(requireActivity());
                    if (validateFields()) {
                        buildValueAndSubmit();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        if (TextUtils.isEmpty(editMobilePhone.getText().toString())) {
            RescueConnectToast.showErrorToast(requireContext(), getString(R.string.enter_phone_number), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        } else if (editMobilePhone.getText().toString().length() != 10) {
            RescueConnectToast.showErrorToast(requireContext(), getString(R.string.phone_number_digits), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        } else if (TextUtils.isEmpty(editMPin.getText().toString())) {
            RescueConnectToast.showErrorToast(requireContext(), getString(R.string.mpin_enter), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        } else if (editMPin.getText().toString().length() != 4) {
            RescueConnectToast.showErrorToast(requireContext(), getString(R.string.mpin_digit_validation), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        } else if (TextUtils.isEmpty(textCity.getText().toString())) {
            RescueConnectToast.showErrorToast(requireContext(), getString(R.string.select_city_alert), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        } else if (TextUtils.isEmpty(textSelectAgency.getText().toString())) {
            RescueConnectToast.showErrorToast(requireContext(), getString(R.string.select_agency_alert), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        } else if (TextUtils.isEmpty(editAgencyOfficerFullName.getText().toString())) {
            RescueConnectToast.showErrorToast(requireContext(), getString(R.string.enter_officer_name), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        } else if (TextUtils.isEmpty(textGender.getText().toString())) {
            RescueConnectToast.showErrorToast(requireContext(), getString(R.string.select_gender), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        }
        return true;
    }

    private void buildValueAndSubmit() {
        try {
            User user = new User();
            user.setMobileNumber(editMobilePhone.getText().toString().trim());
            user.setmPin(editMPin.getText().toString().trim());

            user.setUserCity(textCity.getText().toString().trim());
            user.setMainRole(AppConstants.ROLE_AGENCY_OFFICER);
            user.setIsActive(AppConstants.ACTIVE_USER);
            String userAgency = textSelectAgency.getText().toString().trim();
            String userAgencyPath = agenciesMap.get(userAgency);
            user.setUserType(userAgency);
            user.setUserAgency(userAgency);
            user.setUserAgencyPath(userAgencyPath);
            user.setFullName(editAgencyOfficerFullName.getText().toString().trim());
            user.setGender(textGender.getText().toString().trim());

            verifyUserRegistration(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                                responseMessage = "Hi admin..! rescue agent " + user.getFullName() + " role as a \"" + user.getMainRole() + "\" created successfully.";
                            } else {
                                responseMessage = "Hi admin..! rescue agent" + user.getFullName() + " role as a \"" + user.getMainRole() + "\" created successfully.";
                            }
                            showSuccessAlertDialogForAgencyCreated(responseMessage);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            RescueConnectToast.showErrorToast(requireContext(), "Failed to create agency.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                        }
                    });
        } catch (Exception e) {
            hideProgressDialog();
            RescueConnectToast.showErrorToast(requireContext(), e.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
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
                                RescueConnectToast.showErrorToast(requireContext(), mobileNumber + " Mobile number already exists", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
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

    private void navigateToRescueAdminPage() {
        try {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_content_main, new RescueAdminDashboardFragment()).commit();
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

    public void getAgencyTypeList() {
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.RESCUE_TYPE_TABLE);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        rescueTypeList.clear();
                        rescueTypeStringList.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            RescueType rescueType = postSnapshot.getValue(RescueType.class);
                            if (rescueType != null) {
                                rescueTypeList.add(rescueType);
                                rescueTypeStringList.add(rescueType.getRescueName());
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

    public void getAgencyList() {
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.RESCUE_AGENCIES_TABLE);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        agenciesMap.clear();
                        agenciesStringList.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            Agencies agencies = postSnapshot.getValue(Agencies.class);
                            if (agencies != null) {
                                agenciesMap.put(agencies.getName(), agencies.getPath());
                                agenciesStringList.add(agencies.getName());
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

    public void showSuccessAlertDialogForAgencyCreated(String message) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alert_dialog_with_single_buttons, null);
            builder.setView(dialogView);
            builder.setCancelable(false);

            // TextView and EditText Initialization
            TextView textAlertHeader = dialogView.findViewById(R.id.dialog_message_header);
            TextView textAlertDesc = dialogView.findViewById(R.id.dialog_message_desc);

            TextView textBtn = dialogView.findViewById(R.id.text_button);

            textAlertHeader.setText("Success..!");
            textAlertHeader.setTextColor(requireContext().getColor(R.color.colorSuccess));

            textAlertDesc.setText(message);
            textBtn.setText("Close");

            AlertDialog alert = builder.create();
            alert.show();

            textBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        alert.dismiss();
                        navigateToRescueAdminPage();
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