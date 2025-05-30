package com.example.tablettegourmande.ui.GestionUtilisateurs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tablettegourmande.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FragmentUtilisateur extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    public FragmentUtilisateur() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_utilisateur, container, false);

        viewPager = view.findViewById(R.id.viewPagerUtilisateur);
        tabLayout = view.findViewById(R.id.tabLayoutUtilisateur);

        GestionUtilisateurPagerAdapter adapter = new GestionUtilisateurPagerAdapter(requireActivity());
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Utilisateurs");
                    break;
                case 1:
                    tab.setText("Rôles");
                    break;
                case 2:
                    tab.setText("Autorisations");
                    break;
            }
        }).attach();

        return view;
    }
}

