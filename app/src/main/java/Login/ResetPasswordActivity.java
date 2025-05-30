package Login;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tablettegourmande.R;
import com.google.firebase.auth.FirebaseAuth;

import Utils.ActionBarUtils;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_activity_reset_password);

        emailEditText = findViewById(R.id.email_to_resetPasswordEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        resetPasswordButton.setOnClickListener(v -> resetPassword());

        // Configure la flèche de retour avec un titre
        ActionBarUtils.setupActionBarWithBackButton(this, "Réinitialisation du mot de passe");

    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer votre email.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Email de réinitialisation envoyé. Vérifiez votre boîte mail.", Toast.LENGTH_LONG).show();
                        finish(); // Fermer l'activité après l'envoi
                    } else {
                        Toast.makeText(this, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Gère le clic sur la flèche de retour
        return ActionBarUtils.handleBackButton(this, item) || super.onOptionsItemSelected(item);
    }
}
