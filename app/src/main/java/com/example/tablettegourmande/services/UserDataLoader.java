package com.example.tablettegourmande.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.example.tablettegourmande.MainActivity;
import com.example.tablettegourmande.R;
import com.example.tablettegourmande.ui.GestionUtilisateurs.DialogChangerUtilisateur;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;

public class UserDataLoader extends ServiceBase {

    private String restaurantId;
    /**
     * Charge les données utilisateur (restaurantID et email) dans l'en-tête de navigation.
     */
    public void loadInitialUserData(NavigationView navigationView, Context context) {

        // Récupérez l'en-tête de navigation
        View headerView = navigationView.getHeaderView(0);

        // Titre : restaurantID
        TextView navHeaderTitle = headerView.findViewById(R.id.nav_header_title);
        loadRestaurantName(context, new RestaurantNameCallback() {
            @Override
            public void onSuccess(String restaurantName) {
                navHeaderTitle.setText("Restaurant: " + restaurantName);

            }

            @Override
            public void onFailure(String errorMessage) {
                navHeaderTitle.setText("Restaurant: " + errorMessage);
            }
        });
        UserService userService = new UserService();
        userService.getCurrentUser(new FirestoreCallback() {

            public void onSuccess(DocumentSnapshot document) {
                TextView navHeaderSubtitle = headerView.findViewById(R.id.nav_header_subtitle);
                TextView navHeaderSubtitle2 = headerView.findViewById(R.id.nav_header_subtitle2);
                if(!document.getId().toString().equals("0")){
                    String userEmail = document.get("email").toString();
                    String nom = document.get("nom").toString();
                    String prenom = document.get("prenom").toString();
                    String role = document.get("role").toString();

                    String subtitle = "";
                    subtitle = prenom +" "+ nom;
                    String subtitle2 = role+"\n"+userEmail;
                    navHeaderSubtitle.setText(subtitle);
                    navHeaderSubtitle.setGravity(Gravity.CENTER);
                    navHeaderSubtitle2.setText(subtitle2);
                    navHeaderSubtitle2.setGravity(Gravity.CENTER);
                }else{
                    DialogChangerUtilisateur dialogChangerUtilisateur = new DialogChangerUtilisateur((MainActivity) context);
                    dialogChangerUtilisateur.ouvrirDialog(context, false);
                }


            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }
    public void loadUserData(NavigationView navigationView, Context context, String nom, String prenom) {

        // Récupérez l'en-tête de navigation
        View headerView = navigationView.getHeaderView(0);

        // Titre : restaurantID
        TextView navHeaderTitle = headerView.findViewById(R.id.nav_header_title);
        loadRestaurantName(context, new RestaurantNameCallback() {
            @Override
            public void onSuccess(String restaurantName) {
                navHeaderTitle.setText("Restaurant: " + restaurantName);

            }

            @Override
            public void onFailure(String errorMessage) {
                navHeaderTitle.setText("Restaurant: " + errorMessage);
            }
        });
        UserService userService = new UserService();
        userService.loadUser(nom, prenom, new FirestoreCallback() {

            public void onSuccess(DocumentSnapshot document) {
                TextView navHeaderSubtitle = headerView.findViewById(R.id.nav_header_subtitle);
                TextView navHeaderSubtitle2 = headerView.findViewById(R.id.nav_header_subtitle2);
                String userEmail = document.get("email").toString();
                String nom = document.get("nom").toString();
                String prenom = document.get("prenom").toString();
                String role = document.get("role").toString();

                String subtitle = "";
                subtitle = prenom +" "+ nom;
                String subtitle2 = role+"\n"+userEmail;
                navHeaderSubtitle.setText(subtitle);
                navHeaderSubtitle.setGravity(Gravity.CENTER);
                navHeaderSubtitle2.setText(subtitle2);
                navHeaderSubtitle2.setGravity(Gravity.CENTER);

            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    public void deconnexionUtilisateur(UserUpdateCallback callback) {
        //Verifier si des tables sont ouvertes avec cette utilisateur et envoyer un message d'alerte si c'est le cas

        db.collection("restaurants")
                .document(getRestaurantId())
                .update("current_main_user", "0")
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("UserService", "Erreur lors de la réinitialisation du champ.", e);
                    callback.onFailure(e);
                });
    }

    public interface UserUpdateCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

}
