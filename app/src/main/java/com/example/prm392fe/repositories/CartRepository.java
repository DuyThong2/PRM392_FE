package com.example.prm392fe.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.responses.CartResponse;
import com.example.prm392fe.models.requests.UpdateCartRequest;
import com.google.gson.Gson;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CartRepository {
    private static CartRepository instance;
    private String TAG = "CART_REPOSITORY_TAG";

    private CartRepository() {}

    public static synchronized CartRepository getInstance() {
        if (instance == null) {
            instance = new CartRepository();
        }
        return instance;
    }

    public LiveData<CartResponse> getCartByUserId(int userId) {
        MutableLiveData<CartResponse> data = new MutableLiveData<>();

        ApiClient.getApiService().getCartByUserId(userId)
                .enqueue(new Callback<ApiResponse<CartResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<CartResponse>> call,
                                           Response<ApiResponse<CartResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<CartResponse> apiResponse = response.body();

                            Log.d("CartRepository", "✅ Full Response: " + new Gson().toJson(apiResponse));

                            if (apiResponse.isSuccess() && apiResponse.getResult() != null) {
                                data.setValue(apiResponse.getResult());
                                Log.d("CartRepository", "✅ Loaded cart successfully for user: " + userId);
                            } else {
                                data.setValue(null);
                                Log.w("CartRepository", "⚠️ No cart found or API returned null result");
                            }

                        } else {
                            try {
                                Log.e("CartRepository", "❌ API failed: " + response.message() +
                                        " | Error body: " + response.errorBody().string());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CartResponse>> call, Throwable t) {
                        Log.e("CartRepository", "🚨 Error loading cart: " + t.getMessage(), t);
                    }
                });

        return data;
    }

    public LiveData<Boolean> addToCart(int userId, int productId) {
        MutableLiveData<Boolean> data = new MutableLiveData<>();

        ApiClient.getApiService().addToCart(productId, userId)
                .enqueue(new Callback<ApiResponse<Boolean>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Boolean>> call,
                                           Response<ApiResponse<Boolean>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Boolean> apiResponse = response.body();

                            Log.d(TAG, "✅ Add to cart response: " + new Gson().toJson(apiResponse));

                            if (apiResponse.isSuccess()) {
                                data.setValue(apiResponse.getResult());
                                Log.d(TAG, "✅ Product added to cart successfully");

                            } else {
                                data.setValue(false);
                                Log.w(TAG, "⚠️ Failed to add product to cart: " + apiResponse.getMessage());
                            }
                        } else {
                            data.setValue(false);
                            try {
                                Log.e(TAG, "❌ API failed: " + response.message() +
                                        " | Error body: " + (response.errorBody() != null ? response.errorBody().string() : ""));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                        data.setValue(false);
                        Log.e(TAG, "🚨 Error adding to cart: " + t.getMessage(), t);
                    }
                });

        return data;
    }

    public LiveData<CartResponse> updateCart(int userId, int cartId, java.util.List<UpdateCartRequest.CartItemRequest> items) {
        MutableLiveData<CartResponse> data = new MutableLiveData<>();

        UpdateCartRequest request = UpdateCartRequest.builder()
                .cartId(cartId)
                .items(items)
                .build();

        ApiClient.getApiService().updateCart(userId, request)
                .enqueue(new Callback<ApiResponse<CartResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<CartResponse>> call,
                                           Response<ApiResponse<CartResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<CartResponse> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getResult() != null) {
                                data.setValue(apiResponse.getResult());
                                Log.d(TAG, "✅ Cart updated successfully");
                            } else {
                                data.setValue(null);
                                Log.w(TAG, "⚠️ Failed to update cart: " + apiResponse.getMessage());
                            }
                        } else {
                            data.setValue(null);
                            Log.e(TAG, "❌ Update cart API failed: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CartResponse>> call, Throwable t) {
                        data.setValue(null);
                        Log.e(TAG, "🚨 Error updating cart: " + t.getMessage(), t);
                    }
                });

        return data;
    }

    public LiveData<Boolean> removeFromCart(int userId, int productId) {
        MutableLiveData<Boolean> data = new MutableLiveData<>();

        ApiClient.getApiService().removeFromCart(productId, userId)
                .enqueue(new Callback<ApiResponse<Boolean>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Boolean>> call,
                                           Response<ApiResponse<Boolean>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Boolean> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                data.setValue(apiResponse.getResult());
                                Log.d(TAG, "✅ Product removed from cart successfully");
                            } else {
                                data.setValue(false);
                                Log.w(TAG, "⚠️ Failed to remove product from cart: " + apiResponse.getMessage());
                            }
                        } else {
                            data.setValue(false);
                            Log.e(TAG, "❌ Remove from cart API failed: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                        data.setValue(false);
                        Log.e(TAG, "🚨 Error removing from cart: " + t.getMessage(), t);
                    }
                });

        return data;
    }

}
