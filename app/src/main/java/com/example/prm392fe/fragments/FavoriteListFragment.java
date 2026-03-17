package com.example.prm392fe.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.prm392fe.R;

public class FavoriteListFragment extends Fragment {

    Button btnBuyCart;

    public FavoriteListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_favorite_list, container, false);
//
//        // Ánh xạ button sau khi inflate view
//        btnBuyCart = view.findViewById(R.id.btnBuyCart);
//
//        btnBuyCart.setOnClickListener(v -> {
//            Intent intent = new Intent(requireContext(), CheckoutActivity.class);
//            startActivity(intent);
//        });

        return inflater.inflate(R.layout.fragment_favorite_list, container, false);
    }
}