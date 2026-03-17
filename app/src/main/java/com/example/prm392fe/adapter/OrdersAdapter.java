package com.example.prm392fe.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392fe.R;
import com.example.prm392fe.activities.OrderDetailActivity;
import com.example.prm392fe.models.responses.OrderResponse;

import java.util.List;


public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.VH> {

    private Context context;
    private List<OrderResponse> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDetailClick(OrderResponse item);
    }

    public OrdersAdapter(Context context, List<OrderResponse> list,
                         OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        OrderResponse item = list.get(position);
        holder.tvName.setText(item.getOrderItems().get(0).getProductName());
        holder.tvDate.setText(item.getOrderDate()+"");
        holder.tvStatus.setText(item.getStatus());

        int color = Color.parseColor("#ccac00");
        var status = item.getStatus();
        if (status.equals("CANCELLED") || status.equals("RETURNED")) {
            color = Color.parseColor("#FF0000"); // đỏ
        } else if (status.equals("DELIVERED")) {
            color = Color.parseColor("#008000"); // xanh lá
        }

        // Tạo viền động
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(20);
        bg.setStroke(3, color);
        bg.setColor(Color.TRANSPARENT);

        holder.tvStatus.setTextColor(color);
        holder.tvStatus.setBackground(bg);
        holder.tvPrice.setText(String.format("%,.0fđ", item.getTotalPrice()));
        var img = item.getOrderItems().get(0).getProductImage();
        if (img != null && !img.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(img)
                    .override(300, 300) // fix size 200x200 pixel
                    .centerCrop()       // cắt giữa hình để không méo
                    .into(holder.img);
        }

        holder.btnDetail.setOnClickListener(v -> {
            if (listener != null)
                listener.onDetailClick(item);
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("orderId", item.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvDate, tvStatus, tvPrice;
        Button btnDetail;

        public VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img_product);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvPrice = itemView.findViewById(R.id.tv_price);
            btnDetail = itemView.findViewById(R.id.btn_detail);
        }
    }
}

