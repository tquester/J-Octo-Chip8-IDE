package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import disass.COpcodeTable;
import disass.COpcodeTable.Chip8Opcode;

import java.util.TreeSet;

import javax.swing.text.TableView.TableCell;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;

public class CDialogOpcodes extends Dialog {

	protected Object result;
	protected Shell shlOpcodes;
	private Table mTable;
	private Combo mComboFilter;
	private COpcodeTable mOpcodeTable = new COpcodeTable();
	private boolean comboUpdate;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CDialogOpcodes(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlOpcodes.open();
		shlOpcodes.layout();
		comboUpdate = true;
		mComboFilter.setText("*");
		comboUpdate = false;
		TableColumn column = new TableColumn(mTable, SWT.NONE);

		column.setText("Opcode");
		column = new TableColumn(mTable, SWT.NONE);
		column.setText("Chipper");
		column = new TableColumn(mTable, SWT.NONE);
		column.setText("Octo");
		column = new TableColumn(mTable, SWT.NONE);
		column.setText("Description");
		column = new TableColumn(mTable, SWT.NONE);
		column.setText("Plattform");
		
		fillTable();
		Display display = getParent().getDisplay();
		while (!shlOpcodes.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private void fillTable() {
		if (comboUpdate) return;
		TreeSet<String> filter = new TreeSet<>();
		String strFilter = mComboFilter.getText();
		mTable.removeAll();
		shlOpcodes.setText(strFilter);
		for (Chip8Opcode opcode: mOpcodeTable.mOpcodes) {
			for (String str: opcode.plattforms) {
				filter.add(str);
			}
			if (strFilter.compareTo("*") == 0 || opcode.plattforms.contains(strFilter)) {
				TableItem cell = new TableItem(mTable, SWT.NONE);
				cell.setText(0, opcode.opcodePattern);
				cell.setText(1, opcode.chipper != null ? opcode.chipper : "");
				cell.setText(2,opcode.octo != null ? opcode.octo : "");
				cell.setText(3,opcode.getDescription());
				cell.setText(4,opcode.getPlattforms());
			}
			
		}
		TableColumn columns[] = mTable.getColumns();
		for (int i = 0; i < columns.length; i++) {
			mTable.getColumn(i).pack();
		}
		comboUpdate = true;
		mComboFilter.removeAll();
		mComboFilter.add("*");
		for (String str: filter) {
			mComboFilter.add(str);
		}
		//mComboFilter.setText(strFilter);
		comboUpdate = false;

		
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlOpcodes = new Shell(getParent(), getStyle());
		shlOpcodes.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				if (mTable == null) return;
				Rectangle bounds = shlOpcodes.getBounds();
				Rectangle boundsTable = mTable.getBounds();
				mTable.setBounds(0,boundsTable.y, bounds.width-20, bounds.height-40-boundsTable.y);
			}
		});
		shlOpcodes.setSize(700, 596);
		shlOpcodes.setText("Opcodes");
		
		mTable = new Table(shlOpcodes, SWT.BORDER | SWT.FULL_SELECTION);
		mTable.setBounds(0, 39, 661, 513);
		mTable.setHeaderVisible(true);
		mTable.setLinesVisible(true);
		
		Label lblFilter = new Label(shlOpcodes, SWT.NONE);
		lblFilter.setBounds(0, 10, 41, 15);
		lblFilter.setText("Filter");
		
		mComboFilter = new Combo(shlOpcodes, SWT.NONE);
		mComboFilter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fillTable();
			}
		});
		mComboFilter.setBounds(49, 7, 354, 23);

	}
}
