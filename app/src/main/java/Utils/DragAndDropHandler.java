package Utils;

import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.GridLayout;

import com.example.tablettegourmande.R;

import java.util.ArrayList;

public class DragAndDropHandler {

    public interface DragEventListener {
        void onDrop();
    }

    private final ArrayList<View> cardViews;
    private final GridLayout gridLayout;
    private DragEventListener dragEventListener;

    public DragAndDropHandler(ArrayList<View> cardViews, GridLayout gridLayout) {
        this.cardViews = cardViews;
        this.gridLayout = gridLayout;
    }

    public void setDragEventListener(DragEventListener listener) {
        this.dragEventListener = listener;
    }

    public void setupDragAndDrop() {
        gridLayout.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    if (event.getLocalState() instanceof View) {
                        View draggedView = (View) event.getLocalState();
                        startShakeAnimation(draggedView); // Commence l'animation "Shake"
                    }
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:
                    // Peut être utilisé pour des retours visuels, facultatif
                    return true;

                case DragEvent.ACTION_DROP:
                    if (event.getLocalState() instanceof View) {
                        View draggedView = (View) event.getLocalState();
                        stopShakeAnimation(draggedView);
                        updateOrderDuringDrag(draggedView, event.getX(), event.getY());

                        // Notifier HomeFragment pour sauvegarder
                        if (dragEventListener != null) {
                            dragEventListener.onDrop();
                        }
                    }
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    if (event.getLocalState() instanceof View) {
                        View draggedView = (View) event.getLocalState();
                        stopShakeAnimation(draggedView); // Arrête l'animation même si le drag est annulé
                    }
                    return true;

                default:
                    return false;
            }
        });

    }
    // Démarre l'animation "Shake" pour une vue
    private void startShakeAnimation(View view) {
        ObjectAnimator shakeAnimator = ObjectAnimator.ofFloat(view, "rotation", -2f, 2f);
        shakeAnimator.setDuration(100); // Durée d'un cycle
        shakeAnimator.setRepeatCount(ObjectAnimator.INFINITE); // Répéter infiniment
        shakeAnimator.setRepeatMode(ObjectAnimator.REVERSE); // Revenir à la position initiale
        shakeAnimator.start();

        // Stocker l'animation dans le tag de la vue pour pouvoir l'arrêter plus tard
        view.setTag(R.id.shake_animation, shakeAnimator);
    }

    // Arrête l'animation "Shake" pour une vue
    private void stopShakeAnimation(View view) {
        ObjectAnimator shakeAnimator = (ObjectAnimator) view.getTag(R.id.shake_animation);
        if (shakeAnimator != null) {
            shakeAnimator.cancel(); // Arrêter l'animation
            view.setRotation(0f); // Réinitialiser la rotation à 0
        }
    }
    private void updateOrderDuringDrag(View draggedView, float x, float y) {
        if (draggedView == null) {
            Log.e("DragAndDrop", "Dragged view is null");
            return;
        }

        // Trouver l'index cible à partir des coordonnées
        int targetIndex = getIndexFromCoordinates(x, y);

        if (targetIndex >= 0) {
            int draggedIndex = cardViews.indexOf(draggedView);

            // Si les indices sont valides et différents, réorganiser
            if (draggedIndex >= 0 && draggedIndex != targetIndex) {
                // Réorganise les vues dans la liste en mémoire
                cardViews.remove(draggedIndex);
                cardViews.add(targetIndex, draggedView);

                // Rafraîchir le layout
                refreshGridLayout();

                Log.d("DragAndDrop", "Dragged view moved from " + draggedIndex + " to " + targetIndex);

                // Notifier HomeFragment pour sauvegarder
                if (dragEventListener != null) {
                    dragEventListener.onDrop();
                }
            }
        }
    }

    private int getIndexFromCoordinates(float x, float y) {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (x >= child.getLeft() && x <= child.getRight() &&
                    y >= child.getTop() && y <= child.getBottom()) {
                return i;
            }
        }
        return -1; // Retourne -1 si aucune position valide n'est trouvée
    }

    private void refreshGridLayout() {
        gridLayout.removeAllViews(); // Vide toutes les vues actuelles
        for (View cardView : cardViews) {
            gridLayout.addView(cardView); // Ajoute les vues dans le nouvel ordre
        }
        gridLayout.invalidate(); // Force le rafraîchissement de l'interface
    }

}
