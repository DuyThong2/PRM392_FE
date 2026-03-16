package com.example.prm392fe.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.api.ApiService;
import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.requests.SendMessageRequest;
import com.example.prm392fe.models.requests.UpdateReadMessageRequest;
import com.example.prm392fe.models.responses.CountResponse;
import com.example.prm392fe.models.responses.MessageResponse;
import com.example.prm392fe.utils.AppStompClient;

import java.util.List;

import io.reactivex.disposables.Disposable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageRepository {

    ApiService apiService = ApiClient.getApiService();
    Disposable chatTopicDisp;

    final AppStompClient stompClient;
    final String customerId;
    final String userId;
    final String conversationId;

    String TAG = "MESSAGE_REPO_TAG";

    MutableLiveData<MessageResponse> newIncomingMessage = new MutableLiveData<>();

    public LiveData<List<MessageResponse>> getMessages() {
        final MutableLiveData<List<MessageResponse>> data = new MutableLiveData<>();

        apiService.getMessages(Integer.parseInt(customerId)).enqueue(new Callback<ApiResponse<List<MessageResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MessageResponse>>> call, Response<ApiResponse<List<MessageResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.postValue(response.body().getResult());
                    Log.i(TAG, "onResponse: "+response);
                } else {
                    data.postValue(null);
                    Log.e(TAG, "onResponse: "+response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MessageResponse>>> call, Throwable t) {
                data.postValue(null);
                Log.e(TAG, "onFailure: ", t);
            }
        });

        return data;
    }

    public void updateReadMessages() {

        apiService.updateReadMessages(
                UpdateReadMessageRequest.builder()
                        .receiverId(Integer.parseInt(userId))
                        .conversationId(Integer.parseInt(conversationId))
                        .build()
        ).enqueue(new Callback<ApiResponse<CountResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CountResponse>> call, Response<ApiResponse<CountResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.i(TAG, "Đã update "+response.body().getResult().getCount()+" read messages");
                } else {
                    Log.e(TAG, "onResponse: "+response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CountResponse>> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    public void sendMessage(int conversationId, String content) {
        try {
            Gson gson = new Gson();
            SendMessageRequest request = SendMessageRequest.builder()
                    .conversationId(conversationId)
                    .content(content)
                    .build();
            stompClient.sendMessage(getCustomerId(), gson.toJson(request));

        } catch (Exception e) {
            Log.e(TAG, "onFailure: "+e);
        }
    }

    public void startListeningForChat() {
        if (!stompClient.isConnected()) {
            Log.e(TAG, "STOMP Client not connected. Cannot subscribe.");
            return;
        }

        // Hủy subscription cũ nếu có
        if (chatTopicDisp != null && !chatTopicDisp.isDisposed()) {
            chatTopicDisp.dispose();
        }

        chatTopicDisp = stompClient.subscribeDynamicTopic(customerId, stompMessage -> {
            String payload = stompMessage.getPayload();
            try {
                Gson gson = new Gson();
                // Chuyển đổi JSON Payload thành MessageResponse
                MessageResponse newMessage = gson.fromJson(payload, MessageResponse.class);

                // Đẩy tin nhắn mới vào LiveData real-time
                newIncomingMessage.postValue(newMessage);

            } catch (Exception e) {
                Log.e(TAG, "Lỗi phân tích JSON STOMP: " + payload, e);
            }
        });
    }

    public void stopListeningForChat() {
        if (chatTopicDisp != null && !chatTopicDisp.isDisposed()) {
            chatTopicDisp.dispose();
            Log.i(TAG, "Chat topic subscription disposed.");
        }
    }

    // Phương thức cung cấp LiveData cho tin nhắn mới
//    public LiveData<MessageResponse> getNewIncomingMessage() {
//        return newIncomingMessage;
//    }

}
