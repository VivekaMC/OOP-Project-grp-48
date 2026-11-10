package ui.component4_dashboard;

import ui.AppState;
import ui.UITheme;
import model.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Component 04 - Admin Dashboard.
 * Demonstrates OOP Polymorphism: Calls the same summary method (getSummaryStats())
 * on each module's class, and each returns its own stats differently.
 */
public class AdminDashboardPage extends JPanel {
    private final JPanel kpiContainer;
    private final JPanel polymorphicModulesContainer;
    private final Consumer<String> pageNavigator;
    private final List<DashboardStatProvider> statProviders = new ArrayList<>();

    public AdminDashboardPage(Consumer<String> pageNavigator) {
        this.pageNavigator = pageNavigator;
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Initialize Polymorphic Providers
        setupPolymorphicProviders();

        // Top Header
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel title = UITheme.createSectionTitle("System Executive Admin Dashboard");
        titleRow.add(title, BorderLayout.WEST);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Metrics");
        btnRefresh.addActionListener(e -> refreshDashboard());
        titleRow.add(btnRefresh, BorderLayout.EAST);
        topPanel.add(titleRow);
        topPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // KPI Metric Cards row
        kpiContainer = new JPanel(new GridLayout(1, 4, 15, 0));
        kpiContainer.setOpaque(false);
        topPanel.add(kpiContainer);
        topPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Quick Links Navigation Bar
        JPanel quickLinksCard = UITheme.createCardPanel();
        quickLinksCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 6));

        JLabel qlTitle = new JLabel("Quick Admin Links:");
        qlTitle.setFont(UITheme.FONT_BOLD);
        qlTitle.setForeground(UITheme.TEXT_MAIN);
        quickLinksCard.add(qlTitle);

        JButton qlUsers = UITheme.createSecondaryButton("User Directory");
        qlUsers.addActionListener(e -> pageNavigator.accept("USERS"));

        JButton qlMovies = UITheme.createSecondaryButton("Catalog Management");
        qlMovies.addActionListener(e -> pageNavigator.accept("ADD_MOVIE"));

        JButton qlModeration = UITheme.createSecondaryButton("Review Moderation");
        qlModeration.addActionListener(e -> pageNavigator.accept("MODERATION"));

        JButton qlRentals = UITheme.createSecondaryButton("All Rental Records");
        qlRentals.addActionListener(e -> pageNavigator.accept("RENTALS"));

        quickLinksCard.add(qlUsers);
        quickLinksCard.add(qlMovies);
        quickLinksCard.add(qlModeration);
        quickLinksCard.add(qlRentals);
        topPanel.add(quickLinksCard);

        add(topPanel, BorderLayout.NORTH);

        // Center: Polymorphic Module Breakdown Grid
        polymorphicModulesContainer = new JPanel(new GridLayout(2, 2, 16, 16));
        polymorphicModulesContainer.setBackground(UITheme.BG_DARK);

        JScrollPane scrollPane = new JScrollPane(polymorphicModulesContainer);
        scrollPane.getViewport().setBackground(UITheme.BG_DARK);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        refreshDashboard();
    }

    private void setupPolymorphicProviders() {
        AppState s = AppState.getInstance();
        statProviders.clear();
        statProviders.add(s.getUserService());
        statProviders.add(s.getMovieRentalService());
        statProviders.add(s.getReviewService());
        statProviders.add(s.getWatchlistService());
    }

    public void refreshDashboard() {
        AppState state = AppState.getInstance();

        // 1. Refresh Top KPI Cards
        kpiContainer.removeAll();
        long usersCount = state.getUsers().size();
        long moviesCount = state.getMovies().size();
        long activeRentals = state.getRentals().stream().filter(r -> "ACTIVE".equals(r.getStatus())).count();
        double totalRev = state.getRentals().stream().mapToDouble(Rental::getRentalFee).sum();

        kpiContainer.add(createKpiCard("TOTAL USERS", String.valueOf(usersCount), UITheme.ACCENT_BLUE));
        kpiContainer.add(createKpiCard("CATALOG TITLES", String.valueOf(moviesCount), UITheme.WARNING_COLOR));
        kpiContainer.add(createKpiCard("ACTIVE RENTALS", String.valueOf(activeRentals), UITheme.ACCENT_RED));
        kpiContainer.add(createKpiCard("TOTAL REVENUE", String.format("$%.2f", totalRev), UITheme.SUCCESS_COLOR));
        kpiContainer.revalidate();

        // 2. Refresh Polymorphic Module Cards
        polymorphicModulesContainer.removeAll();
        for (DashboardStatProvider provider : statProviders) {
            polymorphicModulesContainer.add(createModuleStatCard(provider));
        }

        polymorphicModulesContainer.revalidate();
        polymorphicModulesContainer.repaint();
    }

    private JPanel createKpiCard(String label, String value, Color accent) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 0, 0, 0, accent),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(UITheme.TEXT_MAIN);

        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    private JPanel createModuleStatCard(DashboardStatProvider provider) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        JLabel titleLbl = new JLabel(provider.getModuleName());
        titleLbl.setFont(UITheme.FONT_SUBHEADER);
        titleLbl.setForeground(UITheme.TEXT_MAIN);
        card.add(titleLbl, BorderLayout.NORTH);

        // Polymorphic Call: provider.getSummaryStats()
        Map<String, String> stats = provider.getSummaryStats();
        JPanel grid = new JPanel(new GridLayout(stats.size(), 2, 8, 8));
        grid.setOpaque(false);

        for (Map.Entry<String, String> entry : stats.entrySet()) {
            JLabel k = new JLabel(entry.getKey() + ":");
            k.setFont(UITheme.FONT_BODY);
            k.setForeground(UITheme.TEXT_MUTED);

            JLabel v = new JLabel(entry.getValue());
            v.setFont(UITheme.FONT_BOLD);
            v.setForeground(UITheme.TEXT_MAIN);

            grid.add(k);
            grid.add(v);
        }

        card.add(grid, BorderLayout.CENTER);
        return card;
    }
}
