package GUI;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
 
public class TrayIconBasic {
	
	private static TrayIcon trayIcon;
    
    public TrayIconBasic() {
    	
    	try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
        if (!SystemTray.isSupported()) {
            // Go directory to the task;
            return;
        } 
         
        Image icon = createIcon("/Images/fb.png", "NSYNC File Sharing");
        if (icon == null) {
            // Go directory to the task;
            return;
        }
 
        // create the trayIcon itself.
       trayIcon = new TrayIcon(icon);
        //final TrayIcon trayIcon2 = new TrayIcon(icon, "Ok", new JPopupMenu());
 
        // access the system tray. If not supported 
        // or if notification area is not present (Ubuntu)  
        // a NotSupportedException exception is thrown;
 
        final SystemTray tray = SystemTray.getSystemTray();
 
        // Create popup menu
        PopupMenu popup = new PopupMenu();
        MenuItem exit = new MenuItem("Exit");
        MenuItem shareFiles = new MenuItem("Share Files");
 
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Do some cleanup
                tray.remove(trayIcon);
                System.exit(0);
            }
        });
        
        shareFiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Do some cleanup
               //JOptionPane.showMessageDialog(null, "Coming soon!!!");
            	
            	new FileChooserDialog();
            }
        });
 
        popup.add(shareFiles);
        popup.addSeparator();
        popup.add(exit); 
         
        // Add tooltip and menu to trayicon
        trayIcon.setToolTip("NSync");
        trayIcon.setPopupMenu(popup);
 
        // Add the trayIcon to system tray/notification
        // area
 
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("Could not load tray icon !");
        }        
         
        // Just to show how to add an alert/error message
        trayIcon.displayMessage("Alert", "NSYNC Started!!!", 
            TrayIcon.MessageType.INFO);        
    }
 
    //used to send messages to the tray Icon
    public static void displayMessage(String caption, String message, TrayIcon.MessageType messageType) {
    	trayIcon.displayMessage(caption, message, messageType);       
    }
 
 
    // A handy method to create an Image instance.
    protected static Image createIcon(String path, String description) {
        URL imageURL = TrayIconBasic.class.getResource(path);
        if (imageURL == null) {
            System.err.println(path + " not found");
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}