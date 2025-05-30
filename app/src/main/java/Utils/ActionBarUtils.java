package Utils;

import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ActionBarUtils {

    /**
     * Configure une flèche de retour avec un titre dans l'Action Bar.
     *
     * @param activity L'activité actuelle.
     * @param title    Le titre à afficher dans l'Action Bar.
     */
    public static void setupActionBarWithBackButton(@NonNull AppCompatActivity activity, @NonNull String title) {
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Affiche la flèche
            activity.getSupportActionBar().setTitle(title); // Définit le titre de l'Action Bar
        }
    }

    /**
     * Gère le clic sur la flèche de retour pour revenir à l'activité précédente.
     *
     * @param activity   L'activité actuelle.
     * @param item       L'élément du menu cliqué.
     * @return           Vrai si l'élément cliqué est la flèche de retour, sinon false.
     */
    public static boolean handleBackButton(@NonNull Activity activity, @NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            activity.onBackPressed(); // Appelle la méthode de retour arrière
            return true;
        }
        return false;
    }

    public static void addPressAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    // Effet d'appui (réduction légère)
                    animateScale(v, 0.85f);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    // Retour à la taille normale
                    animateScale(v, 1f);
                    break;
            }
            return false;
        });
    }

    private static void animateScale(View view, float scale) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1f, scale, 1f, scale,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(100);
        scaleAnimation.setFillAfter(true);
        view.startAnimation(scaleAnimation);
    }


}
