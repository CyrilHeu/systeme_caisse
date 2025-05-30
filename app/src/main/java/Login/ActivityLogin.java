package Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tablettegourmande.FirebaseManager;
import com.example.tablettegourmande.MainActivity;
import com.example.tablettegourmande.R;
import com.example.tablettegourmande.services.UserService;
import com.example.tablettegourmande.ui.setup.InitialSetupActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ActivityLogin extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private boolean isDebugMode = false;
    private Switch environmentSwitch;
    private TextView environmentText;
    private static final String PREF_NAME = "FirebasePrefs";
    private static final String ENVIRONMENT_KEY = "environment";
    private CheckBox rememberMeCheckBox;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_REMEMBER_ME = "rememberMe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseApp.getApps(this).isEmpty()) {
            Log.d("Firebase issues","Firebase is NOT initialized.");
            try {
                FirebaseManager.initializeFirebase(this, "dev");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            Log.d("Firebase issues","Firebase is already initialized.");
        }

        setContentView(R.layout.auth_activity_login);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        progressBar = findViewById(R.id.progressBar);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        // Action pour se connecter
        loginButton.setOnClickListener(v -> loginUser());

        // Redirection vers l'écran d'inscription
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityLogin.this, ActivityRegister.class);
            startActivity(intent);
        });

        TextView forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityLogin.this, ResetPasswordActivity.class);
            startActivity(intent);
        });

        // Récupérez l'environnement sauvegardé, par défaut "dev"
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String savedEnvironment = prefs.getString(ENVIRONMENT_KEY, "dev");

        // Initialisez Firebase avec l'environnement sauvegardé
        try {
            FirebaseManager.initializeFirebase(this, savedEnvironment);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        environmentSwitch = findViewById(R.id.environmentSwitch);
        environmentText = findViewById(R.id.environmentText);

        // Initialisez Firebase avec l'environnement sauvegardé
        try {
            FirebaseManager.initializeFirebase(this, savedEnvironment);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Configurez le SwitchButton et le TextView en fonction de l'environnement actuel
        environmentSwitch.setChecked(savedEnvironment.equals("prod"));
        updateEnvironmentText(savedEnvironment);

        // Ajoutez un listener pour gérer le basculement
        environmentSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String environment = isChecked ? "prod" : "dev";

            // Sauvegardez l'environnement sélectionné
            prefs.edit().putString(ENVIRONMENT_KEY, environment).apply();

            // Réinitialisez Firebase avec le nouvel environnement
            try {
                FirebaseManager.initializeFirebase(this, environment);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Mettez à jour le texte de l'environnement
            updateEnvironmentText(environment);
        });

        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);

        // Récupérer les préférences enregistrées
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = preferences.getBoolean(PREF_REMEMBER_ME, false);
        if (rememberMe) {
            String savedEmail = preferences.getString(PREF_EMAIL, "");
            String savedPassword = preferences.getString(PREF_PASSWORD, "");
            emailEditText.setText(savedEmail);
            passwordEditText.setText(savedPassword);
            rememberMeCheckBox.setChecked(true);
        }

    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer vos identifiants.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Sauvegarder les identifiants si la CheckBox est cochée
                            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                            if (rememberMeCheckBox.isChecked()) {
                                editor.putString(PREF_EMAIL, email);
                                editor.putString(PREF_PASSWORD, password);
                                editor.putBoolean(PREF_REMEMBER_ME, true);
                            } else {
                                editor.clear();
                            }
                            editor.apply();

                            // Gestion de la première connexion
                            handleFirstLogin(user);
                        } else {
                            Toast.makeText(this, "Veuillez vérifier votre email avant de vous connecter.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Erreur de connexion : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleFirstLogin(FirebaseUser user) {
        if (user == null) {
            Toast.makeText(this, "Aucun utilisateur connecté.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();
        String email = user.getEmail();

        // Vérifie si le document existe dans Firestore
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {
                            // Crée un nouvel utilisateur si le document n'existe pas
                            createUserDocument(db, userId, email);
                        } else {
                            // Met à jour la dernière connexion
                            updateLastLogin(db, userId);
                        }
                    } else {
                        Log.e("FirestoreError", "Erreur Firestore : ", task.getException());
                    }
                });
    }

    private void createUserDocument(FirebaseFirestore db, String userId, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("createdAt", FieldValue.serverTimestamp());
        userData.put("lastLogin", FieldValue.serverTimestamp());

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Redirigez vers l'activité principale après création
                    navigateToMainActivity();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Erreur lors de la création de l'utilisateur : ", e);
                    Toast.makeText(this, "Erreur lors de la création du compte utilisateur : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateLastLogin(FirebaseFirestore db, String userId) {
        db.collection("users").document(userId)
                .update("lastLogin", FieldValue.serverTimestamp())
                .addOnSuccessListener(aVoid -> {
                    navigateToMainActivity();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Erreur lors de la mise à jour de la dernière connexion : ", e);
                    Toast.makeText(this, "Erreur lors de la mise à jour : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private void navigateToMainActivity() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Chargement...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        UserService userService = new UserService();
        userService.checkInitialSetup(new UserService.InitialSetupCallback() {
            @Override
            public void onCompleted() {
                progressDialog.dismiss();
                // Lancer MainActivity avec HomeFragment chargé
                Intent intent = new Intent(ActivityLogin.this, MainActivity.class);
                intent.putExtra("loadFragment", "home");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onNotCompleted() {
                progressDialog.dismiss();
                // Rediriger vers InitialSetupActivity
                Intent intent = new Intent(ActivityLogin.this, InitialSetupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                progressDialog.dismiss();
                Toast.makeText(ActivityLogin.this, "Erreur : " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Méthode pour mettre à jour le texte de l'environnement
    private void updateEnvironmentText(String environment) {
        String environmentTextValue = environment.equals("prod") ? "Environment: Production" : "Environment: Debug";
        environmentText.setText(environmentTextValue);
    }
}
