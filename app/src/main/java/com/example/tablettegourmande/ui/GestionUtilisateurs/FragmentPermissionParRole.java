package com.example.tablettegourmande.ui.GestionUtilisateurs;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tablettegourmande.permissions.PermissionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class FragmentPermissionParRole extends Fragment {

    private static final String ARG_ROLE_ID = "roleId";
    private static final String ARG_RESTAURANT_ID = "restaurantId";

    private String roleId;
    private String restaurantId;

    public static FragmentPermissionParRole newInstance(String roleId, String restaurantId) {
        FragmentPermissionParRole fragment = new FragmentPermissionParRole();
        Bundle args = new Bundle();
        args.putString(ARG_ROLE_ID, roleId);
        args.putString(ARG_RESTAURANT_ID, restaurantId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            roleId = getArguments().getString(ARG_ROLE_ID);
            restaurantId = getArguments().getString(ARG_RESTAURANT_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(getContext());
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        scrollView.addView(layout);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurants")
                .document(restaurantId)
                .collection("roles")
                .document(roleId)
                .get()
                .addOnSuccessListener(doc -> {
                    Map<String, Object> permissions = (Map<String, Object>) doc.get("permissions");

                    for (Map.Entry<String, List<String>> entry : PermissionManager.PERMISSIONS.entrySet()) {
                        String categoryTitle = entry.getKey();
                        if (categoryTitle.equals("Fonctions Sensibles")) {
                            categoryTitle = "Autres fonctions";
                        }

                        TextView categoryHeader = new TextView(getContext());
                        categoryHeader.setText(categoryTitle);
                        categoryHeader.setTextSize(18);
                        categoryHeader.setTypeface(null, Typeface.BOLD);
                        categoryHeader.setPadding(0, 24, 0, 12);
                        layout.addView(categoryHeader);

                        for (String permission : entry.getValue()) {
                            LinearLayout line = new LinearLayout(getContext());
                            line.setOrientation(LinearLayout.HORIZONTAL);
                            line.setPadding(20, 6, 20, 12);
                            line.setLayoutParams(new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            ));

                            GradientDrawable background = new GradientDrawable();
                            background.setColor(Color.parseColor("#F5F5F5")); // gris très clair
                            background.setCornerRadius(12f);
                            line.setBackground(background);

                            TextView label = new TextView(getContext());
                            label.setText(permission);
                            label.setTextSize(15);
                            label.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                            label.setPadding(0, 0, 16, 0);

                            Switch sw = new Switch(getContext());
                            sw.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                            boolean value = permissions != null && Boolean.TRUE.equals(permissions.get(permission));
                            sw.setChecked(value);

                            sw.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                                db.collection("restaurants")
                                        .document(restaurantId)
                                        .collection("roles")
                                        .document(roleId)
                                        .update(FieldPath.of("permissions", permission), isChecked);
                            });

                            line.addView(label);
                            line.addView(sw);
                            layout.addView(line);

                            // Espacement entre les lignes
                            View spacer = new View(getContext());
                            spacer.setLayoutParams(new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    12
                            ));
                            layout.addView(spacer);
                        }
                    }
                });

        return scrollView;
    }
}
