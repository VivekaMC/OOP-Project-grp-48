package ui.component4_dashboard;

import ui.AppState;
import ui.UITheme;
import ui.component2_movie.RentMovieDialog;
import model.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/**
 * Component 04 - Recommendations Page.
 * Abstraction: Generates personalized movie recommendations 
 * without exposing how internal data modules are structured or stored.
 */
public class RecommendationsPage extends JPanel {
    private final JPanel cardsContainer;
    private final Consumer<String> pageNavigator;

    public RecommendationsPage(Consumer<String> pageNavigator) {
        this.pageNavigator = pageNavigator;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel title = UITheme.createSectionTitle("Personalized Movie Recommendations");
        topPanel.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Curated based on your preferences, top ratings & trending titles");
        subtitle.setFont(UITheme.FONT_SUBHEADER);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        topPanel.add(subtitle, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        cardsContainer = new JPanel(new GridLayout(0, 3, 18, 18));
        cardsContainer.setBackground(UITheme.BG_DARK);
        cardsContainer.setBorder(BorderFactory.createEmptyBorder(10, 5, 15, 5));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BG_DARK);
        wrapper.add(cardsContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.getViewport().setBackground(UITheme.BG_DARK);
        scrollPane.setBackground(UITheme.BG_DARK);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        UITheme.styleScrollBar(scrollPane.getVerticalScrollBar());
        UITheme.styleScrollBar(scrollPane.getHorizontalScrollBar());
        add(scrollPane, BorderLayout.CENTER);

        generateRecommendations();
    }

    /**
     * Abstraction Method: Recommendation algorithm.
     * Evaluates data from all modules (genres, ratings, rentals) through abstract logic.
     */
    public void generateRecommendations() {
        cardsContainer.removeAll();
        AppState state = AppState.getInstance();
        java.util.List<java.util.Map.Entry<Movie, String>> recs = state.getWatchlistService().generateRecommendations(
            state.getMovieRentalService(),
            state.getReviewService()
        );

        for (java.util.Map.Entry<Movie, String> entry : recs) {
            cardsContainer.add(createRecommendationCard(entry.getKey(), entry.getValue()));
        }

        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private JPanel createRecommendationCard(Movie movie, String reason) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(280, 230));

        // Reason pill
        JLabel pill = new JLabel(reason);
        pill.setFont(UITheme.FONT_SMALL);
        pill.setForeground(UITheme.WARNING_COLOR);
        card.add(pill);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        // Movie Title
        JLabel title = new JLabel(movie.getTitle());
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_MAIN);
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 4)));

        // Meta
        JLabel meta = new JLabel(movie.getGenre() + " • " + movie.getReleaseYear() + " • " + movie.getFormatDetails());
        meta.setFont(UITheme.FONT_SMALL);
        meta.setForeground(UITheme.TEXT_LIGHT);
        card.add(meta);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        // Price
        JLabel price = new JLabel(String.format("Rental: $%.2f", movie.getBasePrice()));
        price.setFont(UITheme.FONT_BOLD);
        price.setForeground(UITheme.SUCCESS_COLOR);
        card.add(price);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        // Actions
        JPanel actPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        actPanel.setOpaque(false);
        actPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        actPanel.setPreferredSize(new Dimension(240, 36));

        JButton btnRent = UITheme.createPrimaryButton("Rent Now");
        btnRent.addActionListener(e -> {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            RentMovieDialog dialog = new RentMovieDialog(topFrame, movie);
            dialog.setVisible(true);
        });

        JButton btnWatch = UITheme.createSecondaryButton("+ Watchlist");
        btnWatch.addActionListener(e -> {
            User u = AppState.getInstance().getCurrentUser();
            if (u != null) {
                boolean added = AppState.getInstance().getWatchlistService().addToWatchlist(u.getUserId(), movie);
                if (added) {
                    JOptionPane.showMessageDialog(this, "Added \"" + movie.getTitle() + "\" to your Watchlist!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Movie is already in your Watchlist!", "Notice", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        actPanel.add(btnRent);
        actPanel.add(btnWatch);
        card.add(actPanel);

        return card;
    }
}
