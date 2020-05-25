package com.example.gpslocationapp;

import android.location.Location;
//taxiApp

public class TaxiManager {
    private Location destinationLocation;

    public Location getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(Location destinationLocation) {
        this.destinationLocation = destinationLocation;
    }
    public float returnTheDistanceToDestinationInMeters(Location currentLocation){
        if (currentLocation!=null && destinationLocation!=null){
            return  currentLocation.distanceTo(destinationLocation);

        }else {
            return -100.0f;
        }

    }
    public String returnKmsbetweenCurrentLocationAndDestinationLocation(Location currentLocation,int metersPerKm){
        int kms=(int)(returnTheDistanceToDestinationInMeters(currentLocation)/metersPerKm);
        if(kms==1){
            return "1 km";
        }else if (kms>1){
            return kms+" kms left";
        }else {
            return "no km";
        }
    }

    public String returnTheTimeToGetToTheDestination(Location currentLocation,float kmph,int metersPerKm){

        float distanceInMeters=returnTheDistanceToDestinationInMeters(currentLocation);
        float timeLeft=distanceInMeters/(kmph*metersPerKm);
        String timeResult ="";
        int timeLeftInHours=(int) timeLeft;

        if(timeLeftInHours==1){
            timeResult= timeResult + " 1 hour ";
        }else if(timeLeftInHours>1){

            timeResult=timeResult + timeLeftInHours +" Hours ";


        }
        int minutesLeft=(int)((timeLeft-timeLeftInHours)*60);
        if (minutesLeft==1){
            timeResult=timeResult+" 1 min";
        }else if (minutesLeft>1){
            timeResult=timeResult+minutesLeft+" minutes";
        }

        if (timeLeftInHours<=0 && minutesLeft<=0){
            timeResult="less than a minute left to reach";
        }

        return timeResult;


    }

}
