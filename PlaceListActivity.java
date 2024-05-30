package com.app.smartparking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.smartparking.adapter.PlaceAdapter;
import com.app.smartparking.model.Place;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PlaceListActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private PlaceAdapter placeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_list);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);
        placeAdapter = new PlaceAdapter();
        placeAdapter.setOnItemClickedListener(place -> {
            int position = placeAdapter.getItems().indexOf(place);
            Intent it = new Intent(getApplicationContext(), AddPlaceActivity.class);
            it.putExtra(AddPlaceActivity.PLACE_DATA, place);
            startActivityForResult(it, 100);
            recyclerView.setTag(position);
        });
        recyclerView.setAdapter(placeAdapter);
        swipeRefresh.setOnRefreshListener(() -> loadPlaces());
        loadPlaces();
    }

    private void loadPlaces() {
        swipeRefresh.setRefreshing(true);
        placeAdapter.removeAll();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("places");
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        placeAdapter.addItem(child.getValue(Place.class));
                    }
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
    }

    public void fabAddOnClick(View view) {
        startActivityForResult(new Intent(getApplicationContext(), AddPlaceActivity.class), 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null && data.hasExtra(AddPlaceActivity.PLACE_DATA)) {
            Place place = (Place) data.getSerializableExtra(AddPlaceActivity.PLACE_DATA);
            if (resultCode == RESULT_FIRST_USER) {
                placeAdapter.addItem(place);
            } else if (resultCode == RESULT_CANCELED) {
                placeAdapter.removeItem(placeAdapter.getItems().indexOf(place));
            } else if (resultCode == RESULT_OK) {
                placeAdapter.setItem(Integer.parseInt(recyclerView.getTag() + ""), place);
            }
        }
    }
}