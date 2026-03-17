package com.example.prm392fe.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.prm392fe.R;
import com.example.prm392fe.SessionManager;
import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.databinding.ActivityMainBinding;
import com.example.prm392fe.utils.AppStompClient;
import com.example.prm392fe.utils.NotificationUtils;
import com.example.prm392fe.utils.WebSocketService;
import com.google.android.material.navigation.NavigationBarView;


public class MainActivity extends AppCompatActivity {
    //View binding
    private ActivityMainBinding binding;
    private SessionManager sessionManager;
    private AppStompClient stompClient;

    private String userRole;

    String TAG = "MAIN_ACTIVITY";

    // 1. Khai báo một ActivityResultLauncher để xử lý kết quả xin quyền
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Người dùng đã cấp quyền. Bạn có thể tiếp tục các tác vụ liên quan đến thông báo.
                    Toast.makeText(this, "Đã cấp quyền thông báo!", Toast.LENGTH_SHORT).show();
                    // Ví dụ: khởi động WebSocketService của bạn ở đây nếu cần
                } else {
                    // Người dùng đã từ chối quyền.
                    // Bạn nên hiển thị một thông báo giải thích tại sao bạn cần quyền này.
                    Toast.makeText(this, "Bạn sẽ không nhận được thông báo tin nhắn mới.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //activity_main.xml = ActivityMainBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //Chú ý dòng này!!!!! nếu dùng binding
        //setContentView(R.layout.activity_main);
        setContentView(binding.getRoot());
        sessionManager = SessionManager.getInstance(getApplicationContext());
        // Cho phép layout phủ dưới status bar (fix cho Pixel, Android 12+)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.brand_red)); // hoặc mã hex

        // --- Tránh vùng camera (notch) ---
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, bars.top, 0, 0);
            return insets;
        });

        sessionManager = SessionManager.getInstance(MainActivity.this);
        if (!sessionManager.isLoggedIn()) {
            startLoginOptionsActivity();
        } else {
            // Trường hợp tắt app mà chưa logout thì sessionManager vẫn lưu token tuy nhiên ApiClient đã xóa token
            // --> khi mà vào lại app ---> vào thẳng Home ko thông qua login (do sessionManager đã có token)
            // Mà ApiClient chỉ được gán token thông qua login --> bị lỗi 1 số api cần bearer token
            ApiClient.setToken(SessionManager.getInstance(MainActivity.this).getAuthToken());
            userRole = sessionManager.getRole();

            // BẮT ĐẦU SERVICE để nó quản lý việc kết nối và lắng nghe
            startWebSocketService();
            NotificationUtils.createNotificationChannels(this);
            askNotificationPermission();

            System.out.println("Start hereeeee");
            setupBottomNavigationForRole(userRole);
        }
    }

    private void askNotificationPermission() {
        // Chỉ áp dụng cho Android 13 (API 33) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Kiểm tra xem quyền đã được cấp hay chưa
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, không cần làm gì thêm.
            } else {
                // Quyền chưa được cấp, tiến hành hỏi người dùng.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    /**
     * Chọn menu theo vai trò
     */
    private void setupBottomNavigationForRole(String role) {
        binding.bottomNavigation.getMenu().clear();

        if ("STAFF".equalsIgnoreCase(role)) {
            binding.bottomNavigation.inflateMenu(R.menu.menu_staff_bottom);
            setupStaffNavigation();
        } else {
            binding.bottomNavigation.inflateMenu(R.menu.menu_bottom);
            setupCustomerNavigation();
        }
    }

    /**
     * Xử lý navigation cho STAFF
     */
    private void setupStaffNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                return false;
            }
        });
    }


    /**
     * Xử lý navigation cho CUSTOMER (logic cũ giữ nguyên)
     */
    private void setupCustomerNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                return true;
            }
        });
    }

    // ----------------- CUSTOMER FRAGMENTS -----------------


    // ----------------- COMMON UTILS -----------------
    private void startLoginOptionsActivity() {
        startActivity(new Intent(this, LoginOptionsActivity.class));
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(binding.fragmentsFL.getId(), fragment, tag);
        ft.commit();
    }

    private void startWebSocketService() {
        Intent serviceIntent = new Intent(this, WebSocketService.class);
        // Sử dụng startForegroundService để tuân thủ quy tắc Android O+
        // Service sẽ ngay lập tức gọi onCreate() rồi đến onStartCommand()
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    // ----------------- STAFF FRAGMENTS -----------------


    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

}