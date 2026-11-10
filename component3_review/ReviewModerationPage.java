package ui.component3_review;

import ui.AppState;
import ui.UITheme;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Component 03 - Admin Review Moderation Panel.
 * Admin view to moderate and delete inappropriate community reviews.
 */
public class ReviewModerationPage extends JPanel {
    private final JTable reviewTable;
    private final DefaultTableModel tableModel;
    private final Consumer<String> pageNavigator;

    public ReviewModerationPage(Consumer<String> pageNavigator) {
        this.pageNavigator = pageNavigator;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = UITheme.createSectionTitle("Review Moderation Panel (Admin)");
        headerPanel.add(title, BorderLayout.WEST);

        JButton btnBack = UITheme.createSecondaryButton("Back to Reviews");
        btnBack.addActionListener(e -> pageNavigator.accept("REVIEWS"));
        headerPanel.add(btnBack, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"Review ID", "Movie ID", "User ID", "Username", "Stars", "Type", "Comments", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        reviewTable = new JTable(tableModel);
        reviewTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        reviewTable.getColumnModel().getColumn(0).setPreferredWidth(85);  // Review ID
        reviewTable.getColumnModel().getColumn(1).setPreferredWidth(85);  // Movie ID
        reviewTable.getColumnModel().getColumn(2).setPreferredWidth(85);  // User ID
        reviewTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Username
        reviewTable.getColumnModel().getColumn(4).setPreferredWidth(75);  // Stars
        reviewTable.getColumnModel().getColumn(5).setPreferredWidth(95);  // Type
        reviewTable.getColumnModel().getColumn(6).setPreferredWidth(300); // Comments
        reviewTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Date

        JScrollPane scrollPane = UITheme.createTableScrollPane(reviewTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottomPanel.setOpaque(false);

        JButton btnDelete = UITheme.createDangerButton("Delete Inappropriate Review");
        btnDelete.addActionListener(e -> handleDeleteReview());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshTable());

        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnDelete);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Review> list = AppState.getInstance().getReviews();
        for (Review r : list) {
            tableModel.addRow(new Object[]{
                r.getReviewId(),
                r.getMovieId(),
                r.getUserId(),
                r.getUsername(),
                r.getRating() + " / 5",
                r.getReviewType(),
                r.getComment(),
                r.getReviewDate()
            });
        }
    }

    private void handleDeleteReview() {
        int selectedRow = reviewTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a review to moderate/delete.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reviewId = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to remove review (" + reviewId + ") for inappropriate content?",
            "Confirm Moderation Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            AppState.getInstance().getReviewService().deleteReview(reviewId);
            AppState.getInstance().notifyListeners();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Review removed by administrator.", "Moderated", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
