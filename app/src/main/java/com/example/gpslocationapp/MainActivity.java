package com.example.gpslocationapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
//import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import java.util.List;

//implementation of various interfaces to get the location
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, View.OnClickListener/*, LocationListener */{

    EditText edtAddress,edtKmph,edtMetersPerKm;
    Button getData;
    TextView txtTime,txtDistanceValue;
    private String destinationLocation="";
    private TaxiManager taxiManager;


    public static final String TAG="TAG";
    private static final int REQUEST_CODE=1000; //by convention the constants are in capitals

    private GoogleApiClient googleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtAddress=findViewById(R.id.edtAddress);
        edtKmph=findViewById(R.id.edtkmph);
        edtMetersPerKm=findViewById(R.id.edtMetersPerKm);
        getData=findViewById(R.id.btnGetTheData);
        txtTime=findViewById(R.id.txtTime);
        txtDistanceValue=findViewById(R.id.txtDistanceValue);

        getData.setOnClickListener(MainActivity.this);
        taxiManager=new TaxiManager();


        googleApiClient=new GoogleApiClient.Builder(MainActivity.this)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this)
                .addApi(LocationServices.API).build();
    }





    @Override
    public void onConnected(Bundle bundle) {

        Log.d(TAG,"we are connected");
        //for runtime distance changing
//        FusedLocationProviderApi fusedLocationProviderApi=LocationServices.FusedLocationApi;
//        LocationRequest locationRequest=new LocationRequest();
//        locationRequest.setInterval(10000);//every 10 sec the location is requested
//        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);//less power is consumed using this
//        locationRequest.setSmallestDisplacement(5);//after each 5 mts the location is requested
//        if (googleApiClient.isConnected()){
//            fusedLocationProviderApi.requestLocationUpdates(googleApiClient,locationRequest,MainActivity.this); //MainActivity Listens to the location changes and we need to allow mainactivity to listen to the location changes and the class should implement LocationListener
//        }else {
//            googleApiClient.connect();
//        }




    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.d(TAG,"location access suspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG,"connection failed");
        if(connectionResult.hasResolution()==true) { //connectionResult.hasResolution()==true then the connection is failed cz user has not signed up in google play services and then it will ask the user to sign up the user
            try {


                connectionResult.startResolutionForResult(MainActivity.this, REQUEST_CODE); //we need to add it in a try catch block cz user may fail to install google play services
            }catch (Exception e){
                Log.d(TAG,e.getStackTrace().toString());
            }
        }else {
            Toast.makeText(MainActivity.this,"Google play services not working",Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {   //after installing google play services the user can allow to access the location again
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE && requestCode==RESULT_OK){
            googleApiClient.connect();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient!=null){
            googleApiClient.connect();
        }
    }

    @Override
    public void onClick(View v) {
        String addressValue=edtAddress.getText().toString();
        boolean isGeoCoding=true; //geoCoding is coverting latitude and longitude value into human readable text
        if (!addressValue.equals(destinationLocation)){ //the details returned only once not always for the same destination

            destinationLocation=addressValue;
            Geocoder geocoder=new Geocoder(MainActivity.this);
            //as geocode may fail we must put the logic inside the try catch block
            try {

                List<Address> myAddresses=geocoder.getFromLocationName(destinationLocation,4); //our list is going to have 4 most accurate address
                if (myAddresses!=null){
                    double latitude=myAddresses.get(0).getLatitude();
                    double longitude=myAddresses.get(0).getLongitude();
                    Location locationAddress=new Location("mydestination address");
                    locationAddress.setLatitude(latitude);
                    locationAddress.setLongitude(longitude);
                    taxiManager.setDestinationLocation(locationAddress);


                }



            }catch (Exception e){
                isGeoCoding=false;  //if the destination address could not be geocoded
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"plz enter correct location",Toast.LENGTH_LONG).show();
            }

        }
        int permissionCheck=ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION);
        if(permissionCheck==PackageManager.PERMISSION_GRANTED){

            FusedLocationProviderApi fusedLocationProviderApi= LocationServices.FusedLocationApi;//mainEntrance after user provides the location
            Location userCurrentLocation=fusedLocationProviderApi.getLastLocation(googleApiClient);
            if(userCurrentLocation!=null && isGeoCoding==true){

                txtDistanceValue.setText(taxiManager.returnKmsbetweenCurrentLocationAndDestinationLocation(userCurrentLocation,Integer.parseInt(edtMetersPerKm.getText().toString())));
                txtTime.setText(taxiManager.returnTheTimeToGetToTheDestination(userCurrentLocation,Float.parseFloat(edtKmph.getText().toString()),Integer.parseInt(edtMetersPerKm.getText().toString())));






            }

        }else{
            txtDistanceValue.setText("This app requires location permission");
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }


    }

    //for run time distance and time changing

//    @Override
////    public void onLocationChanged(Location location) {
////            onClick(null);      //user does not have to press the button for location
////    }

//    @Override
//    protected void onPause() {      //when the app goes to background or been paused the application stops requesting for the location
//        super.onPause();
//        FusedLocationProviderApi fusedLocationProviderApi=LocationServices.FusedLocationApi;    //FusedLocationProviderApi is an interface and the object cannot be made using new
//        fusedLocationProviderApi.removeLocationUpdates(googleApiClient,MainActivity.this);
//
//
//    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------

    /*

    //custom methods
    private void showTheUserLocation(){
        //ask the user to access the permission and also known as dangerous permission
        int permissionCheck= ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck== PackageManager.PERMISSION_GRANTED){

            FusedLocationProviderApi fusedLocationProviderApi=LocationServices.FusedLocationApi;// FusedLocationProviderApi is an interface and thus its object cannot be created using new
            location=fusedLocationProviderApi.getLastLocation(googleApiClient); //getLastLocation is used mainly for the coarse Location and if it does not get the locatin it returns null which is a very rare case takes googleapiClient instance as a parameter
            if (location!=null){
                double latitude = location.getLatitude();
                double longitude=location.getLongitude();
                txtLocation.setText("latitude is "+latitude+"\n"+"longitude is "+longitude);
            }else {
                txtLocation.setText("app not able to" +
                        " trace location");
            }



        }else {
            txtLocation.setText("THIS APP REQUIRES LOCATION");
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
    }*/
}
