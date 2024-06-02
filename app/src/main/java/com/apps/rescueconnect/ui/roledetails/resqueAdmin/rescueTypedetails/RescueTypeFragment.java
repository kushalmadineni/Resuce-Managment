package com.apps.rescueconnect.ui.roledetails.resqueAdmin.rescueTypedetails;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.FireBaseDatabaseConstants;
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.helper.rescueConnectToast.RescueConnectToast;
import com.apps.rescueconnect.model.RescueType;
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.ui.roledetails.MainActivityInteractor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RescueTypeFragment extends Fragment {
    private static final String TAG = RescueTypeFragment.class.getSimpleName();

    private View rootView;
    private MainActivityInteractor mainActivityInteractor;
    private EditText editAddNewRescueType;
    private ListView rescueTypeListView;
    private List<RescueType> rescueTypeList = new ArrayList<>();
    private List<String> rescueTypeNameList = new ArrayList<>();

    private ProgressDialog progressDialog;
    private Button btnAddRescueType;
    private User loginUser;

    // Firebase Storage
    FirebaseDatabase firebaseDatabase;
    private DatabaseReference mRescueTypeReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_rescue_type, container, false);
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
        mainActivityInteractor.setScreenTitle(getString(R.string.add_new_rescue_type_title));
        progressDialog = new ProgressDialog(requireContext());
        firebaseDatabase = FirebaseDatabase.getInstance();
        mRescueTypeReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.RESCUE_TYPE_TABLE);
        loginUser = Utils.getLoginUserDetails(requireContext());

        showProgressDialog("Processing please wait");

        getRescueTypeList();
    }

    private void setupView() {
        editAddNewRescueType = rootView.findViewById(R.id.edit_add_new_rescue_type);
        btnAddRescueType = rootView.findViewById(R.id.btn_add_rescue_type);
        rescueTypeListView = rootView.findViewById(R.id.rescue_type_listview);

        btnAddRescueType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideKeyboard(requireActivity());
                checkAndCreateRescueType();
            }
        });

        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, rescueTypeNameList);

        rescueTypeListView.setAdapter(itemsAdapter);
    }

    private void checkAndCreateRescueType() {
        if (TextUtils.isEmpty(editAddNewRescueType.getText().toString().trim())) {
            RescueConnectToast.showErrorToastWithBottom(requireContext(), "Please enter rescueType name", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
        } else {
            if (checkRescueTypeNotExists(editAddNewRescueType.getText().toString().trim())) {
                showProgressDialog(getString(R.string.processing_wait));
                RescueType rescueType = new RescueType();
                rescueType.setRescueName(editAddNewRescueType.getText().toString().trim());
                if (loginUser != null) {
                    if (loginUser.getMobileNumber() != null) {
                        rescueType.setCreatedBy(loginUser.getMobileNumber());
                    } else {
                        rescueType.setCreatedBy(getString(R.string.rescue_admin_text));
                    }
                } else {
                    rescueType.setCreatedBy(getString(R.string.rescue_admin_text));
                }
                rescueType.setCreatedOn(Utils.getCurrentTimeStampWithSeconds());
                createNewRescueType(editAddNewRescueType.getText().toString().trim(), rescueType);
            } else {
                RescueConnectToast.showErrorToastWithBottom(requireContext(), "RescueType already added", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            }
        }
    }

    private boolean checkRescueTypeNotExists(String rescueTypeName) {
        if (rescueTypeList.size() > 0) {
            for (RescueType rescueType : rescueTypeList) {
                if (rescueType.getRescueName().equalsIgnoreCase(rescueTypeName)) {
                    return false;
                }
            }
        } else {
            return true;
        }
        return true;
    }


    public void createNewRescueType(String rescueTypeName, RescueType rescueType) {
        try {
            mRescueTypeReference.child(rescueTypeName).setValue(rescueType)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressDialog();
                            RescueConnectToast.showSuccessToastWithBottom(requireContext(), "RescueType created successfully.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
                            editAddNewRescueType.setText("");
                            getRescueTypeList();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            RescueConnectToast.showErrorToastWithBottom(requireContext(), "Failed to create rescueType", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                        }
                    });
        } catch (Exception e) {
            hideProgressDialog();
            RescueConnectToast.showErrorToastWithBottom(requireContext(), e.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            e.printStackTrace();
        }
    }

    public void getRescueTypeList() {
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.RESCUE_TYPE_TABLE);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    hideProgressDialog();
                    rescueTypeList.clear();
                    rescueTypeNameList.clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            RescueType rescueType = postSnapshot.getValue(RescueType.class);
                            Log.d(TAG, "onDataChange: rescueType: " + rescueType);
                            if (rescueType != null) {
                                rescueTypeList.add(rescueType);
                                rescueTypeNameList.add(rescueType.getRescueName());
                            }
                        }
                    }
                    setupView();
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    hideProgressDialog();
                    setupView();
                }
            });
        } catch (Exception e) {
            hideProgressDialog();
            e.printStackTrace();
            setupView();
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