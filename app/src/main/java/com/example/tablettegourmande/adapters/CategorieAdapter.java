package com.example.tablettegourmande.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Categorie;
import com.example.tablettegourmande.services.CategorieService;
import com.example.tablettegourmande.ui.gestion.ColorSpinnerAdapter;
import com.example.tablettegourmande.ui.gestion.PageGestionProduit;

import java.util.List;

import Utils.CustomToast;
import Utils.Normalize;

public class CategorieAdapter extends RecyclerView.Adapter<CategorieAdapter.CategorieViewHolder> {

    private List<Categorie> categories;
    private Context context;
    private final CategorieService categorieService;
    private final OnCategorieActionListener listener;

    public CategorieAdapter(Context context, List<Categorie> categories, CategorieService categorieService, OnCategorieActionListener listener) {
        this.context = context;
        this.categories = categories;
        this.categorieService = categorieService;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategorieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_categorie, parent, false);

        return new CategorieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategorieViewHolder holder, int position) {
        Categorie categorie = categories.get(position); // ✅ Récupère l'objet complet
        holder.nomCategorie.setText(categorie.getNom());

        holder.viewColor.setBackgroundColor(getColorFromCategorie(categorie.getCouleur()));

        holder.btnModifier.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            LayoutInflater inflater = LayoutInflater.from(v.getContext());
            View dialogView = inflater.inflate(R.layout.dialog_layout_categorie, null);
            builder.setView(dialogView);

            TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
            EditText editTextCategory = dialogView.findViewById(R.id.editTextCategory);
            Spinner spinnerColor = dialogView.findViewById(R.id.spinnerColor);
            Button btnCancel = dialogView.findViewById(R.id.btnCancel);
            Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

            dialogTitle.setText("Modifier la catégorie");
            editTextCategory.setText(categorie.getNom());
            String ancienNom = String.valueOf(editTextCategory.getText());

            String[] colors = {"Défaut", "Rouge", "Bleu", "Vert", "Orange", "Violet"};
            int[] colorValues = {android.R.color.transparent, android.R.color.holo_red_dark,
                    android.R.color.holo_blue_dark, android.R.color.holo_green_dark,
                    android.R.color.holo_orange_dark, android.R.color.holo_purple};

            ColorSpinnerAdapter colorAdapter = new ColorSpinnerAdapter(v.getContext(), colors, colorValues);
            spinnerColor.setAdapter(colorAdapter);

            int positionCouleur = 0;
            for (int i = 0; i < colors.length; i++) {
                if (colors[i].equalsIgnoreCase(categorie.getCouleur())) {
                    positionCouleur = i;
                    break;
                }
            }
            spinnerColor.setSelection(positionCouleur);

            AlertDialog dialog = builder.create();
            dialog.show();

            btnCancel.setOnClickListener(view -> dialog.dismiss());

            btnConfirm.setOnClickListener(view -> {
                String nouveauNom = Normalize.normalize(editTextCategory.getText().toString().trim());
                String nouvelleCouleur = colors[spinnerColor.getSelectedItemPosition()];

                if (!nouveauNom.isEmpty()) {
                    categorieService.modifierCategorieEtProduits(categorie.getNom(), nouveauNom, nouvelleCouleur, new CategorieService.Callback() {
                        @Override
                        public void onSuccess() {
                            categorie.setNom(nouveauNom);
                            categorie.setCouleur(nouvelleCouleur);
                            notifyItemChanged(holder.getBindingAdapterPosition());

                            if (listener != null) {
                                listener.modifierCategorie(ancienNom, nouveauNom, nouvelleCouleur);
                            }

                            dialog.dismiss();
                            CustomToast.show(v.getContext(), "Catégorie modifié avec succès !", R.drawable.ic_success);
                        }

                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(v.getContext(), "⚠️ Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    CustomToast.show(v.getContext(), "Veuillez remplir tous les champs !", R.drawable.ic_success);

                }
            });
        });


        holder.btnSupprimer.setOnClickListener(v -> {
            String nomCategorie = holder.nomCategorie.getText().toString(); // ✅ Récupère le nom directement

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Supprimer la catégorie")
                    .setMessage("Voulez-vous vraiment supprimer la catégorie " + nomCategorie + " ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        categorieService.supprimerCategorie(nomCategorie, new CategorieService.Callback() {
                            @Override
                            public void onSuccess() {
                                CustomToast.show(v.getContext(), "Catégorie supprimée !", R.drawable.ic_success);
                                int position = holder.getBindingAdapterPosition();
                                if (position != RecyclerView.NO_POSITION) {
                                    categories.remove(position); // ✅ Supprime en local
                                    notifyItemRemoved(position); // ✅ Met à jour l'affichage
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Exception e) {
                                CustomToast.show(v.getContext(), "Erreur : " + e.getMessage(), R.drawable.ic_error);
                            }
                        });
                    })
                    .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // init du carré de couleur
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class CategorieViewHolder extends RecyclerView.ViewHolder {
        TextView nomCategorie;
        ImageButton btnSupprimer; // 🔥 Instance du bouton suppression
        ImageButton btnModifier;
        View viewColor;

        public CategorieViewHolder(@NonNull View itemView) {
            super(itemView);
            nomCategorie = itemView.findViewById(R.id.nomCategorie);
            btnModifier = itemView.findViewById(R.id.btnModifierCategorie);
            btnSupprimer = itemView.findViewById(R.id.btnSupprimerCategorie); // 🔥 Instance du bouton suppression
            viewColor = itemView.findViewById(R.id.couleurCategorie);
        }
    }


    // 🔥 Afficher un Dialog de suppression
    private void afficherDialogSuppression(String nomCategorie) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Supprimer la catégorie");
        builder.setMessage("Voulez-vous vraiment supprimer cette catégorie ?");

        builder.setPositiveButton("Oui", (dialog, which) -> {
            categorieService.supprimerCategorie(nomCategorie, new CategorieService.Callback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(context, "Catégorie supprimée !", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void mettreAJourListe(List<Categorie> nouvellesCategories) {
        this.categories.clear();
        this.categories.addAll(nouvellesCategories); // ✅ Ajoute les objets `Categorie`
        notifyDataSetChanged(); // ✅ Rafraîchit l'affichage du RecyclerView
    }

    private int getColorFromCategorie(String couleur) {
        switch (couleur.toLowerCase()) {
            case "rouge":
                return ContextCompat.getColor(context, android.R.color.holo_red_dark);
            case "bleu":
                return ContextCompat.getColor(context, android.R.color.holo_blue_dark);
            case "vert":
                return ContextCompat.getColor(context, android.R.color.holo_green_dark);
            case "orange":
                return ContextCompat.getColor(context, android.R.color.holo_orange_dark);
            case "violet":
                return ContextCompat.getColor(context, android.R.color.holo_purple);
            default:
                return ContextCompat.getColor(context, android.R.color.transparent); // Couleur par défaut
        }
    }

    public interface OnCategorieActionListener {
        void modifierCategorie(String ancienNom, String nouveauNom, String nouvelleCouleur);

        void supprimerCategorie(String nomCategorie);
    }

    public interface OnCategorieModifieListener {
        void onCategorieModifiee();
    }

}
