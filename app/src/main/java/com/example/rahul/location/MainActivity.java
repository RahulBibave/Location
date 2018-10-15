package com.example.rahul.location;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    private final static int ALL_PERMISSIONS_RESULT = 101;
    private EditText edtCurrentLoc,edtDestLoc;
    private static final int REQUEST_SELECT_PLACE = 1000;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));


    private TextView txtDestination;
    public String requestLocation;
    public String requestLocationdetails;
    public String strlat;
    public String strlong;
    public String requestLat;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    public String requestLong;
    LocationTrack locationTrack;
    TextView txtCurrent;
    Button start;
    double longitude,latitude,endLat,EndLong;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);
        init();

        permissionsToRequest = findUnAskedPermissions(permissions);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }


        locationTrack = new LocationTrack(MainActivity.this);


        if (locationTrack.canGetLocation()) {


            longitude = locationTrack.getLongitude();
             latitude = locationTrack.getLatitude();

            Log.e("current",""+latitude+","+longitude);

            try {

                Geocoder geo = new Geocoder(MainActivity.this.getApplicationContext(), Locale.getDefault());
                List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
                if (addresses.isEmpty()) {
                    //  yourtextboxname.setText("Waiting for Location");
                }
                else {
                    if (addresses.size() > 0) {
                        txtCurrent.setText(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());

                        Log.e("dasdadasd","l"+strlat+"lllllllllllll"+strlong);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

             Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
        } else {

            locationTrack.showSettingsAlert();
        }



        txtDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autocity();
                start.setVisibility(View.VISIBLE);
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Distance( startLat,startLong,endLat,EndLong);
              // int km=calculateDistanceInKilometer(18.500725,73.858466,18.506341,73.898102);

                int km=calculateDistanceInKilometer(latitude,longitude,endLat,EndLong);
               Log.e("ddddddddddddddddddd",""+km);
                Toast.makeText(MainActivity.this,"Destination Location is "+km+" Km away",Toast.LENGTH_LONG).show();
                if (km==1){
                    Log.e("kkkkk","km---------"+km);
                    sendNotification();
                }


            }
        });
    }

    private void init() {
        txtDestination=findViewById(R.id.txtDestination);
        start=findViewById(R.id.start);
        txtCurrent=findViewById(R.id.txtCurrentLoc);
    }


    public void sendNotification() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                .setTicker("Hearty365")
                //     .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("Location")
                .setContentText("Your destination is in 1 Km")
                .setContentInfo("Info");

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());


    }






    public void autocity(){
        try {
            AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(Place.TYPE_COUNTRY)
                    .setCountry("IN")
                    .build();
            Intent intent = new PlaceAutocomplete.IntentBuilder
                    (PlaceAutocomplete.MODE_OVERLAY)
                    .setFilter(autocompleteFilter)
                    .setBoundsBias(BOUNDS_MOUNTAIN_VIEW)
                    .build(MainActivity.this);
            startActivityForResult(intent, REQUEST_SELECT_PLACE);

        } catch (GooglePlayServicesRepairableException |
                GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

}
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_PLACE) {
           // bArea = false;
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                MainActivity.this.onPlaceSelected(place);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
     //           this.onError(status);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onPlaceSelected(Place place) {
        Log.e("lattitude", String.valueOf(place.getLatLng()));
        LatLng queriedLocation = place.getLatLng();
        Log.e("Latitude is", "" + queriedLocation.latitude);
        Log.e("Longitude is", "" + queriedLocation.longitude);




        String city = "", state = "", country = "";
        try {

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            addresses = geocoder.getFromLocation(queriedLocation.latitude, queriedLocation.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            Log.e("Adrrsssssssssss", "" + addresses);

            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(",");
                }
                //address = strReturnedAddress.toString().trim();

                //  Log.w("My Current loction address", strReturnedAddress.toString());
            } else {
                // Log.w("My Current loction address", "No Address returned!");
            }


            // String addres = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            city = addresses.get(0).getLocality();
            state = addresses.get(0).getAdminArea();
            country = addresses.get(0).getCountryName();
            endLat=addresses.get(0).getLatitude();
            EndLong=addresses.get(0).getLongitude();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            Log.e("city", city);
            Log.e("state", state);
            Log.e("country", country);
            Log.e("postalCode", postalCode);


           // Log.e("location", "Latitude:" + latitude + ", Longitude:" + longitude);
        } catch (Exception e) {
            e.printStackTrace();
        }


        strlat = String.valueOf(queriedLocation.latitude);
        strlong = String.valueOf(queriedLocation.longitude);
        Log.e("place", place.getAddress().toString());
        Log.e("placename", place.getName().toString());
        txtDestination.setText(place.getAddress().toString());

        String placeDetailsStr = place.getName() + "n"
                + place.getId() + "n"
                + place.getLatLng().toString() + "n"
                + place.getAddress() + "n"

                + place.getAttributions();
        Log.e("placeDetailsStr", placeDetailsStr);
        //txtArea.setText(place.getName() + "," + city + "," + state + "," + country);
       // sAreaString = txtArea.getText().toString().trim();
       // requestLocation = sAreaString;


    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
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
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTrack.stopListener();
    }





    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;
    public int calculateDistanceInKilometer(double userLat, double userLng,
                                            double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c));
    }

}
