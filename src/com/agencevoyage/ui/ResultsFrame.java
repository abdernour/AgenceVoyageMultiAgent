package com.agencevoyage.ui;

import com.agencevoyage.agents.AgentUI;
import com.agencevoyage.ontology.concepts.*;
import com.agencevoyage.utils.ReservationManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Results and booking window - UPDATED with room info
 */
public class ResultsFrame extends JFrame {
    private AgentUI agentUI;
    private Proposition proposition;
    private String destination;
    private Date departureDate, returnDate;
    private int adults, children, rooms;  // NEW: rooms field
    private float totalPrice;
    private boolean isMaximized = false;
    private Rectangle normalBounds = null;
    private JButton maxButton;

    public ResultsFrame(AgentUI agent, Proposition prop, String dest,
                        Date dep, Date ret, int ad, int ch, int rm) {  // NEW: rm parameter
        this.agentUI = agent;
        this.proposition = prop;
        this.destination = dest;
        this.departureDate = dep;
        this.returnDate = ret;
        this.adults = ad;
        this.children = ch;
        this.rooms = rm;  // NEW
        this.totalPrice = prop.getCoutTotal();

        setSize(1300, 900);
        Point position = WindowPositionManager.getNextWindowPosition(new Dimension(1300, 900));
        setLocation(position);
        setUndecorated(true);
        setBackground(new Color(0,0,0,0));
        
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
        main.setBorder(new LineBorder(Theme.BORDER, 1));

        main.add(createHeader(), BorderLayout.NORTH);
        main.add(createBookingContent(), BorderLayout.CENTER);

        setContentPane(main);
    }

    private JPanel createHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setOpaque(false);
        h.setBorder(new EmptyBorder(40, 60, 30, 60));

        JButton back = new JButton("← All Offers");
        back.setFont(new Font("Dialog", Font.BOLD, 15));
        back.setForeground(Theme.PRIMARY);
        back.setBorder(null);
        back.setContentAreaFilled(false);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> dispose());

        JLabel title = new JLabel("Complete Your Booking");
        title.setFont(new Font("Dialog", Font.BOLD, 36));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(back);

        // Window controls (maximize, minimize, close)
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        controls.setOpaque(false);
        
        maxButton = createControlButton("□", e -> toggleMaximize());
        JButton min = createControlButton("–", e -> setState(ICONIFIED));
        JButton close = createControlButton("✕", e -> dispose());
        
        controls.add(maxButton);
        controls.add(min);
        controls.add(close);

        h.add(left, BorderLayout.WEST);
        h.add(title, BorderLayout.CENTER);
        h.add(controls, BorderLayout.EAST);
        
        // Make the entire header draggable
        makeDraggable(h);
        return h;
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

    private JPanel createBookingContent() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(20, 60, 80, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 40);
        gbc.weightx = 0.45;
        gbc.weighty = 1;

        gbc.gridx = 0;
        container.add(createSummaryCard(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.55;
        gbc.insets = new Insets(0, 0, 0, 0);
        container.add(createCheckoutForm(), gbc);

        return container;
    }

    private JPanel createSummaryCard() {
        UIComponents.ShadowCard card = new UIComponents.ShadowCard(25);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(50, 45, 50, 45));

        JLabel title = new JLabel("Trip Summary");
        title.setFont(new Font("Dialog", Font.BOLD, 26));
        title.setAlignmentX(LEFT_ALIGNMENT);
        title.setForeground(Theme.TEXT);

        card.add(title);
        card.add(Box.createVerticalStrut(40));

        // Flight details
        card.add(createInfoSection("✈ Flight Details",
                proposition.getVolPropose().getCompagnie() + " Airways",
                proposition.getVolPropose().getVilleDepart() + " → " +
                        proposition.getVolPropose().getVilleArrivee()));

        card.add(Box.createVerticalStrut(30));

        // Hotel details
        card.add(createInfoSection("⌂ Hotel Accommodation",
                proposition.getHotelPropose().getNom(),
                generateStars(proposition.getHotelPropose().getCategorie()) + " Hotel - " + rooms + " Room(s)"));  // NEW

        card.add(Box.createVerticalStrut(30));

        // Trip details
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        int nights = (int) ((returnDate.getTime() - departureDate.getTime()) / (1000 * 60 * 60 * 24));

        card.add(createSummaryRow("📍 Destination", destination));
        card.add(Box.createVerticalStrut(15));
        card.add(createSummaryRow("📅 Check-in", sdf.format(departureDate)));
        card.add(Box.createVerticalStrut(15));
        card.add(createSummaryRow("📅 Check-out", sdf.format(returnDate)));
        card.add(Box.createVerticalStrut(15));
        card.add(createSummaryRow("🌙 Duration", nights + " nights"));
        card.add(Box.createVerticalStrut(15));
        card.add(createSummaryRow("👥 Travelers", adults + " Adults, " + children + " Children"));
        card.add(Box.createVerticalStrut(15));
        card.add(createSummaryRow("🏠 Rooms", rooms + " Room(s)"));  // NEW

        card.add(Box.createVerticalStrut(40));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(500, 1));
        sep.setForeground(Theme.BORDER);
        card.add(sep);

        card.add(Box.createVerticalStrut(30));

        // Total price
        JPanel pricePanel = new JPanel(new BorderLayout());
        pricePanel.setOpaque(false);
        pricePanel.setMaximumSize(new Dimension(500, 80));

        JLabel totalLbl = new JLabel("Total Price");
        totalLbl.setFont(new Font("Dialog", Font.PLAIN, 16));
        totalLbl.setForeground(Theme.TEXT_LIGHT);

        JLabel priceVal = new JLabel(String.format("%.0f DA", totalPrice));
        priceVal.setFont(new Font("Dialog", Font.BOLD, 42));
        priceVal.setForeground(Theme.PRIMARY);

        pricePanel.add(totalLbl, BorderLayout.NORTH);
        pricePanel.add(priceVal, BorderLayout.CENTER);

        card.add(pricePanel);

        return card;
    }

    private JPanel createInfoSection(String title, String line1, String line2) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(500, 100));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Dialog", Font.BOLD, 16));
        titleLbl.setForeground(Theme.TEXT_LIGHT);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel mainLbl = new JLabel(line1);
        mainLbl.setFont(new Font("Dialog", Font.BOLD, 20));
        mainLbl.setForeground(Theme.TEXT);
        mainLbl.setAlignmentX(LEFT_ALIGNMENT);
        mainLbl.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel subLbl = new JLabel(line2);
        subLbl.setFont(new Font("Dialog", Font.PLAIN, 15));
        subLbl.setForeground(Theme.TEXT_LIGHT);
        subLbl.setAlignmentX(LEFT_ALIGNMENT);
        subLbl.setBorder(new EmptyBorder(5, 0, 0, 0));

        section.add(titleLbl);
        section.add(mainLbl);
        section.add(subLbl);

        return section;
    }

    private String generateStars(int count) {
        return "★".repeat(count);
    }

    private JPanel createSummaryRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(500, 30));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Dialog", Font.PLAIN, 15));
        l.setForeground(Theme.TEXT_LIGHT);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Dialog", Font.BOLD, 16));
        v.setForeground(Theme.TEXT);

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    private JPanel createCheckoutForm() {
        UIComponents.ShadowCard card = new UIComponents.ShadowCard(25);
        card.setLayout(new BorderLayout(0, 40));
        card.setBorder(new EmptyBorder(50, 50, 50, 50));

        JPanel inputs = new JPanel();
        inputs.setLayout(new BoxLayout(inputs, BoxLayout.Y_AXIS));
        inputs.setOpaque(false);

        JLabel formTitle = new JLabel("Contact Information");
        formTitle.setFont(new Font("Dialog", Font.BOLD, 24));
        formTitle.setForeground(Theme.TEXT);
        formTitle.setAlignmentX(LEFT_ALIGNMENT);

        JTextField nameField = new UIComponents.RoundedTextField("", "👤");
        JTextField emailField = new UIComponents.RoundedTextField(
                agentUI.getCurrentUserEmail() != null ? agentUI.getCurrentUserEmail() : "", "@");
        JTextField phoneField = new UIComponents.RoundedTextField("", "📱");

        inputs.add(formTitle);
        inputs.add(Box.createVerticalStrut(35));
        inputs.add(createFormField("Full Name", nameField));
        inputs.add(Box.createVerticalStrut(25));
        inputs.add(createFormField("Email Address", emailField));
        inputs.add(Box.createVerticalStrut(25));
        inputs.add(createFormField("Phone Number", phoneField));

        JButton payBtn = new UIComponents.GradientButton("Confirm & Book Now", Theme.SUCCESS, Theme.SUCCESS_DARK);
        payBtn.setPreferredSize(new Dimension(0, 65));
        payBtn.setFont(new Font("Dialog", Font.BOLD, 18));
        payBtn.addActionListener(e -> {
            if(nameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required details.",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            processBooking(nameField.getText().trim(), emailField.getText().trim(), phoneField.getText().trim());
        });

        card.add(inputs, BorderLayout.CENTER);
        card.add(payBtn, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFormField(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(600, 95));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Dialog", Font.BOLD, 15));
        lbl.setForeground(Theme.TEXT);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    // UPDATED: Now passes room count to ReservationManager
    private void processBooking(String name, String email, String phone) {
        new Thread(() -> {
            int flightId = Integer.parseInt(proposition.getVolPropose().getIdVol());
            int hotelId = Integer.parseInt(proposition.getHotelPropose().getIdHotel());

            boolean success = ReservationManager.createReservation(
                    name, email, phone,
                    flightId, hotelId, destination,
                    departureDate, returnDate, adults, children,
                    rooms,  // NEW: Pass room count
                    totalPrice
            );

            SwingUtilities.invokeLater(() -> {
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Booking Confirmed! Check your history for details.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Booking failed. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }
}