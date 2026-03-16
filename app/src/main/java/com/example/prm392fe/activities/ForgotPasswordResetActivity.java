package com.example.prm392fe.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392fe.R;
import com.example.prm392fe.api.ApiService;

public class ForgotPasswordResetActivity extends AppCompatActivity {

    EditText pwEt, confirmEt;
    Button confirmBtn, backBtn;
    ImageButton ibBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_reset);

        pwEt = findViewById(R.id.reset_pw_et);
        confirmEt = findViewById(R.id.reset_confirm_et);
        confirmBtn = findViewById(R.id.reset_confirm_btn);
        backBtn = findViewById(R.id.reset_back_btn);
        ibBack = findViewById(R.id.reset_toolbar_back);

        ibBack.setOnClickListener(v -> finish());
        backBtn.setOnClickListener(v -> finish());

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { checkState(); }
        };

        pwEt.addTextChangedListener(watcher);
        confirmEt.addTextChangedListener(watcher);

        confirmBtn.setOnClickListener(v -> {
            String pw = pwEt.getText().toString();
            String conf = confirmEt.getText().toString();
            if (pw.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 kí tự", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pw.equals(conf)) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }
            // Call backend reset-password API
            String email = getIntent().getStringExtra("forgot_email");
            String code = getIntent().getStringExtra("forgot_code");
            if (email == null) email = "";
            if (code == null) code = "";

            com.example.prm392fe.models.requests.ResetPasswordRequest req = com.example.prm392fe.models.requests.ResetPasswordRequest.builder()
                    .email(email)
                    .code(code)
                    .newPassword(pw)
                    .confirmNewPassword(conf)
                    .build();

            ApiService api = com.example.prm392fe.api.ApiClient.getUnauthService();
            confirmBtn.setEnabled(false);
            api.resetPassword(req).enqueue(new retrofit2.Callback<com.example.prm392fe.models.ApiResponse<String>>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.prm392fe.models.ApiResponse<String>> call, retrofit2.Response<com.example.prm392fe.models.ApiResponse<String>> response) {
                    confirmBtn.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ForgotPasswordResetActivity.this, "Mật khẩu đã được cập nhật", Toast.LENGTH_LONG).show();
                        // After a successful password reset, send the user back to the login screen
                        android.content.Intent toLogin = new android.content.Intent(ForgotPasswordResetActivity.this, com.example.prm392fe.activities.LoginEmailActivity.class);
                        // Clear the activity stack so user cannot navigate back into the reset flow
                        toLogin.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(toLogin);
                        finish();
                    } else {
                        String msg = com.example.prm392fe.api.ApiClient.extractErrorMessage(response, "Không thể đặt lại mật khẩu");
                        Toast.makeText(ForgotPasswordResetActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.prm392fe.models.ApiResponse<String>> call, Throwable t) {
                    confirmBtn.setEnabled(true);
                    Toast.makeText(ForgotPasswordResetActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void checkState() {
        String a = pwEt.getText().toString();
        String b = confirmEt.getText().toString();
        confirmBtn.setEnabled(!a.isEmpty() && !b.isEmpty());
    }
}
