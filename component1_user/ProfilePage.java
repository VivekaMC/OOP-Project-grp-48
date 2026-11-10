package ui.component1_user;

import ui.AppState;
import ui.UITheme;
import model.User;
import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Component 01 - Profile View / Edit Page.
 * Encapsulation: Edits profile with getters/setters.
 * Polymorphism: Different profile display per user type.
 */
public class ProfilePage extends JPanel {
    private final JLabel lblBadge;
    private final JLabel lblUserId;
    private final JLabel lblUsername;
    private final JTextField tfEmail;
    private final JPasswordField pfPassword;
    private final JComboBox<String> cbMembership;
    private final JLabel lblPermissions;
    private final Consumer<String> pageNavigator;

    public ProfilePage(Consumer<String> pageNavigator) {
        this.pageNavigator = pageNavigator;
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = UITheme.createSectionTitle("My Account Profile");
        headerPanel.add(title, BorderLayout.WEST);

        lblBadge = new JLabel();
        lblBadge.setFont(UITheme.FONT_SUBHEADER);
        lblBadge.setForeground(UITheme.WARNING_COLOR);
        headerPanel.add(lblBadge, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Profile Card
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.weightx = 1.0;

        // User ID
        gbc.gridx = 0; gbc.gridy = 0;
        card.add(UITheme.createFieldLabel("Account ID:"), gbc);
        gbc.gridx = 1;
        lblUserId = new JLabel();
        lblUserId.setFont(UITheme.FONT_BOLD);
        lblUserId.setForeground(UITheme.TEXT_MAIN);
        card.add(lblUserId, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = 1;
        card.add(UITheme.createFieldLabel("Username:"), gbc);
        gbc.gridx = 1;
        lblUsername = new JLabel();
        lblUsername.setFont(UITheme.FONT_BOLD);
        lblUsername.setForeground(UITheme.TEXT_MAIN);
        card.add(lblUsername, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        card.add(UITheme.createFieldLabel("Email Address:"), gbc);
        gbc.gridx = 1;
        tfEmail = UITheme.createTextField();
        card.add(tfEmail, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 3;
        card.add(UITheme.createFieldLabel("New Password:"), gbc);
        gbc.gridx = 1;
        pfPassword = UITheme.createPasswordField();
        card.add(pfPassword, gbc);

        // Membership
        gbc.gridx = 0; gbc.gridy = 4;
        card.add(UITheme.createFieldLabel("Membership Tier:"), gbc);
        gbc.gridx = 1;
        cbMembership = new JComboBox<>(new String[]{"Standard", "Premium", "VIP", "Staff"});
        UITheme.styleComboBox(cbMembership);
        card.add(cbMembership, gbc);

        // Permissions summary (Polymorphic method output)
        gbc.gridx = 0; gbc.gridy = 5;
        card.add(UITheme.createFieldLabel("Access Privileges:"), gbc);
        gbc.gridx = 1;
        lblPermissions = new JLabel();
        lblPermissions.setFont(UITheme.FONT_SMALL);
        lblPermissions.setForeground(UITheme.TEXT_MUTED);
        card.add(lblPermissions, gbc);

        // Buttons row
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnPanel.setOpaque(false);

        JButton btnDelete = UITheme.createDangerButton("Delete Account");
        btnDelete.addActionListener(e -> handleDeleteAccount());

        JButton btnSave = UITheme.createSuccessButton("Save Profile Changes");
        btnSave.addActionListener(e -> handleSaveChanges());

        btnPanel.add(btnDelete);
        btnPanel.add(btnSave);

        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 15, 10, 15);
        card.add(btnPanel, gbc);

        add(card, BorderLayout.CENTER);

        // Refresh when loaded
        refreshProfile();
    }

    public void refreshProfile() {
        User user = AppState.getInstance().getCurrentUser();
        if (user != null) {
            lblUserId.setText(user.getUserId());
            lblUsername.setText(user.getUsername());
            tfEmail.setText(user.getEmail());
            pfPassword.setText(user.getPassword());
            cbMembership.setSelectedItem(user.getMembershipType());
            lblBadge.setText(user.getProfileBadge()); // Polymorphic
            lblPermissions.setText("<html>" + user.getPermissionsSummary() + "</html>"); // Polymorphic
        }
    }

    private void handleSaveChanges() {
        User user = AppState.getInstance().getCurrentUser();
        if (user == null) return;

        String newEmail = tfEmail.getText().trim();
        String newPass = new String(pfPassword.getPassword()).trim();
        String newTier = cbMembership.getSelectedItem().toString();

        if (newEmail.isEmpty() || newPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be blank.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean updated = AppState.getInstance().getUserService().updateProfile(user.getUserId(), newEmail, newPass, newTier);

        if (updated) {
            AppState.getInstance().notifyListeners();
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshProfile();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteAccount() {
        User user = AppState.getInstance().getCurrentUser();
        if (user == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to permanently delete your account (" + user.getUsername() + ")?",
            "Confirm Account Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            AppState.getInstance().getUserService().deleteUser(user.getUserId());
            AppState.getInstance().setCurrentUser(null);
            JOptionPane.showMessageDialog(this, "Account deleted successfully.", "Account Deleted", JOptionPane.INFORMATION_MESSAGE);
            pageNavigator.accept("LOGIN");
        }
    }
}
