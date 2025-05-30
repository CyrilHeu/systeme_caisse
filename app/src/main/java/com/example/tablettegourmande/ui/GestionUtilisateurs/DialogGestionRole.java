package com.example.tablettegourmande.ui.GestionUtilisateurs;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Role;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import Utils.CustomToast;
import Utils.Normalize;

public class DialogGestionRole {

    public static void ouvrirDialog(@Nullable Role role, String restaurantId, Context context, boolean creation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_role, null);
        builder.setView(view);

        String ancienNomRole = (role != null) ? role.getNom() : null;

        EditText inputNom = view.findViewById(R.id.inputNomRole);
        Button btnAnnuler = view.findViewById(R.id.btnAnnulerRole);
        Button btnValider = view.findViewById(R.id.btnValiderRole);

        AlertDialog dialog = builder.create();

        if (role != null) {
            inputNom.setText(role.getNom());
        }

        btnValider.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String nom = Normalize.normalize(inputNom.getText().toString().trim());
            if(creation==true){
                db.collection("restaurants")
                        .document(restaurantId)
                        .collection("roles")  // ← attention à bien utiliser "roles" (avec un 's') si c’est ta convention
                        .whereEqualTo("nom", nom)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                CustomToast.show(context, "Nom de rôle déjà existant", R.drawable.ic_error);
                            } else {
                                if (nom.isEmpty()) {
                                    CustomToast.show(context,"Nom requis",R.drawable.ic_error);
                                    return;
                                }

                                Map<String, Object> data = new HashMap<>();
                                data.put("nom", nom);
                                data.put("canEditPermissions", false);

                                if (role == null) {
                                    db.collection("restaurants")
                                            .document(restaurantId)
                                            .collection("roles")
                                            .document(nom).set(data);
                                            CustomToast.show(context, "Rôle créé avec succès", R.drawable.ic_success);
                                } else{

                                    CustomToast.show(context, "Rôle déjà existant", R.drawable.ic_error);
                                }
                                dialog.dismiss();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Erreur", "Erreur lors de la vérification du rôle : " + e.getMessage());
                        });

            }else{
                if (nom.isEmpty()) {
                    CustomToast.show(context,"Nom requis",R.drawable.ic_error);
                    return;
                }

                Map<String, Object> data = new HashMap<>();
                data.put("nom", nom);

                if (role == null) {
                    db.collection("restaurants")
                            .document(restaurantId)
                            .collection("roles")
                            .document(nom).set(data);
                } else {


                    db.collection("restaurants")
                            .document(restaurantId)
                            .collection("roles")
                            .whereEqualTo("nom", nom)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    CustomToast.show(context, "Nom de rôle déjà existant", R.drawable.ic_error);
                                }else{
                                    db.collection("restaurants")
                                            .document(restaurantId)
                                            .collection("roles")
                                            .document(role.getId())
                                            .update(data);
                                    if (ancienNomRole != null && !ancienNomRole.equals(nom)) {
                                        FirebaseFirestore.getInstance()
                                                .collection("restaurants")
                                                .document(restaurantId)
                                                .collection("users")
                                                .whereEqualTo("role", ancienNomRole)
                                                .get()
                                                .addOnSuccessListener(querySnapshot -> {
                                                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                                        doc.getReference().update("role", nom);
                                                    }
                                                });
                                    }
                                    CustomToast.show(context, "Rôle modifié avec succès", R.drawable.ic_success);
                                    dialog.dismiss();
                                }

                            })
                            .addOnFailureListener(e -> {
                                Log.e("Erreur", "Erreur lors de la vérification du rôle : " + e.getMessage());
                            });

                }


            }
        });

        btnAnnuler.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}

