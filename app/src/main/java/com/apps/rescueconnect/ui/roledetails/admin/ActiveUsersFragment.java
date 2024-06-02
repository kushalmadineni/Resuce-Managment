package com.apps.rescueconnect.ui.roledetails.admin;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.FireBaseDatabaseConstants;
import com.apps.rescueconnect.helper.NetworkUtil;
import com.apps.rescueconnect.helper.rescueConnectToast.RescueConnectToast;
import com.apps.rescueconnect.helper.encryption.AESHelper;
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

public class ActiveUsersFragment extends Fragment implements AdminActiveUserMainAdapter.ActiveUserItemClickListener {

    private static final String TAG = ActiveUsersFragment.class.getSimpleName();
    private View rootView;
    private MainActivityInteractor mainActivityInteractor;
    private ProgressDialog progressDialog;
    private TextView textNoUsersAvailable;
    private SwipeRefreshLayout swipeContainer;

    private RecyclerView recyclerActiveUser;
    private AdminActiveUserMainAdapter adminActiveUserMainAdapter;

    private List<User> allUserList = new ArrayList<>();

    public ActiveUsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_active_users, container, false);
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mainActivityInteractor = (MainActivityInteractor) requireActivity();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            mainActivityInteractor.setScreenTitle(getString(R.string.active_users_title));
            progressDialog = new ProgressDialog(requireContext());

            textNoUsersAvailable = rootView.findViewById(R.id.no_users_available);
            recyclerActiveUser = rootView.findViewById(R.id.recycler_active_user_option);

            getAllUserDetailsInList(requireContext(), false);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setUpViews() {
        try {
            if (allUserList.size() > 0) {
                recyclerActiveUser.setVisibility(View.VISIBLE);
                textNoUsersAvailable.setVisibility(View.GONE);
            } else {
                recyclerActiveUser.setVisibility(View.GONE);
                textNoUsersAvailable.setVisibility(View.VISIBLE);
            }

            LinearLayoutManager linearLayoutManager = new GridLayoutManager(requireContext(), 1);
            recyclerActiveUser.setLayoutManager(linearLayoutManager);
            adminActiveUserMainAdapter = new AdminActiveUserMainAdapter(requireContext(), allUserList, this);
            recyclerActiveUser.setAdapter(adminActiveUserMainAdapter);

            if (adminActiveUserMainAdapter != null) {
                adminActiveUserMainAdapter.notifyDataSetChanged();
            }

            swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
            // Configure the refreshing colors
            swipeContainer.setColorSchemeResources(R.color.colorAccent,
                    R.color.colorAccent,
                    R.color.colorAccent,
                    R.color.colorAccent);
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (NetworkUtil.getConnectivityStatus(requireContext())) {
                        getAllUserDetailsInList(requireContext(), true);
                    } else {
                        swipeContainer.setRefreshing(false);
                        RescueConnectToast.showErrorToast(requireContext(), getString(R.string.no_internet), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void userItemClicked(int position, User user) {
        RescueConnectToast.showInfoToast(requireContext(), user.getMainRole(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
    }

    @Override
    public void userSwitchChecked(int position, User user) {
        try {
            showDialogActiveOrInActiveUserByAdmin(requireContext(), position, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAllUserDetailsInList(Context context, boolean isFrormSwipe) {
        try {
            if (!isFrormSwipe) {
                showProgressDialog("Fetching user details, please wait");
            }

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.USERS_TABLE);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (!isFrormSwipe) {
                        hideProgressDialog();
                    }
                    if (snapshot.exists()) {
                        allUserList.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            User user = postSnapshot.getValue(User.class);
                            Log.d(TAG, "onDataChange: getUser: " + user);
                            if (user != null) {
                                if (!(user.getMainRole().equalsIgnoreCase(AppConstants.ROLE_ADMIN))) {
                                    allUserList.add(user);
                                }
                            }
                        }
                    } else {
                        RescueConnectToast.showErrorToastWithBottom(context, "Failed to fetch all user details", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                    }

                    if (isFrormSwipe) {
                        if (swipeContainer != null) {
                            swipeContainer.setRefreshing(false);
                        }

                        if (!(allUserList.size() > 0)) {
                            RescueConnectToast.showAlertToast(context, "No Users Available", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                        }
                    } else {
                        setUpViews();
                    }

                    if (adminActiveUserMainAdapter != null) {
                        adminActiveUserMainAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    if (!isFrormSwipe) {
                        hideProgressDialog();
                    }
                    RescueConnectToast.showErrorToastWithBottom(context, error.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                    if (isFrormSwipe) {
                        if (swipeContainer != null) {
                            swipeContainer.setRefreshing(false);
                        }
                    } else {
                        setUpViews();
                    }
                }
            });
        } catch (Exception e) {
            if (!isFrormSwipe) {
                hideProgressDialog();
            }
            e.printStackTrace();
            RescueConnectToast.showErrorToastWithBottom(context, e.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);

            if (isFrormSwipe) {
                if (swipeContainer != null) {
                    swipeContainer.setRefreshing(false);
                }
            } else {
                setUpViews();
            }
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

    public void showDialogActiveOrInActiveUserByAdmin(Context context, int position, User user) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alert_dialog_with_two_buttons, null);
            builder.setView(dialogView);
            builder.setCancelable(false);

            // TextView and EditText Initialization
            TextView textAlertHeader = dialogView.findViewById(R.id.dialog_message_header);
            TextView textAlertDesc = dialogView.findViewById(R.id.dialog_message_desc);

            TextView textBtnClose = dialogView.findViewById(R.id.text_button_left);
            TextView textBtnActiveOrInActive = dialogView.findViewById(R.id.text_button_right);

            textAlertHeader.setText("Message..!");
            String activeOrInActiveMessage = "Are you sure want to make ";
            String rightButtonText = AppConstants.ACTIVE_USER;

            if (user.getIsActive().equalsIgnoreCase(AppConstants.ACTIVE_USER)) {
                activeOrInActiveMessage = activeOrInActiveMessage + " InActive \n" + user.getFullName() + "?";
                rightButtonText = AppConstants.IN_ACTIVE_USER;
                textBtnActiveOrInActive.setTextColor(context.getResources().getColor(R.color.error_color, null));
                textAlertHeader.setTextColor(context.getResources().getColor(R.color.error_color, null));
            } else {
                activeOrInActiveMessage = activeOrInActiveMessage + " Active \n" + user.getFullName() + "?";
                rightButtonText = AppConstants.ACTIVE_USER;
                textBtnActiveOrInActive.setTextColor(context.getResources().getColor(R.color.success_color, null));
                textAlertHeader.setTextColor(context.getResources().getColor(R.color.success_color, null));
            }

            textAlertDesc.setText(activeOrInActiveMessage);
            textBtnClose.setText("Close");
            textBtnActiveOrInActive.setText(rightButtonText);

            AlertDialog alert = builder.create();
            alert.show();

            textBtnActiveOrInActive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (user.getIsActive().equals(AppConstants.IN_ACTIVE_USER)) {
                            user.setIsActive(AppConstants.ACTIVE_USER);
                        } else {
                            user.setIsActive(AppConstants.IN_ACTIVE_USER);
                        }
                        alert.dismiss();
                        updateUserActiveStatus(context, position, user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            textBtnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        alert.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateUserActiveStatus(Context context, int position, User user) {
        try {
            showProgressDialog("Processing your request.");

            String userMobile = AESHelper.decryptData(user.getMobileNumber());
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.USERS_TABLE);
            databaseReference.child(userMobile).setValue(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressDialog();
                            RescueConnectToast.showSuccessToastWithBottom(requireContext(), "Updated successfully", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);

                            if (adminActiveUserMainAdapter != null) {
                                adminActiveUserMainAdapter.notifyDataSetChanged();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            RescueConnectToast.showErrorToastWithBottom(requireContext(), "Failed to update", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                        }
                    });
        } catch (Exception e) {
            hideProgressDialog();
            RescueConnectToast.showErrorToastWithBottom(requireContext(), e.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            e.printStackTrace();
        }
    }

}