package hospital.view;

import hospital.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;

public class AdminDashboard extends JFrame {

    private Connection conn;
    private JPanel mainPanel;

    // For appointments
    private JComboBox<String> comboPatient;
    private JComboBox<String> comboDoctor;
    private JTextField txtSymptom;
    private JTextField txtDate;

    private HashMap<Integer, String> patientSymptomMap = new HashMap<>();

    public AdminDashboard() {
        try {
            conn = DBConnection.getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed: " + ex.getMessage());
            System.exit(0);
        }

        setTitle("Admin Dashboard - Hospital Management System");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("ADMIN DASHBOARD", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ================= SIDE PANEL ==================
        JPanel sidePanelWrapper = new JPanel(new GridBagLayout());
        sidePanelWrapper.setBackground(new Color(230, 240, 255));
        add(sidePanelWrapper, BorderLayout.WEST);

        JPanel sidePanel = new JPanel(new GridLayout(7, 1, 10, 10));
        sidePanel.setPreferredSize(new Dimension(250, 400));
        sidePanel.setBackground(new Color(255, 255, 255));
        sidePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 90, 160), 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JButton btnDash = new JButton("Dashboard Summary");
        JButton btnPatients = new JButton("Manage Patients");
        JButton btnAppointments = new JButton("Appointments");
        JButton btnBilling = new JButton("Billing / Invoices");
        JButton btnReports = new JButton("Reports");
        JButton btnLogout = new JButton("Logout");

        Color btnColor = new Color(0, 110, 190);
        Color btnText = Color.WHITE;

        JButton[] btns = {btnDash, btnPatients, btnAppointments, btnBilling, btnReports, btnLogout};
        for (JButton b : btns) {
            b.setBackground(btnColor);
            b.setForeground(btnText);
            b.setFocusPainted(false);
            b.setFont(new Font("Arial", Font.BOLD, 15));
        }

        for (JButton b : btns) sidePanel.add(b);
        sidePanelWrapper.add(sidePanel);

        // ================= RIGHT MAIN AREA ==================
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel, BorderLayout.CENTER);

        showDashboardSummary();

        btnDash.addActionListener(e -> showDashboardSummary());
        btnPatients.addActionListener(e -> showPatientPanel());
        btnAppointments.addActionListener(e -> showAppointmentPanel());
        btnBilling.addActionListener(e -> showBillingPanel());
        btnReports.addActionListener(e -> showReportsPanel());
        btnLogout.addActionListener(e -> {
            new LoginForm().setVisible(true);
            dispose();
        });
    }

    // ================= DASHBOARD SUMMARY =================
    private void showDashboardSummary() {
        mainPanel.removeAll();
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        panel.add(createCard("Total Patients", String.valueOf(getCount("patients"))));
        panel.add(createCard("Total Doctors", String.valueOf(getCount("doctors"))));
        panel.add(createCard("Today's Appointments", String.valueOf(getTodayAppointments())));

        mainPanel.add(panel, BorderLayout.NORTH);
        refresh();
    }

    private int getCount(String table) {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private int getTodayAppointments() {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM appointments WHERE date = CURDATE()");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private JPanel createCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(235, 245, 255));
        card.setBorder(BorderFactory.createLineBorder(new Color(0, 90, 160), 2));

        JLabel lbl1 = new JLabel(title, JLabel.CENTER);
        lbl1.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel lbl2 = new JLabel(value, JLabel.CENTER);
        lbl2.setFont(new Font("Arial", Font.BOLD, 28));
        lbl2.setForeground(new Color(0, 122, 180));

        card.add(lbl1, BorderLayout.NORTH);
        card.add(lbl2, BorderLayout.CENTER);
        return card;
    }

    // ================= PATIENT PANEL =================
    private void showPatientPanel() {
        mainPanel.removeAll();

        JPanel panel = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("PATIENT MANAGEMENT", JLabel.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(lbl, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Age", "Gender", "Contact", "Symptom"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        loadPatients(model);

        JPanel btns = new JPanel();
        JButton bAdd = new JButton("Add");
        JButton bEd = new JButton("Edit");
        JButton bDel = new JButton("Delete");

        btns.add(bAdd);
        btns.add(bEd);
        btns.add(bDel);
        panel.add(btns, BorderLayout.SOUTH);

        bAdd.addActionListener(e -> addPatient(model));
        bEd.addActionListener(e -> editPatient(model, table));
        bDel.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int patientId = (int) model.getValueAt(selectedRow, 0);
                deletePatient(patientId, model);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a patient to delete!");
            }
        });

        mainPanel.add(panel);
        refresh();
    }

    private void loadPatients(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            String sql = "SELECT * FROM patients";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("contact"),
                        rs.getString("symptom")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addPatient(DefaultTableModel model) {
        JTextField txtName = new JTextField();
        JTextField txtAge = new JTextField();
        JTextField txtContact = new JTextField();
        JComboBox<String> comboGender = new JComboBox<>(new String[]{"Male", "Female"});
        JComboBox<String> comboSymptom = new JComboBox<>(new String[]{
                "Fever", "Headache", "Cough", "Cold", "Vomiting",
                "Back Pain", "Fatigue", "Diabetes", "Hypertension"
        });

        Object[] inputs = {
                "Name:", txtName,
                "Age:", txtAge,
                "Contact:", txtContact,
                "Gender:", comboGender,
                "Symptom:", comboSymptom
        };

        int result = JOptionPane.showConfirmDialog(this, inputs, "Add Patient", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO patients (name, age, gender, contact, symptom) VALUES (?,?,?,?,?)");
                ps.setString(1, txtName.getText());
                ps.setInt(2, Integer.parseInt(txtAge.getText()));
                ps.setString(3, comboGender.getSelectedItem().toString());
                ps.setString(4, txtContact.getText());
                ps.setString(5, comboSymptom.getSelectedItem().toString());
                ps.executeUpdate();
                loadPatients(model);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void editPatient(DefaultTableModel model, JTable table) {
        int r = table.getSelectedRow();
        if (r < 0) { msg("Select a patient to edit!"); return; }

        int id = Integer.parseInt(model.getValueAt(r, 0).toString());
        String currentName = model.getValueAt(r, 1).toString();
        String currentAge = model.getValueAt(r, 2).toString();
        String currentGender = model.getValueAt(r, 3).toString();
        String currentContact = model.getValueAt(r, 4).toString();
        String currentSymptom = model.getValueAt(r, 5).toString();

        JTextField txtName = new JTextField(currentName);
        JTextField txtAge = new JTextField(currentAge);
        JTextField txtContact = new JTextField(currentContact);
        JComboBox<String> comboGender = new JComboBox<>(new String[]{"Male", "Female"});
        comboGender.setSelectedItem(currentGender);
        JComboBox<String> comboSymptom = new JComboBox<>(new String[]{
                "Fever", "Headache", "Cough", "Cold", "Vomiting",
                "Back Pain", "Fatigue", "Diabetes", "Hypertension"
        });
        comboSymptom.setSelectedItem(currentSymptom);

        Object[] inputs = {
                "Name:", txtName,
                "Age:", txtAge,
                "Contact:", txtContact,
                "Gender:", comboGender,
                "Symptom:", comboSymptom
        };

        int result = JOptionPane.showConfirmDialog(this, inputs, "Edit Patient", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE patients SET name=?, age=?, gender=?, contact=?, symptom=? WHERE id=?"
                );
                ps.setString(1, txtName.getText());
                ps.setInt(2, Integer.parseInt(txtAge.getText()));
                ps.setString(3, comboGender.getSelectedItem().toString());
                ps.setString(4, txtContact.getText());
                ps.setString(5, comboSymptom.getSelectedItem().toString());
                ps.setInt(6, id);
                ps.executeUpdate();

                loadPatients(model);
                msg("Patient updated successfully!");
            } catch (SQLException e) {
                e.printStackTrace();
                msg("Error updating patient: " + e.getMessage());
            }
        }
    }

    private void deletePatient(int patientId, DefaultTableModel model) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this patient?\nAll related appointments will also be deleted!",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Delete related appointments first
                String deleteAppointmentsQuery = "DELETE FROM appointments WHERE patient_id = ?";
                PreparedStatement stmt1 = conn.prepareStatement(deleteAppointmentsQuery);
                stmt1.setInt(1, patientId);
                stmt1.executeUpdate();

                // Delete patient
                String deletePatientQuery = "DELETE FROM patients WHERE id = ?";
                PreparedStatement stmt2 = conn.prepareStatement(deletePatientQuery);
                stmt2.setInt(1, patientId);
                stmt2.executeUpdate();

                msg("Patient deleted successfully!");
                loadPatients(model);
            } catch (SQLException e) {
                e.printStackTrace();
                msg("Error deleting patient: " + e.getMessage());
            }
        }
    }

    // ================= APPOINTMENTS PANEL =================
    private void showAppointmentPanel() {
        mainPanel.removeAll();

        JPanel p = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("APPOINTMENTS", JLabel.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 22));
        p.add(lbl, BorderLayout.NORTH);

        String[] cols = {"Patient ID", "Patient Name", "Symptom", "Doctor ID", "Doctor Name", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btns = new JPanel();
        JButton add = new JButton("Book");
        JButton del = new JButton("Cancel");
        btns.add(add);
        btns.add(del);
        p.add(btns, BorderLayout.SOUTH);

        comboPatient = new JComboBox<>();
        comboDoctor = new JComboBox<>();
        txtSymptom = new JTextField(10);
        txtSymptom.setEditable(false);
        txtDate = new JTextField(10);

        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboPanel.add(new JLabel("Patient:"));
        comboPanel.add(comboPatient);
        comboPanel.add(new JLabel("Symptom:"));
        comboPanel.add(txtSymptom);
        comboPanel.add(new JLabel("Doctor:"));
        comboPanel.add(comboDoctor);
        comboPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        comboPanel.add(txtDate);
        p.add(comboPanel, BorderLayout.NORTH);

        loadComboPatientsForAppointments();
        loadComboDoctorsForAppointments();

        comboPatient.addActionListener(e -> updateSymptomField());

        add.addActionListener(e -> addAppointmentToDB(model));

        del.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { msg("Select an appointment!"); return; }
            int patientId = Integer.parseInt(model.getValueAt(r, 0).toString());
            int doctorId = Integer.parseInt(model.getValueAt(r, 3).toString());
            String date = model.getValueAt(r, 5).toString();

            try {
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM appointments WHERE patient_id=? AND doctor_id=? AND date=? LIMIT 1"
                );
                ps.setInt(1, patientId);
                ps.setInt(2, doctorId);
                ps.setString(3, date);
                ps.executeUpdate();
                model.removeRow(r);
                msg("Appointment deleted!");
            } catch (SQLException ex) {
                ex.printStackTrace();
                msg("Error deleting appointment: " + ex.getMessage());
            }
        });

        loadAppointments(model);

        mainPanel.add(p);
        refresh();
    }

    private void loadComboPatientsForAppointments() {
        comboPatient.removeAllItems();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, name, symptom FROM patients");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String symptom = rs.getString("symptom");
                comboPatient.addItem(id + " - " + name);
                patientSymptomMap.put(id, symptom);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        updateSymptomField();
    }

    private void loadComboDoctorsForAppointments() {
        comboDoctor.removeAllItems();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, name FROM doctors");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                comboDoctor.addItem(id + " - " + name);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void addAppointmentToDB(DefaultTableModel model) {
        Object patientObj = comboPatient.getSelectedItem();
        Object doctorObj = comboDoctor.getSelectedItem();
        String date = txtDate.getText().trim();

        if (patientObj == null || doctorObj == null || date.isEmpty()) {
            msg("Please select a patient, doctor, and enter a valid date!");
            return;
        }

        try {
            String[] pParts = patientObj.toString().split(" - ");
            int patientId = Integer.parseInt(pParts[0].trim());
            String patientName = pParts[1].trim();
            String symptom = patientSymptomMap.getOrDefault(patientId, "");

            String[] dParts = doctorObj.toString().split(" - ");
            int doctorId = Integer.parseInt(dParts[0].trim());
            String doctorName = dParts[1].trim();

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO appointments (patient_id, doctor_id, date) VALUES (?,?,?)"
            );
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setString(3, date);
            ps.executeUpdate();

            model.addRow(new Object[]{patientId, patientName, symptom, doctorId, doctorName, date});
            msg("Appointment booked successfully!");

        } catch (SQLException ex) {
            ex.printStackTrace();
            msg("Error booking appointment: " + ex.getMessage());
        } catch (NumberFormatException nfe) {
            msg("Invalid ID format for patient or doctor!");
        }
    }

    private void loadAppointments(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT a.patient_id, p.name AS patient_name, p.symptom, a.doctor_id, d.name AS doctor_name, a.date " +
                            "FROM appointments a " +
                            "JOIN patients p ON a.patient_id=p.id " +
                            "JOIN doctors d ON a.doctor_id=d.id"
            );
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("patient_id"),
                        rs.getString("patient_name"),
                        rs.getString("symptom"),
                        rs.getInt("doctor_id"),
                        rs.getString("doctor_name"),
                        rs.getString("date")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateSymptomField() {
        Object selected = comboPatient.getSelectedItem();
        if (selected == null) { txtSymptom.setText(""); return; }
        int id = Integer.parseInt(selected.toString().split(" - ")[0]);
        txtSymptom.setText(patientSymptomMap.getOrDefault(id, ""));
    }

    // ================= BILLING PANEL =================
    private void showBillingPanel() {
        mainPanel.removeAll();

        JPanel panel = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("BILLING / INVOICES", JLabel.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(lbl, BorderLayout.NORTH);

        String[] cols = {"Invoice ID", "Patient Name", "Amount", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btns = new JPanel();
        JButton add = new JButton("Generate Invoice");
        JButton del = new JButton("Delete Invoice");
        btns.add(add);
        btns.add(del);
        panel.add(btns, BorderLayout.SOUTH);

        add.addActionListener(e -> {
            JTextField txtPatient = new JTextField();
            JTextField txtAmount = new JTextField();
            JTextField txtDate = new JTextField();

            Object[] inputs = {"Patient Name:", txtPatient, "Amount:", txtAmount, "Date (YYYY-MM-DD):", txtDate};
            int res = JOptionPane.showConfirmDialog(this, inputs, "Generate Invoice", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                try {
                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO billing (patient_name, amount, date) VALUES (?,?,?)"
                    );
                    ps.setString(1, txtPatient.getText());
                    ps.setDouble(2, Double.parseDouble(txtAmount.getText()));
                    ps.setString(3, txtDate.getText());
                    ps.executeUpdate();
                    loadBilling(model);
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        del.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { msg("Select an invoice!"); return; }
            int id = Integer.parseInt(model.getValueAt(r, 0).toString());
            try {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM billing WHERE id=?");
                ps.setInt(1, id);
                ps.executeUpdate();
                model.removeRow(r);
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        loadBilling(model);

        mainPanel.add(panel);
        refresh();
    }

    private void loadBilling(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM billing");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getDouble("amount"),
                        rs.getString("date")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ================= REPORTS PANEL =================
    private void showReportsPanel() {
        mainPanel.removeAll();

        JPanel panel = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("REPORTS", JLabel.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(lbl, BorderLayout.NORTH);

        String[] cols = {"Report ID", "Title", "Patient Name", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btns = new JPanel();
        JButton add = new JButton("Add Report");
        JButton del = new JButton("Delete Report");
        btns.add(add);
        btns.add(del);
        panel.add(btns, BorderLayout.SOUTH);

        add.addActionListener(e -> {
            JTextField txtTitle = new JTextField();
            JTextField txtPatient = new JTextField();
            JTextField txtDate = new JTextField();

            Object[] inputs = {"Title:", txtTitle, "Patient Name:", txtPatient, "Date (YYYY-MM-DD):", txtDate};
            int res = JOptionPane.showConfirmDialog(this, inputs, "Add Report", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                try {
                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO reports (title, patient_name, date) VALUES (?,?,?)"
                    );
                    ps.setString(1, txtTitle.getText());
                    ps.setString(2, txtPatient.getText());
                    ps.setString(3, txtDate.getText());
                    ps.executeUpdate();
                    loadReports(model);
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        del.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { msg("Select a report!"); return; }
            int id = Integer.parseInt(model.getValueAt(r, 0).toString());
            try {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM reports WHERE id=?");
                ps.setInt(1, id);
                ps.executeUpdate();
                model.removeRow(r);
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        loadReports(model);

        mainPanel.add(panel);
        refresh();
    }

    private void loadReports(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM reports");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("patient_name"),
                        rs.getString("date")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void refresh() { mainPanel.revalidate(); mainPanel.repaint(); }
    private void msg(String text) { JOptionPane.showMessageDialog(this, text); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
    }

}
