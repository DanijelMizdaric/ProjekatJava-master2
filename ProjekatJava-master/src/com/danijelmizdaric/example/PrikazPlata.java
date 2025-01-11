package com.danijelmizdaric.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class PrikazPlata {
    private JTable table1;
    private JPanel panel1;
    private JButton nazadButton;
    private String loggedInUsername;
    private String employeeUsername;
    private String userRole;
    private Connection connection;

    public PrikazPlata(String username, String employeeRole) {
        this.loggedInUsername = username;

        this.userRole = employeeRole;


        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/hrms", "root", "Benswolo#1");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Greška prilikom povezivanja sa bazom.");
        }

        loadSalaryHistoryKorisnik();

        nazadButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
            if (frame != null) {
                frame.dispose();
            }
            if ("Employee".equalsIgnoreCase(userRole)) {
                KorisnikForm korisnikForm = new KorisnikForm();
                korisnikForm.showForm();
            } else {
                JOptionPane.showMessageDialog(null, "Nepoznata uloga");
            }
        });
    }

    public PrikazPlata(String username, String employeeUsername, String employeeRole){
        this.loggedInUsername = username;
        this.employeeUsername = employeeUsername;
        this.userRole = employeeRole;
        System.out.println("Employee Username (ID): " + employeeUsername);
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/hrms", "root", "Benswolo#1");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Greška prilikom povezivanja sa bazom.");
        }

        loadSalaryHistory();

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

    private void loadSalaryHistory() {
        try {
            String query = "SELECT old_salary, new_salary, change_date FROM historija_plata WHERE korisnik_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, employeeUsername);
            ResultSet rs = ps.executeQuery();

            DefaultTableModel salaryModel = new DefaultTableModel();
            salaryModel.addColumn("Stara plata");
            salaryModel.addColumn("Nova plata");
            salaryModel.addColumn("Datum");

            while (rs.next()) {
                salaryModel.addRow(new Object[]{
                        rs.getDouble("old_salary"),
                        rs.getDouble("new_salary"),
                        rs.getTimestamp("change_date")
                });
            }
            table1.setModel(salaryModel);

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom učitavanja historije plata.");
        }
    }

    private void loadSalaryHistoryKorisnik() {
        try {
            String idQuery = "SELECT id FROM korisnici WHERE username = ?";
            PreparedStatement idStatement = connection.prepareStatement(idQuery);
            idStatement.setString(1, loggedInUsername);
            ResultSet idResultSet = idStatement.executeQuery();

            if (!idResultSet.next()) {
                JOptionPane.showMessageDialog(panel1, "Nije pronađen korisnik s ovim korisničkim imenom.");
                return;
            }
            int employeeId = idResultSet.getInt("id");

            String query = "SELECT old_salary, new_salary, change_date FROM historija_plata WHERE korisnik_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();

            DefaultTableModel salaryModel = new DefaultTableModel();
            salaryModel.addColumn("Stara plata");
            salaryModel.addColumn("Nova plata");
            salaryModel.addColumn("Datum");

            while (rs.next()) {
                salaryModel.addRow(new Object[]{
                        rs.getDouble("old_salary"),
                        rs.getDouble("new_salary"),
                        rs.getTimestamp("change_date")
                });
            }

            table1.setModel(salaryModel);

            rs.close();
            ps.close();
            idResultSet.close();
            idStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom učitavanja historije plata.");
        }
    }

    public void showForm() {
        JFrame frame = new JFrame("Prikaz Plata");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setVisible(true);
    }
}