package com.danijelmizdaric.example;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginForm {
    private JTextField textField1;
    private JPanel panel1;
    private JPasswordField passwordField1;
    private JButton button1;
    private JButton exitButton;

    public LoginForm() {

        exitButton.addActionListener(e->
                System.exit(0)); //izlazak iz aplikacije

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = textField1.getText();
                String password = new String(passwordField1.getPassword());
                try {
                    Connection connection = DriverManager.getConnection(
                            "jdbc:mysql://127.0.0.1:3306/hrms", "root", "Benswolo#1"); //otvaranje baze

                    String query = "SELECT * FROM korisnici WHERE username = ? AND password = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, username);
                    preparedStatement.setString(2, password);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) { //ako se podaci podudaraju sa onima u bazi, ovaj dio koda se izvrsava
                        String role = resultSet.getString("role");
                        JOptionPane.showMessageDialog(panel1, "Uspješan login!");
                        UserSession.setUserDetails(username, role);
                        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
                        frame.dispose();

                        if (role.equals("Employee")) { // zavisno od uloge otvaraju se drugacije forme
                            new KorisnikForm().showForm();
                        } else if (role.equals("Manager")) {
                            new ManagerForm(username).showForm();
                        } else if (role.equals("SuperAdmin")) {
                            new SuperAdminForm(username).showForm();
                        } else {
                            JOptionPane.showMessageDialog(panel1, "Nepoznata uloga!");
                        }
                    } else {
                        JOptionPane.showMessageDialog(panel1, "Neispravan username ili password!");
                    }

                    resultSet.close();
                    preparedStatement.close();
                    connection.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(panel1, "Greška prilikom povezivanja sa bazom: " + ex.getMessage());
                }
            }
        });
    }

    public void showForm() {
        JFrame frame = new JFrame("Login Form");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 700);
        frame.setVisible(true);
    } // prikaz login forme

    public static void main(String[] args) {
        new LoginForm().showForm(); //jedina main funkcija u programu
    }
}
