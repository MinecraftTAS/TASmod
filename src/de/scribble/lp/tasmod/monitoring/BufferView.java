package de.scribble.lp.tasmod.monitoring;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
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
import de.scribble.lp.tasmod.virtual.VirtualInput;
import de.scribble.lp.tasmod.virtual.VirtualKeyboardEvent;
import de.scribble.lp.tasmod.virtual.VirtualMouseEvent;

public class BufferView extends JFrame {

	private static final long serialVersionUID = -1823965270972132025L;
	private JPanel contentPane;
	private JTable table;
	private static DefaultTableModel model;
	private JButton Refresh;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BufferView frame = new BufferView();
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
	public BufferView() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch (Exception e) {
		}
		setIconImage(Toolkit.getDefaultToolkit().getImage(BufferView.class.getResource("/assets/tasmod/textures/potion2.png")));
		setBackground(Color.WHITE);
		setTitle("Buffer Viewer");
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
		
		Vector<String> title=new Vector<String>();
		title.add("Ticks");
		title.add("KeyboardEvents");
		title.add("MouseEvents");
		title.add("Subticks");
		
		model=new DefaultTableModel(title, 0);
		
		table = new JTable(model);
		table.setBackground(Color.DARK_GRAY);
		table.setForeground(Color.LIGHT_GRAY);
		scrollPane.setViewportView(table);
		
		Refresh = new JButton("Refresh");
		Refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update(ClientProxy.virtual);
			}
		});
		scrollPane.setRowHeaderView(Refresh);
		
		contentPane.add(table.getTableHeader(), BorderLayout.PAGE_START);
		
	}

	public static void update(VirtualInput input) {
		model.getDataVector().clear();
		input.getAllInputEvents().forEach(inputsevents->{
			Vector<Object> row=new Vector<Object>(4);
			row.add(inputsevents.tick);
			
			String kbs="";
			int count=0;
			for (VirtualKeyboardEvent keyb:inputsevents.keyboardevent) {
				kbs=kbs.concat("["+count+"]{"+keyb.toString()+"} ");
				count++;
			}
			
			row.add(kbs);
			
			count=0;
			String mbe="";
			for (VirtualMouseEvent mouseb:inputsevents.mouseEvent) {
				mbe=mbe.concat("["+count+"]{"+mouseb.toString()+"} ");
				count++;
			}
			row.add(mbe);
			
			row.add(inputsevents.subticks.toString().replace("Camera:", ""));
			model.addRow(row);
		});
	}
	
	public static void startBufferView() {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					BufferView frame = new BufferView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
