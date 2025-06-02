package com.example.tablettegourmande.ui.GestionUtilisateurs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.tablettegourmande.MainActivity;
import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Utilisateur;
import com.example.tablettegourmande.services.ServiceBase;
import com.example.tablettegourmande.services.UserDataLoader;
import com.example.tablettegourmande.services.UserService;
import com.example.tablettegourmande.ui.home.HomeFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utils.CustomToast;

public class DialogChangerUtilisateur extends UserService {

    private MainActivity mainActivity;
    public DialogChangerUtilisateur(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

    }

    public void ouvrirDialog(Context context, boolean enabled) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_user, null);
        builder.setView(dialogView);

        EditText inputPassword = dialogView.findViewById(R.id.inputPassword);
        Spinner spinnerUsers = dialogView.findViewById(R.id.spinnerUtilisateurs);
        Button btnAnnuler = dialogView.findViewById(R.id.btnCancelDialog);
        Button btnConnexion = dialogView.findViewById(R.id.btnConnexionUtilisateur);
        TextView tvChangerUtilisateur = dialogView.findViewById(R.id.dialogTitleChangerUtilisateur);


        getAllUsers( true, new UserService.UsersCallback() {
            @Override
            public void onSuccess(List<Utilisateur> users) {
                // Étape 1 : Compter les doublons de noms

                List<String> nomsAffiches = new ArrayList<>();
                for (Utilisateur user : users) {
                    String nom = user.getNom();
                    String prenom = user.getPrenom(); // Assure-toi que getPrenom() existe

                    nomsAffiches.add(nom + " (" + prenom + ")");
                }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item, nomsAffiches);
                spinnerUsers.setAdapter(adapter);

                AlertDialog dialog = builder.create();

                btnAnnuler.setOnClickListener(v -> dialog.dismiss());

                if(!enabled){
                    tvChangerUtilisateur.setText("Se connecter...");
                    dialog.setCancelable(false);
                    btnAnnuler.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mainActivity.finish();

                        }
                    });
                }

                dialog.show();

                NavigationView navigationView = mainActivity.findViewById(R.id.nav_view);
                btnConnexion.setOnClickListener(view -> {
                    String motdepasse = inputPassword.getText().toString();
                    String nom = spinnerUsers.getSelectedItem().toString().split(" \\(")[0];
                    String prenom = spinnerUsers.getSelectedItem().toString().split(" \\(")[1].split("\\)")[0];

                    verifierUtilisateurParNomPrenomEtMotDePasse(getRestaurantId(), nom, prenom, motdepasse, new FirestoreCallback() {
                        @Override
                        public void onSuccess(DocumentSnapshot document) {
                            Log.d("LOGIN", "Utilisateur trouvé : " + document.getId());
                            UserDataLoader userDataLoader = new UserDataLoader();
                            userDataLoader.loadUserData(navigationView,context,nom,prenom);
                            CustomToast.show(context, "Connexion en tant que : "+nom+ " "+prenom, R.drawable.ic_success);
                            dialog.dismiss();
                            if (context instanceof AppCompatActivity) {
                                AppCompatActivity activity = (AppCompatActivity) context;
                                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.fragment_container, new HomeFragment()); // vérifie bien l’ID ici
                                fragmentTransaction.commit();

                            } else {
                                Log.e("LOGIN", "Le context n'est pas une AppCompatActivity !");
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("LOGIN", "Erreur : " + e.getMessage());
                            CustomToast.show(context, "La connexion en tant que : "+nom+ " "+prenom +  " a échoué.", R.drawable.ic_warning);
                        }
                    });
                });

            }

            @Override
            public void onFailure(Exception e) {
            }
        });



    }
    private String getRestaurantIdFromPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("restaurantId", null);
    }
}
