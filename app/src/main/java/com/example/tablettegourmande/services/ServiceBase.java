package com.example.tablettegourmande.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.tablettegourmande.MainActivity;
import com.example.tablettegourmande.models.Utilisateur;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ServiceBase {

    private static final String TAG = "ServiceBase";
    protected FirebaseAuth auth;
    protected FirebaseFirestore db;
    private String restaurantId;
    private static final String PREFS_NAME = "APP_PREFS";
    private static final String KEY_RESTAURANT_ID = "restaurantId";
    private Utilisateur app_current_user;


    public ServiceBase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadRestaurantID();

    }

    /**
     * Récupère l'utilisateur actuellement connecté.
     */
    protected FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void loadRestaurantID() {
        if (restaurantId != null) {
            Log.d("ServiceBase", "Restaurant ID chargé depuis SharedPreferences : " + restaurantId);
            return;
        }

        // 🔥 Si non stocké en local, on récupère depuis Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            restaurantId = documentSnapshot.getString("restaurantId");

                            if (restaurantId != null) {

                                    Log.d("ServiceBase", "✅ restaurantId stocké en SharedPreferences : " + restaurantId);
                                    Log.d("ServiceBase", "Restaurant ID chargé depuis Firebase et stocké localement : " + restaurantId);
                            } else {
                                Log.e("ServiceBase", "restaurantId est NULL après chargement Firebase !");
                            }
                        } else {
                            Log.e("ServiceBase", "Le document utilisateur n'existe pas !");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("ServiceBase", "Erreur lors du chargement de restaurantId", e));
        } else {
            Log.e("ServiceBase", "Aucun utilisateur connecté !");
        }
    }

    protected void loadRestaurantName(Context context, RestaurantNameCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            String userUID = user.getUid();

            // Charger l'ID du restaurant depuis le document utilisateur
            db.collection("users")
                    .document(userUID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot userDocument = task.getResult();
                            String restaurantID = userDocument.getString("restaurantId");

                            if (restaurantID != null) {
                                // Charger le nom du restaurant à partir de l'ID du restaurant
                                db.collection("restaurants")
                                        .document(restaurantID)
                                        .get()
                                        .addOnCompleteListener(restaurantTask -> {
                                            if (restaurantTask.isSuccessful() && restaurantTask.getResult() != null) {
                                                DocumentSnapshot restaurantDocument = restaurantTask.getResult();
                                                String restaurantName = restaurantDocument.getString("_name");
                                                callback.onSuccess(restaurantName != null ? restaurantName : "Nom inconnu");
                                            } else {
                                                Log.e(TAG, "Failed to fetch restaurant name: ", restaurantTask.getException());
                                                callback.onFailure("Impossible de récupérer le nom du restaurant.");
                                            }
                                        });
                            } else {
                                callback.onFailure("Aucun restaurant associé à cet utilisateur.");
                            }
                        } else {
                            Log.e(TAG, "Failed to fetch user document: ", task.getException());
                            callback.onFailure("Impossible de récupérer les informations de l'utilisateur.");
                        }
                    });
        } else {
            callback.onFailure("Aucun utilisateur connecté.");
        }
    }

    protected String getUserEmail() {
        FirebaseUser user = getCurrentUser();
        return (user != null) ? user.getEmail() : null;
    }

    public String getRestaurantId() {
        return restaurantId;
    }
    public interface FirestoreCallback {
        void onSuccess(DocumentSnapshot document);
        void onFailure(Exception e);
    }

    public interface RestaurantIDCallback {
        void onSuccess(String restaurantID);

        void onFailure(String errorMessage);
    }
    public interface RestaurantNameCallback {
        void onSuccess(String restaurantName);
        void onFailure(String errorMessage);
    }
}
