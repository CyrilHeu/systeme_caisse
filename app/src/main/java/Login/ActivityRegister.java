package Login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tablettegourmande.MainActivity;
import com.example.tablettegourmande.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import Utils.ActionBarUtils;

public class ActivityRegister extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private boolean isEmailVerified = false;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_activity_register);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        progressBar = findViewById(R.id.progressBar);

        Button registerButton = findViewById(R.id.registerButton);

        auth = FirebaseAuth.getInstance();

        // Action pour s'inscrire
        registerButton.setOnClickListener(v -> registerUser());

        // Configure la flèche de retour avec un titre
        ActionBarUtils.setupActionBarWithBackButton(this, "Inscription");
    }

    // Méthode pour gérer l'inscription
    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un email et un mot de passe.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Créer un utilisateur Firebase
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Envoyer un email de vérification et rediriger
                        sendVerificationEmail();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Erreur lors de l'inscription : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    // Méthode pour envoyer un email de vérification
    private void sendVerificationEmail() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE); // Arrêter le ProgressBar
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Email de vérification envoyé. Veuillez vérifier votre boîte mail.", Toast.LENGTH_LONG).show();
                            // Rediriger vers l'écran de connexion
                            Intent intent = new Intent(ActivityRegister.this, ActivityLogin.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Erreur lors de l'envoi de l'email : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }


    // Méthode pour vérifier périodiquement si l'email est validé
    private void startEmailVerificationCheck() {
        handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    user.reload()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (user.isEmailVerified()) {
                                        isEmailVerified = true;
                                        handler.removeCallbacksAndMessages(null); // Arrêter le timer
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(ActivityRegister.this, "Email vérifié. Vous pouvez maintenant vous connecter.", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(ActivityRegister.this, MainActivity.class));
                                        finish();
                                    } else {
                                        handler.postDelayed(this, 5000); // Vérifie toutes les 5 secondes
                                    }
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(ActivityRegister.this, "Erreur lors de la vérification de l'email.", Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        };
        handler.post(runnable);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Gère le clic sur la flèche de retour
        return ActionBarUtils.handleBackButton(this, item) || super.onOptionsItemSelected(item);
    }
}
