package com.example.prm392fe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392fe.R;
import com.example.prm392fe.models.responses.LocationResponse;

import java.util.List;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
    String TAG = "LOCATION_ADAP";
    List<LocationResponse> locations;
    OnItemClickListener listener;

    public LocationAdapter(List<LocationResponse> list) {
        this.locations = list;
        notifyDataSetChanged();
    }

    public void setLocations(List<LocationResponse> newLocationList) {
        this.locations = newLocationList;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(LocationResponse location);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationResponse locationResponse = locations.get(position);
        holder.tvShopName.setText("Gundam Shop");
        holder.tvAddress.setText(locationResponse.getAddress());
        holder.tvDistance.setText("16km");
        Glide.with(holder.itemView.getContext())
                .load("https://tse3.mm.bing.net/th/id/OIP.VOm2DzMd_71Cl4rDWx8x0wHaFj?pid=Api&P=0&h=220")
                .into(holder.ivShopImage);

        holder.itemView.setOnClickListener(v -> {
            if(listener != null) {
                listener.onItemClick(locationResponse);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvShopName, tvAddress, tvDistance;
        ImageView ivShopImage;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvShopName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            ivShopImage = itemView.findViewById(R.id.ivShopImage);
        }
    }
}
