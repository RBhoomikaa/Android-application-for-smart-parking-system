package com.app.smartparking.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.smartparking.R;
import com.app.smartparking.model.Place;

import java.util.ArrayList;
import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {
    private List<Place> list;
    private OnItemClickedListener listener;

    public PlaceAdapter() {
        list = new ArrayList<>();
    }

    public PlaceAdapter(List<Place> list) {
        this.list = list == null ? new ArrayList<>() : list;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaceViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setItems(List<Place> list) {
        this.list = list == null ? new ArrayList<>() : list;
        notifyDataSetChanged();
    }

    public List<Place> getItems() {
        return list;
    }

    public void addItem(Place place) {
        list.add(place);
        notifyItemInserted(list.size() - 1);
    }

    public void addItem(int position, Place place) {
        list.add(position, place);
        notifyItemInserted(position);
    }

    public void setItem(int position, Place place) {
        list.set(position, place);
        notifyItemChanged(position);
    }

    public Place removeItem(int position) {
        Place place = list.remove(position);
        notifyItemRemoved(position);
        return place;
    }

    public void removeAll() {
        list.clear();
        notifyDataSetChanged();
    }

    public Place getItem(int position) {
        return list.get(position);
    }

    public void setOnItemClickedListener(OnItemClickedListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickedListener {
        void onItemClicked(Place place);
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView tvName, tvMobile, tvAddress;
        AppCompatImageView ivCall, ivDirection;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvMobile = itemView.findViewById(R.id.tvMobile);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            ivCall = itemView.findViewById(R.id.ivCall);
            ivDirection = itemView.findViewById(R.id.ivDirection);
        }

        public void bind(Place place) {
            if (listener != null)
                itemView.setOnClickListener(view -> listener.onItemClicked(place));
            tvName.setText(place.getName());
            tvMobile.setText(place.getMobile());
            tvAddress.setText(place.getAddress());
            ivCall.setOnClickListener(view -> view.getContext().startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + place.getMobile()))));
            ivDirection.setOnClickListener(view -> view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + place.getLatitude() + "," + place.getLongitude()))));
        }
    }
}
