package com.example.prm392fe.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.example.prm392fe.R;
import com.example.prm392fe.SessionManager;
import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.api.ApiService;
import com.example.prm392fe.databinding.ActivityLoginOptionsBinding;
import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.requests.GoogleTokenRequest;
import com.example.prm392fe.models.responses.AuthenticationResponse;
import com.example.prm392fe.utils.MyUtils;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginOptionsActivity extends AppCompatActivity {

    ActivityLoginOptionsBinding binding;

    static final String TAG = "LOGIN_OPTIONS_TAG";

    //ProgressDialog to show while google sign in
    ProgressDialog progressDialog;

    GoogleSignInClient mGoogleSignInClient;

    ApiService api = ApiClient.getApiService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginOptionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- Tránh vùng camera (notch) ---
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, bars.top, 0, 0);
            return insets;
        });

        // Init/Setup ProgressDialog to show while sign-in
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // default_web_client_id này phải đặt đúng với client-id của web user phía backend
                // setup trong res/values/strings.xml
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.loginEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginEmailActivity();
            }
        });

        // Handle loginGoogleBtn click, begin google sign in
        binding.loginGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginGoogleLogin();
            }
        });

    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void startLoginEmailActivity() {
        startActivity(new Intent(this, LoginEmailActivity.class));
    }

    private void beginGoogleLogin() {
        Log.d(TAG, "beginGoogleLogin: ");
        Intent googleSignInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInARL.launch(googleSignInIntent);
    }

    private ActivityResultLauncher<Intent> googleSignInARL = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: "+result.getResultCode());
                    // handle google signIn result here
                    if (result.getResultCode() == LoginOptionsActivity.RESULT_OK) {

                        // get
                        Intent data = result.getData();

                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                        try {
                            // Google signIn was successful, authenticate with Firebase
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Log.d(TAG, "onActivityResult: AccountID: "+account.getId());

                            api.loginGoogle(GoogleTokenRequest.builder()
                                    .idToken(account.getIdToken())
                                    .build()).enqueue(new Callback<ApiResponse<AuthenticationResponse>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<AuthenticationResponse>> call, Response<ApiResponse<AuthenticationResponse>> response) {
                                    Log.i(TAG, "onResponse: "+response.body().toString());
                                    SessionManager.getInstance(LoginOptionsActivity.this).saveAuthToken(response.body().getResult().getToken());
                                    Toast.makeText(LoginOptionsActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                                    startMainActivity();
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<AuthenticationResponse>> call, Throwable t) {
                                    Toast.makeText(LoginOptionsActivity.this, "error occured!", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "onFailure: ", t);
                                }
                            });

                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    } else {
                        // cancelled from Google signIn options/confirmation dialog
                        Log.d(TAG, "onActivityResult: Cancelled...!");
                        MyUtils.toast(LoginOptionsActivity.this, "Cancelled...!");
                    }
                }
            }
    );
}