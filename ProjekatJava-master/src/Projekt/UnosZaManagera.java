package Projekt;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.NumberFormat;
import java.text.ParseException;

public class UnosZaManagera extends ValidityCheck{
    private JTextField textField1; // Username field
    private JTextField textField2; // Email field
    private JTextField textField3; // Project field
    private JPasswordField passwordField1; // Password field
    private JTextField textField4; // Full name field
    private JTextField textField5; // Role field (Employee, Manager)
    private JTextField textField6; // Salary field
    private JButton nazadButton; // Back button
    private JButton kreirajButton; // Create button
    private JPanel panel1;
    private Connection connection;
    private String loggedInUsername;
    // Constructor for UnosZaManagera
    public UnosZaManagera(String username) {
        this.loggedInUsername=username;
        // Initialize your connection here
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/hrms", "root", "Benswolo#1");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Greška prilikom povezivanja sa bazom.");
        }

        // Add ActionListener to the "Kreiraj" button
        kreirajButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUser();
            }
        });

        // Add ActionListener to the "Nazad" button (if needed)
        nazadButton.addActionListener(e -> {
            // Close the current frame (UnosZaManagera)
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
            if (frame != null) {
                frame.dispose();
            }

            // Provide the required argument to the ManagerForm constructor
            String Username = loggedInUsername; // Replace with the actual value you need
            ManagerForm managerForm = new ManagerForm(Username);
            managerForm.showForm();
        });


    }

    // Method to add a new user
    private void addUser() {
        String username = textField1.getText(); // Get username
        String email = textField2.getText(); // Get email
        String project = textField3.getText(); // Get project

        String password = new String(passwordField1.getPassword()); // Get password
        String role = textField4.getText(); // Get role (Employee, Manager)
        String fullName = textField5.getText(); // Get full name
        String salaryStr = textField6.getText(); // Get salary

        // Validation checks for role and email
        if (!isRoleValid(role)) {
            JOptionPane.showMessageDialog(panel1, "Unesena uloga nije validna.");
            return;
        }
        if (!isEmailValid(email)) {
            JOptionPane.showMessageDialog(panel1, "Uneseni E-mail nije validan.");
            return;
        }

        // Parse salary string to double
        try {
            NumberFormat format = NumberFormat.getInstance();
            Number number = format.parse(salaryStr);
            double salary = number.doubleValue();

            // Insert user into the korisnici table
            String insertKorisnici = "INSERT INTO korisnici (username, email, password, role, project) VALUES (?, ?, ?, ?, ?)";
            String insertDetails = "INSERT INTO korisnik_details (korisnik_id, fullName, salary) VALUES (LAST_INSERT_ID(), ?, ?)";

            PreparedStatement psKorisnici = connection.prepareStatement(insertKorisnici);
            psKorisnici.setString(1, username);
            psKorisnici.setString(2, email);
            psKorisnici.setString(3, password);
            psKorisnici.setString(4, role);
            psKorisnici.setString(5, project);
            psKorisnici.executeUpdate();

            // Insert user details into korisnik_details table
            PreparedStatement psDetails = connection.prepareStatement(insertDetails);
            psDetails.setString(1, fullName);
            psDetails.setDouble(2, salary);
            psDetails.executeUpdate();

            psKorisnici.close();
            psDetails.close();

            JOptionPane.showMessageDialog(panel1, "Uspješno ste dodali korisnika!");


        } catch (SQLException | NumberFormatException | ParseException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom dodavanja korisnika.");
        }
    }


    public void showForm() {

        JFrame frame = new JFrame("Unos Za Managera");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 700);
        frame.setVisible(true);
    }


}
