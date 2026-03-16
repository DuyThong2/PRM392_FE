package com.example.prm392fe;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.utils.MyUtils;

import org.json.JSONObject;

// Dùng Singleton pattern
// Tạo file SessionManager.java
public class SessionManager {

    private static final String PREF_NAME = "GundamShopSession";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private String userId;
    private String role;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    private final MyUtils myUtils = new MyUtils();

    private static SessionManager instance;

    private SessionManager(Context context) {
        this.context = context.getApplicationContext(); // Dùng application context để tránh memory leak
        sharedPreferences = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String savedToken = sharedPreferences.getString(KEY_AUTH_TOKEN, null);
        if(savedToken != null){
            ApiClient.setToken(savedToken);
            decodeAndCacheUserData(savedToken);
        }
    }

    // Từ khóa synchronized đảm bảo rằng chỉ một luồng được phép thực thi nội dung của phương thức này tại một thời điểm.
    // public static synchronized SessionManager getInstance(Context context) {
    public static SessionManager getInstance(Context context) {
        // design pattern Singleton
        if (instance == null) {
            // Chỉ khóa khi instance thực sự null
            // thay vì synchronized đặt ở ngoài thì cứ getInstance thì sẽ phải chờ bất kể instance đó null hay ko null --> giảm Hiệu Suất
            synchronized (SessionManager.class) {
                // Lần kiểm tra thứ hai (trong vùng khóa)
                if (instance == null) {
                    instance = new SessionManager(context);
                }
            }
        }
        return instance;
    }

    // --- Các phương thức quan trọng ---
    private void saveUserDate(String token) {
        if (token == null) {
            userId = null;
            role = null;
            return;
        }

        JSONObject payload = myUtils.decodeJwtPayload(token);
        if (payload != null) {
            this.userId = payload.optString("id", null);
            this.role = payload.optString("role", null);
        }
    }

    public void saveAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply(); // Dùng apply() để lưu bất đồng bộ
        ApiClient.setToken(token);
        saveUserDate(token);
        Log.i("Token", token);
        Log.i("id: ", this.userId);
        Log.i("role: ", this.role);
    }

    private void decodeAndCacheUserData(String token) {
        if (token == null) {
            userId = null;
            role = null;
            return;
        }

        JSONObject payload = myUtils.decodeJwtPayload(token);
        if (payload != null) {
            this.userId = payload.optString("id", null);
            this.role = payload.optString("role", null);
            Log.i("SessionManager", "Decoded: id=" + userId + ", role=" + role);
        } else {
            Log.w("SessionManager", "Cannot decode JWT payload!");
        }
    }

    public String getAuthToken() {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null); // Trả về null nếu không có token
    }

    public String getRole(){
        return this.role;
    }

    public String getUserId(){
        return this.userId;
    }

    public boolean isLoggedIn() {
        return getAuthToken() != null;
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
