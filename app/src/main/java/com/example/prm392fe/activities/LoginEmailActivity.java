package com.example.prm392fe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.example.prm392fe.SessionManager;
import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.api.ApiService;
import com.example.prm392fe.databinding.ActivityLoginEmailBinding;
import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.requests.AuthenticationRequest;
import com.example.prm392fe.models.responses.AuthenticationResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginEmailActivity extends AppCompatActivity {

    private ActivityLoginEmailBinding binding;

    private AuthenticationResponse loginResult = new AuthenticationResponse();

    private static final String TAG = "LOGIN_EMAIL_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- Tránh vùng camera (notch) ---
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, bars.top, 0, 0);
            return insets;
        });

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ApiService api = ApiClient.getApiService();
        binding.loginBtnTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.login(AuthenticationRequest.builder()
                        .email(binding.emailEt.getText().toString())
                        .password(binding.passwordEt.getText().toString())
                        .build()).enqueue(new Callback<ApiResponse<AuthenticationResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AuthenticationResponse>> call, Response<ApiResponse<AuthenticationResponse>> response) {
                        if (response.isSuccessful()) {
                            // Trường hợp THÀNH CÔNG (HTTP 200)
                            ApiResponse<AuthenticationResponse> successResponse = response.body();
                            if (successResponse != null && successResponse.isSuccess()) {
                                // Lưu token và chuyển màn hình
                                String token = successResponse.getResult().getToken();
                                SessionManager.getInstance(LoginEmailActivity.this).saveAuthToken(token);
                                Toast.makeText(LoginEmailActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "onResponse: "+response.body().toString());
                                startMainActivity();
                            } else {
                                // Lỗi logic từ server (Ví dụ: success=false nhưng HTTP 200 OK)
                                Toast.makeText(LoginEmailActivity.this, "Lỗi logic: " + successResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            // Trường hợp THẤT BẠI HTTP (404, 500, 401, v.v.)
                            try {
                                // 1. Lấy body lỗi dưới dạng chuỗi
                                String errorJson = response.errorBody().string();

                                // 2. Sử dụng Gson (hoặc Moshi) để chuyển chuỗi JSON lỗi thành ApiResponse
                                // ==> Cần phải khởi tạo Gson và định nghĩa lại kiểu generic cho ApiResponse
                                // Sử dụng Type: Type type = new TypeToken<ApiResponse<Object>>() {}.getType();
                                // Hoặc đơn giản hơn, nếu bạn chỉ cần message:

                                // SỬ DỤNG GSON ĐỂ PARSE LỖI:
                                Gson gson = new Gson();
                                ApiResponse<?> errorResponse = gson.fromJson(errorJson, ApiResponse.class);

                                if (errorResponse != null && errorResponse.getMessage() != null) {
                                    // Hiển thị thông báo lỗi từ Server (User not existed!)
                                    Toast.makeText(LoginEmailActivity.this, errorResponse.getMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(LoginEmailActivity.this, "Lỗi HTTP " + response.code(), Toast.LENGTH_SHORT).show();
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body: ", e);
                                Toast.makeText(LoginEmailActivity.this, "Lỗi không xác định.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthenticationResponse>> call, Throwable t) {
                        Toast.makeText(LoginEmailActivity.this, "Error occured!", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onFailure: ", t);
                    }
                });
            }
        });

        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegisterEmailActivity();
            }
        });

        binding.forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginEmailActivity.this, ForgotPasswordEmailActivity.class));
            }
        });

    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void startRegisterEmailActivity() {
        startActivity(new Intent(this, RegisterEmailActivity.class));
    }

}