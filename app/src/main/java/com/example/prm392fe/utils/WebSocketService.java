package com.example.prm392fe.utils;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;


import com.example.prm392fe.SessionManager;
import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.api.ApiService;
import com.example.prm392fe.repositories.NotificationRepository;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class WebSocketService extends Service {

    private static final String TAG = "WEBSOCKET_SERVICE";
    // Chuyển từ private thành public để các lớp khác có thể truy cập
    public static final int NOTIFICATION_ID = 101;
    private static final String CHANNEL_ID = "FOREGROUND_SERVICE_CHANNEL_ID";

    private AppStompClient stompClient;
    private CompositeDisposable disposables = new CompositeDisposable();
    private NotificationRepository notificationRepository;
    private Observer<Integer> cartBadgeObserver;

    private final ApiService apiService = ApiClient.getApiService();

    // Nhớ chỉnh trong file AndroidManifest.xml
    // <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    // <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
    // <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    // <application>.....
    //          <service android:name=".utils.WebSocketService"
    //            android:exported="false"
    //            android:enabled="true"
    //            android:foregroundServiceType="dataSync"/>.....
    // </application
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: Đã vào đây");

        // Khởi tạo Client
        String token = SessionManager.getInstance(this).getAuthToken();
        String userId = SessionManager.getInstance(this).getUserId();

        stompClient = AppStompClient.getInstance(token);

        // Khởi tạo Repository
        notificationRepository = new NotificationRepository(stompClient, userId, this);
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: Đã vào đây");
        // MainActivity bị hủy.
        // stompClient có thể bị rò rỉ nếu nó là Singleton (như bạn đã thiết kế) nhưng nó không có Activity nào để "làm việc" cùng.
        // startForeground Service cái này giúp cho process của app ko bị kill bới Android
        // nếu app process bị kill thì tất cả mọi thứ kể cả AppStompClient sẽ bị hủy
        startForeground(NOTIFICATION_ID, createNotification());

        if (!stompClient.isConnected()) {
            Log.i(TAG, "onStartCommand: Stomp chưa connect");
            Disposable lifecycleForegroundDisp = stompClient.getLifecycle()
                    .subscribe(lifecycleEvent -> {
                        switch (lifecycleEvent.getType()) {
                            case OPENED:
                                Log.i(TAG, "STOMP Connection Opened!");
                                // Yêu cầu Repository cập nhật trạng thái
                                notificationRepository.startListeningForPersistenQueues();
                                notificationRepository.fetchInitialCartBadgeCount();
                                startObservingCartBadge();
                                break;
                            case ERROR:
                                Log.e(TAG, "STOMP Connection Error: " + lifecycleEvent.getException().getMessage());
                                break;
                            case CLOSED:
                                Log.w(TAG, "STOMP Connection Closed.");
                                break;
                        }
                    });
            stompClient.addPersistentDisposable(lifecycleForegroundDisp);

            stompClient.connect();

        } else {
            Log.i(TAG, "onStartCommand: Stomp đã connect");
            // Trường hợp Service được gọi lại nhưng kết nối đã mở
            Log.i(TAG, "Client already connected. Ensuring repository subscriptions.");
            notificationRepository.startListeningForPersistenQueues();
        }

        return START_STICKY;
    }

    private android.app.Notification createNotification() {
        // ... (Giữ nguyên logic tạo Foreground Notification)
        // Điều này đảm bảo rằng mã tạo kênh thông báo chỉ chạy trên Android 8.0 (Oreo) trở lên, vì Notification Channels được giới thiệu từ API cấp 26.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    CHANNEL_ID, "Chat Background Service", NotificationManager.IMPORTANCE_LOW
//            );
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            manager.createNotificationChannel(channel);
//        }
//
//        return new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("Dịch vụ chat đang chạy")
//                .setContentText("Đang chờ tin nhắn mới...")
//                .setSmallIcon(R.drawable.ic_nav_chat)
//                .setPriority(NotificationCompat.PRIORITY_MAX)
//                .build();
        return NotificationUtils.createForegroundServiceNotification(this);
    }

    /**
     * Phương thức này sẽ lắng nghe sự thay đổi của LiveData cartBadgeCount
     * và cập nhật Foreground Notification.
     */
    private void startObservingCartBadge() {
        // Tránh tạo observer nhiều lần
        if (cartBadgeObserver != null) return;

        cartBadgeObserver = cartQuantity -> {
            Log.i(TAG, "Service observed cartQuantity change: " + cartQuantity);
            if (cartQuantity != null && cartQuantity > -1) {
                String statusText = "Bạn đang có " + cartQuantity + " sản phẩm trong giỏ hàng";
                notificationRepository.updateForegroundNotificationStatus(statusText);
            } else {
                // Nếu muốn, bạn có thể reset lại text khi giỏ hàng trống
                notificationRepository.updateForegroundNotificationStatus("Đang chờ tin nhắn mới...");
            }
        };

        // LiveData cần được observe trên Main Thread
        new Handler(Looper.getMainLooper()).post(() -> {
            notificationRepository.getCartBadgeCount().observeForever(cartBadgeObserver);
        });
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.i(TAG, "Service being destroyed. Disposing subscriptions.");
//
//        // Tất cả disposable đã dồn về AppStompClient nên chỉ khi logout
//        stompClient.disconnect();
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
