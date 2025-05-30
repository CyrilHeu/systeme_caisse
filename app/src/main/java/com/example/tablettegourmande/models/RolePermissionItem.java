package com.example.tablettegourmande.models;

import java.util.HashMap;
import java.util.Map;

public class RolePermissionItem {
    private String permissionLabel;
    private Map<String, Boolean> roleStatus;

    public RolePermissionItem(String permissionLabel) {
        this.permissionLabel = permissionLabel;
        this.roleStatus = new HashMap<>();
    }

    public String getPermissionLabel() {
        return permissionLabel;
    }

    public void setPermissionLabel(String permissionLabel) {
        this.permissionLabel = permissionLabel;
    }

    public Map<String, Boolean> getRoleStatus() {
        return roleStatus;
    }

    public void setRoleStatus(Map<String, Boolean> roleStatus) {
        this.roleStatus = roleStatus;
    }

    public void setStatusForRole(String roleId, boolean value) {
        roleStatus.put(roleId, value);
    }

    public Boolean getStatusForRole(String roleId) {
        return roleStatus.getOrDefault(roleId, false);
    }
}
