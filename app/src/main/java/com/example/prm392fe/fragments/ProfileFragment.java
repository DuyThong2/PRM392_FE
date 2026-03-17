package com.example.prm392fe.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.prm392fe.viewModel.OrderViewModel;
import com.google.gson.Gson;
import com.example.prm392fe.R;
import com.example.prm392fe.SessionManager;
import com.example.prm392fe.activities.MainActivity;
import com.example.prm392fe.activities.OrderHistoryActivity;
import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.api.ApiService;
import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.requests.LogoutRequest;
import com.example.prm392fe.models.responses.OrderResponse;
import com.example.prm392fe.utils.AppStompClient;
import com.example.prm392fe.utils.WebSocketService;
import com.example.prm392fe.viewModel.UserViewModel;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileFragment extends Fragment {

    TextView tvOrdersCount, tvName, tvPhone, tvPointsCount;
    ImageView imgAvatar;

    OrderViewModel orderViewModel;
    UserViewModel userViewModel;

    int userId = 0;
    String TAG = "PROFILE_FRAGMENT_TAG";
    ApiService apiService = ApiClient.getApiService();

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout và gán vào biến view
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // ✅ Đảm bảo layout tránh vùng camera và status bar
        View rootView = view.findViewById(R.id.FrameLayout);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            var systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Giảm bớt top padding (chỉ giữ lại một phần nhỏ)
            int reducedTop = Math.max(systemBars.top - 120, 0); // giảm 40px hoặc tuỳ chỉnh
            v.setPadding(systemBars.left, reducedTop, systemBars.right, systemBars.bottom);

            return insets;
        });

        tvName = view.findViewById(R.id.tv_user_name);
        tvPhone = view.findViewById(R.id.tv_user_phone);
        tvOrdersCount = view.findViewById(R.id.tv_orders_count);
        tvPointsCount = view.findViewById(R.id.tv_points_count);
        imgAvatar = view.findViewById(R.id.iv_avatar);

        // 🔹 Lấy ViewModel (chuẩn AndroidX)
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // 🔹 Gọi API qua ViewModel
        //1. Api lấy thông tin người dùng
        userViewModel.getInfo().observe(getViewLifecycleOwner(), userResponse -> {
            if (userResponse != null){
                Glide.with(this)
                        .load("https://i.pinimg.com/736x/30/a8/49/30a8490ff409df33d1e23702cf2c4aa8.jpg")
                        .override(300, 300) // fix size 200x200 pixel
                        .centerCrop()       // cắt giữa hình để không méo
                        .into(imgAvatar);

                tvName.setText(userResponse.getFullName());
                tvPhone.setText(userResponse.getPhone());
                userId = userResponse.getId();

                // 2. Api lấy lịch sử mua hàng
                orderViewModel.getOrdersByUserId(userId).observe(getViewLifecycleOwner(), orders -> {
                    if (orders != null && !orders.isEmpty()) {
                        tvOrdersCount.setText(String.valueOf(orders.size()));

                        // ✅ Tính tổng tiền (nếu có field totalPrice trong OrderResponse)
                        double total = 0;
                        for (OrderResponse o : orders) {
                            if(o.getStatus().equals("DELIVERED")){
                                total += o.getTotalPrice();
                            }
                        }
                        tvPointsCount.setText(String.format("%,.0fđ", total));
                    } else {
                        Log.d("OrderHistory", "Không có đơn hàng nào.");
                    }
                });
            }
        });

        LinearLayout orderHistoryLL = view.findViewById(R.id.orderHistoryLL);

        // Replace click handler for "Đổi mật khẩu" to open ChangePasswordFragment instead of PersonalInfoFragment
        LinearLayout llChangePassword = view.findViewById(R.id.ll_change_password);
        llChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    // Replace the main activity's fragment container with ChangePasswordFragment
                    ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentsFL, changePasswordFragment, "ChangePasswordFragment")
                            .addToBackStack("ChangePassword")
                            .commit();
                }
            }
        });

        // Open Favorites when user clicks "Sản phẩm yêu thích"
        LinearLayout llFavorites = view.findViewById(R.id.ll_favorites);
        llFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    FavoritesFragment favoritesFragment = new FavoritesFragment();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentsFL, favoritesFragment, "FavoritesFragment")
                            .addToBackStack("Favorites")
                            .commit();
                }
            }
        });

        orderHistoryLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), OrderHistoryActivity.class));
            }
        });

        view.findViewById(R.id.logoutTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiService.logout(LogoutRequest.builder()
                        .token(SessionManager.getInstance(getActivity()).getAuthToken())
                        .build()).enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful()) {
                            // Trường hợp THÀNH CÔNG (HTTP 200)
                            Toast.makeText(getActivity(), "Đang đăng xuất", Toast.LENGTH_SHORT).show();
                            ApiClient.clearApiClient();
                            AppStompClient.clearInstance();
                            getContext().stopService(new Intent(getContext(), WebSocketService.class));
                            SessionManager.getInstance(getActivity()).clearSession();
                            startActivity(new Intent(getActivity(), MainActivity.class));
                        } else {
                            try {
                                String errorJson = response.errorBody().string();
                                Gson gson = new Gson();
                                ApiResponse<?> errorResponse = gson.fromJson(errorJson, ApiResponse.class);

                                if (errorResponse != null && errorResponse.getMessage() != null) {
                                    // Hiển thị thông báo lỗi từ Server (User not existed!)
                                    Toast.makeText(getActivity(), errorResponse.getMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getActivity(), "Lỗi HTTP " + response.code(), Toast.LENGTH_SHORT).show();
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body: ", e);
                                Toast.makeText(getActivity(), "Lỗi không xác định.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(getActivity(), "Error occured!", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onFailure: ", t);
                    }
                });

            }
        });

        return view;
    }

    private void stopWebSocketService() {
        Intent serviceIntent = new Intent(requireContext(), WebSocketService.class);
        requireContext().stopService(serviceIntent);
    }
}