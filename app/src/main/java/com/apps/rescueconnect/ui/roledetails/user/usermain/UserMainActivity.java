package com.apps.rescueconnect.ui.roledetails.user.usermain;

import android.app.Activity;
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
import com.apps.rescueconnect.ui.roledetails.user.createRescue.CreateRescue;
import com.apps.rescueconnect.ui.roledetails.user.viewRescueRequests.ViewUserRescueRequests;
import com.apps.rescueconnect.ui.settings.SettingsFragment;
import com.apps.rescueconnect.ui.settings.profile.Profile;
import com.apps.rescueconnect.ui.settings.updateMpin.UpdateMPin;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

public class UserMainActivity extends AppCompatActivity implements MainActivityInteractor {

    private static final String TAG = UserMainActivity.class.getSimpleName();
    private FragmentManager fragmentManager;
    private BottomNavigation bottomNavigationUser;
    private ImageView logout;
    private TextView textTitle;
    private User loginUser;

    public static final int SOURCE_ADDRESS_AUTO_REQUEST_CODE = 995;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);
        textTitle = findViewById(R.id.title);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new UserDashboardFragment()).commit();

        setUpViews();

        loginUser = Utils.getLoginUserDetails(UserMainActivity.this);

        verifyUserActiveStatus(loginUser);
    }

    private void setUpViews() {

        bottomNavigationUser = findViewById(R.id.bottom_navigation_user);
        logout = findViewById(R.id.logout);

        bottomNavigationUser.setMenuItemSelectionListener(new BottomNavigation.OnMenuItemSelectionListener() {
            @Override
            public void onMenuItemSelect(int id, int position, boolean b) {
                showUserBottomNavigation(position);
            }

            @Override
            public void onMenuItemReselect(int id, int position, boolean b) {
                showUserBottomNavigation(position);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialogForLogout();
            }
        });
    }

    private void showUserBottomNavigation(int position) {

        switch (position) {
            case 0:
                fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new UserDashboardFragment()).commit();
                break;
            case 1:
                if (checkInternet()) {
                    fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new CreateRescue()).commit();
                }
                break;
            case 2:
                if (checkInternet()) {
                    fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new ViewUserRescueRequests()).commit();
                }
                break;
            case 3:
                if (checkInternet()) {
                    fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new SettingsFragment()).commit();
                }
                break;
        }
    }

    private boolean checkInternet() {
        try {
            if (NetworkUtil.getConnectivityStatus(UserMainActivity.this)) {
                return true;
            } else {
                RescueConnectToast.showErrorToast(UserMainActivity.this, getString(R.string.no_internet), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public void highlightBottomNavigationTabPosition(int position) {
        if (bottomNavigationUser != null) {
            bottomNavigationUser.setSelectedIndex(position, true);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(UserMainActivity.this);
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
            Utils.removeAllDataWhenLogout(UserMainActivity.this);
            startActivity(new Intent(UserMainActivity.this, LoginActivity.class));
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            Fragment fragment = fragmentManager.findFragmentById(R.id.nav_host_fragment_content_main);
            if (fragment instanceof UserDashboardFragment) {
                moveTaskToBack(true);
            } else if (fragment instanceof CreateRescue) {
                fragmentManager.popBackStack();
                fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new UserDashboardFragment()).commit();
                highlightBottomNavigationTabPosition(0);
            } else if (fragment instanceof ViewUserRescueRequests) {
                fragmentManager.popBackStack();
                fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new UserDashboardFragment()).commit();
                highlightBottomNavigationTabPosition(0);
            } else if (fragment instanceof SettingsFragment) {
                fragmentManager.popBackStack();
                fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new UserDashboardFragment()).commit();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == Activity.RESULT_OK) {
                switch (requestCode) {
                    case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                        fragmentManager.findFragmentById(R.id.nav_host_fragment_content_main).onActivityResult(requestCode, resultCode, data);
                        break;
                    case CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE:
                        fragmentManager.findFragmentById(R.id.nav_host_fragment_content_main).onActivityResult(requestCode, resultCode, data);
                        break;
                    case SOURCE_ADDRESS_AUTO_REQUEST_CODE:
                        if (resultCode == RESULT_OK) {
                            Log.d(TAG, "onActivityResult: SOURCE_ADDRESS_AUTO_REQUEST_CODE result code ok");
                            Bundle bundle = data.getBundleExtra("LOC");
                            if (bundle != null) {
                                String dataString = bundle.getString(AppConstants.LOCATION_DATA_STREET, "");
                                Log.d(TAG, "onActivityResult: data Dtring: " + dataString);

                                double dataLatitude = bundle.getDouble(AppConstants.LATITUDE, 0.0);
                                double dataLongitude = bundle.getDouble(AppConstants.LONGITUDE, 0.0);

                                Log.d(TAG, "onActivityResult dataLatitude: " + dataLatitude);
                                Log.d(TAG, "onActivityResult dataLongitude: " + dataLongitude);


                             /*   if (editSrcAddress != null) {
                                    editSrcAddress.setText(dataString);
                                    sourceLatLong = new LatLng(dataLatitude, dataLongitude);

                                    if (!(editDestAddress.getText().toString().trim().isEmpty())) {
                                        callWalkAPIFragment(sourceLatLong, destinationLatLong);
                                    }
                                }*/
                            }

                            fragmentManager.findFragmentById(R.id.nav_host_fragment_content_main).onActivityResult(requestCode, resultCode, data);
                        } else {
                            Log.d(TAG, "onActivityResult: SOURCE_ADDRESS_AUTO_REQUEST_CODE result code not ok");
                        }
                        break;
                    default:
                        fragmentManager.findFragmentById(R.id.nav_host_fragment_content_main).onActivityResult(requestCode, resultCode, data);
                        break;
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main)).onActivityResult(requestCode, resultCode, data);
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
                                RescueConnectToast.showErrorToastWithBottom(UserMainActivity.this, "You are DeActivated now, Please contact your admin", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
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