package ui.component2_movie;

import ui.AppState;
import ui.UITheme;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Component 02 - Add/Edit Movie Page (Admin).
 * Demonstrates CRUD (Create, Update, Delete) and Inheritance (DigitalMovie vs PhysicalMovie).
 */
public class AddEditMoviePage extends JPanel {
    private final JTextField tfTitle;
    private final JComboBox<String> cbGenre;
    private final JTextField tfYear;
    private final JTextField tfPrice;
    private final JComboBox<String> cbMovieType;
    private final JTextField tfStock;
    private final JTextField tfExtraDetail; // Stream Quality or Disc Format
    private final JLabel lblExtra;

    private final JTable movieTable;
    private final DefaultTableModel tableModel;
    private Movie selectedMovie = null;
    private final Consumer<String> pageNavigator;

    public AddEditMoviePage(Consumer<String> pageNavigator) {
        this.pageNavigator = pageNavigator;
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = UITheme.createSectionTitle("Movie Catalog Management (Admin)");
        headerPanel.add(title, BorderLayout.WEST);

        JButton btnBack = UITheme.createSecondaryButton("Back to Catalog");
        btnBack.addActionListener(e -> pageNavigator.accept("CATALOG"));
        headerPanel.add(btnBack, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center split: Form on left, Movie List Table on right
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerLocation(420);

        // Left Form Panel
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JLabel formTitle = new JLabel("Add or Edit Movie Details");
        formTitle.setFont(UITheme.FONT_HEADER);
        formTitle.setForeground(UITheme.TEXT_MAIN);
        formCard.add(formTitle);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Title
        formCard.add(UITheme.createFieldLabel("Movie Title:"));
        tfTitle = UITheme.createTextField();
        formCard.add(tfTitle);
        formCard.add(Box.createRigidArea(new Dimension(0, 8)));

        // Genre
        formCard.add(UITheme.createFieldLabel("Genre:"));
        cbGenre = new JComboBox<>(new String[]{"Sci-Fi", "Action", "Crime", "Animation", "Biography", "Drama", "Comedy", "Horror"});
        UITheme.styleComboBox(cbGenre);
        formCard.add(cbGenre);
        formCard.add(Box.createRigidArea(new Dimension(0, 8)));

        // Release Year
        formCard.add(UITheme.createFieldLabel("Release Year:"));
        tfYear = UITheme.createTextField();
        formCard.add(tfYear);
        formCard.add(Box.createRigidArea(new Dimension(0, 8)));

        // Base Price
        formCard.add(UITheme.createFieldLabel("Base Price ($):"));
        tfPrice = UITheme.createTextField();
        formCard.add(tfPrice);
        formCard.add(Box.createRigidArea(new Dimension(0, 8)));

        // Type
        formCard.add(UITheme.createFieldLabel("Format / Type:"));
        cbMovieType = new JComboBox<>(new String[]{"DIGITAL", "PHYSICAL"});
        UITheme.styleComboBox(cbMovieType);
        formCard.add(cbMovieType);
        formCard.add(Box.createRigidArea(new Dimension(0, 8)));

        // Stock
        formCard.add(UITheme.createFieldLabel("Stock Count (Physical only):"));
        tfStock = UITheme.createTextField();
        tfStock.setText("10");
        formCard.add(tfStock);
        formCard.add(Box.createRigidArea(new Dimension(0, 8)));

        // Dynamic extra field
        lblExtra = UITheme.createFieldLabel("Quality / Disc Specs:");
        formCard.add(lblExtra);
        tfExtraDetail = UITheme.createTextField();
        tfExtraDetail.setText("4K UHD");
        formCard.add(tfExtraDetail);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));

        cbMovieType.addActionListener(e -> {
            boolean isPhysical = "PHYSICAL".equals(cbMovieType.getSelectedItem());
            tfStock.setEnabled(isPhysical);
            lblExtra.setText(isPhysical ? "Disc Format (Blu-ray/DVD/4K Disc):" : "Stream Quality (4K UHD/1080p):");
            tfExtraDetail.setText(isPhysical ? "Blu-ray" : "4K UHD");
        });

        // Form Buttons
        JPanel formBtnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        formBtnRow.setOpaque(false);

        JButton btnSave = UITheme.createPrimaryButton("Save Movie");
        btnSave.addActionListener(e -> handleSaveMovie());

        JButton btnClear = UITheme.createSecondaryButton("Clear");
        btnClear.addActionListener(e -> clearForm());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> handleDeleteMovie());

        formBtnRow.add(btnSave);
        formBtnRow.add(btnClear);
        formBtnRow.add(btnDelete);
        formCard.add(formBtnRow);

        splitPane.setDividerSize(4);
        splitPane.setBackground(UITheme.BORDER_COLOR);

        JScrollPane formScroll = new JScrollPane(formCard);
        formScroll.setBorder(null);
        formScroll.setBackground(UITheme.BG_DARK);
        formScroll.getViewport().setBackground(UITheme.BG_DARK);
        formScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        UITheme.styleScrollBar(formScroll.getVerticalScrollBar());
        splitPane.setLeftComponent(formScroll);

        // Right Table Panel
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        JLabel tableTitle = new JLabel("Existing Movie Catalog");
        tableTitle.setFont(UITheme.FONT_HEADER);
        tableTitle.setForeground(UITheme.TEXT_MAIN);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"ID", "Title", "Genre", "Year", "Price", "Type", "Stock"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        movieTable = new JTable(tableModel);
        JScrollPane tableScroll = UITheme.createTableScrollPane(movieTable);

        // Column widths for optimal visibility
        movieTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        movieTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        movieTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        movieTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        movieTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        movieTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        movieTable.getColumnModel().getColumn(5).setPreferredWidth(85);
        movieTable.getColumnModel().getColumn(6).setPreferredWidth(60);

        movieTable.getSelectionModel().addListSelectionListener(e -> {
            int row = movieTable.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                AppState.getInstance().getMovies().stream()
                    .filter(m -> m.getMovieId().equals(id))
                    .findFirst()
                    .ifPresent(this::populateForm);
            }
        });

        tableCard.add(tableScroll, BorderLayout.CENTER);
        splitPane.setRightComponent(tableCard);

        add(splitPane, BorderLayout.CENTER);
        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Movie> list = AppState.getInstance().getMovies();
        for (Movie m : list) {
            tableModel.addRow(new Object[]{
                m.getMovieId(),
                m.getTitle(),
                m.getGenre(),
                m.getReleaseYear(),
                String.format("$%.2f", m.getBasePrice()),
                m.getMovieType(),
                m.getStock()
            });
        }
    }

    private void populateForm(Movie m) {
        selectedMovie = m;
        tfTitle.setText(m.getTitle());
        cbGenre.setSelectedItem(m.getGenre());
        tfYear.setText(String.valueOf(m.getReleaseYear()));
        tfPrice.setText(String.valueOf(m.getBasePrice()));
        cbMovieType.setSelectedItem(m.getMovieType());
        tfStock.setText(String.valueOf(m.getStock()));

        if (m instanceof DigitalMovie) {
            tfExtraDetail.setText(((DigitalMovie) m).getStreamQuality());
        } else if (m instanceof PhysicalMovie) {
            tfExtraDetail.setText(((PhysicalMovie) m).getDiscFormat());
        }
    }

    private void clearForm() {
        selectedMovie = null;
        tfTitle.setText("");
        tfYear.setText("");
        tfPrice.setText("");
        tfStock.setText("10");
        tfExtraDetail.setText("4K UHD");
        movieTable.clearSelection();
    }

    private void handleSaveMovie() {
        String title = tfTitle.getText().trim();
        String genre = cbGenre.getSelectedItem().toString();
        String yearStr = tfYear.getText().trim();
        String priceStr = tfPrice.getText().trim();
        String type = cbMovieType.getSelectedItem().toString();
        String extra = tfExtraDetail.getText().trim();

        if (title.isEmpty() || yearStr.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title, Year and Price are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);
            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(tfStock.getText().trim());

            AppState state = AppState.getInstance();

            if (selectedMovie == null) {
                // Create New Movie
                String newId = "M00" + (state.getMovies().size() + 1);
                Movie newMovie;
                if ("DIGITAL".equals(type)) {
                    newMovie = new DigitalMovie(newId, title, genre, year, price, extra.isEmpty() ? "4K UHD" : extra);
                } else {
                    newMovie = new PhysicalMovie(newId, title, genre, year, price, stock, extra.isEmpty() ? "Blu-ray" : extra);
                }
                state.getMovieRentalService().addMovie(newMovie);
                JOptionPane.showMessageDialog(this, "New movie added successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Update Existing Movie
                selectedMovie.setTitle(title);
                selectedMovie.setGenre(genre);
                selectedMovie.setReleaseYear(year);
                selectedMovie.setBasePrice(price);
                selectedMovie.setStock(stock);
                if (selectedMovie instanceof DigitalMovie) {
                    ((DigitalMovie) selectedMovie).setStreamQuality(extra);
                } else if (selectedMovie instanceof PhysicalMovie) {
                    ((PhysicalMovie) selectedMovie).setDiscFormat(extra);
                }
                state.getMovieRentalService().updateMovie(selectedMovie);
                JOptionPane.showMessageDialog(this, "Movie updated successfully!", "Updated", JOptionPane.INFORMATION_MESSAGE);
            }

            state.notifyListeners();
            refreshTable();
            clearForm();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number for Year, Price, or Stock.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteMovie() {
        if (selectedMovie == null) {
            JOptionPane.showMessageDialog(this, "Select a movie from the table to delete.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete \"" + selectedMovie.getTitle() + "\"?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            AppState.getInstance().getMovieRentalService().deleteMovie(selectedMovie.getMovieId());
            AppState.getInstance().notifyListeners();
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Movie deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
