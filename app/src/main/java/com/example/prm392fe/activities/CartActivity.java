package com.example.prm392fe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392fe.R;
import com.example.prm392fe.SessionManager;
import com.example.prm392fe.adapter.CartAdapter;
import com.example.prm392fe.models.responses.CartResponse;
import com.example.prm392fe.repositories.NotificationRepository;
import com.example.prm392fe.utils.AppStompClient;
import com.example.prm392fe.viewModel.CartViewModel;

import java.text.DecimalFormat;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemListener {

    private static final String TAG = "CartActivity";

    private CartViewModel cartViewModel;
    private SessionManager sessionManager;
    private CartAdapter cartAdapter;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");

    // Views
    private ImageView ivBack;
    private RecyclerView rvCartItems;
    private TextView tvTotalPrice;
    private Button btnClearCart, btnCheckout, btnContinueShopping;
    private LinearLayout llEmptyCart, llCartContent;
    private ProgressBar progressBar;

    private CartResponse currentCart;
    private NotificationRepository notificationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        AppStompClient stompClient = AppStompClient.getInstance(SessionManager.getInstance(this).getAuthToken());
        String userId = SessionManager.getInstance(this).getUserId();
        notificationRepository = new NotificationRepository(stompClient, userId, this);

        // Đảm bảo layout tránh vùng camera và status bar
        View rootView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            // Lấy kích thước phần đệm của hệ thống (status bar, camera, navigation bar)
            var systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Thêm padding tương ứng để tránh chồng lên vùng notch/camera
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerView();
        setupClickListeners();

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        sessionManager = SessionManager.getInstance(this);

        observeData();
        loadCart();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        rvCartItems = findViewById(R.id.rvCartItems);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnClearCart = findViewById(R.id.btnClearCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);
        llEmptyCart = findViewById(R.id.llEmptyCart);
        llCartContent = findViewById(R.id.llCartContent);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this, notificationRepository);
        cartAdapter.setOnCartItemListener(this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnContinueShopping.setOnClickListener(v -> {
            // Navigate to main activity or product list
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnClearCart.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng xóa tất cả chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        btnCheckout.setOnClickListener(v -> proceedToCheckout());
    }

    private void observeData() {
        cartViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        cartViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCart() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userIdStr = sessionManager.getUserId();
        if (userIdStr == null) {
            Toast.makeText(this, "Lỗi lấy thông tin user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            int userId = Integer.parseInt(userIdStr);
            cartViewModel.setLoading(true);

            cartViewModel.getCartsByUserId(userId).observe(this, cartResponse -> {
                cartViewModel.setLoading(false);

                if (cartResponse != null) {
                    currentCart = cartResponse;
                    updateUI(cartResponse);
                    Log.d(TAG, "✅ Cart loaded successfully with " +
                            (cartResponse.getItems() != null ? cartResponse.getItems().size() : 0) + " items");
                } else {
                    showEmptyCart();
                    Log.w(TAG, "⚠️ No cart data received");
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Lỗi định dạng user ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateUI(CartResponse cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            showEmptyCart();
        } else {
            showCartContent(cart);
        }
    }

    private void showEmptyCart() {
        llEmptyCart.setVisibility(View.VISIBLE);
        llCartContent.setVisibility(View.GONE);
    }

    private void showCartContent(CartResponse cart) {
        llEmptyCart.setVisibility(View.GONE);
        llCartContent.setVisibility(View.VISIBLE);

        cartAdapter.setData(cart.getItems());

        // Update total price
        if (cart.getTotalPrice() != null) {
            tvTotalPrice.setText(decimalFormat.format(cart.getTotalPrice().doubleValue()) + "₫");
        } else {
            tvTotalPrice.setText("0₫");
        }
    }



    private void proceedToCheckout() {
        if (currentCart == null || currentCart.getItems() == null || currentCart.getItems().isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to checkout activity
        Intent intent = new Intent(this, CheckoutActivity.class);
        // You can pass cart data if needed
        // intent.putExtra("cart_id", currentCart.getCartId());
        startActivity(intent);
    }

    @Override
    public void onQuantityChanged(CartItemResponse item, int newQuantity) {
        if (currentCart == null) return;

        String userIdStr = sessionManager.getUserId();
        if (userIdStr == null) {
            Toast.makeText(this, "Lỗi lấy thông tin user", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int userId = Integer.parseInt(userIdStr);

            // Create updated items list
            java.util.List<com.huyntd.superapp.gundamshop_mobilefe.models.request.UpdateCartRequest.CartItemRequest> items = new java.util.ArrayList<>();

            for (CartItemResponse cartItem : currentCart.getItems()) {
                int quantity = cartItem.getProductId() == item.getProductId() ? newQuantity : cartItem.getQuantity();
                items.add(com.huyntd.superapp.gundamshop_mobilefe.models.request.UpdateCartRequest.CartItemRequest.builder()
                        .productId(cartItem.getProductId())
                        .quantity(quantity)
                        .build());
            }

            cartViewModel.setLoading(true);
            cartViewModel.updateCart(userId, currentCart.getCartId(), items).observe(this, updatedCart -> {
                cartViewModel.setLoading(false);
                if (updatedCart != null) {
                    currentCart = updatedCart;
                    updateUI(updatedCart);
                    Toast.makeText(this, "Đã cập nhật số lượng", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Không thể cập nhật số lượng", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Lỗi định dạng user ID", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRemoveItem(CartItemResponse item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc chắn muốn xóa \"" + item.getProductName() + "\" khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    String userIdStr = sessionManager.getUserId();
                    if (userIdStr == null) {
                        Toast.makeText(this, "Lỗi lấy thông tin user", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        int userId = Integer.parseInt(userIdStr);
                        cartViewModel.setLoading(true);

                        cartViewModel.removeFromCart(userId, item.getProductId()).observe(this, success -> {
                            cartViewModel.setLoading(false);
                            if (success != null && success) {
                                Toast.makeText(this, "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                                loadCart(); // Refresh cart
                            } else {
                                Toast.makeText(this, "Không thể xóa sản phẩm", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Lỗi định dạng user ID", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh cart data when returning to this activity
        loadCart();
    }
}

