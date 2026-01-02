package hospital.view;

import hospital.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class DoctorDashboard extends JFrame {

    private int doctorId;
    private Connection conn;

    private JButton btnCheckup, btnWriteMedicine, btnViewAppointments, btnLogout, btnPatientHistory;

    public DoctorDashboard(int doctorId) {
        this.doctorId = doctorId;

        // Connect to DB
        try {
            conn = DBConnection.getConnection();
            insertDummyAppointments(); // insert dummy appointments if none exist
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB connection failed: " + e.getMessage());
            System.exit(0);
        }

        setTitle("Doctor Dashboard");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ================= Dashboard Summary =================
        JPanel summaryPanel = new JPanel(new GridLayout(1,3,10,10));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        summaryPanel.add(createCard("Today's Appointments", String.valueOf(getTodaysAppointments())));
        summaryPanel.add(createCard("Patients Seen", String.valueOf(getTotalCheckups())));
        summaryPanel.add(createCard("Medicines Prescribed", String.valueOf(getTotalMedicines())));
        add(summaryPanel, BorderLayout.NORTH);

        // ================= Center Buttons =================
        JPanel panelButtons = new JPanel(new GridLayout(5, 1, 10, 10));
        panelButtons.setBorder(BorderFactory.createEmptyBorder(20, 350, 20, 350));

        btnCheckup = new JButton("Perform Checkup");
        btnWriteMedicine = new JButton("Write Medicine");
        btnViewAppointments = new JButton("View Upcoming Appointments");
        btnPatientHistory = new JButton("View Patient History");
        btnLogout = new JButton("Logout");

        // ======= BUTTON COLORS & SIZE =======
        Color btnBg = new Color(0, 51, 102); // Dark Blue
        Color btnFg = Color.WHITE;           // Text color
        Font btnFont = new Font("Arial", Font.BOLD, 14); // slightly smaller font
        Dimension btnSize = new Dimension(200, 40);      // slightly smaller buttons

        JButton[] btns = {btnCheckup, btnWriteMedicine, btnViewAppointments, btnPatientHistory, btnLogout};
        for (JButton b : btns) {
            b.setBackground(btnBg);
            b.setForeground(btnFg);
            b.setFocusPainted(false);
            b.setFont(btnFont);
            b.setPreferredSize(btnSize);
        }

        panelButtons.add(btnCheckup);
        panelButtons.add(btnWriteMedicine);
        panelButtons.add(btnViewAppointments);
        panelButtons.add(btnPatientHistory);
        panelButtons.add(btnLogout);

        add(panelButtons, BorderLayout.CENTER);

        // ================= Button Actions =================
        btnCheckup.addActionListener(e -> performCheckup());
        btnWriteMedicine.addActionListener(e -> writeMedicine());
        btnViewAppointments.addActionListener(e -> viewAppointments());
        btnPatientHistory.addActionListener(e -> viewPatientHistory());
        btnLogout.addActionListener(e -> {
            new LoginForm().setVisible(true);
            dispose();
        });
    }
    

    // ================= Dashboard Cards =================
    private JPanel createCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(235, 245, 255));
        card.setBorder(BorderFactory.createLineBorder(new Color(0, 90, 160), 2));

        JLabel lblTitle = new JLabel(title, JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel lblValue = new JLabel(value, JLabel.CENTER);
        lblValue.setFont(new Font("Arial", Font.BOLD, 28));
        lblValue.setForeground(new Color(0, 122, 180));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    private int getTodaysAppointments() {
        int count = 0;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM appointments WHERE doctor_id=? AND date=CURDATE()"
            );
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return count;
    }

    private int getTotalCheckups() {
        int count = 0;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM checkups WHERE doctor_id=?"
            );
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return count;
    }

    private int getTotalMedicines() {
        int count = 0;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM medicines WHERE doctor_id=?"
            );
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return count;
    }

    // ================= Checkup =================
    private void performCheckup() {
        try {
            ArrayList<String> patients = new ArrayList<>();
            PreparedStatement ps = conn.prepareStatement("SELECT id, name FROM patients");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                patients.add(rs.getInt("id") + " - " + rs.getString("name"));
            }

            if (patients.isEmpty()) { 
                JOptionPane.showMessageDialog(this,"No patients found"); 
                return; 
            }

            JComboBox<String> comboPatient = new JComboBox<>(patients.toArray(new String[0]));
            JTextArea txtDiagnosis = new JTextArea(5, 20);

            Object[] inputs = {
                    "Select Patient:", comboPatient,
                    "Diagnosis:", new JScrollPane(txtDiagnosis)
            };
            int result = JOptionPane.showConfirmDialog(this, inputs, "Perform Checkup", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                int patientId = Integer.parseInt(comboPatient.getSelectedItem().toString().split(" - ")[0]);
                PreparedStatement psInsert = conn.prepareStatement(
                    "INSERT INTO checkups (patient_id, doctor_id, diagnosis, date) VALUES (?,?,?,CURDATE())"
                );
                psInsert.setInt(1, patientId);
                psInsert.setInt(2, doctorId);
                psInsert.setString(3, txtDiagnosis.getText());
                psInsert.executeUpdate();
                JOptionPane.showMessageDialog(this,"Checkup recorded successfully!");
                refreshDashboard();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ================= Medicine =================
    private void writeMedicine() {
        try {
            ArrayList<String> patients = new ArrayList<>();
            PreparedStatement ps = conn.prepareStatement("SELECT id, name FROM patients");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                patients.add(rs.getInt("id") + " - " + rs.getString("name"));
            }

            if (patients.isEmpty()) { 
                JOptionPane.showMessageDialog(this,"No patients found"); 
                return; 
            }

            JComboBox<String> comboPatient = new JComboBox<>(patients.toArray(new String[0]));
            JComboBox<String> comboMedicine = new JComboBox<>(new String[]{"Paracetamol","Ibuprofen","Amoxicillin","Aspirin"});
            JTextField txtDosage = new JTextField();

            Object[] inputs = {
                    "Select Patient:", comboPatient,
                    "Medicine:", comboMedicine,
                    "Dosage:", txtDosage
            };
            int result = JOptionPane.showConfirmDialog(this, inputs, "Write Medicine", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                int patientId = Integer.parseInt(comboPatient.getSelectedItem().toString().split(" - ")[0]);
                PreparedStatement psInsert = conn.prepareStatement(
                    "INSERT INTO medicines (patient_id, doctor_id, medicine_name, dosage, date) VALUES (?,?,?,?,CURDATE())"
                );
                psInsert.setInt(1, patientId);
                psInsert.setInt(2, doctorId);
                psInsert.setString(3, comboMedicine.getSelectedItem().toString());
                psInsert.setString(4, txtDosage.getText());
                psInsert.executeUpdate();
                JOptionPane.showMessageDialog(this,"Medicine prescribed successfully!");
                refreshDashboard();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ================= View Appointments =================
    private void viewAppointments() {
        try {
            String[] columns = {"Patient", "Date"};
            DefaultTableModel model = new DefaultTableModel(columns,0);
            JTable table = new JTable(model);

            PreparedStatement ps = conn.prepareStatement(
                "SELECT p.name, a.date FROM patients p " +
                "JOIN appointments a ON a.patient_id=p.id " +
                "WHERE a.doctor_id=? ORDER BY a.date"
            );
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                model.addRow(new Object[]{rs.getString("name"), rs.getDate("date")});
            }

            JOptionPane.showMessageDialog(this, new JScrollPane(table), "Upcoming Appointments", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ================= View Patient History =================
    private void viewPatientHistory() {
        try {
            ArrayList<String> patients = new ArrayList<>();
            PreparedStatement ps = conn.prepareStatement("SELECT id, name FROM patients");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) patients.add(rs.getInt("id")+" - "+rs.getString("name"));

            if (patients.isEmpty()) { JOptionPane.showMessageDialog(this,"No patients found"); return; }

            JComboBox<String> comboPatient = new JComboBox<>(patients.toArray(new String[0]));

            int result = JOptionPane.showConfirmDialog(this, comboPatient, "Select Patient", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                int patientId = Integer.parseInt(comboPatient.getSelectedItem().toString().split(" - ")[0]);
                DefaultTableModel model = new DefaultTableModel(new String[]{"Date","Diagnosis","Medicine","Dosage"},0);

                // Load Checkups
                PreparedStatement psChk = conn.prepareStatement(
                    "SELECT date, diagnosis FROM checkups WHERE doctor_id=? AND patient_id=?"
                );
                psChk.setInt(1, doctorId);
                psChk.setInt(2, patientId);
                ResultSet rsChk = psChk.executeQuery();
                while(rsChk.next()) model.addRow(new Object[]{rsChk.getDate("date"), rsChk.getString("diagnosis"), "", ""});

                // Load Medicines
                PreparedStatement psMed = conn.prepareStatement(
                    "SELECT date, medicine_name, dosage FROM medicines WHERE doctor_id=? AND patient_id=?"
                );
                psMed.setInt(1, doctorId);
                psMed.setInt(2, patientId);
                ResultSet rsMed = psMed.executeQuery();
                while(rsMed.next()) model.addRow(new Object[]{rsMed.getDate("date"), "", rsMed.getString("medicine_name"), rsMed.getString("dosage")});

                JTable table = new JTable(model);
                JOptionPane.showMessageDialog(this, new JScrollPane(table), "Patient History", JOptionPane.PLAIN_MESSAGE);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ================= Insert Dummy Appointments =================
    private void insertDummyAppointments() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM appointments");
        if(rs.next() && rs.getInt(1) == 0) {
            PreparedStatement psPatients = conn.prepareStatement("SELECT id FROM patients");
            ResultSet rsPatients = psPatients.executeQuery();
            ArrayList<Integer> patientIds = new ArrayList<>();
            while(rsPatients.next()) patientIds.add(rsPatients.getInt("id"));

            PreparedStatement psInsert = conn.prepareStatement(
                    "INSERT INTO appointments (patient_id, doctor_id, date) VALUES (?,?,CURDATE())"
            );
            for(Integer pid : patientIds) {
                psInsert.setInt(1, pid);
                psInsert.setInt(2, doctorId);
                psInsert.executeUpdate();
            }
        }
    }

    // ================= Refresh Dashboard Summary =================
    private void refreshDashboard() {
        getContentPane().removeAll();
        new DoctorDashboard(doctorId).setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DoctorDashboard(1).setVisible(true));
    }
}
