package de.scribble.lp.tasmod.externalGui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.inputcontainer.InputContainer;
import de.scribble.lp.tasmod.inputcontainer.TickInputContainer;
import de.scribble.lp.tasmod.virtual.VirtualInput;
import de.scribble.lp.tasmod.virtual.VirtualKeyboardEvent;
import de.scribble.lp.tasmod.virtual.VirtualMouseEvent;

public class InputContainerView extends JFrame {

	private static final long serialVersionUID = -1823965270972132025L;
	private JPanel contentPane;
	private static JTable table;
	private static DefaultTableModel model;
	private static int prevCount = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					InputContainerView frame = new InputContainerView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public InputContainerView() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(InputContainerView.class.getResource("/assets/tasmod/textures/potion2.png")));
		setBackground(Color.WHITE);
		setTitle("InputContainer View");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 673, 448);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);

		Vector<String> title = new Vector<String>();
		title.add("Ticks");
		title.add("Keyboard");
		title.add("Mouse");
		title.add("CameraX");
		title.add("CameraY");

		model = new DefaultTableModel(title, 0);

		table = new JTable(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(5);
		table.setBackground(Color.DARK_GRAY);
		table.setForeground(Color.LIGHT_GRAY);
		scrollPane.setViewportView(table);

		contentPane.add(table.getTableHeader(), BorderLayout.PAGE_START);

	}

//	private static void addEmptyRow() {
//		addRow(0, "", "", 0F, 0F);
//	}

	private static void addRow(int ticks, String keyboard, String mouse, float cameraX, float cameraY) {
		Vector<Object> row = new Vector<Object>(5);
		row.add(ticks);
		row.add(keyboard);
		row.add(mouse);
		row.add(cameraX);
		row.add(cameraY);
		model.addRow(row);
	}

	private static void selectRow(int ticks) {
		if (ticks >= table.getRowCount()) {
			ticks = table.getRowCount() - 1;
		}
		table.setRowSelectionInterval(ticks, ticks);
		table.scrollRectToVisible(new Rectangle(table.getCellRect(ticks, 0, true)));
	}

	public static void update(VirtualInput input) {
		if (model == null) {
			return;
		}
		InputContainer container = input.getContainer();
		if (container == null || container.isEmpty()) {
			return;
		}
		if (prevCount != container.size()) {
			prevCount = container.size();
			model.getDataVector().clear();

			for (int i = 0; i < container.size(); i++) {
				TickInputContainer tickContainer = container.get(i);
				addRow(i + 1, tickContainer.getKeyboard().toString(), tickContainer.getMouse().toString(),
						tickContainer.getSubticks().getPitch(), tickContainer.getSubticks().getYaw());
			}
			selectRow(container.index());
		}
		if (!container.isNothingPlaying()) {
			selectRow(container.index());
		}

//		selectRow(container.index()+1);
//		input.getAllInputEvents().forEach(inputsevents->{
//			Vector<Object> row=new Vector<Object>(4);
//			row.add(inputsevents.tick);
//			
//			String kbs="";
//			int count=0;
//			for (VirtualKeyboardEvent keyb:inputsevents.keyboardevent) {
//				kbs=kbs.concat("["+count+"]{"+keyb.toString()+"} ");
//				count++;
//			}
//			
//			row.add(kbs);
//			
//			count=0;
//			String mbe="";
//			for (VirtualMouseEvent mouseb:inputsevents.mouseEvent) {
//				mbe=mbe.concat("["+count+"]{"+mouseb.toString()+"} ");
//				count++;
//			}
//			row.add(mbe);
//			
//			row.add(inputsevents.subticks.toString().replace("Camera:", ""));
//			model.addRow(row);
//		});

	}

	public static void startBufferView() {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					InputContainerView frame = new InputContainerView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
