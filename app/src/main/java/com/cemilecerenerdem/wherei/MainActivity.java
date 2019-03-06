package com.cemilecerenerdem.wherei;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    public static LocationManager locationManager;
    public static LocationListener locationListener;
    LatLng userLocation;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        Boolean isInternet = isInternet();

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

                    //TODO: 2- Dialog Alert içerisinde google dan alınan konum bilgisi mahalle sokak kapı no olarak yazdırılacak.
                    final String location = "Latidude: " + userLocation.latitude + " - Longitude: " + userLocation.longitude;
                    info.setText(location);

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
                                //TODO: 18- Twittera giriş yapılarak test edilecek.
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
                            //TODO: 16- Mesaj gönderilerek test edilecek.
                            //TODO: 17- Kişi listesi gelecek. Seçilen kişiye direk gönderim yapılacak.
                            if (isInternet() == true) {
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "My location is " + location);
                                sendIntent.setType("text/plain");
                                startActivity(sendIntent);
                                //TODO: 19- Mesaj gönderimi başarılı ise mesaj göster.
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
            if (resultCode == 1) {
                //TODO: 14- İnterneti açtır
            } else {
                finish();
            }
        } else {
            finish();
        }
    }
}
