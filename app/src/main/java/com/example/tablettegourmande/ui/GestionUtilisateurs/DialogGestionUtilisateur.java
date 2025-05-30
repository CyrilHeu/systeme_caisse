package com.example.tablettegourmande.ui.GestionUtilisateurs;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tablettegourmande.MainActivity;
import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Utilisateur;
import com.example.tablettegourmande.services.UserDataLoader;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Utils.CustomToast;
import Utils.Normalize;

public class DialogGestionUtilisateur {
    public static void ouvrirDialog(@Nullable Utilisateur utilisateur, String restaurantId, Context context, boolean modif) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_utilisateur, null);
        builder.setView(dialogView);

        EditText inputNom = dialogView.findViewById(R.id.inputNomUtilisateur);
        EditText inputPrenom = dialogView.findViewById(R.id.inputPrenomUtilisateur);
        EditText inputEmail = dialogView.findViewById(R.id.inputEmailUtilisateur);
        EditText inputMotdepasse = dialogView.findViewById(R.id.inputMdp);
        EditText inputMotdepasseverif = dialogView.findViewById(R.id.inputMdpVerif);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRoleUtilisateur);
        Button btnValider = dialogView.findViewById(R.id.btnValiderUtilisateur);
        EditText inputNumero = dialogView.findViewById(R.id.inputNumeroUtilisateur);
        Button btnAnnuler = dialogView.findViewById(R.id.btnCancelUtilisateur);


        FirebaseFirestore.getInstance()
                .collection("restaurants")
                .document(restaurantId)
                .collection("roles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> roles = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String nom = doc.getString("nom");
                        if (nom != null) roles.add(nom);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                            android.R.layout.simple_spinner_dropdown_item, roles);
                    spinnerRole.setAdapter(adapter);

                    // Pré-sélectionner le rôle si modification
                    if (utilisateur != null && utilisateur.getRole() != null) {
                        int index = roles.indexOf(utilisateur.getRole());
                        if (index >= 0) spinnerRole.setSelection(index);
                    }
                });


        AlertDialog dialog = builder.create();

        FirebaseFirestore.getInstance()
                .collection("restaurants")
                .document(restaurantId)
                .collection("roles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> roles = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String nom = doc.getString("nom");
                        if (nom != null) roles.add(nom);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                            android.R.layout.simple_spinner_dropdown_item, roles);
                    spinnerRole.setAdapter(adapter);

                    // ➜ déplacer ici la pré-sélection du rôle si en modification
                    if (utilisateur != null) {
                        inputNom.setText(utilisateur.getNom());
                        inputPrenom.setText(utilisateur.getPrenom());
                        inputEmail.setText(utilisateur.getEmail());
                        inputNumero.setText(utilisateur.getNumero() != null ? utilisateur.getNumero() : "");

                        if (utilisateur.getRole().equals("Superviseur")) {
                            ArrayAdapter<String> singleAdapter = new ArrayAdapter<>(context,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    Collections.singletonList("Superviseur"));
                            spinnerRole.setAdapter(singleAdapter);
                            spinnerRole.setEnabled(false);
                        } else {

                            int index = roles.indexOf(utilisateur.getRole());
                            if (index >= 0) spinnerRole.setSelection(index);
                            spinnerRole.setEnabled(true);
                        }
                    }
                });

        btnAnnuler.setOnClickListener(v -> dialog.dismiss());


        btnValider.setOnClickListener(v -> {
            String nom = inputNom.getText().toString().trim();
            String prenom = inputPrenom.getText().toString().trim();
            String motDePasse = inputMotdepasse.getText().toString().trim();
            String confirmation = inputMotdepasseverif.getText().toString().trim();
            String role = spinnerRole.getSelectedItem().toString();
            String passwordRegex = "^(?=.*[A-Z]).{8,}$";

            if(nom.contains("(") || nom.contains(")") || prenom.contains("(") || prenom.contains(")")){
                CustomToast.show(context, "Erreur : nom ou prénom.", R.drawable.ic_error);
                return;
            }

            if (nom.isEmpty() || prenom.isEmpty() || (!modif && motDePasse.isEmpty())) {
                CustomToast.show(context, "Veuillez remplir tous les champs obligatoires.", R.drawable.ic_error);
                return;
            }

            // Si mot de passe fourni (création ou modif), faire les vérifications
            if (!motDePasse.isEmpty() || !confirmation.isEmpty()) {
                if (!motDePasse.equals(confirmation)) {
                    CustomToast.show(context, "Les mots de passe ne correspondent pas.", R.drawable.ic_error);
                    return;
                }
                if (!motDePasse.matches(passwordRegex)) {
                    CustomToast.show(context, "Le mot de passe doit contenir au moins 8 caractères et une majuscule.", R.drawable.ic_error);
                    return;
                }
            }
            if(!inputNumero.getText().toString().isEmpty()){
                if(inputNumero.getText().toString().length()<9){
                    CustomToast.show(context, "Le numéro saisie est invalide.", R.drawable.ic_error);
                    return;
                }
            }
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            CollectionReference usersRef = db.collection("restaurants")
                    .document(restaurantId)
                    .collection("users");

            usersRef
                    .whereEqualTo("nom", nom)
                    .whereEqualTo("prenom", prenom)
                    .get()
                    .addOnSuccessListener(userSnapshots -> {
                        boolean existeDeja = false;

                        for (DocumentSnapshot doc : userSnapshots) {
                            if (!modif || (utilisateur == null || !doc.getId().equals(utilisateur.getId()))) {
                                existeDeja = true;
                                break;
                            }
                        }

                        if (existeDeja) {
                            CustomToast.show(context, "Utilisateur déjà existant !", R.drawable.ic_error);
                            return;
                        }

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("nom", nom);
                        userData.put("prenom", prenom);
                        userData.put("role", role);

                        if(!inputEmail.getText().toString().isEmpty()){
                            userData.put("email", inputEmail.getText().toString());

                        }else{
                            userData.put("email", "");
                        }
                        if(!inputNumero.getText().toString().isEmpty()){
                            userData.put("numero", inputNumero.getText().toString());
                        }else{
                            userData.put("numero", "");
                        }
                        // Ajout du mot de passe uniquement :
                        // - en création
                        // - ou si modif avec champs remplis
                        if (!motDePasse.isEmpty()) {
                            userData.put("mot de passe", motDePasse);
                        }

                        if (modif && utilisateur != null) {
                            // Mise à jour
                            usersRef.document(utilisateur.getId())
                                    .update(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        CustomToast.show(context, "Utilisateur modifié avec succès !", R.drawable.ic_success);
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> Log.e("FIREBASE", "Erreur modification utilisateur", e));
                        } else {
                            // Création
                            usersRef.add(userData)
                                    .addOnSuccessListener(documentReference -> {
                                        CustomToast.show(context, "Utilisateur créé avec succès !", R.drawable.ic_success);
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> Log.e("FIREBASE", "Erreur création utilisateur", e));
                        }
                    })
                    .addOnFailureListener(e -> Log.e("FIREBASE", "Erreur vérification doublon", e));

            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;
                NavigationView navigationView = activity.findViewById(R.id.nav_view);

                UserDataLoader userDataLoader = new UserDataLoader();
                userDataLoader.loadUserData(navigationView, context, nom, prenom);
            } else {
                Log.e("UserDataLoader", "Le contexte n'est pas une activité compatible !");
            }
        });

        dialog.show();
    }

}
