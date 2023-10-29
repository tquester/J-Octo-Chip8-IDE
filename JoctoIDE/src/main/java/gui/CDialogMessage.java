package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class CDialogMessage extends Dialog {

	protected Object result;
	protected Shell shlMessage;
	Label lblNewLabel;
	public String text;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CDialogMessage(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlMessage.open();
		shlMessage.layout();
		Display display = getParent().getDisplay();
		while (!shlMessage.isDisposed()) {
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
		shlMessage = new Shell(getParent(), getStyle());
		shlMessage.setSize(340, 245);
		shlMessage.setText("Message");
		
		Button btnOk = new Button(shlMessage, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlMessage.close();
			}
		});
		btnOk.setBounds(125, 177, 75, 25);
		btnOk.setText("OK");
		
		lblNewLabel = new Label(shlMessage, SWT.WRAP);
		lblNewLabel.setBounds(10, 10, 307, 145);
		lblNewLabel.setText(text);

	}
}
