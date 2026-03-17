package com.example.prm392fe.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392fe.R;
import com.example.prm392fe.models.responses.OrderItemResponse;
import com.example.prm392fe.models.responses.OrderResponse;

import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.VH> {
    private Context context;
    private List<OrderItemResponse> list;

    public interface OnItemClickListener {
        void onDetailClick(OrderResponse item);
    }

    public OrderDetailAdapter(Context context, List<OrderItemResponse> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        OrderItemResponse item = list.get(position);
        holder.txtProductName.setText(item.getProductName());

        String label = "Số lượng: ";
        String qty = String.valueOf(item.getQuantity());
        SpannableString qtyText = new SpannableString(label + qty);
        qtyText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        qtyText.setSpan(new ForegroundColorSpan(Color.RED), label.length(), qtyText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.txtProductQty.setText(qtyText);

        var img = item.getProductImage();
        if (img != null && !img.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(img)
                    .override(500, 500) // fix size 200x200 pixel
                    .centerCrop()       // cắt giữa hình để không méo
                    .into(holder.imgProduct);
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtProductName, txtProductQty;

        public VH(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductQty = itemView.findViewById(R.id.txtProductQty);
        }
    }
}
