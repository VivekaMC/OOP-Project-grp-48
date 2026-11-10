package ui.component2_movie;

import ui.AppState;
import ui.UITheme;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Component 02 - My Rentals / Return Page.
 * Displays rentals, calculates late fees, processes returns and deletions.
 */
public class MyRentalsPage extends JPanel {
    private final JTable rentalTable;
    private final DefaultTableModel tableModel;

    public MyRentalsPage() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel title = UITheme.createSectionTitle("My Rentals & Returns");
        topPanel.add(title, BorderLayout.WEST);

        JLabel info = new JLabel("Track rental due dates, pay late-fees & return items");
        info.setFont(UITheme.FONT_SUBHEADER);
        info.setForeground(UITheme.TEXT_MUTED);
        topPanel.add(info, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Rental ID", "Movie Title", "Rental Date", "Due Date", "Fee", "Status", "Days Overdue", "Late Fee Due"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        rentalTable = new JTable(tableModel);
        rentalTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        rentalTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Rental ID
        rentalTable.getColumnModel().getColumn(1).setPreferredWidth(180); // Movie Title
        rentalTable.getColumnModel().getColumn(2).setPreferredWidth(95);  // Rental Date
        rentalTable.getColumnModel().getColumn(3).setPreferredWidth(95);  // Due Date
        rentalTable.getColumnModel().getColumn(4).setPreferredWidth(70);  // Fee
        rentalTable.getColumnModel().getColumn(5).setPreferredWidth(160); // Status
        rentalTable.getColumnModel().getColumn(6).setPreferredWidth(95);  // Days Overdue
        rentalTable.getColumnModel().getColumn(7).setPreferredWidth(95);  // Late Fee Due

        JScrollPane scrollPane = UITheme.createTableScrollPane(rentalTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottomPanel.setOpaque(false);

        JButton btnReturn = UITheme.createSuccessButton("Return Selected Movie");
        btnReturn.addActionListener(e -> handleReturnMovie());

        JButton btnDeleteRental = UITheme.createDangerButton("Delete Rental Record");
        btnDeleteRental.addActionListener(e -> handleDeleteRental());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshTable());

        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnReturn);
        bottomPanel.add(btnDeleteRental);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshTable();
    }

    // READ OPERATION
    // Retrieve rental records and display them in the table
    public void refreshTable() {
        tableModel.setRowCount(0);
        AppState state = AppState.getInstance();
        User currentUser = state.getCurrentUser();
        if (currentUser == null) return;

        // READ: Get all rental records from the system
        List<Rental> list = state.getRentals();
        for (Rental r : list) {
            // Admin sees all rentals, regular user sees their own
            if (currentUser.canManageSystem() || r.getUserId().equals(currentUser.getUserId())) {
                int overdue = r.getDaysOverdue();
                double lateFee = 0.0;
                
                // Calculate polymorphic late fee based on movie type
                if (overdue > 0) {
                    Movie m = state.getMovies().stream()
                        .filter(mov -> mov.getMovieId().equals(r.getMovieId()))
                        .findFirst().orElse(null);
                    if (m != null) {
                        lateFee = m.calculateLateFee(overdue, r.isPremiumRental());
                    } else {
                        lateFee = overdue * 2.0;
                    }
                }

                String displayStatus = r.getStatus();
                if ("ACTIVE".equals(r.getStatus()) && overdue > 0) {
                    displayStatus = "[OVERDUE] (" + overdue + " days)";
                }

                tableModel.addRow(new Object[]{
                    r.getRentalId(),
                    r.getMovieTitle(),
                    r.getRentalDate(),
                    r.getDueDate(),
                    String.format("$%.2f", r.getRentalFee()),
                    displayStatus,
                    overdue > 0 ? overdue + " days" : "On Time",
                    String.format("$%.2f", lateFee)
                });
            }
        }
    }

    // UPDATE OPERATION
    // Updates rental status from ACTIVE to RETURNED
    // Calculates and stores any late fees
    private void handleReturnMovie() {
        int selectedRow = rentalTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a rental item to return.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rentalId = (String) tableModel.getValueAt(selectedRow, 0);
        Rental returnedRental = AppState.getInstance().getMovieRentalService().returnMovie(rentalId);

        if (returnedRental == null) {
            JOptionPane.showMessageDialog(this, "This movie has already been returned or record not found!", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        AppState.getInstance().notifyListeners();
        refreshTable();

        String msg = "Movie successfully returned!";
        if (returnedRental.getLateFee() > 0) {
            msg += "\nLate Fee Incurred: $" + String.format("%.2f", returnedRental.getLateFee()) + " for " + returnedRental.getDaysOverdue() + " overdue day(s).";
        }
        JOptionPane.showMessageDialog(this, msg, "Return Successful", JOptionPane.INFORMATION_MESSAGE);
    }

    // DELETE OPERATION
    // Removes a rental record from the system
    private void handleDeleteRental() {
        int selectedRow = rentalTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a rental record to delete.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rentalId = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete rental record " + rentalId + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            AppState.getInstance().getMovieRentalService().deleteRental(rentalId);
            AppState.getInstance().notifyListeners();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Rental record removed.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
