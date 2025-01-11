package com.danijelmizdaric.example;

import javax.swing.*;
import java.sql.*;
import java.time.LocalDate;

public class PisiZalbu {
    private JButton unesiButton;
    private JButton nazadButton;
    private JTextField textField1;
    private JPanel panel1;
    private Connection connection;
    private String loggedInUsername;
    private String employeeUsername;
    private String userRole;

    public PisiZalbu(String Username, String employeeName, String employeeRole) {
        this.loggedInUsername = Username;
        this.employeeUsername = employeeName;
        this.userRole = employeeRole;
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/hrms", "root", "Benswolo#1");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Greška prilikom povezivanja sa bazom.");
        }

        unesiButton.addActionListener(e -> writeComplaint());
        nazadButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
            if (frame != null) {
                frame.dispose();
            }
            if ("SuperAdmin".equalsIgnoreCase(userRole)) {//otvara formu zavisno od loggovanog usera
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

    private void writeComplaint() {
        String complaintText = textField1.getText();
        if (complaintText != null && !complaintText.isEmpty()) {
            try {
                String insertComplaint = "INSERT INTO korisnik_zalbe " +
                        "(korisnik_id, complaint_text, complaint_date, status) VALUES (?, ?, ?, ?)";//upit za pisanje zalbe u bazu

                PreparedStatement psComplaint = connection.prepareStatement(insertComplaint);
                psComplaint.setInt(1, Integer.parseInt(employeeUsername));
                psComplaint.setString(2, complaintText);
                psComplaint.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                psComplaint.setString(4, "u obradi");
                psComplaint.executeUpdate();//pisanje zalbe
                psComplaint.close();

                JOptionPane.showMessageDialog(panel1, "Žalba uspješno dodana.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel1, "Greška prilikom dodavanja žalbe.");
            }
        } else {
            JOptionPane.showMessageDialog(panel1, "Žalba ne može biti prazna.");
        }
    }

    public void showForm() {
        JFrame frame = new JFrame("Pisi Zalbu");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setVisible(true);
    }
}
