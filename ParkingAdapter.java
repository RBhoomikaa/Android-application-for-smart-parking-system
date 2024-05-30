package com.app.smartparking.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.smartparking.R;
import com.app.smartparking.model.Parking;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {
    private List<String> list;
    private OnItemClickedListener listener;

    public ParkingAdapter() {
        list = new ArrayList<>();
    }

    public ParkingAdapter(List<String> list) {
        this.list = list == null ? new ArrayList<>() : list;
    }

    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ParkingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slot, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setItems(List<String> list) {
        this.list = list == null ? new ArrayList<>() : list;
        notifyDataSetChanged();
    }

    public List<String> getItems() {
        return list;
    }

    public void addItem(String s) {
        list.add(s);
        notifyItemInserted(list.size() - 1);
    }

    public void addItem(int position, String slot) {
        list.add(position, slot);
        notifyItemInserted(position);
    }

    public void setItem(int position, String s) {
        list.set(position, s);
        notifyItemChanged(position);
    }

    public String removeItem(int position) {
        String s = list.remove(position);
        notifyItemRemoved(position);
        return s;
    }

    public void removeAll() {
        list.clear();
        notifyDataSetChanged();
    }

    public String getItem(int position) {
        return list.get(position);
    }

    public void setOnItemClickedListener(OnItemClickedListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickedListener {
        void onItemClicked(Parking parking, int position);
    }

    public class ParkingViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView text1, text2;

        public ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(R.id.text1);
            text2 = itemView.findViewById(R.id.text2);
        }

        public void bind(String parkId, int position) {
            text1.setText((position + 1) + "");
            text2.setText(R.string.available);
            text2.setTextColor(text2.getContext().getResources().getColor(R.color.teal_700));
            itemView.setTag(null);
            if (listener != null) itemView.setOnClickListener(view -> {
                Parking parking = itemView.getTag() == null ? null : (Parking) itemView.getTag();
                listener.onItemClicked(parking, position);
            });
            if (parkId != null && !parkId.isEmpty()) {
                DatabaseReference db = FirebaseDatabase.getInstance().getReference("parking/" + parkId);
                db.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Parking parking = snapshot.getValue(Parking.class);
                            itemView.setTag(parking);
                            if (parking != null && (Parking.Status.BOOKED.equals(parking.getStatus()) || Parking.Status.IN.equals(parking.getStatus()))) {
                                text2.setText(parking.getStatus());
                                text2.setTextColor(text2.getContext().getResources().getColor(R.color.purple_200));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

            }
        }
    }
}