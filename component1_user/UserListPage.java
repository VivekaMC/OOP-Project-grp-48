package ui.component1_user;

import ui.AppState;
import ui.UITheme;
import model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Component 01 - User List Page (Admin View).
 * Displays all registered users with search and delete functionality.
 */
public class UserListPage extends JPanel {
    private final JTable userTable;
    private final DefaultTableModel tableModel;
    private final JTextField tfSearch;

    public UserListPage() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Header and Search bar
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel title = UITheme.createSectionTitle("User Management (Admin Console)");
        topPanel.add(title, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Search User:");
        lblSearch.setFont(UITheme.FONT_SUBHEADER);
        lblSearch.setForeground(UITheme.TEXT_MUTED);

        tfSearch = UITheme.createTextField();
        tfSearch.setPreferredSize(new Dimension(200, 36));

        JButton btnSearch = UITheme.createSecondaryButton("Filter");
        btnSearch.addActionListener(e -> refreshTable(tfSearch.getText().trim()));

        JButton btnReset = UITheme.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            tfSearch.setText("");
            refreshTable("");
        });

        searchPanel.add(lblSearch);
        searchPanel.add(tfSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);
        topPanel.add(searchPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"User ID", "Username", "Email", "Role", "Membership", "Badge / Privileges"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        userTable = new JTable(tableModel);
        userTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        userTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        userTable.getColumnModel().getColumn(1).setPreferredWidth(140); // Username
        userTable.getColumnModel().getColumn(2).setPreferredWidth(210); // Email
        userTable.getColumnModel().getColumn(3).setPreferredWidth(90);  // Role
        userTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Membership
        userTable.getColumnModel().getColumn(5).setPreferredWidth(180); // Badge

        JScrollPane scrollPane = UITheme.createTableScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomPanel.setOpaque(false);

        JButton btnDeleteUser = UITheme.createDangerButton("Delete Selected User");
        btnDeleteUser.addActionListener(e -> handleDeleteSelectedUser());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh List");
        btnRefresh.addActionListener(e -> refreshTable(tfSearch.getText().trim()));

        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnDeleteUser);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshTable("");
    }

    public void refreshTable(String query) {
        tableModel.setRowCount(0);
        List<User> list = AppState.getInstance().getUsers();

        for (User u : list) {
            if (query.isEmpty() || 
                u.getUsername().toLowerCase().contains(query.toLowerCase()) || 
                u.getUserId().toLowerCase().contains(query.toLowerCase())) {
                
                tableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getRole(),
                    u.getMembershipType(),
                    u.getProfileBadge()
                });
            }
        }
    }

    private void handleDeleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userId = (String) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        User currentUser = AppState.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUserId().equals(userId)) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own active admin account from here!", "Action Blocked", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete user: " + username + " (" + userId + ")?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            AppState.getInstance().getUserService().deleteUser(userId);
            JOptionPane.showMessageDialog(this, "User " + username + " has been removed.", "User Deleted", JOptionPane.INFORMATION_MESSAGE);
            refreshTable(tfSearch.getText().trim());
        }
    }
}
