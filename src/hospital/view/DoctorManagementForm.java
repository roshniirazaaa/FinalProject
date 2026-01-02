package hospital.view;

import hospital.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class DoctorManagementForm extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;

    public DoctorManagementForm() {
        setTitle("Doctor Management");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        model = new DefaultTableModel(new String[]{"ID", "Name", "Specialization", "Contact"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel panelButtons = new JPanel(new FlowLayout());
        btnAdd = new JButton("Add Doctor");
        btnEdit = new JButton("Edit Doctor");
        btnDelete = new JButton("Delete Doctor");
        btnRefresh = new JButton("Refresh");
        panelButtons.add(btnAdd);
        panelButtons.add(btnEdit);
        panelButtons.add(btnDelete);
        panelButtons.add(btnRefresh);

        add(panelButtons, BorderLayout.SOUTH);

        // Load data
        loadDoctors();

        // Button actions
        btnAdd.addActionListener(e -> addDoctor());
        btnEdit.addActionListener(e -> editDoctor());
        btnDelete.addActionListener(e -> deleteDoctor());
        btnRefresh.addActionListener(e -> loadDoctors());
    }

    private void loadDoctors() {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM doctor")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("contact")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading doctors: " + ex.getMessage());
        }
    }

    private void addDoctor() {
        JTextField txtName = new JTextField();
        JTextField txtSpec = new JTextField();
        JTextField txtContact = new JTextField();
        Object[] fields = {
                "Name:", txtName,
                "Specialization:", txtSpec,
                "Contact:", txtContact
        };
        int option = JOptionPane.showConfirmDialog(this, fields, "Add Doctor", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO doctor (name, specialization, contact) VALUES (?,?,?)")) {
                ps.setString(1, txtName.getText());
                ps.setString(2, txtSpec.getText());
                ps.setString(3, txtContact.getText());
                ps.executeUpdate();
                loadDoctors();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error adding doctor: " + ex.getMessage());
            }
        }
    }

    private void editDoctor() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a doctor to edit.");
            return;
        }
        int id = (int) model.getValueAt(selectedRow, 0);
        String currentName = (String) model.getValueAt(selectedRow, 1);
        String currentSpec = (String) model.getValueAt(selectedRow, 2);
        String currentContact = (String) model.getValueAt(selectedRow, 3);

        JTextField txtName = new JTextField(currentName);
        JTextField txtSpec = new JTextField(currentSpec);
        JTextField txtContact = new JTextField(currentContact);
        Object[] fields = {
                "Name:", txtName,
                "Specialization:", txtSpec,
                "Contact:", txtContact
        };
        int option = JOptionPane.showConfirmDialog(this, fields, "Edit Doctor", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE doctor SET name=?, specialization=?, contact=? WHERE id=?")) {
                ps.setString(1, txtName.getText());
                ps.setString(2, txtSpec.getText());
                ps.setString(3, txtContact.getText());
                ps.setInt(4, id);
                ps.executeUpdate();
                loadDoctors();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error updating doctor: " + ex.getMessage());
            }
        }
    }

    private void deleteDoctor() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a doctor to delete.");
            return;
        }
        int id = (int) model.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this doctor?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM doctor WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadDoctors();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting doctor: " + ex.getMessage());
            }
        }
    }
}
