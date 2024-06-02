package com.apps.rescueconnect.ui.settings.updateMpin;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.FireBaseDatabaseConstants;
import com.apps.rescueconnect.helper.NetworkUtil;
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.helper.encryption.AESHelper;
import com.apps.rescueconnect.helper.rescueConnectToast.RescueConnectToast;
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.ui.roledetails.MainActivityInteractor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateMPin extends Fragment {
    private static final String TAG = UpdateMPin.class.getSimpleName();
    private View rootView;

    private TextInputEditText editOldMPin, editNewMPin;
    private TextView textPersonName, textMobileNumber;
    private Button btnUpdate;
    private ProgressDialog progressDialog;

    private MainActivityInteractor mainActivityInteractor;

    // Firebase Storage
    FirebaseDatabase firebaseDatabase;
    private DatabaseReference mUserReference;

    public UpdateMPin() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_update_mpin, container, false);
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
            mainActivityInteractor.setScreenTitle(AppConstants.SETTINGS_UPDATE_MPIN);

            progressDialog = new ProgressDialog(requireContext());

            firebaseDatabase = FirebaseDatabase.getInstance();
            mUserReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.USERS_TABLE);

            setUpViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpViews() {
        try {
            editOldMPin = rootView.findViewById(R.id.edit_old_mPin);
            editNewMPin = rootView.findViewById(R.id.edit_new_mPin);

            textPersonName = rootView.findViewById(R.id.text_person_name);
            textMobileNumber = rootView.findViewById(R.id.text_mobile_number_value);

            btnUpdate = rootView.findViewById(R.id.btn_update);

            editOldMPin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
            editNewMPin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (NetworkUtil.getConnectivityStatus(requireContext())) {
                        if (validateFields()) {
                            User loginUser = Utils.getLoginUserDetails(requireContext());
                            Log.d(TAG, "onClick: loginUser:" + loginUser);
                            if (AESHelper.decryptData(loginUser.getmPin()).equals(editOldMPin.getText().toString().trim())) {
                                loginUser.setmPin(AESHelper.encryptData(editNewMPin.getText().toString().trim()));
                                showProgressDialog("Processing please wait.");
                                Log.d(TAG, "onClick: loginUser:" + loginUser);
                                updateUserDetails(loginUser);
                            } else {
                                RescueConnectToast.showErrorToastWithBottom(requireContext(), "Please verify old mPin.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                            }
                        }
                    } else {
                        RescueConnectToast.showErrorToast(requireContext(), getString(R.string.no_internet), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                    }
                }
            });

            User loginUser = Utils.getLoginUserDetails(requireContext());
            Log.d(TAG, "setUpViews: loginUser: " + loginUser);
            updateUserDetailsToView(loginUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        try {
            editOldMPin.setText("");
            editNewMPin.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        try {
            if (Utils.isEmptyField(editOldMPin.getText().toString().trim())) {
                RescueConnectToast.showErrorToastWithBottom(requireContext(), "Please enter old mPin.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            } else if (editOldMPin.getText().toString().trim().length() < 4) {
                RescueConnectToast.showErrorToastWithBottom(requireContext(), "Old mPin must be 4 Digits.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            } else if (Utils.isEmptyField(editNewMPin.getText().toString().trim())) {
                RescueConnectToast.showErrorToastWithBottom(requireContext(), "Please enter new mPin.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            } else if (editNewMPin.getText().toString().trim().length() < 4) {
                RescueConnectToast.showErrorToastWithBottom(requireContext(), "New mPin must be 4 Digits.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            } else if (editOldMPin.getText().toString().trim().equals(editNewMPin.getText().toString().trim())) {
                RescueConnectToast.showErrorToastWithBottom(requireContext(), "New mPin not same as Old mPin", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateUserDetails(User userMain) {
        try {
            String mobileNumber = userMain.getMobileNumber();
            userMain.setMobileNumber(AESHelper.encryptData(mobileNumber));
            mUserReference.child(mobileNumber).setValue(userMain)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressDialog();
                            userMain.setMobileNumber(mobileNumber);
                            Utils.saveLoginUserDetails(requireContext(), userMain);
                            RescueConnectToast.showSuccessToastWithBottom(requireContext(), "mPin updated successfully", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                            clearFields();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            RescueConnectToast.showErrorToastWithBottom(requireContext(), "Failed to update mPin", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                        }
                    });
        } catch (Exception e) {
            hideProgressDialog();
            RescueConnectToast.showErrorToastWithBottom(requireContext(), e.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            e.printStackTrace();
        }
    }

    private void updateUserDetailsToView(User userMain) {
        try {
            if (userMain != null) {
                textPersonName.setText(userMain.getFullName());
                textMobileNumber.setText(userMain.getMobileNumber());
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