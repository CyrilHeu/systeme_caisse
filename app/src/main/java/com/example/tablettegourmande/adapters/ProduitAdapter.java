package com.example.tablettegourmande.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Produit;
import com.example.tablettegourmande.services.ProduitService;

import java.util.List;

import Utils.CustomToast;

public class ProduitAdapter extends RecyclerView.Adapter<ProduitAdapter.ProduitViewHolder> {

    private List<Produit> produits;
    private Context context;
    private final ProduitService produitService;

    private final OnProduitActionListener listener;

    public ProduitAdapter(Context context, List<Produit> produits, ProduitService produitService, OnProduitActionListener listener) {
        this.context = context;
        this.produits = produits;
        this.produitService = produitService;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ProduitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_produit, parent, false);
        return new ProduitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProduitViewHolder holder, int position) {
        Produit produit = produits.get(holder.getBindingAdapterPosition());
        holder.nomProduit.setText(produit.getNom());
        holder.prixProduit.setText(String.format("%.2f €", produit.getPrix()));
        holder.categorieProduit.setText(produit.getCategorie());

        holder.btnModifier.setOnClickListener(v -> {
            if (listener != null) {
                listener.onModifierProduit(produit);
            }
        });

        holder.btnSupprimer.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Supprimer le produit")
                    .setMessage("Voulez-vous vraiment supprimer " + produit.getNom() + " ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        produitService.supprimerProduit(produit.getNom(), new ProduitService.Callback() {
                            @Override
                            public void onSuccess() {
                                int currentPosition = holder.getBindingAdapterPosition();
                                if (currentPosition != RecyclerView.NO_POSITION) {
                                    produits.remove(currentPosition);
                                    notifyItemRemoved(currentPosition);
                                }
                                CustomToast.show(context, "Produit supprimé", R.drawable.ic_warning);
                            }

                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return produits.size();
    }

    public void mettreAJourListe(List<Produit> nouveauxProduits) {
        this.produits.clear();
        this.produits.addAll(nouveauxProduits);
        notifyDataSetChanged();
    }

    public static class ProduitViewHolder extends RecyclerView.ViewHolder {
        TextView nomProduit, prixProduit, categorieProduit;
        ImageButton btnModifier, btnSupprimer;

        public ProduitViewHolder(@NonNull View itemView) {
            super(itemView);
            nomProduit = itemView.findViewById(R.id.nomProduit);
            prixProduit = itemView.findViewById(R.id.prixProduit);
            categorieProduit = itemView.findViewById(R.id.categorieProduit);
            btnModifier = itemView.findViewById(R.id.btnModifierProduit);
            btnSupprimer = itemView.findViewById(R.id.btnSupprimerProduit);
        }
    }
    public interface OnProduitActionListener {
        void onModifierProduit(Produit produit);
    }

}
