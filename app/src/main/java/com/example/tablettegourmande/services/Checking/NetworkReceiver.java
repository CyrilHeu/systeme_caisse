package com.example.tablettegourmande.services.Checking;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import Utils.CustomToast;

public class NetworkReceiver extends BroadcastReceiver {

    private final Context context;
    private ProgressDialog progressDialog;

    public NetworkReceiver(Context context) {
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            handleNetworkChange(isConnected);
        }
    }

    private void handleNetworkChange(boolean isConnected) {
        if (isConnected) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            CustomToast.show(context, "Connexion rétablie", 0);
        } else {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Problème de connexion réseau...");
                progressDialog.setCancelable(false);
            }
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        }
    }
}
