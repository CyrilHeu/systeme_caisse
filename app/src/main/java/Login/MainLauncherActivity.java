package Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tablettegourmande.FirebaseManager;
import com.example.tablettegourmande.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainLauncherActivity extends AppCompatActivity {

    private static final boolean IS_DEV_MODE = true;
    private static final String DEBUG_EMAIL = "heurtauxcyril@gmail.com";
    private static final String DEBUG_PASSWORD = "test123";
    private static final String PREF_NAME = "FirebasePrefs";
    private static final String ENVIRONMENT_KEY = "environment";

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialisez Firebase avant toute autre action
        initializeFirebase();

        // Vérifiez si le mode développement est activé
        if (IS_DEV_MODE) {
            loginWithDebugCredentials();
        } else {
            // Si pas en mode dev, redirigez vers ActivityLogin
            navigateToLoginActivity();
        }
    }

    private void initializeFirebase() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String savedEnvironment = prefs.getString(ENVIRONMENT_KEY, "dev");

        try {
            FirebaseManager.initializeFirebase(this, savedEnvironment);
            Log.d("MainLauncher", "Firebase initialized for environment: " + savedEnvironment);
        } catch (Exception e) {
            Log.e("MainLauncher", "Error initializing Firebase: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void loginWithDebugCredentials() {
        auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(DEBUG_EMAIL, DEBUG_PASSWORD)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("MainLauncher", "Debug login successful for: " + DEBUG_EMAIL);
                        navigateToMainActivity();
                    } else {
                        Log.e("MainLauncher", "Debug login failed: " + task.getException().getMessage());
                        Toast.makeText(this, "Debug login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        navigateToLoginActivity();
                    }
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(MainLauncherActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(MainLauncherActivity.this, ActivityLogin.class);
        startActivity(intent);
        finish();
    }
}
