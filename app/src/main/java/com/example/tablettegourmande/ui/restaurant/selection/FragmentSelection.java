package com.example.tablettegourmande.ui.restaurant.selection;

import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Categorie;
import com.example.tablettegourmande.services.CategorieService;

import java.util.ArrayList;
import java.util.List;

import Utils.ActionBarUtils;
import Utils.ButtonUtils;

public class FragmentSelection extends Fragment {

    private LinearLayout categoryContainer;
    private GridLayout productGrid;
    private Button /*btnTable, btnDirect, */btnComment, btnFollow, btnSend;
    private ImageButton btnTable, btnDirect;
    private CategorieService categorieService;


    public FragmentSelection() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.restaurant_selection, container, false);

        // Récupération de l'ID du restaurant (Ex: depuis les préférences partagées)
        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String restaurantId = prefs.getString("restaurantId", null);

        if (restaurantId == null) {
            Log.e("FragmentSelection", "⚠️ restaurantId est NULL !");
            return view;
        }

        // 🔹 Initialisation du service des catégories
        categorieService = new CategorieService(restaurantId);

        // Initialisation UI
        categoryContainer = view.findViewById(R.id.category_container);
        productGrid = view.findViewById(R.id.product_grid);
        btnComment = view.findViewById(R.id.btn_comment);
        btnFollow = view.findViewById(R.id.btn_follow);
        btnSend = view.findViewById(R.id.btn_send);
        //btnTable = view.findViewById(R.id.btn_table);
        //btnDirect = view.findViewById(R.id.btn_direct);

        // Charger dynamiquement les catégories et produits
        loadCategories();
        loadProducts();

        return view;
    }


    private void initToolBarButtons() {
        androidx.appcompat.widget.Toolbar toolbar = getActivity().findViewById(R.id.toolbar);

        if (toolbar != null) {
            // Ajouter dynamiquement les boutons TABLE et DIRECT
            btnTable = new ImageButton(getContext());
            btnTable.setBackgroundResource(R.drawable.ic_launcher_foreground);
            ActionBarUtils.addPressAnimation(btnTable);
            btnDirect = new ImageButton(getContext());
            btnDirect.setBackgroundResource(R.drawable.ic_launcher_foreground);
            ActionBarUtils.addPressAnimation(btnDirect);
            // Ajouter les boutons dans la Toolbar
            Toolbar.LayoutParams params = new Toolbar.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMarginEnd(16); // Ajouter une marge entre les boutons
            toolbar.setTitle("");
            toolbar.addView(btnTable, params);
            toolbar.addView(btnDirect, params);
        }
    }

    private void loadCategories() {
        categorieService.getCategories(new CategorieService.CallbackList() {
            @Override
            public void onSuccess(List<Categorie> categories) {
                categoryContainer.removeAllViews(); // Nettoyer avant de recharger

                for (Categorie categorie : categories) {
                    Button categoryButton = new Button(getContext());
                    categoryButton.setText(categorie.getNom());

                    // ✅ Appliquer la bonne couleur ou une couleur par défaut
                    int couleur = getColorFromCategorie(categorie.getCouleur());
                    categoryButton.setBackgroundTintList(ColorStateList.valueOf(couleur));
                    categoryButton.setTextColor(Color.WHITE);

                    // ✅ Ajustement design
                    categoryButton.setPadding(24, 12, 24, 12);
                    categoryButton.setTextSize(16);
                    categoryButton.setAllCaps(false);
                    categoryButton.setTypeface(null, Typeface.BOLD);
                    categoryButton.setElevation(6);

                    // ✅ Réduction de l’espacement
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(6, 3, 6, 3);
                    categoryButton.setLayoutParams(params);

                    // ✅ Appliquer un style arrondi
                    categoryButton.setBackgroundResource(R.drawable.button_category_rounded);
                    ButtonUtils.addPressAnimation(categoryButton);

                    // ✅ Activer le Drag & Drop (inchangé)
                    categoryButton.setOnLongClickListener(view -> {
                        ClipData data = ClipData.newPlainText("", "");
                        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                        view.startDragAndDrop(data, shadowBuilder, view, 0);
                        return true;
                    });

                    categoryButton.setOnDragListener((view, event) -> {
                        switch (event.getAction()) {
                            case DragEvent.ACTION_DRAG_STARTED:
                                return true;
                            case DragEvent.ACTION_DRAG_ENTERED:
                                view.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150).start();
                                return true;
                            case DragEvent.ACTION_DRAG_EXITED:
                                view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                                view.setBackgroundResource(R.drawable.button_category_rounded);
                                return true;
                            case DragEvent.ACTION_DROP:
                                View draggedView = (View) event.getLocalState();
                                int index = categoryContainer.indexOfChild(view);
                                categoryContainer.removeView(draggedView);
                                categoryContainer.addView(draggedView, index);
                                draggedView.setVisibility(View.VISIBLE);
                                // ✅ Restaurer immédiatement l'effet arrondi après le drop
                                draggedView.setBackgroundResource(R.drawable.button_category_rounded);
                                view.setBackgroundResource(R.drawable.button_category_rounded);
                                // ✅ Sauvegarder l'ordre mis à jour
                                saveCategoryOrder();
                                return true;
                            case DragEvent.ACTION_DRAG_ENDED:
                                view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                                view.setBackgroundResource(R.drawable.button_category_rounded);
                                return true;

                            default:
                                return false;
                        }
                    });

                    categoryContainer.addView(categoryButton);
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("FragmentSelection", "❌ Erreur lors du chargement des catégories : ", e);
            }
        });
    }

    /**
     * Sauvegarde le nouvel ordre des catégories après un déplacement
     */
    private void saveCategoryOrder() {
        List<String> newOrder = new ArrayList<>();

        for (int i = 0; i < categoryContainer.getChildCount(); i++) {
            Button button = (Button) categoryContainer.getChildAt(i);
            newOrder.add(button.getText().toString());
        }

        categorieService.updateCategoryOrder(newOrder, new CategorieService.Callback() {
            @Override
            public void onSuccess() {
                Log.d("FragmentSelection", "Ordre des catégories mis à jour !");
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("FragmentSelection", "Erreur lors de la mise à jour de l'ordre des catégories", e);
            }
        });
    }

    /**
     * Convertit une chaîne de couleur en int
     */
    private int getColorFromCategorie(String couleur) {
        if (couleur == null || couleur.isEmpty() || couleur.equalsIgnoreCase("Défaut")) {
            return ContextCompat.getColor(getContext(), R.color.category_default_color); // Utilisation du thème principal
        }

        switch (couleur.toLowerCase()) {
            case "rouge":
                return ContextCompat.getColor(getContext(), android.R.color.holo_red_dark);
            case "bleu":
                return ContextCompat.getColor(getContext(), android.R.color.holo_blue_dark);
            case "vert":
                return ContextCompat.getColor(getContext(), android.R.color.holo_green_dark);
            case "orange":
                return ContextCompat.getColor(getContext(), android.R.color.holo_orange_dark);
            case "violet":
                return ContextCompat.getColor(getContext(), android.R.color.holo_purple);
            default:
                return ContextCompat.getColor(getContext(), R.color.category_default_color); // Couleur du thème par défaut
        }
    }



    private void loadProducts() {
        String[] products = {/*"Burger", "Pizza", "Salade", "Coca", "Café"*/};
        for (String product : products) {
            Button productButton = new Button(getContext());
            productButton.setText(product);
            productButton.setLayoutParams(new GridLayout.LayoutParams());
            productButton.setOnClickListener(v -> Log.d("Product", "Produit ajouté au ticket : " + product));
            productGrid.addView(productButton);
        }
    }

    private void toggleColorButton(Button btn, Color color){

    }

    @Override
    public void onDestroyView() {
        androidx.appcompat.widget.Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.removeView(btnDirect);
        toolbar.removeView(btnTable);
        super.onDestroyView();
    }
}
