package com.app.smartparking;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.app.smartparking.utils.AppPermissions;
import com.google.android.material.textfield.TextInputLayout;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.regex.Pattern;

public class VehicleLoginActivity extends AppCompatActivity {
    private final String PATTERN_VEHICLE="^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$";
    private TextInputLayout tVehicleNo,tMobile;
    private String placeId="";
    private AppPermissions perms;
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                try {
                    if (result.getContents() == null) {
                        Toast.makeText(this, "QR scan cancelled", Toast.LENGTH_LONG).show();
                    } else {
                        String text = result.getContents();
                        Toast.makeText(this, "Scanned: " + text, Toast.LENGTH_LONG).show();
                        if (text.contains("http://smartparking.ai/placeId="))
                            placeId = text.split("=", 2)[1];
                        else
                            placeId = "";
                        String vehicleNo = tVehicleNo.getEditText().getText().toString().trim();
                        String mobileNo = tMobile.getEditText().getText().toString().trim();
                        Bundle bundle = new Bundle();
                        bundle.putString(ParkingActivity.VEHICLE_NO, vehicleNo);
                        bundle.putString(ParkingActivity.MOBILE, mobileNo);
                        bundle.putString(ParkingActivity.PLACE_ID, placeId);
                        Intent intent = new Intent(getApplicationContext(), ParkingActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }catch (Exception ex){ex.printStackTrace();}
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_login);
        tVehicleNo=findViewById(R.id.tVehicleNo);
        tMobile=findViewById(R.id.tMobile);
        perms=new AppPermissions(this);
        handleIntent();
    }
    private void handleIntent(){
        if(Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData()!=null){
            placeId=String.valueOf(getIntent().getData()).split("=",2)[1];
            findViewById(R.id.fabViewSlot).setVisibility(View.VISIBLE);
        }else
            findViewById(R.id.fabViewSlot).setVisibility(View.GONE);
    }
    public void viewSlot(View view){
        String vehicleNo=tVehicleNo.getEditText().getText().toString().trim();
        String mobileNo=tMobile.getEditText().getText().toString().trim().replaceAll("[^0-9+]", "");
        if(placeId==null||placeId.isEmpty())
            view.setVisibility(View.GONE);
        else if(vehicleNo.isEmpty()|| !Pattern.matches(PATTERN_VEHICLE,vehicleNo))
            Toast.makeText(this,R.string.enter_vehicle_no,Toast.LENGTH_LONG).show();
        else if(mobileNo.isEmpty()|| !Patterns.PHONE.matcher(mobileNo).matches()|| mobileNo.length()!=10)
            Toast.makeText(this,R.string.enter_mobile,Toast.LENGTH_LONG).show();
        else{
            Bundle bundle=new Bundle();
            bundle.putString(ParkingActivity.VEHICLE_NO,vehicleNo);
            bundle.putString(ParkingActivity.MOBILE,mobileNo);
            bundle.putString(ParkingActivity.PLACE_ID,placeId);
            Intent intent=new Intent(getApplicationContext(),ParkingActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }
    public void openMap(View view){
        String vehicleNo=tVehicleNo.getEditText().getText().toString().trim();
        String mobileNo=tMobile.getEditText().getText().toString().trim();
        if(vehicleNo.isEmpty()|| !Pattern.matches(PATTERN_VEHICLE,vehicleNo))
            Toast.makeText(this,R.string.enter_vehicle_no,Toast.LENGTH_LONG).show();
        else if(mobileNo.isEmpty()|| !Patterns.PHONE.matcher(mobileNo).matches()|| mobileNo.length()!=10)
            Toast.makeText(this,R.string.enter_mobile,Toast.LENGTH_LONG).show();
        else{
            Bundle bundle=new Bundle();
            bundle.putString(ParkingActivity.VEHICLE_NO,vehicleNo);
            bundle.putString(ParkingActivity.MOBILE,mobileNo);
            Intent intent=new Intent(getApplicationContext(),MapsActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public void scanQR(View view){
        String vehicleNo=tVehicleNo.getEditText().getText().toString().trim();
        String mobileNo=tMobile.getEditText().getText().toString().trim();
        if(vehicleNo.isEmpty()|| !Pattern.matches(PATTERN_VEHICLE,vehicleNo))
            Toast.makeText(this,R.string.enter_vehicle_no,Toast.LENGTH_LONG).show();
        else if(mobileNo.isEmpty()|| !Patterns.PHONE.matcher(mobileNo).matches()|| mobileNo.length()!=10)
            Toast.makeText(this,R.string.enter_mobile,Toast.LENGTH_LONG).show();
        else if(perms.hasPermission(AppPermissions.CAMERA_PERMISSION)){
            barcodeLauncher.launch(new ScanOptions());
        }else perms.requestPermission(AppPermissions.CAMERA_PERMISSION,AppPermissions.REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==AppPermissions.REQ_CODE && perms.hasPermission(permissions)){
            barcodeLauncher.launch(new ScanOptions());
        }
    }

}