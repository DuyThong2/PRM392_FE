package com.example.prm392fe.viewModel;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.PageResponse;
import com.example.prm392fe.models.requests.CreateOrderRequest;
import com.example.prm392fe.models.responses.OrderResponse;
import com.example.prm392fe.repositories.OrderRepository;

import java.util.List;

public class OrderViewModel extends ViewModel {
    private final OrderRepository orderRepository = OrderRepository.getInstance();

    public LiveData<List<OrderResponse>> getOrdersByUserId(int userId) {
        return orderRepository.getOrdersByUserId(userId);
    }

    public LiveData<OrderResponse> getOrderDetail(int orderId) {
        return orderRepository.getOrdersById(orderId);
    }

    public LiveData<List<OrderResponse>> getOrdersByStatus(int userId, String status) {
        return orderRepository.getOrdersByStatus(userId, status);
    }

    public LiveData<OrderResponse> createNewOrder(CreateOrderRequest request, boolean status) {
        return orderRepository.createOrder(request, status);
    }

    private final MutableLiveData<ApiResponse<PageResponse<OrderResponse>>> ordersToday = new MutableLiveData<>();

    public LiveData<ApiResponse<PageResponse<OrderResponse>>> getOrdersToday() {
        return ordersToday;
    }

    public void fetchOrdersToday(int page, int size, String sortBy, String sortDir, @Nullable String status) {
        OrderRepository.getInstance().getOrdersToday(page, size, sortBy, sortDir, status, new OrderRepository.RepositoryCallback<ApiResponse<PageResponse<OrderResponse>>>() {
            @Override
            public void onSuccess(ApiResponse<PageResponse<OrderResponse>> result) {
                ordersToday.postValue(result);
            }

            @Override
            public void onError(String error) {
                ordersToday.postValue(null);
            }
        });
    }

    // LiveData cho tổng đơn hôm nay (không lọc status) - dùng endpoint /order/today/no-pagination
    private final MutableLiveData<ApiResponse<List<OrderResponse>>> allOrdersToday = new MutableLiveData<>();

    public LiveData<ApiResponse<List<OrderResponse>>> getAllOrdersToday() {
        return allOrdersToday;
    }

    public void fetchAllOrdersToday() {
        // Không truyền status, không phân trang
        OrderRepository.getInstance().getOrdersTodayNoPagination(new OrderRepository.RepositoryCallback<ApiResponse<List<OrderResponse>>>() {
            @Override
            public void onSuccess(ApiResponse<List<OrderResponse>> result) {
                allOrdersToday.postValue(result);
            }

            @Override
            public void onError(String error) {
                allOrdersToday.postValue(null);
            }
        });
    }
}

