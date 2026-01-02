package hospital.view;

import hospital.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PatientManagementForm extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;

    public PatientManagementForm() {
        setTitle("Patient Management");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        model = new DefaultTableModel(new String[]{"ID", "Name", "Age", "Contact"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel panelButtons = new JPanel(new FlowLayout());
        btnAdd = new JButton("Add Patient");
        btnEdit = new JButton("Edit Patient");
        btnDelete = new JButton("Delete Patient");
        btnRefresh = new JButton("Refresh");
        panelButtons.add(btnAdd);
        panelButtons.add(btnEdit);
        panelButtons.add(btnDelete);
        panelButtons.add(btnRefresh);

        add(panelButtons, BorderLayout.SOUTH);

        // Load data
        loadPatients();

        // Button actions
        btnAdd.addActionListener(e -> addPatient());
        btnEdit.addActionListener(e -> editPatient());
        btnDelete.addActionListener(e -> deletePatient());
        btnRefresh.addActionListener(e -> loadPatients());
    }

    private void loadPatients() {
        model.setRowCount(0); // clear table
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM patient")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("contact")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + ex.getMessage());
        }
    }

    private void addPatient() {
        JTextField txtName = new JTextField();
        JTextField txtAge = new JTextField();
        JTextField txtContact = new JTextField();
        Object[] fields = {
                "Name:", txtName,
                "Age:", txtAge,
                "Contact:", txtContact
        };
        int option = JOptionPane.showConfirmDialog(this, fields, "Add Patient", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO patient (name, age, contact) VALUES (?,?,?)")) {
                ps.setString(1, txtName.getText());
                ps.setInt(2, Integer.parseInt(txtAge.getText()));
                ps.setString(3, txtContact.getText());
                ps.executeUpdate();
                loadPatients();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error adding patient: " + ex.getMessage());
            }
        }
    }

    private void editPatient() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a patient to edit.");
            return;
        }
        int id = (int) model.getValueAt(selectedRow, 0);
        String currentName = (String) model.getValueAt(selectedRow, 1);
        int currentAge = (int) model.getValueAt(selectedRow, 2);
        String currentContact = (String) model.getValueAt(selectedRow, 3);

        JTextField txtName = new JTextField(currentName);
        JTextField txtAge = new JTextField(String.valueOf(currentAge));
        JTextField txtContact = new JTextField(currentContact);
        Object[] fields = {
                "Name:", txtName,
                "Age:", txtAge,
                "Contact:", txtContact
        };
        int option = JOptionPane.showConfirmDialog(this, fields, "Edit Patient", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE patient SET name=?, age=?, contact=? WHERE id=?")) {
                ps.setString(1, txtName.getText());
                ps.setInt(2, Integer.parseInt(txtAge.getText()));
                ps.setString(3, txtContact.getText());
                ps.setInt(4, id);
                ps.executeUpdate();
                loadPatients();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error updating patient: " + ex.getMessage());
            }
        }
    }

    private void deletePatient() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a patient to delete.");
            return;
        }
        int id = (int) model.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this patient?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM patient WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadPatients();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting patient: " + ex.getMessage());
            }
        }
    }
}
