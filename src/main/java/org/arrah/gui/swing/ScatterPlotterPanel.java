package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2006      *
 *     http://www.arrah.in                     *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* 
 * This file is graphical inteface class for 
 * cluster graph.
 * It contains a PlotterPanel and build other
 * component in synch with that.
 *
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;

public class ScatterPlotterPanel extends JPanel implements ActionListener,
		ChangeListener, Serializable

{
	public PlotterPanel g_p; // Accessed for repainting

	private String _dsn = "", _table = "", _column = "", _type = "";
	private JFormattedTextField text;
	private int color_index = 0;
	private JSlider minS = null, maxS = null;
	private DefaultBoundedRangeModel sm_min, sm_max;
	private boolean s_a = false;

	public ScatterPlotterPanel() {

		setLayout(new BorderLayout());
		setOpaque(true);
		add(createTopPane(), BorderLayout.PAGE_START);
		add(createBotPane(), BorderLayout.CENTER);
		add(createSpane(), BorderLayout.LINE_START);
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		setSlideBar();
	}

	private JPanel createTopPane() {

		NumberFormat num = null;
		num = NumberFormat.getNumberInstance();
		num.setParseIntegerOnly(true);
		num.setMaximumIntegerDigits(10);

		JPanel topPane = new JPanel();
		topPane.setLayout(new GridLayout(0, 7));

		final JComboBox c_combo = new JComboBox(new String[] { "Red", "Green",
				"Yellow", "Blue" });
		c_combo.setBorder(new EmptyBorder(0, 4, 0, 0));
		c_combo.setRenderer(new MyCellRenderer());

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				color_index = c_combo.getSelectedIndex();
			}
		};
		c_combo.addActionListener(actionListener);

		JLabel c_l = new JLabel("Group data in:", JLabel.TRAILING);

		text = new JFormattedTextField(num);

		JButton cal = new JButton("Chart");
		cal.addActionListener(this);
		cal.addKeyListener(new KeyBoardListener());
		JButton zin = new JButton("Zoom In");
		zin.addActionListener(this);
		zin.addKeyListener(new KeyBoardListener());
		JButton zout = new JButton("Zoom Out");
		zout.addActionListener(this);
		zout.addKeyListener(new KeyBoardListener());
		JButton res = new JButton("Reset");
		res.addActionListener(this);
		res.addKeyListener(new KeyBoardListener());

		topPane.add(c_combo);
		topPane.add(c_l);
		topPane.add(text);
		topPane.add(cal);
		topPane.add(zin);
		topPane.add(zout);
		topPane.add(res);
		return topPane;
	}

	private JPanel createBotPane() {

		g_p = new PlotterPanel();
		return g_p;

	}

	public void init(Hashtable map) {
		_dsn = (String) map.get("Schema");
		_type = (String) map.get("Type");
		_table = (String) map.get("Table");
		_column = (String) map.get("Column");

	}

	protected int getGC() {
		int i = 0;
		if (text.getValue() == null)
			return i;
		i = Integer.parseInt(text.getValue().toString());
		if (i <= 0)
			i = 0;
		g_p.setGC(i); // Set Group Count Value
		return i;

	}

	public Vector fillValues() {

		final int gc = getGC();
		if (gc == 0)
			return null;

		int counter = 0;
		double d = 0;
		double sum = 0;
		Vector<Double> vc = new Vector<Double>(20, 5);
		int i = 0;

		QueryBuilder s_prof = new QueryBuilder(_dsn, _table, _column,
				Rdbms_conn.getDBType());
		String all_query = s_prof.get_all_query();
		try {
			Rdbms_conn.openConn();
			ResultSet rs = Rdbms_conn.runQuery(all_query);
			while (rs.next()) {
				d = rs.getDouble("like_wise");
				if (rs.wasNull())
					continue;
				counter++;
				if (counter <= gc) {
					sum += d;
					if (counter != gc)
						continue;
				}
				double avg = sum / counter;
				sum = 0;
				counter = 0;
				vc.add(i++, new Double(avg));
			}
			rs.close();
			Rdbms_conn.closeConn();
		} catch (SQLException e) {
			counter = 0;
			ConsoleFrame.addText("\n Can not fill Cluster");
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		}
		// After loop breaks
		// Rounding off the values
		if (counter != 0 && Math.round((float) counter / gc) > 0) {
			double avg = sum / counter;
			vc.add(i, new Double(avg));
		}

		return vc;
	}

	public void actionPerformed(ActionEvent event) {
		String click_val = ((JButton) event.getSource()).getText();

		if (click_val.equalsIgnoreCase("Chart")) {
			try { // for resetting cursor
				int size = 0;
				this.getTopLevelAncestor()
						.setCursor(
								java.awt.Cursor
										.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				Vector vc = fillValues();
				if (vc == null || (size = vc.size()) == 0) {
					JOptionPane.showMessageDialog(null, "No Data fetched",
							"Error Message", JOptionPane.ERROR_MESSAGE);
					return;
				}
				;

				s_a = false;
				g_p.setBInit(vc);
				g_p.setZoomFactor(1);
				g_p.setColorIndex(getColorIndex());

				// Set the Sliderbar
				sm_min.setMinimum(((Double) vc.elementAt(0)).intValue() - 1);
				sm_min.setMaximum(((Double) vc.elementAt(size - 1)).intValue() + 1);
				sm_min.setValue(((Double) vc.elementAt(0)).intValue() - 1);

				sm_max.setMinimum(((Double) vc.elementAt(0)).intValue() - 1);
				sm_max.setMaximum(((Double) vc.elementAt(size - 1)).intValue() + 1);
				sm_max.setValue(((Double) vc.elementAt(size - 1)).intValue() + 1);

				// Turn on labels at major tick marks.

				if ((minS.getMaximum() - minS.getMinimum()) <= 10) {
					Hashtable l_t = minS.createStandardLabels(1);
					minS.setLabelTable(l_t);
					maxS.setLabelTable(l_t);

				} else {
					Hashtable l_t = minS.createStandardLabels((sm_min
							.getMaximum() - sm_min.getMinimum()) / 10);
					minS.setLabelTable(l_t);
					maxS.setLabelTable(l_t);
				}
				minS.setPaintLabels(true);
				maxS.setPaintLabels(true);

				g_p.showBubbleChart();
				s_a = true;

			} finally {
				this.getTopLevelAncestor()
						.setCursor(
								java.awt.Cursor
										.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));

			}
		} else if (click_val.equalsIgnoreCase("Zoom In")) {
			g_p.setZoomFactor(g_p.getZoomFactor() / 1.5);
			g_p.showBubbleChart();
		} else if (click_val.equalsIgnoreCase("Zoom Out")) {
			g_p.setZoomFactor(g_p.getZoomFactor() * 1.5);
			g_p.showBubbleChart();
		} else if (click_val.equalsIgnoreCase("Reset")) {
			g_p.setZoomFactor(1);
			g_p.showBubbleChart();
			// Set the Sliderbar
			sm_min.setValue(sm_min.getMinimum());
			sm_max.setValue(sm_max.getMaximum());

		}
	}

	private class MyCellRenderer extends JLabel implements ListCellRenderer {
		public MyCellRenderer() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel l = new JLabel(value.toString());
			l.setOpaque(true);
			l.setBackground(Color.white);
			switch (index) {
			case 0:
				l.setForeground(Color.red);
				break;
			case 1:
				l.setForeground(Color.green);
				break;
			case 2:
				l.setForeground(Color.yellow);
				break;
			default:
				l.setForeground(Color.blue);
				break;
			}
			if (isSelected == true)
				l.setBorder(BorderFactory.createLineBorder(Color.black));

			return l;
		}
	}

	public int getColorIndex() {
		return color_index;
	}

	private JPanel createSpane() {
		JPanel botP = new JPanel();
		SpringLayout layout = new SpringLayout();
		botP.setLayout(layout);
		// Create Border for center pane
		EmptyBorder emptyBorder = new EmptyBorder(10, 10, 0, 5);
		BevelBorder bevelBorder = new BevelBorder(BevelBorder.LOWERED);
		CompoundBorder compoundBorder = new CompoundBorder(emptyBorder,
				bevelBorder);
		botP.setBorder(new CompoundBorder(compoundBorder, emptyBorder));
		botP.setPreferredSize(new Dimension(200, 250));

		JLabel l_l = new JLabel("Lower");
		JLabel u_l = new JLabel("Upper");

		sm_min = new DefaultBoundedRangeModel();
		sm_max = new DefaultBoundedRangeModel();
		sm_min.addChangeListener(this);
		sm_max.addChangeListener(this);
		minS = new JSlider(sm_min);
		maxS = new JSlider(sm_max);
		minS.setOrientation(JSlider.VERTICAL);
		maxS.setOrientation(JSlider.VERTICAL);
		botP.add(minS);
		botP.add(l_l);
		botP.add(maxS);
		botP.add(u_l);

		layout.putConstraint(SpringLayout.WEST, l_l, 3, SpringLayout.WEST, botP);
		layout.putConstraint(SpringLayout.EAST, u_l, -50, SpringLayout.EAST,
				botP);
		layout.putConstraint(SpringLayout.WEST, minS, 0, SpringLayout.WEST, l_l);
		layout.putConstraint(SpringLayout.WEST, maxS, 0, SpringLayout.WEST, u_l);

		layout.putConstraint(SpringLayout.NORTH, minS, 3, SpringLayout.SOUTH,
				l_l);
		layout.putConstraint(SpringLayout.NORTH, maxS, 3, SpringLayout.SOUTH,
				u_l);
		layout.putConstraint(SpringLayout.NORTH, l_l, 3, SpringLayout.NORTH,
				botP);
		layout.putConstraint(SpringLayout.NORTH, u_l, 3, SpringLayout.NORTH,
				botP);
		return botP;
	}

	public void stateChanged(ChangeEvent e) {
		if (g_p.getBInit() == false)
			return; // Activated only after chart
		if (s_a == false)
			return;
		DefaultBoundedRangeModel source = (DefaultBoundedRangeModel) e
				.getSource();
		if (source.equals(sm_min) && source.getValueIsAdjusting()) {
			if (sm_max.getValue() < (sm_min.getValue() + 1))
				sm_max.setValue(sm_min.getValue() + 1);
		}
		if (source.equals(sm_max) && source.getValueIsAdjusting()) {
			if (sm_min.getValue() > (sm_max.getValue() - 1))
				sm_min.setValue(sm_max.getValue() - 1);
		}

		g_p.setSInit(new Integer(sm_min.getValue()).doubleValue(), new Integer(
				sm_max.getValue()).doubleValue());
		g_p.showSlideBubbleChart();
	}

	public void setSlideBar() {
		// Set the Sliderbar
		sm_min.setValue(sm_min.getMinimum());
		sm_max.setValue(sm_max.getMaximum());
	}
}