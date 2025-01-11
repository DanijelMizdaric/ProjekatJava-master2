package com.danijelmizdaric.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class PrikaziZalbu {
    private JTable table1;
    private JPanel panel1;
    private JButton oznaciKaoZavrsenoButton;
    private JButton nazadButton;
    private Connection connection;
    private String loggedInUsername;
    private String employeeUsername;
    private String userRole;

    public PrikaziZalbu(String username, String employeeName, String role) {
        this.loggedInUsername = username;
        this.employeeUsername = employeeName;
        this.userRole = role;
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/hrms", "root", "Benswolo#1");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Greška prilikom povezivanja sa bazom.");
        }

        loadComplaints();

        oznaciKaoZavrsenoButton.addActionListener(e -> markComplaintAsCompleted());
        nazadButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
            if (frame != null) {
                frame.dispose();
            }
            if ("SuperAdmin".equalsIgnoreCase(userRole)) {
                SuperAdminForm superAdminForm = new SuperAdminForm(loggedInUsername);
                superAdminForm.showForm();
            } else if ("Manager".equalsIgnoreCase(userRole)) {
                ManagerForm managerForm = new ManagerForm(loggedInUsername);
                managerForm.showForm();
            } else {
                JOptionPane.showMessageDialog(null, "Nepoznata uloga");
            }
        });
    }

    private void loadComplaints() {
        try {
            String query = "SELECT id, complaint_text, complaint_date, status FROM korisnik_zalbe WHERE korisnik_id = ?";
            PreparedStatement psComplaints = connection.prepareStatement(query);
            psComplaints.setString(1, employeeUsername);
            ResultSet rs = psComplaints.executeQuery();

            DefaultTableModel complaintModel = new DefaultTableModel();
            complaintModel.addColumn("ID");
            complaintModel.addColumn("Žalba");
            complaintModel.addColumn("Datum");
            complaintModel.addColumn("Status");
            while (rs.next()) {
                complaintModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("complaint_text"),
                        rs.getDate("complaint_date"),
                        rs.getString("status")
                });
            }
            table1.setModel(complaintModel);

            rs.close();
            psComplaints.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom učitavanja žalbi.");
        }
    }

    private void markComplaintAsCompleted() {
        int selectedRow = table1.getSelectedRow();
        if (selectedRow ==-1) {
            JOptionPane.showMessageDialog(panel1, "Molimo odaberite žalbu iz tabele.");
            return;
        }
        int complaintId = (int) table1.getValueAt(selectedRow, 0);
        try {
            String updateQuery = "UPDATE korisnik_zalbe SET status = ? WHERE id = ?";
            PreparedStatement psUpdate = connection.prepareStatement(updateQuery);
            psUpdate.setString(1, "završeno");
            psUpdate.setInt(2, complaintId);
            int rowsUpdated = psUpdate.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(panel1, "Status žalbe je uspješno ažuriran na 'završeno'.");
                loadComplaints();
            } else {
                JOptionPane.showMessageDialog(panel1, "Greška prilikom ažuriranja statusa žalbe.");
            }

            psUpdate.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom ažuriranja statusa žalbe.");
        }
    }

    public void showForm() {
        JFrame frame = new JFrame("Prikaz Žalbi");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setVisible(true);
    }
}
