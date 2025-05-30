package com.example.tablettegourmande.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Option;
import com.example.tablettegourmande.services.OptionService;

import java.util.List;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.ViewHolder> {
    private final Context context;
    private final List<Option> options;
    private final OptionService optionService;
    private final OnOptionActionListener listener;

    private String restaurantId;

    public interface OnOptionActionListener {
        void onModifierOption(Option option);
    }

    public OptionAdapter(Context context, List<Option> options, OptionService optionService, OnOptionActionListener listener, String restaurantId) {
        this.restaurantId = restaurantId;
        this.context = context;
        this.options = options;
        this.optionService = optionService;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition(); // ✅ Utiliser l'index actuel
        if (adapterPosition == RecyclerView.NO_POSITION) return; // ⚠️ Sécurité pour éviter un crash

        Option option = options.get(adapterPosition); // ✅ Bonne pratique

        holder.nomOption.setText(option.getNom());
        holder.typeOption.setText(option.isMultiple() ? "Multiple" : "Simple");


        String valeursConcat = TextUtils.join(", ", option.getChoix());

        if (valeursConcat.length() > 30) {
            valeursConcat = valeursConcat.substring(0, 27) + "...";
        }
        holder.valeursOption.setText(valeursConcat);

        holder.btnModifier.setOnClickListener(v -> listener.onModifierOption(option));

        holder.btnSupprimer.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Supprimer l'option")
                    .setMessage("Voulez-vous vraiment supprimer l'option' " + option.getNom() + " ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        optionService.supprimerOption(restaurantId, option.getNom(), new OptionService.Callback() {
                            @Override
                            public void onSuccess() {
                                int currentPosition = holder.getAdapterPosition();
                                if (currentPosition != RecyclerView.NO_POSITION) {
                                    options.remove(currentPosition);
                                    notifyItemRemoved(currentPosition);
                                }
                            }
                            @Override
                            public void onFailure(Exception e) {
                                Log.e("Firebase", "Erreur lors de la suppression de l'option", e);
                            }
                        });
                    })
                    .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                    .show();
        });

    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public void mettreAJourListe(List<Option> nouvellesOptions) {
        options.clear();
        options.addAll(nouvellesOptions);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nomOption, typeOption, valeursOption;
        ImageButton btnModifier, btnSupprimer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nomOption = itemView.findViewById(R.id.nomOption);
            typeOption = itemView.findViewById(R.id.typeOption);  // 🔥 Ajout de `typeOption`
            valeursOption = itemView.findViewById(R.id.valeursOption);  // 🔥 Ajout de `valeursOption`
            btnModifier = itemView.findViewById(R.id.btnModifierOption);
            btnSupprimer = itemView.findViewById(R.id.btnSupprimerOption);
        }
    }

}
