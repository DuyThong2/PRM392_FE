package com.example.prm392fe.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392fe.R;
import com.example.prm392fe.models.responses.CartItemResponse;
import com.example.prm392fe.repositories.NotificationRepository;

import java.text.DecimalFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItemResponse> cartItems;
    private Context context;
    private OnCartItemListener listener;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private NotificationRepository notificationRepository;

    public interface OnCartItemListener {
        void onQuantityChanged(CartItemResponse item, int newQuantity);
        void onRemoveItem(CartItemResponse item);
    }

    public CartAdapter(Context context, NotificationRepository notificationRepository) {
        this.context = context;
    }

    public void setData(List<CartItemResponse> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    public void setOnCartItemListener(OnCartItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItemResponse item = cartItems.get(position);
        holder.bind(item);

    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName, tvProductPrice, tvQuantity, tvTotalItemPrice;
        private ImageButton btnDecreaseQuantity, btnIncreaseQuantity, btnRemoveItem;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotalItemPrice = itemView.findViewById(R.id.tvTotalItemPrice);
            btnDecreaseQuantity = itemView.findViewById(R.id.btnDecreaseQuantity);
            btnIncreaseQuantity = itemView.findViewById(R.id.btnIncreaseQuantity);
            btnRemoveItem = itemView.findViewById(R.id.btnRemoveItem);
        }

        public void bind(CartItemResponse item) {
            // Set product name
            tvProductName.setText(item.getProductName());

            // Set product price
            tvProductPrice.setText(decimalFormat.format(item.getProductPrice()) + "₫");

            // Set quantity
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            // Calculate and set total item price
            double totalItemPrice = item.getProductPrice() * item.getQuantity();
            tvTotalItemPrice.setText(decimalFormat.format(totalItemPrice) + "₫");

            // Load product image
            if (item.getProductImage() != null && !item.getProductImage().isEmpty()) {
                Glide.with(context)
                        .load(item.getProductImage())
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .centerCrop()
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_placeholder);
            }

            // Set up click listeners
            btnIncreaseQuantity.setOnClickListener(v -> {
                if (listener != null) {
                    int newQuantity = item.getQuantity() + 1;
                    listener.onQuantityChanged(item, newQuantity);
                }
            });

            btnDecreaseQuantity.setOnClickListener(v -> {
                if (listener != null) {
                    int currentQuantity = item.getQuantity();
                    if (currentQuantity > 1) {
                        int newQuantity = currentQuantity - 1;
                        listener.onQuantityChanged(item, newQuantity);
                    }
                }
            });

            btnRemoveItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(item);
                }
            });

            // Disable decrease button if quantity is 1
            btnDecreaseQuantity.setEnabled(item.getQuantity() > 1);
            btnDecreaseQuantity.setAlpha(item.getQuantity() > 1 ? 1.0f : 0.5f);
        }
    }
}

