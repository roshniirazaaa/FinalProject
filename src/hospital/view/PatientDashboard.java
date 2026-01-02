package hospital.view;

import javax.swing.*;
import java.awt.*;

public class PatientDashboard extends JFrame {

    private int patientId;
    private JButton btnBookAppointment, btnViewPending, btnLogout;

    public PatientDashboard(int patientId) {
        this.patientId = patientId;

        setTitle("Patient Dashboard");
        setSize(750, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Welcome label
        JLabel lblWelcome = new JLabel("Welcome, Patient!", JLabel.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 28));
        lblWelcome.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(lblWelcome, BorderLayout.NORTH);

        // Panel for buttons
        JPanel panelButtons = new JPanel(new GridLayout(3, 1, 10, 10));
        panelButtons.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));

        btnBookAppointment = new JButton("Book Appointment");
        btnViewPending = new JButton("View Pending Appointments");
        btnLogout = new JButton("Logout");

        panelButtons.add(btnBookAppointment);
        panelButtons.add(btnViewPending);
        panelButtons.add(btnLogout);

        add(panelButtons, BorderLayout.CENTER);

        // Button actions
        btnBookAppointment.addActionListener(e -> new BookAppointmentForm(patientId).setVisible(true));
        btnViewPending.addActionListener(e -> new PendingAppointmentsForm(patientId).setVisible(true));
        btnLogout.addActionListener(e -> {
            new LoginForm().setVisible(true);
            this.dispose();
        });
    }
}
