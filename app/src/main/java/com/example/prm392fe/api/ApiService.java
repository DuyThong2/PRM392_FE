package com.example.prm392fe.api;

import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.PageResponse;
import com.example.prm392fe.models.requests.AuthenticationRequest;
import com.example.prm392fe.models.requests.ChangePasswordRequest;
import com.example.prm392fe.models.requests.CreateOrderRequest;
import com.example.prm392fe.models.requests.DeleteAccountRequest;
import com.example.prm392fe.models.requests.GoogleTokenRequest;
import com.example.prm392fe.models.requests.LogoutRequest;
import com.example.prm392fe.models.requests.PaymentRequest;
import com.example.prm392fe.models.requests.ResetPasswordRequest;
import com.example.prm392fe.models.requests.UpdateCartRequest;
import com.example.prm392fe.models.requests.UserProfileUpdateRequest;
import com.example.prm392fe.models.requests.UserRegisterRequest;
import com.example.prm392fe.models.responses.AuthenticationResponse;
import com.example.prm392fe.models.responses.CartResponse;
import com.example.prm392fe.models.responses.CountResponse;
import com.example.prm392fe.models.responses.LocationResponse;
import com.example.prm392fe.models.responses.OrderResponse;
import com.example.prm392fe.models.responses.ProductResponse;
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

    //-----------------------------------PRODUCT------------------------------------------
    @GET("api/products")
    @SkipAuth
    Call<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @Query("sort") String sort
    );

    @GET("api/products/{id}")
    Call<ApiResponse<ProductResponse>> getProduct(@Path("id") int id);

    @GET("api/products/category/{categoryId}")
    Call<ApiResponse<PageResponse<ProductResponse>>> getProductsByCategory(
            @Path("categoryId") int categoryId,
            @Query("page") int page,
            @Query("size") int size);

    // -----------------------------------ORDER-------------------------------------------
    @GET("order/user/{id}")
    Call<ApiResponse<PageResponse<OrderResponse>>> getOrdersByUserId(@Path("id") int id);

    @GET("order/status/user/{id}")
    Call<ApiResponse<PageResponse<OrderResponse>>> getOrdersByStatusAndUserId(@Path("id") int id,
                                                                              @Query("status") String status);

    @GET("order/{id}")
    Call<ApiResponse<OrderResponse>> getOrderDetail(@Path("id") int id);

    @GET("order")
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

    //---------------------------------PAYMENT------------------------------------------
    @POST("payment/vnpay-create-payment")
    Call<ApiResponse<Map<String, String>>> createVNPAYPayment(@Body PaymentRequest request);
    @POST("payment/momo-create-payment")
    Call<ApiResponse<Map<String, String>>> createMomoPayment(@Body PaymentRequest request);

    //-----------------------------------CART-------------------------------------------
    @GET("cart/user/{id}")
    Call<ApiResponse<CartResponse>> getCartByUserId(@Path("id") int id);

    @POST("cart")
    Call<ApiResponse<Boolean>> addToCart(@Query("productId") int productId, @Query("userId") int userId);

    @PUT("cart/user/{id}")
    Call<ApiResponse<CartResponse>> updateCart(@Path("id") int userId, @Body UpdateCartRequest request);

    @DELETE("cart")
    Call<ApiResponse<Boolean>> removeFromCart(@Query("productId") int productId, @Query("userId") int userId);

    @GET("cart/total-quantity/{customerId}")
    Call<ApiResponse<CountResponse>> totalItemsQuantityByCustomerId(@Path("customerId") int customerId);

    // -------------------------------DASHBOARD--------------------------------------
    @GET("/order/todays/count")
    Call<ApiResponse<Long>> getTodaysOrderCount();

    @GET("/order/pending/count")
    Call<ApiResponse<Long>> getPendingOrdersCount();

    @GET("/api/products/low-stock-count")
    Call<Long> getLowStockCount(@Query("threshold") int threshold);

    // ----------------------------------LOCATION---------------------------------------
    @GET("api/locations")
    Call<ApiResponse<List<LocationResponse>>> getLocations();

}