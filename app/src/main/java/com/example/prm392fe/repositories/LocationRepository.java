package com.example.prm392fe.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392fe.SessionManager;
import com.example.prm392fe.api.ApiService;
import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.responses.LocationResponse;

import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationRepository {
    ApiService apiService;
    SessionManager sessionManager;
    String TAG = "LOCATION_REPO_TAG";

    public LiveData<List<LocationResponse>> getLocations() {
        final MutableLiveData<List<LocationResponse>> data = new MutableLiveData<>();

        apiService.getLocations().enqueue(new Callback<ApiResponse<List<LocationResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<LocationResponse>>> call, Response<ApiResponse<List<LocationResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.i(TAG, "onResponse: "+response.body());
                    data.postValue(response.body().getResult());
                } else {
                    data.postValue(null);
                    Log.e(TAG, "onResponse: "+response.body());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<LocationResponse>>> call, Throwable t) {
                data.postValue(null);
                Log.e(TAG, "onFailure: ", t);
            }
        });

        return data;
    }
}
