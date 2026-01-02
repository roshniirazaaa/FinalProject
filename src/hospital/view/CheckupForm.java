package hospital.view;

import hospital.db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CheckupForm extends JFrame {
    private int doctorId;

    public CheckupForm(int doctorId) {
        this.doctorId = doctorId;

        setTitle("Perform Checkup");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(5, 2, 10, 10));

        JTextField txtPatientId = new JTextField();
        JTextField txtSymptoms = new JTextField();
        JTextField txtDiagnosis = new JTextField();
        JButton btnSubmit = new JButton("Submit Checkup");

        add(new JLabel("Patient ID:"));
        add(txtPatientId);
        add(new JLabel("Symptoms:"));
        add(txtSymptoms);
        add(new JLabel("Diagnosis:"));
        add(txtDiagnosis);
        add(new JLabel());
        add(btnSubmit);

        btnSubmit.addActionListener(e -> {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO checkup (doctor_id, patient_id, symptoms, diagnosis) VALUES (?,?,?,?)")) {
                ps.setInt(1, doctorId);
                ps.setInt(2, Integer.parseInt(txtPatientId.getText()));
                ps.setString(3, txtSymptoms.getText());
                ps.setString(4, txtDiagnosis.getText());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Checkup added successfully!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
    }
}
