package com.cemilecerenerdem.wherei;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    public static LocationManager locationManager;
    public static LocationListener locationListener;
    private WifiManager wifiManager;
    LatLng userLocation;
    ProgressDialog dialog;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String isLang = Locale.getDefault().getLanguage();
        Locale locale = new Locale(isLang);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        Boolean isInternet = isInternet();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.dialog_msg_waiting));
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        if (isInternet == true) {
            mapFragment.getMapAsync(this);
        } else {

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setView(R.layout.dialog_warning);
            alertBuilder.setCancelable(false);
            alertBuilder.setPositiveButton(R.string.dialog_PositiveButton_OpenInternet, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onActivityResult(1, 1, null);
                }
            });
            alertBuilder.setNegativeButton(R.string.dialog_NegativeButton_CloseApp, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onActivityResult(0, 0, null);
                }
            });

            alertBuilder.create().show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                mMap.clear();

                //Double latitude = Double.parseDouble("40.718732");
                //Double longitude = Double.parseDouble("29.794408");

                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                //userLocation = new LatLng(latitude, longitude);
                dialog.hide();

                mMap.addMarker(new MarkerOptions().position(userLocation).title(getString(R.string.mapIcons_text)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        //region Permission
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                onActivityResult(2, 1, null);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 1000, locationListener);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 1000, locationListener);
        }
        //endregion
    }

    //region Share Me Location
    public void ShareMeLocation(View v) {

        if (isInternet() == true) {
            if (userLocation.latitude != 0 || userLocation.longitude != 0) {
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_msg);
                dialog.setTitle(getString(R.string.dialog_msgTitle));

                TextView info = (TextView) dialog.findViewById(R.id.txt_view);

                if (userLocation.latitude != 0 && userLocation.longitude != 0) {

                    //Test Location -> 40.718732, 29.794408
                    Geocoder geocoder;
                    List<Address> addresses = null;
                    geocoder = new Geocoder(this, Locale.getDefault());

                    //Double latitude = Double.parseDouble("40.718732");
                    //Double longitude = Double.parseDouble("29.794408");

                    try {
                        addresses = geocoder.getFromLocation(userLocation.latitude, userLocation.longitude, 1);
                        //addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    final String address = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();

                    info.setText(address.toString());

                    Button contact = (Button) dialog.findViewById(R.id.btn_contact);

                    //Contact Guide
                    contact.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String ekMetin = getString(R.string.msg_myLocationIs);
                            if (isInternet() == true) {
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, ekMetin + address.toString());
                                sendIntent.setType("text/plain");
                                startActivity(sendIntent);
                            } else {
                                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                                alertBuilder.setView(R.layout.dialog_warning);
                                alertBuilder.setCancelable(false);
                                alertBuilder.setPositiveButton(R.string.dialog_PositiveButton_OpenInternet, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        onActivityResult(1, 1, null);
                                    }
                                });
                                alertBuilder.setNegativeButton(R.string.dialog_NegativeButton_CloseApp, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        onActivityResult(0, 0, null);
                                    }
                                });

                                alertBuilder.create().show();
                            }
                        }
                    });

                } else {
                    info.setText(getString(R.string.text_YourLocationNotFound));
                }
                dialog.show();
            } else {
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_warning);
                dialog.setTitle(getString(R.string.text_YourLocationNotFound));
                dialog.show();
            }
        } else {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setView(R.layout.dialog_warning);
            alertBuilder.setCancelable(false);
            alertBuilder.setPositiveButton(R.string.dialog_PositiveButton_OpenInternet, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onActivityResult(1, 1, null);
                }
            });
            alertBuilder.setNegativeButton(R.string.dialog_NegativeButton_CloseApp, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onActivityResult(0, 0, null);
                }
            });

            alertBuilder.create().show();
        }
    }
    //endregion

    public boolean isInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null) {
            connectivityManager.getActiveNetworkInfo().isConnected();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Comment
        // requestCode, resultCode 1: Open Internet
        // requestCode 2: Permission Location is true

        if (requestCode == 1) { //Open Internet
            dialog.hide();

            if (resultCode == 1) {
                WifionStart();
            } else {
                finish();
            }
        } else if (requestCode == 2) {
            if (resultCode == 1) {
                refreshApp();
            }
        }
    }

    protected void WifionStart() {
        dialog.setMessage(getString(R.string.dialog_msg_waiting));
        dialog.show();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver, intentFilter);
    }

    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifi.setWifiEnabled(true);
            dialog.hide();
            refreshApp();
        }
    };

    public void refreshApp() {

        try {
            Thread.sleep(4000);
            Intent intents = getApplication().getBaseContext().getPackageManager().getLaunchIntentForPackage(getApplication().getBaseContext().getPackageName());
            intents.setFlags(intents.FLAG_ACTIVITY_NEW_TASK | intents.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intents);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
