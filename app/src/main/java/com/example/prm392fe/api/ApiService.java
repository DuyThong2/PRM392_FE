package com.example.prm392fe.api;



import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.requests.AuthenticationRequest;
import com.example.prm392fe.models.requests.ChangePasswordRequest;
import com.example.prm392fe.models.requests.GoogleTokenRequest;
import com.example.prm392fe.models.requests.LogoutRequest;
import com.example.prm392fe.models.requests.ResetPasswordRequest;
import com.example.prm392fe.models.requests.UpdateReadMessageRequest;
import com.example.prm392fe.models.responses.AuthenticationResponse;
import com.example.prm392fe.models.responses.ConversationResponse;
import com.example.prm392fe.models.responses.CountResponse;
import com.example.prm392fe.models.responses.MessageResponse;
import com.example.prm392fe.models.responses.UserResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    //-------------------------------AUTHENTICATION--------------------------------------
    // Mấy cái login, register ko cần @SkipAuth. Do cơ chế bên ApiClient ko có token thì ko cần auth
    @POST("auth/log-in")
    Call<ApiResponse<AuthenticationResponse>> login(@Body AuthenticationRequest request);

    @POST("auth/google-android")
    Call<ApiResponse<AuthenticationResponse>> loginGoogle(@Body GoogleTokenRequest request);

    @POST("user/register")
    Call<ApiResponse<UserResponse>> register(@Body UserRegisterRequest request);

    @GET("user/myInfo")
    Call<ApiResponse<UserResponse>> getInfo();

    @POST("/auth/logout")
    Call<ApiResponse<Void>> logout(@Body LogoutRequest request);

    @POST("/auth/change-password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest request);

    // Forgot / Reset password
    @POST("auth/forgot-password")
    Call<ApiResponse<Void>> forgotPassword(@Query("email") String email);

    @POST("auth/reset-password")
    Call<ApiResponse<String>> resetPassword(@Body ResetPasswordRequest request);

    // Verify reset code (accepts email+code). Uses a Map payload in callers, so expose a Map<String,String> body here.
    @POST("auth/verify-reset-code")
    Call<ApiResponse<Void>> verifyResetCode(@Body java.util.Map<String, String> payload);



    @GET("api/conversations/{customerId}")
    Call<ApiResponse<ConversationResponse>> getConversationByCustomerId(@Path("customerId") int id);

    @GET("cart/total-quantity/{customerId}")
    Call<ApiResponse<CountResponse>> totalItemsQuantityByCustomerId(@Path("customerId") int customerId);

    // ----------------------------------MESSAGE----------------------------------------
    @GET("api/messages/{customerId}")
    Call<ApiResponse<List<MessageResponse>>> getMessages(@Path("customerId") int id);

    @GET("api/messages/unread/{receiverId}")
    Call<ApiResponse<CountResponse>> countUnreadMessages(@Path("receiverId") int id);

    @PUT("api/messages/read")
    Call<ApiResponse<CountResponse>> updateReadMessages(@Body UpdateReadMessageRequest request);



}