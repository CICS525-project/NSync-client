package GUI;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileSystemView;

import com.microsoft.azure.storage.StorageException;

import Controller.UserProperties;

public class FileChooserDialog {

	public FileChooserDialog() throws InvalidKeyException, URISyntaxException, StorageException {

		JFrame jf = new JFrame();
		jf.setIconImage(Toolkit.getDefaultToolkit().getImage(
				ClientSignUpGUI.class.getResource("/Images/fb.png")));

		FileSystemView fsv = new SingleRootFileSystemView(new File(
				UserProperties.getDirectory()));
		JFileChooser chooser = new JFileChooser("Choose File To Share");
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setApproveButtonText("Share Files");
		chooser.setFileSystemView(fsv);
		chooser.setBackground(new Color(248, 248, 255));
		chooser.updateUI();
		chooser.setCurrentDirectory(new File(UserProperties.getDirectory()));
		int choice = chooser.showOpenDialog(jf);
		if (choice != JFileChooser.APPROVE_OPTION)
			return;
		String text = JOptionPane
				.showInputDialog(
						jf,
						"Enter Email Addresses To Share With. \nAll links expire after 1 hour\n Each email address should be separated with a comma",
						"Enter Emails", JOptionPane.PLAIN_MESSAGE);
		File[] chosenFile = chooser.getSelectedFiles();
		String[] emails = text.trim().split(",");

		for (String email : emails) {
			System.out.println("The emails are " + email);
			String message = "Hello , \n\n" +
		UserProperties.getUsername() + " has shared the following files with you. \n\n" + 
					"The filles and links are \n";
			for (File f : chosenFile) {				
				System.out.println("The name of the files is " + f.getName());
				message += "\n " + f.getName() + " - " + ShareFiles.shareFiles(UserProperties.getUsername(), f.getName());
			}
			
			message += "\n\nThe links expire after one hour so you need to download them very fast";
			
			new SendMail(email, "NSync Files Shared", message);
		}
	}

}
