package com.apps.rescueconnect.ui.settings.profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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
import com.apps.rescueconnect.helper.NetworkUtil;
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.helper.rescueConnectToast.RescueConnectToast;
import com.apps.rescueconnect.helper.dataUtils.DataUtils;
import com.apps.rescueconnect.helper.encryption.AESHelper;
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.ui.roledetails.MainActivityInteractor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jakewharton.rxbinding.view.RxView;

import java.util.List;

public class Profile extends Fragment {
    private static final String TAG = Profile.class.getSimpleName();
    private View rootView;

    private EditText editUserName;
    private TextView textGender, textMobileNumber, textUserNameHeader;
    private Button btnUpdate;

    private User loginUser;

    private ProgressDialog progressDialog;

    // Firebase Storage
    FirebaseDatabase firebaseDatabase;
    private DatabaseReference mUserReference;

    private MainActivityInteractor mainActivityInteractor;

    public Profile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
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
            mainActivityInteractor.setScreenTitle(AppConstants.SETTINGS_MY_PROFILE);

            progressDialog = new ProgressDialog(requireContext());
            textMobileNumber = rootView.findViewById(R.id.text_mobile_number_value);

            firebaseDatabase = FirebaseDatabase.getInstance();
            mUserReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.USERS_TABLE);

            loginUser = Utils.getLoginUserDetails(requireContext());
            Log.d(TAG, "onViewCreated: loginUser: " + loginUser);

            setUpViews();

            updateUserDetailsToView(loginUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpViews() {
        try {
            editUserName = rootView.findViewById(R.id.edit_user_name);

            textGender = rootView.findViewById(R.id.text_gender);
            textUserNameHeader = rootView.findViewById(R.id.text_user_name_header);

            btnUpdate = rootView.findViewById(R.id.btn_update);

            if (loginUser.getMainRole().equalsIgnoreCase(AppConstants.ROLE_USER) || loginUser.getMainRole().equalsIgnoreCase(AppConstants.ROLE_ADMIN)) {
                if (loginUser.getUserType().equalsIgnoreCase(AppConstants.USER_TYPE_ANONYMOUS)) {
                    textUserNameHeader.setText("Full Name (Anonymous Not Editable)");
                    editUserName.setEnabled(false);
                } else {
                    textUserNameHeader.setText("Full Name");
                    editUserName.setEnabled(true);
                }
            }

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

            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (NetworkUtil.getConnectivityStatus(requireContext())) {
                        if (validateFields()) {
                            User loginUser = Utils.getLoginUserDetails(requireContext());
                            User loginUserUpdate = Utils.getLoginUserDetails(requireContext());

                            loginUserUpdate.setFullName(Utils.getFieldValue(editUserName));
                            loginUserUpdate.setGender(textGender.getText().toString().trim());

                            Log.d(TAG, "onClick: loginUser: " + loginUser);
                            Log.d(TAG, "onClick: loginUserUpdate: " + loginUserUpdate);

                            if (isAnyUpdate(loginUser, loginUserUpdate)) {
                                showProgressDialog("Processing please wait.");
                                updateUserDetails(loginUserUpdate);
                            } else {
                                RescueConnectToast.showInfoToast(requireContext(), "Nothing to update.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                            }
                        }
                    } else {
                        RescueConnectToast.showErrorToast(requireContext(), getString(R.string.no_internet), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isAnyUpdate(User loginUser, User editedUserDetails) {
        try {
            if (loginUser != null) {
                if (!(loginUser.getFullName().equals(editedUserDetails.getFullName()))) {
                    return true;
                } else return !(loginUser.getGender().equals(editedUserDetails.getGender()));
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean validateFields() {
        try {
            if (Utils.isEmptyField(editUserName.getText().toString().trim())) {
                RescueConnectToast.showErrorToastWithBottom(requireContext(), "Please enter full name.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            } else if (Utils.isEmptyField(textGender.getText().toString().trim())) {
                RescueConnectToast.showErrorToastWithBottom(requireContext(), "Please select gender.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
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
                            Log.d(TAG, "onSuccess: loginUserUpdate: " + Utils.getLoginUserDetails(requireContext()));
                            RescueConnectToast.showSuccessToastWithBottom(requireContext(), "User details updated successfully", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);

                            updateUserDetailsToView(userMain);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            RescueConnectToast.showErrorToastWithBottom(requireContext(), "Failed to update details", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
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
                editUserName.setText(userMain.getFullName());
                textGender.setText(userMain.getGender());
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