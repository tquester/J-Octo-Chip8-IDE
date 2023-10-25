package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.wb.swt.SWTResourceManager;

import disass.C8DisassEmitter;
import disass.C8DisassEmitterCowgod;
import disass.C8Emitter;
import disass.C8EmitterZ80;
import disass.C8LabelType;
import disass.C8TableEmitter;
import disass.CC8Decoder;
import disass.CC8Label;
import disass.COpcodeTable.Dialect;
import emulator.C8DebugSource;
import ide.CCallback;
import ide.CMainMenus;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;

public class CDialogDisassembler extends Dialog {

	protected Object result;
	protected Shell shlChipDisassembler;
	private Text mText;
	private Text mTextLabelName;
	private Text mTextItemsPerRow;
	private Text mTextAlphabet;
	Combo mComboLabelTyp;
	Combo mComboCode;
	List mListLabels;
	CC8Decoder mDecoder = new CC8Decoder();
	C8DisassEmitter mDisassEmiter = new C8DisassEmitter();
	C8DisassEmitter mAssemblerEmiter = new C8DisassEmitter();
	C8DisassEmitterCowgod mDisassEmiterCowgod = new C8DisassEmitterCowgod();
	C8DisassEmitterCowgod mAssemblerEmiterCowgod = new C8DisassEmitterCowgod();
	C8EmitterZ80 mZ80Emitter = new C8EmitterZ80();
	C8TableEmitter mTableEmitterOcto = new C8TableEmitter();
	C8TableEmitter mTableEmitterOctoDis = new C8TableEmitter();
	C8TableEmitter mTableEmitterChipper = new C8TableEmitter();
	C8TableEmitter mTableEmitterChipperDis = new C8TableEmitter();
	TreeMap<String, C8Emitter> mMapEmitters = new TreeMap<>();
	TreeMap<String, C8LabelType> mMapString2LabelFormat = new TreeMap<>();
	TreeMap<C8LabelType, String> mMapLabelFormat2String = new TreeMap<>();
	private String mChip8Filename;
	private TreeSet<String> mSetNames;
	private CC8Label mSelectedLabel;
	private Label mLblAddress;
	Composite mCompositeRight;
	private Text mTextFind;
	private String mFindText;
	private int mFindPos;
	private Menu menuBar;
	private MenuItem fileMenuHeader;
	private Menu fileMenu;
	private MenuItem fileSaveItem;
	public byte[] memory = null;
	CMainMenus mMainMenus;
	public int codesize;
	public C8DebugSource mDebugSource=null;
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CDialogDisassembler(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlChipDisassembler.open();
		fillCombos();
		shlChipDisassembler.layout();
		createMenu();
		if (memory != null) 
			disassMemory(memory);
		Display display = getParent().getDisplay();
		while (!shlChipDisassembler.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	
	void addLabelFormat(String name, C8LabelType type) {
		mComboLabelTyp.add(name);
		mMapLabelFormat2String.put(type, name);
		mMapString2LabelFormat.put(name, type);
	}

	private void fillCombos() {
		
		mDisassEmiter.disassFormat = true;
		mDisassEmiter.hexadecimal = true;
		mAssemblerEmiter.disassFormat = false;
		mAssemblerEmiter.hexadecimal = false;
		mDisassEmiterCowgod.disassFormat = true;
		mDisassEmiterCowgod.hexadecimal = true;
		mAssemblerEmiterCowgod.disassFormat = false;
		mAssemblerEmiterCowgod.hexadecimal = false;
		
		mTableEmitterOcto.mDialect = Dialect.OCTO;
		mTableEmitterOctoDis.mDialect = Dialect.OCTO;
		mTableEmitterChipper.mDialect = Dialect.CHIPPER;
		mTableEmitterChipperDis.mDialect = Dialect.CHIPPER;
		mTableEmitterOctoDis.disassFormat = true;
		mTableEmitterChipper.disassFormat = true;
		

		
		mMapEmitters.put("Chipper Disassembly", mDisassEmiterCowgod);
		mMapEmitters.put("Chipper Assembly", mAssemblerEmiterCowgod);
		mMapEmitters.put("Octo Disassembly", mDisassEmiter);
		mMapEmitters.put("Octo Assembly", mAssemblerEmiter);

		mMapEmitters.put("Octo Table Assembly", mTableEmitterOcto);
		mMapEmitters.put("Octo Table Disassembly", mTableEmitterOctoDis);
		mMapEmitters.put("Chipper Table Assembly", mTableEmitterChipper);
		mMapEmitters.put("Chipper Table Disassembly", mTableEmitterChipperDis);
		
		mMapEmitters.put("Z80 Assembly", mZ80Emitter);
		String text = null;
		for (String key: mMapEmitters.keySet()) {
			if (text == null) text = key;
			mComboCode.add(key);
		}
		mComboCode.setText(text);
		
		addLabelFormat("Code", C8LabelType.CODE);
		addLabelFormat("Data (undef)",C8LabelType.DATA);
		addLabelFormat("Sprite 8 Pixel",C8LabelType.SPRITE8);
		addLabelFormat("Sprite 16 Pixel", C8LabelType.SPRITE16);
		addLabelFormat("ASCII",C8LabelType.ASCII);
		addLabelFormat("Text", C8LabelType.LETTERS);
		addLabelFormat("Hex", C8LabelType.HEX);
		addLabelFormat("Skip", C8LabelType.SKIP);
		
		
		
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		
		
		
		shlChipDisassembler = new Shell(getParent(), getStyle());
		//setParent();
		//
		shlChipDisassembler.setLayout(null);
		shlChipDisassembler.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				onResize();
			}
		});
		shlChipDisassembler.setSize(994, 718);
		shlChipDisassembler.setText("Chip8 Disassembler");
		
		mText = new Text(shlChipDisassembler, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		mText.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.NORMAL));
		mText.setBounds(10, 53, 646, 591);
		
		mCompositeRight = new Composite(shlChipDisassembler, SWT.NONE);
		mCompositeRight.setBounds(662, 10, 300, 634);
		
		mListLabels = new List(mCompositeRight, SWT.BORDER | SWT.V_SCROLL);
		mListLabels.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onDisplayLabel();
			}
		});
		mListLabels.setBounds(10, 96, 255, 179);
		
		Label lblLabels = new Label(mCompositeRight, SWT.NONE);
		lblLabels.setBounds(10, 70, 55, 22);
		lblLabels.setText("Labels");
		
		Label lblName = new Label(mCompositeRight, SWT.NONE);
		lblName.setBounds(10, 281, 47, 23);
		lblName.setText("Name");
		
		mTextLabelName = new Text(mCompositeRight, SWT.BORDER);
		mTextLabelName.setBounds(10, 306, 255, 28);
		
		Label lblTyp = new Label(mCompositeRight, SWT.NONE);
		lblTyp.setBounds(10, 342, 55, 23);
		lblTyp.setText("Typ");
		
		mComboLabelTyp = new Combo(mCompositeRight, SWT.NONE);
		mComboLabelTyp.setBounds(10, 369, 255, 23);
		
		Label lblItemsPerRow = new Label(mCompositeRight, SWT.NONE);
		lblItemsPerRow.setBounds(10, 409, 100, 28);
		lblItemsPerRow.setText("items per row");
		
		mTextItemsPerRow = new Text(mCompositeRight, SWT.BORDER);
		mTextItemsPerRow.setText("1");
		mTextItemsPerRow.setBounds(116, 406, 149, 33);
		
		Label lblAlphabet = new Label(mCompositeRight, SWT.NONE);
		lblAlphabet.setBounds(10, 440, 75, 23);
		lblAlphabet.setText("Alphabet");
		
		mTextAlphabet = new Text(mCompositeRight, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		mTextAlphabet.setBounds(10, 467, 255, 78);
		
		Button btnOpenChip = new Button(mCompositeRight, SWT.NONE);
		btnOpenChip.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onOpenChip8();
			}
		});
		btnOpenChip.setBounds(10, 599, 100, 33);
		btnOpenChip.setText("Open chip8");
		
		Button btnSaveText = new Button(mCompositeRight, SWT.NONE);
		btnSaveText.setBounds(175, 599, 100, 33);
		btnSaveText.setText("Save Text");
		
		Button btnSave = new Button(mCompositeRight, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSave();
			}
		});
		btnSave.setBounds(175, 560, 100, 33);
		btnSave.setText("Save");
		
		Label lblCode = new Label(mCompositeRight, SWT.NONE);
		lblCode.setBounds(10, 0, 55, 28);
		lblCode.setText("Code");
		
		mComboCode = new Combo(mCompositeRight, SWT.NONE);
		mComboCode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				disass();
			}
		});
		mComboCode.setBounds(10, 35, 255, 23);
		
		mLblAddress = new Label(mCompositeRight, SWT.NONE);
		mLblAddress.setBounds(76, 281, 189, 23);
		mLblAddress.setText("$0000");
		
		Composite composite = new Composite(shlChipDisassembler, SWT.NONE);
		composite.setBounds(10, 10, 646, 38);
		
		mTextFind = new Text(composite, SWT.BORDER);
		mTextFind.setBounds(10, 4, 432, 28);
		
		Button button = new Button(composite, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onFindPrev();
			}
		});
		button.setBounds(540, 5, 45, 25);
		button.setText("<<");
		
		Button button_1 = new Button(composite, SWT.NONE);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onFindNext();
			}
		});
		button_1.setText(">>");
		button_1.setBounds(591, 5, 45, 25);
		
		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onFind();
			}
		});
		btnNewButton.setBounds(459, 4, 75, 25);
		btnNewButton.setText("Find");

	}
	
	void createMenu() {
		
		mMainMenus = new CMainMenus(shlChipDisassembler);
		mMainMenus.addMenu("&Tools")
			.add("&Sprite Editor", new CCallback() {
				@Override
				public void callback() {
					onSpriteEdit();
				}
			})
			.add("&Ide", new CCallback() {
				@Override
				public void callback() {
					onIde();
				}
			});
		
	}

	protected void onIde() {
		CDialogIDE dlg = new CDialogIDE(shlChipDisassembler, SWT.TITLE | SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.CLOSE);
		dlg.open();
		
	}

	protected void onSpriteEdit() {
		CDialogSpriteEditor dlg = new CDialogSpriteEditor(shlChipDisassembler, SWT.TITLE | SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.CLOSE);
		dlg.open();
		
	}

	protected void onFindPrev() {
		try {
			if (!mFindText.isEmpty()) {
				String text = mText.getText().toLowerCase();
				if (mFindPos == -1) mFindPos =text.length();
				int pos=-1;
				int prev=-1;
				while (true) {
					pos = text.indexOf(mFindText, pos+1);
					if (pos == -1) break;
					if (pos >= mFindPos) break;
					prev = pos;
				}
				mFindPos = prev;
				if (mFindPos != -1) {
					mText.setSelection(mFindPos, mFindPos + mFindText.length());
				}
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	protected void onFindNext() {
		try {
			if (!mFindText.isEmpty()) {
				if (mFindPos == -1) mFindPos = 0;
				String text = mText.getText().toLowerCase();
				mFindPos = text.indexOf(mFindText, mFindPos+1);
				if (mFindPos != -1) {
					mText.setSelection(mFindPos, mFindPos + mFindText.length());
				}
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	protected void onFind() {
		try {
			if (mFindPos == -1) return;
			mFindText = mTextFind.getText().toLowerCase();
			String text = mText.getText().toLowerCase();
			
			mFindPos = text.indexOf(mFindText, mFindPos+1);
			if (mFindPos != -1) {
				mText.setSelection(mFindPos, mFindPos + mFindText.length());
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		
		
	}

	private void setParent() {
		shlChipDisassembler = getParent();
		
	}

	protected void onResize() {
		if (mText == null) return;
		if (mCompositeRight == null) return;
		Rectangle rect = shlChipDisassembler.getBounds();
		Rectangle rectText = mText.getBounds();
		Rectangle rectComposite = mCompositeRight.getBounds();
		mText.setBounds(rectText.x, rectText.y, rect.width-rectComposite.width-50, rect.height-rectText.y-60);
		mCompositeRight.setBounds(rect.width-rectComposite.width-20, rectComposite.y, rectComposite.width, rect.height-60);;
		
		
	}

	protected void onSave() {
		if (mSelectedLabel == null) return;
		C8LabelType oldType = mSelectedLabel.mLabelType;
		CC8Label label = mDecoder.mLabels.get(mSelectedLabel.mTarget);
		mSelectedLabel.mName = mTextLabelName.getText();
		mSelectedLabel.mItemsPerRow = Integer.parseInt(mTextItemsPerRow.getText());
		mSelectedLabel.mLabelType = mMapString2LabelFormat.get(mComboLabelTyp.getText());
		mSelectedLabel.mAlphabet = mTextAlphabet.getText();
		if (mSelectedLabel.mLabelType == C8LabelType.CODE && oldType != C8LabelType.CODE) {
			int prev = 0;
			int adr3=0;
			for (Integer adr2:mDecoder.mLabels.keySet()) {
				if (prev == mSelectedLabel.mTarget) {
					adr3 = adr2.intValue();
					break;
				}
				prev = adr2.intValue();
			}
			mSelectedLabel.mEnd = adr3;
		}
		label.mAlphabet = mSelectedLabel.mAlphabet;
		label.mName = mSelectedLabel.mName;
		label.mItemsPerRow = mSelectedLabel.mItemsPerRow;
		label.mLabelType = mSelectedLabel.mLabelType;
				
		
		
		mDecoder.saveHints(mChip8Filename+".hints");
		Point sel = mText.getSelection();
	    disass();
	    mText.setSelection(sel);
	}

	protected void onDisplayLabel() {
		mSetNames = new TreeSet<String>();
		String selection[] = mListLabels.getSelection();
		if (selection.length != 1) return;
		String selectedLabel = selection[0];
		try {
			for (Integer adr: mDecoder.mLabels.keySet()) {
				
				CC8Label label = mDecoder.mLabels.get(adr);
				String text = label.toString();
				
				mSetNames.add(text);
				if (text.compareTo(selectedLabel) == 0) {
					String code = mText.getText();
					int pos = code.indexOf(text+":");
					if (pos != -1) 
						mText.setSelection(pos);
					mSelectedLabel = label;
					mLblAddress.setText(String.format("$%04x", label.mTarget));
					mTextLabelName.setText(text);
					mComboLabelTyp.setText(mMapLabelFormat2String.get(label.mLabelType));
					mTextAlphabet.setText(label.mAlphabet == null ? "" : label.mAlphabet);
					mTextItemsPerRow.setText(String.format("%d", label.mItemsPerRow));
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	protected void onOpenChip8() {
		String[] filterExt = { "*.??8", "*.ch8", "*.sc8" };
		FileDialog fd = new FileDialog(shlChipDisassembler, SWT.OPEN);
        fd.setText("Open chip8 program");
        fd.setFilterExtensions(filterExt);
        mChip8Filename = fd.open();	
        mDecoder.mLabels.clear();
        mDecoder.loadHints(mChip8Filename+".hints");
        disass();
        
	}
	
	public void addLabel(CC8Label lbl) {
		mDecoder.mLabels.put(lbl.mTarget, lbl);
		
	}


	private void disass() {
		try {
		String strEmitter = mComboCode.getText();
		C8Emitter emitter = mMapEmitters.get(strEmitter);
		if (mDebugSource != null) {
			emitter.mSourceHints = mDebugSource;
			emitter.replaceAlias = true;
		}
		if (emitter == null) 
			emitter = mDisassEmiter;
		mDecoder.emitter = emitter;
		if (memory == null)
			mDecoder.start(mChip8Filename, null);
		else
			mDecoder.start(memory, codesize);
		mText.setText(mDecoder.emitter.getText());
		mListLabels.removeAll();
		mSetNames = new TreeSet<>();
		for (Integer adr: mDecoder.mLabels.keySet()) {
			CC8Label label = mDecoder.mLabels.get(adr);
			mSetNames.add(label.toString());
			label.mTarget = adr.intValue();
		}
		for (String name: mSetNames) {
			mListLabels.add(name);
		}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	
		
		
	}
	
	private void disassMemory(byte[] memory) {
		try {
		String strEmitter = mComboCode.getText();
		C8Emitter emitter = mMapEmitters.get(strEmitter);
		if (emitter == null) 
			emitter = mDisassEmiter;
		
		if (mDebugSource != null) {
			emitter.mSourceHints = mDebugSource;
			emitter.commenAlias = true;
		}
		
		mDecoder.emitter = emitter;
		mDecoder.start(memory, codesize);
		mText.setText(mDecoder.emitter.getText());
		mListLabels.removeAll();
		mSetNames = new TreeSet<>();
		for (Integer adr: mDecoder.mLabels.keySet()) {
			CC8Label label = mDecoder.mLabels.get(adr);
			mSetNames.add(label.toString());
			label.mTarget = adr.intValue();
		}
		for (String name: mSetNames) {
			mListLabels.add(name);
		}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	
		
		
	}

	public void setDebugSource(C8DebugSource debugSource) {
		
		mDebugSource = debugSource;
		
	}

}
