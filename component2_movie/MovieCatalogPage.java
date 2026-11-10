package ui.component2_movie;

import ui.AppState;
import ui.UITheme;
import model.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Component 02 - Movie Catalog / Search Page.
 * Displays movies in dynamic cards with search and filtering.
 */
public class MovieCatalogPage extends JPanel {
    private final JTextField tfSearch;
    private final JComboBox<String> cbGenre;
    private final JComboBox<String> cbType;
    private final JPanel moviesContainer;
    private final Consumer<String> pageNavigator;
    private final JButton btnAddMovie;

    public MovieCatalogPage(Consumer<String> pageNavigator) {
        this.pageNavigator = pageNavigator;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Top Header and Filters
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel lblTitle = UITheme.createSectionTitle("Movie Catalog & Streaming Hub");
        titleRow.add(lblTitle, BorderLayout.WEST);

        btnAddMovie = UITheme.createPrimaryButton("+ Add New Movie (Admin)");
        btnAddMovie.addActionListener(e -> pageNavigator.accept("ADD_MOVIE"));
        titleRow.add(btnAddMovie, BorderLayout.EAST);
        topPanel.add(titleRow);
        topPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Filter Bar
        JPanel filterBar = UITheme.createCardPanel();
        filterBar.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 5));

        filterBar.add(UITheme.createFieldLabel("Search:"));
        tfSearch = UITheme.createTextField();
        tfSearch.setPreferredSize(new Dimension(180, 36));
        filterBar.add(tfSearch);

        filterBar.add(UITheme.createFieldLabel("Genre:"));
        cbGenre = new JComboBox<>(new String[]{"All Genres", "Sci-Fi", "Action", "Crime", "Animation", "Biography", "Drama", "Horror"});
        UITheme.styleComboBox(cbGenre);
        cbGenre.setPreferredSize(new Dimension(140, 36));
        filterBar.add(cbGenre);

        filterBar.add(UITheme.createFieldLabel("Type:"));
        cbType = new JComboBox<>(new String[]{"All Formats", "Digital", "Physical"});
        UITheme.styleComboBox(cbType);
        cbType.setPreferredSize(new Dimension(140, 36));
        filterBar.add(cbType);

        JButton btnApply = UITheme.createPrimaryButton("Filter");
        btnApply.addActionListener(e -> renderMovies());
        filterBar.add(btnApply);

        JButton btnReset = UITheme.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            tfSearch.setText("");
            cbGenre.setSelectedIndex(0);
            cbType.setSelectedIndex(0);
            renderMovies();
        });
        filterBar.add(btnReset);

        topPanel.add(filterBar);
        add(topPanel, BorderLayout.NORTH);

        // Center: Movies Grid Container inside a wrapper
        moviesContainer = new JPanel();
        moviesContainer.setLayout(new GridLayout(0, 3, 18, 18));
        moviesContainer.setBackground(UITheme.BG_DARK);
        moviesContainer.setBorder(BorderFactory.createEmptyBorder(10, 5, 15, 5));

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UITheme.BG_DARK);
        wrapperPanel.add(moviesContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.getViewport().setBackground(UITheme.BG_DARK);
        scrollPane.setBackground(UITheme.BG_DARK);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        UITheme.styleScrollBar(scrollPane.getVerticalScrollBar());
        UITheme.styleScrollBar(scrollPane.getHorizontalScrollBar());
        add(scrollPane, BorderLayout.CENTER);

        renderMovies();
    }

    public void renderMovies() {
        moviesContainer.removeAll();

        AppState state = AppState.getInstance();
        User currentUser = state.getCurrentUser();
        btnAddMovie.setVisible(currentUser != null && currentUser.canManageSystem());

        String query = tfSearch.getText().trim().toLowerCase();
        String selectedGenre = cbGenre.getSelectedItem().toString();
        String selectedType = cbType.getSelectedItem().toString();

        List<Movie> filtered = state.getMovies().stream()
            .filter(m -> {
                boolean matchesQuery = query.isEmpty() || 
                    m.getTitle().toLowerCase().contains(query) ||
                    m.getGenre().toLowerCase().contains(query) ||
                    String.valueOf(m.getReleaseYear()).contains(query);

                boolean matchesGenre = selectedGenre.equals("All Genres") || m.getGenre().equalsIgnoreCase(selectedGenre);
                boolean matchesType = selectedType.equals("All Formats") || m.getMovieType().equalsIgnoreCase(selectedType);

                return matchesQuery && matchesGenre && matchesType;
            })
            .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            emptyPanel.setOpaque(false);
            JLabel emptyLbl = new JLabel("No movies found matching your search criteria.");
            emptyLbl.setFont(UITheme.FONT_SUBHEADER);
            emptyLbl.setForeground(UITheme.TEXT_MUTED);
            emptyPanel.add(emptyLbl);
            moviesContainer.setLayout(new BorderLayout());
            moviesContainer.add(emptyPanel, BorderLayout.CENTER);
        } else {
            moviesContainer.setLayout(new GridLayout(0, 3, 18, 18));
            for (Movie movie : filtered) {
                moviesContainer.add(createMovieCard(movie));
            }
        }

        moviesContainer.revalidate();
        moviesContainer.repaint();
    }

    private JPanel createMovieCard(Movie movie) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(280, 260));

        // Format Pill Header
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(240, 24));
        JLabel iconLbl = new JLabel(movie instanceof DigitalMovie ? "[ DIGITAL STREAM ]" : "[ PHYSICAL DISC ]");
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        iconLbl.setForeground(movie instanceof DigitalMovie ? UITheme.ACCENT_BLUE : UITheme.WARNING_COLOR);
        iconPanel.add(iconLbl);
        card.add(iconPanel);

        // Title
        JLabel titleLbl = new JLabel("<html><center><b>" + movie.getTitle() + "</b></center></html>");
        titleLbl.setFont(UITheme.FONT_HEADER);
        titleLbl.setForeground(UITheme.TEXT_MAIN);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        // Details
        JLabel metaLbl = new JLabel(movie.getGenre() + " • " + movie.getReleaseYear());
        metaLbl.setFont(UITheme.FONT_BODY);
        metaLbl.setForeground(UITheme.TEXT_MUTED);
        metaLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(metaLbl);
        card.add(Box.createRigidArea(new Dimension(0, 4)));

        // Stock / Format info
        JLabel formatLbl = new JLabel(movie.getFormatDetails());
        formatLbl.setFont(UITheme.FONT_SMALL);
        formatLbl.setForeground(UITheme.TEXT_LIGHT);
        formatLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(formatLbl);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        // Price
        JLabel priceLbl = new JLabel(String.format("$%.2f / rental", movie.getBasePrice()));
        priceLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        priceLbl.setForeground(UITheme.SUCCESS_COLOR);
        priceLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(priceLbl);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        // Buttons
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        actionPanel.setOpaque(false);

        JButton btnRent = UITheme.createPrimaryButton("Rent Now");
        btnRent.addActionListener(e -> {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            RentMovieDialog dialog = new RentMovieDialog(topFrame, movie);
            dialog.setVisible(true);
            if (dialog.isRentalCreated()) {
                renderMovies();
            }
        });

        JPanel secondaryBtns = new JPanel(new GridLayout(1, 2, 6, 0));
        secondaryBtns.setOpaque(false);

        JButton btnWatchlist = UITheme.createSecondaryButton("+ Watchlist");
        btnWatchlist.setFont(UITheme.FONT_SMALL);
        btnWatchlist.addActionListener(e -> handleAddToWatchlist(movie));

        JButton btnReviews = UITheme.createSecondaryButton("Reviews");
        btnReviews.setFont(UITheme.FONT_SMALL);
        btnReviews.addActionListener(e -> pageNavigator.accept("REVIEWS"));

        secondaryBtns.add(btnWatchlist);
        secondaryBtns.add(btnReviews);

        actionPanel.add(btnRent);
        actionPanel.add(secondaryBtns);
        card.add(actionPanel);

        return card;
    }

    private void handleAddToWatchlist(Movie movie) {
        AppState state = AppState.getInstance();
        User user = state.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Please log in first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean added = state.getWatchlistService().addToWatchlist(user.getUserId(), movie);
        if (added) {
            JOptionPane.showMessageDialog(this, "Added \"" + movie.getTitle() + "\" to your Watchlist!", "Watchlist", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Movie is already in your Watchlist!", "Notice", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
