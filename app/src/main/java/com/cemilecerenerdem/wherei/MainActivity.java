package com.cemilecerenerdem.wherei;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

    //TODO: 1- Uygulamaya icon bulunacak.

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

    public void WriteMeLocation(View v) {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_msg);
        dialog.setTitle(getString(R.string.dialog_msgTitle));

        TextView info = (TextView) dialog.findViewById(R.id.txt_view);

        if (userLocation.latitude != 0 && userLocation.longitude != 0) {
            info.setText("Latidude: " + userLocation.latitude + " - Longitude: " + userLocation.longitude);

            Button whatsApp = (Button) dialog.findViewById(R.id.btn_whts);
            Button twitter = (Button) dialog.findViewById(R.id.btn_twt);
            Button person = (Button) dialog.findViewById(R.id.btn_person);

        } else {
            info.setText("Your location not found.");
        }

        dialog.show();

        //TODO: 2- Dialog Alert içerisinde google dan alınan konum bilgisi mahalle sokak kapı no olarak yazdırılacak.
        //TODO: 3- Dialog Alert içerisine paylaş butonları işlemleri yazılacak..
    }
}
