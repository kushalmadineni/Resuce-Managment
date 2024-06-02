package com.apps.rescueconnect.helper;

import com.apps.rescueconnect.R;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import java.util.ArrayList;
import java.util.List;

public class SliderUtils {
    private static final String TAG = SliderUtils.class.getSimpleName();
    public static final int SLIDER_TIME = 2000;

    // Admin Dashboard Slider Items
    public static List<SlideModel> getAdminDashboardSliderItemList() {
        List<SlideModel> adminSliderList = new ArrayList<>();
        adminSliderList.add(new SlideModel(R.drawable.ic_admin_slider_1, ScaleTypes.FIT));
        adminSliderList.add(new SlideModel(R.drawable.ic_admin_slider_2, ScaleTypes.FIT));
        adminSliderList.add(new SlideModel(R.drawable.ic_admin_slider_3, ScaleTypes.FIT));
        adminSliderList.add(new SlideModel(R.drawable.ic_admin_slider_4, ScaleTypes.FIT));
        adminSliderList.add(new SlideModel(R.drawable.ic_admin_slider_5, ScaleTypes.FIT));
        return adminSliderList;
    }

    // User Dashboard Slider Items
    public static List<SlideModel> getUserDashboardSliderItemList() {
        List<SlideModel> userSliderList = new ArrayList<>();
        userSliderList.add(new SlideModel(R.drawable.ic_user_slider_1, ScaleTypes.FIT));
        userSliderList.add(new SlideModel(R.drawable.ic_user_slider_2, ScaleTypes.FIT));
        userSliderList.add(new SlideModel(R.drawable.ic_user_slider_3, ScaleTypes.FIT));
        userSliderList.add(new SlideModel(R.drawable.ic_user_slider_4, ScaleTypes.FIT));
        userSliderList.add(new SlideModel(R.drawable.ic_user_slider_5, ScaleTypes.FIT));
        return userSliderList;
    }

    // AgencyOfficer Dashboard Slider Items
    public static List<SlideModel> getAgencyOfficerDashboardSliderItemList() {
        List<SlideModel> agencyOfficerSliderList = new ArrayList<>();
        agencyOfficerSliderList.add(new SlideModel(R.drawable.ic_rescue_officer_slider_1, ScaleTypes.FIT));
        agencyOfficerSliderList.add(new SlideModel(R.drawable.ic_rescue_officer_slider_2, ScaleTypes.FIT));
        agencyOfficerSliderList.add(new SlideModel(R.drawable.ic_rescue_officer_slider_3, ScaleTypes.FIT));
        agencyOfficerSliderList.add(new SlideModel(R.drawable.ic_rescue_officer_slider_4, ScaleTypes.FIT));
        agencyOfficerSliderList.add(new SlideModel(R.drawable.ic_rescue_officer_slider_5, ScaleTypes.FIT));
        agencyOfficerSliderList.add(new SlideModel(R.drawable.ic_rescue_officer_slider_6, ScaleTypes.FIT));
        return agencyOfficerSliderList;
    }


}
