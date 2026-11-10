package ui.component3_review;

import ui.AppState;
import ui.UITheme;
import model.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Component 03 - Movie Reviews List Page.
 * Displays reviews, average ratings, and offers write/edit capabilities.
 */
public class MovieReviewsPage extends JPanel {
    private final JComboBox<Movie> cbMovies;
    private final JLabel lblAvgRating;
    private final JLabel lblReviewCount;
    private final JPanel reviewsContainer;
    private final Consumer<String> pageNavigator;

    public MovieReviewsPage(Consumer<String> pageNavigator) {
        this.pageNavigator = pageNavigator;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header Panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel title = UITheme.createSectionTitle("Movie Reviews & Ratings");
        headerRow.add(title, BorderLayout.WEST);

        JPanel headerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerBtns.setOpaque(false);

        JButton btnWrite = UITheme.createPrimaryButton("Write Review");
        btnWrite.addActionListener(e -> handleWriteReview());

        JButton btnModeration = UITheme.createSecondaryButton("Moderation Panel");
        btnModeration.addActionListener(e -> pageNavigator.accept("MODERATION"));

        headerBtns.add(btnWrite);
        headerBtns.add(btnModeration);
        headerRow.add(headerBtns, BorderLayout.EAST);

        topPanel.add(headerRow);
        topPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Movie Selection & Summary Banner
        JPanel bannerCard = UITheme.createCardPanel();
        bannerCard.setLayout(new BorderLayout(15, 10));

        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        selectorPanel.setOpaque(false);
        selectorPanel.add(UITheme.createFieldLabel("Select Movie:"));

        cbMovies = new JComboBox<>();
        UITheme.styleComboBox(cbMovies);
        cbMovies.setPreferredSize(new Dimension(300, 36));
        cbMovies.addActionListener(e -> refreshReviewsList());
        selectorPanel.add(cbMovies);
        bannerCard.add(selectorPanel, BorderLayout.WEST);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        statsPanel.setOpaque(false);

        lblAvgRating = new JLabel("Rating: 0.0 / 5.0");
        lblAvgRating.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblAvgRating.setForeground(UITheme.WARNING_COLOR);

        lblReviewCount = new JLabel("(0 reviews)");
        lblReviewCount.setFont(UITheme.FONT_BODY);
        lblReviewCount.setForeground(UITheme.TEXT_MUTED);

        statsPanel.add(lblAvgRating);
        statsPanel.add(lblReviewCount);
        bannerCard.add(statsPanel, BorderLayout.EAST);

        topPanel.add(bannerCard);
        add(topPanel, BorderLayout.NORTH);

        // Center Reviews List
        reviewsContainer = new JPanel();
        reviewsContainer.setLayout(new BoxLayout(reviewsContainer, BoxLayout.Y_AXIS));
        reviewsContainer.setBackground(UITheme.BG_DARK);

        JScrollPane scrollPane = new JScrollPane(reviewsContainer);
        scrollPane.getViewport().setBackground(UITheme.BG_DARK);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        UITheme.styleScrollBar(scrollPane.getVerticalScrollBar());
        add(scrollPane, BorderLayout.CENTER);

        populateMoviesDropdown();
    }

    public void populateMoviesDropdown() {
        cbMovies.removeAllItems();
        for (Movie m : AppState.getInstance().getMovies()) {
            cbMovies.addItem(m);
        }
        if (cbMovies.getItemCount() > 0) {
            cbMovies.setSelectedIndex(0);
        }
        refreshReviewsList();
    }

    public void refreshReviewsList() {
        reviewsContainer.removeAll();
        Movie selectedMovie = (Movie) cbMovies.getSelectedItem();
        if (selectedMovie == null) return;

        AppState state = AppState.getInstance();
        User currentUser = state.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.canManageSystem();

        List<Review> movieReviews = state.getReviews().stream()
            .filter(r -> r.getMovieId().equals(selectedMovie.getMovieId()))
            .collect(Collectors.toList());

        // Calculate average rating
        if (movieReviews.isEmpty()) {
            lblAvgRating.setText("Rating: No ratings yet");
            lblReviewCount.setText("(0 reviews)");

            JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            emptyPanel.setOpaque(false);
            JLabel emptyLbl = new JLabel("No reviews submitted for this movie yet. Be the first to review!");
            emptyLbl.setFont(UITheme.FONT_SUBHEADER);
            emptyLbl.setForeground(UITheme.TEXT_MUTED);
            emptyPanel.add(emptyLbl);
            reviewsContainer.add(emptyPanel);
        } else {
            double avg = movieReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            lblAvgRating.setText(String.format("Rating: %.1f / 5.0", avg));
            lblReviewCount.setText("(" + movieReviews.size() + " reviews)");

            for (Review r : movieReviews) {
                reviewsContainer.add(createReviewCard(r, selectedMovie, currentUser, isAdmin));
                reviewsContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        reviewsContainer.revalidate();
        reviewsContainer.repaint();
    }

    private JPanel createReviewCard(Review review, Movie movie, User currentUser, boolean isAdmin) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 8));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Top info: author, stars, badge
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel userLbl = new JLabel(review.getUsername() + "   [" + review.getRating() + " / 5 Stars]");
        userLbl.setFont(UITheme.FONT_BOLD);
        userLbl.setForeground(UITheme.TEXT_MAIN);
        top.add(userLbl, BorderLayout.WEST);

        JLabel badgeLbl = new JLabel(review.getReviewBadge());
        badgeLbl.setFont(UITheme.FONT_SMALL);
        badgeLbl.setForeground(review instanceof VerifiedReview ? UITheme.SUCCESS_COLOR : UITheme.TEXT_MUTED);
        top.add(badgeLbl, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Center: Comment
        JLabel commentLbl = new JLabel("<html>\"" + review.getComment() + "\"</html>");
        commentLbl.setFont(UITheme.FONT_BODY);
        commentLbl.setForeground(new Color(220, 222, 230));
        card.add(commentLbl, BorderLayout.CENTER);

        // Bottom: Date and Actions
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        // Polymorphic formatted display string
        JLabel dateLbl = new JLabel("Reviewed on " + review.getReviewDate() + (isAdmin ? " • [Admin Verified]" : ""));
        dateLbl.setFont(UITheme.FONT_SMALL);
        dateLbl.setForeground(UITheme.TEXT_MUTED);
        bottom.add(dateLbl, BorderLayout.WEST);

        boolean isAuthor = currentUser != null && currentUser.getUserId().equals(review.getUserId());
        if (isAuthor || isAdmin) {
            JPanel actPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            actPanel.setOpaque(false);

            if (isAuthor) {
                JButton btnEdit = UITheme.createSecondaryButton("Edit");
                btnEdit.setFont(UITheme.FONT_SMALL);
                btnEdit.addActionListener(e -> {
                    JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                    WriteReviewDialog dialog = new WriteReviewDialog(topFrame, movie, review);
                    dialog.setVisible(true);
                    if (dialog.isSubmitted()) refreshReviewsList();
                });
                actPanel.add(btnEdit);
            }

            JButton btnDel = UITheme.createDangerButton("Delete");
            btnDel.setFont(UITheme.FONT_SMALL);
            btnDel.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this, "Delete this review?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    AppState.getInstance().getReviewService().deleteReview(review.getReviewId());
                    AppState.getInstance().notifyListeners();
                    refreshReviewsList();
                }
            });
            actPanel.add(btnDel);

            bottom.add(actPanel, BorderLayout.EAST);
        }

        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private void handleWriteReview() {
        Movie selectedMovie = (Movie) cbMovies.getSelectedItem();
        if (selectedMovie == null) return;

        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        WriteReviewDialog dialog = new WriteReviewDialog(topFrame, selectedMovie, null);
        dialog.setVisible(true);
        if (dialog.isSubmitted()) {
            refreshReviewsList();
        }
    }
}
