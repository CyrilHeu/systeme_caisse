package com.example.tablettegourmande.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.tablettegourmande.models.Categorie;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utils.Normalize;

public class CategorieService {

    private final CollectionReference categoriesRef;
    private final CollectionReference produitsRef;

    public CategorieService(String restaurantId) {

        if (restaurantId == null || restaurantId.isEmpty()) {
            throw new IllegalArgumentException("restaurantId ne peut pas être NULL ou vide !");
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        categoriesRef = db.collection("restaurants").document(restaurantId).collection("categories");
        produitsRef = db.collection("restaurants").document(restaurantId).collection("produits");
    }

    // ✅ Ajouter une catégorie
    public void ajouterCategorie(String categoryName, String color, int order, Callback callback) {

        if (categoryName == null || categoryName.trim().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Le nom de la catégorie est vide !"));
            return;
        }

        // Vérifier si la catégorie existe déjà
        categoriesRef.whereEqualTo("nom", categoryName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                callback.onFailure(new Exception("Cette catégorie existe déjà !")); // ✅ Envoie bien ce message
            } else {
                // Ajouter la nouvelle catégorie
                Map<String, Object> newCategory = new HashMap<>();
                newCategory.put("nom", categoryName);
                newCategory.put("btn_color", color);
                newCategory.put("btn_order", order);

                categoriesRef.add(newCategory)
                        .addOnSuccessListener(documentReference -> callback.onSuccess())
                        .addOnFailureListener(callback::onFailure);
            }
        });
    }

    public void modifierCategorie(String ancienNom, String nouveauNom, String nouvelleCouleur, Callback callback) {
        if (nouveauNom == null || nouveauNom.trim().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Le nom de la catégorie est vide !"));
            return;
        }

        // Étape 1️⃣ : Récupérer l’ID de la catégorie actuelle et la modifier directement
        categoriesRef.whereEqualTo("nom", ancienNom).get().addOnCompleteListener(taskOld -> {
            if (!taskOld.isSuccessful() || taskOld.getResult().isEmpty()) {
                return; // On arrête ici, mais inutile d'afficher une erreur puisque tout est géré ailleurs
            }

            DocumentSnapshot documentOld = taskOld.getResult().getDocuments().get(0);
            String idCategorieActuelle = documentOld.getId();

            // Étape 2️⃣ : Vérifier si une autre catégorie avec ce nom existe
            categoriesRef.whereEqualTo("nom", nouveauNom).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        if (!document.getId().equals(idCategorieActuelle)) {
                            callback.onFailure(new Exception("Une catégorie avec ce nom existe déjà !"));
                            return;
                        }
                    }
                }

                // Étape 3️⃣ : Mise à jour de la catégorie sans vérifications inutiles
                documentOld.getReference().update("nom", nouveauNom, "btn_color", nouvelleCouleur)
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(callback::onFailure);
            });

        });
    }


    public void supprimerCategorie(String nomCategorie, Callback callback) {
        produitsRef.whereEqualTo("categorie", nomCategorie).get().addOnCompleteListener(taskProduits -> {
            if (taskProduits.isSuccessful()) {
                if (!taskProduits.getResult().isEmpty()) {
                    callback.onFailure(new Exception("Impossible de supprimer, des produits sont liés à cette catégorie !"));
                } else {
                    categoriesRef.whereEqualTo("nom", nomCategorie).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                int ordreSupprime = document.getLong("btn_order").intValue();

                                // Supprimer la catégorie
                                document.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // Mettre à jour les autres catégories après suppression
                                            reordonnerCategories(ordreSupprime, callback);
                                        })
                                        .addOnFailureListener(callback::onFailure);
                            }
                        } else {
                            callback.onFailure(new Exception("Catégorie non trouvée !"));
                        }
                    });
                }
            } else {
                callback.onFailure(taskProduits.getException());
            }
        });
    }

    // Méthode pour réordonner les catégories après suppression
    private void reordonnerCategories(int ordreSupprime, Callback callback) {
        categoriesRef.whereGreaterThan("btn_order", ordreSupprime).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    int nouvelOrdre = document.getLong("btn_order").intValue() - 1;
                    document.getReference().update("btn_order", nouvelOrdre);
                }
                callback.onSuccess();
            } else {
                callback.onFailure(task.getException());
            }
        });
    }

    public void getCategories(CallbackList callback) {
        categoriesRef.orderBy("btn_order").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Categorie> categories = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nom = document.getString("nom");
                    String couleur = document.getString("btn_color");

                    if (nom != null && couleur != null) {
                        categories.add(new Categorie(nom, couleur)); // ✅ Ajoute l'objet `Categorie`
                    }
                }
                callback.onSuccess(categories);
            } else {
                callback.onFailure(task.getException());
            }
        });
    }


    public void getCategorieByNom(String nomCategorie, CallbackCategorie callback) {
        categoriesRef.whereEqualTo("nom", nomCategorie).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Map<String, Object> categorieData = document.getData();

                    if (categorieData.containsKey("btn_color")) {
                        callback.onSuccess(categorieData);
                    } else {
                        callback.onFailure(new Exception("Le champ btn_color est introuvable !"));
                    }
                    return;
                }
            }
            callback.onFailure(new Exception("Catégorie non trouvée !"));
        });
    }
    public void updateCategoryOrder(List<String> newOrder, Callback callback) {
        for (int i = 0; i < newOrder.size(); i++) {
            String categoryName = newOrder.get(i);
            int newOrderIndex = i;

            categoriesRef.whereEqualTo("nom", categoryName).get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    document.getReference().update("btn_order", newOrderIndex)
                            .addOnFailureListener(callback::onFailure);
                }
                if (newOrderIndex == newOrder.size() - 1) {
                    callback.onSuccess();
                }
            }).addOnFailureListener(callback::onFailure);
        }
    }

    public void modifierCategorieEtProduits(String ancienNom, String nouveauNom, String nouvelleCouleur, Callback callback) {
        if (nouveauNom == null || nouveauNom.trim().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Le nom de la catégorie est vide !"));
            return;
        }

        // Vérifier si une catégorie avec ce nom existe déjà (sauf si c'est le même)
        categoriesRef.whereEqualTo("nom", nouveauNom).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty() && !ancienNom.equals(nouveauNom)) {
                callback.onFailure(new Exception("Une catégorie avec ce nom existe déjà !"));
            } else {
                // Modifier la catégorie existante
                categoriesRef.whereEqualTo("nom", ancienNom).get().addOnCompleteListener(taskOld -> {
                    if (taskOld.isSuccessful() && !taskOld.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : taskOld.getResult()) {
                            Map<String, Object> categoryData = document.getData();
                            categoryData.put("nom", nouveauNom);
                            categoryData.put("btn_color", nouvelleCouleur);

                            document.getReference().update(categoryData)
                                    .addOnSuccessListener(aVoid -> {
                                        // ✅ Une fois la catégorie modifiée, mettre à jour les produits
                                        mettreAJourProduitsCategorie(ancienNom, nouveauNom, callback);
                                    })
                                    .addOnFailureListener(callback::onFailure);
                        }
                    } else {
                        callback.onFailure(new Exception("Catégorie non trouvée !"));
                    }
                });
            }
        });
    }
    private void mettreAJourProduitsCategorie(String ancienNomCategorie, String nouveauNomCategorie, Callback callback) {
        produitsRef.whereEqualTo("categorie", ancienNomCategorie).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    document.getReference().update("categorie", nouveauNomCategorie)
                            .addOnFailureListener(callback::onFailure);
                }
                callback.onSuccess(); // ✅ Confirme que tous les produits ont été mis à jour
            } else {
                callback.onFailure(task.getException());
            }
        });
    }


    // Interface pour récupérer les infos d'une catégorie
    public interface CallbackCategorie {
        void onSuccess(Map<String, Object> categorieData);
        void onFailure(@NonNull Exception e);
    }


    // Interface callback pour les listes
    public interface CallbackList {
        void onSuccess(List<Categorie> data);
        void onFailure(@NonNull Exception e);
    }

    // Interface Callback pour gérer les résultats asynchrones
    public interface Callback {
        void onSuccess();
        void onFailure(@NonNull Exception e);
    }
}
