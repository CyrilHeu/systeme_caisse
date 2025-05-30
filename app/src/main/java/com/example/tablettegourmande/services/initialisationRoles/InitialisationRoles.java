package com.example.tablettegourmande.services.initialisationRoles;

import android.util.Log;

import com.example.tablettegourmande.permissions.PermissionTemplates;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class InitialisationRoles {

    private static final String TAG = "InitRoles";

    public static void lancerInitialisation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Aucun utilisateur connecté.");
            return;
        }

        String userUID = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Récupérer restaurantId depuis le document utilisateur
        db.collection("users")
                .document(userUID)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists() && userDoc.contains("restaurantId")) {
                        String restaurantId = userDoc.getString("restaurantId");
                        if (restaurantId == null || restaurantId.isEmpty()) {
                            Log.e(TAG, "restaurantId vide ou null");
                            return;
                        }

                        // Requête pour vérifier si les rôles manager et serveur existent
                        db.collection("restaurants")
                                .document(restaurantId)
                                .collection("roles")
                                .get()
                                .addOnSuccessListener(query -> {
                                    boolean hasManager = false;
                                    boolean hasServeur = false;

                                    for (var doc : query.getDocuments()) {
                                        String id = doc.getId();
                                        if (id.equals("Manager")) hasManager = true;
                                        if (id.equals("Serveur")) hasServeur = true;
                                    }

                                    if (!hasManager) initialiserManager(db, restaurantId);
                                    if (!hasServeur) initialiserServeur(db, restaurantId);

                                    Log.d(TAG, "Initialisation des rôles terminée pour le restaurant : " + restaurantId);
                                });

                    } else {
                        Log.e(TAG, "Champ restaurantId manquant dans l'utilisateur.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Erreur lors de la récupération du document utilisateur", e));
    }

    private static void initialiserManager(FirebaseFirestore db, String restaurantId) {
        Map<String, Object> data = new HashMap<>();
        data.put("nom", "Manager");
        data.put("canEditPermissions", false);
        data.put("permissions", PermissionTemplates.getManagerPermissions());

        db.collection("restaurants").document(restaurantId)
                .collection("roles").document("Manager").set(data);
    }

    private static void initialiserServeur(FirebaseFirestore db, String restaurantId) {
        Map<String, Object> data = new HashMap<>();
        data.put("nom", "Serveur");
        data.put("canEditPermissions", false);
        data.put("permissions", PermissionTemplates.getServeurPermissions());

        db.collection("restaurants").document(restaurantId)
                .collection("roles").document("Serveur").set(data);
    }
}
