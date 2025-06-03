package com.example.tablettegourmande.ui.restaurant.selection;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Categorie;
import com.example.tablettegourmande.models.Produit;
import com.example.tablettegourmande.services.CategorieService;
import com.example.tablettegourmande.services.ProduitService;
import com.example.tablettegourmande.services.TableService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

import Utils.ActionBarUtils;
import Utils.ButtonUtils;
import Utils.CustomToast;

public class FragmentSelection extends Fragment {

    private LinearLayout categoryContainer, cuisineLayout;
    private GridLayout productGrid;
    private Button btnComment, btnFollow, btnSend;
    private Button btnTable, btnDirect;
    private CategorieService categorieService;
    private ProduitService produitService;
    private TableService tableService;
    private String categorieSelectionnee;
    private List<Produit> produitsOriginaux = new ArrayList<>();

    public FragmentSelection() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.restaurant_selection, container, false);

        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String restaurantId = prefs.getString("restaurantId", null);

        if (restaurantId == null) {
            Log.e("FragmentSelection", "⚠️ restaurantId est NULL !");
            return view;
        }

        categorieService = new CategorieService(restaurantId);
        produitService = new ProduitService(restaurantId);
        tableService = new TableService(restaurantId);

        categoryContainer = view.findViewById(R.id.category_container);
        productGrid = view.findViewById(R.id.gridLayoutProduits);
        btnComment = view.findViewById(R.id.btn_comment);
        btnFollow = view.findViewById(R.id.btn_follow);
        btnSend = view.findViewById(R.id.btn_send);
        cuisineLayout = view.findViewById(R.id.layout_cuisine_btn);

        btnTable = view.findViewById(R.id.btn_table);
        btnDirect = view.findViewById(R.id.btn_direct);

        btnDirect.setOnClickListener(v -> {
            TableService tableService = new TableService(restaurantId);
            tableService.setCurrentTable("direct", new TableService.TableUpdateCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Mode direct activé", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        btnTable.setOnClickListener(v -> afficherDialogueSelectionTable(restaurantId));

        loadCategories();
        loadTable();

        return view;
    }

    private void afficherDialogueSelectionTable(String restaurantId) {
        TableService tableService = new TableService(restaurantId);
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_select_table, null);

        TextView tvInput = dialogView.findViewById(R.id.tvInputTableNumber);
        GridLayout keypad = dialogView.findViewById(R.id.keypad);
        GridLayout tableGrid = dialogView.findViewById(R.id.tableGrid);
        TextView tvOpenedLabel = dialogView.findViewById(R.id.tvOpenedLabel);
        tvOpenedLabel.setVisibility(View.GONE);

        StringBuilder currentInput = new StringBuilder();

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setTitle("Ouvrir table")
                .create();

        // === Clavier numérique ===
        String[] keys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "←", "0", "✓"};
        for (String key : keys) {
            Button btn = new Button(getContext());
            btn.setText(key);
            btn.setAllCaps(false);
            int couleur = ButtonUtils.getColor("défaut", getContext());
            btn.setBackgroundTintList(ColorStateList.valueOf(couleur));
            btn.setTextColor(Color.WHITE);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(2, 2, 2, 2);
            params.setGravity(Gravity.FILL_HORIZONTAL);
            btn.setLayoutParams(params);

            btn.setPadding(16, 16, 16, 16);
            // bouton stylé via le thème → rien à ajouter ici

            btn.setOnClickListener(v -> {
                switch (key) {
                    case "←":
                        if (currentInput.length() > 0)
                            currentInput.deleteCharAt(currentInput.length() - 1);
                        break;
                    case "✓":
                        if (currentInput.length() > 0) {
                            String table = currentInput.toString();
                            tableService.setCurrentTable(table, new TableService.TableUpdateCallback() {
                                @Override
                                public void onSuccess() {
                                    tableService.addOpenedTable(table);
                                    Toast.makeText(getContext(), "Table " + table + " activée", Toast.LENGTH_SHORT).show();
                                    alertDialog.dismiss();
                                }

                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(getContext(), "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        break;
                    default:
                        currentInput.append(key);
                }
                tvInput.setText(currentInput.toString());
            });

            keypad.addView(btn);
        }

        // === Tables ouvertes ===
        tableService.getOpenedTables(new TableService.OpenedTablesCallback() {
            @Override
            public void onSuccess(List<String> openedTables) {
                if (openedTables != null && !openedTables.isEmpty()) {
                    tvOpenedLabel.setVisibility(View.VISIBLE);

                    // ✅ Tri numérique croissant
                    openedTables.sort((a, b) -> {
                        try {
                            return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
                        } catch (NumberFormatException e) {
                            return a.compareTo(b);
                        }
                    });
                }

                for (String table : openedTables) {
                    Button tableBtn = new Button(getContext());
                    tableBtn.setText(table);
                    tableBtn.setAllCaps(false);

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 0;
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                    params.setMargins(8, 8, 8, 8);
                    params.setGravity(Gravity.FILL_HORIZONTAL);
                    tableBtn.setLayoutParams(params);
                    int couleur = ButtonUtils.getColor("défaut", getContext());
                    tableBtn.setBackgroundTintList(ColorStateList.valueOf(couleur));
                    tableBtn.setTextColor(Color.WHITE);
                    tableBtn.setPadding(16, 16, 16, 16);
                    // bouton stylé via le thème → rien à ajouter ici

                    tableBtn.setOnClickListener(v -> {
                        tableService.setCurrentTable(table, new TableService.TableUpdateCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getContext(), "Table " + table + " activée", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(getContext(), "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });

                    tableGrid.addView(tableBtn);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Erreur tables ouvertes", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.show();
    }



    private void loadCategories() {
        categorieService.getCategories(new CategorieService.CallbackList() {
            @Override
            public void onSuccess(List<Categorie> categories) {
                categoryContainer.removeAllViews();

                for (int i = 0; i < categories.size(); i++) {
                    Categorie categorie = categories.get(i);
                    Button categoryButton = new Button(getContext());
                    categoryButton.setText(categorie.getNom());

                    int couleur = ButtonUtils.getColor(categorie.getCouleur(), getContext());
                    categoryButton.setBackgroundTintList(ColorStateList.valueOf(couleur));
                    categoryButton.setTextColor(Color.WHITE);

                    categoryButton.setPadding(24, 12, 24, 12);
                    categoryButton.setTextSize(16);
                    categoryButton.setAllCaps(false);
                    categoryButton.setTypeface(null, Typeface.BOLD);
                    categoryButton.setElevation(6);

                    float scale = getContext().getResources().getDisplayMetrics().density;

                    // Exemple 120dp largeur, 48dp hauteur (à ajuster)
                    int widthPx = (int) (160 * scale + 0.5f);
                    int heightPx = (int) (80 * scale + 0.5f);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthPx, heightPx);
                    params.setMargins(6, 3, 6, 3);
                    categoryButton.setLayoutParams(params);

                    /*params.setMargins(6, 3, 6, 3);
                    categoryButton.setLayoutParams(params);*/

                    categoryButton.setBackgroundResource(R.drawable.button_category_rounded);
                    ButtonUtils.addPressAnimation(categoryButton);

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
                            case DragEvent.ACTION_DRAG_ENDED:
                                view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                                view.setBackgroundResource(R.drawable.button_category_rounded);
                                return true;
                            case DragEvent.ACTION_DROP:
                                View draggedView = (View) event.getLocalState();
                                int index = categoryContainer.indexOfChild(view);
                                categoryContainer.removeView(draggedView);
                                categoryContainer.addView(draggedView, index);
                                draggedView.setVisibility(View.VISIBLE);
                                draggedView.setBackgroundResource(R.drawable.button_category_rounded);
                                view.setBackgroundResource(R.drawable.button_category_rounded);
                                saveCategoryOrder();
                                return true;
                            default:
                                return false;
                        }
                    });

                    // ✅ Gestion du clic sur la catégorie
                    categoryButton.setOnClickListener(v -> {
                        categorieSelectionnee = categorie.getNom();

                        produitService.getProduits(new ProduitService.CallbackList() {
                            @Override
                            public void onSuccess(List<Produit> produits) {
                                produitsOriginaux = produits;

                                afficherProduitsParCategorie(
                                        requireContext(),
                                        categorieSelectionnee,
                                        produitsOriginaux,
                                        productGrid,
                                        produitView -> {
                                            Produit produit = (Produit) produitView.getTag();
                                            // TODO : ajouter au ticket
                                            CustomToast.show(getContext(),produit.getNom()+" - Prix : "+produit.getPrix(), R.drawable.ic_add);
                                        },
                                        (listeReordonnee, categorie) -> {
                                            updateProductOrder(listeReordonnee);
                                        }
                                );
                            }

                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("DEBUG_PRODUITS", "Erreur lors du chargement des produits : ", e);
                            }
                        });
                    });


                    categoryContainer.addView(categoryButton);

                    // ✅ Chargement automatique de la première catégorie
                    if (i == 0) {
                        categorieSelectionnee = categorie.getNom();
                        produitService.getProduits(new ProduitService.CallbackList() {
                            @Override
                            public void onSuccess(List<Produit> produits) {
                                produitsOriginaux = produits;
                                afficherProduitsParCategorie(
                                        requireContext(),
                                        categorieSelectionnee,
                                        produitsOriginaux,
                                        productGrid,
                                        produitView -> {
                                            Produit produit = (Produit) produitView.getTag();
                                            // TODO : ajouter au ticket
                                            CustomToast.show(getContext(),produit.getNom()+" - Prix : "+produit.getPrix(), R.drawable.ic_add);

                                        },
                                        (listeReordonnee, categorie) -> {
                                            updateProductOrder(listeReordonnee);
                                        }
                                );
                            }

                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("FragmentSelection", "Erreur chargement produits", e);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("FragmentSelection", "❌ Erreur chargement catégories", e);
            }
        });
    }

    private void loadTable(){
        tableService.checkOrInitCurrentTable(new TableService.CurrentTableCallback() {
            @Override
            public void onResult(String tableValue) {

            }

            @Override
            public void onError(Exception e) {

            }
        });
    }
    public void afficherProduitsParCategorie(Context context,
                                             String nomCategorie,
                                             List<Produit> allProduits,
                                             GridLayout gridLayout,
                                             View.OnClickListener listener,
                                             BiConsumer<List<Produit>, String> callbackOrdre) {

        gridLayout.removeAllViews();

        List<Produit> produitsCategorie = new ArrayList<>();
        for (Produit p : allProduits) {
            if (p.getCategorie() != null && p.getCategorie().equalsIgnoreCase(nomCategorie)) {
                produitsCategorie.add(p);
            }
        }

        produitsCategorie.sort(Comparator.comparingLong(p -> p.getBtn_order() != null ? p.getBtn_order() : 0L));

        final int columnCount = 5;
        float scale = context.getResources().getDisplayMetrics().density;
        final int marginDp = 2;
        final int marginPx = (int) (marginDp * scale + 0.5f);

        // Calcul taille et création boutons différé après layout pour récupérer largeur réelle
        gridLayout.post(() -> {
            int gridWidth = gridLayout.getWidth()
                    - gridLayout.getPaddingStart()
                    - gridLayout.getPaddingEnd();

            int marginCount = columnCount - 1;
            int totalMarginPx = marginPx * marginCount;

            int availableWidth = gridWidth - totalMarginPx;

            // Taille brute d’un bouton
            int buttonSize = availableWidth / columnCount;

            // Réduction de 2~5% pour être sûr que ça rentre bien
            float reductionFactor = 0.95f;  // 95% de la taille calculée

            int adjustedButtonSize = (int)(buttonSize * reductionFactor);

            Log.d("GridDebug", "gridWidth=" + gridWidth + ", totalMarginPx=" + totalMarginPx +
                    ", availableWidth=" + availableWidth + ", buttonSize=" + buttonSize +
                    ", adjustedButtonSize=" + adjustedButtonSize);

            gridLayout.removeAllViews();

            for (int index = 0; index < produitsCategorie.size(); index++) {
                Produit produit = produitsCategorie.get(index);
                FrameLayout btn = creerBoutonProduitCustom(context, produit, adjustedButtonSize, marginPx,
                        listener, gridLayout, callbackOrdre, nomCategorie, columnCount);

                int row = index / columnCount;
                int col = index % columnCount;

                GridLayout.LayoutParams params = (GridLayout.LayoutParams) btn.getLayoutParams();
                params.columnSpec = GridLayout.spec(col);
                params.rowSpec = GridLayout.spec(row);
                btn.setLayoutParams(params);

                gridLayout.addView(btn);
            }
        });
    }

    private FrameLayout creerBoutonProduitCustom(Context context,
                                                 Produit produit,
                                                 int buttonSize,
                                                 int marginPx,
                                                 View.OnClickListener clickListener,
                                                 GridLayout gridLayout,
                                                 BiConsumer<List<Produit>, String> callbackOrdre,
                                                 String nomCategorie,
                                                 int columnCount) {

        FrameLayout container = new FrameLayout(context);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = buttonSize;
        params.height = buttonSize;
        params.setMargins(marginPx, marginPx, marginPx, marginPx);
        container.setLayoutParams(params);
        container.setBackgroundResource(R.drawable.button_category_rounded);
        container.setClickable(true);
        container.setFocusable(true);

        LinearLayout linear = new LinearLayout(context);
        linear.setOrientation(LinearLayout.VERTICAL);
        linear.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams linearParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        linear.setLayoutParams(linearParams);
        int padding = (int) (12 * context.getResources().getDisplayMetrics().density);
        linear.setPadding(padding, padding, padding, padding);

        // Nom produit
        TextView tvNom = new TextView(context);
        tvNom.setText(produit.getNom());
        tvNom.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tvNom.setTypeface(null, Typeface.BOLD);
        tvNom.setMaxLines(2);
        tvNom.setEllipsize(TextUtils.TruncateAt.END);
        tvNom.setGravity(Gravity.CENTER);
        tvNom.setTextColor(Color.WHITE);

        // Prix produit
        TextView tvPrix = new TextView(context);
        tvPrix.setText(String.format("%.2f €", produit.getPrix()));
        tvPrix.setTypeface(null, Typeface.BOLD);
        tvPrix.setGravity(Gravity.CENTER);
        tvPrix.setTextColor(Color.WHITE);

        linear.addView(tvNom);
        linear.addView(tvPrix);

        container.addView(linear);

        // Couleur dynamique
        int couleur = ButtonUtils.getColor(produit.getCouleur(), context);
        container.setBackgroundTintList(ColorStateList.valueOf(couleur)); // Nécessite API 21+

        container.setTag(produit);
        container.setOnClickListener(clickListener);

        // DRAG & DROP
        container.setOnLongClickListener(view -> {
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDragAndDrop(data, shadowBuilder, view, 0);
            return true;
        });

        container.setOnDragListener((view, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    view.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150).start();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                case DragEvent.ACTION_DRAG_ENDED:
                    view.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
                    view.setBackgroundResource(R.drawable.button_category_rounded);
                    return true;
                case DragEvent.ACTION_DROP:
                    int draggedIndex = gridLayout.indexOfChild(view);
                    View draggedView = (View) event.getLocalState();

                    // Récupérer la liste des vues existantes sauf draggedView
                    List<View> children = new ArrayList<>();
                    for (int i = 0; i < gridLayout.getChildCount(); i++) {
                        View child = gridLayout.getChildAt(i);
                        if (child != draggedView) {
                            children.add(child);
                        }
                    }

                    // Insérer draggedView à la bonne position
                    children.add(draggedIndex, draggedView);

                    // Supprimer toutes les vues du gridLayout
                    float scale = context.getResources().getDisplayMetrics().density;
                    int marginDp = 8;
                    int marginPx1 = (int) (marginDp * scale + 0.5f);

                    gridLayout.removeAllViews();

                    for (int i = 0; i < children.size(); i++) {
                        View child = children.get(i);

                        GridLayout.LayoutParams params1 = new GridLayout.LayoutParams();
                        params1.width = buttonSize;
                        params1.height = buttonSize;
                        params1.columnSpec = GridLayout.spec(i % columnCount);
                        params1.rowSpec = GridLayout.spec(i / columnCount);
                        params1.setMargins(marginPx1, marginPx1, marginPx1, marginPx1);

                        child.setLayoutParams(params1);
                        child.requestLayout();

                        gridLayout.addView(child);
                    }

                    // Mettre à jour la liste locale produits et Firestore ici
                    // Par exemple, reconstruire 'produits' à partir des tags des vues réordonnées
                    List<Produit> reordered = new ArrayList<>();
                    for (View child : children) {
                        Produit p = (Produit) child.getTag();
                        reordered.add(p);
                    }

                    for (int i = 0; i < reordered.size(); i++) {
                        Produit p = reordered.get(i);
                        long newOrder = i;
                        p.setBtn_order(newOrder);
                        produitService.mettreAJourOrdreProduit(p.getId(), newOrder, new ProduitService.Callback() {
                            @Override
                            public void onSuccess() {
                                Log.d("ORDRE", "btn_order mis à jour : " + p.getNom() + " → " + newOrder);
                            }
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("ORDRE", "Erreur MAJ ordre produit", e);
                            }
                        });
                    }

                    return true;
            }
            return false;
        });

        return container;
    }


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
                Log.e("FragmentSelection", "Erreur mise à jour ordre", e);
            }
        });
    }
    private void updateProductOrder(List<Produit> produits) {
        for (Produit p : produits) {
            produitService.mettreAJourOrdreProduit(p.getId(), p.getBtn_order(), new ProduitService.Callback() {
                @Override
                public void onSuccess() {
                    Log.d("ORDRE", "btn_order mis à jour : " + p.getNom() + " → " + p.getBtn_order());
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("ORDRE", "Échec mise à jour ordre pour " + p.getNom(), e);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.removeView(btnDirect);
            toolbar.removeView(btnTable);
        }
        super.onDestroyView();
    }
}
