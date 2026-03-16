package com.example.prm392fe.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.prm392fe.R;
import com.example.prm392fe.activities.MainActivity;

public class NotificationUtils {

    // Kênh cho các thông báo thông thường (tin nhắn, cập nhật...)
    private static final String GENERAL_CHANNEL_ID = "GENERAL_NOTIFICATIONS";
    // Kênh cho dịch vụ chạy nền (Foreground Service)
    public static final String FOREGROUND_SERVICE_CHANNEL_ID = "FOREGROUND_SERVICE_CHANNEL";


    /**
     * Phương thức này phải được gọi một lần khi ứng dụng khởi động (ví dụ trong Application class
     * hoặc MainActivity's onCreate) để đăng ký các kênh thông báo.
     * @param context Context của ứng dụng
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            // Kênh thông báo chung
            NotificationChannel generalChannel = new NotificationChannel(
                    GENERAL_CHANNEL_ID,
                    "Thông báo chung",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            generalChannel.setDescription("Kênh cho các thông báo tin nhắn và cập nhật.");
            manager.createNotificationChannel(generalChannel);

            // Kênh cho dịch vụ nền
            NotificationChannel foregroundChannel = new NotificationChannel(
                    FOREGROUND_SERVICE_CHANNEL_ID,
                    "Dịch vụ chạy nền",
                    NotificationManager.IMPORTANCE_LOW // Ưu tiên thấp để không làm phiền
            );
            foregroundChannel.setDescription("Thông báo cho biết dịch vụ đang chạy ngầm.");
            manager.createNotificationChannel(foregroundChannel);
        }
    }

    /**
     * Hiển thị một thông báo thông thường cho người dùng.
     * @param context Context để tạo thông báo
     * @param title   Tiêu đề thông báo
     * @param message Nội dung thông báo
     */
    public static void showGeneralNotification(Context context, String title, String message) {
        // Tạo PendingIntent để mở MainActivity khi người dùng nhấn vào
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, GENERAL_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_chat) // Thay bằng icon phù hợp
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Tự xóa khi nhấn vào

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // ID duy nhất cho mỗi thông báo để chúng không ghi đè lên nhau
        // Dùng thời gian hiện tại là một cách đơn giản để có ID duy nhất
        int notificationId = (int) System.currentTimeMillis();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("NotificationUtils", "Permission to post notifications not granted.");
            return;
        }
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Tạo một Notification object cho Foreground Service với nội dung mặc định.
     */
    public static android.app.Notification createForegroundServiceNotification(Context context) {
        // Gọi phiên bản nâng cao với nội dung mặc định
        return createForegroundServiceNotification(context, "Đang chờ tin nhắn mới...");
    }

    /**
     * PHIÊN BẢN NÂNG CẤP: Tạo một Notification object cho Foreground Service
     * với nội dung tùy chỉnh.
     * @param context Context của Service
     * @param contentText Nội dung mới cần hiển thị
     * @return Một đối tượng Notification
     */
    public static android.app.Notification createForegroundServiceNotification(Context context, String contentText) {
        // 1. Lấy Bitmap của ảnh logo lớn
        android.graphics.Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notification);

        return new NotificationCompat.Builder(context, FOREGROUND_SERVICE_CHANNEL_ID)
                .setContentTitle("Giỏ hàng Gundam Shop")
                .setContentText(contentText) // <-- Sử dụng nội dung được truyền vào
                .setSmallIcon(R.drawable.ic_cart) // Vẫn phải có, dùng cho Status Bar
                .setLargeIcon(largeIcon)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true) // <-- Flag quan trọng, cho biết thông báo này không thể xóa
                .build();
    }
}
