package com.example.prm392fe.api;



import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.PageResponse;
import com.example.prm392fe.models.requests.AuthenticationRequest;
import com.example.prm392fe.models.requests.ChangePasswordRequest;
import com.example.prm392fe.models.requests.CreateOrderRequest;
import com.example.prm392fe.models.requests.DeleteAccountRequest;
import com.example.prm392fe.models.requests.GoogleTokenRequest;
import com.example.prm392fe.models.requests.LogoutRequest;
import com.example.prm392fe.models.requests.ResetPasswordRequest;
import com.example.prm392fe.models.requests.UpdateReadMessageRequest;
import com.example.prm392fe.models.requests.UserProfileUpdateRequest;
import com.example.prm392fe.models.requests.UserRegisterRequest;
import com.example.prm392fe.models.responses.AuthenticationResponse;
import com.example.prm392fe.models.responses.ConversationResponse;
import com.example.prm392fe.models.responses.CountResponse;
import com.example.prm392fe.models.responses.LocationResponse;
import com.example.prm392fe.models.responses.MessageResponse;
import com.example.prm392fe.models.responses.OrderResponse;
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

    @PUT("user/me")
    Call<ApiResponse<UserResponse>> updateMyProfile(@Body UserProfileUpdateRequest request);

    @retrofit2.http.HTTP(method = "DELETE", path = "user/me", hasBody = true)
    Call<ApiResponse<Void>> deleteMyAccount(@Body DeleteAccountRequest request);


    @GET("order/user/{id}")
    Call<ApiResponse<PageResponse<OrderResponse>>> getOrdersByUserId(@Path("id") int id);

    @GET("order/status/user/{id}")
    Call<ApiResponse<PageResponse<OrderResponse>>> getOrdersByStatusAndUserId(@Path("id") int id,
                                                                              @Query("status") String status);

    @GET("order/{id}")
    Call<ApiResponse<OrderResponse>> getOrderDetail(@Path("id") int id);

    @GET("order/today")
    Call<ApiResponse<PageResponse<OrderResponse>>> getOrdersToday(
            @Query("page") int page,
            @Query("size") int size,
            @Query("sortBy") String sortBy,
            @Query("sortDir") String sortDir,
            @Query("status") String status
    );
    @PUT("order/{id}/status")
    Call<ApiResponse<OrderResponse>> updateOrderStatus(
            @Path("id") int id,
            @Query("status") String status
    );

    @POST("order")
    Call<ApiResponse<OrderResponse>> createOrder(@Body CreateOrderRequest request, @Query("status") boolean status);



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

    // ----------------------------------LOCATION---------------------------------------
    @GET("api/locations")
    Call<ApiResponse<List<LocationResponse>>> getLocations();

    @GET("api/conversations/list/{staffId}")
    Call<ApiResponse<List<ConversationResponse>>> getConversations(@Path("staffId") int id);



}