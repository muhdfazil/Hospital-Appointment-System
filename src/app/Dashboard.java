package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingUtilities;
import java.awt.*;
import javax.swing.*;

// CSV + File I/O
import java.io.File;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

// PDFBox
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


public class Dashboard extends JFrame {

    private JPanel sidePanel;
    private JPanel mainPanel; 
    private CardLayout cardLayout;

    // Buttons (side menu)
    private JButton btnAddPatient, btnViewPatients, btnAddDoctor, btnViewDoctors, btnBookAppointment, btnViewAppointments, btnLogout, btnRefresh, btnCreatePatientUser;
    private JTable patientsTable;
    private DefaultTableModel patientsModel;
    private JTable doctorsTable;
    private DefaultTableModel doctorsModel;


    private String userRole = "guest"; // default

    // --- Constructors ---
    public Dashboard(String role) {
        this.userRole = (role == null ? "guest" : role);
        Session.currentUserRole = this.userRole;
        initUI();
        applyRolePermissions();
    }


    // default constructor for testing
    public Dashboard() {
        this("admin");
    }

    // --- init UI ---
    private void initUI() {
        // DEBUG: quick DB connection test when Dashboard opens
        try (java.sql.Connection c = DBConnection.getConnection()) {
            System.out.println("DBG: Dashboard DB OK");
        } catch (Exception ex) {
            ex.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this, "DBG: Dashboard DB error: " + ex.getMessage());
        }

        // page background
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(240,240,240)); // light gray page

        
        setTitle("Hospital Appointment System - Dashboard");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null); // center
        setLayout(new BorderLayout());

        // Side panel (left)
        sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(240, getHeight()));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));
        sidePanel.setBackground(new Color(34, 45, 65));

        // Title on side panel
        JLabel lblAppTitle = new JLabel("<html><div style='text-align:center;'>Hospital<br>Appointment</div></html>");
        lblAppTitle.setForeground(Color.WHITE);
        lblAppTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblAppTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAppTitle.setBorder(BorderFactory.createEmptyBorder(4, 4, 20, 4));
        sidePanel.add(lblAppTitle);

        // Create buttons (style them)
        btnAddPatient      = makeMenuButton("Add Patient");
        btnViewPatients    = makeMenuButton("View Patients");
        btnAddDoctor       = makeMenuButton("Add Doctor");
        btnViewDoctors     = makeMenuButton("View Doctors");
        btnCreatePatientUser= makeMenuButton("Create Patient User"); 
        btnBookAppointment = makeMenuButton("Book Appointment");
        btnViewAppointments= makeMenuButton("View Appointments");
        btnLogout          = makeMenuButton("Logout");

        // add buttons in desired order
        sidePanel.add(btnAddPatient);
        sidePanel.add(Box.createVerticalStrut(8));
        sidePanel.add(btnViewPatients);
        sidePanel.add(Box.createVerticalStrut(8));
        sidePanel.add(btnAddDoctor);
        sidePanel.add(Box.createVerticalStrut(8));
        sidePanel.add(btnViewDoctors);
        sidePanel.add(Box.createVerticalStrut(8));
        sidePanel.add(btnCreatePatientUser); 
        sidePanel.add(Box.createVerticalStrut(8));
        sidePanel.add(btnBookAppointment);
        sidePanel.add(Box.createVerticalStrut(8));
        sidePanel.add(btnViewAppointments);
        sidePanel.add(Box.createVerticalGlue());
        sidePanel.add(btnLogout);

        add(sidePanel, BorderLayout.WEST);

        // Main panel with CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Placeholder panels
        mainPanel.add(makeLabelPanel("Welcome to Hospital Appointment System\n\nSelect an option from the left."), "HOME");
        mainPanel.add(makeCreatePatientUserPanel(), "CREATE_PATIENT_USER");
        mainPanel.add(makeAddPatientPanel(), "ADD_PATIENT");
        mainPanel.add(makeViewPatientsPanel(), "VIEW_PATIENTS");
        mainPanel.add(makeAddDoctorPanel(), "ADD_DOCTOR");
        mainPanel.add(makeViewDoctorsPanel(), "VIEW_DOCTORS");
        mainPanel.add(makeBookAppointmentPanel(), "BOOK_APPOINTMENT");
        mainPanel.add(makeViewAppointmentsPanel(), "VIEW_APPOINTMENTS");

        add(mainPanel, BorderLayout.CENTER);

        // Default show home or add patient
        cardLayout.show(mainPanel, "HOME");
        btnCreatePatientUser.addActionListener(e -> cardLayout.show(mainPanel, "CREATE_PATIENT_USER"));
        // Button listeners: switch cards
        btnAddPatient.addActionListener(e -> cardLayout.show(mainPanel, "ADD_PATIENT"));
        btnViewPatients.addActionListener(e -> cardLayout.show(mainPanel, "VIEW_PATIENTS"));
        btnAddDoctor.addActionListener(e -> cardLayout.show(mainPanel, "ADD_DOCTOR"));
        // btnViewDoctors = makeMenuButton("View Doctors");
        // sidePanel.add(btnViewDoctors);
        // sidePanel.add(Box.createVerticalStrut(8));
        btnViewDoctors.addActionListener(e -> cardLayout.show(mainPanel, "VIEW_DOCTORS"));
        btnBookAppointment.addActionListener(e -> cardLayout.show(mainPanel, "BOOK_APPOINTMENT"));
        btnViewAppointments.addActionListener(e -> cardLayout.show(mainPanel, "VIEW_APPOINTMENTS"));
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Logout and return to login?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new LoginForm().setVisible(true);
                this.dispose();
            }
        });
    }

    // --- Role permission application ---
    private void applyRolePermissions() {
        // admin = full access
        if (userRole.equalsIgnoreCase("admin")) {
            // nothing to hide
        } else if (userRole.equalsIgnoreCase("receptionist")) {
            // receptionists should not be able to add/remove doctors
            if (btnAddDoctor != null) btnAddDoctor.setVisible(false);
        } else if (userRole.equalsIgnoreCase("patient")) {

            // Patients should only be allowed to:
            // - Book appointments (but only for their own patient_id; booking form locks that field)
            // - View their appointments (view panel already enforces ownership checks)
            // - Logout

            if (btnAddDoctor != null) btnAddDoctor.setVisible(false);
            if (btnViewDoctors != null) btnViewDoctors.setVisible(false);
            if (btnCreatePatientUser != null) btnCreatePatientUser.setVisible(false);
            if (btnAddPatient != null) btnAddPatient.setVisible(false);
            if (btnViewPatients != null) btnViewPatients.setVisible(false);

            // Booking visible for patients (they will only be able to book for their own id).
            if (btnBookAppointment != null) btnBookAppointment.setVisible(true);

            // View appointments are visible so patient can see their own appointments
            if (btnViewAppointments != null) btnViewAppointments.setVisible(true);
   
        } else { // guest or unknown
            if (btnAddDoctor != null) btnAddDoctor.setVisible(false);
            if (btnViewPatients != null) btnViewPatients.setVisible(false);
            if (btnAddPatient != null) btnAddPatient.setVisible(false);
        }
    }

    // Helper to create the styled menu button
    private JButton makeMenuButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(200, 40));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Colors 
        Color bg = new Color(60, 90, 140);
        Color hover = new Color(78, 110, 170);
        Color fg = Color.WHITE;

        b.setBackground(bg);
        b.setForeground(fg);
        b.setBorder(new RoundedBorder(10, new Color(40, 60, 100), 1));
        b.setFont(new Font("SansSerif", Font.PLAIN, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(hover);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(bg);
            }
        });

        return b;
    }

    // Simple home panel with centered text
    private JPanel makeLabelPanel(String msg) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel("<html><div style='font-size:14px;'>" + msg.replaceAll("\n", "<br>") + "</div></html>");
        l.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    // ---- Panels ----

    private JPanel makeCreatePatientUserPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Box.createVerticalStrut(12));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx=0; g.gridy=0; form.add(new JLabel("Patient ID:"), g);
        g.gridx=1; g.gridy=0; JTextField tPatientId = new JTextField(8); form.add(tPatientId, g);

        g.gridx=0; g.gridy=1; form.add(new JLabel("Username:"), g);
        g.gridx=1; g.gridy=1; JTextField tUsername = new JTextField(16); form.add(tUsername, g);

        g.gridx=0; g.gridy=2; form.add(new JLabel("Password:"), g);
        g.gridx=1; g.gridy=2; JPasswordField tPassword = new JPasswordField(16); form.add(tPassword, g);

        g.gridx=0; g.gridy=3; form.add(new JLabel("Confirm Password:"), g);
        g.gridx=1; g.gridy=3; JPasswordField tConfirm = new JPasswordField(16); form.add(tConfirm, g);

        g.gridx=1; g.gridy=4; g.anchor = GridBagConstraints.EAST;
        JButton btnCreate = new JButton("Create Patient User");
        form.add(btnCreate, g);

        // Info label
        g.gridx=0; g.gridy=5; g.gridwidth=2;
        JLabel info = new JLabel("<html><i>Only admin/receptionist can create patient users. Patient must exist in patients table.</i></html>");
        form.add(info, g);

        // Action
        btnCreate.addActionListener(ev -> {
            String role = Session.currentUserRole == null ? "guest" : Session.currentUserRole;
            if (!role.equalsIgnoreCase("admin") && !role.equalsIgnoreCase("receptionist")) {
                JOptionPane.showMessageDialog(this, "Access denied. Only admin/receptionist can create patient users.");
                return;
            }

            String pidStr = tPatientId.getText().trim();
            String username = tUsername.getText().trim();
            String pass = new String(tPassword.getPassword()).trim();
            String conf = new String(tConfirm.getPassword()).trim();

            if (pidStr.isEmpty() || username.isEmpty() || pass.isEmpty() || conf.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields required.");
                return;
            }
            if (!pass.equals(conf)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.");
                return;
            }

            int pid;
            try { pid = Integer.parseInt(pidStr); } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Patient ID must be a number.");
                return;
            }

            // Check patient exists
            String chk = "SELECT COUNT(*) FROM patients WHERE patient_id = ?";
            String ins = "INSERT INTO users (username, password, role, patient_ref_id) VALUES (?, ?, 'patient', ?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pchk = con.prepareStatement(chk)) {

                pchk.setInt(1, pid);
                try (ResultSet rs = pchk.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        JOptionPane.showMessageDialog(this, "Patient ID does not exist. Create patient first.");
                        return;
                    }
                }

                // try insert - check username uniqueness first
                String existsQ = "SELECT COUNT(*) FROM users WHERE username = ?";
                try (PreparedStatement pex = con.prepareStatement(existsQ)) {
                    pex.setString(1, username);
                    try (ResultSet r2 = pex.executeQuery()) {
                        r2.next();
                        if (r2.getInt(1) > 0) {
                            JOptionPane.showMessageDialog(this, "Username already taken. Choose another.");
                            return;
                        }
                    }
                }

                try (PreparedStatement pins = con.prepareStatement(ins)) {
                    pins.setString(1, username);
                    pins.setString(2, pass);
                    pins.setInt(3, pid);
                    int aff = pins.executeUpdate();
                    if (aff > 0) {
                        JOptionPane.showMessageDialog(this, "Patient user created successfully.");
                        tPatientId.setText(""); tUsername.setText(""); tPassword.setText(""); tConfirm.setText("");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to create user.");
                    }
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
            }
        });

        p.add(form);
        return makeCardPanel("Create Patient User Account", p);
    }


    private JPanel makeAddPatientPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Box.createVerticalStrut(12));

        // form
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Name:"), g);
        g.gridx = 1; g.gridy = 0; JTextField txtName = new JTextField(20); form.add(txtName, g);

        g.gridx = 0; g.gridy = 1; form.add(new JLabel("Age:"), g);
        g.gridx = 1; g.gridy = 1; JTextField txtAge = new JTextField(6); form.add(txtAge, g);

        g.gridx = 0; g.gridy = 2; form.add(new JLabel("Gender:"), g);
        g.gridx = 1; g.gridy = 2; JComboBox<String> cmbGender = new JComboBox<>(new String[] {"Male","Female","Other"}); form.add(cmbGender, g);

        g.gridx = 0; g.gridy = 3; form.add(new JLabel("Phone:"), g);
        g.gridx = 1; g.gridy = 3; JTextField txtPhone = new JTextField(12); form.add(txtPhone, g);

        g.gridx = 0; g.gridy = 4; form.add(new JLabel("Address:"), g);
        g.gridx = 1; g.gridy = 4; JTextField txtAddress = new JTextField(20); form.add(txtAddress, g);

        g.gridx = 0; g.gridy = 5; form.add(new JLabel("Link to user (optional):"), g);
        g.gridx = 1; g.gridy = 5; JTextField txtUserRef = new JTextField(6); txtUserRef.setToolTipText("Enter user_id if creating a linked user"); form.add(txtUserRef, g);

        g.gridx = 1; g.gridy = 6; g.anchor = GridBagConstraints.EAST;
        JButton btnSave = new JButton("Save Patient");
        form.add(btnSave, g);

        // Save button action -> INSERT into patients
        btnSave.addActionListener(ev -> {
            // Role check: receptionist and admin can add patients
            String role = Session.currentUserRole == null ? "guest" : Session.currentUserRole;
            if (!role.equalsIgnoreCase("admin") && !role.equalsIgnoreCase("receptionist")) {
                JOptionPane.showMessageDialog(this, "Access denied. Only admin/receptionist can add patients.");
                return;
            }

            String name = txtName.getText().trim();
            String ageStr = txtAge.getText().trim();
            String gender = (String) cmbGender.getSelectedItem();
            String phone = txtPhone.getText().trim();
            String address = txtAddress.getText().trim();
            String userRefStr = txtUserRef.getText().trim();

            if (name.isEmpty() || ageStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Name and Age.");
                return;
            }

            int age = 0;
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Age must be a number.");
                return;
            }

            // Insert into DB
            String sql = "INSERT INTO patients (name, age, gender, phone, address) VALUES (?, ?, ?, ?, ?)";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pst.setString(1, name);
                pst.setInt(2, age);
                pst.setString(3, gender);
                pst.setString(4, phone);
                pst.setString(5, address);

                int affected = pst.executeUpdate();
                if (affected == 0) {
                    JOptionPane.showMessageDialog(this, "Failed to save patient.");
                    return;
                }

                // get generated patient_id
                try (ResultSet keys = pst.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newPatientId = keys.getInt(1);
                        // optionally link user -> (if userRefStr provided)
                        if (!userRefStr.isEmpty()) {
                            try {
                                int userId = Integer.parseInt(userRefStr);
                                String upd = "UPDATE users SET patient_ref_id = ? WHERE user_id = ?";
                                try (PreparedStatement pst2 = con.prepareStatement(upd)) {
                                    pst2.setInt(1, newPatientId);
                                    pst2.setInt(2, userId);
                                    pst2.executeUpdate();
                                }
                            } catch (NumberFormatException ex) {
                                // ignore invalid user id input
                            }
                        }
                        // If current user is a patient and logged in, and they created themself, set session
                        if ("patient".equalsIgnoreCase(Session.currentUserRole)) {
                            Session.currentPatientId = newPatientId;
                        }

                        JOptionPane.showMessageDialog(this, "Patient saved with ID: " + newPatientId);
                    } else {
                        JOptionPane.showMessageDialog(this, "Patient saved, but couldn't read ID.");
                    }
                }

                // clear fields
                txtName.setText("");
                txtAge.setText("");
                txtPhone.setText("");
                txtAddress.setText("");
                txtUserRef.setText("");

                // refresh table if visible
                SwingUtilities.invokeLater(() -> {
                    loadPatients();
                    cardLayout.show(mainPanel, "VIEW_PATIENTS");
                });

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
            }
        });

        p.add(form);
        return makeCardPanel("Add Patient", p);
    }

    private JPanel makeCardPanel(String titleText, JComponent content) {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(240, 240, 240)); // page background

        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(700, 420));
        card.setBackground(Color.WHITE);
        card.setBorder(new RoundedBorder(14, new Color(220,220,220), 1));
        card.setOpaque(true);

        // header
        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(16,16,8,16));

        card.add(title, BorderLayout.NORTH);

        // content wrapper with padding
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(12, 24, 24, 24));
        wrapper.add(content, BorderLayout.CENTER);

        card.add(wrapper, BorderLayout.CENTER);

        root.add(card);
        return root;
    }

    
    private JPanel makeViewPatientsPanel() {
        JPanel p = new JPanel(new BorderLayout());

        // Table and model (columns)
        String[] cols = {"patient_id","name","age","gender","phone","address"};
        patientsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // disallow direct cell edits here
            }
        };
        patientsTable = new JTable(patientsModel);
        JScrollPane sp = new JScrollPane(patientsTable);

        // Control panel with refresh and delete
        JPanel controls = new JPanel();
        JButton btnRefresh = new JButton("Refresh");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnExport = new JButton("Export CSV");
        JButton btnPrint = new JButton("Print");
        controls.add(btnExport);
        controls.add(btnPrint);
        controls.add(btnRefresh);
        controls.add(btnDelete);

        p.add(sp, BorderLayout.CENTER);
        p.add(controls, BorderLayout.SOUTH);

        // Refresh action -> load from DB
        btnRefresh.addActionListener(e -> loadPatients());
        btnExport.addActionListener(e -> exportPatientsCSV());
        btnPrint.addActionListener(e -> printPatients());
        // Delete selected row action
        btnDelete.addActionListener(e -> {
            int sel = patientsTable.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(this, "Select a row to delete.");
                return;
            }

            // role check - only admin/receptionist can delete
            String role = Session.currentUserRole == null ? "guest" : Session.currentUserRole;
            if (!role.equalsIgnoreCase("admin") && !role.equalsIgnoreCase("receptionist")) {
                JOptionPane.showMessageDialog(this, "Access denied. Only admin/receptionist can delete patients.");
                return;
            }

            int id = Integer.parseInt(patientsModel.getValueAt(sel, 0).toString());
            int conf = JOptionPane.showConfirmDialog(this, "Delete patient ID " + id + " ? This will also remove related appointments.", "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION) return;

            String del = "DELETE FROM patients WHERE patient_id = ?";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(del)) {
                pst.setInt(1, id);
                int affected = pst.executeUpdate();
                if (affected > 0) {
                    JOptionPane.showMessageDialog(this, "Patient deleted.");
                    loadPatients();
                } else {
                    JOptionPane.showMessageDialog(this, "Delete failed.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
            }
        });

        // Initial load
        loadPatients();

        return makeCardPanel("View Patients", p);
    }

    private JPanel makeAddDoctorPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(Box.createVerticalStrut(12));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Name:"), g);
        g.gridx = 1; g.gridy = 0; JTextField txtName = new JTextField(20); form.add(txtName, g);

        g.gridx = 0; g.gridy = 1; form.add(new JLabel("Specialization:"), g);
        g.gridx = 1; g.gridy = 1; JTextField txtSpec = new JTextField(20); form.add(txtSpec, g);

        g.gridx = 0; g.gridy = 2; form.add(new JLabel("Phone:"), g);
        g.gridx = 1; g.gridy = 2; JTextField txtPhone = new JTextField(20); form.add(txtPhone, g);

        g.gridx = 1; g.gridy = 3; g.anchor = GridBagConstraints.EAST;
        JButton btnSave = new JButton("Save Doctor");
        form.add(btnSave, g);

        // --- Save Doctor (ADMIN ONLY) ---
        btnSave.addActionListener(e -> {
            if (!Session.currentUserRole.equalsIgnoreCase("admin")) {
                JOptionPane.showMessageDialog(this, "Only ADMIN can add doctors.");
                return;
            }

            String name = txtName.getText().trim();
            String spec = txtSpec.getText().trim();
            String phone = txtPhone.getText().trim();

            if (name.isEmpty() || spec.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter name & specialization.");
                return;
            }

            String sql = "INSERT INTO doctors (name, specialization, phone) VALUES (?, ?, ?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(sql)) {

                pst.setString(1, name);
                pst.setString(2, spec);
                pst.setString(3, phone);

                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Doctor Added!");

                txtName.setText("");
                txtSpec.setText("");
                txtPhone.setText("");

                SwingUtilities.invokeLater(() -> {
                    loadDoctors();
                    cardLayout.show(mainPanel, "VIEW_DOCTORS");
                });

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        p.add(form);
        return makeCardPanel("Add Doctor", p);
    }

    private JPanel makeViewDoctorsPanel() {
        JPanel p = new JPanel(new BorderLayout());

        String[] cols = {"doctor_id","name","specialization","phone"};
        doctorsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        doctorsTable = new JTable(doctorsModel);
        JScrollPane sp = new JScrollPane(doctorsTable);

        JPanel controls = new JPanel();
        JButton btnRefresh = new JButton("Refresh");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnExport = new JButton("Export CSV");
        JButton btnPrint = new JButton("Print");
        controls.add(btnExport);
        controls.add(btnPrint);
        controls.add(btnRefresh);
        controls.add(btnDelete);

        p.add(sp, BorderLayout.CENTER);
        p.add(controls, BorderLayout.SOUTH);

        // Refresh
        btnRefresh.addActionListener(e -> loadDoctors());
        
        btnExport.addActionListener(e -> exportDoctorsCSV());
        btnPrint.addActionListener(e -> printDoctors());
        
        // Delete (ADMIN ONLY)
        btnDelete.addActionListener(e -> {
            if (!Session.currentUserRole.equalsIgnoreCase("admin")) {
                JOptionPane.showMessageDialog(this, "Only ADMIN can delete doctors.");
                return;
            }

            int row = doctorsTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a row first.");
                return;
            }

            int id = Integer.parseInt(doctorsModel.getValueAt(row, 0).toString());
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete Doctor ID " + id + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) return;

            String del = "DELETE FROM doctors WHERE doctor_id = ?";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(del)) {

                pst.setInt(1, id);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Doctor Deleted.");
                loadDoctors();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        // initial load
        loadDoctors();
        return makeCardPanel("View Doctors", p);
    }
    

    private JPanel makeBookAppointmentPanel() {
        JPanel p = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx=0; g.gridy=0; form.add(new JLabel("Patient ID:"), g);
        g.gridx=1; g.gridy=0; JTextField tPid = new JTextField(10); form.add(tPid, g);

        g.gridx=0; g.gridy=1; form.add(new JLabel("Doctor ID:"), g);
        g.gridx=1; g.gridy=1; JTextField tDid = new JTextField(10); form.add(tDid, g);

        g.gridx=0; g.gridy=2; form.add(new JLabel("Date (YYYY-MM-DD):"), g);
        g.gridx=1; g.gridy=2; JTextField tDate = new JTextField(10); form.add(tDate, g);

        g.gridx=0; g.gridy=3; form.add(new JLabel("Time (e.g. 10:30 AM):"), g);
        g.gridx=1; g.gridy=3; JTextField tTime = new JTextField(12); form.add(tTime, g);

        g.gridx=0; g.gridy=4; form.add(new JLabel("Symptoms (optional):"), g);
        g.gridx=1; g.gridy=4; JTextField tSymptoms = new JTextField(30); form.add(tSymptoms, g);

        g.gridx=1; g.gridy=5; g.anchor = GridBagConstraints.EAST;
        JButton btnBook = new JButton("Book Appointment");
        form.add(btnBook, g);

        // Pre-fill / lock patient id if logged-in user is a patient
        if ("patient".equalsIgnoreCase(Session.currentUserRole) && Session.currentPatientId > 0) {
            tPid.setText(String.valueOf(Session.currentPatientId));
            tPid.setEditable(false);
        }

        btnBook.addActionListener(e -> {
            // Role check
            String role = Session.currentUserRole == null ? "guest" : Session.currentUserRole;
            if (!role.equalsIgnoreCase("admin") && !role.equalsIgnoreCase("receptionist") && !role.equalsIgnoreCase("patient")) {
                JOptionPane.showMessageDialog(this, "Access denied. Please login with a valid user.");
                return;
            }

            String pidStr = tPid.getText().trim();
            String didStr = tDid.getText().trim();
            String dateStr = tDate.getText().trim();
            String timeStr = tTime.getText().trim();
            String symptoms = tSymptoms.getText().trim();

            if (pidStr.isEmpty() || didStr.isEmpty() || dateStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter Patient ID, Doctor ID and Date.");
                return;
            }

            int pid, did;
            try {
                pid = Integer.parseInt(pidStr);
                did = Integer.parseInt(didStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Patient ID and Doctor ID must be numbers.");
                return;
            }

            // If current user is patient, ensure they book only for themselves
            if ("patient".equalsIgnoreCase(role) && Session.currentPatientId > 0 && pid != Session.currentPatientId) {
                JOptionPane.showMessageDialog(this, "Patients can only book for their own ID.");
                return;
            }

            // ---- SAFE DATE PARSER ----
            java.sql.Date sqlDate;
            try {
                String s = (dateStr == null ? "" : dateStr.trim());
                if (s.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Date required.");
                    return;
                }

                java.time.LocalDate ld = null;

                java.time.format.DateTimeFormatter[] fmts = {
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                    java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                    java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy")
                };

                for (var f : fmts) {
                    try { ld = java.time.LocalDate.parse(s, f); break; }
                    catch (Exception ignore) {}
                }

                if (ld == null) {
                    try { ld = java.time.LocalDate.parse(s); } catch (Exception ignore) {}
                }

                if (ld == null) {
                    JOptionPane.showMessageDialog(this,
                        "Unrecognized date. Use YYYY-MM-DD or DD-MM-YYYY or MM/DD/YYYY.");
                    return;
                }

                sqlDate = java.sql.Date.valueOf(ld);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date: " + ex.getMessage());
                return;
            }

            // Check patient exists and doctor exists
            String chkP = "SELECT COUNT(*) FROM patients WHERE patient_id = ?";
            String chkD = "SELECT COUNT(*) FROM doctors WHERE doctor_id = ?";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pChkP = con.prepareStatement(chkP);
                 PreparedStatement pChkD = con.prepareStatement(chkD)) {

                pChkP.setInt(1, pid);
                try (ResultSet rs = pChkP.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        JOptionPane.showMessageDialog(this, "Patient ID does not exist.");
                        return;
                    }
                }

                pChkD.setInt(1, did);
                try (ResultSet rs2 = pChkD.executeQuery()) {
                    rs2.next();
                    if (rs2.getInt(1) == 0) {
                        JOptionPane.showMessageDialog(this, "Doctor ID does not exist.");
                        return;
                    }
                }

                // Insert appointment
                String ins = "INSERT INTO appointments (patient_id, doctor_id, date, time, symptoms) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pst = con.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                    pst.setInt(1, pid);
                    pst.setInt(2, did);
                    pst.setDate(3, sqlDate);
                    pst.setString(4, timeStr);
                    pst.setString(5, symptoms);

                    int aff = pst.executeUpdate();
                    if (aff > 0) {
                        try (ResultSet keys = pst.getGeneratedKeys()) {
                            if (keys.next()) {
                                int apptId = keys.getInt(1);
                                JOptionPane.showMessageDialog(this, "Appointment booked (ID: " + apptId + ")");
                            } else {
                                JOptionPane.showMessageDialog(this, "Appointment booked.");
                            }
                        }
                        // clear fields (if patient, keep pid locked)
                        if (!"patient".equalsIgnoreCase(role)) tPid.setText("");
                        tDid.setText("");
                        tDate.setText("");
                        tTime.setText("");
                        tSymptoms.setText("");

                        // refresh appointments table and switch view
                        SwingUtilities.invokeLater(() -> {
                            btnRefresh.doClick();   // auto-refresh via the View Appointments panel
                            cardLayout.show(mainPanel, "VIEW_APPOINTMENTS");
                        });
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to book appointment.");
                    }
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
            }
        });

        p.add(form, BorderLayout.CENTER);
        return makeCardPanel("Book Appointment", p);
    }


    private String formatDateFromResultSet(ResultSet rs, String col) throws SQLException {
        Object obj = rs.getObject(col);
        if (obj == null) return "";
        try {
            // java.sql.Date or Timestamp
            if (obj instanceof java.sql.Date) return obj.toString();
            if (obj instanceof java.sql.Timestamp) {
                java.time.LocalDate ld = ((java.sql.Timestamp) obj).toLocalDateTime().toLocalDate();
                return ld.toString();
            }

            // Numeric epoch stored as integer/long (seconds or milliseconds)
            if (obj instanceof Number) {
                long v = ((Number) obj).longValue();
                // heuristics: if < 1e12 treat as seconds, otherwise ms
                if (v < 1000000000000L) v = v * 1000L;
                java.time.Instant ins = java.time.Instant.ofEpochMilli(v);
                java.time.LocalDate ld = java.time.LocalDateTime.ofInstant(ins, java.time.ZoneId.systemDefault()).toLocalDate();
                return ld.toString();
            }

            // string: try numeric parse first (epoch), then known formats
            String s = obj.toString().trim();
            if (s.isEmpty()) return "";
            try {
                long lv = Long.parseLong(s);
                long v = lv;
                if (v < 1000000000000L) v = v * 1000L;
                java.time.Instant ins = java.time.Instant.ofEpochMilli(v);
                java.time.LocalDate ld = java.time.LocalDateTime.ofInstant(ins, java.time.ZoneId.systemDefault()).toLocalDate();
                return ld.toString();
            } catch (Exception ignore) {}

            // Try several date formats
            java.time.format.DateTimeFormatter[] fmts = new java.time.format.DateTimeFormatter[] {
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                java.time.format.DateTimeFormatter.ISO_DATE_TIME,
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
            };

            for (var f : fmts) {
                try {
                    java.time.LocalDate ld = java.time.LocalDate.parse(s, f);
                    return ld.toString();
                } catch (Exception ignore) {}
                // If pattern had time, try LocalDateTime then extract date
                try {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(s, f);
                    return ldt.toLocalDate().toString();
                } catch (Exception ignore) {}
            }

            return s;

        } catch (Exception ex) {
            // return original string
            try { return rs.getString(col); } catch (Exception ignore) { return ""; }
        }
    }

    private JPanel makeViewAppointmentsPanel() {
        JPanel p = new JPanel(new BorderLayout());

        String[] cols = {"appointment_id","patient_name","doctor_name","date","time","symptoms"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);

        JPanel controls = new JPanel();
        JButton btnRefresh = new JButton("Refresh");
        JButton btnEdit = new JButton("Edit Selected");
        JButton btnDelete = new JButton("Delete Selected"); 
        JButton btnExport = new JButton("Export CSV");
        JButton btnExportPdf = new JButton("Export PDF");
        JButton btnPrint = new JButton("Print");
        // order: Export CSV, Export PDF, Print, Refresh, Edit, Delete
        btnExport.setPreferredSize(new Dimension(110, 28));
        btnExportPdf.setPreferredSize(new Dimension(110, 28));
        btnPrint.setPreferredSize(new Dimension(80, 28));
        btnRefresh.setPreferredSize(new Dimension(80, 28));
        btnEdit.setPreferredSize(new Dimension(100, 28));
        btnDelete.setPreferredSize(new Dimension(120, 28));

        controls.add(btnExport);
        controls.add(btnExportPdf);
        controls.add(btnPrint);
        controls.add(btnRefresh);
        controls.add(btnEdit);
        controls.add(btnDelete); 

        p.add(sp, BorderLayout.CENTER);
        p.add(controls, BorderLayout.SOUTH);
        
        btnExport.addActionListener(e -> exportAppointmentsCSV());
        btnPrint.addActionListener(e -> printAppointments());
        btnExportPdf.addActionListener(e -> exportAppointmentsPDF());
        
        // Refresh action: refill model
        btnRefresh.addActionListener(e -> {
            model.setRowCount(0);
            String sql = "SELECT a.appointment_id, p.name AS patient_name, d.name AS doctor_name, a.date, a.time, a.symptoms, a.patient_id " +
                         "FROM appointments a " +
                         "LEFT JOIN patients p ON a.patient_id = p.patient_id " +
                         "LEFT JOIN doctors d ON a.doctor_id = d.doctor_id " +
                         "ORDER BY a.appointment_id DESC";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String dateStr = formatDateFromResultSet(rs, "date");
                    model.addRow(new Object[] {
                        rs.getInt("appointment_id"),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        dateStr,
                        rs.getString("time"),
                        rs.getString("symptoms")
                    });
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
            }
        });

        // Edit action: opens a small dialog to change date/time (with role checks)
        btnEdit.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel == -1) { JOptionPane.showMessageDialog(this, "Select a row to edit."); return; }
            int apptId = Integer.parseInt(model.getValueAt(sel, 0).toString());

            // Get appointment's patient_id to check ownership
            int apptPatientId = -1;
            String q = "SELECT patient_id, date, time FROM appointments WHERE appointment_id = ?";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(q)) {
                pst.setInt(1, apptId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        apptPatientId = rs.getInt("patient_id");
                    } else {
                        JOptionPane.showMessageDialog(this, "Appointment not found.");
                        return;
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
                return;
            }

            String role = Session.currentUserRole == null ? "guest" : Session.currentUserRole;
            boolean allowed = role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("receptionist")
                              || (role.equalsIgnoreCase("patient") && Session.currentPatientId == apptPatientId);
            if (!allowed) { JOptionPane.showMessageDialog(this, "Access denied."); return; }

            // Build dialog
            JDialog d = new JDialog(this, "Edit Appointment " + apptId, true);
            d.setSize(360,200);
            d.setLocationRelativeTo(this);
            JPanel fp = new JPanel(new GridBagLayout());
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6,6,6,6);
            g.anchor = GridBagConstraints.WEST;

            g.gridx=0; g.gridy=0; fp.add(new JLabel("Date (YYYY-MM-DD):"), g);
            g.gridx=1; g.gridy=0; JTextField tDate = new JTextField(12); fp.add(tDate, g);

            g.gridx=0; g.gridy=1; fp.add(new JLabel("Time (e.g. 10:30 AM):"), g);
            g.gridx=1; g.gridy=1; JTextField tTime = new JTextField(12); fp.add(tTime, g);

            // Load existing values safely
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement("SELECT date, time FROM appointments WHERE appointment_id = ?")) {
                pst.setInt(1, apptId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        String dateStr = formatDateFromResultSet(rs, "date");
                        tDate.setText(dateStr == null ? "" : dateStr);
                        tTime.setText(rs.getString("time"));
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            JButton btnSave = new JButton("Save");
            JButton btnCancel = new JButton("Cancel");
            JPanel bot = new JPanel();
            bot.add(btnSave);
            bot.add(btnCancel);

            d.getContentPane().setLayout(new BorderLayout());
            d.getContentPane().add(fp, BorderLayout.CENTER);
            d.getContentPane().add(bot, BorderLayout.SOUTH);

            btnCancel.addActionListener(ae -> d.dispose());

            btnSave.addActionListener(ae -> {
                String dateStr = tDate.getText().trim();
                String timeStr = tTime.getText().trim();
                if (dateStr.isEmpty()) { JOptionPane.showMessageDialog(d, "Date required."); return; }
                
                java.sql.Date sqlDate;
                try {
                    String s = (dateStr == null ? "" : dateStr.trim());
                    if (s.isEmpty()) {
                        JOptionPane.showMessageDialog(d, "Date required.");
                        return;
                    }

                    java.time.LocalDate ld = null;

                    java.time.format.DateTimeFormatter[] fmts = {
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                        java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                        java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy")
                    };

                    for (var f : fmts) {
                        try { ld = java.time.LocalDate.parse(s, f); break; }
                        catch (Exception ignore) {}
                    }

                    if (ld == null) {
                        try { ld = java.time.LocalDate.parse(s); } catch (Exception ignore) {}
                    }

                    if (ld == null) {
                        JOptionPane.showMessageDialog(d,
                            "Unrecognized date. Use YYYY-MM-DD or DD-MM-YYYY or MM/DD/YYYY.");
                        return;
                    }

                    sqlDate = java.sql.Date.valueOf(ld);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(d, "Invalid date: " + ex.getMessage());
                    return;
                }

                String upd = "UPDATE appointments SET date = ?, time = ? WHERE appointment_id = ?";
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement pst = con.prepareStatement(upd)) {
                    pst.setDate(1, sqlDate);
                    pst.setString(2, timeStr);
                    pst.setInt(3, apptId);
                    int aff = pst.executeUpdate();
                    if (aff > 0) {
                        JOptionPane.showMessageDialog(d, "Appointment updated.");
                        d.dispose();
                        btnRefresh.doClick(); // refresh the table
                    } else {
                        JOptionPane.showMessageDialog(d, "Update failed.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(d, "DB Error: " + ex.getMessage());
                }
            });

            d.setVisible(true);
        });

        // DELETE appointment action 
        btnDelete.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel == -1) { JOptionPane.showMessageDialog(this, "Select an appointment to delete."); return; }
            int apptId = Integer.parseInt(model.getValueAt(sel, 0).toString());

            // check ownership/permission
            int apptPatientId = -1;
            String q2 = "SELECT patient_id FROM appointments WHERE appointment_id = ?";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(q2)) {
                pst.setInt(1, apptId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) apptPatientId = rs.getInt("patient_id");
                }
            } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage()); return; }

            String role = Session.currentUserRole == null ? "guest" : Session.currentUserRole;
            boolean allowed = false;
            if (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("receptionist")) allowed = true;
            if (role.equalsIgnoreCase("patient") && Session.currentPatientId == apptPatientId) allowed = true;
            if (!allowed) { JOptionPane.showMessageDialog(this, "Access denied."); return; }

            int conf = JOptionPane.showConfirmDialog(this, "Delete appointment " + apptId + " ?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION) return;

            String del = "DELETE FROM appointments WHERE appointment_id = ?";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(del)) {
                pst.setInt(1, apptId);
                int aff = pst.executeUpdate();
                if (aff > 0) {
                    JOptionPane.showMessageDialog(this, "Appointment deleted.");
                    btnRefresh.doClick();
                } else {
                    JOptionPane.showMessageDialog(this, "Delete failed.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
            }
        });

        // initial load
        btnRefresh.doClick();

        // stored btnRefresh so other methods (book appointment) can call it
        this.btnRefresh = btnRefresh;

        return makeCardPanel("View Appointments", p);
    }

        private void loadPatients() {
        // clear model
        if (patientsModel == null) return;
        patientsModel.setRowCount(0);

        String sql = "SELECT patient_id, name, age, gender, phone, address FROM patients ORDER BY patient_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Object[] row = new Object[] {
                    rs.getInt("patient_id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("gender"),
                    rs.getString("phone"),
                    rs.getString("address")
                };
                patientsModel.addRow(row);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            
            // JOptionPane.showMessageDialog(this, "DB Error while loading patients: " + ex.getMessage());
        }
    }

            private void loadDoctors() {
        if (doctorsModel == null) return;
        doctorsModel.setRowCount(0);

        String sql = "SELECT doctor_id, name, specialization, phone FROM doctors ORDER BY doctor_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                doctorsModel.addRow(new Object[]{
                    rs.getInt("doctor_id"),
                    rs.getString("name"),
                    rs.getString("specialization"),
                    rs.getString("phone")
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // --- CSV export helper ------------------------------------------------
    private boolean writeCsvFile(File f, String[] headers, java.util.List<String[]> rows) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(f), "UTF-8"))) {
            // header
            pw.println(String.join(",", headers));
            for (String[] r : rows) {
                // escaped any double-quotes and wrap fields in quotes
                for (int i = 0; i < r.length; i++) {
                    if (r[i] == null) r[i] = "";
                    r[i] = "\"" + r[i].replace("\"", "\"\"") + "\"";
                }
                pw.println(String.join(",", r));
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Export error: " + ex.getMessage());
            return false;
        }
    }

    // quick file chooser (returns selected file or null)
    private File askSaveFile(String defaultName) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(defaultName));
        int r = fc.showSaveDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            // add .csv if missing
            if (!f.getName().toLowerCase().endsWith(".csv")) f = new File(f.getAbsolutePath() + ".csv");
            return f;
        }
        return null;
    }

    private void exportPatientsCSV() {
        File f = askSaveFile("patients.csv");
        if (f == null) return;
        String sql = "SELECT patient_id, name, age, gender, phone, address FROM patients ORDER BY patient_id";
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[] {
                    String.valueOf(rs.getInt("patient_id")),
                    rs.getString("name"),
                    String.valueOf(rs.getInt("age")),
                    rs.getString("gender"),
                    rs.getString("phone"),
                    rs.getString("address")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
            return;
        }
        String[] hdr = {"patient_id","name","age","gender","phone","address"};
        if (writeCsvFile(f, hdr, rows)) JOptionPane.showMessageDialog(this, "Saved: " + f.getAbsolutePath());
    }
    
    private void exportDoctorsCSV() {
        File f = askSaveFile("doctors.csv");
        if (f == null) return;
        String sql = "SELECT doctor_id, name, specialization, phone FROM doctors ORDER BY doctor_id";
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[] {
                    String.valueOf(rs.getInt("doctor_id")),
                    rs.getString("name"),
                    rs.getString("specialization"),
                    rs.getString("phone")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
            return;
        }
        String[] hdr = {"doctor_id","name","specialization","phone"};
        if (writeCsvFile(f, hdr, rows)) JOptionPane.showMessageDialog(this, "Saved: " + f.getAbsolutePath());
    }

    private void exportAppointmentsCSV() {
        File f = askSaveFile("appointments.csv");
        if (f == null) return;
        String sql = "SELECT a.appointment_id, p.name AS patient_name, d.name AS doctor_name, a.date, a.time, a.symptoms " +
                     "FROM appointments a LEFT JOIN patients p ON a.patient_id = p.patient_id " +
                     "LEFT JOIN doctors d ON a.doctor_id = d.doctor_id ORDER BY a.appointment_id";
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                String dateStr = formatDateFromResultSet(rs, "date");
                rows.add(new String[] {
                    String.valueOf(rs.getInt("appointment_id")),
                    rs.getString("patient_name"),
                    rs.getString("doctor_name"),
                    dateStr,
                    rs.getString("time"),
                    rs.getString("symptoms")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
            return;
        }
        String[] hdr = {"appointment_id","patient_name","doctor_name","date","time","symptoms"};
        if (writeCsvFile(f, hdr, rows)) JOptionPane.showMessageDialog(this, "Saved: " + f.getAbsolutePath());
    }

    // Prints patients table 
    private void printPatients() {
        try {
            if (patientsTable != null) patientsTable.print();
        } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage()); }
    }

    private void printDoctors() {
        try {
            if (doctorsTable != null) doctorsTable.print();
        } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage()); }
    }

    // For appointments, this creates a temporary JTable from query and prints it
    private void printAppointments() {
        DefaultTableModel m = new DefaultTableModel(new String[] {"ID","Patient","Doctor","Date","Time","Symptoms"}, 0);
        String sql = "SELECT a.appointment_id, p.name AS patient_name, d.name AS doctor_name, a.date, a.time, a.symptoms " +
                     "FROM appointments a LEFT JOIN patients p ON a.patient_id = p.patient_id " +
                     "LEFT JOIN doctors d ON a.doctor_id = d.doctor_id ORDER BY a.appointment_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                String dateStr = formatDateFromResultSet(rs, "date");
                m.addRow(new Object[] {
                    rs.getInt("appointment_id"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_name"),
                    dateStr,
                    rs.getString("time"),
                    rs.getString("symptoms")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
            return;
        }
        JTable tmp = new JTable(m);
        try { tmp.print(); } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage()); }
    }
   
    // file chooser for PDF (returns file with .pdf extension)
    private File askSaveFilePdf(String defaultName) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(defaultName));
        int r = fc.showSaveDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".pdf")) f = new File(f.getAbsolutePath() + ".pdf");
            return f;
        }
        return null;
    }
    
    private void exportAppointmentsPDF() {
        File f = askSaveFilePdf("appointments.pdf");
        if (f == null) return;

        String sql = "SELECT a.appointment_id, p.name AS patient_name, d.name AS doctor_name, a.date, a.time, a.symptoms " +
                     "FROM appointments a LEFT JOIN patients p ON a.patient_id = p.patient_id " +
                     "LEFT JOIN doctors d ON a.doctor_id = d.doctor_id ORDER BY a.appointment_id";

        PDDocument doc = null;
        PDPageContentStream cs = null;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            doc = new PDDocument();
            PDPage page = new PDPage();
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);

            // Starting coordinates
            float margin = 50;
            float y = page.getMediaBox().getHeight() - margin;
            float leading = 16; // line spacing

            // Title
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
            cs.newLineAtOffset(margin, y);
            cs.showText("Appointments Report");
            cs.endText();
            y -= leading + 6;

            // Header
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
            cs.newLineAtOffset(margin, y);
            String header = String.format("%-8s %-20s %-20s %-12s %-10s %-20s", "ID", "Patient", "Doctor", "Date", "Time", "Symptoms");
            cs.showText(header);
            cs.endText();
            y -= leading;

            // Divider line
            cs.moveTo(margin, y);
            cs.lineTo(page.getMediaBox().getWidth() - margin, y);
            cs.stroke();
            y -= leading;

            // Rows
            cs.setFont(PDType1Font.HELVETICA, 10);
            while (rs.next()) {
                int id = rs.getInt("appointment_id");
                String pName = rs.getString("patient_name") == null ? "" : rs.getString("patient_name");
                String dName = rs.getString("doctor_name") == null ? "" : rs.getString("doctor_name");
                String date = formatDateFromResultSet(rs, "date");
                String time = rs.getString("time") == null ? "" : rs.getString("time");
                String sym = rs.getString("symptoms") == null ? "" : rs.getString("symptoms");

                String row = String.format("%-8s %-20s %-20s %-12s %-10s %-20s",
                        String.valueOf(id),
                        truncateForPdf(pName, 18),
                        truncateForPdf(dName, 18),
                        date,
                        truncateForPdf(time, 9),
                        truncateForPdf(sym, 18)
                );

                // If not enough space on page, create a new one
                if (y < 70) {
                    cs.close();
                    page = new PDPage();
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = page.getMediaBox().getHeight() - margin;
                }

                cs.beginText();
                cs.newLineAtOffset(margin, y);
                cs.showText(row);
                cs.endText();
                y -= leading;
            }

            // finish and save
            cs.close();
            doc.save(f);
            doc.close();
            JOptionPane.showMessageDialog(this, "PDF saved: " + f.getAbsolutePath());

        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
            try { if (cs != null) cs.close(); } catch (IOException ignored) {}
            try { if (doc != null) doc.close(); } catch (IOException ignored) {}
            JOptionPane.showMessageDialog(this, "PDF export failed: " + ex.getMessage());
        }
    }

    // small util used above to keep fields short in the table
    private String truncateForPdf(String s, int max) {
        if (s == null) return "";
        s = s.replaceAll("\\s+", " ").trim();
        return s.length() <= max ? s : s.substring(0, max-1) + "…";
    }
    
    
            
    // Main for quick testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Dashboard().setVisible(true);
        });
    }
}
