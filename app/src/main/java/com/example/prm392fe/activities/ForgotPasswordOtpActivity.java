package com.example.prm392fe.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392fe.R;
import com.example.prm392fe.api.ApiService;

public class ForgotPasswordOtpActivity extends AppCompatActivity {

    TextView tvDesc, tvPhone;
    EditText otp1, otp2, otp3, otp4, otp5, otp6;
    Button btnConfirm, btnBack;
    ImageButton ibBack;
    private boolean isFillingFromPaste = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_otp);

        tvDesc = findViewById(R.id.otp_desc);
        tvPhone = findViewById(R.id.otp_phone);
        otp1 = findViewById(R.id.otp_1);
        otp2 = findViewById(R.id.otp_2);
        otp3 = findViewById(R.id.otp_3);
        otp4 = findViewById(R.id.otp_4);
        otp5 = findViewById(R.id.otp_5);
        otp6 = findViewById(R.id.otp_6);
        btnConfirm = findViewById(R.id.otp_confirm_btn);
        btnBack = findViewById(R.id.otp_back_btn);
        ibBack = findViewById(R.id.otp_toolbar_back);

        String email = getIntent().getStringExtra("forgot_email");
        if (email == null) email = "";
        tvPhone.setText(email);

        ibBack.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());

        TextWatcher digitWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { updateConfirmState(); }
        };

        otp1.addTextChangedListener(createOtpTextWatcher(otp1, otp2));
        otp2.addTextChangedListener(createOtpTextWatcher(otp2, otp3));
        otp3.addTextChangedListener(createOtpTextWatcher(otp3, otp4));
        otp4.addTextChangedListener(createOtpTextWatcher(otp4, otp5));
        otp5.addTextChangedListener(createOtpTextWatcher(otp5, otp6));
        otp6.addTextChangedListener(createOtpTextWatcher(otp6, null));

        // Also ensure confirm state updates if user pastes or edits
        otp1.addTextChangedListener(digitWatcher);
        otp2.addTextChangedListener(digitWatcher);
        otp3.addTextChangedListener(digitWatcher);
        otp4.addTextChangedListener(digitWatcher);
        otp5.addTextChangedListener(digitWatcher);
        otp6.addTextChangedListener(digitWatcher);

        btnConfirm.setOnClickListener(v -> {
            String code = otp1.getText().toString()+otp2.getText().toString()+otp3.getText().toString()+otp4.getText().toString()+otp5.getText().toString()+otp6.getText().toString();
            if (code.length() < 6) {
                Toast.makeText(this, "Vui lòng nhập mã 6 chữ số", Toast.LENGTH_SHORT).show();
                return;
            }
            // Verify code with backend before navigating
            verifyCodeAndProceed(code);
        });

        // focus first
        otp1.requestFocus();
    }

    // Navigate to reset screen with email+code
    private void navigateToResetWithCode(String code) {
        android.content.Intent i = new android.content.Intent(this, ForgotPasswordResetActivity.class);
        String email1 = getIntent().getStringExtra("forgot_email");
        if (email1 == null) email1 = "";
        i.putExtra("forgot_email", email1);
        i.putExtra("forgot_code", code);
        startActivity(i);
    }

    // Try to fill all OTP boxes from a pasted string (keep digits only)
    public void onOtpPaste(String raw) {
        fillOtpFromString(raw);
    }

    private void fillOtpFromString(String raw) {
        if (raw == null || raw.isEmpty()) return;
        String digits = raw.replaceAll("\\D+", "");
        if (digits.isEmpty()) return;

        isFillingFromPaste = true;
        // only take first 6 digits
        int len = Math.min(6, digits.length());
        char[] arr = digits.toCharArray();
        if (len > 0) otp1.setText(String.valueOf(arr[0])); else otp1.setText("");
        if (len > 1) otp2.setText(String.valueOf(arr[1])); else otp2.setText("");
        if (len > 2) otp3.setText(String.valueOf(arr[2])); else otp3.setText("");
        if (len > 3) otp4.setText(String.valueOf(arr[3])); else otp4.setText("");
        if (len > 4) otp5.setText(String.valueOf(arr[4])); else otp5.setText("");
        if (len > 5) otp6.setText(String.valueOf(arr[5])); else otp6.setText("");
        isFillingFromPaste = false;

        updateConfirmState();
        // if we have 6 digits, auto-submit
        String code = otp1.getText().toString()+otp2.getText().toString()+otp3.getText().toString()+otp4.getText().toString()+otp5.getText().toString()+otp6.getText().toString();
        if (code.length() == 6) {
            // do not navigate directly; verify with backend first
            verifyCodeAndProceed(code);
        }
    }

    // Verify OTP code with backend and navigate to reset if valid
    private void verifyCodeAndProceed(String code) {
        btnConfirm.setEnabled(false);
        ApiService api = com.example.prm392fe.api.ApiClient.getUnauthService();
        String email = getIntent().getStringExtra("forgot_email"); if (email == null) email = "";
        java.util.Map<String, String> payload = new java.util.HashMap<>();
        payload.put("email", email);
        payload.put("code", code);
        retrofit2.Call<com.example.prm392fe.models.ApiResponse<Void>> call = api.verifyResetCode(payload);
        call.enqueue(new retrofit2.Callback<com.example.prm392fe.models.ApiResponse<Void>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.prm392fe.models.ApiResponse<Void>> call, retrofit2.Response<com.example.prm392fe.models.ApiResponse<Void>> response) {
                btnConfirm.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    navigateToResetWithCode(code);
                } else {
                    // Use ApiClient helper to get a clean message for users; log full raw body for debugging
                    String msg = com.example.prm392fe.api.ApiClient.extractErrorMessage(response, "Mã OTP không hợp lệ hoặc đã hết hạn");
                    try {
                        if (response.errorBody() != null) {
                            String raw = response.errorBody().string();
                            android.util.Log.d("ForgotOTP", "Raw error body (hidden from user): " + raw);
                        }
                    } catch (Exception ex) {
                        // ignore
                    }
                    Toast.makeText(ForgotPasswordOtpActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.prm392fe.models.ApiResponse<Void>> call, Throwable t) {
                btnConfirm.setEnabled(true);
                android.util.Log.e("ForgotOTP", "verify request failure", t);
                Toast.makeText(ForgotPasswordOtpActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private TextWatcher createOtpTextWatcher(final EditText current, final EditText next) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFillingFromPaste) return; // avoid recursion while programmatically setting text

                String txt = s.toString();
                // handle paste: if user pasted multiple digits into one box
                if (txt.length() > 1) {
                    fillOtpFromString(txt);
                    return;
                }

                if (txt.length() > 0) {
                    // move to next
                    if (next != null) next.requestFocus();
                } else {
                    // if deleted, move to previous if any
                    int id = current.getId();
                    if (id == R.id.otp_6 && otp5 != null) otp5.requestFocus();
                    else if (id == R.id.otp_5 && otp4 != null) otp4.requestFocus();
                    else if (id == R.id.otp_4 && otp3 != null) otp3.requestFocus();
                    else if (id == R.id.otp_3 && otp2 != null) otp2.requestFocus();
                    else if (id == R.id.otp_2 && otp1 != null) otp1.requestFocus();
                }
                updateConfirmState();
            }
        };
    }

    private void updateConfirmState() {
        boolean all = !otp1.getText().toString().isEmpty() && !otp2.getText().toString().isEmpty() && !otp3.getText().toString().isEmpty()
                && !otp4.getText().toString().isEmpty() && !otp5.getText().toString().isEmpty() && !otp6.getText().toString().isEmpty();
        btnConfirm.setEnabled(all);
    }
}
