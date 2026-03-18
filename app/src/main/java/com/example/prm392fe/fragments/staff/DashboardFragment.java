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

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Quan sát LiveData
        observeViewModel();

        // Load dữ liệu từ API
        viewModel.loadDashboardData();

        // Nút xem chi tiết Dashboard
        binding.btnViewDetails.setOnClickListener(v ->
                Toast.makeText(getContext(), "Mở chi tiết Dashboard", Toast.LENGTH_SHORT).show()
        );

        return binding.getRoot();
    }

    private void observeViewModel() {
        viewModel.getTodayOrders().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.tvTodayOrders.setText("Tổng đơn hôm nay: " + count);
            } else {
                binding.tvTodayOrders.setText("Tổng đơn hôm nay: 0");
            }
        });

        viewModel.getPendingOrders().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.tvPendingOrders.setText("Đơn đang chờ xử lý: " + count);
            } else {
                binding.tvPendingOrders.setText("Đơn đang chờ xử lý: 0");
            }
        });

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
