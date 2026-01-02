package hospital.view;

import hospital.db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

public class BookAppointmentForm extends JFrame {
    private int patientId;

    public BookAppointmentForm(int patientId) {
        this.patientId = patientId;

        setTitle("Book Appointment");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(4, 2, 10, 10));

        JTextField txtDoctorId = new JTextField();
        JTextField txtDate = new JTextField(); // format: YYYY-MM-DD
        JButton btnBook = new JButton("Book Appointment");

        add(new JLabel("Doctor ID:"));
        add(txtDoctorId);
        add(new JLabel("Date (YYYY-MM-DD):"));
        add(txtDate);
        add(new JLabel());
        add(btnBook);

        btnBook.addActionListener(e -> {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO appointment (doctor_id, patient_id, date, status) VALUES (?,?,?,?)")) {
                ps.setInt(1, Integer.parseInt(txtDoctorId.getText()));
                ps.setInt(2, patientId);
                ps.setDate(3, java.sql.Date.valueOf(txtDate.getText()));
                ps.setString(4, "Pending");
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Appointment booked successfully!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
    }
}
