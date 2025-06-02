package Utils;

import android.content.Context;
import android.view.View;
import android.view.animation.ScaleAnimation;

import androidx.core.content.ContextCompat;

import com.example.tablettegourmande.R;

public class ButtonUtils {

    public static void addPressAnimation(View button) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    // Effet de réduction légère au clic
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    // Retour à la taille normale
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    break;
            }
            return false;
        });
    }
    public static int getColor(String couleur, Context context) {
        if (couleur == null || couleur.isEmpty() || couleur.equalsIgnoreCase("Défaut")) {
            return ContextCompat.getColor(context, R.color.category_default_color);
        }

        switch (couleur.toLowerCase()) {
            case "rouge": return ContextCompat.getColor(context, android.R.color.holo_red_dark);
            case "bleu": return ContextCompat.getColor(context, android.R.color.holo_blue_dark);
            case "vert": return ContextCompat.getColor(context, android.R.color.holo_green_dark);
            case "orange": return ContextCompat.getColor(context, android.R.color.holo_orange_dark);
            case "violet": return ContextCompat.getColor(context, android.R.color.holo_purple);
            default: return ContextCompat.getColor(context, R.color.category_default_color);
        }
    }
}
