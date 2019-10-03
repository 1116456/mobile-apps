package com.mycheckins;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;

// This class will run on background which keeps track the location of the user
public class LocationService extends Service {

    public static Location lastLocationRecorded;
    private boolean isRunning = false;

    // Nothing to do for this method but required to be here
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Start the location service which will update the location GPS
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return START_STICKY;

        // If the location service is currently running then don't rerun it
        if(isRunning)
            return START_STICKY;

        // Get the best location provider (either Network or GPS)
        final LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        final String provider = locationManager.getBestProvider(criteria, true);

        // Stop if no provider found
        if(provider == null)
            return START_STICKY;

        locationManager.requestLocationUpdates(provider, 2 * 60 * 1000, 10, new LocationListener() {
            // Update the new location found
            @Override
            public void onLocationChanged(Location location) {
                lastLocationRecorded = location;
            }

            // Nothing to do here
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            // Rerun our location listener if GPS is enabled
            @Override
            public void onProviderEnabled(String s) {
                if(ContextCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    return;

                locationManager.requestLocationUpdates(provider, 2 * 60 * 1000, 10, this);
                isRunning = true;
            }

            // Stop our location listener if GPS is disabled
            @Override
            public void onProviderDisabled(String s) {
                locationManager.removeUpdates(this);
                isRunning = false;
            }
        });

        isRunning = true;
        return START_STICKY;
    }
}
