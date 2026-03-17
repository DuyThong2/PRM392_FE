package com.example.prm392fe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392fe.R;
import com.example.prm392fe.models.responses.CartItemResponse;


import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.VH> {

    private List<CartItemResponse> list;
    private Context ctx;

    public CheckoutAdapter(Context ctx, List<CartItemResponse> list) {
        this.ctx = ctx;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_checkout, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CartItemResponse p = list.get(position);
        holder.tvTitle.setText(p.getProductName());
        holder.tvPrice.setText(String.format("%,.0fđ", p.getProductPrice()));
        holder.tvQuantity.setText("Số lượng: " + String.format("%02d", p.getQuantity()));
        var img = p.getProductImage();
        if (img != null && !img.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(img)
                    .override(300, 300) // fix size 200x200 pixel
                    .centerCrop()       // cắt giữa hình để không méo
                    .into(holder.img);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvTitle, tvPrice, tvQuantity;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantityItem);
        }
    }
}
