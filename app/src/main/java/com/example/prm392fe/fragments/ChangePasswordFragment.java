package com.example.prm392fe.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.example.prm392fe.R;
import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.api.ApiService;
import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.requests.ChangePasswordRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordFragment extends Fragment {

    EditText etCurrent, etNew, etConfirm;
    MaterialButton btnConfirm;
    ImageView ivBack;
    private final Gson gson = new Gson();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        etCurrent = view.findViewById(R.id.et_current_password);
        etNew = view.findViewById(R.id.et_new_password);
        etConfirm = view.findViewById(R.id.et_confirm_password);
        btnConfirm = view.findViewById(R.id.btn_change_password);
        ivBack = view.findViewById(R.id.iv_change_pw_back);

        ivBack.setOnClickListener(v -> {
            // go back to previous fragment
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        btnConfirm.setOnClickListener(v -> {
            String current = etCurrent.getText().toString().trim();
            String nw = etNew.getText().toString().trim();
            String confirm = etConfirm.getText().toString().trim();

            if (TextUtils.isEmpty(current)) {
                Toast.makeText(getContext(), "Vui lòng nhập mật khẩu hiện tại", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(nw)) {
                Toast.makeText(getContext(), "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nw.length() < 6) {
                Toast.makeText(getContext(), "Mật khẩu mới phải có ít nhất 6 kí tự", Toast.LENGTH_SHORT).show();
                return;
            }
            // basic rule: contains letter and digit
            if (!nw.matches("(?=.*[0-9])(?=.*[A-Za-z]).+")) {
                Toast.makeText(getContext(), "Mật khẩu phải chứa ít nhất 1 chữ và 1 số", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!nw.equals(confirm)) {
                Toast.makeText(getContext(), "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button while processing
            btnConfirm.setEnabled(false);
            btnConfirm.setText(getString(R.string.processing));

            // Create request and call API
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword(current)
                    .newPassword(nw)
                    .confirmNewPassword(confirm)
                    .build();

            ApiService api = ApiClient.getApiService();
            Call<ApiResponse<Void>> call = api.changePassword(request);

            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText(getString(R.string.confirm));

                    // If HTTP 2xx and body present
                    if (response.isSuccessful()) {
                        ApiResponse<Void> body = response.body();
                        if (body != null) {
                            if (body.isSuccess()) {
                                Toast.makeText(getContext(), getString(R.string.change_pw_success), Toast.LENGTH_SHORT).show();
                                if (getActivity() != null) {
                                    getActivity().getSupportFragmentManager().popBackStack();
                                }
                                return;
                            } else {
                                // Backend returned structured error in 2xx
                                String friendly = mapErrorCodeToMessage(body.getCode(), body.getMessage());
                                Toast.makeText(getContext(), friendly, Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        // No body: generic fallback
                        Toast.makeText(getContext(), getString(R.string.change_pw_failed) + ": Lỗi không xác định từ server.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Non-2xx: try to parse error body which may still be structured ApiResponse
                    try (okhttp3.ResponseBody eb = response.errorBody()) {
                        if (eb != null) {
                            String err = eb.string();
                            ApiResponse<?> errResp = gson.fromJson(err, ApiResponse.class);
                            if (errResp != null) {
                                String friendly = mapErrorCodeToMessage(errResp.getCode(), errResp.getMessage());
                                Toast.makeText(getContext(), friendly, Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    } catch (Exception ex) {
                        // ignore parsing errors
                    }

                    // final fallback: show HTTP code
                    Toast.makeText(getContext(), getString(R.string.change_pw_failed) + ": HTTP " + response.code(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText(getString(R.string.confirm));
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        });

        return view;
    }

    private String mapErrorCodeToMessage(Integer code, String serverMessage) {
        if (code == null) return (serverMessage == null || serverMessage.isEmpty()) ? getString(R.string.change_pw_failed) : serverMessage;
        switch (code) {
            case 1014:
                return "Mật khẩu mới và mật khẩu xác nhận không khớp.";
            case 1015:
                return "Mật khẩu mới phải khác mật khẩu hiện tại.";
            default:
                if (serverMessage != null && !serverMessage.isEmpty()) return serverMessage;
                return getString(R.string.change_pw_failed);
        }
    }
}
