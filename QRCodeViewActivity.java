package com.app.smartparking;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.print.PrintHelper;

import com.app.smartparking.model.Place;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeViewActivity extends AppCompatActivity {
    public final static String PLACE_DATA = "placeData";
    private Place place;
    private AppCompatTextView tvTitle;
    private AppCompatImageView ivQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_view);
        tvTitle = findViewById(R.id.tvTitle);
        ivQR = findViewById(R.id.ivQR);
        place = getIntent().hasExtra(PLACE_DATA) ? (Place) getIntent().getSerializableExtra(PLACE_DATA) : null;
        if (place == null) finish();
        tvTitle.setText(place.getName());
        generateQR();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_qrcode_view, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_print) {
            print();
        }
        return super.onOptionsItemSelected(item);
    }

    private void generateQR() {
        ivQR.post(() -> {
            try {
                ivQR.setImageBitmap(new BarcodeEncoder().encodeBitmap("http://smartparking.ai/placeId=" + place.getPlaceId(), BarcodeFormat.QR_CODE, 360, 360));
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "QR code generation failed\n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void print() {
        PrintHelper printHelper = new PrintHelper(this);
        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        printHelper.printBitmap(getString(R.string.qr_code), screenShot());
    }

    private Bitmap screenShot() {
        View rootView = findViewById(R.id.container);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(),
                rootView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        rootView.draw(canvas);
        return bitmap;
    }
}