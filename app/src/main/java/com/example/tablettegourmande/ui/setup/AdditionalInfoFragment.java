package com.example.tablettegourmande.ui.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tablettegourmande.R;

public class AdditionalInfoFragment extends Fragment {

    private EditText phoneEditText, emailEditText, siretEditText, nomEditText, prenomEditText, tvaEditText;

    private CheckBox notifyCheckBox;
    private Button nextButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.setup_fragment_additional_info, container, false);

        nomEditText = root.findViewById(R.id.nom_user);
        prenomEditText = root.findViewById(R.id.prenom_user);
        tvaEditText = root.findViewById(R.id.tva_intracom);
        phoneEditText = root.findViewById(R.id.phone);
        emailEditText = root.findViewById(R.id.email);
        siretEditText = root.findViewById(R.id.siret);
        nextButton = root.findViewById(R.id.next_button);
        notifyCheckBox = root.findViewById(R.id.email_notify);

        nextButton.setOnClickListener(v -> {
            InitialSetupActivity activity = (InitialSetupActivity) requireActivity();

            activity.updateDataUser("prenom", prenomEditText.getText().toString());
            activity.updateDataUser("nom", nomEditText.getText().toString());
            activity.updateDataUser("email", emailEditText.getText().toString());

            activity.updateRestaurantData("phone", phoneEditText.getText().toString());
            activity.updateRestaurantData("email", emailEditText.getText().toString());
            activity.updateRestaurantData("siret", siretEditText.getText().toString());
            activity.updateRestaurantData("tvaIntracom", tvaEditText.getText().toString());
            activity.updateRestaurantData("emailNotify", notifyCheckBox.isChecked());
            activity.goToNextPage();

        });

        return root;
    }
}
