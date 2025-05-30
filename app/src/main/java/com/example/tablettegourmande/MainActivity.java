package com.example.tablettegourmande;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

import com.example.tablettegourmande.services.initialisationRoles.InitialisationRoles;
import com.example.tablettegourmande.ui.GestionUtilisateurs.DialogChangerUtilisateur;
import com.example.tablettegourmande.ui.home.HomeFragment;
import com.example.tablettegourmande.ui.settings.SettingsFragment;
import com.google.android.material.navigation.NavigationView;
import com.example.tablettegourmande.services.UserDataLoader;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tablettegourmande.services.Checking.*;


public class MainActivity extends AppCompatActivity {

    private NetworkReceiver networkReceiver;
    private ProgressDialog progressDialog;
    private TimeRealTimeChecker timeChecker;
    private UserDataLoader userDataLoader;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Configure la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configure le DrawerLayout
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Configure la NavigationView
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            } else if (item.getItemId() == R.id.nav_change_user){
                DialogChangerUtilisateur dialogChangerUtilisateur = new DialogChangerUtilisateur(this);
                dialogChangerUtilisateur.ouvrirDialog(this, true);


            } else if (item.getItemId() == R.id.nav_logout) {
                // Gère la déconnexion
                new AlertDialog.Builder(this)
                        .setTitle("Déconnexion")
                        .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
                        .setPositiveButton("Oui", (dialog, which) -> {

                            userDataLoader.deconnexionUtilisateur(new UserDataLoader.UserUpdateCallback() {
                                @Override
                                public void onSuccess() {
                                    Log.d("UserService", "Champ current_main_user remis à zéro.");
                                    finish();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.d("UserService", "Echec : Champ current_main_user remise à zéro.");
                                }
                            });
                        })
                        .setNegativeButton("Non", (dialog, which) -> {
                            dialog.dismiss(); // Ferme simplement le dialog
                        })
                        .show();


                return true;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        // Modifier la teinte des icônes
        navigationView.setItemIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));

        // Charge le fragment par défaut
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        drawer.closeDrawer(GravityCompat.START);

        networkReceiver = new NetworkReceiver(this);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
        // Initialiser la vérification en temps réel
        timeChecker = new TimeRealTimeChecker(this);
        timeChecker.startChecking();
        Log.d("BuildConfig", "BuildConfig :" + BuildConfig.APPLICATION_ID);

        // Initialisez UserDataLoader
        userDataLoader = new UserDataLoader();
        // Récupérez le NavigationView
        navigationView = findViewById(R.id.nav_view);
        // Chargez les données utilisateur dans l'en-tête
        userDataLoader.loadInitialUserData(navigationView, this);

        InitialisationRoles initRoles = new InitialisationRoles();
        initRoles.lancerInitialisation();


    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            new AlertDialog.Builder(this)
                    .setTitle("Quitter l'application")
                    .setMessage("Voulez-vous vraiment quitter l'application ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        // Quitter l'application
                        finishAffinity(); // Termine toutes les activités et ferme l'application
                    })
                    .setNegativeButton("Non", null)
                    .show();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // Ouvrir le fragment des paramètres
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SettingsFragment())
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkReceiver); // Désenregistrement du BroadcastReceiver
        if (timeChecker != null) {
            timeChecker.stopChecking();
        }
    }


}