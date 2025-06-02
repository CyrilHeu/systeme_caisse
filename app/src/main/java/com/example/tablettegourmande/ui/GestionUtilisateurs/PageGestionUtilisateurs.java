package com.example.tablettegourmande.ui.GestionUtilisateurs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Utilisateur;
import com.example.tablettegourmande.services.UtilisateurService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PageGestionUtilisateurs extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAjouter;
    private UtilisateurAdapter adapter;
    private UtilisateurService utilisateurService;
    private EditText editTextRecherche;

    private String restaurantId;
    private List<Utilisateur> listeComplete = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_gestion_utilisateur, container, false);

        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        restaurantId = prefs.getString("restaurantId", null);

        recyclerView = view.findViewById(R.id.recyclerViewUtilisateurs);
        fabAjouter = view.findViewById(R.id.fabAjouterUtilisateur);
        editTextRecherche = view.findViewById(R.id.editTextRechercheUtilisateur);

        adapter = new UtilisateurAdapter(getContext(), listeComplete, restaurantId);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.setAdapter(adapter);

        utilisateurService = new UtilisateurService(restaurantId, new UtilisateurService.OnUtilisateurChangeListener() {
            @Override
            public void onUtilisateursChange(List<Utilisateur> utilisateurs) {
                listeComplete = utilisateurs;
                adapter.updateList(utilisateurs);
            }
        });


        fabAjouter.setOnClickListener(v -> DialogGestionUtilisateur.ouvrirDialog(null, restaurantId, getContext(), false));

        editTextRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrer(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

}
