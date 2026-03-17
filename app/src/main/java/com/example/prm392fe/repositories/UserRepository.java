package com.example.prm392fe.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.responses.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private static final String TAG = "UserRepository";
    private static UserRepository instance;

    private UserRepository() {
    }

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    // Hàm gọi API trả về LiveData
    public LiveData<UserResponse> getInfo() {
        MutableLiveData<UserResponse> data = new MutableLiveData<>();

        ApiClient.getApiService().getInfo().enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body().getResult());
                } else {
                    Log.e(TAG, "API failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });

        return data;
    }
}
