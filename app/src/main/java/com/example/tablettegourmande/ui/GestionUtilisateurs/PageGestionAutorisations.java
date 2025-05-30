package com.example.tablettegourmande.ui.GestionUtilisateurs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Role;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class PageGestionAutorisations extends Fragment {

    private List<Role> currentRoles = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_page_gestion_autorisations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.roleTabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.roleViewPager);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) return;

                    String restaurantId = userDoc.getString("restaurantId");
                    if (restaurantId == null || restaurantId.isEmpty()) return;

                    db.collection("restaurants")
                            .document(restaurantId)
                            .collection("roles")
                            .addSnapshotListener((query, error) -> {
                                if (error != null || query == null) return;

                                List<Role> roles = new ArrayList<>();

                                for (DocumentSnapshot doc : query.getDocuments()) {
                                    String roleId = doc.getId();
                                    if (!roleId.equals("superviseur")) {
                                        Role role = doc.toObject(Role.class);
                                        if (role != null) {
                                            role.setId(roleId);
                                            if (role.getNom() == null || role.getNom().trim().isEmpty()) {
                                                role.setNom(roleId);
                                            }
                                            roles.add(role);
                                        }
                                    }
                                }

                                roles.sort(Comparator.comparing(Role::getNom, String.CASE_INSENSITIVE_ORDER));

                                // Ne relancer que si les noms ont changé
                                boolean nomChange = roles.size() != currentRoles.size();
                                if (!nomChange) {
                                    for (int i = 0; i < roles.size(); i++) {
                                        if (!Objects.equals(roles.get(i).getId(), currentRoles.get(i).getId()) ||
                                                !Objects.equals(roles.get(i).getNom(), currentRoles.get(i).getNom())) {
                                            nomChange = true;
                                            break;
                                        }
                                    }
                                }

                                if (!nomChange) return;
                                currentRoles = roles;

                                int currentItem = viewPager.getCurrentItem();

                                RolePagerAdapter adapter = new RolePagerAdapter(requireActivity(), roles, restaurantId);
                                viewPager.setAdapter(adapter);

                                new TabLayoutMediator(tabLayout, viewPager,
                                        (tab, position) -> tab.setText(roles.get(position).getNom())
                                ).attach();

                                viewPager.setCurrentItem(currentItem, false);
                            });
                });
    }
}
