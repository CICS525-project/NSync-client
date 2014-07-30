package GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import java.awt.Font;
import java.awt.Color;
import java.awt.Toolkit;

public class ClientSignUpGUI extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField emailField;
	private JButton okButton;
	private JLabel message;
	private JTextField password;
	private JTextField textField;

	public JLabel getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		getMessage().setText(message);
	}	
	
	public ClientSignUpGUI getThis() {
		return this;
	}

	/**
	 * Create the dialog.
	 */
	public ClientSignUpGUI() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(ClientSignUpGUI.class.getResource("/Images/fb.png")));
		getContentPane().setFont(new Font("SansSerif", Font.PLAIN, 14));
		setTitle("Create Account");
		setType(Type.POPUP);
		setModal(true);
		setBounds(100, 100, 500, 239);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBackground(new Color(248, 248, 255));
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(23dlu;default)"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(23dlu;default)"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(23dlu;default)"),}));
		{
			message = new JLabel("");
			contentPanel.add(message, "6, 2");
		}
		{
			JLabel lblUsername = new JLabel("Username");
			lblUsername.setFont(new Font("SansSerif", Font.PLAIN, 14));
			contentPanel.add(lblUsername, "4, 4, right, default");
		}
		{
			textField = new JTextField();
			textField.setFont(new Font("Tahoma", Font.PLAIN, 14));
			textField.setName("username");
			textField.setColumns(10);
			contentPanel.add(textField, "6, 4, fill, fill");
		}
		{
			JLabel lblPassword = new JLabel("Password");
			lblPassword.setFont(new Font("SansSerif", Font.PLAIN, 14));
			contentPanel.add(lblPassword, "4, 6, right, default");
		}
		{
			password = new JTextField();
			password.setFont(new Font("Tahoma", Font.PLAIN, 14));
			password.setName("password");
			password.setColumns(10);
			contentPanel.add(password, "6, 6, fill, fill");
		}
		{
			JLabel lblEnterYourEmail = new JLabel("Email");
			lblEnterYourEmail.setFont(new Font("SansSerif", Font.PLAIN, 14));
			contentPanel.add(lblEnterYourEmail, "4, 8, right, default");
		}
		{
			emailField = new JTextField();
			emailField.setName("email");
			contentPanel.add(emailField, "6, 8, fill, fill");
			emailField.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBackground(new Color(248, 248, 255));
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("Create Account");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						String username = getUsername().getText();
						String password = getPassword().getText();
						String email = getEmailField().getText();
						System.out.println(username + " " + password + " " + email);
						ClientHelper.createAccount(getThis(), username, password, email);
					}
				});
				okButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						dispose();
					}
				});
				buttonPane.add(cancelButton);
			}
		}
	}

	public JTextField getEmailField() {
		return emailField;
	}

	public JButton getOkButton() {
		return okButton;
	}
	public JTextField getUsername() {
		return textField;
	}
	public JTextField getPassword() {
		return password;
	}
}