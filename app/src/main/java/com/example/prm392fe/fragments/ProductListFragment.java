package com.example.prm392fe.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.prm392fe.activities.CartActivity;
import com.example.prm392fe.activities.ProductDetailActivity;
import com.example.prm392fe.adapter.ProductListAdapter;
import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.models.responses.ProductResponse;
import com.example.prm392fe.ui.theme.GridSpacingItemDecoration;
import com.example.prm392fe.R;
import com.example.prm392fe.SessionManager;
import com.example.prm392fe.viewModel.ProductListViewModel;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class ProductListFragment extends Fragment {

    private ProductListViewModel viewModel;
    private ProductListAdapter adapter;
    private List<ProductResponse> allProducts = new ArrayList<>();

    final String TAG = "PRODUCT_LIST_FRAGMENT";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);

        // Ánh xạ view
        RecyclerView rvProducts = view.findViewById(R.id.rvProducts);
        EditText etSearch = view.findViewById(R.id.etSearch);
        ImageView ivCart = view.findViewById(R.id.ivCart);
        ImageView ivFilter = view.findViewById(R.id.ivFilter);
        ImageView ivChat = view.findViewById(R.id.ivChat);

        // Khởi tạo adapter
        adapter = new ProductListAdapter();
        adapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });

        rvProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        rvProducts.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));
        rvProducts.setAdapter(adapter);

        ivFilter.setOnClickListener(v -> openSortDialog());

        // ViewModel
        viewModel = new ViewModelProvider(this).get(ProductListViewModel.class);
        observeProducts("price,asc");

        // Search listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Cart click listener
        ivCart.setOnClickListener(v -> startActivity(new Intent(requireContext(), CartActivity.class)));

        // Chat click listener (đã xóa WebSocket)
        ivChat.setOnClickListener(v -> {
            // Xóa toàn bộ logic liên quan đến Conversation
            startActivity(new Intent(requireContext(), CartActivity.class));
        });

        return view;
    }

    private void filterProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.setProducts(allProducts);
            return;
        }

        List<ProductResponse> filtered = new ArrayList<>();
        for (ProductResponse p : allProducts) {
            if (p.getName() != null && p.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(p);
            }
        }
        adapter.setProducts(filtered);
    }

    private void openSortDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_sort_product, null);

        TextView sortNameAsc = sheet.findViewById(R.id.sortNameAsc);
        TextView sortNameDesc = sheet.findViewById(R.id.sortNameDesc);
        TextView sortPriceAsc = sheet.findViewById(R.id.sortPriceAsc);
        TextView sortPriceDesc = sheet.findViewById(R.id.sortPriceDesc);

        sortNameAsc.setOnClickListener(v -> {
            observeProducts("name,asc");
            dialog.dismiss();
        });

        sortNameDesc.setOnClickListener(v -> {
            observeProducts("name,desc");
            dialog.dismiss();
        });

        sortPriceAsc.setOnClickListener(v -> {
            observeProducts("price,asc");
            dialog.dismiss();
        });

        sortPriceDesc.setOnClickListener(v -> {
            observeProducts("price,desc");
            dialog.dismiss();
        });

        dialog.setContentView(sheet);
        dialog.show();
    }

    private void observeProducts(String sort) {
        viewModel.getProducts(sort).observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                allProducts.clear();
                allProducts.addAll(products);
                adapter.setProducts(products);
                Log.d("ProductListFragment", "Updated " + products.size() + " products");
            }
        });
    }
}