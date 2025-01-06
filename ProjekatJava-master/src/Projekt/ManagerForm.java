package Projekt;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;

public class ManagerForm extends ValidityCheck{
    private JTable PrikazTabela;
    private JPanel panel1;
    private JButton nazadButton;
    private JButton dodajButton;
    private JButton izbrisiButton;
    private JButton azurirajButton;
    private JButton prikazPlateButton;
    private JButton napisiZalbuButton;
    private JButton prikaziZalbuButton;
    private JButton statusZalbeButton;
    private Connection connection;
    private DefaultTableModel tableModel;
    public ManagerForm(){

        tableModel = new DefaultTableModel(new String[]{"ID", "Username", "Email", "Role", "Full Name", "Salary"}, 0);
        PrikazTabela.setModel(tableModel);


        loadData();

        nazadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
                frame.dispose();
                new LoginForm().showForm();
            }
        });
        // Akcija za dodavanje novog korisnika
        dodajButton.addActionListener(e -> addUser());

        // Akcija za brisanje korisnika
        izbrisiButton.addActionListener(e -> deleteUser());

        // Akcija za ažuriranje korisnika
        azurirajButton.addActionListener(e -> updateUser());

        napisiZalbuButton.addActionListener(e-> writeComplaint());

        prikaziZalbuButton.addActionListener(e-> showComplaint());

        statusZalbeButton.addActionListener(e->statusComplaint());

        prikazPlateButton.addActionListener(e -> {
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

            // Modifikovani upit da filtrira samo korisnike sa ulogom 'employee'
            String query = "SELECT k.id, k.username, k.email, k.role, d.fullName, d.salary " +
                    "FROM korisnici k JOIN korisnik_details d ON k.id = d.korisnik_id " +
                    "WHERE k.role = 'employee'"; // Filtriranje samo korisnika sa ulogom 'employee'

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            tableModel.setRowCount(0); // Resetovanje tabele
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
        String username = JOptionPane.showInputDialog("Unesite username:");
        String email = JOptionPane.showInputDialog("Unesite email:");

        JPasswordField passwordField = new JPasswordField();
        JOptionPane.showMessageDialog(null, passwordField, "Unesite password:", JOptionPane.PLAIN_MESSAGE);
        String password = new String(passwordField.getPassword());

        String role = JOptionPane.showInputDialog("Unesite ulogu: (Employee, Manager)");
        String fullName = JOptionPane.showInputDialog("Unesite puno ime:");

        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        JFormattedTextField salaryField = new JFormattedTextField(numberFormat);
        salaryField.setValue(0);
        JOptionPane.showMessageDialog(null, salaryField, "Unesite platu:", JOptionPane.PLAIN_MESSAGE);
        String salaryStr = salaryField.getText();

        if (!isRoleValid(role)) {
            JOptionPane.showMessageDialog(panel1, "Unesena uloga nije validna.");
            return;
        }
        if (!isEmailValid(email)) {
            JOptionPane.showMessageDialog(panel1, "Uneseni E-mail nije validan.");
            return;
        }

        try {
            double salary = Double.parseDouble(salaryStr);

            String insertKorisnici = "INSERT INTO korisnici (username, email, password, role) VALUES (?, ?, ?, ?)";
            String insertDetails = "INSERT INTO korisnik_details (korisnik_id, fullName, salary) VALUES (LAST_INSERT_ID(), ?, ?)";

            PreparedStatement psKorisnici = connection.prepareStatement(insertKorisnici);
            psKorisnici.setString(1, username);
            psKorisnici.setString(2, email);
            psKorisnici.setString(3, password);
            psKorisnici.setString(4, role);
            psKorisnici.executeUpdate();

            PreparedStatement psDetails = connection.prepareStatement(insertDetails);
            psDetails.setString(1, fullName);
            psDetails.setDouble(2, salary);
            psDetails.executeUpdate();

            psKorisnici.close();
            psDetails.close();

            JOptionPane.showMessageDialog(panel1, "Uspješno ste dodali korisnika!");
            loadData();
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom dodavanja korisnika.");
        }
    }

    private void deleteUser() {
        int selectedRow = PrikazTabela.getSelectedRow();
        selectedUser(selectedRow);

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            String deleteDetails = "DELETE FROM korisnik_details WHERE korisnik_id = ?";
            String deleteKorisnici = "DELETE FROM korisnici WHERE id = ?";

            PreparedStatement psDetails = connection.prepareStatement(deleteDetails);
            psDetails.setInt(1, id);
            psDetails.executeUpdate();

            PreparedStatement psKorisnici = connection.prepareStatement(deleteKorisnici);
            psKorisnici.setInt(1, id);
            psKorisnici.executeUpdate();

            psDetails.close();
            psKorisnici.close();

            JOptionPane.showMessageDialog(panel1, "Korisnik je uspješno isbrisan.");
            loadData();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom brisanja korisnika.");
        }
    }

    private void updateUser() {
        int selectedRow = PrikazTabela.getSelectedRow();
        selectedUser(selectedRow);

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String newUsername = JOptionPane.showInputDialog("Unesite novi username:", tableModel.getValueAt(selectedRow, 1));

        String newEmail = JOptionPane.showInputDialog("Unesite novi email:", tableModel.getValueAt(selectedRow, 2));
        String newRole = JOptionPane.showInputDialog("Unesite novu ulogu (Employee, Manager):", tableModel.getValueAt(selectedRow, 3));
        String newFullName = JOptionPane.showInputDialog("Unesite novo puno ime:", tableModel.getValueAt(selectedRow, 4));

        // Polje za unos plate sa validacijom samo brojeva
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        JFormattedTextField salaryField = new JFormattedTextField(numberFormat);
        salaryField.setValue(tableModel.getValueAt(selectedRow, 5)); // Trenutna plata
        JOptionPane.showMessageDialog(null, salaryField, "Unesite novu platu:", JOptionPane.PLAIN_MESSAGE);
        String salaryStr = salaryField.getText().replace(",", "");

        // Validacija
        if (!isRoleValid(newRole)) {
            JOptionPane.showMessageDialog(panel1, "Unesena uloga nije validna. Dozvoljeno: Employee, Manager.");
            return;
        }
        if (!isEmailValid(newEmail)) {
            JOptionPane.showMessageDialog(panel1, "Uneseni e-mail nije validan.");
            return;
        }

        try {
            double newSalary = Double.parseDouble(salaryStr);
            double oldSalary = (double) tableModel.getValueAt(selectedRow, 5);
            // SQL upiti
            String updateKorisnici = "UPDATE korisnici SET username = ?, email = ?, role = ?, password = ? WHERE id = ?";
            String updateDetails = "UPDATE korisnik_details SET fullName = ?, salary = ? WHERE korisnik_id = ?";
            String plataChange = "INSERT INTO historija_plata (korisnik_id, old_salary, new_salary) VALUES (?, ?, ?)";

            String currentPasswordQuery = "SELECT password FROM korisnici WHERE id = ?";
            PreparedStatement psPassword = connection.prepareStatement(currentPasswordQuery);
            psPassword.setInt(1, id);
            ResultSet rs = psPassword.executeQuery();
            String currentPassword = null;

            if (rs.next()) {
                currentPassword = rs.getString("password");
            }
            rs.close();
            psPassword.close();

            PreparedStatement psPlata = connection.prepareStatement(plataChange);
            psPlata.setInt(1, id);
            psPlata.setDouble(2, oldSalary); // Stara plata
            psPlata.setDouble(3, newSalary); // Nova plata
            psPlata.executeUpdate();
            psPlata.close();

            PreparedStatement psKorisnici = connection.prepareStatement(updateKorisnici);
            psKorisnici.setString(1, newUsername);
            psKorisnici.setString(2, newEmail);
            psKorisnici.setString(3, newRole);
            psKorisnici.setString(4,currentPassword);
            psKorisnici.setInt(5, id);
            psKorisnici.executeUpdate();

            PreparedStatement psDetails = connection.prepareStatement(updateDetails);
            psDetails.setString(1, newFullName);
            psDetails.setDouble(2, newSalary);
            psDetails.setInt(3, id);
            psDetails.executeUpdate();

            psKorisnici.close();
            psDetails.close();

            JOptionPane.showMessageDialog(panel1, "Korisnik uspješno ažuriran.");
            loadData();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom ažuriranja korisnika.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(panel1, "Plata mora biti broj.");
        }
    }

    private void writeComplaint(){
        int selectedRow = PrikazTabela.getSelectedRow();
        selectedUser(selectedRow);
        int korisnikId = (int) tableModel.getValueAt(selectedRow, 0);
        String complaintText = JOptionPane.showInputDialog("Unesite žalbu:");

        if (complaintText != null && !complaintText.isEmpty()) {
            try {
                String insertComplaint = "INSERT INTO korisnik_zalbe (korisnik_id, complaint_text, complaint_date, status) VALUES (?, ?, ?, ?)";
                PreparedStatement psComplaint = connection.prepareStatement(insertComplaint);
                psComplaint.setInt(1, korisnikId);
                psComplaint.setString(2, complaintText);
                psComplaint.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                psComplaint.setString(4, "u obradi");  // Početni status žalbe
                psComplaint.executeUpdate();
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

    private void showComplaint(){
        int selectedRow = PrikazTabela.getSelectedRow();
        selectedUser(selectedRow);
        int korisnikId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            String query = "SELECT id, complaint_text, complaint_date, status FROM korisnik_zalbe WHERE korisnik_id = ?";
            PreparedStatement psComplaints = connection.prepareStatement(query);
            psComplaints.setInt(1, korisnikId);

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

            JTable complaintsTable = new JTable(complaintModel);

            // Prilagodba širine stupaca
            complaintsTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
            complaintsTable.getColumnModel().getColumn(1).setPreferredWidth(300); // Žalba
            complaintsTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Datum
            complaintsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Status

            JScrollPane scrollPane = new JScrollPane(complaintsTable);


            JOptionPane.showMessageDialog(panel1, scrollPane, "Lista Žalbi", JOptionPane.INFORMATION_MESSAGE);

            rs.close();
            psComplaints.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom učitavanja žalbi.");
        }
    }

    private void statusComplaint() {
        int selectedRow = PrikazTabela.getSelectedRow();
        selectedUser(selectedRow);
        int korisnikId = (int) tableModel.getValueAt(selectedRow, 0);

        try {
            // Učitavanje žalbi za odabranog korisnika
            String query = "SELECT id, complaint_text, status FROM korisnik_zalbe WHERE korisnik_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, korisnikId);

            ResultSet rs = ps.executeQuery();

            DefaultTableModel complaintModel = new DefaultTableModel();
            complaintModel.addColumn("ID");
            complaintModel.addColumn("Žalba");
            complaintModel.addColumn("Status");

            while (rs.next()) {
                complaintModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("complaint_text"),
                        rs.getString("status")
                });
            }

            JTable complaintsTable = new JTable(complaintModel);

            JScrollPane scrollPane = new JScrollPane(complaintsTable);
            int option = JOptionPane.showConfirmDialog(panel1, scrollPane, "Odaberite žalbu za ažuriranje statusa", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                int selectedComplaintRow = complaintsTable.getSelectedRow();
                if (selectedComplaintRow < 0) {
                    JOptionPane.showMessageDialog(panel1, "Molimo odaberite žalbu iz tabele.");
                    return;
                }

                int complaintId = (int) complaintModel.getValueAt(selectedComplaintRow, 0);
                String updateQuery = "UPDATE korisnik_zalbe SET status = ? WHERE id = ?";
                PreparedStatement psUpdate = connection.prepareStatement(updateQuery);
                psUpdate.setString(1, "završeno"); // Novi status
                psUpdate.setInt(2, complaintId);

                int rowsUpdated = psUpdate.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(panel1, "Status žalbe je uspješno ažuriran na 'završeno'.");
                } else {
                    JOptionPane.showMessageDialog(panel1, "Greška prilikom ažuriranja statusa žalbe.");
                }

                psUpdate.close();
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom ažuriranja statusa žalbe.");
        }

    }



    private void showSalaryHistory(int korisnikId) {
        try {
            String query = "SELECT old_salary, new_salary, change_date FROM historija_plata WHERE korisnik_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, korisnikId);

            ResultSet rs = ps.executeQuery();

            StringBuilder history = new StringBuilder("Historija plata:\n\n");
            while (rs.next()) {
                double oldSalary = rs.getDouble("old_salary");
                double newSalary = rs.getDouble("new_salary");
                Timestamp changeDate = rs.getTimestamp("change_date");

                history.append(String.format("Stara plata: %.2f, Nova plata: %.2f, Datum: %s\n",
                        oldSalary, newSalary, changeDate.toString()));
            }

            rs.close();
            ps.close();

            JOptionPane.showMessageDialog(panel1, history.toString(), "Historija plata", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel1, "Greška prilikom učitavanja historije plata.");
        }
    }
    public void showForm() {
        JFrame frame = new JFrame("Admin Form");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 700);

        frame.setVisible(true);
    }


}

