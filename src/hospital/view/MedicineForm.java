package hospital.view;

import hospital.db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MedicineForm extends JFrame {
    private int doctorId;

    public MedicineForm(int doctorId) {
        this.doctorId = doctorId;

        setTitle("Write Prescription");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(5, 2, 10, 10));

        JTextField txtPatientId = new JTextField();
        JTextField txtMedicine = new JTextField();
        JTextField txtDosage = new JTextField();
        JButton btnSubmit = new JButton("Add Prescription");

        add(new JLabel("Patient ID:"));
        add(txtPatientId);
        add(new JLabel("Medicine:"));
        add(txtMedicine);
        add(new JLabel("Dosage:"));
        add(txtDosage);
        add(new JLabel());
        add(btnSubmit);

        btnSubmit.addActionListener(e -> {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO prescription (doctor_id, patient_id, medicine, dosage) VALUES (?,?,?,?)")) {
                ps.setInt(1, doctorId);
                ps.setInt(2, Integer.parseInt(txtPatientId.getText()));
                ps.setString(3, txtMedicine.getText());
                ps.setString(4, txtDosage.getText());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Prescription added successfully!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
    }
}
