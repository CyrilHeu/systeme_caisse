package com.example.tablettegourmande;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.json.JSONObject;
import java.io.InputStream;

public class FirebaseManager {

    private static FirebaseApp currentApp = null;

    public static void initializeFirebase(Context context, String environment) throws Exception {
        try {
            System.out.println("Initializing Firebase for environment: " + environment);
            // Déterminez le fichier JSON à utiliser
            String configFile = environment.equals("dev") ? "google-services-dev.json" : "google-services-prod.json";

            // Chargez le fichier JSON depuis les assets
            InputStream inputStream = context.getAssets().open(configFile);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            // Parsez le fichier JSON
            JSONObject jsonConfig = new JSONObject(new String(buffer));
            // Configurez FirebaseOptions
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setProjectId(jsonConfig.getJSONObject("project_info").getString("project_id"))
                    .setApplicationId(jsonConfig.getJSONArray("client").getJSONObject(0)
                            .getJSONObject("client_info").getString("mobilesdk_app_id"))
                    .setApiKey(jsonConfig.getJSONArray("client").getJSONObject(0)
                            .getJSONArray("api_key").getJSONObject(0).getString("current_key"))
                    .setStorageBucket(jsonConfig.getJSONObject("project_info").getString("storage_bucket"))
                    .build();

            // Supprimez l'instance Firebase actuelle si nécessaire
            if (FirebaseApp.getApps(context).size() > 0) {
                FirebaseApp.getInstance().delete();
            }

            FirebaseApp.initializeApp(context, options);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing Firebase: " + e.getMessage());
        }
    }

}
