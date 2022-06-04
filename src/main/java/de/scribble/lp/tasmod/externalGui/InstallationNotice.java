package de.scribble.lp.tasmod.externalGui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import java.awt.event.ActionListener;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.Box;

public class InstallationNotice {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6543906188129304021L;
	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			InstallationNotice dialog = new InstallationNotice();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public InstallationNotice() {
		frame = new JFrame();
		Container contentPanel = frame.getContentPane();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		frame.setIconImage(Toolkit.getDefaultToolkit()
				.getImage(InputContainerView.class.getResource("/assets/tasmod/textures/potion2.png")));
		frame.setBounds(100, 100, 399, 147);
		frame.setLocationRelativeTo(null);
		frame.setTitle("TASmod");
		frame.setFont(new Font("Arial", Font.PLAIN, 12));
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
		frame.getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("OK");
		buttonPane.add(okButton);
		okButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				frame.setVisible(false);
				frame.dispose();
			}
		});
		frame.getRootPane().setDefaultButton(okButton);
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		
				JLabel lblNewLabel = new JLabel("You need Minecraft Forge to install TASmod. A guide can be found here:");
				panel.add(lblNewLabel);
		JLabel link = new JLabel("https://minecrafttas.com/tutorials/installation/setup_mclauncher/");
		panel.add(link);
		link.setForeground(Color.BLUE);
		link.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					try {
						String element = "Hydrogen";
						URI uri = new URI("https://minecrafttas.com/tutorials/installation/setup_mclauncher/#mc-versions-189---1122");
						desktop.browse(uri);
					} catch (IOException ex) {
						ex.printStackTrace();
					} catch (URISyntaxException ex) {
						ex.printStackTrace();
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				Font font = link.getFont();
				Map attributes = font.getAttributes();
				attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
				link.setFont(font.deriveFont(attributes));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				link.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				Font font = link.getFont();
				Map attributes = font.getAttributes();
				attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE);
				link.setFont(font.deriveFont(attributes));
			}
		});
		frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void startNoticeView() {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				main(null);
			}
		});
	}
}
