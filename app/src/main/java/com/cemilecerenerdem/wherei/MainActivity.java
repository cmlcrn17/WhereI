package com.cemilecerenerdem.wherei;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
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
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    public static LocationManager locationManager;
    public static LocationListener locationListener;
    private WifiManager wifiManager;
    LatLng userLocation;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(R.layout.activity_main);

        String isLang = Locale.getDefault().getLanguage();
        Locale locale = new Locale(isLang);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        Boolean isInternet = isInternet();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        TwitterLoginButton btnTwt = (TwitterLoginButton) findViewById(R.id.btn_twt);


        Callback twitterCallback = new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                TwitterAuthToken authToken = session.getAuthToken();
                String token = authToken.token;
                String secret = authToken.secret;

                login(session);
            }

            @Override
            public void failure(TwitterException exception) {

            }
        };
        //btnTwt.setCallback(twitterCallback);

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

                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
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

        //Get Permission
        if (Build.VERSION.SDK_INT >= 23) {

            //TODO: 4- İzin verdikten sonra tekrar uygulamayı açmam gerekiyor. Bu kod akışı araştırılacak.
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 1000, locationListener);
            }

        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 1000, locationListener);
        }
    }

    public void ShareMeLocation(View v) {

        if (isInternet() == true) {
            if (userLocation.latitude != 0 || userLocation.longitude != 0) {
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_msg);
                dialog.setTitle(getString(R.string.dialog_msgTitle));

                TextView info = (TextView) dialog.findViewById(R.id.txt_view);

                if (userLocation.latitude != 0 && userLocation.longitude != 0) {

                    //TODO: 20 - Mahalle, Sokak, Kapı no bilgisi düzgün şekilde yazdırılacak.
                    //Test Location -> 40.718732, 29.794408
                    Geocoder geocoder;
                    List<Address> addresses = null;
                    geocoder = new Geocoder(this, Locale.getDefault());

                    //Double latitude = Double.parseDouble("40.718732");
                    //Double longitude = Double.parseDouble("29.794408");

                    try {
                        addresses = geocoder.getFromLocation(userLocation.latitude, userLocation.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    final String address = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();

                    //Write Lat, Long
                    //final String location = "Latidude: " + userLocation.latitude + " - Longitude: " + userLocation.longitude;
                    //info.setText(location);

                    info.setText(address.toString());

                    Button whatsApp = (Button) dialog.findViewById(R.id.btn_whts);
                    Button twitter = (Button) dialog.findViewById(R.id.btn_twt);
                    Button contact = (Button) dialog.findViewById(R.id.btn_contact);

                    //WhatsApp
                    whatsApp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            //TODO 19- WhatsApp olan android telefonda test edilecek.
                            if (isInternet() == true) {
                                //TODO: 6- WhatsApp kişi listesi açılacak.
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

                    //Twitter
                    twitter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isInternet() == true) {
                                //TODO: 15- Twitterda paylaş yapılacak.
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

                    //Contact Guide
                    contact.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //TODO: TEST - Mesaj gönderilerek test edilecek.
                            if (isInternet() == true) {
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "My location is " + address.toString());
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

        if (requestCode == 1) { //Open Internet

            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
            this.finishAffinity();

            if (resultCode == 1) {
                onStart();
            } else {
                finish();
            }
        }
    }

    //TODO - TEST - Wifi aç telefonda test edilecek.
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver, intentFilter);
    }

    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

            switch (wifiStateExtra) {
                case WifiManager.WIFI_STATE_ENABLED:
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    break;
            }
        }
    };

    public void login(TwitterSession session) {
        String username = session.getUserName();
        Intent intent = new Intent(this, Homepage.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}
