package com.example.tablettegourmande.ui.gestion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tablettegourmande.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChoixAdapter extends RecyclerView.Adapter<ChoixAdapter.ChoixViewHolder> {

    private final List<String> choixList;
    private final OnStartDragListener dragListener;
    private RecyclerView recyclerView;

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public ChoixAdapter(List<String> choixList, OnStartDragListener dragListener) {
        this.choixList = choixList;
        this.dragListener = dragListener;
    }

    @NonNull
    @Override
    public ChoixViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_choix, parent, false);
        return new ChoixViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChoixViewHolder holder, int position) {
        holder.editTextChoix.setText(choixList.get(position));

        // Déclenchement du drag & drop sur appui du bouton ☰
        holder.btnDrag.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                dragListener.onStartDrag(holder);
            }
            return false;
        });

        // Mettre à jour la liste à chaque modification
        holder.editTextChoix.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                choixList.set(position, holder.editTextChoix.getText().toString().trim());
            }
        });

        holder.btnSupprimerChoix.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                choixList.remove(currentPosition);
                notifyItemRemoved(currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return choixList.size();
    }

    public static class ChoixViewHolder extends RecyclerView.ViewHolder {
        EditText editTextChoix;
        ImageButton btnDrag, btnSupprimerChoix; // 🔥 Ajout du bouton

        public ChoixViewHolder(@NonNull View itemView) {
            super(itemView);
            editTextChoix = itemView.findViewById(R.id.editTextChoix);
            btnDrag = itemView.findViewById(R.id.btnDrag);
            btnSupprimerChoix = itemView.findViewById(R.id.btnSupprimerChoix); // 🔥 Initialisation
        }
    }


    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(choixList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public List<String> getChoixList() {
        List<String> choixMisAJour = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            if (recyclerView != null) {  // 🔥 Vérifie que recyclerView n'est pas null
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
                if (viewHolder instanceof ChoixViewHolder) {
                    ChoixViewHolder holder = (ChoixViewHolder) viewHolder;
                    String choixTexte = holder.editTextChoix.getText().toString().trim();
                    if (!choixTexte.isEmpty()) {
                        choixMisAJour.add(choixTexte);
                    }
                } else {
                    choixMisAJour.add(choixList.get(i));  // 🔥 Ajoute l’ancienne valeur si la vue n’est pas active
                }
            } else {
                choixMisAJour.add(choixList.get(i));  // 🔥 Sécurité supplémentaire
            }
        }
        return choixMisAJour;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }



}
