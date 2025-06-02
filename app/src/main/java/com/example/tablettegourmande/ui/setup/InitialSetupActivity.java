package com.example.tablettegourmande.ui.setup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tablettegourmande.MainActivity;
import com.example.tablettegourmande.R;
import com.example.tablettegourmande.adapters.SetupPagerAdapter;
import com.example.tablettegourmande.models.Restaurant;
import com.example.tablettegourmande.models.Utilisateur;
import com.example.tablettegourmande.services.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitialSetupActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Restaurant restaurantData = new Restaurant();
    private Utilisateur userData = new Utilisateur();
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_activity_initial_setup);

        viewPager = findViewById(R.id.viewPager);
        SetupPagerAdapter adapter = new SetupPagerAdapter(this);
        viewPager.setAdapter(adapter);

        userService = new UserService();
    }

    // Naviguer vers la page suivante
    public void goToNextPage() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < 2) { // On a 3 fragments au total
            viewPager.setCurrentItem(currentItem + 1, true);
        }
    }
    public void saveSetup() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Enregistrement en cours...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Générer un ID unique pour le restaurant
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Créer une carte avec les données du restaurant
        Map<String, Object> restaurantMap = new HashMap<>();
        restaurantMap.put("_name", restaurantData.getName());
        restaurantMap.put("_ownerId", restaurantData.getOwnerId());
        restaurantMap.put("settings", restaurantData.getSettings());
        restaurantMap.put("contactInfo", restaurantData.getContactInfo());
        restaurantMap.put("locationInfo", restaurantData.getLocationInfo());

        // Sauvegarder le restaurant dans Firestore
        userService.checkIfRestaurantExists(new UserService.RestaurantExistenceCallback() {
            @Override
            public void onRestaurantExists(String restaurantId_) {
                DocumentReference newRestaurantRef1 = db.collection("restaurants").document(restaurantId_);
                newRestaurantRef1.set(restaurantMap, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Setup1", "Restaurant data saved");

                            // Une fois le restaurant sauvegardé, récupérer l'ID du restaurant et sauvegarder l'utilisateur
                            String restaurantId = restaurantId_;
                            saveUserData(restaurantId, progressDialog);  // Passer l'ID du restaurant à saveUserData

                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(InitialSetupActivity.this, "Erreur lors de l'enregistrement du restaurant", Toast.LENGTH_LONG).show();
                        });
            }

            @Override
            public void onRestaurantDoesNotExist() {
                DocumentReference newRestaurantRef = db.collection("restaurants").document();
                restaurantMap.put("_createdAt", restaurantData.getCreatedAt());
                newRestaurantRef.set(restaurantMap)
                        .addOnSuccessListener(aVoid -> {
                            // Une fois le restaurant sauvegardé, récupérer l'ID du restaurant et sauvegarder l'utilisateur
                            String restaurantId = newRestaurantRef.getId();
                            saveUserData(restaurantId, progressDialog);  // Passer l'ID du restaurant à saveUserData

                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(InitialSetupActivity.this, "Erreur lors de l'enregistrement du restaurant", Toast.LENGTH_LONG).show();
                        });
            }

            @Override
            public void onError(String errorMessage) {

            }
        });
    }

    private void saveUserData(String restaurantId, ProgressDialog progressDialog) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Créer une carte avec les données de l'utilisateur
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", "1");
        userMap.put("nom", userData.getNom());
        userMap.put("prenom", userData.getPrenom());
        userMap.put("email", userData.getEmail());
        userMap.put("mot de passe", "");
        userMap.put("role", "Superviseur");

        // Sauvegarder l'utilisateur dans Firestore sous le restaurantId
        db.collection("restaurants")
                .document(restaurantId)  // Utilisation de restaurantId dynamique
                .collection("users")
                .document("1")  // ID de l'administrateur
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Setup", "User data saved");

                    // Appel pour mettre à jour l'initialSetupCompleted
                    userService.updateUserInitialSetup(new UserService.SaveUserCallback() {
                        @Override
                        public void onSuccess() {
                            progressDialog.dismiss();
                            Map<String, Object> userFirebase = new HashMap<>();
                            userFirebase.put("restaurantId", restaurantId);

                            db.collection("users").document(FirebaseAuth.getInstance().getUid())
                                    .update(userFirebase)
                                    .addOnSuccessListener(aVoid -> {
                                        // Redirigez vers l'activité principale après création
                                        db.collection("restaurants")
                                                .document(restaurantId)
                                                .update("current_main_user", "1")
                                                .addOnSuccessListener(aVoid1 -> {
                                                    navigateToMainActivity();
                                                })
                                                .addOnFailureListener(e -> {
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FirestoreError", "Erreur lors de l'ajout du restaurant a l'utilisateur firebase", e);
                                    });

                        }

                        @Override
                        public void onError(String errorMessage) {
                            progressDialog.dismiss();
                            Toast.makeText(InitialSetupActivity.this, "Erreur : " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(InitialSetupActivity.this, "Erreur lors de l'enregistrement des données utilisateur", Toast.LENGTH_LONG).show();
                });

    }

    private void navigateToMainActivity() {
        Toast.makeText(InitialSetupActivity.this, "Configuration terminée avec succès !", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(InitialSetupActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToMainActivity(ProgressDialog progressDialog) {
        Toast.makeText(InitialSetupActivity.this, "Configuration terminée avec succès !", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(InitialSetupActivity.this, MainActivity.class);
        startActivity(intent);
        progressDialog.dismiss();
        finish();
    }

    public void updateRestaurantData(String key, String value) {
        switch (key) {
            case "name":
                restaurantData.setName(value);  // Mettre à jour le nom du restaurant
                break;
            case "address":
                restaurantData.getLocationInfo().setAddress(value);  // Adresse du restaurant
                break;
            case "country":
                restaurantData.getLocationInfo().setCountry(value);  // Pays du restaurant
                break;
            case "city":
                restaurantData.getLocationInfo().setCity(value);  // Ville du restaurant
                break;
            case "postalCode":
                restaurantData.getLocationInfo().setPostalCode(value);  // Code postal du restaurant
                break;
            case "phone":
                restaurantData.getContactInfo().setPhone(value);  // Numéro de téléphone
                break;
            case "email":
                restaurantData.getContactInfo().setEmail(value);  // Email du restaurant
                break;
            case "siret":
                restaurantData.getSettings().setSiret(value);  // SIRET du restaurant
                break;
            case "tvaIntracom":
                restaurantData.getSettings().setTvaIntracom(value);  // TVA intracommunautaire
                break;
            case "language":
                restaurantData.getSettings().setLanguage(value);  // Langue du restaurant
                break;
            case "timezone":
                restaurantData.getSettings().setTimezone(value);  // Fuseau horaire du restaurant
                break;
            case "ticketMessage":
                restaurantData.getSettings().setTicketMessage(value);  // Message personnalisé sur le ticket
                break;
            case "ownerId":
                restaurantData.setOwnerId(value);
                break;

            // Ajoutez d'autres cas si nécessaire pour d'autres champs
        }
    }
    public void updateRestaurantData(String key, List<String> value) {
        switch (key) {
            case "closedDays":
                restaurantData.getSettings().setClosedDays(value);
                break;
            case "acceptedPayments":
                restaurantData.getSettings().setAcceptedPayments(value);
                break;
        }
    }
    public void updateDataUser(String key, String value) {
        switch (key) {
            case "nom":
                userData.setNom(value);
                break;
            case "prenom":
                userData.setPrenom(value);
                break;
            case "email":
                userData.setEmail(value);
                break;
        }
    }


    public void updateRestaurantData(String key, boolean checked) {
        switch (key) {
            case "emailNotify":
                restaurantData.getContactInfo().setNotifyEmail(checked);
                break;

        }
    }

    public void updateRestaurantData(String key, FieldValue fieldValue) {
        switch (key) {
            case "createdAt":
                restaurantData.setCreatedAt(fieldValue);
                break;

        }
    }
}
