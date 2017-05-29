package sat.gui;

import sat.SelfAssessmentTool;

import javax.swing.*;
import java.awt.*;
import java.net.URL;


public class TrayManager {
    public void showTray() {
        if (!SystemTray.isSupported()) {
            return;
        }
        Image imageIcon = createImage("trayicon.png", "tray icon");
        if (imageIcon == null) return;
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon =
                new TrayIcon(imageIcon);
        trayIcon.setImageAutoSize(true);
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a pop-up menu components
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(s -> System.exit(0));
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }//Obtain the image URL
    protected static Image createImage(String path, String description) {
        URL imageURL = SelfAssessmentTool.class.getClassLoader().getResource(path);
        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }

}
