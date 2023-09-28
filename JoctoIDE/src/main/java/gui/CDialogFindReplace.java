package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

public class CDialogFindReplace extends Dialog {

	protected Object result;
	protected Shell shell;
	private Text mTextFind;
	private Text mTextReplace;
	public CSearchReplace mSerachReplace;
	public StyledText mTextSource;
	Button btnCheckIgnoreCase;
	Button btnWords;
	public CDialogIDE mDialogIDE;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CDialogFindReplace(Shell parent, int style) {
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
		shell.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println("Key Released "+e.keyCode);
			}
		});
		shell.setSize(450, 259);
		shell.setText(getText());
		
		Label lblSearch = new Label(shell, SWT.NONE);
		lblSearch.setBounds(10, 10, 55, 15);
		lblSearch.setText("Search");
		
		mTextFind = new Text(shell, SWT.BORDER);
		mTextFind.setBounds(10, 31, 401, 21);
		
		Label lblReplace = new Label(shell, SWT.NONE);
		lblReplace.setBounds(10, 72, 55, 15);
		lblReplace.setText("Replace");
		
		mTextReplace = new Text(shell, SWT.BORDER);
		mTextReplace.setBounds(10, 93, 401, 21);
		
		btnCheckIgnoreCase = new Button(shell, SWT.CHECK);
		btnCheckIgnoreCase.setBounds(10, 133, 93, 16);
		btnCheckIgnoreCase.setText("Ignore case");
		
		btnWords = new Button(shell, SWT.CHECK);
		btnWords.setBounds(136, 133, 93, 16);
		btnWords.setText("words");
		
		Button btnFind = new Button(shell, SWT.NONE);

		btnFind.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onFind();
			}
		});
		btnFind.setBounds(10, 175, 75, 25);
		btnFind.setText("&Find");
		
		Button btnReplace = new Button(shell, SWT.NONE);
		btnReplace.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onReplace();
			}
		});
		btnReplace.setBounds(249, 175, 75, 25);
		btnReplace.setText("Replace");
		
		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onReplaceAll();
			}
		});
		btnNewButton.setBounds(330, 175, 75, 25);
		btnNewButton.setText("Replace all");
		
		Button btnFindNext = new Button(shell, SWT.NONE);
		btnFindNext.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onFindNext();
			}
		});
		btnFindNext.setBounds(90, 175, 75, 25);
		btnFindNext.setText("Find next");

	}

	protected void onReplaceAll() {
		// TODO Auto-generated method stub
		
	}

	protected void onReplace() {
		// TODO Auto-generated method stub
		
	}

	protected void onFindNext() {
		try {
		String str = mTextFind.getText();
		boolean word = btnWords.getSelection();
		boolean ignore = btnCheckIgnoreCase.getSelection();
		int pos = mSerachReplace.find(str, word, ignore);
		if (pos != -1) {
			mDialogIDE.getTextSource().setSelection(pos, pos+str.length());
		}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}

	protected void onFind() {
		try {
		String str = mTextFind.getText();
		mSerachReplace.wordParaser.pos = 0;
		boolean word = btnWords.getSelection();
		boolean ignore = btnCheckIgnoreCase.getSelection();
		mSerachReplace.start(mDialogIDE.getTextSource().getText());
		int pos = mSerachReplace.find(str, word, ignore);
		if (pos != -1) {
			mDialogIDE.getTextSource().setSelection(pos, pos+str.length());
		}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
}
