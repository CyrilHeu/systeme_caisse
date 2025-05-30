package com.example.tablettegourmande.ui.gestion;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.adapters.CategorieAdapter;
import com.example.tablettegourmande.models.Categorie;
import com.example.tablettegourmande.services.CategorieService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Utils.CustomToast;
import Utils.Normalize;

public class PageGestionCategorie extends Fragment implements CategorieAdapter.OnCategorieActionListener {
    private RecyclerView recyclerView;
    private CategorieAdapter adapter;
    private List<Categorie> categories = new ArrayList<>();
    private CategorieService categorieService;
    private String restaurantId;
    private EditText editTextRecherche;
    private Spinner spinnerTri;
    private List<Categorie> categoriesOriginales = new ArrayList<>();
    private int triSelection = 0;  // 0 = A-Z, 1 = Z-A

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_gestion_categorie, container, false);

        // Récupération de l'ID du restaurant
        restaurantId = getRestaurantIdFromSharedPreferences();
        if (restaurantId == null) {
            Log.e("PageGestionCategorie", "⚠️ restaurantId est NULL !");
            return view;
        }

        // Initialisation du service des catégories
        categorieService = new CategorieService(restaurantId);

        // Initialisation des vues
        recyclerView = view.findViewById(R.id.recyclerViewCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        editTextRecherche = view.findViewById(R.id.editTextRecherche);
        spinnerTri = view.findViewById(R.id.spinnerTri);

        FloatingActionButton fabAjouterCategorie = view.findViewById(R.id.fabAjouterCategorie);
        fabAjouterCategorie.setOnClickListener(v -> afficherDialogAjoutCategorie());

        adapter = new CategorieAdapter(getContext(), categories, categorieService, this);
        recyclerView.setAdapter(adapter);

        chargerCategories();

        // 🔍 Recherche en temps réel
        editTextRecherche.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrerCategories(s.toString());
                trierCategories();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    filtrerCategories("");
                }
            }
        });

        // 🔀 Gestion du tri
        ArrayAdapter<CharSequence> adapterTri = new ArrayAdapter<>(requireContext(), R.layout.custom_spinner_item, getResources().getStringArray(R.array.tri_options));
        adapterTri.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTri.setAdapter(adapterTri);

        spinnerTri.setSelection(0);
        spinnerTri.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                triSelection = position;
                trierCategories();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private void afficherDialogAjoutCategorie() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_layout_categorie, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText editTextCategory = dialogView.findViewById(R.id.editTextCategory);
        Spinner spinnerColor = dialogView.findViewById(R.id.spinnerColor);

        // Liste des couleurs disponibles
        String[] colors = {"Défaut", "Rouge", "Bleu", "Vert", "Orange", "Violet"};
        int[] colorValues = {android.R.color.transparent, android.R.color.holo_red_dark,
                android.R.color.holo_blue_dark, android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark, android.R.color.holo_purple};

        // Adapter personnalisé pour le spinner avec un carré de couleur
        ColorSpinnerAdapter colorAdapter = new ColorSpinnerAdapter(requireContext(), colors, colorValues);
        spinnerColor.setAdapter(colorAdapter);

        // Sélectionner "Défaut" par défaut
        spinnerColor.setSelection(0);

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            String categoryName = Normalize.normalize(editTextCategory.getText().toString().trim());
            String selectedColor = colors[spinnerColor.getSelectedItemPosition()];

            if (!categoryName.isEmpty()) {
                ajouterCategorie(categoryName, selectedColor);
                dialog.dismiss();
            } else {
                editTextCategory.setError("Le nom de la catégorie est requis !");
            }
        });

        dialog.show();
    }

    private void chargerCategories() {
        categorieService.getCategories(new CategorieService.CallbackList() {
            @Override
            public void onSuccess(List<Categorie> data) {
                if (data.isEmpty()) {
                    Log.d("PageGestionCategorie", "✅ Aucune catégorie enregistrée.");
                    return;
                }

                categoriesOriginales.clear();
                categoriesOriginales.addAll(data);

                categories = new ArrayList<>(categoriesOriginales);
                adapter.mettreAJourListe(categories); // ✅ Passe une `List<Categorie>` à l'adapter
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("PageGestionCategorie", "⚠️ Erreur lors du chargement des catégories", e);
            }
        });
    }

    private void filtrerCategories(String texte) {
        List<Categorie> categoriesFiltrees = new ArrayList<>();

        for (Categorie categorie : categoriesOriginales) {
            if (categorie.getNom().toLowerCase().contains(texte.toLowerCase())) {
                categoriesFiltrees.add(categorie);
            }
        }

        adapter.mettreAJourListe(categoriesFiltrees); // ✅ Passe bien une `List<Categorie>`
    }

    private void trierCategories() {
        if (triSelection == 0) { // Tri A-Z
            Collections.sort(categoriesOriginales, (c1, c2) -> c1.getNom().compareToIgnoreCase(c2.getNom()));
        } else if (triSelection == 1) { // Tri Z-A
            Collections.sort(categoriesOriginales, (c1, c2) -> c2.getNom().compareToIgnoreCase(c1.getNom()));
        }
        filtrerCategories(editTextRecherche.getText().toString());
    }


    private void ajouterCategorie(String categoryName, String color) {
        int ordreBouton = categoriesOriginales.size();

        categorieService.ajouterCategorie(categoryName, color, ordreBouton, new CategorieService.Callback() {
            @Override
            public void onSuccess() {
                Log.d("PageGestionCategorie", "✅ Catégorie ajoutée avec succès !");
                chargerCategories();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("PageGestionCategorie", "⚠️ Erreur lors de l'ajout de la catégorie", e);

                // ✅ Afficher un message si la catégorie existe déjà
                if (e.getMessage().equals("Cette catégorie existe déjà !")) {
                    CustomToast.show(getContext(), "Le nom de la catégorie est déjà utilisé !", R.drawable.ic_warning);
                } else {
                    Toast.makeText(getContext(), "⚠️ Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void modifierCategorie(String ancienNom, String nouveauNom, String nouvelleCouleur) {
        categorieService.modifierCategorie(ancienNom, nouveauNom, nouvelleCouleur, new CategorieService.Callback() {
            @Override
            public void onSuccess() {
                Log.d("PageGestionCategorie", "✅ Catégorie modifiée avec succès !");
                chargerCategories(); // ✅ Recharge la liste après modification

            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("PageGestionCategorie", "⚠️ Erreur lors de la modification", e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void supprimerCategorie(String categoryName) {
        categorieService.supprimerCategorie(categoryName, new CategorieService.Callback() {
            @Override
            public void onSuccess() {
                Log.d("PageGestionCategorie", "✅ Catégorie supprimée avec succès !");
                chargerCategories();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("PageGestionCategorie", "⚠️ Erreur lors de la suppression", e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getRestaurantIdFromSharedPreferences() {
        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        return prefs.getString("restaurantId", null);
    }
}
