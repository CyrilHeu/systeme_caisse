package com.example.tablettegourmande.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.tablettegourmande.ui.setup.GeneralInfoFragment;
import com.example.tablettegourmande.ui.setup.AdditionalInfoFragment;
import com.example.tablettegourmande.ui.setup.ConfigurationFragment;

public class SetupPagerAdapter extends FragmentStateAdapter {

    public SetupPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new GeneralInfoFragment();
            case 1:
                return new AdditionalInfoFragment();
            case 2:
                return new ConfigurationFragment();
            case 3:
                //return new ProfessionalInfoFragment(); // Dernier fragment
            default:
                return new GeneralInfoFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Quatre fragments au total
    }
}
