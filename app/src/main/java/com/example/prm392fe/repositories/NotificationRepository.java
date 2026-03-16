package com.example.prm392fe.repositories;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.example.prm392fe.SessionManager;
import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.responses.CountResponse;
import com.example.prm392fe.api.ApiService;
import com.example.prm392fe.utils.AppStompClient;
import com.example.prm392fe.utils.NotificationUtils;
import com.example.prm392fe.utils.WebSocketService;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
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
public class NotificationRepository {
    ApiService apiService = ApiClient.getApiService();

    final AppStompClient stompClient;
    final String currentUserId;
    final Context context;

    String TAG = "NOTIFICATION_REPO";
    Gson gson = new Gson();

    //LiveData là một lớp trừu tượng (abstract class). Nó có các đặc điểm chính:
    //Chỉ đọc (Read-only): Bạn không thể thay đổi giá trị của nó từ bên ngoài. Nó không có các phương thức public như setValue() hay postValue().
    //MutableLiveData là một lớp con của LiveData. Nó kế thừa tất cả các đặc điểm của LiveData và thêm vào khả năng thay đổi giá trị.
    MutableLiveData<Integer> chatBadgeCount = new MutableLiveData<>(0);
    MutableLiveData<Integer> cartBadgeCount = new MutableLiveData<>(0);
    MutableLiveData<String> generalNotification = new MutableLiveData<>();

    // --- CÁC PHƯƠNG THỨC LẤY DỮ LIỆU BAN ĐẦU (REST API) ---

    public void fetchInitialChatBadgeCount() {
        // TODO: Gọi API REST để tải số đếm ban đầu
        // Hiện tại: Giả định là 0
        // Sau khi API call thành công, gọi chatBadgeCount.postValue(result);
        apiService.countUnreadMessages(Integer.parseInt(currentUserId)).enqueue(new Callback<ApiResponse<CountResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CountResponse>> call, Response<ApiResponse<CountResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.i(TAG, "onResponse: "+response.body());
                    chatBadgeCount.postValue(response.body().getResult().getCount());
                } else {
                    chatBadgeCount.postValue(-1);
                    Log.e(TAG, "onResponse ChatBadge: "+response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CountResponse>> call, Throwable t) {
                chatBadgeCount.postValue(-1);
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    public void fetchInitialCartBadgeCount() {
        apiService.totalItemsQuantityByCustomerId(Integer.parseInt(currentUserId)).enqueue(new Callback<ApiResponse<CountResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CountResponse>> call, Response<ApiResponse<CountResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.i(TAG, "onResponse: "+response.body());
                    cartBadgeCount.postValue(response.body().getResult().getCount());
                } else {
                    cartBadgeCount.postValue(-1);
                    Log.e(TAG, "onResponse CartBadge: "+response.body());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CountResponse>> call, Throwable t) {
                cartBadgeCount.postValue(-1);
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    // --- CÁC PHƯƠNG THỨC QUẢN LÝ REAL-TIME (STOMP) ---

    /**
     * Bắt đầu lắng nghe các topic STOMP bền vững (như badge count).
     * Phương thức này sẽ được gọi khi NotificationViewModel được khởi tạo.
     */
    public void startListeningForPersistenQueues() {

        Log.i(TAG, "startListeningForPersistentTopics: Đã vào đây");

        // 1. Theo dõi Badge Count (Chat) từ Topic "/user/queue/unread-messages"
        // Lấy luồng dữ liệu thô từ AppStompClient
        Disposable unreadMessageDisp = stompClient.getCountingMessagesQueue()
                // RxJava: Chuyển sang luồng background để xử lý JSON
                .subscribeOn(Schedulers.io())
                .subscribe(stompMessage -> {
                    try {
                        Log.i(TAG, "Nhận được message từ /user/queue/counting-messages");
                        String payload = stompMessage.getPayload();
                        CountResponse dto = gson.fromJson(payload, CountResponse.class);
                        Log.i(TAG, "counting-message listener: "+dto);

                        // Chỉ xử lý nếu là loại badge chat
                        if ("UNREAD_MESSAGE".equals(dto.getType())) {
                            // Đẩy dữ liệu lên LiveData (sử dụng postValue vì đang ở background thread)
                            chatBadgeCount.postValue(dto.getCount());
                            Log.i(TAG, "Badge Count Updated via STOMP: " + dto.getType());
                        }
                        if ("QUANTITY_CART".equals(dto.getType())) {
                            cartBadgeCount.postValue(dto.getCount());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi phân tích JSON DTO: ", e);
                    }
                }, throwable -> {
                    // Xử lý lỗi khi subscribe hoặc nhận dữ liệu từ topic này
                    Log.e(TAG, "Lỗi subscription /user/queue/counting-messages", throwable);
                });

        // Lưu disposable vào chung với persistentDisposable của AppStompClient để quản lý
        stompClient.addPersistentDisposable(unreadMessageDisp);

        // 2. (Tùy chọn) Theo dõi thông báo sự kiện chung của ứng dụng
        Disposable notificationDisp = stompClient.getNotificationsQueue()
                .subscribeOn(Schedulers.io())
                .subscribe(stompMessage -> {
                    // Xử lý thông báo sự kiện (ví dụ: thông báo đơn hàng mới)
                    generalNotification.postValue(stompMessage.getPayload());
                    Log.i(TAG, "General Notification received.");
                }, throwable -> {
                    Log.e(TAG, "Lỗi subscription /user/queue/notifications", throwable);
                });

        stompClient.addPersistentDisposable(notificationDisp);
    }

    // --- CÁC PHƯƠNG THỨC GET LIVE DATA ---

    public LiveData<Integer> getChatBadgeCount() {
        return chatBadgeCount;
    }

    public LiveData<String> getGeneralNotification() {
        return generalNotification;
    }

    /**
     * Hàm này sẽ cập nhật nội dung của thông báo chạy nền.
     * @param newStatusText Nội dung trạng thái mới cần hiển thị.
     */
    public void updateForegroundNotificationStatus(String newStatusText) {
        // 1. Lấy NotificationManager
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // 2. Sử dụng NotificationUtils để xây dựng đối tượng Notification đã được cập nhật
        android.app.Notification updatedNotification =
                NotificationUtils.createForegroundServiceNotification(context, newStatusText);

        // 3. Gọi notify() với CÙNG MỘT ID đã được dùng trong startForeground()
        // Đây chính là bước thực hiện việc CẬP NHẬT
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Nếu không có quyền, chúng ta không thể làm gì hơn ngoài việc ghi log và thoát.
            // Việc XIN QUYỀN phải được thực hiện từ Activity.
            Log.w("NotificationRepo", "POST_NOTIFICATIONS permission not granted. Cannot update notification.");
            return;
        }
        notificationManager.notify(WebSocketService.NOTIFICATION_ID, updatedNotification);
    }
}
