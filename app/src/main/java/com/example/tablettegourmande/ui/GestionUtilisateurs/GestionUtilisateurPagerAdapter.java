package com.example.tablettegourmande.ui.GestionUtilisateurs;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class GestionUtilisateurPagerAdapter extends FragmentStateAdapter {

    public GestionUtilisateurPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PageGestionUtilisateurs(); // La page que tu as déjà
            case 1:
                return new PageGestionRoles();       // À implémenter
            case 2:
                return new PageGestionAutorisations(); // À implémenter
            default:
                return new PageGestionUtilisateurs();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
