package com.example.justideas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.justideas.Prevalent.Prevalent;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.angmarch.views.NiceSpinner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ConfirmOrderActivity extends AppCompatActivity implements LocationListener {

    // variables Initializer
    public Button locationFinder, confirmDtlBtn;
    public static EditText addressLine1, addressLine2, username, postcode, county, country, town, userPhone;
    public String totalAmount = "";
    public TextView backBtn;
    public NiceSpinner savedAddress;

    protected LocationManager locationManager;
    protected Context context;
    protected FusedLocationProviderClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        // Assigning values for static fields
        backBtn = (TextView) findViewById(R.id.back_delivery_btn);
        locationFinder = findViewById(R.id.currentLocationBtn);
        client = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Assigning values for manual address input
        username = findViewById(R.id.user_dvName);
        userPhone = findViewById(R.id.user_dvPhone);
        addressLine1 = findViewById(R.id.user_dvAddressL1);
        addressLine2 = findViewById(R.id.user_dvAddressL2);
        postcode = findViewById(R.id.user_dvPostcode);
        county = findViewById(R.id.user_dvCounty);
        town = findViewById(R.id.user_dvTown);
        country = findViewById(R.id.user_dvCountry);

        // Assigning values for action fields
        confirmDtlBtn = (Button) findViewById(R.id.confirm_button);
        totalAmount = getIntent().getStringExtra("Total Price");

        // location finder must be non focusable
        locationFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(ConfirmOrderActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(ConfirmOrderActivity.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    client.getLastLocation().addOnSuccessListener(ConfirmOrderActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                getAddress(getBaseContext(), location.getLatitude(), location.getLongitude());
                            }
                        }
                    });
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    new AlertDialog.Builder(ConfirmOrderActivity.this)
                            .setTitle("Location Permission")
                            .setMessage("Use only when desired delivery address matches your location")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(ConfirmOrderActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                                }
                            })
                            .create()
                            .show();
                } else {
                    //Prompt the user once explanation has been shown
                    ActivityCompat.requestPermissions(ConfirmOrderActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ConfirmOrderActivity.this);
            }
        });

        confirmDtlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDetails();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {
                    Toast.makeText(this, "Permission was not granted", Toast.LENGTH_LONG).show();
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }


    private void checkDetails() {
        if (TextUtils.isEmpty(username.getText().toString())) {
            Toast.makeText(this, "Please Fill out all mandatory fields", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(userPhone.getText().toString())) {
            Toast.makeText(this, "Please Fill out all mandatory fields", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(addressLine1.getText().toString())) {
            Toast.makeText(this, "Please Fill out all mandatory fields", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(postcode.getText().toString())) {
            Toast.makeText(this, "Please Fill out all mandatory fields", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(country.getText().toString())) {
            Toast.makeText(this, "Please Fill out all mandatory fields", Toast.LENGTH_LONG).show();
        } else {
            ConfirmOrder();
        }
    }

    private void ConfirmOrder() {

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("mm, dd, yyyy");
        final String saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        final String saveCurrentTime = currentTime.format(calendar.getTime());

        final DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference()
                .child("Orders")
                .child(Prevalent.currentOnlineUser.getPhone());

        HashMap<String, Object> ordersMap = new HashMap<>();
        ordersMap.put("totalAmount", totalAmount);
        ordersMap.put("name", username.getText().toString());
        ordersMap.put("phone", userPhone.getText().toString());
        ordersMap.put("date", saveCurrentDate);
        ordersMap.put("time", saveCurrentTime);
        ordersMap.put("addressLine1", addressLine1.getText().toString());
        ordersMap.put("addressLine2", addressLine2.getText().toString());
        ordersMap.put("postcode", postcode.getText().toString());
        ordersMap.put("county", country.getText().toString());
        ordersMap.put("town", town.getText().toString());
        ordersMap.put("country", country.getText().toString());
        ordersMap.put("state", "Awaiting Verification");

        orderRef.updateChildren(ordersMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Basket Items")
                                    .child("User View")
                                    .child(Prevalent.currentOnlineUser.getPhone())
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ConfirmOrderActivity.this, "Order Confirmed", Toast.LENGTH_LONG).show();

                                                Intent intent = new Intent(ConfirmOrderActivity.this, HomeActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    public void getAddress(Context context, double LATITUDE, double LONGITUDE) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);

            if (addresses != null && addresses.size() > 0) {
                //If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                addressLine1.setText(addresses.get(0).getAddressLine(0));

                town.setText(addresses.get(0).getLocality());
                county.setText(addresses.get(0).getAdminArea());
                country.setText(addresses.get(0).getCountryName());
                postcode.setText(addresses.get(0).getPostalCode());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
