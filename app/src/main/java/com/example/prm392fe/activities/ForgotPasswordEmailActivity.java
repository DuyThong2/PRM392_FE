package com.example.prm392fe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392fe.R;
import com.example.prm392fe.api.ApiService;

public class ForgotPasswordEmailActivity extends AppCompatActivity {

    EditText etEmail;
    Button btnContinue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_email);

        etEmail = findViewById(R.id.fp_email_et);
        btnContinue = findViewById(R.id.fp_continue_btn);

        btnContinue.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, getString(R.string.enter_valid_email), Toast.LENGTH_SHORT).show();
                return;
            }

            // Call forgot-password API
            ApiService api = com.example.prm392fe.api.ApiClient.getUnauthService();
            btnContinue.setEnabled(false);
            api.forgotPassword(email).enqueue(new retrofit2.Callback<com.example.prm392fe.models.ApiResponse<Void>>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.prm392fe.models.ApiResponse<Void>> call, retrofit2.Response<com.example.prm392fe.models.ApiResponse<Void>> response) {
                    btnContinue.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null) {
                        // proceed to OTP screen (do not reveal whether email exists)
                        Intent i = new Intent(ForgotPasswordEmailActivity.this, ForgotPasswordOtpActivity.class);
                        i.putExtra("forgot_email", email);
                        startActivity(i);
                    } else {
                        String msg = com.example.prm392fe.api.ApiClient.extractErrorMessage(response, "Lỗi gửi yêu cầu");
                        Toast.makeText(ForgotPasswordEmailActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.prm392fe.models.ApiResponse<Void>> call, Throwable t) {
                    btnContinue.setEnabled(true);
                    Toast.makeText(ForgotPasswordEmailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
