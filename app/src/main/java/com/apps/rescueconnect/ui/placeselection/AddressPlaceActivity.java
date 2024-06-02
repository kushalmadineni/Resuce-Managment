package com.apps.rescueconnect.ui.placeselection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;

import com.apps.rescueconnect.R;
import com.apps.rescueconnect.helper.AppConstants;
import com.apps.rescueconnect.helper.ServerConstants;
import com.apps.rescueconnect.helper.Utils;
import com.apps.rescueconnect.service.FetchAddressIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.rxbinding.view.RxView;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class AddressPlaceActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    private static final String TAG = "AddressPlaceActivity";

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 999;
    private final int LOCATION_SETTINGS_CODE = 111;
    private CoordinatorLayout locationMapCoordinatorLayout;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private TextView locationText, locationText2, locationMarkerText;
    private LatLng latLng, centerLatLong, sourceLatLng;
    private boolean isSourceSet;
    private String addressOutput, streetOutput, areaOutput, cityOutput, stateOutput, countryOutput, postalCodeOutput;

    private Location alreadySelectedLocation;
    private Bundle bundle;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private MaterialSearchBar materialSearchBar;
    private List<AutocompletePrediction> predictionList;

    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    private final float DEFAULT_ZOOM = 15;
    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private AddressResultReceiver mResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_place);
        try {
            Context mContext = this;
            materialSearchBar = findViewById(R.id.searchBar);
            buildGoogleApiClient();

            locationRequest = new LocationRequest();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            locationMapCoordinatorLayout = findViewById(R.id.address_cooradinator);

            locationMarkerText = findViewById(R.id.text_location_marker);
            locationText = findViewById(R.id.text_address);
            locationText2 = findViewById(R.id.text_address_2);

            locationMarkerText.setText("Select Location");

            if (checkPlayServices()) {
                // If this check succeeds, proceed with normal processing.
                // Otherwise, prompt user to get valid Play Services APK.
                if (Utils.checkLocationPermission(AddressPlaceActivity.this)) {
                    if (Utils.isLocationEnabled(mContext)) {
                        Log.d(TAG, "onCreate: GPS enabled");
                        initializeMap();
                    } else {
                        Log.d(TAG, "onCreate: GPS disabled");
                        // Notify user to enable GPS
                        showGPSDisabledAlertToUser();
                    }
                } else {
                    requestPermission();
                }
            } else {
                Toast.makeText(mContext, "Location not supported in this device", Toast.LENGTH_SHORT).show();
            }
            MapsInitializer.initialize(getApplicationContext());
            mResultReceiver = new AddressResultReceiver(new Handler());
            initGooglePlacesApi();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initGooglePlacesApi() {
        try {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(AddressPlaceActivity.this);
            Places.initialize(AddressPlaceActivity.this, ServerConstants.getDecodedGMPApiKey());
            placesClient = Places.createClient(this);
            final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

            materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                @Override
                public void onSearchStateChanged(boolean enabled) {

                }

                @Override
                public void onSearchConfirmed(CharSequence text) {
                    startSearch(text.toString(), true, null, true);
                }

                @Override
                public void onButtonClicked(int buttonCode) {
                    if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                        //opening or closing a navigation drawer
                    } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                        materialSearchBar.disableSearch();
                    }
                }
            });

            materialSearchBar.addTextChangeListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                            .setCountry(ServerConstants.COUNTRY_SELECTED)
                            .setTypeFilter(TypeFilter.ADDRESS)
                            .setSessionToken(token)
                            .setQuery(s.toString())
                            .build();
                    placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                            if (task.isSuccessful()) {
                                FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                                if (predictionsResponse != null) {
                                    predictionList = predictionsResponse.getAutocompletePredictions();
                                    List<String> suggestionsList = new ArrayList<>();
                                    for (int i = 0; i < predictionList.size(); i++) {
                                        AutocompletePrediction prediction = predictionList.get(i);
                                        suggestionsList.add(prediction.getFullText(null).toString());
                                    }
                                    materialSearchBar.updateLastSuggestions(suggestionsList);
                                    if (!materialSearchBar.isSuggestionsVisible()) {
                                        materialSearchBar.showSuggestionsList();
                                    }
                                }
                            } else {
                                Log.i("mytag", "prediction fetching task unsuccessful");
                            }
                        }
                    });
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            materialSearchBar.setSuggstionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
                @Override
                public void OnItemClickListener(int position, View v) {
                    if (position >= predictionList.size()) {
                        return;
                    }
                    AutocompletePrediction selectedPrediction = predictionList.get(position);
                    String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                    materialSearchBar.setText(suggestion);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            materialSearchBar.clearSuggestions();
                        }
                    }, 1000);
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null)
                        imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                    final String placeId = selectedPrediction.getPlaceId();
                    List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

                    FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                    placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                        @Override
                        public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                            Place place = fetchPlaceResponse.getPlace();
                            Log.i("mytag", "Place found: " + place.getName());
                            LatLng latLngOfPlace = place.getLatLng();
                            if (latLngOfPlace != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM));
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (e instanceof ApiException) {
                                ApiException apiException = (ApiException) e;
                                apiException.printStackTrace();
                                int statusCode = apiException.getStatusCode();
                                Log.i("mytag", "place not found: " + e.getMessage());
                                Log.i("mytag", "status code: " + statusCode);
                            }
                        }
                    });
                }

                @Override
                public void OnItemDeleteListener(int position, View v) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            Objects.requireNonNull(AddressPlaceActivity.this.getSupportActionBar()).hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if(AddressPlaceActivity.this.getSupportActionBar() != null){
                AddressPlaceActivity.this.getSupportActionBar().show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            googleApiClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        try {
            if (googleApiClient != null && googleApiClient.isConnected()) {
                googleApiClient.disconnect();
            }
//            Objects.requireNonNull(AddressPlaceActivity.this.getSupportActionBar()).show();

            if(AddressPlaceActivity.this.getSupportActionBar() != null){
                AddressPlaceActivity.this.getSupportActionBar().show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            super.onStop();
        }
    }

    private void initializeMap() {
        try {
            mapFragment.getMapAsync(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Manipulates the map once available. This callback is triggered when the map is ready to be
     * used. This is where we can add markers or lines, add listeners or move the camera. In this
     * case, we just add a marker near Sydney, Australia. If Google Play services is not installed
     * on the device, the user will be prompted to install it inside the SupportMapFragment. This
     * method will only be triggered once the user has installed Google Play services and returned
     * to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            map = googleMap;

            map.setOnCameraChangeListener(cameraPosition -> {
                centerLatLong = cameraPosition.target;

                // Don't know why I commented this.
//            map.clear();

                if (alreadySelectedLocation != null) {
                    try {
                        alreadySelectedLocation.setLatitude(centerLatLong.latitude);
                        alreadySelectedLocation.setLongitude(centerLatLong.longitude);

                        startIntentService(alreadySelectedLocation);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Location mLocation = new Location("");
                        mLocation.setLatitude(centerLatLong.latitude);
                        mLocation.setLongitude(centerLatLong.longitude);

                        startIntentService(mLocation);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });

            if (isSourceSet) {
                if (map != null) {
                    map.addMarker(new MarkerOptions()
                            .position(sourceLatLng)
                            .title("Pick up source")
                            .snippet("Pick up source")
                            .icon(Utils.getBitmapDescriptor(AddressPlaceActivity.this, R.drawable.ic_pin_drop_pickup)));
                }
            }

            if (map != null) {
                map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        return false;
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            if (alreadySelectedLocation != null) {
                changeMap(alreadySelectedLocation);
            } else {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (mLastLocation != null) {
                    changeMap(mLastLocation);
                } else {
                    try {
                        LocationServices.FusedLocationApi.removeLocationUpdates(
                                googleApiClient, this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        try {
            Log.i(TAG, "Connection suspended");
            googleApiClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            if (alreadySelectedLocation != null) {
                changeMap(alreadySelectedLocation);
            } else {
                try {
                    if (location != null) {
                        changeMap(location);
                    }
                    LocationServices.FusedLocationApi.removeLocationUpdates(
                            googleApiClient, this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    protected synchronized void buildGoogleApiClient() {
        try {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkPlayServices() {
        try {
            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            int resultCode = googleAPI.isGooglePlayServicesAvailable(this);
            if (resultCode != ConnectionResult.SUCCESS) {
                if (googleAPI.isUserResolvableError(resultCode)) {
                    googleAPI.getErrorDialog(this, resultCode,
                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
                } else {
                    finish();
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void changeMap(Location location) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            // check if map is created successfully or not
            if (map != null) {
                LatLng latLong;

                latLong = new LatLng(location.getLatitude(), location.getLongitude());

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLong).zoom(18).tilt(45).build();

                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.getUiSettings().setZoomControlsEnabled(true);
                map.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));

                startIntentService(location);

                RxView.clicks(locationMarkerText).subscribe(view -> {
                    if (latLng != null) {
                        Intent returnIntent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString(AppConstants.LOCATION_DATA_STREET, streetOutput);

                        // Un Used Values For Future Purpose
                        bundle.putString(AppConstants.RESULT_DATA_KEY, addressOutput);
                        bundle.putString(AppConstants.LOCATION_DATA_AREA, areaOutput);
                        bundle.putString(AppConstants.LOCATION_DATA_CITY, cityOutput);
                        bundle.putString(AppConstants.LOCATION_DATA_STATE, stateOutput);
                        bundle.putString(AppConstants.LOCATION_DATA_COUNTRY, countryOutput);
                        bundle.putString(AppConstants.LOCATION_DATA_ZIP_CODE, postalCodeOutput);
                        bundle.putDouble(AppConstants.LATITUDE, latLng.latitude);
                        bundle.putDouble(AppConstants.LONGITUDE, latLng.longitude);
                        returnIntent.putExtra("LOC", bundle);

                        Log.d(TAG, "changeMap: result select 1: " + addressOutput);
                        Log.d(TAG, "changeMap: result select 2: " + streetOutput);
                        Log.d(TAG, "changeMap: result latLng: " + latLng);

                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                });

            } else {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the address in the UI.
     */
    protected void displayAddressOutput() {
        try {
            if (addressOutput != null) {
                if (!isSourceSet) {
                   /* locationText.setText(streetOutput + ", " + areaOutput + ",\n"
                            + cityOutput + ", " + stateOutput + ", " + countryOutput + ", " + postalCodeOutput);*/

                    locationText.setText(streetOutput);
                } else {
                   /* locationText2.setText("Destination : " + streetOutput + ", " + areaOutput + ",\n"
                            + cityOutput + ", " + stateOutput + ", " + countryOutput + ", " + postalCodeOutput);*/

                    locationText2.setText(streetOutput);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService(Location mLocation) {
        try {
            // Create an intent for passing to the intent service responsible for fetching the address.
            Intent intent = new Intent(this, FetchAddressIntentService.class);

            // Pass the result receiver as an extra to the service.
            intent.putExtra(AppConstants.RECEIVER, mResultReceiver);

            // Pass the location data as an extra to the service.
            intent.putExtra(AppConstants.LOCATION_DATA_EXTRA, mLocation);

            // Start the service. If the service isn't already running, it is instantiated and started
            // (creating a process for it if needed); if it is running then it remains running. The
            // service kills itself automatically once all intents are processed.
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestPermission() {
        try {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AddressPlaceActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(AddressPlaceActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Display a SnackBar with an explanation and a button to trigger the request.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Snackbar.make(locationMapCoordinatorLayout, "Some core functions of the app might not work correctly without these permissions.\nPlease ALLOW them in app settings",
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction("ALLOW", view -> requestPermissions(AppConstants.PERMISSIONS_LOCATION, AppConstants.PERMISSION_REQUEST_LOCATION))
                            .show();
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(AppConstants.PERMISSIONS_LOCATION, AppConstants.PERMISSION_REQUEST_LOCATION);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // region Permission requests

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == AppConstants.PERMISSION_REQUEST_LOCATION) {
                // BEGIN_INCLUDE(permission_result)
                // Received permission result for location permission.
                // Check if the only required permission has been granted
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Location permission granted after asking");
                    // Location permission has been granted, location can be displayed
                    if (Utils.isLocationEnabled(AddressPlaceActivity.this)) {
                        Log.d(TAG, "onRequestPermissionsResult: GPS enabled after permission check");
                        initializeMap();
                    } else {
                        Log.d(TAG, "onRequestPermissionsResult: GPS disabled after permission check");
                        showGPSDisabledAlertToUser();
                    }
                    Snackbar.make(locationMapCoordinatorLayout, "Permission Granted, Now you can access location data.", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(locationMapCoordinatorLayout, "Permission Denied, You cannot access location data.", Snackbar.LENGTH_SHORT).show();
                }
                // END_INCLUDE(permission_result)
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showGPSDisabledAlertToUser() {
        try {
            Log.d(TAG, "showGPSDisabledAlertToUser: GPS request");
            LocationSettingsRequest locationSettingsRequestBuilder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
                    .setAlwaysShow(true)
                    .build();

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequestBuilder);

            result.setResultCallback(locationSettingsResult -> {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d(TAG, "GPS enabled after request");
                        initializeMap();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(AddressPlaceActivity.this, LOCATION_SETTINGS_CODE);
                        } catch (IntentSender.SendIntentException ignored) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.e(TAG, "Settings change unavailable. We have no way to fix the settings so we won't show the dialog.");
                        break;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Used to check the result of the check of the user location settings
     *
     * @param requestCode code of the request made
     * @param resultCode  code of the result of that request
     * @param intent      intent with further information
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            super.onActivityResult(requestCode, resultCode, intent);
            switch (requestCode) {
                case LOCATION_SETTINGS_CODE:
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            initializeMap();
                            break;
                        case Activity.RESULT_CANCELED:
                            // The user was asked to change settings, but chose not to
                            Snackbar.make(locationMapCoordinatorLayout, "Please ENABLE GPS for better performance of app.", Snackbar.LENGTH_LONG)
                                    .setAction("Enable", v -> showGPSDisabledAlertToUser()).show();
                            break;
                        default:
                            break;
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            /**
             * The formatted location address.
             */
            addressOutput = resultData.getString(AppConstants.RESULT_DATA_KEY);
            streetOutput = resultData.getString(AppConstants.LOCATION_DATA_STREET);
            areaOutput = resultData.getString(AppConstants.LOCATION_DATA_AREA);
            cityOutput = resultData.getString(AppConstants.LOCATION_DATA_CITY);
            stateOutput = resultData.getString(AppConstants.LOCATION_DATA_STATE);
            countryOutput = resultData.getString(AppConstants.LOCATION_DATA_COUNTRY);
            postalCodeOutput = resultData.getString(AppConstants.LOCATION_DATA_ZIP_CODE);
            latLng = new LatLng(resultData.getDouble(AppConstants.LATITUDE),
                    resultData.getDouble(AppConstants.LONGITUDE));

            // Display the address string or an error message sent from the intent service.
            displayAddressOutput();
        }
    }
    // endregion

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        try {
            mFusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful()) {
                                mLastKnownLocation = task.getResult();
                                if (mLastKnownLocation != null) {
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                } else {
                                    final LocationRequest locationRequest = LocationRequest.create();
                                    locationRequest.setInterval(10000);
                                    locationRequest.setFastestInterval(5000);
                                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                    locationCallback = new LocationCallback() {
                                        @Override
                                        public void onLocationResult(LocationResult locationResult) {
                                            super.onLocationResult(locationResult);
                                            if (locationResult == null) {
                                                return;
                                            }
                                            mLastKnownLocation = locationResult.getLastLocation();
                                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                            mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                        }
                                    };
                                    mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                                }
                            } else {
                                Toast.makeText(AddressPlaceActivity.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
