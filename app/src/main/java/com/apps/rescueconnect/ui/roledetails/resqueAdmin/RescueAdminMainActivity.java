package com.apps.rescueconnect.ui.roledetails.resqueAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.FireBaseDatabaseConstants;
import com.apps.rescueconnect.helper.NetworkUtil;
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.helper.rescueConnectToast.RescueConnectToast;
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.ui.login.LoginActivity;
import com.apps.rescueconnect.ui.roledetails.MainActivityInteractor;
import com.apps.rescueconnect.ui.roledetails.resqueAdmin.rescueTypedetails.CreateAgencyFragment;
import com.apps.rescueconnect.ui.roledetails.resqueAdmin.rescueTypedetails.RescueTypeFragment;
import com.apps.rescueconnect.ui.settings.SettingsFragment;
import com.apps.rescueconnect.ui.settings.profile.Profile;
import com.apps.rescueconnect.ui.settings.updateMpin.UpdateMPin;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

public class RescueAdminMainActivity extends AppCompatActivity implements MainActivityInteractor {

    private static final String TAG = RescueAdminMainActivity.class.getSimpleName();
    private FragmentManager fragmentManager;
    private BottomNavigation bottomNavigationAdmin;
    private ImageView logout;
    private TextView textTitle;

    private User loginUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_rescue_admin);
        textTitle = findViewById(R.id.title);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new RescueAdminDashboardFragment()).commit();
        setUpViews();

        loginUser = Utils.getLoginUserDetails(RescueAdminMainActivity.this);

        verifyUserActiveStatus(loginUser);
    }

    private void setUpViews() {
        bottomNavigationAdmin = findViewById(R.id.bottom_navigation_admin);
        logout = findViewById(R.id.logout);

        bottomNavigationAdmin.setMenuItemSelectionListener(new BottomNavigation.OnMenuItemSelectionListener() {
            @Override
            public void onMenuItemSelect(int id, int position, boolean b) {
                showRescueAdminBottomNavigation(position);
            }

            @Override
            public void onMenuItemReselect(int id, int position, boolean b) {
                showRescueAdminBottomNavigation(position);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialogForLogout();
            }
        });
    }

    private void showRescueAdminBottomNavigation(int position) {

        switch (position) {
            case 0:
                fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new RescueAdminDashboardFragment()).commit();
                break;
            case 1:
                if (checkInternet()) {
                    fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new CreateAgencyFragment()).commit();
                }
                break;
            case 2:
                if (checkInternet()) {
                    fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new RescueTypeFragment()).commit();
                }
                break;
            case 3:
                fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new SettingsFragment()).commit();
                break;
        }
    }

    private boolean checkInternet() {
        try {
            if (NetworkUtil.getConnectivityStatus(RescueAdminMainActivity.this)) {
                return true;
            } else {
                RescueConnectToast.showErrorToast(RescueAdminMainActivity.this, getString(R.string.no_internet), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public void highlightBottomNavigationTabPosition(int position) {
        if (bottomNavigationAdmin != null) {
            bottomNavigationAdmin.setSelectedIndex(position, true);
        }
    }

    @Override
    public void setScreenTitle(String title) {
        if (textTitle != null) {
            textTitle.setText(title);
        }
    }

    public void showAlertDialogForLogout() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(RescueAdminMainActivity.this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alert_dialog_with_two_buttons, null);
            builder.setView(dialogView);
            builder.setCancelable(false);

            // TextView and EditText Initialization
            TextView textAlertHeader = dialogView.findViewById(R.id.dialog_message_header);
            TextView textAlertDesc = dialogView.findViewById(R.id.dialog_message_desc);

            TextView textBtnNo = dialogView.findViewById(R.id.text_button_left);
            TextView textBtnYes = dialogView.findViewById(R.id.text_button_right);

            textAlertHeader.setText("Alert..!");
            String logoutSureMessage = "Are you sure want to logout from app?";

            textAlertDesc.setText(logoutSureMessage);
            textBtnNo.setText("No");
            textBtnYes.setText("Yes");

            AlertDialog alert = builder.create();
            alert.show();

            textBtnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        alert.dismiss();
                        logout();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            textBtnNo.setOnClickListener(new View.OnClickListener() {
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

    public void logout() {
        try {
            Utils.removeAllDataWhenLogout(RescueAdminMainActivity.this);
            startActivity(new Intent(RescueAdminMainActivity.this, LoginActivity.class));
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            Fragment fragment = fragmentManager.findFragmentById(R.id.nav_host_fragment_content_main);
            if (fragment instanceof RescueAdminDashboardFragment) {
                moveTaskToBack(true);
            } else if (fragment instanceof ActiveUsersFragment) {
                fragmentManager.popBackStack();
                fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new RescueAdminDashboardFragment()).commit();
                highlightBottomNavigationTabPosition(0);
            } else if (fragment instanceof RescueTypeFragment) {
                fragmentManager.popBackStack();
                fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new RescueAdminDashboardFragment()).commit();
                highlightBottomNavigationTabPosition(0);
            } else if (fragment instanceof SettingsFragment) {
                fragmentManager.popBackStack();
                fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new RescueAdminDashboardFragment()).commit();
                highlightBottomNavigationTabPosition(0);
            } else if (fragment instanceof Profile) {
                fragmentManager.popBackStack();
                fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new SettingsFragment()).commit();
                highlightBottomNavigationTabPosition(3);
            } else if (fragment instanceof UpdateMPin) {
                fragmentManager.popBackStack();
                fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new SettingsFragment()).commit();
                highlightBottomNavigationTabPosition(3);
            } else {
                super.onBackPressed();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void verifyUserActiveStatus(User user) {
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.USERS_TABLE);

            databaseReference.child(user.getMobileNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String isActiveStatus = snapshot.child(FireBaseDatabaseConstants.IS_ACTIVE).getValue(String.class);
                        Log.d(TAG, "onDataChange: isActiveStatus: " + isActiveStatus);
                        if (isActiveStatus != null) {
                            if (isActiveStatus.equalsIgnoreCase(AppConstants.IN_ACTIVE_USER)) {
                                RescueConnectToast.showErrorToastWithBottom(RescueAdminMainActivity.this, "You are DeActivated now, Please contact your admin", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
                                logout();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}