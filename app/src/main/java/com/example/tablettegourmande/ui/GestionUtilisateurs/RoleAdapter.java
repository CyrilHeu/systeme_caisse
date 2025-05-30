package com.example.tablettegourmande.ui.GestionUtilisateurs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Role;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import Utils.CustomToast;

public class RoleAdapter extends RecyclerView.Adapter<RoleAdapter.RoleViewHolder> {

    private Context context;
    private List<Role> listeOriginale;
    private List<Role> listeAffichee;
    private String restaurantId;

    public RoleAdapter(Context context, List<Role> roles, String restaurantId) {
        this.context = context;
        this.listeOriginale = roles;
        this.listeAffichee = new ArrayList<>(roles);
        this.restaurantId = restaurantId;
    }

    @NonNull
    @Override
    public RoleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_role, parent, false);
        return new RoleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoleViewHolder holder, int position) {
        Role role = listeAffichee.get(position);
        holder.textNomRole.setText(role.getNom());

        holder.btnModifier.setOnClickListener(v -> {
            DialogGestionRole.ouvrirDialog(role, restaurantId, context, false);
        });

        holder.btnSupprimer.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Supprimer ce rôle ?")
                    .setMessage("Cette action est irréversible.")
                    .setPositiveButton("Supprimer", (dialog, which) -> {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

// Vérifie si des utilisateurs utilisent ce rôle
                        db.collection("restaurants")
                                .document(restaurantId)
                                .collection("users")
                                .whereEqualTo("role", role.getNom())
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (querySnapshot.isEmpty()) {
                                        // Aucun utilisateur avec ce rôle → suppression autorisée
                                        db.collection("restaurants")
                                                .document(restaurantId)
                                                .collection("roles")
                                                .document(role.getId())
                                                .delete()
                                                .addOnSuccessListener(aVoid -> CustomToast.show(context, "Rôle supprimé",  R.drawable.ic_success));
                                    } else {
                                        // Utilisateurs liés → suppression refusée
                                        CustomToast.show(context, "Ce rôle est utilisé par un ou plusieurs utilisateurs", R.drawable.ic_error);
                                    }
                                });

                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return listeAffichee.size();
    }

    public void updateList(List<Role> nouvelleListe) {
        this.listeOriginale = nouvelleListe;
        this.listeAffichee = new ArrayList<>(nouvelleListe);
        notifyDataSetChanged();
    }

    public void filtrer(String texte) {
        if (texte.isEmpty()) {
            listeAffichee = new ArrayList<>(listeOriginale);
        } else {
            List<Role> filtrée = new ArrayList<>();
            String recherche = texte.toLowerCase();

            for (Role r : listeOriginale) {
                if (r.getNom() != null && r.getNom().toLowerCase().contains(recherche)) {
                    filtrée.add(r);
                }
            }
            listeAffichee = filtrée;
        }
        notifyDataSetChanged();
    }

    static class RoleViewHolder extends RecyclerView.ViewHolder {
        TextView textNomRole;
        ImageButton btnModifier, btnSupprimer;

        public RoleViewHolder(@NonNull View itemView) {
            super(itemView);
            textNomRole = itemView.findViewById(R.id.textNomRole);
            btnModifier = itemView.findViewById(R.id.btnModifierRole);
            btnSupprimer = itemView.findViewById(R.id.btnSupprimerRole);
        }
    }
}
