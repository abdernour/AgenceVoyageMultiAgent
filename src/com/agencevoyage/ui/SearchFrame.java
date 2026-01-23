package com.agencevoyage.ui;

import com.agencevoyage.agents.AgentUI;
import com.agencevoyage.ontology.concepts.Voyage;
import com.agencevoyage.utils.DatabaseManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;

/**
 * Main search window - WITH ROOM SELECTION
 */
public class SearchFrame extends JFrame {
    private AgentUI agentUI;
    private JComboBox<String> destCombo;
    private UIComponents.RoundedDateChooser depDate, retDate;
    private JSpinner adultsSpinner, childrenSpinner, roomsSpinner; // NEW: roomsSpinner
    private JTextField budgetField;
    private JButton searchBtn;
    private boolean isMaximized = false;
    private Rectangle normalBounds = null;
    private JButton maxButton;

    public SearchFrame(AgentUI agent) {
        this.agentUI = agent;

        setTitle("TravelAI - Smart Planning");
        setSize(1400, 900);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        
        // Ensure window stays within bounds
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent e) {
                if (!isMaximized) {
                    Point location = getLocation();
                    Point constrained = WindowPositionManager.constrainToScreen(location, getSize());
                    if (!location.equals(constrained)) {
                        setLocation(constrained);
                    }
                }
            }
        });

        JPanel main = new UIComponents.GradientPanel();
        main.setLayout(new BorderLayout());
        main.setBorder(new LineBorder(new Color(255, 255, 255, 50), 1));

        main.add(createTitleBar(), BorderLayout.NORTH);
        main.add(createContent(), BorderLayout.CENTER);

        setContentPane(main);
    }

    private JPanel createTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(30, 50, 30, 50));

        JLabel logo = new JLabel("TravelAI");
        logo.setFont(new Font("Dialog", Font.BOLD, 26));
        logo.setForeground(Theme.PRIMARY);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        controls.setOpaque(false);

        JButton myBookings = createControlButton("▤ My Bookings", e -> showBookingHistory());
        maxButton = createControlButton("□", e -> toggleMaximize());
        JButton min = createControlButton("–", e -> setState(ICONIFIED));
        JButton close = createControlButton("✕", e -> System.exit(0));

        controls.add(myBookings);
        controls.add(maxButton);
        controls.add(min);
        controls.add(close);

        bar.add(logo, BorderLayout.WEST);
        bar.add(controls, BorderLayout.EAST);

        makeDraggable(bar);
        return bar;
    }

    private JButton createControlButton(String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Dialog", Font.BOLD, 16));
        btn.setForeground(Theme.TEXT_LIGHT);
        btn.setBorder(null);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(Theme.TEXT);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(Theme.TEXT_LIGHT);
            }
        });
        return btn;
    }

    private void toggleMaximize() {
        if (isMaximized) {
            // Restore to normal size
            if (normalBounds != null) {
                setBounds(normalBounds);
            }
            isMaximized = false;
            maxButton.setText("□");
        } else {
            // Maximize
            normalBounds = getBounds();
            Rectangle screenBounds = WindowPositionManager.getUsableScreenBounds();
            setBounds(screenBounds);
            isMaximized = true;
            maxButton.setText("❐");
        }
    }
    
    private void makeDraggable(JPanel panel) {
        final Point[] dragStart = {null};
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isMaximized) {
                    dragStart[0] = e.getLocationOnScreen();
                }
            }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart[0] != null && !isMaximized) {
                    Point current = e.getLocationOnScreen();
                    Point windowLocation = getLocationOnScreen();
                    int deltaX = current.x - dragStart[0].x;
                    int deltaY = current.y - dragStart[0].y;
                    Point newLocation = new Point(windowLocation.x + deltaX, windowLocation.y + deltaY);
                    
                    // Constrain to screen bounds
                    newLocation = WindowPositionManager.constrainToScreen(newLocation, getSize());
                    setLocation(newLocation);
                    dragStart[0] = current;
                }
            }
        });
    }

    private JPanel createContent() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(0, 80, 100, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Hero section
        JPanel hero = createHero();
        gbc.gridy = 0;
        container.add(hero, gbc);

        // Spacer
        gbc.gridy = 1;
        gbc.insets = new Insets(60, 0, 0, 0);

        // Search card
        JPanel card = createSearchCard();
        container.add(card, gbc);

        return container;
    }

    private JPanel createHero() {
        JPanel hero = new JPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setOpaque(false);
        hero.setBorder(new EmptyBorder(40, 0, 0, 0));

        JLabel title = new JLabel("Find Your Perfect Escape");
        title.setFont(new Font("Dialog", Font.BOLD, 56));
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("AI-powered travel planning for unforgettable journeys");
        sub.setFont(new Font("Dialog", Font.PLAIN, 20));
        sub.setForeground(Theme.TEXT_LIGHT);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        sub.setBorder(new EmptyBorder(20, 0, 0, 0));

        hero.add(title);
        hero.add(sub);
        return hero;
    }

    private JPanel createSearchCard() {
        JPanel card = new UIComponents.ShadowCard(30);
        card.setMaximumSize(new Dimension(1200, 650));
        card.setPreferredSize(new Dimension(1200, 650));
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(new EmptyBorder(60, 70, 60, 70));

        JPanel form = createSearchForm();

        searchBtn = new UIComponents.GradientButton("➤ Search Best Trips");
        searchBtn.setPreferredSize(new Dimension(350, 65));
        searchBtn.setFont(new Font("Dialog", Font.BOLD, 18));
        searchBtn.addActionListener(e -> performSearch());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(30, 0, 0, 0));
        btnPanel.add(searchBtn);

        card.add(form, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createSearchForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // Row 1: Destination
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 35, 0);
        destCombo = createDestinationCombo();
        form.add(createLabel("Destination", destCombo), gbc);

        // Row 2: Dates
        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 35, 30);
        depDate = new UIComponents.RoundedDateChooser("→");
        depDate.setDate(new Date());
        form.add(createLabel("Departure Date", depDate), gbc);

        gbc.gridx = 1;
        retDate = new UIComponents.RoundedDateChooser("←");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 7);
        retDate.setDate(c.getTime());
        form.add(createLabel("Return Date", retDate), gbc);

        gbc.gridx = 2; gbc.insets = new Insets(0, 0, 35, 0);
        budgetField = new UIComponents.RoundedTextField("100000", "$");
        form.add(createLabel("$ Maximum Budget (DA)", budgetField), gbc);

        // Row 3: Travelers + Rooms 
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 30);
        adultsSpinner = new UIComponents.RoundedSpinner(new SpinnerNumberModel(2, 1, 10, 1), "");
        form.add(createLabel("Adults (12+ years)", adultsSpinner), gbc);

        gbc.gridx = 1;
        childrenSpinner = new UIComponents.RoundedSpinner(new SpinnerNumberModel(0, 0, 10, 1), "");
        form.add(createLabel("Children (2-11 years)", childrenSpinner), gbc);

        // NEW: Room selector
        gbc.gridx = 2; gbc.insets = new Insets(0, 0, 0, 0);
        roomsSpinner = new UIComponents.RoundedSpinner(new SpinnerNumberModel(1, 1, 5, 1), "");
        form.add(createLabel("Rooms Needed", roomsSpinner), gbc);

        return form;
    }

    private JComboBox<String> createDestinationCombo() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(new Font("Dialog", Font.PLAIN, 16));
        combo.setBackground(Theme.INPUT_BG);
        combo.setForeground(Theme.TEXT);
        combo.setBorder(new EmptyBorder(16, 18, 16, 18));
        combo.setPreferredSize(new Dimension(0, 60));
        combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loadDestinations(combo);
        return combo;
    }

    private void loadDestinations(JComboBox<String> combo) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String sql = "SELECT DISTINCT city FROM hotels WHERE active = TRUE ORDER BY city";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                combo.addItem(rs.getString("city"));
            }
            rs.close();
            stmt.close();

            if (combo.getItemCount() == 0) {
                combo.addItem("Rome"); combo.addItem("Barcelona");
                combo.addItem("London"); combo.addItem("Dubai");
            }
        } catch (SQLException e) {
            combo.addItem("Paris"); combo.addItem("New York"); combo.addItem("Tokyo");
        }
    }

    private JPanel createLabel(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Dialog", Font.BOLD, 15));
        l.setForeground(Theme.TEXT);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void performSearch() {
        try {
            Date dep = depDate.getDate();
            Date ret = retDate.getDate();

            if (dep == null || ret == null || ret.before(dep)) {
                JOptionPane.showMessageDialog(this, "Please check your travel dates",
                        "Invalid Dates", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String dest = (String) destCombo.getSelectedItem();
            int ad = (int) adultsSpinner.getValue();
            int ch = (int) childrenSpinner.getValue();
            int rooms = (int) roomsSpinner.getValue(); // NEW: Get room count
            float budget = Float.parseFloat(budgetField.getText());

            showLoading();
            searchBtn.setEnabled(false);

            Voyage voyage = new Voyage();
            voyage.setDestination(dest);
            voyage.setDateDepart(dep);
            voyage.setDateRetour(ret);
            voyage.setNombreAdultes(ad);
            voyage.setNombreEnfants(ch);
            voyage.setNombreChambres(rooms); 
            voyage.setBudgetMax(budget);

            String convId = "travel-" + System.currentTimeMillis();
            agentUI.sendSearchRequest(voyage, convId);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Check your inputs and try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            hideLoading();
        }
    }

    public void showLoading() {
        JPanel glassPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 120));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        glassPane.setOpaque(false);
        glassPane.setLayout(new GridBagLayout());

        JPanel loadingCard = new UIComponents.ShadowCard(28);
        loadingCard.setPreferredSize(new Dimension(400, 250));
        loadingCard.setLayout(new BoxLayout(loadingCard, BoxLayout.Y_AXIS));
        loadingCard.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel sp = new JLabel("Loading...");
        sp.setFont(new Font("Dialog", Font.PLAIN, 64));
        sp.setAlignmentX(CENTER_ALIGNMENT);

        JLabel txt = new JLabel("Finding best options...");
        txt.setFont(new Font("Dialog", Font.BOLD, 18));
        txt.setForeground(Theme.PRIMARY);
        txt.setAlignmentX(CENTER_ALIGNMENT);

        loadingCard.add(Box.createVerticalGlue());
        loadingCard.add(sp);
        loadingCard.add(Box.createVerticalStrut(25));
        loadingCard.add(txt);
        loadingCard.add(Box.createVerticalGlue());

        glassPane.add(loadingCard);
        setGlassPane(glassPane);
        glassPane.setVisible(true);
    }

    public void hideLoading() {
        getGlassPane().setVisible(false);
        searchBtn.setEnabled(true);
    }

    private void showBookingHistory() {
        new BookingHistoryDialog(this, agentUI).setVisible(true);
    }

    // Getters (UPDATED)
    public String getDestination() { return (String) destCombo.getSelectedItem(); }
    public Date getDepartureDate() { return depDate.getDate(); }
    public Date getReturnDate() { return retDate.getDate(); }
    public int getAdults() { return (int) adultsSpinner.getValue(); }
    public int getChildren() { return (int) childrenSpinner.getValue(); }
    public int getRooms() { return (int) roomsSpinner.getValue(); } // NEW
}