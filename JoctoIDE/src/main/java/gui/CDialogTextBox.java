package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class CDialogTextBox extends Dialog {

	protected Object result;
	protected Shell shlText;
	private Text mText;
	
	public String mString;
	
	public String getString() { return mString; }
	public void setString(String str) { mString = str; }

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CDialogTextBox(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlText.open();
		shlText.layout();
		mText.setText(mString);
		Display display = getParent().getDisplay();
		while (!shlText.isDisposed()) {
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
		shlText = new Shell(getParent(), getStyle());
		shlText.setSize(450, 269);
		shlText.setText("Text");
		
		mText = new Text(shlText, SWT.BORDER | SWT.MULTI);
		mText.setBounds(10, 10, 419, 179);
		
		Button btnOk = new Button(shlText, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mString = mText.getText();
				shlText.close();
			}
		});
		btnOk.setBounds(348, 195, 75, 25);
		btnOk.setText("OK");
		
		Button btnCancel = new Button(shlText, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlText.close();
			}
		});
		btnCancel.setBounds(10, 195, 75, 25);
		btnCancel.setText("Cancel");

	}

}
