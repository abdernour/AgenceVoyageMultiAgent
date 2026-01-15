package com.agencevoyage.ui;

import java.awt.Color;

/**
 * Enhanced theme configuration for a premium, modern feel
 */
public class Theme {

    // Primary Brand Colors - Modern Indigo
    public static final Color PRIMARY = new Color(99, 102, 241);
    public static final Color PRIMARY_DARK = new Color(79, 70, 229);
    public static final Color PRIMARY_LIGHT = new Color(199, 210, 254);

    // Accent & Semantic Colors
    public static final Color ACCENT = new Color(244, 63, 94); // Rose/Red accent
    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Color SUCCESS_DARK = new Color(21, 128, 61);

    // Backgrounds - Neutral Slate scale
    public static final Color BG_START = new Color(248, 250, 252);
    public static final Color BG_END = new Color(226, 232, 240);
    public static final Color CARD = Color.WHITE;
    public static final Color CARD_HOVER = new Color(252, 252, 255);

    // Text - High contrast Slate
    public static final Color TEXT = new Color(15, 23, 42);
    public static final Color TEXT_LIGHT = new Color(100, 116, 139);
    public static final Color TEXT_MUTED = new Color(148, 163, 184);

    // Input & Borders
    public static final Color INPUT_BG = new Color(255, 255, 255);
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Color BORDER_FOCUS = new Color(165, 180, 252);

    public static Color blend(Color c1, Color c2, float ratio) {
        int r = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int g = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int b = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return new Color(r, g, b);
    }
}