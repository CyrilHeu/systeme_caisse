package com.example.tablettegourmande.ui.gestion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tablettegourmande.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FragmentGestion extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gestion, container, false);

        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        // Vérifier que l'adaptateur est bien défini
        GestionPagerAdapter adapter = new GestionPagerAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                page.setAlpha(1 - Math.abs(position) * 0.3f);
                page.setScaleY(1 - Math.abs(position) * 0.1f);
            }
        });

        // Lier le TabLayout au ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Produits");
                    break;
                case 1:
                    tab.setText("Catégories");
                    break;
                case 2:
                    tab.setText("Options");
                    break;
                case 3:
                    tab.setText("Menus");
                    break;
            }
        }).attach();

        return view;
    }
}

