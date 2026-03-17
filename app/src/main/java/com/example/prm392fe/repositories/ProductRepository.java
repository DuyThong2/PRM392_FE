package com.example.prm392fe.repositories;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.api.ApiService;
import com.example.prm392fe.models.ApiResponse;
import com.example.prm392fe.models.PageResponse;
import com.example.prm392fe.models.responses.ProductResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {
    private final ApiService apiService = ApiClient.getApiService();
//
//    public ProductRepository() {
//        this.apiService = apiService;
//    }

    public LiveData<List<ProductResponse>> getProducts(String sort) {
        MutableLiveData<List<ProductResponse>> data = new MutableLiveData<>();

        apiService.getProducts(sort).enqueue(new Callback<ApiResponse<PageResponse<ProductResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<ProductResponse>>> call, Response<ApiResponse<PageResponse<ProductResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    data.setValue(response.body().getResult().getContent()); // ✅ getResult() bây giờ trả về List<ProductResponse>
                } else {
                    Log.e("ProductRepository", "API failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<ProductResponse>>> call, Throwable t) {
                Log.e("ProductRepository", "Error: " + t.getMessage());
            }
        });

        return data;
    }


    public LiveData<ProductResponse> getProductDetail(int productId) {
        MutableLiveData<ProductResponse> data = new MutableLiveData<>();

        apiService.getProduct(productId).enqueue(new Callback<ApiResponse<ProductResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductResponse>> call,
                                   Response<ApiResponse<ProductResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.postValue(response.body().getResult());
                } else {
                    data.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductResponse>> call, Throwable t) {
                data.postValue(null);
            }
        });

        return data;
    }

    public LiveData<List<ProductResponse>> getRelatedProducts(int categoryId) {
        MutableLiveData<List<ProductResponse>> data = new MutableLiveData<>();

        Log.d("ProductRepo", "👉 getRelatedProducts called with categoryId = " + categoryId);

        apiService.getProductsByCategory(categoryId, 0, 10)
                .enqueue(new Callback<ApiResponse<PageResponse<ProductResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PageResponse<ProductResponse>>> call,
                                           Response<ApiResponse<PageResponse<ProductResponse>>> response) {
                        Log.d("ProductRepo", "✅ onResponse called");
                        if (response.isSuccessful() && response.body() != null) {
                            PageResponse<ProductResponse> page = response.body().getResult();
                            if (page != null && page.getContent() != null) {
                                Log.d("ProductRepo", "🟢 Loaded " + page.getContent().size() + " products");
                                data.setValue(page.getContent());
                            } else {
                                Log.e("ProductRepo", "⚠️ Page or content is null");
                                data.setValue(new ArrayList<>());
                            }
                        } else {
                            Log.e("ProductRepo", "❌ API failed: code = " + response.code());
                            if (response.errorBody() != null) {
                                try {
                                    Log.e("ProductRepo", "Error body: " + response.errorBody().string());
                                } catch (Exception e) {
                                    Log.e("ProductRepo", "Error reading errorBody", e);
                                }
                            }
                            data.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PageResponse<ProductResponse>>> call, Throwable t) {
                        Log.e("ProductRepo", "🔥 API call failed: " + t.getMessage(), t);
                        data.setValue(null);
                    }
                });

        return data;
    }
}
