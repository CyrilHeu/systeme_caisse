package com.example.tablettegourmande.ui.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tablettegourmande.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;

public class GeneralInfoFragment extends Fragment {

    private EditText restaurantNameEditText, addressEditText, countryEditText, editTextCodePostal;
    private AutoCompleteTextView autoCompleteVille;
    private Button nextButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.setup_fragment_general_info, container, false);

        restaurantNameEditText = root.findViewById(R.id.restaurant_name);
        addressEditText = root.findViewById(R.id.address);
        countryEditText = root.findViewById(R.id.country);
        autoCompleteVille = root.findViewById(R.id.autoCompleteVille);
        editTextCodePostal = root.findViewById(R.id.editTextCodePostal);
        nextButton = root.findViewById(R.id.next_button);

        nextButton.setOnClickListener(v -> {
            InitialSetupActivity activity = (InitialSetupActivity) requireActivity();
            activity.updateRestaurantData("name", restaurantNameEditText.getText().toString());
            activity.updateRestaurantData("address", addressEditText.getText().toString());
            activity.updateRestaurantData("country", countryEditText.getText().toString());
            activity.updateRestaurantData("city", autoCompleteVille.getText().toString());
            activity.updateRestaurantData("postalCode", editTextCodePostal.getText().toString());

            activity.updateRestaurantData("ownerId", FirebaseAuth.getInstance().getUid());
            activity.updateRestaurantData("createdAt", FieldValue.serverTimestamp());
            activity.goToNextPage();
        });

        return root;
    }
}
