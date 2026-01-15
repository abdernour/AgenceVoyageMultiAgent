package com.agencevoyage.ui;

import com.agencevoyage.agents.AgentUI;
import com.agencevoyage.utils.DatabaseManager;
import com.agencevoyage.utils.ReservationManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * Booking History Dialog - WITH CANCELLATION FEATURE
 */
public class BookingHistoryDialog extends JDialog {
    private AgentUI agentUI;
    private JFrame parentFrame;
    private DefaultTableModel tableModel;
    private JTable table;
    private String currentEmail;

    public BookingHistoryDialog(JFrame parent, AgentUI agent) {
        super(parent, "My Bookings", true);
        this.parentFrame = parent;
        this.agentUI = agent;

        // Get email first if not set
        currentEmail = agentUI.getCurrentUserEmail();
        if (currentEmail == null) {
            String email = JOptionPane.showInputDialog(parent,
                    "Enter your email to view your bookings:",
                    "My Bookings",
                    JOptionPane.QUESTION_MESSAGE);
            if (email == null || email.trim().isEmpty()) {
                dispose();
                return;
            }
            currentEmail = email.trim();
            agentUI.setCurrentUserEmail(currentEmail);
        }

        setSize(1200, 700);
        setLocationRelativeTo(parent);

        JPanel main = createMainPanel();
        setContentPane(main);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG_START);

        // Header
        panel.add(createHeader(), BorderLayout.NORTH);

        // Table
        JScrollPane scroll = createHistoryTable();
        scroll.setBorder(new EmptyBorder(20, 40, 40, 40));
        scroll.setBackground(Theme.BG_START);
        scroll.getViewport().setBackground(Theme.BG_START);
        UIComponents.styleScrollBar(scroll.getVerticalScrollBar());

        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PRIMARY);
        header.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Your Booking History");
        title.setFont(new Font("Dialog", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel user = new JLabel(currentEmail);
        user.setForeground(Theme.PRIMARY_LIGHT);
        user.setFont(new Font("Dialog", Font.PLAIN, 14));

        header.add(title, BorderLayout.WEST);
        header.add(user, BorderLayout.EAST);

        return header;
    }

    private JScrollPane createHistoryTable() {
        String[] columns = {"ID", "Booking Ref", "Destination", "Departure", "Return",
                "Travelers", "Rooms", "Price", "Status", "Action"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                // Only Action column (9) is editable
                return c == 9;
            }
        };

        loadBookings();

        table = new JTable(tableModel);
        table.setFont(new Font("Dialog", Font.PLAIN, 14));
        table.setRowHeight(55);
        table.setIntercellSpacing(new Dimension(10, 5));
        table.setShowGrid(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Header styling
        table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 14));
        table.getTableHeader().setBackground(Theme.BG_END);
        table.getTableHeader().setForeground(Theme.TEXT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 50));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Ref
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Dest
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Dep
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Ret
        table.getColumnModel().getColumn(5).setPreferredWidth(80);  // Travelers
        table.getColumnModel().getColumn(6).setPreferredWidth(70);  // Rooms
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Price
        table.getColumnModel().getColumn(8).setPreferredWidth(100); // Status
        table.getColumnModel().getColumn(9).setPreferredWidth(120); // Action

        // Status column with colors
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusCellRenderer());

        // Action column with button
        table.getColumnModel().getColumn(9).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(9).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        return scrollPane;
    }

    private void loadBookings() {
        tableModel.setRowCount(0);

        try {
            Connection conn = DatabaseManager.getConnection();
            String sql = "SELECT r.*, f.airline, h.name as hotel_name " +
                    "FROM reservations r " +
                    "LEFT JOIN flights f ON r.flight_id = f.flight_id " +
                    "LEFT JOIN hotels h ON r.hotel_id = h.hotel_id " +
                    "WHERE r.user_email = ? " +
                    "ORDER BY r.booking_date DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentEmail);
            ResultSet rs = stmt.executeQuery();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                String status = rs.getString("status");

                tableModel.addRow(new Object[]{
                        reservationId,
                        rs.getString("booking_reference"),
                        rs.getString("destination"),
                        sdf.format(rs.getDate("departure_date")),
                        sdf.format(rs.getDate("return_date")),
                        rs.getInt("adults") + rs.getInt("children"),
                        rs.getInt("rooms"),
                        String.format("%.0f DA", rs.getFloat("total_price")),
                        status,
                        "CONFIRMED".equals(status) ? "Cancel" : "" // Button text
                });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load bookings: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Custom renderer for status column with colors
    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String status = (String) value;
            setHorizontalAlignment(CENTER);
            setFont(new Font("Dialog", Font.BOLD, 13));

            if ("CONFIRMED".equals(status)) {
                setForeground(Theme.SUCCESS);
                setBackground(isSelected ? table.getSelectionBackground() : new Color(34, 197, 94, 20));
            } else if ("CANCELLED".equals(status)) {
                setForeground(Theme.ACCENT);
                setBackground(isSelected ? table.getSelectionBackground() : new Color(244, 63, 94, 20));
            } else {
                setForeground(Theme.TEXT_LIGHT);
                setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            }

            return c;
        }
    }

    // Button renderer for action column
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            String text = (value == null) ? "" : value.toString();
            setText(text);

            if (text.isEmpty()) {
                setVisible(false);
            } else {
                setVisible(true);
                setFont(new Font("Dialog", Font.BOLD, 12));
                setForeground(Color.WHITE);
                setBackground(Theme.ACCENT);
                setBorder(new EmptyBorder(8, 16, 8, 16));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            return this;
        }
    }

    // Button editor for action column
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Dialog", Font.BOLD, 12));
            button.setForeground(Color.WHITE);
            button.setBackground(Theme.ACCENT);
            button.setBorder(new EmptyBorder(8, 16, 8, 16));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            button.addActionListener(e -> {
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {

            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                handleCancelBooking(currentRow);
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    private void handleCancelBooking(int row) {
        int reservationId = (int) tableModel.getValueAt(row, 0);
        String bookingRef = (String) tableModel.getValueAt(row, 1);
        String status = (String) tableModel.getValueAt(row, 8);

        if (!"CONFIRMED".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "This booking cannot be cancelled.",
                    "Invalid Action",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel booking " + bookingRef + "?\n\n" +
                        "This will restore flight seats and hotel rooms.\n" +
                        "This action cannot be undone.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Show loading (non-modal so it doesn't block)
        JDialog loadingDialog = createLoadingDialog();

        // Cancel in background thread
        new Thread(() -> {
            try {
                // Show loading dialog
                SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));

                // Perform cancellation
                boolean success = ReservationManager.cancelReservation(reservationId);

                // Close loading and show result
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();

                    if (success) {
                        JOptionPane.showMessageDialog(BookingHistoryDialog.this,
                                "Booking " + bookingRef + " has been cancelled successfully.\n" +
                                        "Flight seats and hotel rooms have been restored.",
                                "Cancellation Successful",
                                JOptionPane.INFORMATION_MESSAGE);

                        // Reload table
                        loadBookings();
                    } else {
                        JOptionPane.showMessageDialog(BookingHistoryDialog.this,
                                "Failed to cancel booking. Please try again or contact support.",
                                "Cancellation Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    JOptionPane.showMessageDialog(BookingHistoryDialog.this,
                            "Error: " + e.getMessage(),
                            "Cancellation Error",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private JDialog createLoadingDialog() {
        JDialog dialog = new JDialog(this, "Processing...", false); // CHANGED: false = non-modal
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 2),
                new EmptyBorder(30, 30, 30, 30)
        ));

        JLabel spinner = new JLabel("⏳");
        spinner.setFont(new Font("Dialog", Font.PLAIN, 48));
        spinner.setAlignmentX(CENTER_ALIGNMENT);

        JLabel text = new JLabel("Cancelling booking...");
        text.setFont(new Font("Dialog", Font.BOLD, 14));
        text.setForeground(Theme.TEXT);
        text.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(spinner);
        panel.add(Box.createVerticalStrut(15));
        panel.add(text);

        dialog.setContentPane(panel);

        return dialog;
    }
}