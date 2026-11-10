package ui.component4_dashboard;

import ui.AppState;
import ui.UITheme;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Component 04 - Watchlist Page.
 * Encapsulation: WatchlistEntry stores data privately.
 * CRUD: Add, Read, Update (watched / order), Delete.
 */
public class WatchlistPage extends JPanel {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final Consumer<String> pageNavigator;

    public WatchlistPage(Consumer<String> pageNavigator) {
        this.pageNavigator = pageNavigator;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel title = UITheme.createSectionTitle("My Saved Watchlist");
        topPanel.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Plan your viewing queue, prioritize titles & track watched movies");
        subtitle.setFont(UITheme.FONT_SUBHEADER);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        topPanel.add(subtitle, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"Priority", "Watchlist ID", "Movie Title", "Genre", "Added Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(75);  // Priority
        table.getColumnModel().getColumn(1).setPreferredWidth(95);  // Watchlist ID
        table.getColumnModel().getColumn(2).setPreferredWidth(220); // Title
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Genre
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Date
        table.getColumnModel().getColumn(5).setPreferredWidth(140); // Status

        JScrollPane scrollPane = UITheme.createTableScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);

        JButton btnToggleWatched = UITheme.createPrimaryButton("Toggle Watched");
        btnToggleWatched.addActionListener(e -> handleToggleWatched());

        JButton btnMoveUp = UITheme.createSecondaryButton("Priority Up");
        btnMoveUp.addActionListener(e -> handleReorder(-1));

        JButton btnMoveDown = UITheme.createSecondaryButton("Priority Down");
        btnMoveDown.addActionListener(e -> handleReorder(1));

        JButton btnRemove = UITheme.createDangerButton("Remove");
        btnRemove.addActionListener(e -> handleRemove());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshTable());

        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnMoveUp);
        bottomPanel.add(btnMoveDown);
        bottomPanel.add(btnToggleWatched);
        bottomPanel.add(btnRemove);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        AppState state = AppState.getInstance();
        User currentUser = state.getCurrentUser();
        if (currentUser == null) return;

        List<WatchlistEntry> list = state.getWatchlistService().getUserWatchlist(currentUser.getUserId());

        for (WatchlistEntry w : list) {
            tableModel.addRow(new Object[]{
                "#" + w.getPriorityOrder(),
                w.getWatchlistId(),
                w.getMovieTitle(),
                w.getGenre(),
                w.getAddedDate(),
                w.isWatched() ? "[ Watched ]" : "[ Plan to Watch ]"
            });
        }
    }

    private void handleToggleWatched() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a watchlist item.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String wId = (String) tableModel.getValueAt(selectedRow, 1);
        AppState.getInstance().getWatchlistService().toggleWatched(wId);
        refreshTable();
    }

    private void handleReorder(int direction) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a watchlist item to move.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String wId = (String) tableModel.getValueAt(selectedRow, 1);
        AppState.getInstance().getWatchlistService().reorderPriority(wId, direction);
        refreshTable();
    }

    private void handleRemove() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a watchlist item to remove.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String wId = (String) tableModel.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Remove movie from your watchlist?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            AppState.getInstance().getWatchlistService().removeFromWatchlist(wId);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Removed from watchlist.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
