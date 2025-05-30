package com.example.tablettegourmande.permissions;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class PermissionUtils {

    public interface PermissionCallback {
        void onResult(boolean authorized);
    }

    public static void hasPermission(String permission, PermissionCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null || permission == null) {
            Log.e("PermissionUtils", "Utilisateur non connecté ou permission nulle");
            callback.onResult(false);
            return;
        }

        // Étape 1 : Récupérer restaurantId depuis collection principale 'users'
        db.collection("users").document(uid).get()
                .addOnSuccessListener(userMetaDoc -> {
                    if (!userMetaDoc.exists()) {
                        Log.e("PermissionUtils", "Utilisateur introuvable dans la collection principale 'users'");
                        callback.onResult(false);
                        return;
                    }

                    String restaurantId = userMetaDoc.getString("restaurantId");
                    if (restaurantId == null) {
                        Log.e("PermissionUtils", "restaurantId manquant dans le document utilisateur");
                        callback.onResult(false);
                        return;
                    }

                    // Étape 2 : Lire le current_main_user dans le restaurant
                    db.collection("restaurants").document(restaurantId).get()
                            .addOnSuccessListener(restaurantDoc -> {
                                String mainUserId = restaurantDoc.getString("current_main_user");
                                if (mainUserId == null) {
                                    Log.e("PermissionUtils", "current_main_user manquant dans le document restaurant");
                                    callback.onResult(false);
                                    return;
                                }

                                // Étape 3 : Récupérer le rôle du current_main_user
                                db.collection("restaurants").document(restaurantId)
                                        .collection("users").document(mainUserId)
                                        .get()
                                        .addOnSuccessListener(mainUserDoc -> {
                                            String role = mainUserDoc.getString("role");
                                            if (role == null) {
                                                Log.e("PermissionUtils", "Rôle manquant pour l'utilisateur actif");
                                                callback.onResult(false);
                                                return;
                                            }

                                            // ✅ Superviseur = tous les droits
                                            if (role.trim().equalsIgnoreCase("superviseur")) {
                                                Log.d("PermissionUtils", "Rôle superviseur détecté : autorisation automatique.");
                                                callback.onResult(true);
                                                return;
                                            }

                                            // Étape 4 : Vérifier la permission dans le rôle
                                            db.collection("restaurants").document(restaurantId)
                                                    .collection("roles").document(role.trim())
                                                    .get()
                                                    .addOnSuccessListener(roleDoc -> {
                                                        Map<String, Object> roleData = roleDoc.getData();
                                                        Map<String, Object> permissions = null;

                                                        if (roleData != null && roleData.containsKey("permissions")) {
                                                            Object raw = roleData.get("permissions");
                                                            if (raw instanceof Map) {
                                                                permissions = (Map<String, Object>) raw;
                                                            }
                                                        }

                                                        if (permissions != null) {
                                                            boolean allowed = false;

                                                            for (Map.Entry<String, Object> entry : permissions.entrySet()) {
                                                                if (entry.getKey().trim().equalsIgnoreCase(permission.trim())) {
                                                                    allowed = Boolean.TRUE.equals(entry.getValue());
                                                                    Log.d("PermissionUtils", "Permission trouvée : " + entry.getKey() + " => " + allowed);
                                                                    break;
                                                                }
                                                            }

                                                            if (!allowed) {
                                                                Log.w("PermissionUtils", "Permission '" + permission + "' non trouvée ou désactivée. Clés disponibles :");
                                                                for (String key : permissions.keySet()) {
                                                                    Log.d("PermissionUtils", "- " + key);
                                                                }
                                                            }

                                                            callback.onResult(allowed);
                                                        } else {
                                                            Log.e("PermissionUtils", "Aucune permission trouvée pour le rôle '" + role + "'");
                                                            callback.onResult(false);
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("PermissionUtils", "Erreur récupération rôle : " + e.getMessage());
                                                        callback.onResult(false);
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("PermissionUtils", "Erreur récupération utilisateur actif : " + e.getMessage());
                                            callback.onResult(false);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("PermissionUtils", "Erreur récupération document restaurant : " + e.getMessage());
                                callback.onResult(false);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("PermissionUtils", "Erreur récupération document utilisateur global : " + e.getMessage());
                    callback.onResult(false);
                });
    }



}
