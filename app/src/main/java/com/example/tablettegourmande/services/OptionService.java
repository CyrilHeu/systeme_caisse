package com.example.tablettegourmande.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Option;
import com.example.tablettegourmande.models.Produit;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utils.CustomToast;

public class OptionService {
    private final CollectionReference optionsRef;

    public OptionService(String restaurantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        optionsRef = db.collection("restaurants").document(restaurantId).collection("options");
    }

    public void ajouterOption(Option option, CallbackSimple callback) {
        optionsRef.document(option.getNom()).set(option)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Option ajoutée avec succès");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Erreur lors de l'ajout de l'option", e));
    }



    public void modifierOption(Option option, CallbackSimple callback) {
        Log.d("DEBUG", "Mise à jour de l'option: " + option.getNom() + ", Choix: " + option.getChoix());

        optionsRef.document(option.getNom()).get().addOnSuccessListener(documentSnapshot -> {
            Option ancienneOption = documentSnapshot.toObject(Option.class);
            if (ancienneOption != null && ancienneOption.equals(option)) { // ✅ Vérifie avant d’écrire
                callback.onSuccess(); // Rien à changer
                return;
            }

            // Si l’option a changé, alors seulement on met à jour Firebase
            optionsRef.document(option.getNom()).update(
                    "multiple", option.isMultiple(),
                    "choix", option.getChoix()
            ).addOnSuccessListener(aVoid -> {
                callback.onSuccess();
            });
        });

    }


    public void supprimerOption(String restaurantId, String optionNom, Callback callback) {
        // Supprimer l’option des produits
        supprimerOptionDeTousLesProduits(restaurantId, optionNom);

        // Supprimer l’option elle-même
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference optionsRef = db.collection("restaurants").document(restaurantId).collection("options");
        optionsRef.document(optionNom).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void getOptions(CallbackSimpleList callback) {
        optionsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Option> options = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    options.add(document.toObject(Option.class));
                }
                callback.onSuccess(options);
            }
        });
    }

    public void verifierDoublonNom(String nom, CallbackSimple callbackExiste, CallbackSimple callbackNonExiste) {
        optionsRef.document(nom).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Log.d("DEBUG", "Option déjà existante : " + nom);
                callbackExiste.onSuccess();  // Si l'option existe déjà
            } else {
                callbackNonExiste.onSuccess();  // Si l'option n'existe pas encore
            }
        }).addOnFailureListener(e -> Log.e("Firebase", "Erreur lors de la vérification du doublon", e));
    }

    public void mettreAJourProduitsAvecOption(String restaurantId, String ancienNom, Option nouvelleOption, CallbackSimple callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference produitsRef = db.collection("restaurants").document(restaurantId).collection("produits");

        produitsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                Produit produit = document.toObject(Produit.class);
                if (produit != null && produit.getOptions() != null) {
                    List<String> optionsProduit = new ArrayList<>(produit.getOptions());

                    // 🔥 Recherche de l'option à modifier dans la liste
                    int indexOption = optionsProduit.indexOf(ancienNom);
                    if (indexOption != -1) {
                        optionsProduit.set(indexOption, nouvelleOption.getNom()); // ✅ Mise à jour de l'option existante
                    } else {
                        Log.w("DEBUG", "Option " + ancienNom + " non trouvée dans " + produit.getNom());
                    }

                    // 🔥 Mise à jour de Firebase uniquement si modification effectuée
                    produitsRef.document(document.getId()).update("options", optionsProduit)
                            .addOnSuccessListener(aVoid -> Log.d("DEBUG", "Option mise à jour dans le produit : " + produit.getNom()))
                            .addOnFailureListener(e -> Log.e("Firebase", "Erreur lors de la mise à jour de l'option dans les produits", e));
                }
            }
            callback.onSuccess();
        }).addOnFailureListener(e -> Log.e("Firebase", "Erreur lors de la récupération des produits", e));
    }




    public void recupererIndexOptionDansProduits(String restaurantId, String optionNom, CallbackIndex callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference produitsRef = db.collection("restaurants").document(restaurantId).collection("produits");

        produitsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            int indexTrouve = -1;

            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                Produit produit = document.toObject(Produit.class);
                if (produit != null && produit.getOptions() != null) {
                    List<String> optionsProduit = produit.getOptions();
                    int index = optionsProduit.indexOf(optionNom);

                    if (index != -1) {
                        indexTrouve = index;
                        break;  // On arrête dès qu'on trouve l'index
                    }
                }
            }

            callback.onSuccess(indexTrouve);

        }).addOnFailureListener(e -> Log.e("Firebase", "Erreur lors de la récupération de l'index de l'option", e));
    }


    public void supprimerOptionDeTousLesProduits(String restaurantId, String optionSupprimee) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference produitsRef = db.collection("restaurants").document(restaurantId).collection("produits");

        produitsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                Produit produit = document.toObject(Produit.class);
                if (produit != null && produit.getOptions() != null) {
                    List<String> optionsProduit = new ArrayList<>(produit.getOptions());

                    if (optionsProduit.contains(optionSupprimee)) {
                        optionsProduit.remove(optionSupprimee);

                        // 🔥 Mettre à jour Firebase en supprimant l'option
                        produitsRef.document(document.getId()).update("options", optionsProduit)
                                .addOnSuccessListener(aVoid -> Log.d("DEBUG", "Option supprimée du produit : " + produit.getNom()))
                                .addOnFailureListener(e -> Log.e("Firebase", "Erreur lors de la suppression de l'option dans les produits", e));
                    }
                }
            }
        }).addOnFailureListener(e -> Log.e("Firebase", "Erreur lors de la récupération des produits", e));
    }

    public interface CallbackSimple {
        void onSuccess();
    }
    public interface CallbackSimpleList {
        void onSuccess(List<Option> data);
    }

    public interface Callback {
        void onSuccess();
        void onFailure(@NonNull Exception e);
    }

    public interface CallbackList {
        void onSuccess(List<Option> data);
        void onFailure(@NonNull Exception e);
    }
    public interface CallbackIndex {
        void onSuccess(int index);
    }

}
