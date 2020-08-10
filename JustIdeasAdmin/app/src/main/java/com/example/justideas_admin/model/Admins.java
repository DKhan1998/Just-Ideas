package com.example.justideas_admin.model;

public class Admins {
    private String Name, AdminID, Password;

    public Admins()
    {

    }

    public Admins(String Name, String AdminID, String Password) {
        this.Name = Name;
        this.AdminID = AdminID;
        this.Password = Password;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getAdminID() {
        return AdminID;
    }

    public void setAdminID(String AdminID) {
        this.AdminID = AdminID;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String Password) {
        this.Password = Password;
    }
}
