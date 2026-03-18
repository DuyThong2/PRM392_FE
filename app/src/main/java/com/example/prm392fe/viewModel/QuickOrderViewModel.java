package com.example.prm392fe.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.prm392fe.enums.OrderStatus;
import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.PageResponse;
import com.example.prm392fe.models.responses.OrderResponse;
import com.example.prm392fe.repositories.OrderRepository;

import java.util.ArrayList;
import java.util.List;

public class QuickOrderViewModel extends ViewModel {

    private static final String TAG = "QuickOrderViewModel";

    private final OrderRepository orderRepository = OrderRepository.getInstance();

    private final MutableLiveData<List<OrderResponse>> todayOrders = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLastPage = new MutableLiveData<>(false);

    private final MutableLiveData<OrderResponse> orderDetail = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private int currentPage = 0;
    private static final int PAGE_SIZE = 10;

    public LiveData<List<OrderResponse>> getTodayOrders() { return todayOrders; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsLastPage() { return isLastPage; }

    public LiveData<OrderResponse> getOrderDetail() {
        return orderDetail;
    }

    // ------------------ LOAD DATA ------------------
    public void loadTodayOrders(boolean refresh) {
        if (isLoading.getValue() != null && isLoading.getValue()) return;

        isLoading.setValue(true);
        Log.d(TAG, "📡 Loading all orders today (no pagination)");

        orderRepository.getOrdersTodayNoPagination(
                new OrderRepository.RepositoryCallback<ApiResponse<List<OrderResponse>>>() {
                    @Override
                    public void onSuccess(ApiResponse<List<OrderResponse>> result) {
                        isLoading.postValue(false);

                        if (result != null && result.isSuccess() && result.getResult() != null) {
                            List<OrderResponse> orders = result.getResult();
                            todayOrders.postValue(orders);
                            isLastPage.postValue(true);
                            Log.d(TAG, "✅ Loaded " + orders.size() + " orders today");
                        } else {
                            todayOrders.postValue(new ArrayList<>());
                            isLastPage.postValue(true);
                            Log.w(TAG, "⚠️ API returned empty or invalid data");
                        }
                    }

                    @Override
                    public void onError(String error) {
                        isLoading.postValue(false);
                        errorMessage.postValue(error);
                        Log.e(TAG, "🚨 Failed to load orders today: " + error);
                    }
                }
        );
    }

    // ------------------ LOAD NEXT PAGE ------------------
    public void loadNextPage() {
        Boolean last = isLastPage.getValue();
        Boolean loading = isLoading.getValue();

        if (Boolean.TRUE.equals(last) || Boolean.TRUE.equals(loading)) return;
        loadTodayOrders(false);
    }

    // ------------------- UPDATE ORDER STATUS ------------------
    public void updateOrderStatus(int orderId, OrderStatus newStatus) {
        isLoading.setValue(true);
        System.out.println("🔄 Updating order ID " + orderId + " to status: " + newStatus.name());
        // Gửi status lên server
        orderRepository.updateOrderStatus(orderId, newStatus.name(), new OrderRepository.RepositoryCallback<OrderResponse>() {
            @Override
            public void onSuccess(OrderResponse result) {
                isLoading.postValue(false);
                // Cập nhật LiveData
                orderDetail.postValue(result);
                Log.d(TAG, "✅ Updated order status to: " + newStatus.name());
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
                Log.e(TAG, "🚨 Failed to update status: " + error);
            }
        });
    }

    // ------------------- UPDATE ORDER STATUS (ENDPOINT MỚI) ------------------
    // ------------------ REFRESH ------------------
    public void refresh() {
        loadTodayOrders(true);
    }
}