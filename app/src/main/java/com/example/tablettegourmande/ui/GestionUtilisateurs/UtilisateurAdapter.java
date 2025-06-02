package com.example.tablettegourmande.ui.GestionUtilisateurs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Utilisateur;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import Utils.CustomToast;

public class UtilisateurAdapter extends RecyclerView.Adapter<UtilisateurAdapter.UtilisateurViewHolder> {

    private Context context;
    private List<Utilisateur> listeOriginale;
    private List<Utilisateur> listeAffichee;
    private String restaurantId;

    public UtilisateurAdapter(Context context, List<Utilisateur> liste, String restaurantId) {
        this.context = context;
        this.listeOriginale = liste;
        this.listeAffichee = new ArrayList<>(liste);
        this.restaurantId = restaurantId;
    }

    @NonNull
    @Override
    public UtilisateurViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_utilisateur, parent, false);
        return new UtilisateurViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UtilisateurViewHolder holder, int position) {
        Utilisateur utilisateur = listeAffichee.get(position);

        // Ligne principale : Nom Prénom • Rôle
        String ligneNom = utilisateur.getNom() + " " + utilisateur.getPrenom();

        String role = utilisateur.getRole();

        holder.textNom.setText(ligneNom);

        holder.textRole.setText(role);

        holder.textEmail.setText(utilisateur.getEmail());

        if (utilisateur.getNumero() != null && !utilisateur.getNumero().isEmpty()) {

            holder.textNumero.setText(utilisateur.getNumero().replaceAll("(..)(?=..)", "$1-"));
            holder.textNumero.setVisibility(View.VISIBLE);
        } else {
            holder.textNumero.setVisibility(View.GONE);
        }
        holder.btnModifier.setVisibility(View.INVISIBLE);

        FirebaseFirestore.getInstance()
                .collection("restaurants")
                .document(restaurantId)
                .get()
                .addOnSuccessListener(restaurantDoc -> {
                    String currentMainUserId = restaurantDoc.getString("current_main_user");

                    if(currentMainUserId.equals("1")){
                        holder.btnModifier.setVisibility(View.VISIBLE);
                    }
                    if(role.equals("Superviseur") && !currentMainUserId.equals("1")){
                        holder.btnModifier.setVisibility(View.INVISIBLE);
                    }else{
                        holder.btnModifier.setVisibility(View.VISIBLE);
                    }

                });

        holder.btnModifier.setOnClickListener(v -> {
                DialogGestionUtilisateur.ouvrirDialog(utilisateur, restaurantId, context, true);
        });

        if (!utilisateur.getRole().equals("Superviseur")) {
            // On récupère current_main_user depuis Firestore
            FirebaseFirestore.getInstance()
                    .collection("restaurants")
                    .document(restaurantId)
                    .get()
                    .addOnSuccessListener(restaurantDoc -> {
                        String currentMainUserId = restaurantDoc.getString("current_main_user");

                        if (utilisateur.getId().equals(currentMainUserId)) {
                            // L'utilisateur est le current_main_user : message d’impossibilité
                            holder.btnSupprimer.setOnClickListener(v -> {
                                new AlertDialog.Builder(context)
                                        .setTitle("Action non autorisée")
                                        .setMessage("Impossible de supprimer l'utilisateur actuellement connecté.")
                                        .setPositiveButton("OK", null)
                                        .show();
                            });
                        } else {
                            // Suppression normale possible
                            holder.btnSupprimer.setOnClickListener(v -> {
                                new AlertDialog.Builder(context)
                                        .setTitle("Suppression")
                                        .setMessage("Supprimer cet utilisateur ?")
                                        .setPositiveButton("Oui", (dialog, which) -> {
                                            FirebaseFirestore.getInstance()
                                                    .collection("restaurants")
                                                    .document(restaurantId)
                                                    .collection("users")
                                                    .document(utilisateur.getId())
                                                    .delete();
                                        })
                                        .setNegativeButton("Annuler", null)
                                        .show();
                            });
                        }
                    });
        } else {
            holder.btnSupprimer.setVisibility(View.INVISIBLE);
        }


    }

    @Override
    public int getItemCount() {
        return listeAffichee.size();
    }

    public void updateList(List<Utilisateur> nouvelleListe) {
        this.listeOriginale = nouvelleListe;
        this.listeAffichee = new ArrayList<>(nouvelleListe);
        notifyDataSetChanged();
    }

    public void filtrer(String texte) {
        if (texte.isEmpty()) {
            listeAffichee = new ArrayList<>(listeOriginale);
        } else {
            List<Utilisateur> filtrée = new ArrayList<>();
            String recherche = texte.toLowerCase();

            for (Utilisateur u : listeOriginale) {
                String contenu = (
                        (u.getNom() != null ? u.getNom() : "") + " " +
                                (u.getPrenom() != null ? u.getPrenom() : "") + " " +
                                (u.getRole() != null ? u.getRole() : "") + " " +
                                (u.getEmail() != null ? u.getEmail() : "") + " " +
                                (u.getNumero() != null ? u.getNumero() : "")
                ).toLowerCase();

                if (contenu.contains(recherche)) {
                    filtrée.add(u);
                }
            }

            listeAffichee = filtrée;
        }

        notifyDataSetChanged();
    }

    static class UtilisateurViewHolder extends RecyclerView.ViewHolder {
        TextView textNom, textEmail, textNumero, textRole;
        ImageButton btnModifier, btnSupprimer;

        public UtilisateurViewHolder(@NonNull View itemView) {
            super(itemView);
            textNom = itemView.findViewById(R.id.textNomUtilisateur);
            textRole = itemView.findViewById(R.id.textRoleUtilisateur);
            textEmail = itemView.findViewById(R.id.textEmailUtilisateur);
            textNumero = itemView.findViewById(R.id.textNumeroUtilisateur);
            btnModifier = itemView.findViewById(R.id.btnModifierUtilisateur);
            btnSupprimer = itemView.findViewById(R.id.btnSupprimerUtilisateur);
        }
    }
}

