package org.tensorflow.lite.examples.objectdetection.locationandtextdetect;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.tensorflow.lite.examples.objectdetection.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LiveLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int PERMISSION_REQUEST_CODE = 200;
    Location location = null;
    LocationManager mlocManager;
    private GoogleMap mMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    Location currentLocation;
    String locationAddress;
    AppLocationService appLocationService;
    LatLng latLng = null;
    TextToSpeech textToSpeech;


    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_location);

        appLocationService = new AppLocationService(
                LiveLocationActivity.this);

        geocoder = new Geocoder(this, Locale.getDefault());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(LiveLocationActivity.this);

        checkPermission();
        requestPermission();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(LiveLocationActivity.this);
        updateLocation(); //update user location in database
        fetchLocation(); //fetching current user current location


        Location location = appLocationService
                .getLocation(LocationManager.GPS_PROVIDER);

//        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        longitude = String.valueOf(location.getLongitude());
//        latitude = String.valueOf(location.getLatitude());

        mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        LocationListener mlocListener = new MyLocationListener();
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);

        // create an object textToSpeech and adding features into it
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                // if No error is found then only it will run
                if(i!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    Toast.makeText(LiveLocationActivity.this, "success ", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(LiveLocationActivity.this, "an error occurr \n" + TextToSpeech.ERROR, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


    //update current user location in database
    private void updateLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

                }
            }
        });
    }

    //fetchLocation
    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    assert supportMapFragment != null;
                    latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                    float zoomLevel = 16.0f; //This goes up to 21
                    final MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(locationAddress);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

                    mMap.addMarker(markerOptions);
                    mMap.addCircle(new CircleOptions()
                            .center(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                            .radius(5000)
                            .strokeColor(Color.RED)
                            .strokeWidth((float) 1.0)
                            .fillColor(Color.parseColor("#22E1BEE7")));


                    mMap.addCircle(new CircleOptions()
                            .center(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                            .radius(2000)
                            .strokeColor(Color.parseColor("#227a69d9"))
                            .fillColor(Color.parseColor("#22a2d5fa")));


                    mMap.addCircle(new CircleOptions()
                            .center(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                            .radius(1000)
                            .strokeColor(Color.parseColor("#224CAF50"))
                            .fillColor(Color.parseColor("#22ff9400")));


                    Geocoder geocoder = new Geocoder(LiveLocationActivity.this, Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);

                        if (addresses.size() > 0 ){
                            Log.d("sjbjhsb", addresses.get(0).getSubLocality() + " SubLocality");
                            Log.d("sjbjhsb", addresses.get(0).getLocality() + " Locality");
                            Log.d("sjbjhsb", addresses.get(0).getAdminArea() + " AdminArea");
                            Log.d("sjbjhsb", addresses.get(0).getSubAdminArea() + " Sub AdminArea");
                            Log.d("sjbjhsb", addresses.get(0).getLocale() + " getLocale");
                            String loc = "You are currently in " + addresses.get(0).getSubLocality() +
                                    " , " + addresses.get(0).getLocality();

                            textToSpeech.speak(loc, TextToSpeech.QUEUE_FLUSH, null);
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    /* Class My Location Listener */

    @Override
    public void onMapReady(GoogleMap googleMap) {

        float zoomLevel = 16.0f; //This goes up to 21
        mMap = googleMap;


        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_night);
        mMap.setMapStyle(style);


        if (latLng != null) {


            final MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(locationAddress);
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

            googleMap.addMarker(markerOptions);
        }

    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {


            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }

        }
    }

    public class MyLocationListener implements LocationListener {
        @Override

        public void onLocationChanged(Location loc){

            location = loc;
            Geocoder geocoder = new Geocoder(LiveLocationActivity.this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                if (addresses.size() > 0 ){
                    Log.d("utiyteiure", addresses.get(0).getSubLocality() + " SubLocality");
                    Log.d("utiyteiure", addresses.get(0).getLocality() + " Locality");
                    Log.d("utiyteiure", addresses.get(0).getAdminArea() + " AdminArea");
                    Log.d("utiyteiure", addresses.get(0).getSubAdminArea() + " Sub AdminArea");
                    Log.d("utiyteiure", addresses.get(0).getAddressLine(0) + " getAddressLine");
                    String loc1 = "You are currently in " + addresses.get(0).getSubLocality() +
                            " , " + addresses.get(0).getLocality();

                    if (textToSpeech != null) {
                        textToSpeech.speak(loc1, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("hjvbdxjhb", "lat: " + location.getLatitude() + " :: " + "long: " + location.getLongitude());

        }
        public void onProviderDisabled(String provider){

            //nothin
        }


        public void onProviderEnabled(String provider){

            //nothin
        }

        public void onStatusChanged(String provider, int status, Bundle extras){
            //nothin
            Log.d("dfsffsdfeerw", "provider: " + provider + " :: " + "status: " + status);
        }
    }/* End of Class MyLocationListener */

    private boolean checkPermission() {

        int result5 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int result6 = ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION);
        return result5 == PackageManager.PERMISSION_GRANTED
                && result6 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted)
                        Toast.makeText(this, "Permission Granted, Now you can access location data and camera.", Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(this, "Permission Denied, You cannot access location data and camera.", Toast.LENGTH_SHORT).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            showMessageOKCancel("You need to allow access to both the permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE},
                                                        PERMISSION_REQUEST_CODE);
                                            }
                                        }
                                    });
                            return;

                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new androidx.appcompat.app.AlertDialog.Builder(LiveLocationActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        textToSpeech = null;
        location = null;
        currentLocation = null;
    }
}