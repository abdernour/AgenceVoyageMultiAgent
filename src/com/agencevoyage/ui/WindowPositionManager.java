package com.agencevoyage.ui;

import java.awt.*;

/**
 * Utility class to manage window positioning and prevent overlap
 * Ensures windows respect taskbar and screen bounds
 */
public class WindowPositionManager {
    private static int windowOffset = 30;
    private static Point lastWindowPosition = null;
    
    /**
     * Get the usable screen bounds (respecting taskbar)
     */
    public static Rectangle getUsableScreenBounds() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return ge.getMaximumWindowBounds();
    }
    
    /**
     * Constrain a point and size to stay within screen bounds
     */
    public static Point constrainToScreen(Point location, Dimension size) {
        Rectangle screenBounds = getUsableScreenBounds();
        
        int x = location.x;
        int y = location.y;
        
        // Ensure window doesn't go off the right or bottom
        if (x + size.width > screenBounds.x + screenBounds.width) {
            x = screenBounds.x + screenBounds.width - size.width;
        }
        if (y + size.height > screenBounds.y + screenBounds.height) {
            y = screenBounds.y + screenBounds.height - size.height;
        }
        
        // Ensure window doesn't go off the left or top
        if (x < screenBounds.x) {
            x = screenBounds.x;
        }
        if (y < screenBounds.y) {
            y = screenBounds.y;
        }
        
        return new Point(x, y);
    }
    
    /**
     * Get a position for a new window that doesn't overlap with previous windows
     */
    public static Point getNextWindowPosition(Dimension windowSize) {
        Rectangle screenBounds = getUsableScreenBounds();
        
        if (lastWindowPosition == null) {
            // First window: center on screen
            int x = screenBounds.x + (screenBounds.width - windowSize.width) / 2;
            int y = screenBounds.y + (screenBounds.height - windowSize.height) / 2;
            lastWindowPosition = new Point(x, y);
            return constrainToScreen(lastWindowPosition, windowSize);
        }
        
        // Subsequent windows: offset from last position
        int newX = lastWindowPosition.x + windowOffset;
        int newY = lastWindowPosition.y + windowOffset;
        
        // Make sure window stays on screen
        if (newX + windowSize.width > screenBounds.x + screenBounds.width) {
            newX = screenBounds.x + windowOffset;
        }
        if (newY + windowSize.height > screenBounds.y + screenBounds.height) {
            newY = screenBounds.y + windowOffset;
        }
        
        lastWindowPosition = new Point(newX, newY);
        return constrainToScreen(lastWindowPosition, windowSize);
    }
    
    /**
     * Register an existing window's position (useful for the main SearchFrame)
     */
    public static void registerWindowPosition(Point position) {
        lastWindowPosition = position;
    }
    
    /**
     * Reset the position manager (useful when search frame is reopened)
     */
    public static void reset() {
        lastWindowPosition = null;
    }
}
