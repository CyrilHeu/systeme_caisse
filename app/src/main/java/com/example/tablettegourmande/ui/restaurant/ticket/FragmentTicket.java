package com.example.tablettegourmande.ui.restaurant.ticket;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.Utilisateur;
import com.example.tablettegourmande.services.ServiceBase;
import com.example.tablettegourmande.services.TableService;
import com.example.tablettegourmande.services.UserService;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Locale;

import Utils.Normalize;

public class FragmentTicket extends Fragment implements FragmentClavierPaiement.OnMontantSaisiListener {

    private LinearLayout ticketContainer;
    private LinearLayout paymentOptions;
    private Button btnCash, btnCard, btnTR, btnPay, btnTogglePayment, btnCloseTicket;
    private TextView tvSaisieMontant, tvTotal, tvSolde, tvInfo1, tvInfo2;
    private double totalCommande = 50.00; // Valeur temporaire pour test

    private UserService userService;
    private TableService tableService;
    private ListenerRegistration tableListener;

    public FragmentTicket() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.restaurant_ticket, container, false);

        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String restaurantId = prefs.getString("restaurantId", null);

        if (restaurantId == null) {
            Log.e("FragmentSelection", "⚠️ restaurantId est NULL !");
            return view;
        }
        // Initialisation des éléments UI
        ticketContainer = view.findViewById(R.id.ticket_container);
        paymentOptions = view.findViewById(R.id.payment_options);
        btnCash = view.findViewById(R.id.btn_cash);
        btnCard = view.findViewById(R.id.btn_card);
        btnTR = view.findViewById(R.id.btn_tr);
        btnPay = view.findViewById(R.id.btn_pay);
        btnTogglePayment = view.findViewById(R.id.btn_toggle_payment);

        tvSaisieMontant = view.findViewById(R.id.tv_saisie_montant);
        tvTotal = view.findViewById(R.id.tv_total);
        tvSolde = view.findViewById(R.id.tv_solde);

        tvInfo1 = view.findViewById(R.id.info_ticket_1);
        tvInfo2 = view.findViewById(R.id.info_ticket_2);

        btnCloseTicket = view.findViewById(R.id.close_ticket_button);

        // Initialisation du total
        mettreAJourAffichage();

        // Gestion des boutons de paiement
        btnCash.setOnClickListener(v -> Log.d("Ticket", "Paiement en espèces"));
        btnCard.setOnClickListener(v -> Log.d("Ticket", "Paiement CB"));
        btnTR.setOnClickListener(v -> Log.d("Ticket", "Paiement Ticket Resto"));
        btnPay.setOnClickListener(v -> Log.d("Ticket", "Mode encaissement détaillé"));

        // Bouton pour cacher ou afficher le clavier rapide
        btnTogglePayment.setOnClickListener(v -> togglePaymentOptions());

        // Chargement du FragmentClavierPaiement
        FragmentClavierPaiement clavierPaiement = new FragmentClavierPaiement();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_clavier_container, clavierPaiement);
        transaction.commit();

        userService = new UserService();

        userService.getCurrentUser(new ServiceBase.FirestoreCallback() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                String prenom = document.getString("prenom");
                String nom = document.getString("nom");
                String role = document.getString("role");
                tvInfo1.setText("Serveur : "+prenom);
                tvInfo1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Dialog dialogInfoVendeur = new Dialog(getContext());
                        dialogInfoVendeur.setTitle("Informations serveur");
                        // Définir le contenu du Dialog
                        TextView message = new TextView(getContext());
                        String info = prenom + " "+ nom + "\n"+ "Rôle : "+role;
                        message.setText(info);
                        message.setPadding(40, 40, 40, 40);
                        message.setTextSize(16);
                        // Ajouter le TextView au Dialog
                        dialogInfoVendeur.setContentView(message);
                        dialogInfoVendeur.show();
                    }
                });

            }
            @Override
            public void onFailure(Exception e) {
                tvInfo1.setText("Serveur : Undefined");
            }
        });


        tableService = new TableService(restaurantId);
        /*tableService.checkOrInitCurrentTable(new TableService.CurrentTableCallback() {
            @Override
            public void onResult(String tableValue) {
                if(tableValue.equals("direct")){
                    tvInfo2.setText("Ticket en direct");
                }else{
                    tvInfo2.setText("Table : "+ tableValue);
                    tvInfo2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Dialog dialogInfoTable = new Dialog(getContext());
                            dialogInfoTable.setTitle("Informations table");
                            // Définir le contenu du Dialog
                            TextView message = new TextView(getContext());
                            message.setText("Informations table");
                            message.setPadding(40, 40, 40, 40);
                            message.setTextSize(16);
                            // Ajouter le TextView au Dialog
                            dialogInfoTable.setContentView(message);
                            dialogInfoTable.show();

                        }
                    });
                }


            }

            @Override
            public void onError(Exception e) {

            }
        });*/
        tableService.startCurrentTableListener(new TableService.CurrentTableListener() {
            @Override
            public void onTableChanged(String tableName) {
                if(tableName.equals("direct")){
                    tvInfo2.setText("Ticket en direct");
                }else{
                    tvInfo2.setText("Table : "+ tableName);

                }
                tvInfo2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /* ajout dialog avec heure d'ouverture */
                        tableService.getTableOpeningTimestamp(tableName, new TableService.TableTimestampCallback() {
                            @Override
                            public void onSuccess(Timestamp timestamp, String utcOffset) {
                                Dialog dialogInfoTable = new Dialog(getContext());
                                dialogInfoTable.setTitle("Informations table");
                                // Définir le contenu du Dialog
                                TextView message = new TextView(getContext());
                                String heureOuverture = Normalize.formatTimestampToFrenchText(timestamp,utcOffset);
                                message.setText("Heure d'ouverture :"+"\n"+heureOuverture);
                                message.setPadding(40, 40, 40, 40);
                                message.setTextSize(16);
                                // Ajouter le TextView au Dialog
                                dialogInfoTable.setContentView(message);
                                dialogInfoTable.show();
                            }

                            @Override
                            public void onFailure(Exception e) {

                            }
                        });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("FragmentTicket", "Erreur dans le listener : ", e);
            }
        });



        btnCloseTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialogCloseTicket = new Dialog(getContext());
                dialogCloseTicket.setTitle("Fermeture ticket");

                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(40, 40, 40, 40);
                TextView message = new TextView(getContext());
                message.setText("Fermeture ticket"
                        +'\n'+"Conditions et validation :"
                        +'\n'+ "Solde doit être à 0."
                        +'\n'+ "Ou solde à hauteur du total et aucun produit"
                        +'\n'+ "marqué comme \"envoyé\"."
                        +'\n'+ "Sans réunir ces conditions : fermeture impossible.");

                message.setPadding(40, 40, 40, 40);
                message.setTextSize(16);

                Button btnValider = new Button(getContext());
                btnValider.setText("Valider");
                btnValider.setEnabled(false);
                btnValider.setPadding(20, 20, 20, 20);
                btnValider.setOnClickListener(v -> {
                    dialogCloseTicket.dismiss(); // Fermer le Dialog après validation
                    Toast.makeText(getContext(), "Action validée", Toast.LENGTH_SHORT).show();
                });

                layout.addView(message);
                layout.addView(btnValider);
                dialogCloseTicket.setContentView(layout);
                dialogCloseTicket.show();

            }
        });


        return view;
    }

    @Override
    public void onMontantSaisi(String montant) {
        if (montant.isEmpty()) {
            montant = "0";
        }

        double montantSaisi = Double.parseDouble(montant.replace(",", "."));
        double solde = totalCommande - montantSaisi;

        // Mettre à jour l'affichage
        tvSaisieMontant.setText(montant);
        tvSolde.setText(String.format("Solde : %.2f€", solde));

        if (montant.equals("0")) {
            FragmentClavierPaiement clavierPaiement = (FragmentClavierPaiement) getChildFragmentManager().findFragmentById(R.id.fragment_clavier_container);
            if (clavierPaiement != null) {
                clavierPaiement.resetMontant();
            }
        }
    }

    private void mettreAJourAffichage() {
        tvTotal.setText(String.format("Total : %.2f€", totalCommande));
        tvSolde.setText(String.format("Solde : %.2f€", totalCommande));
    }

    private void togglePaymentOptions() {
        if (paymentOptions.getVisibility() == View.VISIBLE) {
            paymentOptions.setVisibility(View.GONE);
            tvSaisieMontant.setVisibility(View.GONE);
            tvSaisieMontant.setText("");
            onMontantSaisi("0");


            btnTogglePayment.setBackgroundResource(R.drawable.arrow_down);
        } else {
            paymentOptions.setVisibility(View.VISIBLE);
            tvSaisieMontant.setVisibility(View.VISIBLE);
            btnTogglePayment.setBackgroundResource(R.drawable.arrow_up);
        }
    }
}
