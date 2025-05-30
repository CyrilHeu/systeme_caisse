package com.example.tablettegourmande.ui.GestionUtilisateurs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tablettegourmande.R;
import com.example.tablettegourmande.models.RolePermissionItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class RolePermissionAdapter extends RecyclerView.Adapter<RolePermissionAdapter.ViewHolder> {

    private final List<RolePermissionItem> permissions;
    private final List<String> roleIds;
    private final String restaurantId;
    private final Context context;

    public RolePermissionAdapter(Context context, List<RolePermissionItem> permissions, List<String> roleIds, String restaurantId) {
        this.context = context;
        this.permissions = permissions;
        this.roleIds = roleIds;
        this.restaurantId = restaurantId;
    }

    @NonNull
    @Override
    public RolePermissionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_permission_role, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RolePermissionAdapter.ViewHolder holder, int position) {
        RolePermissionItem item = permissions.get(position);
        holder.permissionLabel.setText(item.getPermissionLabel());
        holder.switchContainer.removeAllViews();

        for (String roleId : roleIds) {
            Switch sw = new Switch(context);
            sw.setText(roleId);
            boolean value = item.getStatusForRole(roleId);
            sw.setChecked(value);

            sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                FirebaseFirestore.getInstance()
                        .collection("restaurants")
                        .document(restaurantId)
                        .collection("roles")
                        .document(roleId)
                        .update("permissions." + item.getPermissionLabel(), isChecked);
            });

            holder.switchContainer.addView(sw);
        }
    }

    @Override
    public int getItemCount() {
        return permissions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView permissionLabel;
        LinearLayout switchContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            permissionLabel = itemView.findViewById(R.id.textPermissionLabel);
            switchContainer = itemView.findViewById(R.id.switchContainer);
        }
    }
}
