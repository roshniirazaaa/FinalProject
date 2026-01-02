package hospital.view;

import hospital.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PendingAppointmentsForm extends JFrame {
    private int patientId;
    private JTable table;
    private DefaultTableModel model;

    public PendingAppointmentsForm(int patientId) {
        this.patientId = patientId;

        setTitle("Pending Appointments");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(new String[]{"Appointment ID", "Doctor ID", "Date", "Status"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadPendingAppointments();
    }

    private void loadPendingAppointments() {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM appointment WHERE patient_id=? AND status='Pending'")) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("doctor_id"),
                        rs.getDate("date"),
                        rs.getString("status")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading appointments: " + ex.getMessage());
        }
    }
}
