package com.example.prm392fe.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.requests.PaymentRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentRepository {
    private static PaymentRepository instance;
    private String TAG = "PAYMENT_REPOSITORY_TAG";

    private PaymentRepository() {}

    public static synchronized PaymentRepository getInstance() {
        if (instance == null) {
            instance = new PaymentRepository();
        }
        return instance;
    }

    public LiveData<String> createVNPAYPayment(PaymentRequest request) {
        MutableLiveData<String> data = new MutableLiveData<>();

        ApiClient.getApiService().createVNPAYPayment(request).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, String>>> call, Response<ApiResponse<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("PaymentDebug", "paymentUrl received: " + response); // debug đầu tiên
                    Map<String, String> result = response.body().getResult();
                    data.setValue(result.get("paymentUrl"));
                } else {
                    Log.d("PaymentDebug", "Payment URL is null or empty");
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                Log.d("PaymentDebug", "Fail Payment URL is null or empty");
                data.setValue(null);
            }
        });

        return data;
    }

    public LiveData<String> createMomoPayment(PaymentRequest request) {
        MutableLiveData<String> data = new MutableLiveData<>();

        ApiClient.getApiService().createMomoPayment(request).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, String>>> call, Response<ApiResponse<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("PaymentDebug", "paymentUrl received: " + response); // debug đầu tiên
                    Map<String, String> result = response.body().getResult();
                    data.setValue(result.get("paymentUrl"));
                } else {
                    Log.d("PaymentDebug", "Payment URL is null or empty");
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                Log.d("PaymentDebug", "Fail Payment URL is null or empty");
                data.setValue(null);
            }
        });

        return data;
    }

}
