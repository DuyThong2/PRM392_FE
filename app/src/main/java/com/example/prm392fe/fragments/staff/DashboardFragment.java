package com.example.prm392fe.fragments.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.prm392fe.databinding.FragmentDashboardBinding;
import com.example.prm392fe.viewModel.DashboardViewModel;
import com.example.prm392fe.viewModel.OrderViewModel;
import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.PageResponse;
import com.example.prm392fe.models.responses.OrderResponse;

import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private OrderViewModel orderViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        // Quan sát LiveData
        observeViewModel();

        // Load dữ liệu từ API
        // 1. Tổng đơn hôm nay (không lọc status)
        orderViewModel.fetchOrdersToday(0, 1000, "id", "desc", "PENDING");
        // 2. Đơn hàng PENDING hôm nay
        orderViewModel.fetchOrdersToday(0, 1000, "id", "desc", "PENDING");

        // Nút xem chi tiết Dashboard
        binding.btnViewDetails.setOnClickListener(v ->
                Toast.makeText(getContext(), "Mở chi tiết Dashboard", Toast.LENGTH_SHORT).show()
        );

        return binding.getRoot();
    }

    private void observeViewModel() {
        // Quan sát tổng đơn hôm nay (không lọc status)
        orderViewModel.getAllOrdersToday().observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isSuccess()) {
                PageResponse<OrderResponse> pageResponse = apiResponse.getResult();
                if (pageResponse != null && pageResponse.getContent() != null) {
                    binding.tvTodayOrders.setText("Tổng đơn hôm nay: " + pageResponse.getContent().size());
                } else {
                    binding.tvTodayOrders.setText("Tổng đơn hôm nay: 0");
                }
            } else {
                binding.tvTodayOrders.setText("Tổng đơn hôm nay: 0");
            }
        });

        // Quan sát đơn hàng PENDING hôm nay
        orderViewModel.getOrdersToday().observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isSuccess()) {
                PageResponse<OrderResponse> pageResponse = apiResponse.getResult();
                if (pageResponse != null && pageResponse.getContent() != null) {
                    binding.tvPendingOrders.setText("Đơn đang chờ xử lý: " + pageResponse.getContent().size());
                } else {
                    binding.tvPendingOrders.setText("Đơn đang chờ xử lý: 0");
                }
            } else {
                binding.tvPendingOrders.setText("Đơn đang chờ xử lý: 0");
            }
        });

        // Các thông tin khác vẫn từ DashboardViewModel
        viewModel.getLowStock().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.tvLowStock.setText("Sản phẩm sắp hết: " + count);
            } else {
                binding.tvLowStock.setText("Sản phẩm sắp hết: 0");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Giải phóng reference
    }
}