package com.danijelmizdaric.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class KorisnikForm {
    private JPanel panel1;
    private JButton BackButton;
    private JTable PrikazInfo;
    private JButton prikaziHistorijuPlateButton;
    private String loggedInUsername;
    private String userRole;
    public KorisnikForm() {
        this.loggedInUsername = UserSession.getUsername();
        this.userRole=UserSession.getRole();

        BackButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
            if (frame != null) {
                frame.dispose();
            }
            new LoginForm().showForm();
        });

        prikaziHistorijuPlateButton.addActionListener(e-> showSalaryHistory());
        loadUserData();
    }

    private void loadUserData() {
        String[] columnNames = {"ID", "Username", "Email", "Role", "Project", "Full Name", "Salary"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        PrikazInfo.setModel(model);
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/hrms", "root", "Benswolo#1");
            String query = "SELECT k.id, k.username, k.email, k.role, k.project, d.fullName, d.salary " +
                    "FROM korisnici k JOIN korisnik_details d ON k.id = d.korisnik_id " +
                    "WHERE k.username = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, loggedInUsername);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String username = resultSet.getString("username");
                String email = resultSet.getString("email");
                String role = resultSet.getString("role");
                String project = resultSet.getString("project");
                String fullName = resultSet.getString("fullName");
                double salary = resultSet.getDouble("salary");
                model.addRow(new Object[]{id, username, email, role, project, fullName, salary});
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom učitavanja podataka.");
        }
    }

    private void showSalaryHistory() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
        if (frame != null) {
            frame.dispose();
        }

        if (loggedInUsername != null) {
            new PrikazPlata(loggedInUsername, userRole).showForm();
        } else {
            JOptionPane.showMessageDialog(null, "Greška pri dohvaćanju podataka korisnika.");
        }
    }

    public void showForm() {
        JFrame frame = new JFrame("Korisnik Form");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 700);
        frame.setVisible(true);
    }
}
