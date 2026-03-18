package com.example.prm392fe.fragments.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392fe.adapter.StaffOrderAdapter;
import com.example.prm392fe.databinding.FragmentQuickOrderBinding;
import com.example.prm392fe.viewModel.QuickOrderViewModel;

public class QuickOrderFragment extends Fragment {

    private FragmentQuickOrderBinding binding;
    private QuickOrderViewModel viewModel;
    private StaffOrderAdapter adapter;
    private boolean isUserScrolling = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQuickOrderBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(QuickOrderViewModel.class);

        setupRecyclerView();
        observeViewModel();
        setupScrollListener();

        viewModel.loadTodayOrders(true);

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new StaffOrderAdapter(requireContext(), item -> {
            // Nếu muốn xử lý click tại đây
        });
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrders.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getTodayOrders().observe(getViewLifecycleOwner(), orders -> {
            adapter.setData(orders);
            binding.tvEmpty.setVisibility(
                    (orders == null || orders.isEmpty()) ? View.VISIBLE : View.GONE
            );
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE)
        );
    }

    private void setupScrollListener() {
        binding.rvOrders.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                isUserScrolling = newState == RecyclerView.SCROLL_STATE_DRAGGING;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) return;

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (isUserScrolling && !Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
                            && firstVisibleItemPosition >= 0) {
                        viewModel.loadNextPage();
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
