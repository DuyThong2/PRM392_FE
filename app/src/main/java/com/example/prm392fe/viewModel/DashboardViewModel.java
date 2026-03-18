package com.example.prm392fe.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.models.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardViewModel extends ViewModel {

    private final MutableLiveData<Integer> todayOrders = new MutableLiveData<>();
    private final MutableLiveData<Integer> pendingOrders = new MutableLiveData<>();
    private final MutableLiveData<Integer> lowStock = new MutableLiveData<>();



    public LiveData<Integer> getTodayOrders() { return todayOrders; }
    public LiveData<Integer> getPendingOrders() { return pendingOrders; }
    public LiveData<Integer> getLowStock() { return lowStock; }

    public void loadDashboardData() {
        loadTodaysOrders();
        loadPendingOrders();
        loadLowStock();
    }

    private void loadTodaysOrders() {
        ApiClient.getApiService().getTodaysOrderCount().enqueue(new Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(Call<ApiResponse<Long>> call, Response<ApiResponse<Long>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    todayOrders.postValue(response.body().getResult().intValue());
                } else {
                    todayOrders.postValue(0);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                todayOrders.postValue(0);
            }
        });
    }

    private void loadPendingOrders() {
        ApiClient.getApiService().getPendingOrdersCount().enqueue(new Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(Call<ApiResponse<Long>> call, Response<ApiResponse<Long>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pendingOrders.postValue(response.body().getResult().intValue());
                } else {
                    pendingOrders.postValue(0);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                pendingOrders.postValue(0);
            }
        });
    }

    private void loadLowStock() {
        ApiClient.getApiService().getLowStockCount(5).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    lowStock.postValue(response.body().intValue());
                } else {
                    lowStock.postValue(0);
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                lowStock.postValue(0);
            }
        });
    }
}
