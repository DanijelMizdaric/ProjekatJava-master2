package com.danijelmizdaric.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class SuperAdminForm extends ValidityCheck{
    private JTable PrikazTabela;
    private JPanel panel1;
    private JButton nazadButton;
    private JButton dodajButton;
    private JButton izbrisiButton;
    private JButton azurirajButton;
    private JButton prikaziPlateButton;
    private JButton prikaziZalbuButton;
    private JButton napisiZalbuButton;
    private Connection connection;
    private DefaultTableModel tableModel;
    private String loggedInUsername;
    private String userRole;

    public SuperAdminForm(String username) {
        this.loggedInUsername=username;
        this.userRole = UserSession.getRole();
        tableModel = new DefaultTableModel(new String[]{"ID", "Username", "Email", "Role", "Full Name", "Salary"}, 0);
        PrikazTabela.setModel(tableModel);
        loadData();

        nazadButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
            if (frame != null) {
                frame.dispose();
            }
            new LoginForm().showForm();
        });

        dodajButton.addActionListener(e -> addUser());
        izbrisiButton.addActionListener(e -> deleteUser());
        azurirajButton.addActionListener(e -> updateUser());
        napisiZalbuButton.addActionListener(e-> writeComplaint());
        prikaziZalbuButton.addActionListener(e-> showComplaint());

        prikaziPlateButton.addActionListener(e -> {
            int selectedRow = PrikazTabela.getSelectedRow();
            selectedUser(selectedRow);
            int korisnikId = (int) tableModel.getValueAt(selectedRow, 0);
            showSalaryHistory(korisnikId);
        });
    }

    private void loadData() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/hrms", "root", "Benswolo#1");
            String query = "SELECT k.id, k.username, k.email, k.role, d.fullName, d.salary " +
                    "FROM korisnici k JOIN korisnik_details d ON k.id = d.korisnik_id " +
                    "WHERE k.role != 'superadmin'";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            tableModel.setRowCount(0);

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String username = resultSet.getString("username");
                String email = resultSet.getString("email");
                String role = resultSet.getString("role");
                String fullName = resultSet.getString("fullName");
                double salary = resultSet.getDouble("salary");
                tableModel.addRow(new Object[]{id, username, email, role, fullName, salary});
            }

            resultSet.close();
            statement.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom učitavanja podataka.");
        }
    }

    private void addUser() {
    new UnosKorisnika(loggedInUsername,userRole).showForm();
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
        frame.dispose();
    }

    private void deleteUser() {
        int selectedRow = PrikazTabela.getSelectedRow();
        selectedUser(selectedRow);
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            String deleteDetails = "DELETE FROM korisnik_details WHERE korisnik_id = ?";
            String deleteZalbe = "DELETE FROM korisnik_zalbe WHERE korisnik_id = ?";
            String deleteKorisnici = "DELETE FROM korisnici WHERE id = ?";

            PreparedStatement psDetails = connection.prepareStatement(deleteDetails);
            psDetails.setInt(1, id);
            psDetails.executeUpdate();
            psDetails.close();

            PreparedStatement psZalbe = connection.prepareStatement(deleteZalbe);
            psZalbe.setInt(1, id);
            psZalbe.executeUpdate();
            psZalbe.close();

            PreparedStatement psKorisnici = connection.prepareStatement(deleteKorisnici);
            psKorisnici.setInt(1, id);
            psKorisnici.executeUpdate();
            psKorisnici.close();

            JOptionPane.showMessageDialog(panel1, "Korisnik je uspješno izbrisan.");
            loadData();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom brisanja korisnika.");
        }
    }

    private void updateUser() {
        String selectedEmployeeUsername = getSelectedEmployeeName();
        if (selectedEmployeeUsername != null) {
            new AzuriranjeSuperAdmin(loggedInUsername, selectedEmployeeUsername).showForm();
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
            frame.dispose();
        } else {
            JOptionPane.showMessageDialog(panel1, "Morate odabrati korisnika prvo.");
        }
    }

    private void showSalaryHistory(int korisnikId) {
        String selectedEmployeeUsername = getSelectedEmployeeName();
        if (selectedEmployeeUsername != null) {
            new PrikazPlata(loggedInUsername, selectedEmployeeUsername,userRole).showForm();
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
            frame.dispose();
        } else {
            JOptionPane.showMessageDialog(panel1, "Morate odabrati korisnika prvo.");
        }
    }

    private void writeComplaint(){
        String selectedEmployeeUsername = getSelectedEmployeeName();
        if (selectedEmployeeUsername!=null) {
            new PisiZalbu(loggedInUsername, selectedEmployeeUsername,userRole).showForm();
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
            frame.dispose();
        }else {
            JOptionPane.showMessageDialog(panel1, "Morate odabrat korisnika prvo.");
        }
    }

    private void showComplaint(){
        String selectedEmployeeUsername = getSelectedEmployeeName();
        if (selectedEmployeeUsername!=null) {
            new PrikaziZalbu(loggedInUsername, selectedEmployeeUsername,userRole).showForm();
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
            frame.dispose();
        } else {
            JOptionPane.showMessageDialog(panel1, "Morate odabrat korisnika prvo.");
        }
    }

    private String getSelectedEmployeeName() {
        int selectedRow = PrikazTabela.getSelectedRow();
        if (selectedRow != -1) {
            return PrikazTabela.getValueAt(selectedRow, 0).toString();
        }
        return null;
    }

    public void showForm() {
            JFrame frame = new JFrame("SuperAdmin Form");
            frame.setContentPane(panel1);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 700);
            frame.setVisible(true);
    }
}
