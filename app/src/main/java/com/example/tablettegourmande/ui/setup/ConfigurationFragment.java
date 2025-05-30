package com.example.tablettegourmande.ui.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tablettegourmande.R;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationFragment extends Fragment {

    private Spinner languageSpinner, timezoneSpinner;
    private Button finishButton;
    private CheckBox checkboxMonday, checkboxTuesday, checkboxWednesday, checkboxThursday,
            checkboxFriday, checkboxSaturday, checkboxSunday;
    private CheckBox checkboxCash, checkboxCard, checkboxTicketRestaurant, checkboxCheque;
    private EditText ticketMessageEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.setup_fragment_configuration, container, false);

        languageSpinner = root.findViewById(R.id.language_spinner);
        timezoneSpinner = root.findViewById(R.id.timezone_spinner);
        finishButton = root.findViewById(R.id.finish_button);  // Utilisation du nouvel ID pour le bouton "Terminer"
        ticketMessageEditText = root.findViewById(R.id.ticket_message);

        checkboxMonday = root.findViewById(R.id.checkbox_monday);
        checkboxTuesday = root.findViewById(R.id.checkbox_tuesday);
        checkboxWednesday = root.findViewById(R.id.checkbox_wednesday);
        checkboxThursday = root.findViewById(R.id.checkbox_thursday);
        checkboxFriday = root.findViewById(R.id.checkbox_friday);
        checkboxSaturday = root.findViewById(R.id.checkbox_saturday);
        checkboxSunday = root.findViewById(R.id.checkbox_sunday);

        checkboxCash = root.findViewById(R.id.checkbox_cash);
        checkboxCard = root.findViewById(R.id.checkbox_card);
        checkboxTicketRestaurant = root.findViewById(R.id.checkbox_ticket_restaurant);
        checkboxCheque = root.findViewById(R.id.checkbox_cheque);

        setupSpinners();

        finishButton.setOnClickListener(v -> {
            InitialSetupActivity activity = (InitialSetupActivity) requireActivity();

            // Collecter toutes les données du fragment
            String language = languageSpinner.getSelectedItem().toString();
            String timezone = timezoneSpinner.getSelectedItem().toString();
            String ticketMessage = ticketMessageEditText.getText().toString();

            // Collecter les jours de fermeture
            List<String> closedDays = new ArrayList<>();
            if (checkboxMonday.isChecked()) closedDays.add("Lundi");
            if (checkboxTuesday.isChecked()) closedDays.add("Mardi");
            if (checkboxWednesday.isChecked()) closedDays.add("Mercredi");
            if (checkboxThursday.isChecked()) closedDays.add("Jeudi");
            if (checkboxFriday.isChecked()) closedDays.add("Vendredi");
            if (checkboxSaturday.isChecked()) closedDays.add("Samedi");
            if (checkboxSunday.isChecked()) closedDays.add("Dimanche");

            // Collecter les modes de paiement
            List<String> acceptedPayments = new ArrayList<>();
            if (checkboxCash.isChecked()) acceptedPayments.add("Espèces");
            if (checkboxCard.isChecked()) acceptedPayments.add("Carte bancaire");
            if (checkboxTicketRestaurant.isChecked()) acceptedPayments.add("Tickets restaurant");
            if (checkboxCheque.isChecked()) acceptedPayments.add("Chèques");

            activity.updateRestaurantData("language", language);
            activity.updateRestaurantData("timezone", timezone);
            activity.updateRestaurantData("ticketMessage",ticketMessage);
            activity.updateRestaurantData("closedDays", closedDays);
            activity.updateRestaurantData("acceptedPayments", acceptedPayments);


            activity.saveSetup();
        });

        return root;
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.languages,
                android.R.layout.simple_spinner_item
        );
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdapter);

        ArrayAdapter<CharSequence> timezoneAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.timezones,
                android.R.layout.simple_spinner_item
        );
        timezoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timezoneSpinner.setAdapter(timezoneAdapter);

        preselectTimezone("GMT+01:00 (Paris, Madrid)");
    }

    private void preselectTimezone(String defaultTimezone) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) timezoneSpinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().contains(defaultTimezone)) {
                timezoneSpinner.setSelection(i);
                break;
            }
        }
    }
}
