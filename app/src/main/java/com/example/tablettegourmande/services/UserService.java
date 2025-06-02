package com.example.tablettegourmande.services;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.tablettegourmande.models.Restaurant;
import com.example.tablettegourmande.models.Utilisateur;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utils.CustomToast;

public class UserService extends ServiceBase {
    private String current_user_id = "";
    // Vérifie si l'initialisation a été terminée
    public void checkInitialSetup(InitialSetupCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            Boolean initialSetupCompleted = document.getBoolean("initialSetupCompleted");
                            if (initialSetupCompleted != null && initialSetupCompleted) {
                                callback.onCompleted(); // Si l'initialisation est terminée
                            } else {
                                callback.onNotCompleted(); // Si l'initialisation n'est pas terminée
                            }
                        } else {
                            callback.onError("Erreur lors de la vérification de l'initialisation.");
                        }
                    });
        } else {
            callback.onError("Utilisateur non connecté.");
        }
    }

    // Fonction pour vérifier l'existence d'un restaurant et récupérer son ID
    public void checkIfRestaurantExists(RestaurantExistenceCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            db.collection("restaurants")
                    .whereEqualTo("_ownerId", user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String restaurantId = task.getResult().getDocuments().get(0).getId();
                            callback.onRestaurantExists(restaurantId);
                        } else if (task.isSuccessful()) {
                            callback.onRestaurantDoesNotExist();
                        } else {
                            callback.onError("Erreur lors de la vérification du restaurant.");
                        }
                    });
        } else {
            callback.onError("Utilisateur non connecté.");
        }
    }

    // Sauvegarde des sous-collections (détails, contact, paramètres, etc.)
    private void saveSubCollections(String restaurantId, Restaurant restaurant, SaveRestaurantCallback callback) {
        // Sauvegarder locationInfo
        Map<String, Object> locationInfo = new HashMap<>();
        locationInfo.put("address", restaurant.getLocationInfo().getAddress());
        locationInfo.put("city", restaurant.getLocationInfo().getCity());
        locationInfo.put("postalCode", restaurant.getLocationInfo().getPostalCode());
        locationInfo.put("country", restaurant.getLocationInfo().getCountry());

        db.collection("restaurants").document(restaurantId).set(locationInfo)
                .addOnSuccessListener(aVoid -> Log.d("Setup", "LocationInfo sauvegardé avec succès"))
                .addOnFailureListener(e -> Log.e("Setup", "Erreur LocationInfo", e));

        // Sauvegarder contactInfo
        Map<String, Object> contactInfo = new HashMap<>();
        contactInfo.put("phone", restaurant.getContactInfo().getPhone());
        contactInfo.put("email", restaurant.getContactInfo().getEmail());
        contactInfo.put("notifyEmail", restaurant.getContactInfo().getNotifyEmail());

        db.collection("restaurants").document(restaurantId).set(contactInfo)
                .addOnSuccessListener(aVoid -> Log.d("Setup", "ContactInfo sauvegardé avec succès"))
                .addOnFailureListener(e -> Log.e("Setup", "Erreur ContactInfo", e));

        // Sauvegarder settings
        Map<String, Object> settings = new HashMap<>();
        settings.put("language", restaurant.getSettings().getLanguage());
        settings.put("timezone", restaurant.getSettings().getTimezone());
        settings.put("ticketMessage", restaurant.getSettings().getTicketMessage());
        settings.put("closedDays", restaurant.getSettings().getClosedDays());
        settings.put("acceptedPayments", restaurant.getSettings().getAcceptedPayments());
        settings.put("tvaIntracom", restaurant.getSettings().getTvaIntracom());
        settings.put("siret", restaurant.getSettings().getSiret());

        db.collection("restaurants").document(restaurantId).collection("settings")
                .document("global settings").set(settings)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Setup", "Settings sauvegardé avec succès");
                    callback.onSuccess(restaurantId);
                })
                .addOnFailureListener(e -> {
                    Log.e("Setup", "Erreur Settings", e);
                    callback.onError("Erreur lors de la sauvegarde des paramètres du restaurant.");
                });
    }

    public void updateUserInitialSetup(SaveUserCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .update("initialSetupCompleted", true)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        callback.onError("Erreur lors de la mise à jour de l'état d'initialisation.");
                    });
        } else {
            callback.onError("Utilisateur non connecté.");
        }
    }
    public void getCurrentUser(FirestoreCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            String userUID = user.getUid();

            // Charger l'ID du restaurant depuis le document utilisateur
            db.collection("users")
                    .document(userUID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            // Étape 1 : récupérer le champ "current_main_user"
                            DocumentReference restaurantRef = db.collection("restaurants").document(getRestaurantId());

                            restaurantRef.get()
                                    .addOnSuccessListener(restaurantSnapshot -> {
                                        if (restaurantSnapshot.exists()) {
                                            String currentUserId = restaurantSnapshot.getString("current_main_user");

                                            if (currentUserId != null) {
                                                // Étape 2 : aller chercher le document du user correspondant
                                                DocumentReference userRef = restaurantRef.collection("users").document(currentUserId);
                                                userRef.get()
                                                        .addOnSuccessListener(callback::onSuccess)
                                                        .addOnFailureListener(callback::onFailure);
                                            } else {
                                                callback.onFailure(new Exception("Champ 'current_main_user' absent ou nul"));
                                            }
                                        } else {
                                            callback.onFailure(new Exception("Document restaurant inexistant"));
                                        }
                                    })
                                    .addOnFailureListener(callback::onFailure);
                        }
                    });
        }
    }
    public void loadUser(String nom, String prenom, FirestoreCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            String userUID = user.getUid();

            // Charger l'ID du restaurant depuis le document utilisateur
            db.collection("users")
                    .document(userUID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            String restaurantId = getRestaurantId();

                            db.collection("restaurants")
                                    .document(getRestaurantId())
                                    .collection("users")
                                    .whereEqualTo("nom", nom)
                                    .whereEqualTo("prenom", prenom)
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        if (!querySnapshot.isEmpty()) {
                                            DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0); // Premier utilisateur trouvé
                                            String userId = userDoc.getId();

                                            // Mise à jour du champ "main_current_user" dans le document restaurant
                                            db.collection("restaurants")
                                                    .document(restaurantId)
                                                    .update("current_main_user", userId)
                                                    .addOnSuccessListener(aVoid -> {
                                                        callback.onSuccess(userDoc); // Optionnel : on peut renvoyer l'utilisateur
                                                    })
                                                    .addOnFailureListener(callback::onFailure);
                                        } else {
                                            callback.onFailure(new Exception("Aucun utilisateur trouvé avec ce nom et prénom"));
                                        }
                                    })
                                    .addOnFailureListener(callback::onFailure);
                        }
                    });
        }

    }

    public void getAllUsers(boolean exceptCurrent, UsersCallback callback) {
        FirebaseUser user = getCurrentUser();

        if (user != null) {
            String userUID = user.getUid();
            db.collection("users")
                    .document(userUID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot userDocument = task.getResult();
                            String restaurantID = userDocument.getString("restaurantId");
                            if(exceptCurrent==true){
                                getCurrentUser(new FirestoreCallback() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot document) {
                                        current_user_id = document.getId();
                                        Log.d("TAG123", "current_user_id"+current_user_id);
                                    }
                                    @Override
                                    public void onFailure(Exception e) {

                                    }
                                });
                            }
                            getCurrentUser(new FirestoreCallback() {
                                @Override
                                public void onSuccess(DocumentSnapshot document) {
                                    if (restaurantID != null) {
                                        db.collection("restaurants")
                                                .document(restaurantID)
                                                .collection("users")
                                                .get()
                                                .addOnSuccessListener(querySnapshot -> {
                                                    List<Utilisateur> users = new ArrayList<>();
                                                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                                        Utilisateur user = new Utilisateur();
                                                        user.setNom(doc.getString("nom"));
                                                        user.setPrenom(doc.getString("prenom"));
                                                        user.setMdp(doc.getString("mot de passe"));
                                                        Log.d("User MDP","doc.getString(\"mot de passe\")  "+doc.getString("mot de passe"));
                                                        user.setId(doc.getId());
                                                        if (user.getNom() != null ){
                                                            if(exceptCurrent==true){
                                                                if(user.getId().toString().equals(current_user_id.toString())==false){
                                                                    users.add(user);
                                                                }
                                                            }else{
                                                                users.add(user);
                                                            }

                                                        }
                                                    }
                                                    callback.onSuccess(users);
                                                })
                                                .addOnFailureListener(callback::onFailure);
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {

                                }
                            });
                        }
                    });
        }
    }

    public void verifierUtilisateurParNomPrenomEtMotDePasse(String restaurantId, String nom, String prenom, String motDePasse, FirestoreCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("restaurants")
                .document(restaurantId)
                .collection("users")
                .whereEqualTo("nom", nom)
                .whereEqualTo("prenom",prenom)
                .whereEqualTo("mot de passe", motDePasse)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0); // Premier match
                        callback.onSuccess(userDoc);
                    } else {
                        callback.onFailure(new Exception("Aucun utilisateur ne correspond"));

                    }
                })
                .addOnFailureListener(callback::onFailure);
    }


    public interface UsersCallback {
        void onSuccess(List<Utilisateur> users);
        void onFailure(Exception e);
    }
    // Interface pour la sauvegarde de l'utilisateur
    public interface SaveUserCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface RestaurantExistenceCallback {
        void onRestaurantExists(String restaurantId);
        void onRestaurantDoesNotExist();
        void onError(String errorMessage);
    }

    // Interface de callback pour les mises à jour du restaurant
    public interface UpdateRestaurantCallback {
        void onSuccess();  // Si la mise à jour est réussie
        void onFailure(String errorMessage);  // En cas d'erreur
    }

    // Interface pour la vérification de l'initialisation
    public interface InitialSetupCallback {
        void onCompleted(); // L'initialisation est terminée
        void onNotCompleted(); // L'initialisation n'est pas terminée
        void onError(String errorMessage); // Une erreur s'est produite
    }


    // Interfaces de callback
    public interface SaveRestaurantCallback {
        void onSuccess(String restaurantId);
        void onError(String errorMessage);
    }

}
