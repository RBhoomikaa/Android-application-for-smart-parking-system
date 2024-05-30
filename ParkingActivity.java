package com.app.smartparking;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.smartparking.adapter.ParkingAdapter;
import com.app.smartparking.model.Parking;
import com.app.smartparking.model.Place;
import com.app.smartparking.utils.DateUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.util.Date;

public class ParkingActivity extends AppCompatActivity {
    public static final String PLACE_ID = "placeId", MOBILE = "mobile", VEHICLE_NO = "vehicleNo";
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerSlot;
    private String placeId, mobile, vehicleNo;
    private Place place;
    private ParkingAdapter parkingAdapter;
    private boolean bookingExist,parkingExist;
    private String exitSlot="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(() -> loadPlace());
        recyclerSlot = findViewById(R.id.recyclerSlot);
        parkingAdapter = new ParkingAdapter();
        recyclerSlot.setAdapter(parkingAdapter);
        parkingAdapter.setOnItemClickedListener((parking1, position) -> checkParking(parking1,position));
        loadPlace();
        bookingExpire();
    }
    private void checkParking(Parking parking1, int position){
        if(parking1==null &&(bookingExist||parkingExist)){
        String s=bookingExist?Parking.Status.BOOKED:parkingExist?Parking.Status.IN:"";
        new MaterialAlertDialogBuilder(this)
                .setTitle("Alert")
                .setMessage(vehicleNo+" is already "+s+" "+exitSlot+". So booking / parking not allowed.")
                .setPositiveButton("Ok",(dialogInterface, i) -> dialogInterface.dismiss())
                .show();
        }else if (parking1 == null) {
            Parking parking = new Parking();
            parking.setSlotId(position + "");
            parking.setPlaceId(placeId);
            parking.setVehicleMobileNo(mobile);
            parking.setVehicleNo(vehicleNo);
            int min=5;
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                try{
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Location curLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    int km=(int)Math.round(SphericalUtil.computeDistanceBetween(new LatLng(curLoc.getLatitude(),curLoc.getLongitude()),new LatLng(place.getLatitude(),place.getLongitude()))/1000);
                    min=min+(km*2);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            final int mins=min;
            new MaterialAlertDialogBuilder(this)
                    .setTitle(place.getName())
                    .setMessage("Slot: " + (position + 1) + "\nVehicle No: " + vehicleNo)
                    .setPositiveButton(R.string.park_in, (dialogInterface, i) -> {
                        parking.setTimeIn(DateUtils.now());
                        parking.setStatus(Parking.Status.IN);
                        dialogInterface.dismiss();
                        saveParking(parking, position);
                    })
                    .setNegativeButton(R.string.book, (dialogInterface, i) -> {
                        parking.setTimeIn(DateUtils.now());
                        parking.setStatus(Parking.Status.BOOKED);
                        dialogInterface.dismiss();
                        saveParking(parking, position);
                        Toast.makeText(this,"Booked. booking will expire in "+mins+" minutes",Toast.LENGTH_SHORT).show();
                    })
                    .setNeutralButton(R.string.close, (dialogInterface, i) -> {
                        dialogInterface.cancel();
                    }).show();

        } else if (parking1.getVehicleNo().equals(vehicleNo) && parking1.getPlaceId().equals(placeId)) {
            Parking parking = parking1;
            if (Parking.Status.BOOKED.equals(parking.getStatus())) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(place.getName())
                        .setMessage("Slot: " + (position + 1) + "\nVehicle No: " + vehicleNo)
                        .setPositiveButton(R.string.park_in, (dialogInterface, i) -> {
                            parking.setTimeIn(DateUtils.now());
                            parking.setStatus(Parking.Status.IN);
                            dialogInterface.dismiss();
                            saveParking(parking, position);
                        })
                        .setNegativeButton(R.string.cancel_booking, (dialogInterface, i) -> {
                            parking.setTimeOut(DateUtils.now());
                            parking.setStatus(Parking.Status.CANCELED);
                            dialogInterface.dismiss();
                            saveParking(parking, position);
                        })
                        .setNeutralButton(R.string.close, (dialogInterface, i) -> {
                            dialogInterface.cancel();
                        }).show();
            } else if (Parking.Status.IN.equals(parking.getStatus())) {
                int diffHr = (int)((new Date().getTime()-DateUtils.stringToDate(parking.getTimeIn()).getTime()) / (android.text.format.DateUtils.HOUR_IN_MILLIS));
                diffHr=diffHr==0?1:diffHr;
                int amount=diffHr*place.getRate();
                new MaterialAlertDialogBuilder(ParkingActivity.this)
                        .setTitle(place.getName())
                        .setMessage("Slot: " + (position + 1) + "\nVehicle No: " + vehicleNo+"\n Parked Time: "+parking.getTimeIn()
                                +"\nParking charge:"+amount)
                        .setPositiveButton(R.string.park_out, (dialogInterface, i) -> {
                            parking.setTimeOut(DateUtils.now());
                            parking.setStatus(Parking.Status.OUT);
                            saveParking(parking, position);
                            dialogInterface.dismiss();
                            try{
                                Uri uri =new Uri.Builder().scheme("upi").authority("pay").appendQueryParameter("cu", "INR")
                                                .appendQueryParameter("pa", place.getUpiId())
                                                .appendQueryParameter("pn", place.getName())
                                                .appendQueryParameter("tr", parking.getParkingId())
                                                .appendQueryParameter("tn", getString(R.string.app_name))
                                                .appendQueryParameter("am", ""+amount)
                                                .build();
                                startActivity(new Intent(Intent.ACTION_VIEW,uri));
                            }catch (Exception ex){
                                ex.printStackTrace();
                                new MaterialAlertDialogBuilder(ParkingActivity.this)
                                        .setCancelable(false)
                                        .setTitle(place.getName())
                                        .setMessage(R.string.make_cash_payment)
                                        .setNeutralButton(R.string.i_pay, (dialog, which) -> {
                                            dialogInterface.cancel();
                                            finish();
                                        }).show();
                            }
                        })
                        .setNeutralButton(R.string.close, (dialogInterface, i) -> {
                            dialogInterface.cancel();
                        }).show();
            }
        }else{
            new MaterialAlertDialogBuilder(this)
                    .setTitle(place.getName())
                    .setMessage("Slot: " + (position + 1) + "\nVehicle No: " + parking1.getVehicleNo()+"\nStatus:"+parking1.getStatus())
                    .setNeutralButton(R.string.close, (dialogInterface, i) -> {
                        dialogInterface.cancel();
                    }).show();
        }
    }

    private void saveParking(Parking parking, int position) {
        DatabaseReference db = null;
        if (parking.getParkingId() == null) {
            db = FirebaseDatabase.getInstance().getReference("parking").push();
            parking.setParkingId(db.getKey());
        } else {
            db = FirebaseDatabase.getInstance().getReference("parking/" + parking.getParkingId());
        }
        db.setValue(parking).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (Parking.Status.BOOKED.equals(parking.getStatus()) || Parking.Status.IN.equals(parking.getStatus())) {
                    FirebaseDatabase.getInstance().getReference("places/" + placeId + "/slots/" + position).setValue(parking.getParkingId());
                    parkingAdapter.setItem(position, parking.getParkingId());
                } else {
                    FirebaseDatabase.getInstance().getReference("places/" + placeId + "/slots/" + position).setValue("");
                    parkingAdapter.setItem(position, "");
                }

            }
        });
        refreshExisting();
    }

    private void loadPlace() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            Toast.makeText(this, "Invalid Inputs, Try Again", Toast.LENGTH_SHORT).show();
            finish();
        }
        placeId = bundle.getString(PLACE_ID, "");
        mobile = bundle.getString(MOBILE, "");
        vehicleNo = bundle.getString(VEHICLE_NO, "");
        if (placeId.isEmpty() || mobile.isEmpty() || vehicleNo.isEmpty()) {
            Toast.makeText(this, "Invalid Inputs, Try Again", Toast.LENGTH_SHORT).show();
            finish();
        }
        swipeRefresh.setRefreshing(true);
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("places/" + placeId);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    place = snapshot.getValue(Place.class);
                    parkingAdapter.setItems(place.getSlots());
                } else {
                    Toast.makeText(getApplicationContext(), R.string.no_places, Toast.LENGTH_SHORT).show();
                }
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage() + "", Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
            }
        });
        refreshExisting();

    }
    private void refreshExisting(){
        FirebaseDatabase.getInstance().getReference("parking").orderByChild("vehicleNo").equalTo(vehicleNo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingExist=false;parkingExist=false;exitSlot="";
                for(DataSnapshot snap:snapshot.getChildren()){
                    Parking p=snap.getValue(Parking.class);
                    if(Parking.Status.BOOKED.equalsIgnoreCase(p.getStatus())) {
                        bookingExist = true;
                        exitSlot=(Integer.valueOf(p.getSlotId())+1)+"";
                    }if(Parking.Status.IN.equalsIgnoreCase(p.getStatus())) {
                        parkingExist = true;
                        exitSlot=(Integer.valueOf(p.getSlotId())+1)+"";
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void bookingExpire(){
        FirebaseDatabase.getInstance().getReference("parking").orderByChild("status").equalTo(Parking.Status.BOOKED).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long oneHr=1*60*60+1000;
                for(DataSnapshot snap:snapshot.getChildren()){
                    Parking p=snap.getValue(Parking.class);
                    if(new Date().getTime()-DateUtils.stringToDate(p.getTimeIn()).getTime()>oneHr) {
                        p.setStatus(Parking.Status.CANCELED);
                        snap.getRef().setValue(p);
                        FirebaseDatabase.getInstance().getReference("places/"+p.getPlaceId()+"/slots/"+p.getSlotId()).setValue("");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}