package GUI;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileSystemView;

import Controller.UserProperties;

public class FileChooserDialog {

	public FileChooserDialog() {

		JFrame jf = new JFrame();
		jf.setIconImage(Toolkit.getDefaultToolkit().getImage(
				ClientSignUpGUI.class.getResource("/Images/fb.png")));

		FileSystemView fsv = new SingleRootFileSystemView(new File(
				UserProperties.getDirectory()));
		JFileChooser chooser = new JFileChooser("Choose File To Share");
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setApproveButtonText("Share Files");
		chooser.setFileSystemView(fsv);
		chooser.setBackground(new Color(248, 248, 255));
		chooser.updateUI();
		chooser.setCurrentDirectory(new File(UserProperties.getDirectory()));
		int choice = chooser.showOpenDialog(jf);
		if (choice != JFileChooser.APPROVE_OPTION)
			return;
		JOptionPane
				.showInputDialog(jf, "Email addresses should be separated by a comma ", "Enter Email Addreses To Share With", JOptionPane.PLAIN_MESSAGE);
		File[] chosenFile = chooser.getSelectedFiles();
		for (File f : chosenFile) {
			System.out.println("The name of the files is " + f.getName());
		}
	}

}
