package ui.component3_review;

import ui.AppState;
import ui.UITheme;
import model.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * Dialog to submit or edit a movie review.
 * Encapsulation: Validates 1-5 rating.
 * Inheritance: Instantiates VerifiedReview if user has rented the movie, else PublicReview.
 */
public class WriteReviewDialog extends JDialog {
    private final JComboBox<Integer> cbRating;
    private final JTextArea taComment;
    private final Movie movie;
    private final Review existingReview;
    private boolean submitted = false;

    public WriteReviewDialog(JFrame parent, Movie movie, Review existingReview) {
        super(parent, existingReview == null ? "Write Review - " + movie.getTitle() : "Edit Review - " + movie.getTitle(), true);
        this.movie = movie;
        this.existingReview = existingReview;

        setSize(460, 420);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(movie.getTitle());
        titleLbl.setFont(UITheme.FONT_HEADER);
        titleLbl.setForeground(UITheme.TEXT_MAIN);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel(existingReview == null ? "Share your experience with other viewers" : "Update your rating & thoughts");
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(UITheme.TEXT_MUTED);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLbl);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(subLbl);
        card.add(Box.createRigidArea(new Dimension(0, 15)));

        // Rating picker
        card.add(UITheme.createFieldLabel("Your Rating (1 to 5 Stars):"));
        cbRating = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        UITheme.styleComboBox(cbRating);
        cbRating.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        if (existingReview != null) {
            cbRating.setSelectedItem(existingReview.getRating());
        }
        card.add(cbRating);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        // Comment area
        card.add(UITheme.createFieldLabel("Review Comments:"));
        taComment = new JTextArea(5, 20);
        taComment.setFont(UITheme.FONT_BODY);
        taComment.setBackground(UITheme.SURFACE);
        taComment.setForeground(UITheme.TEXT_MAIN);
        taComment.setCaretColor(UITheme.TEXT_MAIN);
        taComment.setLineWrap(true);
        taComment.setWrapStyleWord(true);
        if (existingReview != null) {
            taComment.setText(existingReview.getComment());
        }
        JScrollPane textScroll = new JScrollPane(taComment);
        textScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        card.add(textScroll);
        card.add(Box.createRigidArea(new Dimension(0, 18)));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = UITheme.createPrimaryButton(existingReview == null ? "Submit Review" : "Update Review");
        btnSubmit.addActionListener(e -> handleSubmit());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        card.add(btnPanel);

        add(card, BorderLayout.CENTER);
    }

    private void handleSubmit() {
        String comment = taComment.getText().trim();
        int rating = (int) cbRating.getSelectedItem();

        if (comment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please write a comment.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AppState state = AppState.getInstance();
        User currentUser = state.getCurrentUser();
        if (currentUser == null) return;

        if (existingReview != null) {
            // Update
            state.getReviewService().updateReview(existingReview.getReviewId(), rating, comment);
            JOptionPane.showMessageDialog(this, "Review updated successfully!", "Updated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Check if user has rented this movie (Verified Renter vs Public)
            Rental userRental = state.getRentals().stream()
                .filter(r -> r.getUserId().equals(currentUser.getUserId()) && r.getMovieId().equals(movie.getMovieId()))
                .findFirst().orElse(null);

            Review newReview = state.getReviewService().addReview(
                movie.getMovieId(),
                currentUser.getUserId(),
                currentUser.getUsername(),
                rating,
                comment,
                userRental != null ? userRental.getRentalId() : null
            );

            JOptionPane.showMessageDialog(this, "Review published successfully!\nType: " + newReview.getReviewBadge(), "Review Published", JOptionPane.INFORMATION_MESSAGE);
        }

        state.notifyListeners();
        submitted = true;
        dispose();
    }

    public boolean isSubmitted() {
        return submitted;
    }
}
