package com.apps.rescueconnect.ui.roledetails.admin.citydetails;

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
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.model.citydetails.City;
import com.apps.rescueconnect.model.citydetails.Taluk;
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

public class CityFragment extends Fragment {
    private static final String TAG = CityFragment.class.getSimpleName();

    private View rootView;
    private MainActivityInteractor mainActivityInteractor;
    private EditText editAddNewCity;
    private ListView cityListView;
    private List<City> cityList = new ArrayList<>();
    private List<String> cityNameList = new ArrayList<>();

    private ProgressDialog progressDialog;
    private Button btnAddCity;
    private User loginUser;

    // Firebase Storage
    FirebaseDatabase firebaseDatabase;
    private DatabaseReference mCityReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_city, container, false);
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
        mainActivityInteractor.setScreenTitle(getString(R.string.add_new_city_title));
        progressDialog = new ProgressDialog(requireContext());
        firebaseDatabase = FirebaseDatabase.getInstance();
        mCityReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.CITY_TABLE);
        loginUser = Utils.getLoginUserDetails(requireContext());

        showProgressDialog("Processing please wait");

        getCityList();
    }

    private void setupView() {
        editAddNewCity = rootView.findViewById(R.id.edit_add_new_city);
        btnAddCity = rootView.findViewById(R.id.btn_add_city);
        cityListView = rootView.findViewById(R.id.city_listview);

        btnAddCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideKeyboard(requireActivity());
                checkAndCreateCity();
            }
        });

        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, cityNameList);

        cityListView.setAdapter(itemsAdapter);
    }

    private void checkAndCreateCity() {
        if (TextUtils.isEmpty(editAddNewCity.getText().toString().trim())) {
            RescueConnectToast.showErrorToastWithBottom(requireContext(), "Please enter city name", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
        } else {
            if (checkCityNotExists(editAddNewCity.getText().toString().trim())) {
                showProgressDialog(getString(R.string.processing_wait));
                City city = new City();
                city.setCityName(editAddNewCity.getText().toString().trim());
                if(loginUser != null){
                    if(loginUser.getMobileNumber() != null){
                        city.setCreatedBy(loginUser.getMobileNumber());
                    }else{
                        city.setCreatedBy(getString(R.string.rescue_admin_text));
                    }
                }else{
                    city.setCreatedBy(getString(R.string.rescue_admin_text));
                }
                city.setCreatedOn(Utils.getCurrentTimeStampWithSeconds());
                city.setTalukList(new ArrayList<Taluk>());
                createNewCity(editAddNewCity.getText().toString().trim(), city);
            } else {
                RescueConnectToast.showErrorToastWithBottom(requireContext(), "City already added", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            }
        }
    }

    private boolean checkCityNotExists(String cityName) {
        if (cityList.size() > 0) {
            for (City city : cityList) {
                if (city.getCityName().equalsIgnoreCase(cityName)) {
                    return false;
                }
            }
        } else {
            return true;
        }
        return true;
    }


    public void createNewCity(String cityName, City city) {
        try {
            mCityReference.child(cityName).setValue(city)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressDialog();
                            RescueConnectToast.showSuccessToastWithBottom(requireContext(), "City created successfully.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
                            editAddNewCity.setText("");
                            getCityList();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            RescueConnectToast.showErrorToastWithBottom(requireContext(), "Failed to create city", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                        }
                    });
        } catch (Exception e) {
            hideProgressDialog();
            RescueConnectToast.showErrorToastWithBottom(requireContext(), e.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            e.printStackTrace();
        }
    }

    public void getCityList() {
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.CITY_TABLE);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    hideProgressDialog();
                    cityList.clear();
                    cityNameList.clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            City city = postSnapshot.getValue(City.class);
                            Log.d(TAG, "onDataChange: city: " + city);
                            if (city != null) {
                                cityList.add(city);
                                cityNameList.add(city.getCityName());
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