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

import com.example.prm392fe.R;
import com.example.prm392fe.activities.OrderDetailStaffActivity;
import com.example.prm392fe.models.responses.OrderItemResponse;
import com.example.prm392fe.models.responses.OrderResponse;

import java.util.ArrayList;
import java.util.List;

public class StaffOrderAdapter extends RecyclerView.Adapter<StaffOrderAdapter.VH> {

    private final Context context;
    private final List<OrderResponse> list = new ArrayList<>();

    public interface OnItemClickListener {
        void onDetailClick(OrderResponse item);
    }

    private final OnItemClickListener listener;

    public StaffOrderAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setData(List<OrderResponse> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        notifyDataSetChanged();
    }

    public void addData(List<OrderResponse> moreList) {
        if (moreList == null || moreList.isEmpty()) return;
        int oldSize = list.size();
        list.addAll(moreList);
        notifyItemRangeInserted(oldSize, moreList.size());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.staff_item_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        OrderResponse item = list.get(position);
        holder.tvName.setText("Mã đơn hàng" + String.valueOf(item.getId()));
        // 🧩 Lấy sản phẩm đầu tiên (nếu có)
        if (item.getOrderItems() != null && !item.getOrderItems().isEmpty()) {
            OrderItemResponse first = item.getOrderItems().get(0);
//            holder.tvName.setText(first.getProductName());
        } else {
//            holder.tvName.setText("(Không có sản phẩm)");
            holder.img.setImageResource(R.drawable.no_image);
        }

        holder.tvDate.setText(item.getOrderDate());
        holder.tvStatus.setText(item.getStatus());
        holder.tvPrice.setText(String.format("%,.0fđ", item.getTotalPrice()));

        // 🎨 Màu viền trạng thái
        int color = Color.parseColor("#FFD700"); // vàng mặc định
        String status = item.getStatus() == null ? "" : item.getStatus().toUpperCase();
        if (status.equals("CANCELLED") || status.equals("RETURNED")) {
            color = Color.parseColor("#FF0000");
        } else if (status.equals("DELIVERED")) {
            color = Color.parseColor("#008000");
        }

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(20);
        bg.setStroke(3, color);
        bg.setColor(Color.TRANSPARENT);
        holder.tvStatus.setTextColor(color);
        holder.tvStatus.setBackground(bg);

        holder.btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gọi listener nếu cần (ví dụ để xử lý callback)
                if (listener != null) {
                    listener.onDetailClick(item);
                }

                // Tạo Intent để mở OrderDetailStaffActivity
                Intent intent = new Intent(v.getContext(), OrderDetailStaffActivity.class);
                intent.putExtra("orderId", item.getId()); // truyền id của đơn hàng
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
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

