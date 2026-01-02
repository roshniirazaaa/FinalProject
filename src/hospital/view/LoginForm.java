package hospital.view;

import hospital.controller.UserController;
import hospital.model.User;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class LoginForm extends JFrame {

    private JComboBox<String> cmbRole;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    // ----- BLUE COLORS -----
    private final Color PRIMARY_BLUE = new Color(0, 102, 204);       // button color / border
    private final Color LIGHT_BLUE_BG = new Color(225, 240, 255);    // background color

    public LoginForm() {
        setTitle("Hospital Management System - Login");
        setSize(460, 520);                        // thora bara kiya so look improve
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(LIGHT_BLUE_BG);   // whole background blue

        // ========= CARD PANEL =========
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(380, 470));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 4));   // blue border
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        add(card);

        // ========= LOGO =========
        JLabel lblLogo = new JLabel("", JLabel.CENTER);
        URL logoPath = getClass().getResource("/resources/H1.jpg");
        if (logoPath != null) {
            ImageIcon icon = new ImageIcon(logoPath);
            Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);  // thora bara
            lblLogo.setIcon(new ImageIcon(img));
        }
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(15));
        card.add(lblLogo);

        // ========= TITLE =========
        JLabel title = new JLabel("HOSPITAL MANAGEMENT SYSTEM");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(PRIMARY_BLUE);   // blue heading
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(10));
        card.add(title);
        card.add(Box.createVerticalStrut(25));

        // ========= FORM PANEL =========
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 15));
        form.setMaximumSize(new Dimension(300, 130));
        form.setBackground(Color.WHITE);

        Font f = new Font("Arial", Font.BOLD, 14);

        JLabel l1 = new JLabel("Select Role:");
        l1.setFont(f);
        form.add(l1);

        cmbRole = new JComboBox<>(new String[]{"Admin", "Doctor"});
        form.add(cmbRole);

        JLabel l2 = new JLabel("Username:");
        l2.setFont(f);
        form.add(l2);

        txtUsername = new JTextField();
        form.add(txtUsername);

        JLabel l3 = new JLabel("Password:");
        l3.setFont(f);
        form.add(l3);

        txtPassword = new JPasswordField();
        form.add(txtPassword);

        card.add(form);
        card.add(Box.createVerticalStrut(30));

        // ========= LOGIN BUTTON =========
        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 15));
        btnLogin.setPreferredSize(new Dimension(140, 38));   // thora chota & wide
        btnLogin.setFocusPainted(false);
        btnLogin.setBackground(PRIMARY_BLUE);
        btnLogin.setForeground(Color.WHITE);

        // hover effect
        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(new Color(0, 85, 170));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(PRIMARY_BLUE);
            }
        });

        btnLogin.addActionListener(e -> loginAction());

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnLogin);

        card.add(btnPanel);
        card.add(Box.createVerticalStrut(10));
    }

    private void loginAction() {
        String role = (String) cmbRole.getSelectedItem();
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        User user = UserController.login(username, password);

        if (user != null && user.getRole().equals(role)) {
            switch (user.getRole()) {
                case "Admin":
                    new AdminDashboard().setVisible(true);
                    break;
                case "Doctor":
                    new DoctorDashboard(user.getId()).setVisible(true);
                    break;
            }
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username, password, or role!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
