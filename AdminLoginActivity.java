package com.app.smartparking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AdminLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
    }

    public void loginOnClick(View view) {
        TextInputLayout tUsername = findViewById(R.id.tUsername);
        TextInputLayout tPassword = findViewById(R.id.tPassword);
        String username = tUsername.getEditText().getText().toString().trim();
        String password = tPassword.getEditText().getText().toString().trim();
        tUsername.setError(username.isEmpty() ? tUsername.getErrorContentDescription() : null);
        tPassword.setError(password.isEmpty() ? tPassword.getErrorContentDescription() : null);
        if (username.isEmpty() || password.isEmpty()) return;
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("admin");
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, String> adminData = snapshot.getValue(new GenericTypeIndicator<HashMap<String, String>>() {
                });
                if (username.equals(adminData.get("username")) && password.equals(adminData.get("password"))) {
                    startActivity(new Intent(getApplicationContext(), PlaceListActivity.class));
                } else
                    Toast.makeText(getApplicationContext(), R.string.wrong_credentials, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage() + "", Toast.LENGTH_SHORT).show();
            }
        });
    }
}