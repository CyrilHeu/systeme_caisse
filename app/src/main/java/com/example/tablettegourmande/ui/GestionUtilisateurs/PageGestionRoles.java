package com.example.tablettegourmande.ui.GestionUtilisateurs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Role;
import com.example.tablettegourmande.services.RoleService;
import com.example.tablettegourmande.permissions.PermissionUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import Utils.CustomToast;

public class PageGestionRoles extends Fragment {

    private RecyclerView recyclerView;
    private EditText editTextRecherche;
    private FloatingActionButton fabAjouter;
    private RoleAdapter adapter;
    private RoleService roleService;
    private String restaurantId;
    private List<Role> listeComplete = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_gestion_role, container, false);

        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        restaurantId = prefs.getString("restaurantId", null);

        recyclerView = view.findViewById(R.id.recyclerViewRoles);
        editTextRecherche = view.findViewById(R.id.editTextRechercheRole);
        fabAjouter = view.findViewById(R.id.fabAjouterRole);

        adapter = new RoleAdapter(requireContext(), new ArrayList<>(), restaurantId);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        roleService = new RoleService(restaurantId, roles -> {
            listeComplete = roles;
            adapter.updateList(roles);
        });

        fabAjouter.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  PermissionUtils.hasPermission("Gestion des utilisateurs", authorized -> {
                      if (authorized) {
                          // L'utilisateur a le droit
                          DialogGestionRole.ouvrirDialog(null, restaurantId, getContext(), true);
                      } else {
                          CustomToast.show(getContext(), "Droits insuffisants pour effectuer cette action.", R.drawable.ic_warning);
                      }
                  });
              }
          }
        );

        editTextRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrer(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }
}
