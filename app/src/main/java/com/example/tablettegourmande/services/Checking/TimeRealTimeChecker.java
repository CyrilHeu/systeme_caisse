package com.example.tablettegourmande.services.Checking;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TimeRealTimeChecker {

    private static final String TIME_API_URL = "https://aperichill.fr/api/getSynchronisedTime.php";
    private static final int ALLOWED_OFFSET_SECONDS = 300;
    private static final int CHECK_INTERVAL = 10000; // 10 secondes

    private final Context context;
    private final Handler handler;
    private ProgressDialog progressDialog;
    private boolean isChecking = false;

    public TimeRealTimeChecker(Context context) {
        this.context = context;
        this.handler = new Handler();
    }

    public void startChecking() {
        isChecking = true;

        // Afficher un ProgressDialog bloquant si une désynchronisation est détectée
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Vérification de l'heure en cours...");
        progressDialog.setCancelable(false);

        // Lancer la première vérification
        handler.post(checkTimeTask);
    }

    public void stopChecking() {
        isChecking = false;
        handler.removeCallbacks(checkTimeTask);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private final Runnable checkTimeTask = new Runnable() {
        @Override
        public void run() {
            if (!isChecking) return;

            // Effectuer une requête pour vérifier l'heure
            new Thread(() -> {
                try {
                    URL url = new URL(TIME_API_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        String[] parts = response.toString().split("<br>");
                        //Log.d("heure1", parts[0]);
                        if (parts.length >= 2) {
                            long serverTimestamp = Long.parseLong(parts[0]);
                            boolean isSynchronized = isTimeSynchronized(serverTimestamp);

                            // Mettre à jour l'interface utilisateur sur le thread principal
                            handler.post(() -> {
                                if (isSynchronized) {
                                    if (progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    //Toast.makeText(context, "L'heure est synchronisée.", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (!progressDialog.isShowing()) {
                                        progressDialog.show();
                                    }
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // Planifier la prochaine vérification
            handler.postDelayed(this, CHECK_INTERVAL);
        }
    };

    private boolean isTimeSynchronized(long serverTimestamp) {
        // Récupérer l'heure actuelle de la tablette
        long localTimestamp = System.currentTimeMillis() / 1000L;

        // Calculer la différence
        long difference = Math.abs(localTimestamp - serverTimestamp);
        //Log.d("heure1", "difference"+difference);
        // Vérifier si la différence dépasse le seuil autorisé
        return difference <= ALLOWED_OFFSET_SECONDS;
    }
}
