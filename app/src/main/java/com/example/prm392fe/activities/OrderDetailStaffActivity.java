package com.example.prm392fe.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392fe.R;
import com.example.prm392fe.adapter.OrderDetailAdapter;
import com.example.prm392fe.enums.OrderStatus;
import com.example.prm392fe.mapper.StatusMapper;
import com.example.prm392fe.models.responses.OrderItemResponse;
import com.example.prm392fe.models.responses.OrderResponse;
import com.example.prm392fe.viewModel.OrderViewModel;
import com.example.prm392fe.viewModel.QuickOrderViewModel;

import java.util.List;

public class OrderDetailStaffActivity extends AppCompatActivity {

    private TextView tvStatus, tvOrderCode, tvOrderDate;
    private TextView tvTotalPrice, tvPaymentMethod, tvShipping, tvFinalPrice;
    private TextView tvCustomerName, tvCustomerEmail, tvCustomerPhone, tvCustomerAddress;

    private Spinner spStatus;
    private ImageButton btnEdit;
    private boolean isEditing = false;

    private RecyclerView rvOrderItems;
    private OrderDetailAdapter orderDetailAdapter;

    private OrderViewModel orderViewModel;
    private QuickOrderViewModel quickOrderViewModel;

    private int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_staff);

        // Bind views
        tvOrderCode = findViewById(R.id.tvOrderCode);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvStatus = findViewById(R.id.tvStatus);
        spStatus = findViewById(R.id.spStatus);
        btnEdit = findViewById(R.id.btnEditStatus);

        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvShipping = findViewById(R.id.tvShipping);
        tvFinalPrice = findViewById(R.id.tvFinalPrice);

        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCustomerEmail = findViewById(R.id.tvCustomerEmail);
        tvCustomerPhone = findViewById(R.id.tvCustomerPhone);
        tvCustomerAddress = findViewById(R.id.tvCustomerAddress);

        rvOrderItems = findViewById(R.id.rvOrderItems);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        orderDetailAdapter = new OrderDetailAdapter(this, null);
        rvOrderItems.setAdapter(orderDetailAdapter);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish(); // Quay về Activity trước
        });

        // Lấy orderId từ Intent
        orderId = getIntent().getIntExtra("orderId", -1);
        Log.d("OrderDetailStaff", "Loading orderId: " + orderId);

        // Khởi tạo ViewModel
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        quickOrderViewModel = new ViewModelProvider(this).get(QuickOrderViewModel.class);

        // Load chi tiết order
        // Lấy orderId từ intent
        int orderId = getIntent().getIntExtra("orderId", -1);

        // Quan sát LiveData từ ViewModel
        OrderViewModel orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        orderViewModel.getOrderDetail(orderId).observe(this, order -> {
            if (order != null) {
                // Bind dữ liệu vào UI
                bindOrderData(order);
            }
        });

        // Nút chỉnh sửa trạng thái
        btnEdit.setOnClickListener(v -> {
            if (!isEditing) {
                // Bật chế độ chỉnh sửa
                tvStatus.setVisibility(View.GONE);
                spStatus.setVisibility(View.VISIBLE);
                btnEdit.setImageResource(android.R.drawable.ic_menu_save);
                isEditing = true;
            } else {
                // Lấy giá trị spinner (tiếng Việt)
                String selectedVN = spStatus.getSelectedItem().toString();
                String enumStatus = String.valueOf(StatusMapper.toEnum(selectedVN)); // Chuyển về enum server cần

                // Cập nhật chi tiết order qua OrderViewModel
                quickOrderViewModel.updateOrderStatus(orderId, StatusMapper.toEnum(selectedVN));

                // Đồng thời refresh danh sách tổng quát hôm nay
                quickOrderViewModel.refresh();

                // Hiển thị lại textView
                tvStatus.setText(selectedVN);
                tvStatus.setTextColor(getStatusColor(enumStatus));
                tvStatus.setVisibility(View.VISIBLE);
                spStatus.setVisibility(View.GONE);
                btnEdit.setImageResource(android.R.drawable.ic_menu_edit);
                isEditing = false;
            }
        });
    }

    private void bindOrderData(OrderResponse order) {
        tvOrderCode.setText("Mã đơn hàng: " + order.getId());
        tvOrderDate.setText("Ngày tạo: " + order.getOrderDate());
        String vnStatus = StatusMapper.toVietnamese(order.getStatus());
        tvStatus.setText(vnStatus);
        tvStatus.setTextColor(getStatusColor(order.getStatus()));

        // Thông tin thanh toán
        tvTotalPrice.setText(String.format("%.0fđ", order.getTotalPrice()));
        tvPaymentMethod.setText(order.getPaymentMethod());
        tvShipping.setText("Miễn phí");
        tvFinalPrice.setText(String.format("%.0fđ", order.getTotalPrice()));

        // Thông tin khách hàng
        tvCustomerName.setText("Nguyễn Văn A");
        tvCustomerEmail.setText("nguyenvana@example.com");
        tvCustomerPhone.setText("0912 345 678");
        tvCustomerAddress.setText("123 Nguyễn Trãi, Quận 5, TP.HCM");

        // Sản phẩm
        List<OrderItemResponse> items = order.getOrderItems();
        if (items != null) {
            // Tạo adapter mới với list từ API
            orderDetailAdapter = new OrderDetailAdapter(this, items);
            rvOrderItems.setAdapter(orderDetailAdapter);
        }
    }


    private int getStatusColor(String statusEnum) {
        if (statusEnum == null) return Color.parseColor("#333333");

        switch (statusEnum) {
            case "PENDING":
                return Color.parseColor("#FF9800"); // Chờ xử lý
            case "PROCESSING":
            case "SHIPPED":
                return Color.parseColor("#FF8C00"); // Đang giao/Đang vận chuyển
            case "DELIVERED":
                return Color.parseColor("#008000"); // Đã giao hàng
            case "CANCELLED":
            case "RETURNED":
                return Color.parseColor("#E53935"); // Đã hủy / Bị trả lại
            case "REFUNDED":
                return Color.parseColor("#9C27B0"); // Đã hoàn tiền
            default:
                return Color.parseColor("#333333"); // Mặc định
        }
    }

}
