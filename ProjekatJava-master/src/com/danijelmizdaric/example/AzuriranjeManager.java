package com.danijelmizdaric.example;

import javax.swing.*;
import java.sql.*;
import java.text.NumberFormat;
import java.text.ParseException;

public class AzuriranjeManager extends ValidityCheck {
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JButton azurirajButton;
    private JButton nazadButton;
    private String loggedInUsername;
    private Connection connection;
    private JPanel panel1;
    private String employeeUsername;
    private double oldSalary;

    public AzuriranjeManager(String username, String employeeName) {
        this.loggedInUsername = username;
        this.employeeUsername = employeeName;
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
            ManagerForm managerForm = new ManagerForm(loggedInUsername);
            managerForm.showForm();
        });
    }

    private void fetchEmployeeData() {
        String query = "SELECT username, email, project, role, fullName, salary " +
                "FROM korisnici " +
                "JOIN korisnik_details ON korisnici.id = korisnik_details.korisnik_id " +
                "WHERE korisnici.id = ?";//upit za dohvatanje podataka iz baze

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(employeeUsername));
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                textField1.setText(resultSet.getString("username"));
                textField2.setText(resultSet.getString("email"));
                textField3.setText(resultSet.getString("project"));
                textField4.setText(resultSet.getString("role"));
                textField5.setText(resultSet.getString("fullName"));
                textField6.setText(String.valueOf(resultSet.getDouble("salary"))); // upisivanje podataka unutar textfieldove
                oldSalary = resultSet.getDouble("salary");//spremanje trenutne plate u varijablue, u slucaju da ce se promjenit plata
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
        String role = textField4.getText();
        String fullName = textField5.getText();
        String salaryStr = textField6.getText(); //uzimanje texta iz textfield

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

            String updateEmployee = "UPDATE korisnici SET email = ?, project = ?, role = ? WHERE username = ?";
            String updateDetails = "UPDATE korisnik_details SET fullName = ?, salary = ? WHERE korisnik_id = (SELECT id FROM korisnici WHERE username = ?)";
            String insertSalaryHistory = "INSERT INTO historija_plata (korisnik_id, old_salary, new_salary, change_date) " +
                    "VALUES ((SELECT id FROM korisnici WHERE username = ?), ?, ?, ?)"; //upit za azuriranje tabela unutar baze, ukljucujuci i historija_plata

            PreparedStatement psEmployee = connection.prepareStatement(updateEmployee);
            psEmployee.setString(1, email);
            psEmployee.setString(2, project);
            psEmployee.setString(3, role);
            psEmployee.setString(4, username);
            psEmployee.executeUpdate();

            PreparedStatement psDetails = connection.prepareStatement(updateDetails);
            psDetails.setString(1, fullName);
            psDetails.setDouble(2, salary);
            psDetails.setString(3, username);
            psDetails.executeUpdate();

            PreparedStatement psSalaryHistory = connection.prepareStatement(insertSalaryHistory);
            psSalaryHistory.setString(1, username);
            psSalaryHistory.setDouble(2, oldSalary);
            psSalaryHistory.setDouble(3, salary);//upisivanje podataka
            psSalaryHistory.setTimestamp(4, new Timestamp(System.currentTimeMillis())); //upisivanje trenutnog vremena
            psSalaryHistory.executeUpdate();

            psEmployee.close();
            psDetails.close();
            psSalaryHistory.close();

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