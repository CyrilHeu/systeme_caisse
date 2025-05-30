package com.example.tablettegourmande.ui.restaurant.ticket;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tablettegourmande.R;

public class FragmentClavierPaiement extends Fragment {

    private String montantActuel = "";
    private OnMontantSaisiListener listener;

    public FragmentClavierPaiement() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.restaurant_clavier_paiement, container, false);

        // Récupérer FragmentTicket comme listener
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof OnMontantSaisiListener) {
            listener = (OnMontantSaisiListener) parentFragment;
        } else {
            throw new RuntimeException("Le fragment parent doit implémenter OnMontantSaisiListener");
        }

        // Initialisation des boutons
        int[] buttonIds = {
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3,
                R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7,
                R.id.btn_8, R.id.btn_9, R.id.btn_virgule, R.id.btn_clear
        };

        for (int id : buttonIds) {
            Button button = view.findViewById(id);
            button.setOnClickListener(this::onButtonClick);
        }

        return view;
    }

    private void onButtonClick(View v) {
        Button button = (Button) v;
        String buttonText = button.getText().toString();

        if (buttonText.equals("C")) {
            montantActuel = "";
        } else {
            if (buttonText.equals(",") && montantActuel.contains(",")) {
                return; // Empêcher plusieurs virgules
            }
            montantActuel += buttonText;
        }
        // Envoyer la mise à jour à FragmentTicket
        if (listener != null) {
            listener.onMontantSaisi(montantActuel);
        }
    }

    public interface OnMontantSaisiListener {
        void onMontantSaisi(String montant);
    }
    public void resetMontant() {
        montantActuel = ""; // Réinitialisation de la variable interne
    }
}




