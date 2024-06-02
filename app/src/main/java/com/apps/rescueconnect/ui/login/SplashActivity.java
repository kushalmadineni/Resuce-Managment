package com.apps.rescueconnect.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.ui.roledetails.admin.AdminMainActivity;
import com.apps.rescueconnect.ui.roledetails.rescueTeamOfficer.RescueOfficerMainActivity;
import com.apps.rescueconnect.ui.roledetails.resqueAdmin.RescueAdminMainActivity;
import com.apps.rescueconnect.ui.roledetails.user.usermain.UserMainActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private static final int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        setUpView();
    }

    private void setUpView() {
        try {
            String loginToken = Utils.getSharedPrefsString(SplashActivity.this, AppConstants.LOGIN_TOKEN);
            String userRole = Utils.getSharedPrefsString(SplashActivity.this, AppConstants.USER_ROLE);

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (loginToken.isEmpty()) {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        if (userRole != null) {
                            switch (userRole) {
                                case AppConstants.ROLE_ADMIN: {
                                    startActivity(new Intent(SplashActivity.this, AdminMainActivity.class));
                                    finish();
                                    break;
                                }
                                case AppConstants.ROLE_AGENCY_ADMIN: {
                                    startActivity(new Intent(SplashActivity.this, RescueAdminMainActivity.class));
                                    finish();
                                    break;
                                }
                                case AppConstants.ROLE_AGENCY_OFFICER: {
                                    startActivity(new Intent(SplashActivity.this, RescueOfficerMainActivity.class));
                                    finish();
                                    break;
                                }
                                default: {
                                    startActivity(new Intent(SplashActivity.this, UserMainActivity.class));
                                    finish();
                                    break;
                                }
                            }
                        }else{
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        }
                    }
                }
            }, SPLASH_TIME_OUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}