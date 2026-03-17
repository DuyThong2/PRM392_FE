package com.example.prm392fe.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392fe.SessionManager;
import com.example.prm392fe.api.ApiService;
import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.responses.ConversationResponse;

import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationRepository {

    ApiService apiService;
    SessionManager sessionManager;
    String TAG = "CONVERSATION_REPO_TAG";

    public LiveData<List<ConversationResponse>> getConversations() {
        final MutableLiveData<List<ConversationResponse>> data = new MutableLiveData<>();

        apiService.getConversations(Integer.parseInt(sessionManager.getUserId())).enqueue(new Callback<ApiResponse<List<ConversationResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ConversationResponse>>> call, Response<ApiResponse<List<ConversationResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.postValue(response.body().getResult());
                } else {
                    data.postValue(null);
                    Log.e(TAG, "onResponse: "+response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ConversationResponse>>> call, Throwable t) {
                data.postValue(null);
                Log.e(TAG, "onFailure: ", t);
            }
        });

        return data;
    }

    public LiveData<ConversationResponse> getConversationByCustomerId(String customerId) {
        final MutableLiveData<ConversationResponse> data = new MutableLiveData<>();

        apiService.getConversationByCustomerId(Integer.parseInt(customerId)).enqueue(new Callback<ApiResponse<ConversationResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ConversationResponse>> call, Response<ApiResponse<ConversationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.i(TAG, "onResponse: "+response);
                    data.postValue(response.body().getResult());
                } else {
                    data.postValue(null);
                    Log.e(TAG, "onResponse: "+response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ConversationResponse>> call, Throwable t) {
                data.postValue(null);
                Log.e(TAG, "onFailure: ", t);
            }
        });

        return data;
    }

}
