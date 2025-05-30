package com.example.tablettegourmande.ui.gestion;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.adapters.CategorieAdapter;
import com.example.tablettegourmande.adapters.ProduitAdapter;
import com.example.tablettegourmande.models.Produit;
import com.example.tablettegourmande.services.CategorieService;
import com.example.tablettegourmande.services.ProduitService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import Utils.CustomToast;
import Utils.Normalize;

public class PageGestionProduit extends Fragment {
    private RecyclerView recyclerView;
    private ProduitAdapter adapter;
    private List<Produit> produits = new ArrayList<>();
    private ProduitService produitService;
    private String restaurantId;
    private EditText editTextRecherche;
    private Spinner spinnerTri;
    private List<Produit> produitsOriginaux = new ArrayList<>();
    private int triSelection = 0;
    private List<String> categoriesEnMemoire = new ArrayList<>();

    private CategorieService categorieService;
    private List<String> optionsDisponibles = new ArrayList<>();

    private List<String> optionsSelectionnees = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_gestion_produit, container, false);

        // Récupération de l'ID du restaurant
        restaurantId = getRestaurantIdFromSharedPreferences();
        if (restaurantId == null) {
            Toast.makeText(getContext(), "Erreur : ID restaurant introuvable", Toast.LENGTH_SHORT).show();
            return view;
        }

        produitService = new ProduitService(restaurantId);
        categorieService = new CategorieService(restaurantId);

        recyclerView = view.findViewById(R.id.recyclerViewProduits);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        editTextRecherche = view.findViewById(R.id.searchProduit);
        spinnerTri = view.findViewById(R.id.spinnerTriProduit);
        FloatingActionButton fabAjouterProduit = view.findViewById(R.id.fabAjouterProduit);
        fabAjouterProduit.setOnClickListener(v -> afficherDialogAjoutModification(null));

        adapter = new ProduitAdapter(getContext(), produits, produitService, new ProduitAdapter.OnProduitActionListener() {
            @Override
            public void onModifierProduit(Produit produit) {
                afficherDialogAjoutModification(produit);
            }
        });

        recyclerView.setAdapter(adapter);

        chargerCategories();
        chargerProduits();
        setupTriProduit();

        // 🔍 Recherche en temps réel
        editTextRecherche.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrerProduits(s.toString());
                trierProduits(spinnerTri.getSelectedItemPosition());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    private void afficherDialogAjoutModification(@Nullable Produit produitExistant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_layout_produit, null);
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        EditText editTextNom = dialogView.findViewById(R.id.editTextNomProduit);
        EditText editTextPrix = dialogView.findViewById(R.id.editTextPrixProduit);
        Spinner spinnerCategorie = dialogView.findViewById(R.id.spinnerCategorieProduit);
        Spinner spinnerCouleur = dialogView.findViewById(R.id.spinnerCouleurProduit);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);
        Button btnAjouterOption = dialogView.findViewById(R.id.btnAjouterOption);
        LinearLayout layoutOptions = dialogView.findViewById(R.id.layoutOptions);


        // 🔥 Modifier le titre selon l'action
        if (produitExistant != null) {
            dialogTitle.setText("Modifier un produit");
            editTextNom.setText(produitExistant.getNom());
            editTextPrix.setText(String.format(Locale.US, "%.2f", produitExistant.getPrix()));

        } else {
            dialogTitle.setText("Ajouter un produit");
        }

        // 🔥 Charger les catégories dans le Spinner avec le bon affichage
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.custom_spinner_item, categoriesEnMemoire);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategorie.setAdapter(adapter);

        // Sélectionner la bonne catégorie si modification
        if (produitExistant != null) {
            int positionCategorie = categoriesEnMemoire.indexOf(produitExistant.getCategorie());
            if (positionCategorie != -1) {
                spinnerCategorie.setSelection(positionCategorie);
            }
        }

        // 🔥 Charger les couleurs avec `ColorSpinnerAdapter`
        String[] colors = {"Défaut", "Rouge", "Bleu", "Vert", "Orange", "Violet"};
        int[] colorValues = {android.R.color.transparent, android.R.color.holo_red_dark,
                android.R.color.holo_blue_dark, android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark, android.R.color.holo_purple};

        ColorSpinnerAdapter colorAdapter = new ColorSpinnerAdapter(getContext(), colors, colorValues);
        spinnerCouleur.setAdapter(colorAdapter);

        // Sélectionner la bonne couleur si modification
        if (produitExistant != null) {
            int positionCouleur = 0;
            for (int i = 0; i < colors.length; i++) {
                if (colors[i].equalsIgnoreCase(produitExistant.getCouleur())) {
                    positionCouleur = i;
                    break;
                }
            }
            spinnerCouleur.setSelection(positionCouleur);
        }

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> chargerOptionsDansDialog(dialog, produitExistant)); // ✅ Passe bien `produitExistant`

        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String nom = Normalize.normalize(editTextNom.getText().toString().trim());
            String prixStr = editTextPrix.getText().toString().trim();
            double prix = prixStr.isEmpty() ? 0 : Double.parseDouble(prixStr);
            String categorieSelectionnee = (String) spinnerCategorie.getSelectedItem();
            String couleurSelectionnee = (String) spinnerCouleur.getSelectedItem();

            List<String> optionsFinales = new ArrayList<>();
            for (int i = 0; i < layoutOptions.getChildCount(); i++) {
                LinearLayout optionLayout = (LinearLayout) layoutOptions.getChildAt(i);
                Spinner spinnerOption = (Spinner) optionLayout.getChildAt(0);
                String selectedOption = spinnerOption.getSelectedItem().toString();
                if (!optionsFinales.contains(selectedOption)) {
                    optionsFinales.add(selectedOption);
                }
            }


            if (nom.isEmpty() || prix == 0 || categorieSelectionnee == null) {
                CustomToast.show(getContext(), "Veuillez remplir tous les champs", R.drawable.ic_warning);
                return;
            }

            if(produitExistant==null){ // ajout
                produitService.verifierProduitExiste(nom, new ProduitService.CallbackBoolean() {
                    @Override
                    public void onResult(boolean existe) {
                        if(!existe){
                            produitService.ajouterProduit(nom, couleurSelectionnee, prix, categorieSelectionnee, optionsFinales, new ProduitService.Callback() {
                                @Override
                                public void onSuccess() {
                                    CustomToast.show(getContext(), "Produit ajouté avec succès !", R.drawable.ic_success);
                                    chargerProduits();
                                    dialog.dismiss();
                                }

                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else{
                            CustomToast.show(getContext(), "Un produit avec ce nom existe déjà !!", R.drawable.ic_success);
                        }
                    }
                });
            }else{ // modif
                produitService.verifierProduitExiste(nom, new ProduitService.CallbackBoolean() {
                    @Override
                    public void onResult(boolean existe) {
                        if(existe && produitExistant.getNom().equals(nom)){
                            produitService.modifierProduit(produitExistant.getNom(), nom, couleurSelectionnee, prix, categorieSelectionnee, optionsFinales, new ProduitService.Callback() {
                                @Override
                                public void onSuccess() {
                                    CustomToast.show(getContext(), "Produit modifié avec succès !", R.drawable.ic_success);
                                    chargerProduits();
                                    dialog.dismiss();
                                }

                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else{
                            if(!existe){
                                produitService.modifierProduit(produitExistant.getNom(), nom, couleurSelectionnee, prix, categorieSelectionnee, optionsFinales, new ProduitService.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        CustomToast.show(getContext(), "Produit modifié avec succès !", R.drawable.ic_success);
                                        chargerProduits();
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else{
                                CustomToast.show(getContext(), "Un produit avec ce nom existe déjà !!", R.drawable.ic_success);
                            }
                        }
                    }
                });
            }

        });

        // Charger les options UNE SEULE FOIS lors de l'ouverture du dialog
        produitService.getOptions(
                restaurantId,
                options -> {
                    optionsDisponibles.clear();
                    optionsDisponibles.addAll(options);
                    Log.d("DEBUG", "Options préchargées : " + optionsDisponibles);
                },
                erreur -> Log.e("Firebase", "Erreur lors du chargement des options", erreur)
        );

        btnAjouterOption.setOnClickListener(v -> {
            if (optionsDisponibles.isEmpty()) {
                CustomToast.show(getContext(), "Aucune option disponible", R.drawable.ic_warning);
                return;
            }
            ajouterSpinnerOption(layoutOptions, null); // 🔥 Ajoute une option vide
        });

    }

    private void chargerProduits() {
        produitService.getProduits(new ProduitService.CallbackList() {
            @Override
            public void onSuccess(List<Produit> data) {

                produitsOriginaux.clear();
                produitsOriginaux.addAll(data);
                produits = new ArrayList<>(produitsOriginaux);
                adapter.mettreAJourListe(produits);
                trierProduits(0);
                filtrerProduits(editTextRecherche.getText().toString());

            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Erreur de chargement : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTriProduit() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.custom_spinner_item,
                new String[]{"A-Z", "Z-A", "Prix croissant", "Prix décroissant", "Par catégorie"}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(getResources().getColor(R.color.black));
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                textView.setTextColor(getResources().getColor(R.color.black));
                textView.setBackgroundColor(getResources().getColor(android.R.color.white));
                return textView;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTri.setAdapter(adapter);

        // 🔥 Déclencher immédiatement le tri après sélection
        spinnerTri.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                trierProduits(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    private void trierProduits(int triSelection) {
        if (produits == null || produits.isEmpty()) return;

        // 🔥 Vérifier si une recherche est en cours
        String texteRecherche = editTextRecherche.getText().toString().trim();
        List<Produit> produitsTries = new ArrayList<>();

        if (!texteRecherche.isEmpty()) {
            // 🔥 Si recherche en cours, appliquer le tri sur les résultats filtrés
            for (Produit produit : produitsOriginaux) {
                if (produit.getNom().toLowerCase().contains(texteRecherche.toLowerCase()) ||
                        produit.getCategorie().toLowerCase().contains(texteRecherche.toLowerCase())) {
                    produitsTries.add(produit);
                }
            }
        } else {
            // 🔥 Sinon, travailler sur la liste complète
            produitsTries.addAll(produitsOriginaux);
        }

        // Appliquer le tri
        switch (triSelection) {
            case 0: // A-Z
                Collections.sort(produitsTries, (p1, p2) -> p1.getNom().compareToIgnoreCase(p2.getNom()));
                break;
            case 1: // Z-A
                Collections.sort(produitsTries, (p1, p2) -> p2.getNom().compareToIgnoreCase(p1.getNom()));
                break;
            case 2: // Prix croissant
                Collections.sort(produitsTries, Comparator.comparingDouble(Produit::getPrix));
                break;
            case 3: // Prix décroissant
                Collections.sort(produitsTries, (p1, p2) -> Double.compare(p2.getPrix(), p1.getPrix()));
                break;
            case 4: // Par catégorie
                Collections.sort(produitsTries, (p1, p2) -> p1.getCategorie().compareToIgnoreCase(p2.getCategorie()));
                break;
        }

        adapter.mettreAJourListe(produitsTries); // 🔥 Mettre à jour uniquement avec la liste triée
    }



    private void chargerCategories() {
        produitService.ecouterCategories(new ProduitService.CallbackListCategories() {
            @Override
            public void onSuccess(List<String> categories) {
                categoriesEnMemoire.clear();
                categoriesEnMemoire.addAll(categories);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Erreur de mise à jour des catégories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filtrerProduits(String texte) {
        List<Produit> produitsFiltres = new ArrayList<>();
        for (Produit produit : produitsOriginaux) {
            if (produit.getNom().toLowerCase().contains(texte.toLowerCase()) ||
                    produit.getCategorie().toLowerCase().contains(texte.toLowerCase())) {
                produitsFiltres.add(produit);
            }
        }
        adapter.mettreAJourListe(produitsFiltres);
    }

    private String getRestaurantIdFromSharedPreferences() {
        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String restaurantId = prefs.getString("restaurantId", null);

        if (restaurantId == null || restaurantId.isEmpty()) {
            Toast.makeText(getContext(), "ID restaurant non défini, veuillez le configurer.", Toast.LENGTH_LONG).show();
        }
        return restaurantId;
    }


    // si modification catégorie : ce listener permet la modification du tableau
    private ListenerRegistration produitListener;

    @Override
    public void onStart() {
        super.onStart();
        ecouterChangementsProduits(); // Démarre l'écoute en temps réel

    }

    @Override
    public void onStop() {
        super.onStop();
        if (produitListener != null) {
            produitListener.remove(); // Arrête l'écoute quand on quitte le fragment
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (editTextRecherche != null) {
            editTextRecherche.setText(""); // 🔥 Efface la recherche quand l'utilisateur quitte le fragment
        }

    }
    @Override
    public void onResume(){
        super.onResume();
        chargerProduits();
        setupTriProduit();
        //filtrerProduits("");
        //trierProduits(0);
    }

    private void ecouterChangementsProduits() {
        produitListener = produitService.getProduitsStream(new ProduitService.CallbackList() {
            @Override
            public void onSuccess(List<Produit> data) {
                produits.clear();
                produits.addAll(data);
                adapter.mettreAJourListe(produits);
                Log.d("PageGestionProduit", "✅ Mise à jour automatique des produits depuis Firebase !");
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("PageGestionProduit", "⚠️ Erreur lors de l'écoute des produits : " + e.getMessage());
            }
        });
    }
    private void chargerOptionsDansDialog(AlertDialog dialog, Produit produitExistant) {
        LinearLayout layoutOptions = dialog.findViewById(R.id.layoutOptions);
        if (layoutOptions == null) {
            Log.e("DEBUG", "Impossible de trouver layoutOptions dans le dialog !");
            return;
        }

        produitService.getOptions(
                restaurantId,
                options -> {
                    optionsDisponibles.clear();
                    optionsDisponibles.addAll(options);
                    Log.d("DEBUG", "Options disponibles après récupération: " + optionsDisponibles);

                    // 🔥 Maintenant que les options sont chargées, on peut ajouter les Spinners
                    if (produitExistant != null && produitExistant.getOptions() != null) {
                        optionsSelectionnees.clear();
                        optionsSelectionnees.addAll(produitExistant.getOptions()); // 🔥 Charger les options existantes
                        Log.d("DEBUG", "Options existantes pour ce produit: " + optionsSelectionnees);

                        // 🔥 Ajouter chaque option existante après que les optionsDisponibles soient chargées
                        for (String option : optionsSelectionnees) {
                            ajouterSpinnerOption(layoutOptions, option);
                        }
                    }
                },
                erreur -> Log.e("Firebase", "Erreur lors du chargement des options après affichage du dialog", erreur)
        );
    }

    private void ajouterSpinnerOption(LinearLayout layoutOptions, String optionSelectionnee) {
        Context context = getContext();

        // 🔥 Création dynamique du Spinner
        Spinner spinnerOption = new Spinner(context);
        ArrayAdapter<String> adapterOptions = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, optionsDisponibles);
        adapterOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOption.setAdapter(adapterOptions);
        spinnerOption.setBackgroundResource(R.drawable.custom_spinner_background);

        // ✅ Augmenter légèrement la hauteur pour éviter qu'il soit trop serré
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                0,  // ✅ Largeur flexible
                55, // ✅ Hauteur légèrement augmentée
                0.2f // ✅ Le `Spinner` prend 40% de la largeur
        );
        spinnerOption.setLayoutParams(spinnerParams);

        // ✅ Masquer temporairement le `Spinner` pour éviter la désynchronisation visuelle
        spinnerOption.setVisibility(View.INVISIBLE);

        // 🔥 Sélectionner l'option déjà enregistrée après affichage
        if (optionSelectionnee != null) {
            spinnerOption.post(() -> {
                int position = optionsDisponibles.indexOf(optionSelectionnee);
                if (position >= 0) {
                    spinnerOption.setSelection(position);
                }
                // ✅ Afficher le `Spinner` seulement après avoir mis la bonne sélection
                spinnerOption.setVisibility(View.VISIBLE);
            });
        } else {
            // ✅ Afficher immédiatement si aucune option n'est sélectionnée
            spinnerOption.setVisibility(View.VISIBLE);
        }

        // 🔥 Création du bouton "Supprimer"
        ImageButton btnSupprimer = new ImageButton(context);
        btnSupprimer.setImageResource(android.R.drawable.ic_delete);
        btnSupprimer.setBackgroundColor(Color.TRANSPARENT);
        btnSupprimer.setPadding(10, 10, 10, 10);

        // ✅ Espacement réduit entre le `Spinner` et le bouton "Supprimer"
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(10, 0, 0, 0);
        btnSupprimer.setLayoutParams(buttonParams);

        // ✅ Réduction de l'espacement vertical entre chaque option
        LinearLayout optionLayout = new LinearLayout(context);
        optionLayout.setOrientation(LinearLayout.HORIZONTAL);
        optionLayout.setGravity(Gravity.CENTER_VERTICAL);
        optionLayout.setPadding(0, 2, 5, 2); // ✅ Moins d'espace en haut et en bas
        optionLayout.addView(spinnerOption);
        optionLayout.addView(btnSupprimer);
        layoutOptions.addView(optionLayout);

        // 🔥 Gestion de la suppression
        btnSupprimer.setOnClickListener(v -> {
            layoutOptions.removeView(optionLayout);
            optionsSelectionnees.remove(spinnerOption.getSelectedItem().toString());
        });
    }

}
