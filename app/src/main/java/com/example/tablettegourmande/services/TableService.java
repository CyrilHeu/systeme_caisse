package com.example.tablettegourmande.services;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class TableService extends ServiceBase {

    private final String restaurantId;
    private ListenerRegistration currentTableListener;
    public TableService(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public interface CurrentTableCallback {
        void onResult(String tableValue);
        void onError(Exception e);
    }

    public void checkOrInitCurrentTable(CurrentTableCallback callback) {
        if (restaurantId == null || restaurantId.isEmpty()) {
            callback.onError(new IllegalStateException("restaurantId est null ou vide"));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference restaurantDoc = db.collection("restaurants").document(restaurantId);

        restaurantDoc.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                callback.onError(new Exception("Le document restaurant n'existe pas"));
                return;
            }

            if (snapshot.contains("current_table")) {
                String currentTable = snapshot.getString("current_table");
                callback.onResult(currentTable != null ? currentTable : "direct");
            } else {
                restaurantDoc.update("current_table", "direct")
                        .addOnSuccessListener(aVoid -> callback.onResult("direct"))
                        .addOnFailureListener(callback::onError);
            }
        }).addOnFailureListener(callback::onError);
    }
    public void setCurrentTable(String newValue, TableUpdateCallback callback) {
        if (restaurantId == null || restaurantId.isEmpty()) {
            callback.onError(new IllegalStateException("restaurantId vide"));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("restaurants").document(restaurantId);

        docRef.update("current_table", newValue)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }



    public void getOpenedTables(OpenedTablesCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference tablesRef = db
                .collection("restaurants")
                .document(restaurantId)
                .collection("tables_ouvertes");

        tablesRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> tableNumbers = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        tableNumbers.add(doc.getId()); // ou doc.getString("numero")
                    }
                    callback.onSuccess(tableNumbers);
                })
                .addOnFailureListener(callback::onError);
    }

    public void addOpenedTable(String tableNumber) {
        if (restaurantId == null || restaurantId.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference tableDoc = db
                .collection("restaurants")
                .document(restaurantId)
                .collection("tables_ouvertes")
                .document(tableNumber);

        Map<String, Object> data = new HashMap<>();
        data.put("numero", tableNumber);
        data.put("status", "ouverte");
        data.put("timestamp", FieldValue.serverTimestamp());

        // Calcul de l'offset UTC actuel
        TimeZone tz = TimeZone.getDefault();  // ou TimeZone.getTimeZone("Europe/Paris") si tu veux forcer
        int offsetMs = tz.getOffset(System.currentTimeMillis());
        int offsetHours = offsetMs / (1000 * 60 * 60);
        String offsetStr = String.format("UTC%+d", offsetHours);

        data.put("utc_offset", offsetStr);  // ajout du champ UTC

        tableDoc.set(data, SetOptions.merge());
    }



    public void startCurrentTableListener(CurrentTableListener listener) {
        if (restaurantId == null || restaurantId.isEmpty()) {
            listener.onError(new IllegalStateException("restaurantId vide"));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference restaurantDoc = db.collection("restaurants").document(restaurantId);

        currentTableListener = restaurantDoc.addSnapshotListener((snapshot, error) -> {
            if (error != null || snapshot == null || !snapshot.exists()) {
                listener.onError(error != null ? error : new Exception("Document restaurant invalide"));
                return;
            }

            String currentTable = snapshot.getString("current_table");
            if (currentTable != null) {
                listener.onTableChanged(currentTable);
            }
        });
    }

    public void getTableOpeningTimestamp(String tableNumber, TableTimestampCallback callback) {
        if (restaurantId == null || restaurantId.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference tableDoc = db
                .collection("restaurants")
                .document(restaurantId)
                .collection("tables_ouvertes")
                .document(tableNumber);

        tableDoc.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Timestamp timestamp = documentSnapshot.getTimestamp("timestamp");
                        String utcOffset = documentSnapshot.getString("utc_offset");
                        callback.onSuccess(timestamp, utcOffset);
                    } else {
                        callback.onFailure(new Exception("Document table non trouvé"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public interface TableTimestampCallback {
        void onSuccess(Timestamp timestamp, String utcOffset);
        void onFailure(Exception e);
    }


    public void stopCurrentTableListener() {
        if (currentTableListener != null) {
            currentTableListener.remove();
            currentTableListener = null;
        }
    }
    public interface CurrentTableListener {
        void onTableChanged(String tableName);
        void onError(Exception e);
    }
    public interface TableUpdateCallback {
        void onSuccess();
        void onError(Exception e);
    }
    public interface OpenedTablesCallback {
        void onSuccess(List<String> openedTables);
        void onError(Exception e);
    }

}


