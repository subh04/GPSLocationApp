package com.example.gpslocationapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;

//implementation of various interfaces to get the location
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG="TAG";
    private static final int REQUEST_CODE=1000; //by convention the constants are in capitals

    private GoogleApiClient googleApiClient;
    private Location location;
    private TextView txtLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtLocation=findViewById(R.id.txtLocation);
        googleApiClient=new GoogleApiClient.Builder(MainActivity.this)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.d(TAG,"we are connected");

        showTheUserLocation();

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
    }
}
