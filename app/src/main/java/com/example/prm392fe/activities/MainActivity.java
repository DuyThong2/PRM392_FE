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
import com.example.prm392fe.fragments.FavoriteListFragment;
import com.example.prm392fe.fragments.MapsFragment;
import com.example.prm392fe.fragments.PersonalInfoFragment;
import com.example.prm392fe.fragments.ProductListFragment;
import com.example.prm392fe.fragments.ProfileFragment;
import com.example.prm392fe.fragments.staff.DashboardFragment;
import com.example.prm392fe.fragments.staff.QuickOrderFragment;
import com.example.prm392fe.utils.NotificationUtils;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    //View binding
    private ActivityMainBinding binding;
    private SessionManager sessionManager;

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
        setContentView(binding.getRoot());

        // Khởi tạo SessionManager
        sessionManager = SessionManager.getInstance(this);

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (!sessionManager.isLoggedIn()) {
            startLoginOptionsActivity();
            finish();
            return; // Dừng lại, không thực hiện các bước sau
        }

        // Lấy vai trò của người dùng từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            userRole = intent.getStringExtra("USER_ROLE");
        }

        userRole = sessionManager.getRole();
        boolean isStaff = (userRole != null && (userRole.equals("STAFF") || userRole.equals("ADMIN")));

        // 2. Xin quyền thông báo (chỉ trên Android 13 trở lên)
        // Chỉ xin quyền nếu là STAFF
        if (isStaff) {
            // Android 13 (API 33) trở lên
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

        // Thiết lập BottomNavigation theo vai trò
        setupBottomNavigationForRole(userRole);

        // ✅ Đảm bảo layout tránh vùng camera và status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            // Lấy kích thước phần đệm của hệ thống (status bar, navigation bar)
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Áp dụng padding để tránh nội dung bị che khuất
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Chọn menu theo vai trò
     */
    private void setupBottomNavigationForRole(String role) {
        binding.bottomNavigation.getMenu().clear();

        if ("STAFF".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
            binding.bottomNavigation.inflateMenu(R.menu.menu_staff_bottom);
            showDashboardFragment(); // mặc định staff mở Dashboard
            setupStaffNavigation();
        } else {
            binding.bottomNavigation.inflateMenu(R.menu.menu_bottom);
            showProductListFragment(); // mặc định customer mở danh sách sản phẩm
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
                if (id == R.id.nav_home) {
                    showDashboardFragment();
                    return true;
                } else if (id == R.id.nav_orders) {
                    showQuickOrderFragment();
                    return true;
                } else if (id == R.id.nav_profile) {
                    showProfileFragment();
                }
//            } else if (id == R.id.nav_search) {
//                showProductSearchFragment();
//                return true;
//            } else if (id == R.id.nav_notify) {
//                showNotificationsFragment();
//                return true;
//            }
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
                if (id == R.id.nav_home) {
                    showProductListFragment();
                } else if (id == R.id.nav_map) {
                    showMapsFragment();
                } else if (id == R.id.nav_profile) {
                    showProfileFragment();
                }
                return true;
            }
        });
    }

    // ----------------- CUSTOMER FRAGMENTS -----------------
    private void showProductListFragment() {
        // Kiểm tra nếu fragment đã tồn tại, không cần tạo lại
        Fragment existingFragment = getSupportFragmentManager().findFragmentByTag("ProductListFragment");

        if (existingFragment == null) {
            ProductListFragment productListFragment = new ProductListFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(binding.fragmentsFL.getId(), productListFragment, "ProductListFragment")
                    .commit();
        } else {
            // Nếu fragment đã có (VD: xoay màn hình), chỉ cần hiển thị lại
            getSupportFragmentManager().beginTransaction()
                    .show(existingFragment)
                    .commit();
        }
    }

    private void showFavoriteListFragment() {
        FavoriteListFragment favoriteListFragment = new FavoriteListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFL.getId(), favoriteListFragment, "FavoriteListFragment");
        fragmentTransaction.commit();

    }
    private void showProfileFragment() {
        ProfileFragment profileFragment = new ProfileFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFL.getId(), profileFragment, "ProfileFragment");
        fragmentTransaction.commit();

    }

    private void showMapsFragment() {
        replaceFragment(new MapsFragment(), TAG);
    }

    // Called from fragment_profile.xml via android:onClick
    public void openPersonalInfo(android.view.View view) {
        android.util.Log.d("MainActivity", "openPersonalInfo clicked");
        android.widget.Toast.makeText(this, "Opening Thông tin cá nhân", android.widget.Toast.LENGTH_SHORT).show();
        PersonalInfoFragment personalInfoFragment = new PersonalInfoFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.fragmentsFL.getId(), personalInfoFragment, "PersonalInfoFragment")
                .addToBackStack(null)
                .commit();
    }

    // ----------------- COMMON UTILS -----------------
    private void startLoginOptionsActivity() {
        startActivity(new Intent(this, LoginOptionsActivity.class));
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(binding.fragmentsFL.getId(), fragment, tag);
        ft.commit();
    }

    // ----------------- STAFF FRAGMENTS -----------------
    private void showDashboardFragment() {
        replaceFragment(new DashboardFragment(), "DashboardFragment");
    }

    private void showQuickOrderFragment() {
        replaceFragment(new QuickOrderFragment(), "QuickOrderFragment");
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

}