package com.danijelmizdaric.example;

import javax.swing.*;
import java.sql.*;
import java.text.NumberFormat;
import java.text.ParseException;


public class AzuriranjeSuperAdmin extends ValidityCheck{
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField5;
    private JTextField textField6;
    private JPasswordField passwordField1;
    private JTextField textField4;
    private JButton nazadButton;
    private JButton azurirajButton;
    private JPanel panel1;
    private String loggedInUsername;
    private Connection connection;
    private String employeeUsername;
    private double oldSalary;

    public AzuriranjeSuperAdmin(String Username, String employeeName){
        this.loggedInUsername = Username;
        this.employeeUsername=employeeName;
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/hrms", "root", "Benswolo#1");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Greška prilikom povezivanja sa bazom.");
        }
        fetchEmployeeData();

        azurirajButton.addActionListener(e -> updateEmployee());

        nazadButton.addActionListener(e -> {

            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
            if (frame != null) {
                frame.dispose();
            }
            SuperAdminForm superAdminForm = new SuperAdminForm(loggedInUsername);
            superAdminForm.showForm();
        });

    }
    private void fetchEmployeeData() {
        String query = "SELECT username, email, project, password, role, fullName, salary " +
                "FROM korisnici " +
                "JOIN korisnik_details ON korisnici.id = korisnik_details.korisnik_id " +
                "WHERE korisnici.id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(employeeUsername));
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                textField1.setText(resultSet.getString("username"));
                textField2.setText(resultSet.getString("email"));
                textField3.setText(resultSet.getString("project"));
                passwordField1.setText(resultSet.getString("password"));
                textField5.setText(resultSet.getString("role"));
                textField6.setText(resultSet.getString("fullName"));
                textField4.setText(String.valueOf(resultSet.getDouble("salary")));
                oldSalary = resultSet.getDouble("salary");
            } else {
                System.out.println("Podaci nisu pronadjeni.");
            }

            resultSet.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom učitavanja podataka zaposlenika.");
        }
    }

    private void updateEmployee() {
        String username = textField1.getText();
        String email = textField2.getText();
        String project = textField3.getText();
        String password = new String(passwordField1.getPassword());
        String role = textField5.getText();
        String fullName = textField6.getText();
        String salaryStr = textField4.getText();

        if (!isRoleValid(role)) {
            JOptionPane.showMessageDialog(panel1, "Unesena uloga nije validna.");
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

            String updateEmployee = "UPDATE korisnici SET email = ?, project = ?, role = ?, password = ? WHERE username = ?";
            String updateDetails = "UPDATE korisnik_details SET fullName = ?, salary = ? WHERE korisnik_id = (SELECT id FROM korisnici WHERE username = ?)";
            String insertSalaryHistory = "INSERT INTO historija_plata (korisnik_id, old_salary, new_salary, change_date) " +
                    "VALUES ((SELECT id FROM korisnici WHERE username = ?), ?, ?, ?)";

            PreparedStatement psEmployee = connection.prepareStatement(updateEmployee);
            psEmployee.setString(1, email);
            psEmployee.setString(2, project);
            psEmployee.setString(3, role);
            psEmployee.setString(4, password);
            psEmployee.setString(5, username);
            psEmployee.executeUpdate();

            PreparedStatement psDetails = connection.prepareStatement(updateDetails);
            psDetails.setString(1, fullName);
            psDetails.setDouble(2, salary);
            psDetails.setString(3, username);
            psDetails.executeUpdate();

            PreparedStatement psSalaryHistory = connection.prepareStatement(insertSalaryHistory);
            psSalaryHistory.setString(1, username);
            psSalaryHistory.setDouble(2, oldSalary);
            psSalaryHistory.setDouble(3, salary);
            psSalaryHistory.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            psSalaryHistory.executeUpdate();

            psEmployee.close();
            psDetails.close();

            JOptionPane.showMessageDialog(panel1, "Uspješno ste azurirali zaposlenika!");

        } catch (SQLException | NumberFormatException | ParseException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom ažuriranja zaposlenika.");
        }
    }

    public void showForm() {
        JFrame frame = new JFrame("Azuriranje Zaposlenika");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 700);
        frame.setVisible(true);
    }
}