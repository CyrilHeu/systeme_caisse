package com.example.tablettegourmande.ui.GestionUtilisateurs;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tablettegourmande.models.Role;

import java.util.List;

public class RolePagerAdapter extends FragmentStateAdapter {

    private final List<Role> roles;
    private final String restaurantId;

    public RolePagerAdapter(@NonNull FragmentActivity fragmentActivity,
                            List<Role> roles,
                            String restaurantId) {
        super(fragmentActivity);
        this.roles = roles;
        this.restaurantId = restaurantId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return FragmentPermissionParRole.newInstance(roles.get(position).getId(), restaurantId);
    }

    @Override
    public int getItemCount() {
        return roles.size();
    }
}
