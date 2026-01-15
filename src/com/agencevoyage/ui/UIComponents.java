package com.agencevoyage.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

public class UIComponents {

    private static final Font ICON_FONT = new Font("Dialog", Font.PLAIN, 18);

    public static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, Theme.BG_START, 0, getHeight(), Theme.BG_END);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public static class ShadowCard extends JPanel {
        private int radius;
        private boolean isHovered = false;

        public ShadowCard(int radius) {
            this.radius = radius;
            setBackground(Theme.CARD);
            setOpaque(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Enhanced shadow
            int shadowSize = isHovered ? 12 : 8;
            for(int i = 0; i < shadowSize; i++) {
                int alpha = isHovered ? (15 - i) : (10 - i);
                if (alpha < 0) alpha = 0;
                g2.setColor(new Color(0, 0, 0, alpha));
                g2.fillRoundRect(i, i + 3, getWidth() - (i * 2), getHeight() - (i * 2), radius, radius);
            }

            g2.setColor(getBackground());
            g2.fillRoundRect(3, 3, getWidth() - 10, getHeight() - 10, radius, radius);

            g2.setColor(Theme.BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(3, 3, getWidth() - 10, getHeight() - 10, radius, radius);

            g2.dispose();
        }
    }

    public static class GradientButton extends JButton {
        private Color c1, c2;
        private boolean isPressed = false;

        public GradientButton(String text) {
            this(text, Theme.PRIMARY, Theme.PRIMARY_DARK);
        }

        public GradientButton(String text, Color color1, Color color2) {
            super(text);
            this.c1 = color1;
            this.c2 = color2;
            setFont(new Font("Dialog", Font.BOLD, 15));
            setForeground(Color.WHITE);
            setBorder(new EmptyBorder(14, 30, 14, 30));
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isPressed) {
                g2.translate(0, 2);
            }

            GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), 0, c2);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

            super.paintComponent(g);
            g2.dispose();
        }
    }

    public static class RoundedTextField extends JTextField {
        private String icon;
        private boolean isFocused = false;

        public RoundedTextField(String text, String icon) {
            super(text);
            this.icon = icon;
            setOpaque(false);
            setFont(new Font("Dialog", Font.PLAIN, 16));
            setForeground(Theme.TEXT);
            setCaretColor(Theme.PRIMARY);
            setBorder(new EmptyBorder(16, icon != null ? 50 : 18, 16, 18));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Theme.INPUT_BG);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);

            g2.setColor(isFocused ? Theme.PRIMARY : Theme.BORDER);
            g2.setStroke(new BasicStroke(isFocused ? 2f : 1f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);

            if (icon != null) {
                g2.setFont(ICON_FONT);
                g2.setColor(Theme.TEXT_LIGHT);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(icon, 18, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class RoundedSpinner extends JSpinner {
        private String icon;

        public RoundedSpinner(SpinnerModel model, String icon) {
            super(model);
            this.icon = icon;
            setOpaque(false);
            setBorder(null);

            JComponent ed = getEditor();
            if (ed instanceof JSpinner.DefaultEditor) {
                JTextField tf = ((JSpinner.DefaultEditor) ed).getTextField();
                tf.setBorder(new EmptyBorder(16, icon != null ? 50 : 18, 16, 18));
                tf.setBackground(Theme.INPUT_BG);
                tf.setFont(new Font("Dialog", Font.PLAIN, 16));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Theme.INPUT_BG);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);

            g2.setColor(Theme.BORDER);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);

            if (icon != null) {
                g2.setFont(ICON_FONT);
                g2.setColor(Theme.TEXT_LIGHT);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(icon, 18, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
            super.paintComponent(g);
            g2.dispose();
        }
    }

    public static class RoundedDateChooser extends JDateChooser {
        private String icon;
        private boolean isFocused = false;

        public RoundedDateChooser(String icon) {
            this.icon = icon;
            setOpaque(false);
            setBorder(null);
            setDateFormatString("dd/MM/yyyy");

            JTextFieldDateEditor editor = (JTextFieldDateEditor) getDateEditor();
            editor.setBorder(new EmptyBorder(16, icon != null ? 50 : 18, 16, 45));
            editor.setBackground(Theme.INPUT_BG);
            editor.setFont(new Font("Dialog", Font.PLAIN, 16));

            editor.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Theme.INPUT_BG);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);

            g2.setColor(isFocused ? Theme.PRIMARY : Theme.BORDER);
            g2.setStroke(new BasicStroke(isFocused ? 2f : 1f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);

            if (icon != null) {
                g2.setFont(ICON_FONT);
                g2.setColor(Theme.TEXT_LIGHT);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(icon, 18, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
            super.paintComponent(g);
            g2.dispose();
        }
    }

    public static void styleScrollBar(JScrollBar scrollBar) {
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(203, 213, 225);
                trackColor = new Color(0,0,0,0);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0,0));
                return b;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(r.x + 2, r.y, r.width - 4, r.height, 8, 8);
                g2.dispose();
            }
        });
        scrollBar.setPreferredSize(new Dimension(8, 0));
    }
}