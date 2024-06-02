package com.apps.rescueconnect.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.rescueconnect.BuildConfig;
import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.NetworkUtil;
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.helper.rescueConnectToast.RescueConnectToast;
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.ui.roledetails.MainActivityInteractor;
import com.apps.rescueconnect.ui.settings.profile.Profile;
import com.apps.rescueconnect.ui.settings.updateMpin.UpdateMPin;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment implements SettingsMainAdapter.SettingsItemClickListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private View rootView;
    private User loginUser;
    private String appVersion;

    private List<String> adminSettingsOptionList = new ArrayList<>();
    private RecyclerView recyclerAdminSettingOption;
    private SettingsMainAdapter settingsMainAdapter;
    private FragmentManager fragmentManager;
    private MainActivityInteractor mainActivityInteractor;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
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
        loginUser = Utils.getLoginUserDetails(requireContext());
        adminSettingsOptionList = Utils.getAdminSettingsOption(loginUser.getMainRole());
        fragmentManager = getParentFragmentManager();
        appVersion = "v." + BuildConfig.VERSION_NAME;
        setUpViews();
    }

    private void setUpViews() {
        try {
            recyclerAdminSettingOption = rootView.findViewById(R.id.recycler_admin_setting_option);

            LinearLayoutManager linearLayoutManager = new GridLayoutManager(requireContext(), 1);
            recyclerAdminSettingOption.setLayoutManager(linearLayoutManager);
            settingsMainAdapter = new SettingsMainAdapter(requireContext(), adminSettingsOptionList, this);
            recyclerAdminSettingOption.setAdapter(settingsMainAdapter);

            if (settingsMainAdapter != null) {
                settingsMainAdapter.notifyDataSetChanged();
            }

            if (loginUser != null) {
                if (loginUser.getMainRole() != null) {
                    switch (loginUser.getMainRole()) {
                        case AppConstants.ROLE_ADMIN: {
                            String settingAdminTitle = getString(R.string.admin_settings_title) + " [" + appVersion + "]";
                            mainActivityInteractor.setScreenTitle(settingAdminTitle);
                            break;
                        }
                        case AppConstants.ROLE_USER: {
                            String settingUserTitle = getString(R.string.user_settings_title) + " [" + appVersion + "]";
                            mainActivityInteractor.setScreenTitle(settingUserTitle);
                            break;
                        }
                        case AppConstants.ROLE_AGENCY_ADMIN: {
                            String settingPoliceTitle = getString(R.string.rescue_admin_settings_title) + " [" + appVersion + "]";
                            mainActivityInteractor.setScreenTitle(settingPoliceTitle);
                            break;
                        }
                        case AppConstants.ROLE_AGENCY_OFFICER: {
                            String settingMunicipalTitle = getString(R.string.agency_officer_settings_title) + " [" + appVersion + "]";
                            mainActivityInteractor.setScreenTitle(settingMunicipalTitle);
                            break;
                        }
                        default: {
                            String settingsTitle = getString(R.string.settings_title) + " [" + appVersion + "]";
                            mainActivityInteractor.setScreenTitle(settingsTitle);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void settingsItemClicked(int position, String item) {
        try {
            switch (item) {
                case AppConstants
                        .SETTINGS_MY_PROFILE:
                    if (checkInternet()) {
                        fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new Profile()).commit();
                    }
                    break;
                case AppConstants
                        .SETTINGS_UPDATE_MPIN:
                    if (checkInternet()) {
                        fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, new UpdateMPin()).commit();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkInternet() {
        try {
            if (NetworkUtil.getConnectivityStatus(requireContext())) {
                return true;
            } else {
                RescueConnectToast.showErrorToast(requireContext(), getString(R.string.no_internet), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}