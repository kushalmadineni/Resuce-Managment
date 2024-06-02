package com.apps.rescueconnect.ui.roledetails.user.createRescue;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.apps.rescueconnect.BuildConfig;
import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.FireBaseDatabaseConstants;
import com.apps.rescueconnect.helper.NetworkUtil;
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.helper.rescueConnectToast.RescueConnectToast;
import com.apps.rescueconnect.model.RescueType;
import com.apps.rescueconnect.model.User;
import com.apps.rescueconnect.model.citydetails.City;
import com.apps.rescueconnect.model.rescueRequest.RescueRequestMaster;
import com.apps.rescueconnect.model.rescueRequest.RescueRequestSubDetails;
import com.apps.rescueconnect.ui.placeselection.AddressPlaceActivity;
import com.apps.rescueconnect.ui.roledetails.MainActivityInteractor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jakewharton.rxbinding.view.RxView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import rx.functions.Action1;

public class CreateRescue extends Fragment {
    private static final String TAG = CreateRescue.class.getSimpleName();
    private View rootView;
    private CoordinatorLayout rescueCoordinator;

    public static final int SOURCE_ADDRESS_AUTO_REQUEST_CODE = 995;

    private TextView textCity, textRescueAgencies, textRescueType, textRescueTypeHeader;
    private EditText editRescueTitle, editRescueDesc;
    private ImageView imageSelectedPhoto, imageCameraIcon;
    private Button btnSubmit;
    private ProgressDialog progressDialog;
    private long MAX_2_MB = 2000000;
    private String rescueType = "";
    private String rescueAccessType = AppConstants.CALAMITY_ACCESS_TYPE_PRIVATE;

    private LatLng selectedLatlng;
    private String selectedAddress;
    private TextView textSelectLocation, textSelectedAddress, textSelectedLatLong;

    private PlacesClient placesClient;
    private AutocompleteSessionToken autocompleteSessionToken;

    private StringBuilder mResult;

    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE};

    final private int MULTIPLE_PERMISSIONS = 124;


    private static final int GALLERY_REQUEST_CODE = 111;
    private static final int CAMERA_REQUEST_CODE = 222;
    private Uri cropImageUri, photoUploadUri;
    private String cameraFilePath;

    // Firebase Storage
    FirebaseDatabase firebaseDatabase;
    private DatabaseReference mUserReferenceRescue;

    private StorageReference storageReference;

    private List<City> cityList = new ArrayList<>();
    private List<String> cityStringList = new ArrayList<>();

    private List<RescueType> rescueTypeList = new ArrayList<>();
    private List<String> rescueTypeStringList = new ArrayList<>();

    private List<User> mainUserList = new ArrayList<>();

    private RadioGroup radioRescueAccessType;
    private MainActivityInteractor mainActivityInteractor;

    public CreateRescue() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for requireContext() fragment
        rootView = inflater.inflate(R.layout.fragment_create_rescue_request, container, false);
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
            mainActivityInteractor.setScreenTitle(getString(R.string.create_rescue_request));

            progressDialog = new ProgressDialog(requireContext());

            firebaseDatabase = FirebaseDatabase.getInstance();
            mUserReferenceRescue = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.RESQUE_REQUEST_LIST_TABLE);
            storageReference = FirebaseStorage.getInstance().getReference();

            getCityList();

            getUserList();

            getRescueTypeList();

            checkPermissions();

            setUpViews();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpViews() {
        try {
            rescueCoordinator = rootView.findViewById(R.id.create_rescue_coordinate);
            textCity = rootView.findViewById(R.id.text_city);
            textRescueType = rootView.findViewById(R.id.text_rescue_type);
            textRescueAgencies = rootView.findViewById(R.id.text_rescue_agencies);

            textSelectLocation = rootView.findViewById(R.id.text_select_location_header);
            textSelectedAddress = rootView.findViewById(R.id.text_location);
            textSelectedLatLong = rootView.findViewById(R.id.text_location_latlong);

            editRescueTitle = rootView.findViewById(R.id.edit_rescue);
            editRescueDesc = rootView.findViewById(R.id.edit_rescue_desc);

            radioRescueAccessType = rootView.findViewById(R.id.radio_rescue_access_type);

            textRescueTypeHeader = rootView.findViewById(R.id.text_rescue_access_type_header);

            imageSelectedPhoto = rootView.findViewById(R.id.place_image);
            imageCameraIcon = rootView.findViewById(R.id.image_camera_icon);
            btnSubmit = rootView.findViewById(R.id.btn_submit);

            RxView.touches(textSelectLocation).subscribe(motionEvent -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    Intent intent = new Intent(getContext(), AddressPlaceActivity.class);
                    startActivityForResult(intent, SOURCE_ADDRESS_AUTO_REQUEST_CODE);
                }
            });

            User loginUser = Utils.getLoginUserDetails(requireContext());

            if (loginUser != null) {
                textCity.setText(loginUser.getUserCity());
            } else {
                RxView.touches(textCity).subscribe(motionEvent -> {
                    try {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            AlertDialog.Builder builderSingle = new AlertDialog.Builder(requireContext());
                            builderSingle.setTitle("Select City");

                            final ArrayAdapter<String> citySelectionAdapter = new ArrayAdapter<String>(requireContext(),
                                    android.R.layout.select_dialog_singlechoice, cityStringList) {
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

                            builderSingle.setAdapter(citySelectionAdapter, (dialog, position) -> {
                                textCity.setText(citySelectionAdapter.getItem(position));
                            });
                            builderSingle.show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            RxView.touches(textRescueAgencies).subscribe(motionEvent -> {
                try {
                    String cityName = textCity.getText().toString().trim();
                    if (!cityName.isEmpty()) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            List<String> cityWiseAgenciesList = getCityWiseAgenciesList(cityName);
                            AlertDialog.Builder builderSingle = new AlertDialog.Builder(requireContext());
                            builderSingle.setTitle("Select Agencies");

                            final ArrayAdapter<String> rescueAgenciesSelectionAdapter = new ArrayAdapter<String>(requireContext(),
                                    android.R.layout.select_dialog_singlechoice, cityWiseAgenciesList) {
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

                            builderSingle.setAdapter(rescueAgenciesSelectionAdapter, (dialog, position) -> {
                                textRescueAgencies.setText(rescueAgenciesSelectionAdapter.getItem(position));
                            });
                            builderSingle.show();
                        }
                    } else {
                        RescueConnectToast.showErrorToast(requireContext(), "Please select City", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            RxView.touches(textRescueType).subscribe(motionEvent -> {
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(requireContext());
                        builderSingle.setTitle("Select Rescue Type");

                        final ArrayAdapter<String> accessTypeSelectionAdapter = new ArrayAdapter<String>(requireContext(),
                                android.R.layout.select_dialog_singlechoice, rescueTypeStringList) {
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

                        builderSingle.setAdapter(accessTypeSelectionAdapter, (dialog, position) -> {
                            textRescueType.setText(accessTypeSelectionAdapter.getItem(position));
                        });
                        builderSingle.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            radioRescueAccessType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int id) {
                    if (id != 0) {
                        if (id == R.id.radio_rescue_access_private) {
                            rescueAccessType = AppConstants.CALAMITY_ACCESS_TYPE_PRIVATE;
                        } else if (id == R.id.radio_rescue_access_public) {
                            rescueAccessType = AppConstants.CALAMITY_ACCESS_TYPE_PUBLIC;
                        }
                    }
                }
            });

            imageSelectedPhoto.setImageDrawable(ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.empty_image, null));

            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (NetworkUtil.getConnectivityStatus(requireContext())) {
                        if (validateFields()) {
                            if (radioRescueAccessType.getCheckedRadioButtonId() == R.id.radio_rescue_access_public) {
                                submitRescueRequest(AppConstants.CALAMITY_ACCESS_TYPE_PUBLIC);
                            } else {
                                submitRescueRequest(AppConstants.CALAMITY_ACCESS_TYPE_PRIVATE);
                            }
                        }
                    } else {
                        RescueConnectToast.showErrorToast(requireContext(), getString(R.string.no_internet), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                    }
                }
            });

            RxView.clicks(imageCameraIcon).subscribe(new Action1<Void>() {
                @Override
                public void call(Void unused) {
                    try {
                        showPicChooser();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> getCityWiseAgenciesList(String cityName) {
        List<String> agenciesName = new ArrayList<>();
        try {
            if (mainUserList != null && mainUserList.size() > 0) {
                for (User user : mainUserList) {
                    if (user != null && user.getUserCity().equalsIgnoreCase(cityName)) {
                        agenciesName.add(user.getFullName() + " : ("+user.getUserAgency()+")");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agenciesName;
    }

    private String getAgenciesOfficerName(String selectedAgencies) {
        try {
            if (selectedAgencies != null) {
                String[] spittedName = selectedAgencies.split(" : ");
                if(spittedName.length > 1){
                    return spittedName[0];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return selectedAgencies;
    }

    private void submitRescueRequest(String rescueAccessType) {
        try {
            User loginUser = Utils.getLoginUserDetails(requireContext());

            String userId = loginUser.getMobileNumber();
            String userName = loginUser.getFullName();
            String role = loginUser.getMainRole();
            String selectedCity = textCity.getText().toString().trim();
            String selectedAgencyName = getAgenciesOfficerName(textRescueAgencies.getText().toString().trim());

            String mainRescueId = Utils.getCurrentTimeStampWithSeconds();

            RescueRequestMaster rescueRequestMaster = new RescueRequestMaster();
            rescueRequestMaster.setRescueRequestCity(selectedCity);
            rescueRequestMaster.setRescueRequestType(textRescueType.getText().toString().trim());
            rescueRequestMaster.setRescueRequestAssignAgencies(selectedAgencyName.trim());
            rescueRequestMaster.setRescueRequestAccessType(rescueAccessType);
            rescueRequestMaster.setRescueRequestHeader(editRescueTitle.getText().toString().trim());
            rescueRequestMaster.setRescueRequestDescription(editRescueDesc.getText().toString().trim());
            rescueRequestMaster.setRescueRequestAcceptedOfficerId(userId);
            rescueRequestMaster.setRescueRequestAcceptedOfficerName(userName);
            rescueRequestMaster.setRescueRequestPlacePhotoId(Utils.getCurrentTimeStampWithSecondsAsId());
            rescueRequestMaster.setRescueRequestPlacePhotoUploadedDate(Utils.getCurrentTimeStampWithSeconds());
            // Initial PhotoPath is Empty
            rescueRequestMaster.setRescueRequestPlacePhotoPath("");
            rescueRequestMaster.setRescueRequestCreatedOn(mainRescueId);
            rescueRequestMaster.setRescueRequestPlaceLatitude(selectedLatlng.latitude);
            rescueRequestMaster.setRescueRequestPlaceLongitude(selectedLatlng.longitude);
            rescueRequestMaster.setRescueRequestPlaceAddress(textSelectedAddress.getText().toString().trim());

            List<RescueRequestSubDetails> rescueRequestSubDetailsList = new ArrayList<>();
            RescueRequestSubDetails rescueRequestSubDetails = new RescueRequestSubDetails();
            rescueRequestSubDetails.setRescueRequestId(mainRescueId);
            rescueRequestSubDetails.setRescueRequestAcceptedId(userId);
            rescueRequestSubDetails.setRescueRequestStatus(AppConstants.NEW_STATUS);
            rescueRequestSubDetails.setRescueRequestModifiedBy(userId);
            rescueRequestSubDetails.setRescueRequestModifiedOn(Utils.getCurrentTimeStampWithSeconds());
            rescueRequestSubDetails.setRescueRequestAcceptedRole(role);

            rescueRequestSubDetailsList.add(rescueRequestSubDetails);
            rescueRequestMaster.setRescueRequestSubDetailsList(rescueRequestSubDetailsList);

            long photoSize = getFileSize(photoUploadUri);

            Log.d(TAG, "onClick: photoSize:" + photoSize);
            if (photoSize > MAX_2_MB) {
                int scaleDivider = 4;

                try {
                    // 1. Convert uri to bitmap
                    Bitmap fullBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), photoUploadUri);

                    // 2. Get the downsized image content as a byte[]
                    int scaleWidth = fullBitmap.getWidth() / scaleDivider;
                    int scaleHeight = fullBitmap.getHeight() / scaleDivider;
                    byte[] downsizedImageBytes =
                            getDownsizedImageBytes(fullBitmap, scaleWidth, scaleHeight);

                    if (downsizedImageBytes != null) {
                        Log.d(TAG, "onClick: mnRescueMaster down: " + rescueRequestMaster);
                        Log.d(TAG, "onClick: mnRescueMaster down: " + downsizedImageBytes);
                        upLoadPlacePhotoMoreSizeRescueRequest(rescueRequestMaster, downsizedImageBytes, userId);
                    } else {
                        RescueConnectToast.showErrorToast(requireContext(), "Failed to reduce photo size, try again.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                    }
                } catch (IOException ioEx) {
                    ioEx.printStackTrace();
                }
            } else {
                Log.d(TAG, "onClick: mnRescueMaster: " + rescueRequestMaster);
                Log.d(TAG, "onClick: photoUploadUri: " + photoUploadUri);
                upLoadPlacePhotoOfRescueRequest(rescueRequestMaster, photoUploadUri, userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        try {
            if (textCity.getText().toString().trim().isEmpty()) {
                RescueConnectToast.showErrorToast(requireContext(), "Please select city", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            } else if (textRescueType.getText().toString().trim().isEmpty()) {
                RescueConnectToast.showErrorToast(requireContext(), "Please select rescue type", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            } else if (textRescueAgencies.getText().toString().trim().isEmpty()) {
                RescueConnectToast.showErrorToast(requireContext(), "Please select rescue agencies", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            } else if ((!(radioRescueAccessType.getCheckedRadioButtonId() == R.id.radio_rescue_access_private || radioRescueAccessType.getCheckedRadioButtonId() == R.id.radio_rescue_access_public))) {
                RescueConnectToast.showErrorToast(requireContext(), "Please select Rescue Access Type", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            } else if (editRescueTitle.getText().toString().trim().isEmpty()) {
                RescueConnectToast.showErrorToast(requireContext(), "Please enter rescue request title", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            } else if (editRescueDesc.getText().toString().trim().isEmpty()) {
                RescueConnectToast.showErrorToast(requireContext(), "Please enter rescue request description", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            } else if (textSelectedAddress.getText().toString().trim().isEmpty()) {
                RescueConnectToast.showErrorToast(requireContext(), "Please select place location", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            } else if (selectedLatlng == null) {
                RescueConnectToast.showErrorToast(requireContext(), "Please select place location", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            } else if (photoUploadUri == null) {
                RescueConnectToast.showErrorToast(requireContext(), "Please select photo", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            RescueConnectToast.showErrorToast(requireContext(), "Exception occurred, please try again.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            return false;
        }
    }

    public void getCityList() {
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.CITY_TABLE);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        cityList.clear();
                        cityStringList.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            City city = postSnapshot.getValue(City.class);
                            if (city != null) {
                                cityList.add(city);
                                cityStringList.add(city.getCityName());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: error: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getRescueTypeList() {
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.RESCUE_TYPE_TABLE);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        rescueTypeList.clear();
                        rescueTypeStringList.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            RescueType rescueType = postSnapshot.getValue(RescueType.class);
                            if (rescueType != null) {
                                rescueTypeList.add(rescueType);
                                rescueTypeStringList.add(rescueType.getRescueName());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: error: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getUserList() {
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FireBaseDatabaseConstants.USERS_TABLE);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        mainUserList.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            User user = postSnapshot.getValue(User.class);
                            if (user != null) {
                                if (user.getMainRole().equalsIgnoreCase(AppConstants.ROLE_AGENCY_OFFICER)) {
                                    mainUserList.add(user);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: error: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void upLoadPlacePhotoOfRescueRequest(RescueRequestMaster rescueRequestMaster, Uri photoUploadUri, String userId) {
        try {
            showProgressDialog("Processing your request..");

            String photoExt = rescueRequestMaster.getRescueRequestCity() + "_" + userId + "_" + rescueRequestMaster.getRescueRequestPlacePhotoId() + "." + getFileExtension(photoUploadUri);
            StorageReference fileRef = storageReference.child(photoExt);
            fileRef.putFile(photoUploadUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d(TAG, "onSuccess: uri: " + uri);
                            rescueRequestMaster.setRescueRequestPlacePhotoPath(uri.toString());
                            hideProgressDialog();
                            submitPhotoRescueRequestDetails(rescueRequestMaster, userId);
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    hideProgressDialog();
                    RescueConnectToast.showErrorToast(requireContext(), "Failed to upload photo", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            hideProgressDialog();
            RescueConnectToast.showErrorToast(requireContext(), e.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            e.printStackTrace();
        }
    }

    private void upLoadPlacePhotoMoreSizeRescueRequest(RescueRequestMaster rescueRequestMaster, byte[] downsizedImageBytes, String userId) {
        try {
            showProgressDialog("Processing your request..");
            String photoExt = rescueRequestMaster.getRescueRequestCity() + "_" + userId + "_" + rescueRequestMaster.getRescueRequestPlacePhotoId() + "." + getFileExtension(photoUploadUri);
            StorageReference fileRef = storageReference.child(photoExt);
            fileRef.putBytes(downsizedImageBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d(TAG, "onSuccess: 2: " + uri);
                            rescueRequestMaster.setRescueRequestPlacePhotoPath(uri.toString());
                            hideProgressDialog();
                            submitPhotoRescueRequestDetails(rescueRequestMaster, userId);
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull @NotNull UploadTask.TaskSnapshot snapshot) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    hideProgressDialog();
                    RescueConnectToast.showErrorToast(requireContext(), "Failed to upload photo", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            hideProgressDialog();
            RescueConnectToast.showErrorToast(requireContext(), e.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            e.printStackTrace();
        }
    }

    public byte[] getDownsizedImageBytes(Bitmap fullBitmap, int scaleWidth, int scaleHeight) throws IOException {
        try {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(fullBitmap, scaleWidth, scaleHeight, true);

            // 2. Instantiate the downsized image content as a byte[]
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void submitPhotoRescueRequestDetails(RescueRequestMaster rescueRequestMaster, String userId) {
        try {
            showProgressDialog("Submitting please wait.");

            mUserReferenceRescue.child(rescueRequestMaster.getRescueRequestCity()).child(userId).child(rescueRequestMaster.getRescueRequestPlacePhotoId()).setValue(rescueRequestMaster)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressDialog();
                            RescueConnectToast.showSuccessToast(requireContext(), "Rescue request submitted successfully.", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_LONG);
                            clearAllFields();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            RescueConnectToast.showErrorToast(requireContext(), "Failed to submit rescue request", RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
                        }
                    });
        } catch (Exception e) {
            hideProgressDialog();
            RescueConnectToast.showErrorToast(requireContext(), e.getMessage(), RescueConnectToast.RESCUE_CONNECT_TOAST_LENGTH_SHORT);
            e.printStackTrace();
        }
    }

    private void clearAllFields() {
        try {
            radioRescueAccessType.clearCheck();
            photoUploadUri = null;
            cropImageUri = null;
            selectedLatlng = null;
            selectedAddress = null;
            textSelectedAddress.setText("");
            textSelectedLatLong.setText("");
            editRescueTitle.setText("");
            textCity.setText("");
            textRescueType.setText("");
            textRescueAgencies.setText("");
            editRescueDesc.setText("");
            imageSelectedPhoto.setImageURI(null);
            imageSelectedPhoto.setImageDrawable(ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.empty_image, null));

            radioRescueAccessType.setVisibility(View.GONE);
            textRescueTypeHeader.setVisibility(View.GONE);
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

    private String getFileExtension(Uri profilePicUri) {
        try {
            ContentResolver contentResolver = requireActivity().getContentResolver();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

            if (mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(profilePicUri)) != null) {
                return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(profilePicUri));
            } else {
                return "jpg";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "jpg";
        }
    }

    private long getFileSize(Uri profilePicUri) {
        long fileSize = 0;
        ContentResolver contentResolver = requireActivity().getContentResolver();
        AssetFileDescriptor afd = null;
        try {
            afd = contentResolver.openAssetFileDescriptor(profilePicUri, "r");
            fileSize = afd.getLength();
            afd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileSize;
    }

    private void showPicChooser() {
        try {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());// , android.R.style.Theme_Translucent
            View dialogView = getLayoutInflater().inflate(R.layout.alert_dialog_with_centered_icon_profile_pic_chooser, null);

            ImageView imageCamera = dialogView.findViewById(R.id.image_camera);
            TextView textCamera = dialogView.findViewById(R.id.text_camera);

            ImageView imageGallery = dialogView.findViewById(R.id.image_gallery);
            TextView textGallery = dialogView.findViewById(R.id.text_gallery);

            TextView textCancel = dialogView.findViewById(R.id.text_cancel_button);

            builder.setCancelable(false);
            builder.setView(dialogView);
            final android.app.AlertDialog dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

            // Camera Option Clicked
            imageCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    captureImageFromCamera();
                }
            });

            textCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    captureImageFromCamera();
                }
            });

            // GalleryOptionClick
            imageGallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    chooseImageFromGallery();
                }
            });

            textGallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    chooseImageFromGallery();
                }
            });

            //Cancel Button Click
            textCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void captureImageFromCamera() {
        try {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", createImageFile()));
                    startActivityForResult(intent, CAMERA_REQUEST_CODE);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                requestStoragePermission();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private File createImageFile() throws IOException {
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            //This is the directory in which the file will be created. This is the default location of Camera photos
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM), "Camera");
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            cameraFilePath = image.getAbsolutePath();
            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void chooseImageFromGallery() {
        try {
            //Create an Intent with action as ACTION_PICK
            Intent intent = new Intent(Intent.ACTION_PICK);
            // Sets the type as image/*. This ensures only components of type image are selected
            intent.setType("image/*");
            //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
            String[] mimeTypes = {"image/jpeg", "image/png"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            // Launching the Intent
            startActivityForResult(intent, GALLERY_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // region Permission requests
    private void requestStoragePermission() {
        try {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    ||
                    ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Display a SnackBar with an explanation and a button to trigger the request.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Snackbar.make(rescueCoordinator, requireActivity().getResources().getString(R.string.permission_mandatory_alert),
                                    Snackbar.LENGTH_INDEFINITE)
                            .setAction("Allow", view -> requestPermissions(AppConstants.PERMISSIONS_STORAGE, AppConstants.PERMISSION_REQUEST_STORAGE))
                            .show();
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(AppConstants.PERMISSIONS_STORAGE, AppConstants.PERMISSION_REQUEST_STORAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Log.d(TAG, "onActivityResult: requestCode:"+requestCode);
            switch (requestCode) {
                case CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE:
                    if (resultCode == RESULT_OK) {
                        try {
                            Uri imageUri = CropImage.getPickImageResultUri(requireContext(), data);

                            // For API >= 23 we need to check specifically that we have permissions to read external storage.
                            if (CropImage.isReadExternalStoragePermissionsRequired(requireContext(), imageUri)) {
                                // request permissions and handle the result in onRequestPermissionsResult()
                                cropImageUri = imageUri;

                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
                            } else {
                                // no permissions required or already grunted, can start crop image activity
                                cropImage(imageUri);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {
                        photoUploadUri = result.getUri();
                        try {
                            Bitmap selectedImage = decodeBitmapUri(requireActivity(), photoUploadUri);
                            imageSelectedPhoto.setImageBitmap(selectedImage);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Log.d(TAG, "onActivityResult: Error in cropping");
                    }
                    break;

                case GALLERY_REQUEST_CODE:
                    //data.getData return the content URI for the selected Image
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        if (selectedImage != null) {
                            cropImageUri = selectedImage;
                            CropImage.activity(selectedImage)
                                    .start(requireActivity());
                        } else {
                            Toast.makeText(requireContext(), "Failed to load image from gallery.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;

                case CAMERA_REQUEST_CODE:
                    File imgFileCamera = new File(cameraFilePath);
                    if (imgFileCamera.exists()) {
                        cropImageUri = Uri.fromFile(imgFileCamera);
                        CropImage.activity(cropImageUri)
                                .start(requireActivity());
                    }
                    break;

                case SOURCE_ADDRESS_AUTO_REQUEST_CODE: {
                    if (resultCode == RESULT_OK) {
                        try {
                            Log.d(TAG, "onActivityResult: local result code ok");
                            Bundle bundle = data.getBundleExtra("LOC");
                            if (bundle != null) {
                                String address = bundle.getString(AppConstants.LOCATION_DATA_STREET, "");
                                Log.d(TAG, "onActivityResult: local data String: " + address);

                                double dataLatitude = bundle.getDouble(AppConstants.LATITUDE, 0.0);
                                double dataLongitude = bundle.getDouble(AppConstants.LONGITUDE, 0.0);

                                Log.d(TAG, "onActivityResult local dataLatitude: " + dataLatitude);
                                Log.d(TAG, "onActivityResult local dataLongitude: " + dataLongitude);
                                if (textSelectedAddress != null) {
                                    selectedAddress = address;
                                    textSelectedAddress.setText(address);
                                    selectedLatlng = new LatLng(dataLatitude, dataLongitude);
                                    String latLongText = "Latitude: " + dataLatitude + ", Longitude: " + dataLongitude;
                                    textSelectedLatLong.setText(latLongText);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "onActivityResult: SOURCE_ADDRESS_AUTO_REQUEST_CODE result code not ok");
                    }
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        try {
            int targetW = 300;
            int targetH = 300;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            return BitmapFactory.decodeStream(ctx.getContentResolver()
                    .openInputStream(uri), null, bmOptions);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            Log.d(TAG, "onRequestPermissionsResult: Request Code: " + requestCode);

            if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CropImage.startPickImageActivity(requireActivity());
                } else {
                    Toast.makeText(requireContext(), requireActivity().getResources().getString(R.string.canceling_permission_not_granted), Toast.LENGTH_LONG).show();
                }
            }
            if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
                if (cropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // required permissions granted, start crop image activity
                    cropImage(cropImageUri);
                } else {
                    Toast.makeText(requireContext(), requireActivity().getResources().getString(R.string.canceling_permission_not_granted), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cropImage(Uri imageUri) {
        try {
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(requireContext(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkPermissions() {
        try {
            int result;
            List<String> listPermissionsNeeded = new ArrayList<>();
            for (String p : permissions) {
                result = ContextCompat.checkSelfPermission(requireContext(), p);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p);
                }
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(requireActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}