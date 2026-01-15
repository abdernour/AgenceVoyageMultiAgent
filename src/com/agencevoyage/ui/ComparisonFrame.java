package com.agencevoyage.ui;

import com.agencevoyage.agents.AgentUI;
import com.agencevoyage.ontology.concepts.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.util.List;

/**
 * Price comparison window - UPDATED to track room count
 */
public class ComparisonFrame extends JFrame {
    private AgentUI agentUI;
    private List<Proposition> allPropositions;
    private String destination;
    private Date departureDate, returnDate;
    private int adults, children, rooms;  // NEW: rooms field
    private boolean isMaximized = false;
    private Rectangle normalBounds = null;
    private JButton maxButton;

    public ComparisonFrame(AgentUI agent, List<Proposition> props, String dest,
                           Date dep, Date ret, int ad, int ch, int rm) {  // NEW: rm parameter
        this.agentUI = agent;
        this.allPropositions = props;
        this.destination = dest;
        this.departureDate = dep;
        this.returnDate = ret;
        this.adults = ad;
        this.children = ch;
        this.rooms = rm;  // NEW

        setSize(1400, 900);
        Point position = WindowPositionManager.getNextWindowPosition(new Dimension(1400, 900));
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
        main.add(createOfferList(), BorderLayout.CENTER);

        setContentPane(main);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(45, 70, 25, 70));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);

        JButton back = new JButton("← Back to Search");
        back.setFont(new Font("Dialog", Font.BOLD, 15));
        back.setForeground(Theme.PRIMARY);
        back.setBorder(null);
        back.setContentAreaFilled(false);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> {
            dispose();
            WindowPositionManager.reset();
            // Re-register SearchFrame's centered position
            JFrame searchFrame = agentUI.getSearchFrame();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle screenBounds = ge.getMaximumWindowBounds();
            int centerX = (screenBounds.width - searchFrame.getWidth()) / 2;
            int centerY = (screenBounds.height - searchFrame.getHeight()) / 2;
            WindowPositionManager.registerWindowPosition(new Point(centerX, centerY));
            searchFrame.setVisible(true);
        });

        JLabel title = new JLabel("Found " + allPropositions.size() + " Travel Options for " + destination);
        title.setFont(new Font("Dialog", Font.BOLD, 32));
        title.setForeground(Theme.TEXT);
        title.setBorder(new EmptyBorder(15, 0, 0, 0));

        leftPanel.add(back, BorderLayout.NORTH);
        leftPanel.add(title, BorderLayout.CENTER);

        // Window controls (maximize, minimize, close)
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        controls.setOpaque(false);
        
        maxButton = createControlButton("□", e -> toggleMaximize());
        JButton min = createControlButton("–", e -> setState(ICONIFIED));
        JButton close = createControlButton("✕", e -> {
            dispose();
            WindowPositionManager.reset();
            JFrame searchFrame = agentUI.getSearchFrame();
            if (searchFrame != null) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Rectangle screenBounds = ge.getMaximumWindowBounds();
                int centerX = (screenBounds.width - searchFrame.getWidth()) / 2;
                int centerY = (screenBounds.height - searchFrame.getHeight()) / 2;
                WindowPositionManager.registerWindowPosition(new Point(centerX, centerY));
                searchFrame.setVisible(true);
            }
        });
        
        controls.add(maxButton);
        controls.add(min);
        controls.add(close);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);
        
        // Make the entire header draggable
        makeDraggable(header);
        return header;
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

    private JScrollPane createOfferList() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(30, 70, 80, 70));

        for (int i = 0; i < allPropositions.size(); i++) {
            Proposition p = allPropositions.get(i);
            container.add(createOfferCard(p, i == 0));
            if (i < allPropositions.size() - 1) {
                container.add(Box.createVerticalStrut(30));
            }
        }

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        UIComponents.styleScrollBar(scroll.getVerticalScrollBar());
        return scroll;
    }

    private JPanel createOfferCard(Proposition prop, boolean isBestDeal) {
        UIComponents.ShadowCard card = new UIComponents.ShadowCard(22);
        card.setLayout(new BorderLayout(40, 0));
        card.setBorder(new EmptyBorder(35, 45, 35, 45));
        card.setMaximumSize(new Dimension(1260, 180));

        // Left: Details
        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.setOpaque(false);

        if (isBestDeal) {
            JLabel badge = new JLabel("✓ Best Deal");
            badge.setFont(new Font("Dialog", Font.BOLD, 13));
            badge.setForeground(Theme.SUCCESS);
            badge.setOpaque(true);
            badge.setBackground(new Color(34, 197, 94, 30));
            badge.setBorder(new EmptyBorder(6, 12, 6, 12));
            badge.setAlignmentX(LEFT_ALIGNMENT);
            details.add(badge);
            details.add(Box.createVerticalStrut(15));
        }

        JLabel flight = new JLabel("✈  " + prop.getVolPropose().getCompagnie() + " Airways");
        flight.setFont(new Font("Dialog", Font.BOLD, 20));
        flight.setForeground(Theme.TEXT);
        flight.setAlignmentX(LEFT_ALIGNMENT);

        JLabel hotel = new JLabel("⌂  " + prop.getHotelPropose().getNom() + " (" +
                generateStars(prop.getHotelPropose().getCategorie()) + ") - " + rooms + " Room(s)");  // NEW
        hotel.setFont(new Font("Dialog", Font.PLAIN, 17));
        hotel.setForeground(Theme.TEXT_LIGHT);
        hotel.setAlignmentX(LEFT_ALIGNMENT);
        hotel.setBorder(new EmptyBorder(10, 0, 0, 0));

        details.add(flight);
        details.add(hotel);

        // Right: Price and Action
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 35, 0));
        actions.setOpaque(false);

        JPanel pricePanel = new JPanel();
        pricePanel.setLayout(new BoxLayout(pricePanel, BoxLayout.Y_AXIS));
        pricePanel.setOpaque(false);

        JLabel priceLabel = new JLabel("Total Price");
        priceLabel.setFont(new Font("Dialog", Font.PLAIN, 13));
        priceLabel.setForeground(Theme.TEXT_LIGHT);
        priceLabel.setAlignmentX(RIGHT_ALIGNMENT);

        JLabel price = new JLabel(String.format("%.0f DA", prop.getCoutTotal()));
        price.setFont(new Font("Dialog", Font.BOLD, 36));
        price.setForeground(Theme.PRIMARY);
        price.setAlignmentX(RIGHT_ALIGNMENT);

        pricePanel.add(priceLabel);
        pricePanel.add(price);

        JButton select = new UIComponents.GradientButton("Select Deal");
        select.setPreferredSize(new Dimension(200, 55));
        select.setFont(new Font("Dialog", Font.BOLD, 16));
        select.addActionListener(e -> {
            dispose();
            // UPDATED: Pass rooms parameter
            new ResultsFrame(agentUI, prop, destination, departureDate, returnDate,
                    adults, children, rooms).setVisible(true);
        });

        actions.add(pricePanel);
        actions.add(select);

        card.add(details, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    private String generateStars(int count) {
        return "★".repeat(count);
    }
}