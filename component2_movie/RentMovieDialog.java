package ui.component2_movie;

import ui.AppState;
import ui.UITheme;
import model.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Rent Movie Dialog / Modal.
 * Abstraction: checks movie availability before rental.
 * Polymorphism: different late-fee preview for standard vs premium rentals.
 */
public class RentMovieDialog extends JDialog {
    private boolean rentalCreated = false;

    public RentMovieDialog(JFrame parent, Movie movie) {
        super(parent, "Rent Movie - " + movie.getTitle(), true);
        setSize(460, 520);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(movie.getTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UITheme.TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel typeLbl = new JLabel(movie.getFormatDetails());
        typeLbl.setFont(UITheme.FONT_SUBHEADER);
        typeLbl.setForeground(UITheme.ACCENT_RED);
        typeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(typeLbl);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        // Details Grid
        JPanel infoGrid = new JPanel(new GridLayout(5, 2, 10, 10));
        infoGrid.setOpaque(false);
        infoGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        infoGrid.add(UITheme.createFieldLabel("Genre:"));
        JLabel valGenre = new JLabel(movie.getGenre());
        valGenre.setForeground(UITheme.TEXT_MAIN);
        infoGrid.add(valGenre);

        infoGrid.add(UITheme.createFieldLabel("Release Year:"));
        JLabel valYear = new JLabel(String.valueOf(movie.getReleaseYear()));
        valYear.setForeground(UITheme.TEXT_MAIN);
        infoGrid.add(valYear);

        infoGrid.add(UITheme.createFieldLabel("Rental Base Price:"));
        JLabel valPrice = new JLabel(String.format("$%.2f", movie.getBasePrice()));
        valPrice.setFont(UITheme.FONT_BOLD);
        valPrice.setForeground(UITheme.SUCCESS_COLOR);
        infoGrid.add(valPrice);

        infoGrid.add(UITheme.createFieldLabel("Rental Period:"));
        JLabel valPeriod = new JLabel("7 Days Standard");
        valPeriod.setForeground(UITheme.TEXT_MAIN);
        infoGrid.add(valPeriod);

        infoGrid.add(UITheme.createFieldLabel("Due Date:"));
        JLabel valDue = new JLabel(LocalDate.now().plusDays(7).toString());
        valDue.setFont(UITheme.FONT_BOLD);
        valDue.setForeground(UITheme.WARNING_COLOR);
        infoGrid.add(valDue);

        card.add(infoGrid);
        card.add(Box.createRigidArea(new Dimension(0, 15)));

        // Premium tier checkbox option
        JCheckBox chkPremium = new JCheckBox("Premium Extended Rental (+ $2.00, lower late fees)");
        chkPremium.setFont(UITheme.FONT_SMALL);
        chkPremium.setForeground(UITheme.TEXT_MUTED);
        chkPremium.setOpaque(false);
        chkPremium.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(chkPremium);

        card.add(Box.createRigidArea(new Dimension(0, 25)));

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnConfirm = UITheme.createPrimaryButton("Confirm & Pay Rental");
        btnConfirm.addActionListener(e -> {
            AppState state = AppState.getInstance();
            User currentUser = state.getCurrentUser();
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this, "Please log in to rent movies.", "Authentication Required", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (currentUser instanceof RegularUser) {
                RegularUser ru = (RegularUser) currentUser;
                long activeCount = state.getRentals().stream()
                    .filter(r -> r.getUserId().equalsIgnoreCase(ru.getUserId()) && "ACTIVE".equalsIgnoreCase(r.getStatus()))
                    .count();
                if (activeCount >= ru.getRentalQuota()) {
                    JOptionPane.showMessageDialog(this,
                        "You have reached your active rental limit (" + ru.getRentalQuota() + " movies) for membership tier " + ru.getMembershipType() + "!\nPlease return an active rental before renting more.",
                        "Rental Limit Reached", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // CREATE OPERATION
            // Creates a new rental record when a user rents a movie
            Rental newRental = state.getMovieRentalService().createRental(currentUser.getUserId(), movie.getMovieId(), chkPremium.isSelected());
            if (newRental == null) {
                JOptionPane.showMessageDialog(this, 
                    "Sorry, this physical movie is currently out of stock!", 
                    "Rental Unavailable", JOptionPane.WARNING_MESSAGE);
                return;
            }

            state.notifyListeners();
            rentalCreated = true;

            JOptionPane.showMessageDialog(this,
                "Rental Confirmed!\nRental ID: " + newRental.getRentalId() + "\nMovie: " + movie.getTitle() + 
                "\nTotal Charged: $" + String.format("%.2f", newRental.getRentalFee()) + "\nDue on: " + newRental.getDueDate(),
                "Rental Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);
        card.add(btnPanel);

        add(card, BorderLayout.CENTER);
    }

    public boolean isRentalCreated() {
        return rentalCreated;
    }
}
