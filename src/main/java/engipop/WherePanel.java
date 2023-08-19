package engipop;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import engipop.Node.WaveSpawnNode;

//manages where jtable + add/remove buttons
@SuppressWarnings("serial")
public class WherePanel extends EngiPanel {
	private DefaultTableModel whereModel = new DefaultTableModel(0, 1);
	private JTable whereTable = new JTable(whereModel);
	private static List<Object> whereList = new ArrayList<Object>();
	
	public WherePanel() {
		JButton addWhereRow = new JButton("+");
		JButton removeWhereRow = new JButton("-");
		JScrollPane whereScroll = new JScrollPane(whereTable);
		JPanel buttonPanel = new JPanel();
		
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		this.setOpaque(false);
		buttonPanel.setOpaque(false);
		
		whereTable.setTableHeader(null);
		removeWhereRow.setEnabled(false);
		
		buttonPanel.add(addWhereRow);
		buttonPanel.add(removeWhereRow);
		addWhereRow.setToolTipText("Add a row to the where list");
		removeWhereRow.setToolTipText("Remove the currently selected row from the where list");
		
		whereTable.getSelectionModel().addListSelectionListener(event -> {
			if(whereTable.getSelectedRowCount() == 1) {
				removeWhereRow.setEnabled(true);
			}
			else {
				removeWhereRow.setEnabled(false);
			}
		});
		addWhereRow.addActionListener(event -> {
			whereModel.addRow(new String[] {""});
		});
		removeWhereRow.addActionListener(event -> {
			whereModel.removeRow(whereTable.getSelectedRow());
		});
		
		for(Object where : whereList) {
			whereModel.addRow(new String[] {(String) where});
		}
		
		whereTable.setPreferredScrollableViewportSize(new Dimension (210, 50));
		whereScroll.setMinimumSize(whereTable.getPreferredScrollableViewportSize());
		
		addGB(whereScroll, 0, 0);
		addGB(buttonPanel, 0, 1);
	}
	
	public void updateWhere(List<Object> wheres) {
		
		List<Object> list = new ArrayList<Object>();
		list.addAll(wheres);
		
		whereTable.clearSelection();
		
		//select all the wheres already in model
		for(int i = 0; i < whereModel.getRowCount(); i++) {
			if(list.contains(whereModel.getValueAt(i, 0))) {
				whereTable.changeSelection(i, 1, true, false);
				list.remove(whereModel.getValueAt(i, 0));
			}
		}
		
		//then add new tags that weren't added
		for(Object newWhere : list) {
			whereModel.addRow(new String[] {(String) newWhere});
			whereTable.changeSelection(whereModel.getRowCount() - 1, 0, true, false);
		}
	}
	
	public List<String> updateNode() {
		List<String> wheres = new ArrayList<String>(4);
		for(int row : whereTable.getSelectedRows()) {
			wheres.add((String) whereTable.getValueAt(row, 0));
		}
		return wheres;
	}
	
	public void updateModel(List<String> list) {
		whereModel.setRowCount(0);
		
		for(String s : list) {
			whereModel.addRow(new String[] {s});
		}
	}
	
	public void clearSelection() {
		whereTable.clearSelection();
	}
}
