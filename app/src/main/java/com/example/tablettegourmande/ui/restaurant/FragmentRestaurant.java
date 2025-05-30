package com.example.tablettegourmande.ui.restaurant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.ui.restaurant.selection.FragmentSelection;
import com.example.tablettegourmande.ui.restaurant.ticket.FragmentTicket;

public class FragmentRestaurant extends Fragment {

    public FragmentRestaurant() {
        // Constructeur vide requis
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.restaurant_fragment, container, false);

        // Chargement des sous-fragments
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_selection_container, new FragmentSelection());
            transaction.replace(R.id.fragment_ticket_container, new FragmentTicket());
            transaction.commit();
        }

        return view;
    }
}
