package com.autotimetable;

import com.autotimetable.ui.MainForm;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Setup the FlatLaf Dark theme for modern and premium dark aesthetics
                if (FlatDarkLaf.setup()) {
                    // Customize UI component corner arcs to give a modern, sleek, slightly rounded feel
                    UIManager.put("Button.arc", 10);
                    UIManager.put("Component.arc", 10);
                    UIManager.put("ProgressBar.arc", 10);
                    UIManager.put("TextComponent.arc", 10);
                    UIManager.put("TabbedPane.showTabSeparators", true);
                    UIManager.put("TabbedPane.tabHeight", 36);
                }
                
                // Create, center, and display the main dashboard
                MainForm dashboard = new MainForm();
                dashboard.setLocationRelativeTo(null);
                dashboard.setVisible(true);
            } catch (Exception e) {
                System.err.println("Failed to initialize FlatLaf dark look and feel: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
