package com.example.tablettegourmande.ui.home;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.permissions.PermissionUtils;
import com.example.tablettegourmande.ui.GestionUtilisateurs.FragmentUtilisateur;
import com.example.tablettegourmande.ui.gestion.FragmentGestion;
import com.example.tablettegourmande.ui.restaurant.FragmentRestaurant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utils.CustomToast;
import Utils.DragAndDropHandler;

public class HomeFragment extends Fragment {

    private GridLayout gridLayout;
    private ArrayList<View> cardViews = new ArrayList<>();
    private String restaurantId;
    private DragAndDropHandler dragAndDropHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        gridLayout = root.findViewById(R.id.grid_layout);

        // Initialiser le gestionnaire de drag-and-drop
        dragAndDropHandler = new DragAndDropHandler(cardViews, gridLayout);

        // Configurer le callback pour la sauvegarde
        dragAndDropHandler.setDragEventListener(() -> {
            Log.d("HomeFragment", "Drag event dropped, saving configuration...");
            saveButtonConfigToFirestore(); // Sauvegarder après un drop
        });

        // Charger les boutons
        loadRestaurantIdAndSetupButtons();

        //Mode dev - Restaurant
        /*
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new FragmentRestaurant());
        transaction.addToBackStack(null); // Permet le retour en arrière
        transaction.commit();*/

        return root;
    }
    private void loadRestaurantIdAndSetupButtons() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("HomeFragment", "Utilisateur non connecté !");
            setupGrid(12); // Charger une grille par défaut
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        restaurantId = documentSnapshot.getString("restaurantId");
                        if (restaurantId != null) {
                            loadButtonConfigFromFirestore();
                            SharedPreferences prefs = getContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("restaurantId", restaurantId);
                            editor.apply();
                            Log.d("TAG123", "setup buttons and id"+restaurantId);

                        } else {
                            Log.e("HomeFragment", "Restaurant ID introuvable !");
                            setupGrid(12); // Charger une grille par défaut
                        }
                    } else {
                        Log.e("HomeFragment", "Document utilisateur introuvable !");
                        setupGrid(12);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Erreur lors du chargement de l'utilisateur", e);
                    setupGrid(12);
                });
    }

    private void setupGrid(int itemCount) {
        cardViews.clear();
        gridLayout.removeAllViews();

        for (int i = 0; i < itemCount; i++) {
            CardView cardView = createCardView(i, "Bouton " + (i + 1), "ic_launcher_foreground");
            cardViews.add(cardView);
            gridLayout.addView(cardView);
        }

        dragAndDropHandler.setupDragAndDrop();
        Log.d("HomeFragment", "Grille configurée avec " + itemCount + " boutons par défaut.");
    }

    private CardView createCardView(int index, String label, String icon) {
        CardView cardView = new CardView(requireContext());
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = 0;
        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        layoutParams.setMargins(16, 16, 16, 16);
        cardView.setLayoutParams(layoutParams);

        cardView.setPadding(16, 16, 16, 16);
        cardView.setCardElevation(8);
        cardView.setRadius(12);
        cardView.setClickable(true);
        cardView.setFocusable(true);
        cardView.setTag("Card_" + index);
        cardView.setBackgroundColor(getResources().getColor(R.color.light_gray, null));

        TypedValue outValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        cardView.setForeground(requireContext().getDrawable(outValue.resourceId));

        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(View.TEXT_ALIGNMENT_CENTER);

        ImageView imageView = new ImageView(requireContext());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(100, 120)); // position de l'icon sur la CardView
        imageView.setImageResource(getResources().getIdentifier(icon, "drawable", requireContext().getPackageName()));
        imageView.setTag(icon);
        linearLayout.addView(imageView);

        TextView textView = new TextView(requireContext());
        textView.setText(label);
        textView.setTextSize(16);
        textView.setLines(3);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        linearLayout.addView(textView);

        cardView.addView(linearLayout);

        // Configure le drag-and-drop pour cette CardView
        setupCardViewDragAndDrop(cardView);

        return cardView;
    }

    private void saveButtonConfigToFirestore() {
        if (restaurantId == null) {
            Log.e("HomeFragment", "Impossible de sauvegarder : Restaurant ID manquant.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("restaurants")
                .document(restaurantId)
                .collection("settings")
                .document("home button config");

        db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(docRef);
                    if (!snapshot.exists()) {
                        Log.e("HomeFragment", "Document Firestore introuvable !");
                        return null;
                    }

                    // Récupérer la liste existante des boutons
                    List<Map<String, Object>> existingButtons = (List<Map<String, Object>>) snapshot.get("buttons");
                    if (existingButtons == null) {
                        Log.e("HomeFragment", "Aucune donnée de boutons existante !");
                        return null;
                    }

                    // Créer une nouvelle liste qui conservera l'ordre mis à jour
                    List<Map<String, Object>> updatedButtons = new ArrayList<>();

                    for (int i = 0; i < cardViews.size(); i++) {
                        View cardView = cardViews.get(i);
                        LinearLayout contentLayout = (LinearLayout) ((CardView) cardView).getChildAt(0);

                        TextView textView = (TextView) contentLayout.getChildAt(1);
                        ImageView imageView = (ImageView) contentLayout.getChildAt(0);

                        // Récupérer l'ID FIXE du bouton, en le recherchant dans la liste existante
                        String buttonId = null;
                        for (Map<String, Object> buttonData : existingButtons) {
                            cardView.setTag(buttonData.get("id"));
                            if (buttonData.get("label").equals(textView.getText().toString()) &&
                                    buttonData.get("icon").equals(imageView.getTag())) {
                                buttonId = (String) buttonData.get("id"); // Récupérer l'ID d'origine
                                break;
                            }
                        }

                        if (buttonId == null) {
                            Log.e("HomeFragment", "Impossible de retrouver l'ID du bouton !");
                            continue;
                        }

                        // Créer une nouvelle version du bouton avec le même ID, mais la nouvelle position
                        Map<String, Object> updatedButton = new HashMap<>();
                        updatedButton.put("id", buttonId); // L'ID reste inchangé
                        updatedButton.put("label", textView.getText().toString());
                        updatedButton.put("position", i); // Seule la position change
                        updatedButton.put("icon", (String) imageView.getTag());

                        updatedButtons.add(updatedButton);
                    }

                    // Mettre à jour Firestore avec la nouvelle liste de boutons
                    transaction.update(docRef, "buttons", updatedButtons);
                    return null;
                }).addOnSuccessListener(aVoid -> Log.d("HomeFragment", "Mise à jour des positions réussie sans changer les ID."))
                .addOnFailureListener(e -> Log.e("HomeFragment", "Erreur lors de la mise à jour.", e));
    }

    private void saveInitialButtonConfigToFirestore() {
        if (restaurantId == null) {
            Log.e("HomeFragment", "Impossible de sauvegarder : Restaurant ID manquant.");
            return;
        }

        ArrayList<Map<String, Object>> buttons = new ArrayList<>();
        for (int i = 0; i < cardViews.size(); i++) {
            View cardView = cardViews.get(i);
            LinearLayout contentLayout = (LinearLayout) ((CardView) cardView).getChildAt(0);
            TextView textView = (TextView) contentLayout.getChildAt(1);
            ImageView imageView = (ImageView) contentLayout.getChildAt(0);

            Map<String, Object> buttonData = new HashMap<>();
            buttonData.put("id", "button_" + i);
            buttonData.put("label", textView.getText().toString());
            buttonData.put("position", i);
            buttonData.put("icon", (String) imageView.getTag());

            buttons.add(buttonData);
        }

        Map<String, Object> buttonConfig = new HashMap<>();
        buttonConfig.put("buttons", buttons);

        FirebaseFirestore.getInstance()
                .collection("restaurants")
                .document(restaurantId)
                .collection("settings")
                .document("home button config")
                .set(buttonConfig)
                .addOnSuccessListener(aVoid -> Log.d("HomeFragment", "Configuration sauvegardée avec succès."))
                .addOnFailureListener(e -> Log.e("HomeFragment", "Erreur lors de la sauvegarde.", e));
    }
    private void loadButtonConfigFromFirestore() {
        Log.d("HomeFragment", "loadButtonConfigFromFirestore called");

        if (restaurantId == null) {
            Log.e("HomeFragment", "Restaurant ID is null");
            setupGridWithDefaults(); // Charger une grille par défaut si aucun ID de restaurant
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("restaurants")
                .document(restaurantId)
                .collection("settings")
                .document("home button config")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("HomeFragment", "Document found in Firestore");

                        ArrayList<Map<String, Object>> buttons = (ArrayList<Map<String, Object>>) documentSnapshot.get("buttons");
                        if (buttons != null && !buttons.isEmpty()) {
                            Log.d("HomeFragment", "Buttons found: " + buttons.toString());
                            cardViews.clear();
                            gridLayout.removeAllViews();

                            for (Map<String, Object> buttonData : buttons) {
                                String label = (String) buttonData.get("label");
                                String icon = (String) buttonData.getOrDefault("icon", "ic_launcher_foreground");

                                // Créez les boutons à partir des données de Firestore
                                CardView cardView = createCardView(buttons.indexOf(buttonData), label, icon);
                                cardView.setTag((String) buttonData.get("id"));
                                cardViews.add(cardView);
                                gridLayout.addView(cardView);
                            }

                            dragAndDropHandler.setupDragAndDrop();
                            Log.d("HomeFragment", "Buttons loaded from Firestore.");
                        } else {
                            Log.w("HomeFragment", "No buttons found in Firestore. Creating default buttons...");
                            setupGridWithDefaults(); // Crée une grille par défaut
                        }
                    } else {
                        Log.w("HomeFragment", "Document not found in Firestore. Creating default buttons...");
                        setupGridWithDefaults(); // Crée une grille par défaut
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Error loading from Firestore", e);
                    setupGridWithDefaults(); // Crée une grille par défaut en cas d'erreur
                });
    }

    private void setupCardViewDragAndDrop(CardView cardView) {
        cardView.setOnLongClickListener(v -> {
            ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
            ClipData dragData = new ClipData(
                    (CharSequence) v.getTag(),
                    new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                    item
            );

            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(dragData, shadowBuilder, v, 0);
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            return true;
        });

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("view selected :",cardView.getTag().toString());
                switch (cardView.getTag().toString()){
                    case "button_0" :
                        // restaurant
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, new FragmentRestaurant());
                        transaction.addToBackStack(null); // Permet le retour en arrière
                        transaction.commit();
                        break;
                    case "button_1" :
                        // Gestion : produit, catégories, options, menu
                        FragmentTransaction transactionGestion = getParentFragmentManager().beginTransaction();
                        transactionGestion.replace(R.id.fragment_container, new FragmentGestion());
                        transactionGestion.addToBackStack(null);
                        transactionGestion.commit();
                        break;
                    case "button_2" :
                        // Mode nuit/sécurité
                        break;
                    case "button_3" :
                        // parametre restaurant
                        break;
                    case "button_4" :
                        // parametre avancés des taxes
                        break;
                    case "button_5" :
                        // z-reporting
                        break;
                    case "button_6" :
                        // mise a jour système
                        break;
                    case "button_7" :
                        // configuration matérielle
                        break;
                    case "button_8" :
                        // Utlisateurs : gestion, modification, ajout, ...
                        PermissionUtils.hasPermission("Gestion des utilisateurs", new PermissionUtils.PermissionCallback() {
                            @Override
                            public void onResult(boolean authorized) {
                                if(authorized){
                                    FragmentTransaction transactionUtilisateur = getParentFragmentManager().beginTransaction();
                                    transactionUtilisateur.replace(R.id.fragment_container, new FragmentUtilisateur());
                                    transactionUtilisateur.addToBackStack(null);
                                    transactionUtilisateur.commit();
                                }else{
                                    CustomToast.show(getContext(),"Droits insuffisants pour effectuer ces actions.", R.drawable.ic_warning);
                                }
                            }
                        });

                        break;
                    case "button_9" :
                        // Personnaliser l'interface
                        break;
                    case "button_10" :
                        // historique des commandes
                        break;
                    case "button_11" :
                        // Rapports de performance
                        break;
                }
            }
        });
    }

    private void setupGridWithDefaults() {
        cardViews.clear();
        gridLayout.removeAllViews();

        // Définir les boutons par défaut
        String[][] defaultButtons = {
                {"Restaurant", "ic_launcher_foreground"},
                {"Gestion : produit, catégories, options, menu", "ic_launcher_foreground"},
                {"Mode Nuit/Sécurité", "ic_launcher_foreground"},
                {"Paramètres restaurant", "ic_launcher_foreground"},
                {"Paramètres avancés des taxes", "ic_launcher_foreground"},
                {"Z-Reporting", "ic_launcher_foreground"},
                {"Mise à jour système", "ic_launcher_foreground"},
                {"Configuration matérielle", "ic_launcher_foreground"},
                {"Paramètres utilisateurs", "ic_launcher_foreground"},
                {"Personnaliser l’interface", "ic_launcher_foreground"},
                {"Historique des commandes", "ic_launcher_foreground"},
                {"Rapports de performance", "ic_launcher_foreground"}
        };

        // Créer et ajouter les boutons par défaut
        for (int i = 0; i < defaultButtons.length; i++) {
            String label = defaultButtons[i][0];
            String icon = defaultButtons[i][1];

            CardView cardView = createCardView(i, label, icon);
            cardView.setTag("button_"+i);
            cardViews.add(cardView);
            gridLayout.addView(cardView);

        }
        dragAndDropHandler.setupDragAndDrop();

        // Sauvegarder les boutons par défaut dans Firestore
        saveInitialButtonConfigToFirestore();
        Log.d("HomeFragment", "Default buttons created and saved to Firestore.");
    }

}
