package com.app.smartparking;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.app.smartparking.model.Place;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AddPlaceActivity extends AppCompatActivity {
    public final static String PLACE_DATA = "placeData";
    private final String REGEX_UPI="^[0-9A-Za-z.-]{2,256}@[A-Za-z]{2,64}$";
    private TextInputLayout tLat, tLng, tName, tAddress, tMobile, tSlot,tRate,tUpiId;
    private AppCompatButton bSave;
    private Place place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        tLat = findViewById(R.id.tLat);
        tLng = findViewById(R.id.tLng);
        tName = findViewById(R.id.tName);
        tAddress = findViewById(R.id.tAddress);
        tMobile = findViewById(R.id.tMobile);
        tSlot = findViewById(R.id.tSlot);
        tRate = findViewById(R.id.tRate);
        tUpiId = findViewById(R.id.tUpiId);
        bSave = findViewById(R.id.bSave);
        place = getIntent().hasExtra(PLACE_DATA) ? (Place) getIntent().getSerializableExtra(PLACE_DATA) : null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (place == null) {
            bSave.setText(R.string.register);
            setTitle(R.string.add_place);
        } else {
            tLat.getEditText().setText(place.getLatitude() + "");
            tLng.getEditText().setText(place.getLongitude() + "");
            tName.getEditText().setText(place.getName() + "");
            tAddress.getEditText().setText(place.getAddress() + "");
            tMobile.getEditText().setText(place.getMobile() + "");
            tSlot.getEditText().setText(place.getSlotCount() + "");
            tRate.getEditText().setText(place.getRate() + "");
            tUpiId.getEditText().setText(place.getUpiId() + "");
            bSave.setText(R.string.save);
            setTitle(R.string.edit_place);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_add_place, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_delete).setVisible(place != null);
        menu.findItem(R.id.action_qrcode).setVisible(place != null);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            deletePlace(place);
        } else if (item.getItemId() == R.id.action_qrcode) {
            Intent it = new Intent(getApplicationContext(), QRCodeViewActivity.class);
            it.putExtra(QRCodeViewActivity.PLACE_DATA, place);
            startActivity(it);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deletePlace(Place place) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("places/" + place.getPlaceId());
        db.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent data = new Intent();
                data.putExtra(PLACE_DATA, place);
                setResult(RESULT_CANCELED, data);
            }
            finish();
        });
    }

    private void createPlace(Place place) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("places").push();
        place.setPlaceId(db.getKey());
        List<String> slots = place.getSlots();
        for (int i = 0; i < place.getSlotCount(); i++) {
            slots.add("");
        }
        place.setSlots(slots);
        db.setValue(place).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), R.string.register_success, Toast.LENGTH_SHORT).show();
                Intent data = new Intent();
                data.putExtra(PLACE_DATA, place);
                setResult(RESULT_FIRST_USER, data);
                finish();
                Intent it = new Intent(getApplicationContext(), QRCodeViewActivity.class);
                it.putExtra(QRCodeViewActivity.PLACE_DATA, place);
                startActivity(it);
            } else
                Toast.makeText(getApplicationContext(), R.string.register_failed, Toast.LENGTH_SHORT).show();
        });
    }

    private void updatePlace(Place place) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("places/" + place.getPlaceId());
        List<String> slots = place.getSlots();
        for (int i = 0; i < place.getSlotCount(); i++) {
            try {
                slots.get(i);
            } catch (IndexOutOfBoundsException ex) {
                slots.add("");
            }
        }
        for (int i = slots.size() - 1; i >= place.getSlotCount(); i--) {
            slots.remove(i);
        }
        place.setSlots(slots);
        db.setValue(place).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), R.string.saved, Toast.LENGTH_SHORT).show();
                Intent data = new Intent();
                data.putExtra(PLACE_DATA, place);
                setResult(RESULT_OK, data);
                finish();
            } else
                Toast.makeText(getApplicationContext(), R.string.failed, Toast.LENGTH_SHORT).show();
        });
    }

    public void saveOnClick(View view) {
        String lat = tLat.getEditText().getText().toString().trim();
        String lng = tLng.getEditText().getText().toString().trim();
        String name = tName.getEditText().getText().toString().trim();
        String address = tAddress.getEditText().getText().toString().trim();
        String mobile = tMobile.getEditText().getText().toString().trim().replaceAll("[^0-9+]", "");
        String slots = tSlot.getEditText().getText().toString().trim();
        String rate = tRate.getEditText().getText().toString().trim();
        String upiId = tUpiId.getEditText().getText().toString().trim();
        if (lat.isEmpty() || lng.isEmpty() || name.isEmpty() || address.isEmpty() || mobile.isEmpty() || slots.isEmpty() || rate.isEmpty()||upiId.isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.enter_all_value, Toast.LENGTH_SHORT).show();
        }else if(mobile.length()!=10){
            Toast.makeText(getApplicationContext(), R.string.mobile_10_only, Toast.LENGTH_SHORT).show();
        } else {
            place = place == null ? new Place() : place;
            place.setMobile(mobile);
            place.setLatitude(Double.parseDouble(lat));
            place.setLongitude(Double.parseDouble(lng));
            place.setName(name);
            place.setAddress(address);
            place.setSlotCount(Integer.parseInt(slots));
            place.setRate(Integer.parseInt(rate));
            place.setUpiId(upiId);
            if (place.getPlaceId() == null)
                createPlace(place);
            else updatePlace(place);
        }
    }
}