package com.example.tablettegourmande.ui.gestion;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.ui.gestion.ChoixAdapter;
import com.example.tablettegourmande.adapters.OptionAdapter;
import com.example.tablettegourmande.models.Option;
import com.example.tablettegourmande.services.OptionService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Utils.CustomToast;
import Utils.Normalize;

public class PageGestionOption extends Fragment {
    private RecyclerView recyclerView;
    private OptionAdapter adapter;
    private List<Option> options = new ArrayList<>();
    private OptionService optionService;
    private EditText editTextRecherche;
    private Spinner spinnerTri;
    private FloatingActionButton fabAjouterOption;
    private ChoixAdapter choixAdapter;
    private List<String> choixList = new ArrayList<>();
    private RecyclerView recyclerViewChoix;
    private ItemTouchHelper itemTouchHelper;
    private List<Option> optionsOriginaux = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_gestion_option, container, false);

        String restaurantId = getRestaurantIdFromSharedPreferences();
        optionService = new OptionService(restaurantId);

        recyclerView = view.findViewById(R.id.recyclerViewOptions);
        editTextRecherche = view.findViewById(R.id.searchOption);
        spinnerTri = view.findViewById(R.id.spinnerTriOption);
        fabAjouterOption = view.findViewById(R.id.fabAjouterOption);

        fabAjouterOption.setOnClickListener(v -> afficherDialogAjoutModification(null));

        adapter = new OptionAdapter(getContext(), options, optionService, option -> afficherDialogAjoutModification(option), restaurantId);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupTriOption();
        chargerOptions();

        editTextRecherche.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrerOptions(s.toString());
                trierOptions(spinnerTri.getSelectedItemPosition());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void afficherDialogAjoutModification(@Nullable Option optionExistant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_layout_option, null);
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        EditText editTextNom = dialogView.findViewById(R.id.editTextNomOption);
        CheckBox checkBoxMultiple = dialogView.findViewById(R.id.checkBoxMultiple);
        recyclerViewChoix = dialogView.findViewById(R.id.recyclerViewChoix);

        Button btnAjouterChoix = dialogView.findViewById(R.id.btnAjouterChoix);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        List<View> editTextChoixList = new ArrayList<>();

        choixList.clear(); // 🔥 S'assurer que la liste est vide au départ

        // Remplissage si modification
        if (optionExistant != null) {
            dialogTitle.setText("Modifier une option");
            editTextNom.setText(optionExistant.getNom());
            checkBoxMultiple.setChecked(optionExistant.isMultiple());

            choixList.clear();

            if (choixAdapter == null) {
                choixAdapter = new ChoixAdapter(choixList, viewHolder -> itemTouchHelper.startDrag(viewHolder));
                recyclerViewChoix.setAdapter(choixAdapter);
            } else {
                choixAdapter.notifyDataSetChanged(); // 🔥 Appelé seulement si choixAdapter n'est pas NULL
            }

            for (String choix : optionExistant.getChoix()) {
                choixList.add(choix);
            }

            // 🔥 Mettre à jour l'adaptateur après modification
            choixAdapter.notifyDataSetChanged();
        }
        else {
            dialogTitle.setText("Ajouter une option");
        }

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setOnShowListener(dialogInterface -> {
            for (Option option : options) {
                if (option.getNom().equals(editTextNom.getText().toString().trim())) {
                    editTextNom.setText(option.getNom());
                    checkBoxMultiple.setChecked(option.isMultiple());
                    choixList.clear();
                    choixList.addAll(option.getChoix());
                    choixAdapter.notifyDataSetChanged();
                    break;
                }
            }
        });

        btnAjouterChoix.setOnClickListener(v -> {
            if (choixAdapter == null) {
                choixAdapter = new ChoixAdapter(choixList, viewHolder -> itemTouchHelper.startDrag(viewHolder));
                recyclerViewChoix.setAdapter(choixAdapter);
            }

            choixList.add("");
            choixAdapter.notifyItemInserted(choixList.size() - 1);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String nom = Normalize.normalize(editTextNom.getText().toString().trim());
            boolean multiple = checkBoxMultiple.isChecked();

            // Récupérer la nouvelle liste de choix
            List<String> choixListUpdated = new ArrayList<>();
            for (String choix : choixAdapter.getChoixList()) {
                if (!choix.trim().isEmpty()) {
                    choixListUpdated.add(choix);
                }
            }

            if (nom.isEmpty() || choixListUpdated.isEmpty()) {
                CustomToast.show(getContext(), "Le nom et au moins un choix sont requis",R.drawable.ic_warning);
                return;
            }

            String restaurantId = getRestaurantIdFromSharedPreferences();

            if (optionExistant == null) {
                optionService.verifierDoublonNom(nom, () -> {
                    CustomToast.show(getContext(), "Ce nom d'option existe déjà !",0);
                }, () -> {
                    optionService.ajouterOption(new Option(nom, multiple, choixListUpdated), () -> chargerOptions());
                    CustomToast.show(getContext(), "Option créée avec succès !",R.drawable.ic_success);
                });
            } else {
                String ancienNom = optionExistant.getNom();
                boolean nomChange = !ancienNom.equals(nom);

                if (nomChange) {
                    optionService.verifierDoublonNom(nom, () -> {
                        CustomToast.show(getContext(), "Ce nom d'option existe déjà !",R.drawable.ic_warning);
                    }, () -> {
                        // Suppression et ajout si le nom n'existe pas encore
                        optionService.recupererIndexOptionDansProduits(restaurantId, ancienNom, index -> {
                            optionService.mettreAJourProduitsAvecOption(restaurantId, ancienNom, new Option(nom, multiple, choixListUpdated), () -> {
                                optionService.supprimerOption(restaurantId, ancienNom, new OptionService.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        optionService.ajouterOption(new Option(nom, multiple, choixListUpdated), () -> {
                                            CustomToast.show(getContext(), "Modification effectuée avec succès !!", R.drawable.ic_success);
                                            mettreAJourOptionDansAdapter(ancienNom, nom, multiple, choixListUpdated);

                                        });
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        CustomToast.show(getContext(), "Erreur lors de la suppression de l'option !", R.drawable.ic_warning);
                                        Log.e("Firebase", "Erreur lors de la suppression de l'option", e);
                                    }
                                });

                            });
                        });

                    });
                } else {
                    optionExistant.setMultiple(multiple);
                    optionExistant.setChoix(choixListUpdated);
                    optionService.modifierOption(optionExistant, () -> {
                        optionService.recupererIndexOptionDansProduits(restaurantId, ancienNom, index -> {
                            optionService.mettreAJourProduitsAvecOption(restaurantId, ancienNom, optionExistant, () -> {
                                CustomToast.show(getContext(), "Modification effectuée avec succès !", R.drawable.ic_success);
                                chargerOptions();
                            });
                        });

                    });
                }
            }

            dialog.dismiss();
        });


        recyclerViewChoix = dialogView.findViewById(R.id.recyclerViewChoix);
        recyclerViewChoix.setLayoutManager(new LinearLayoutManager(getContext()));

        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                Collections.swap(choixList, fromPosition, toPosition);
                choixAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Ne rien faire sur un swipe
            }
        };

        itemTouchHelper = new ItemTouchHelper(callback);

        if (choixAdapter == null) {
            choixAdapter = new ChoixAdapter(choixList, viewHolder -> itemTouchHelper.startDrag(viewHolder));
            recyclerViewChoix.setAdapter(choixAdapter);
        }

        recyclerViewChoix.setAdapter(choixAdapter);
        itemTouchHelper.attachToRecyclerView(recyclerViewChoix);

    }

    private void chargerOptions() {
        optionService.getOptions(optionsList -> {
            options.clear();
            options.addAll(optionsList);
            optionsOriginaux.clear();
            optionsOriginaux.addAll(optionsList);
            adapter.notifyDataSetChanged();
        });

    }
    private void mettreAJourOptionDansAdapter(String ancienNom, String nouveauNom, boolean multiple, List<String> choixListUpdated) {
        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);

            // 🔥 Vérifier si c'est l'ancienne option avant modification
            if (option.getNom().equals(ancienNom)) {
                option.setNom(nouveauNom); // 🔥 Met à jour le nom si nécessaire
                option.setMultiple(multiple);
                option.setChoix(choixListUpdated);
                adapter.notifyItemChanged(i); // 🔥 Rafraîchit uniquement cette option dans la liste
                return;
            }
        }

        // 🔥 Si l'option n'existe pas encore dans la liste, l'ajouter
        Option nouvelleOption = new Option(nouveauNom, multiple, choixListUpdated);
        options.add(nouvelleOption);
        adapter.notifyDataSetChanged(); // 🔥 Rafraîchit toute la liste pour afficher la nouvelle option
    }


    private void filtrerOptions(String texte) {
        List<Option> optionsFiltrees = new ArrayList<>();
        for (Option option : optionsOriginaux) {  // ✅ Utilise la liste complète
            if (option.getNom().toLowerCase().contains(texte.toLowerCase())) {
                optionsFiltrees.add(option);
            }
        }
        adapter.mettreAJourListe(optionsFiltrees);
    }

    private void setupTriOption() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"A-Z", "Z-A"}){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(getResources().getColor(R.color.black));
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                textView.setTextColor(getResources().getColor(R.color.black));
                textView.setBackgroundColor(getResources().getColor(android.R.color.white));
                return textView;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTri.setAdapter(adapter);

        spinnerTri.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                trierOptions(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void trierOptions(int triSelection) {
        List<Option> optionsTries = new ArrayList<>(options);

        if (triSelection == 0) { // A-Z
            Collections.sort(optionsTries, (o1, o2) -> o1.getNom().compareToIgnoreCase(o2.getNom()));
        } else if (triSelection == 1) { // Z-A
            Collections.sort(optionsTries, (o1, o2) -> o2.getNom().compareToIgnoreCase(o1.getNom()));
        }

        adapter.mettreAJourListe(optionsTries);
    }


    private String getRestaurantIdFromSharedPreferences() {
        return requireContext().getSharedPreferences("APP_PREFS", getContext().MODE_PRIVATE).getString("restaurantId", null);
    }
}
