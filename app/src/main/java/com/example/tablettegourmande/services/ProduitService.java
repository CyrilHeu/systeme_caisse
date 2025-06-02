package com.example.tablettegourmande.services;

import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Produit;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import Utils.ButtonUtils;

public class ProduitService {
    private final CollectionReference produitsRef;
    private final CollectionReference categoriesRef;

    public ProduitService(String restaurantId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        produitsRef = db.collection("restaurants").document(restaurantId).collection("produits");
        categoriesRef = db.collection("restaurants").document(restaurantId).collection("categories");
    }

    private String getRestaurantIdFromPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("restaurantId", null);
    }

    public void ajouterProduit(String nom, String couleur, double prix, String categorieNom, List<String> options, Callback callback) {
        if (categorieNom == null || categorieNom.trim().isEmpty()) {
            callback.onFailure(new Exception("categorieNom manquant"));
            return;
        }

        produitsRef.whereEqualTo("categorie", categorieNom)
                .get()
                .addOnSuccessListener(query -> {
                    long maxOrder = 0;
                    for (QueryDocumentSnapshot doc : query) {
                        Produit p = doc.toObject(Produit.class);
                        if (p.getBtn_order() != null && p.getBtn_order() > maxOrder) {
                            maxOrder = p.getBtn_order();
                        }
                    }
                    long ordre = maxOrder + 1;

                    Produit produit = new Produit();
                    produit.setNom(nom);
                    produit.setCouleur(couleur);
                    produit.setPrix(prix);
                    produit.setCategorie(categorieNom);
                    produit.setOptions(options);
                    produit.setBtn_order(ordre);

                    produitsRef.add(produit)
                            .addOnSuccessListener(doc -> {
                                doc.update("id", doc.getId());
                                callback.onSuccess();
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);

    }


    public void modifierProduit(String ancienNom, String nouveauNom, String nouvelleCouleur, double nouveauPrix, String nouvelleCategorie, List<String> options, Callback callback) {
        produitsRef.whereEqualTo("nom", ancienNom).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    document.getReference().update(
                                    "nom", nouveauNom,
                                    "couleur", nouvelleCouleur,
                                    "prix", nouveauPrix,
                                    "categorie", nouvelleCategorie,
                                    "options", options

                            ).addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(callback::onFailure);
                }
            } else {
                callback.onFailure(new Exception("Produit non trouvé !"));
            }
        });
    }


    public void supprimerProduit(String nomProduit, Callback callback) {
        produitsRef.whereEqualTo("nom", nomProduit).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    document.getReference().delete()
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(callback::onFailure);
                }
            } else {
                callback.onFailure(new Exception("Produit non trouvé !"));
            }
        });
    }

    public void getProduits(CallbackList callback) {
        produitsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Produit> produits = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    List<String> options = (List<String>) document.get("options");
                    if (options == null) options = new ArrayList<>();

                    Produit produit = new Produit(
                            document.getString("nom"),
                            document.getString("couleur"),
                            document.getDouble("prix"),
                            document.getString("categorie"),
                            document.getLong("btn_order"),
                            options
                    );

                    // ✅ Affecter l'ID Firestore
                    produit.setId(document.getId());

                    produits.add(produit);
                }

                Collections.sort(produits, (p1, p2) -> p1.getNom().compareToIgnoreCase(p2.getNom()));
                callback.onSuccess(produits);
            } else {
                callback.onFailure(task.getException());
            }
        });
    }

    public void getOptions(String restaurantId, CallbackSimpleList callbackSuccess, CallbackFailure callbackError) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference optionsRef = db.collection("restaurants").document(restaurantId).collection("options"); // 🔥 Récupération des options spécifiques au restaurant

        optionsRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> optionsList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nom = document.getId(); // 🔥 Utilisation de l'ID du document comme nom de l'option
                            optionsList.add(nom);
                        }
                        Log.d("DEBUG", "Options finales après récupération: " + optionsList);
                        callbackSuccess.onSuccess(optionsList); // ✅ On retourne bien une List<String>
                    } else {
                        if (callbackError != null) {
                            callbackError.onFailure(task.getException()); // ✅ Gestion des erreurs
                        }
                        Log.e("Firebase", "Erreur lors de la récupération des options", task.getException());
                    }
                });
    }

    public void getCategories(CallbackListCategories callback) {
        FirebaseFirestore.getInstance().collection("categories")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> categories = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            categories.add(document.getString("nom")); // 🔥 Assure-toi que "nom" est le bon champ
                        }
                        callback.onSuccess(categories);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }


    public void verifierProduitExiste(String nomProduit, CallbackBoolean callback) {
        produitsRef.whereEqualTo("nom", nomProduit).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                callback.onResult(true); // ✅ Le produit existe déjà
            } else {
                callback.onResult(false); // ❌ Le produit n'existe pas
            }
        });
    }
    public void ecouterCategories(CallbackListCategories callback) {
        categoriesRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                callback.onFailure(e);
                return;
            }

            if (queryDocumentSnapshots != null) {
                List<String> categories = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    categories.add(document.getString("nom"));
                }
                callback.onSuccess(categories);
            }
        });
    }



    public void supprimerProduitsParCategorie(String nomCategorie, Callback callback) {
        produitsRef.whereEqualTo("categorie", nomCategorie).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments(); // 🔥 Correction ici
                if (documents.isEmpty()) {
                    callback.onSuccess(); // ✅ Aucun produit à supprimer
                    return;
                }

                // 🔥 Supprimer chaque produit lié à la catégorie supprimée
                int[] compteurSuppressions = {0};
                for (DocumentSnapshot document : documents) { // 🔥 Correction ici
                    document.getReference().delete()
                            .addOnSuccessListener(aVoid -> {
                                compteurSuppressions[0]++;
                                if (compteurSuppressions[0] == documents.size()) {
                                    callback.onSuccess(); // ✅ Suppression terminée
                                }
                            })
                            .addOnFailureListener(callback::onFailure);
                }
            } else {
                callback.onFailure(task.getException());
            }
        });
    }
    public ListenerRegistration getProduitsStream(CallbackList callback) {
        return produitsRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                callback.onFailure(e);
                return;
            }

            List<Produit> produits = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                List<String> options = (List<String>) document.get("options");
                if (options == null) {
                    options = new ArrayList<>();
                }

                produits.add(new Produit(
                        document.getString("nom"),
                        document.getString("couleur"),
                        document.getDouble("prix"),
                        document.getString("categorie"),
                        document.getLong("btn_order"),
                        options  // 🔥 Options avec une liste par défaut si null
                ));

            }
            Collections.sort(produits, (p1, p2) -> p1.getNom().compareToIgnoreCase(p2.getNom()));
            callback.onSuccess(produits);
        });
    }


    public void mettreAJourOrdreProduit(String produitId, Long nouvelOrdre, Callback callback) {
        produitsRef.document(produitId)
                .update("btn_order", nouvelOrdre)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public interface OnProduitsLoadedListener {
        void onProduitsLoaded(List<Produit> produits);
    }

    // Interface callback pour la vérification de l'existence
    public interface CallbackBoolean {
        void onResult(boolean existe);
    }
    public interface CallbackList {
        void onSuccess(List<Produit> data);
        void onFailure(@NonNull Exception e);
    }

    public interface CallbackSimpleList {
        void onSuccess(List<String> data);
    }

    public interface CallbackFailure {
        void onFailure(@NonNull Exception e);
    }



    public interface Callback {
        void onSuccess();
        void onFailure(@NonNull Exception e);
    }
    public interface CallbackListCategories {
        void onSuccess(List<String> categories);
        void onFailure(@NonNull Exception e);
    }


}
