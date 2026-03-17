package com.example.prm392fe.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.prm392fe.models.responses.CartResponse;
import com.example.prm392fe.repositories.CartRepository;


public class CartViewModel extends ViewModel {
    private final CartRepository cartRepository = CartRepository.getInstance();

    // LiveData to hold current cart state
    private MutableLiveData<CartResponse> currentCart = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<CartResponse> getCartsByUserId(int userId) {
        return cartRepository.getCartByUserId(userId);
    }

    public LiveData<Boolean> addToCart(int userId, int productId) {
        isLoading.setValue(true);
        return cartRepository.addToCart(userId, productId);
    }

    public LiveData<CartResponse> updateCart(int userId, int cartId, java.util.List<com.example.prm392fe.models.requests.UpdateCartRequest.CartItemRequest> items) {
        isLoading.setValue(true);
        return cartRepository.updateCart(userId, cartId, items);
    }

    public LiveData<Boolean> removeFromCart(int userId, int productId) {
        isLoading.setValue(true);
        return cartRepository.removeFromCart(userId, productId);
    }



    // Getters for LiveData
    public LiveData<CartResponse> getCurrentCart() {
        return currentCart;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Helper methods
    public void setCurrentCart(CartResponse cart) {
        currentCart.setValue(cart);
        isLoading.setValue(false);
    }

    public void setError(String error) {
        errorMessage.setValue(error);
        isLoading.setValue(false);
    }

    public void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }
}
