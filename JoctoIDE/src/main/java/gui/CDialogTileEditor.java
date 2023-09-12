package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;

public class CDialogTileEditor extends Dialog {

	protected Object result;
	protected Shell shell;
	private Text text;
	private Text text_1;
	private Text text_2;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CDialogTileEditor(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(970, 594);
		shell.setText(getText());
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBounds(10, 0, 942, 43);
		
		Label lblWidth = new Label(composite, SWT.NONE);
		lblWidth.setBounds(10, 14, 40, 15);
		lblWidth.setText("Width");
		
		text = new Text(composite, SWT.BORDER);
		text.setText("16");
		text.setBounds(56, 10, 24, 21);
		
		Label lblHeight = new Label(composite, SWT.NONE);
		lblHeight.setBounds(86, 14, 40, 15);
		lblHeight.setText("Height");
		
		text_1 = new Text(composite, SWT.BORDER);
		text_1.setText("8");
		text_1.setBounds(132, 10, 24, 21);
		
		Label lblTiles = new Label(composite, SWT.NONE);
		lblTiles.setBounds(162, 14, 35, 15);
		lblTiles.setText("Tiles");
		
		Label lblSprites = new Label(composite, SWT.NONE);
		lblSprites.setBounds(203, 14, 40, 15);
		lblSprites.setText("Sprites");
		
		Combo combo = new Combo(composite, SWT.NONE);
		combo.setBounds(257, 11, 55, 23);
		
		Label lblSpriteSet = new Label(composite, SWT.NONE);
		lblSpriteSet.setBounds(334, 14, 55, 15);
		lblSpriteSet.setText("Sprite set");
		
		Combo combo_1 = new Combo(composite, SWT.NONE);
		combo_1.setBounds(395, 10, 132, 23);
		
		text_2 = new Text(shell, SWT.BORDER | SWT.MULTI);
		text_2.setBounds(10, 387, 942, 167);

	}
}
