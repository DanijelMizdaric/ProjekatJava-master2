package Projekt;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ValidityCheck {
    private Component panel1;

    protected boolean isRoleValid(String role) {
        return role.equalsIgnoreCase("Employee") ||
                role.equalsIgnoreCase("Manager");
    }

    // Proverava da li je uneseni e-mail u ispravnom formatu
    protected boolean isEmailValid(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return email.matches(emailRegex);
    }
    protected void selectedUser(int selectedRow){
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(panel1, "Molimo izaberite korisnika.");
            return;
        }
        
    }
}
