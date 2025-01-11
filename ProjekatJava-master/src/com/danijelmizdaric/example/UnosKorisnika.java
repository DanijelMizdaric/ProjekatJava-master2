package com.danijelmizdaric.example;

import javax.swing.*;
import java.sql.*;
import java.text.NumberFormat;
import java.text.ParseException;

public class UnosKorisnika extends ValidityCheck{
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JPasswordField passwordField1;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JButton nazadButton;
    private JButton kreirajButton;
    private JPanel panel1;
    private Connection connection;
    private String loggedInUsername;
    private String userRole;

    public UnosKorisnika(String username, String employeeRole) {
        this.loggedInUsername=username;
        this.userRole=employeeRole;
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/hrms", "root", "Benswolo#1");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Greška prilikom povezivanja sa bazom.");
        }

        kreirajButton.addActionListener(e -> addUser());

        nazadButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
            if (frame != null) {
                frame.dispose();
            }

            String Username = loggedInUsername;
            String EmRole = userRole;
            if ("Manager".equalsIgnoreCase(userRole)) { //otvara razlicite forme zavisno od uloge korisnika
                ManagerForm managerForm = new ManagerForm(Username);
                managerForm.showForm();
            } else if ("SuperAdmin".equalsIgnoreCase(userRole)){
                SuperAdminForm superAdminForm = new SuperAdminForm(Username);
                superAdminForm.showForm();
            } else {
                JOptionPane.showMessageDialog(null, "Nepoznata uloga");
            }});
    }

    private void addUser() {
        String username = textField1.getText();
        String email = textField2.getText();
        String project = textField3.getText();
        String password = new String(passwordField1.getPassword());
        String role = textField4.getText();
        String fullName = textField5.getText();
        String salaryStr = textField6.getText();//uzimamo text sa gui-a

        if (!isRoleValid(role)) {
            JOptionPane.showMessageDialog(panel1, "Unesena uloga nije validna."); //validacija
            return;
        }
        if (!isEmailValid(email)) {
            JOptionPane.showMessageDialog(panel1, "Uneseni E-mail nije validan.");
            return;
        }
        try {
            NumberFormat format = NumberFormat.getInstance();
            Number number = format.parse(salaryStr);
            double salary = number.doubleValue();

            String insertKorisnici = "INSERT INTO korisnici (username, email, password, role, project) VALUES (?, ?, ?, ?, ?)";
            String insertDetails = "INSERT INTO korisnik_details (korisnik_id, fullName, salary) VALUES (LAST_INSERT_ID(), ?, ?)"; //upit sa pisanje u tabelu korisnici i korisnik_details

            PreparedStatement psKorisnici = connection.prepareStatement(insertKorisnici);
            psKorisnici.setString(1, username);
            psKorisnici.setString(2, email);
            psKorisnici.setString(3, password);
            psKorisnici.setString(4, role);
            psKorisnici.setString(5, project);
            psKorisnici.executeUpdate();

            PreparedStatement psDetails = connection.prepareStatement(insertDetails);
            psDetails.setString(1, fullName);
            psDetails.setDouble(2, salary);
            psDetails.executeUpdate(); //pisanje u tabele

            psKorisnici.close();
            psDetails.close();

            JOptionPane.showMessageDialog(panel1, "Uspješno ste dodali korisnika!");

        } catch (SQLException | NumberFormatException | ParseException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom dodavanja korisnika.");
        }
    }

    public void showForm() {
        JFrame frame = new JFrame("UnosKorisnika");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 700);
        frame.setVisible(true);
    }
}