package Projekt;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

        // Add ActionListener to the "Azuriraj" button
        azurirajButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateEmployee();
            }
        });

        // Add ActionListener to the "Nazad" button
        nazadButton.addActionListener(e -> {

            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
            if (frame != null) {
                frame.dispose();
            }
            SuperAdminForm superAdminForm = new SuperAdminForm(loggedInUsername);
            superAdminForm.showForm();
        });
    }private void fetchEmployeeData() {
        // Query to fetch data for the selected employee (not manager)
        String query = "SELECT username, email, project, role, fullName, salary " +
                "FROM korisnici " +
                "JOIN korisnik_details ON korisnici.id = korisnik_details.korisnik_id " +
                "WHERE korisnici.id = ?";


        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(employeeUsername)); // Use employeeUsername to fetch the employee's data

            System.out.println("Executing query for employee: " + employeeUsername);
            ResultSet resultSet = stmt.executeQuery();

            // Check if data is available
            if (resultSet.next()) {
                System.out.println("Data found"); // This will confirm if data was retrieved
                // Fill text fields with employee data
                textField1.setText(resultSet.getString("username"));
                textField2.setText(resultSet.getString("email"));
                textField3.setText(resultSet.getString("project"));
                textField4.setText(resultSet.getString("role"));
                textField5.setText(resultSet.getString("fullName"));
                textField6.setText(String.valueOf(resultSet.getDouble("salary")));
            } else {
                System.out.println("No data found for the query.");
            }

            resultSet.close();  // Close the resultSet
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom učitavanja podataka zaposlenika.");
        }
    }



    // Method to update employee data in the database
    private void updateEmployee() {
        String username = textField1.getText(); // Get username
        String email = textField2.getText(); // Get email
        String project = textField3.getText(); // Get project
        String role = textField4.getText(); // Get role (Employee)
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

        try {
            // Parse salary string to double
            NumberFormat format = NumberFormat.getInstance();
            Number number = format.parse(salaryStr);
            double salary = number.doubleValue();

            // Update employee data in the database
            String updateEmployee = "UPDATE korisnici SET email = ?, project = ?, role = ? WHERE username = ?";
            String updateDetails = "UPDATE korisnik_details SET fullName = ?, salary = ? WHERE korisnik_id = (SELECT id FROM korisnici WHERE username = ?)";

            PreparedStatement psEmployee = connection.prepareStatement(updateEmployee);
            psEmployee.setString(1, email);
            psEmployee.setString(2, project);
            psEmployee.setString(3, role);
            psEmployee.setString(4, username);
            psEmployee.executeUpdate();

            // Update employee details in korisnik_details table
            PreparedStatement psDetails = connection.prepareStatement(updateDetails);
            psDetails.setString(1, fullName);
            psDetails.setDouble(2, salary);
            psDetails.setString(3, username);
            psDetails.executeUpdate();

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


