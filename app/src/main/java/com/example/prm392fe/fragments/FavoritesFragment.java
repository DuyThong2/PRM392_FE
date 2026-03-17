package com.example.prm392fe.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.prm392fe.R;
import com.example.prm392fe.activities.MainActivity;

public class FavoritesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.favorites_title));
        try {
            toolbar.setNavigationIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back));
        } catch (Exception ignored) { }
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
        });

        View btn = view.findViewById(R.id.btn_shop_now);
        btn.setOnClickListener(v -> {
            if (getActivity() != null) startActivity(new Intent(getActivity(), MainActivity.class));
        });

        return view;
    }
}
