package com.mycheckins;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;

    // Initialize the activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the required permission for this application
        LocationManager locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET
            };

            // If one of the permissions are not yet granted, ask for all permissions to be enabled
            for(String permission : permissions) {
                if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, 1);
                    return;
                }
            }

            // Start the application
            initializeApplication();
        } else {
            // Stop the application if the GPS is not on
            Toast.makeText(this, "This application requires the GPS service to be enabled.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // This method is called after the user grants permissions required. So we check again that
    // all required permissions are granted
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == 1) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Start the application
                initializeApplication();
            } else {
                // Stop application if the permission has not been approved
                Toast.makeText(this, "The application requires necessary permissions granted.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // Initialize anything that needs to be done to start this application
    private void initializeApplication() {
        showListUIFragment();
        instance = this;
        startService(new Intent(this, LocationService.class));
    }

    // Display the fragment for displaying the list of items
    public void showListUIFragment() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, new ListUIFragment(), "List UI");
        fragmentTransaction.commit();
    }

    // Display the fragment for creating an item or viewing an item
    public void showItemUIFragment(String itemId) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        ItemUIFragment fragment = new ItemUIFragment();

        if(itemId != null) {
            Bundle bundle = new Bundle();
            bundle.putString("item_id", itemId);
            fragment.setArguments(bundle);
        }

        fragmentTransaction.replace(R.id.fragment_container, fragment, "Item UI");
        fragmentTransaction.commit();
    }

    // Pressing the back button will always go back to the list UI
    @Override
    public void onBackPressed() {
        showListUIFragment();
    }

    // Return the main activity instance
    public static MainActivity getInstance() {
        return instance;
    }
}
