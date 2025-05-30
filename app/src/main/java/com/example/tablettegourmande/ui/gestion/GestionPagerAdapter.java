package com.example.tablettegourmande.ui.gestion;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class GestionPagerAdapter extends FragmentStateAdapter {

    public GestionPagerAdapter(Fragment fragment) {
        super(fragment);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PageGestionProduit();
            case 1:
                return new PageGestionCategorie();
            case 2:
                return new PageGestionOption();
            case 3:
                return new PageGestionMenu();
            default:
                return new PageGestionProduit();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Nombre total d'onglets
    }
}

