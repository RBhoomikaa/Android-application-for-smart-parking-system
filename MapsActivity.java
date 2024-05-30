package com.app.smartparking;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.app.smartparking.databinding.ActivityMapsBinding;
import com.app.smartparking.model.Place;
import com.app.smartparking.utils.AppPermissions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowLongClickListener {

    private GoogleMap mMap;
    private AppPermissions perms;
    private TextToSpeech tts;
    private Location myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        perms = new AppPermissions(this);
        perms.setRationaleMessage(R.string.location_required);
        perms.setRationaleIndefinite(true);
        tts = new TextToSpeech(this, status -> {
        });
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMyLocationChangeListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowLongClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        checkGps();
    }

    @SuppressLint("MissingPermission")
    private void checkGps() {
        if (!perms.hasPermission(AppPermissions.LOCATION_PERMISSION))
            perms.requestPermission(AppPermissions.LOCATION_PERMISSION, AppPermissions.REQ_CODE);
        else if (!perms.isGPSEnabled())
            perms.showGpsSettingDialog(AppPermissions.REQ_CODE);
        else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            myLocation = mMap.getMyLocation();
            if (myLocation == null) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (myLocation != null)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())));
            loadPlaces();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AppPermissions.REQ_CODE)
            checkGps();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppPermissions.REQ_CODE)
            checkGps();
    }

    private void loadPlaces() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("places");
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Place> list = new ArrayList<>();
                if (snapshot.exists()) for (DataSnapshot child : snapshot.getChildren())
                    list.add(child.getValue(Place.class));
                createMarkers(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void createMarkers(List<Place> placeList) {
        for (Place place : placeList) {
            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(place.getLatitude(), place.getLongitude())).title(place.getName()));
            marker.setTag(place);
        }
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        Place place = (Place) marker.getTag();
        new MaterialAlertDialogBuilder(this)
                .setTitle(place.getName())
                .setMessage(place.getAddress())
                .setNegativeButton("Direction", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        onInfoWindowLongClick(marker);
                    }
                })
                .setPositiveButton("View Slot", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bundle bundle = getIntent().getExtras();
                        bundle.putString(ParkingActivity.PLACE_ID, place.getPlaceId());
                        Intent intent = new Intent(getApplicationContext(), ParkingActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }).show();
    }

    @Override
    public void onInfoWindowLongClick(@NonNull Marker marker) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + marker.getPosition().latitude + "," + marker.getPosition().longitude));
        startActivity(intent);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        if (myLocation == null) return false;
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(marker.getPosition().latitude);
        location.setLongitude(marker.getPosition().longitude);
        float distance = Math.round(location.distanceTo(myLocation));
        String text = marker.getTitle() + " is " + distance + " meters";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationChange(@NonNull Location location) {
        myLocation = location;
        //if(myLocation!=null)mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(myLocation.getLatitude(),myLocation.getLongitude())));
    }
}