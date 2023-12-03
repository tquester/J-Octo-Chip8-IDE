package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;

import java.io.File;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.nebula.widgets.richtext.RichTextEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wb.swt.SWTResourceManager;

import assembler.CChip8Assembler;
import assembler.CChip8Assembler.CMemoryStatistic;
import assembler.CToken;
import assembler.CTokenizer;
import assembler.CTokens;
import assembler.CWordParser;
import assembler.Token;
import disass.CC8Label;
import disass.Tools;
import ide.CCallback;
import ide.CMainMenus;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.ModifyEvent;

public class CDialogIDE extends Dialog {

	public String mFolder;

	protected Object result;
	protected Shell shlJoctoIde;
	private String mFilename;
	private String mStrLabels = "";
	CTabFolder mTabFolder;
	Composite composite;
	List mListLabels;
	Label mLblStatus;
	Display display;
	int time = 1000;
	private Text mTextErrors;
	private CMainMenus mMainMenus;
	private List mListCompletion;
	private int mWordBegin;
	private int mWordEnd;
	Composite mBarFiles;
	Composite mBarErrors;
	Composite mBarLeftRight;
	OctoLineStyler mOctoLineStyler = new OctoLineStyler();
	Tree mListFiles;
	static final int constErrorSpaceBottom = 80;
	static final int key_back = 0x1000003;
	static final int state_mask_back = 0x10000;

	static final int key_forward = 0x1000003;
	static final int state_mask_forward = 0x10000;

	static final int key_hyperjump = 0x1000015;
	static final int state_mask_hyperjump = 0x00;

	static final int key_completion = 0x20;
	static final int state_mask_completion = 0x40000;

	Stack<Integer> mLineNumberStack = new Stack<>();
	private Stack<String> mStackUndo = new Stack<>();
	protected boolean mResizing = false;
	protected int mResizeBase;
	private boolean mEditorDirty = true;

	public StyledText getTextSource() {
		StyledText result = null;
		if (mTabFolder.getSelectionIndex() == -1)
			return null;
		CTabItem item = mTabFolder.getItems()[mTabFolder.getSelectionIndex()];
		result = (StyledText) item.getControl();
		return result;
	}

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public CDialogIDE(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {

		createContents();
		shlJoctoIde.open();
		shlJoctoIde.layout();
		createMenu();

		mFilename = "test.o8";
		if (mFolder == null)
			mFolder = ".";
		loadFilesTree();
		modifyTitle();
		autoload();
		display = getParent().getDisplay();
		display.timerExec(time, timer);

		while (!shlJoctoIde.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private void loadFilesTree() {
		mListFiles.removeAll();
		TreeItem newItem = new TreeItem(mListFiles, SWT.None);

		findFiles(mFolder, null);

	}

	private void findFiles(String strdir, TreeItem root) {
		File dir = new File(strdir.trim());
		File files[] = dir.listFiles();
		if (files == null) {
			dir = new File(strdir + "\\");
			files = dir.listFiles();
		}

		TreeItem fileItem;
		if (files == null)
			return;
		for (File file : files) {
			if (file.isDirectory()) {
				if (root == null)
					fileItem = new TreeItem(mListFiles, SWT.None);
				else
					fileItem = new TreeItem(root, SWT.None);
				fileItem.setText(file.getName());
				findFiles(file.getAbsolutePath(), fileItem);
			} else {

				if (root != null)
					fileItem = new TreeItem(root, SWT.None);
				else
					fileItem = new TreeItem(mListFiles, SWT.None);
				fileItem.setData(file.getAbsolutePath());
				fileItem.setText(file.getName());
			}
		}

	}

	private void modifyTitle() {
		shlJoctoIde.setText("J-Octo IDE -- " + mFilename);
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		Display display = getParent().getDisplay();
		shlJoctoIde = new Shell(display);

		// shlJoctoIde = new Shell(getParent(), getStyle());
		// shlJoctoIde = getParent();
		shlJoctoIde.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				if (mSplitError != 0) {
					if (mBarErrors != null) {
						Rectangle rect = shlJoctoIde.getBounds();
						Rectangle rectSplit = mBarErrors.getBounds();
						double v = rect.height;
						v *= mSplitError;
						// mBarErrors.setBounds(rectSplit.x,(int) v, rectSplit.width, rectSplit.height);
					}

				}
				onResize();
			}
		});

		shlJoctoIde.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE) {
					e.doit = false;
				}
			}
		});
//		shlJoctoIde = getParent();
		shlJoctoIde.setSize(954, 589);
		shlJoctoIde.setText("J-octo IDE");

		composite = new Composite(shlJoctoIde, SWT.NONE);
		composite.setBounds(10, 10, 905, 41);

		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSpriteEditor();
			}
		});
		btnNewButton.setBounds(583, 10, 100, 25);
		btnNewButton.setText("Sprite Editor");

		Button btnNewButton_1 = new Button(composite, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onTileEditor();
			}
		});
		btnNewButton_1.setBounds(689, 10, 100, 25);
		btnNewButton_1.setText("Tile Editor");

		Button btnDisassembler = new Button(composite, SWT.NONE);
		btnDisassembler.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onDisassembler();
			}
		});
		btnDisassembler.setBounds(795, 10, 100, 25);
		btnDisassembler.setText("Disassembler");

		Button btnOpen = new Button(composite, SWT.NONE);
		btnOpen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onOpenFile();
			}
		});
		btnOpen.setBounds(10, 10, 75, 25);
		btnOpen.setText("Open");

		Button btnSave = new Button(composite, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSaveFile();
			}
		});
		btnSave.setBounds(91, 10, 75, 25);
		btnSave.setText("Save");

		Button btnCompile = new Button(composite, SWT.NONE);
		btnCompile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onCompile();
			}
		});
		btnCompile.setBounds(172, 10, 75, 25);
		btnCompile.setText("Compile");

		Button btnEmulator = new Button(composite, SWT.NONE);
		btnEmulator.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onEmulator();
			}
		});
		btnEmulator.setBounds(477, 10, 100, 25);
		btnEmulator.setText("Emulator");

		Button btnCompileDisass = new Button(composite, SWT.NONE);
		btnCompileDisass.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onCompileDisass();
			}
		});
		btnCompileDisass.setText("Compile+Disass");
		btnCompileDisass.setBounds(253, 10, 140, 25);

		mListLabels = new List(shlJoctoIde, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		mListLabels.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onListLabelSelected(e);
			}
		});
		mListLabels.setBounds(10, 229, 159, 323);

		mTextErrors = new Text(shlJoctoIde, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		mTextErrors.setBounds(185, 414, 740, 103);

		mLblStatus = new Label(shlJoctoIde, SWT.NONE);
		mLblStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND));
		mLblStatus.setBounds(179, 523, 533, 20);
		mLblStatus.setText("status");

		mListFiles = new Tree(shlJoctoIde, SWT.BORDER);
		mListFiles.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		mListFiles.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onDoubleClickFileTree(e);
			}
		});
		mListFiles.setBounds(10, 57, 159, 152);

		mBarFiles = new Composite(shlJoctoIde, SWT.NONE);
		mBarFiles.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
		mBarFiles.setBounds(10, 215, 159, 8);

		mBarFiles.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				if (mResizing) {
					System.out.println(String.format("x=%d", e.x));
					Rectangle rect = mBarFiles.getBounds();
					mBarFiles.setBounds(rect.x, mResizeBase + e.y, rect.width, rect.height);
					onResize();
				}
			}
		});
		mBarFiles.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				Rectangle rect = mBarFiles.getBounds();
				mResizeBase = rect.y;
				mResizing = true;
			}

			@Override
			public void mouseUp(MouseEvent e) {
				mResizing = false;
			}
		});

		mBarErrors = new Composite(shlJoctoIde, SWT.NONE);
		mBarErrors.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
		mBarErrors.setBounds(175, 400, 707, 8);

		mBarErrors.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				if (mResizing) {
					System.out.println(String.format("x=%d", e.x));
					Rectangle rectErrors = mTextErrors.getBounds();
					Rectangle rectText = getTextSource().getBounds();
					Rectangle rectBarErrors = mBarErrors.getBounds();
					Rectangle rect = shlJoctoIde.getBounds();
					int top = mResizeBase + e.y;
					mTextErrors.setBounds( //
							rectErrors.x, //
							top, rect.width - rectErrors.x - 30, //
							rect.height - top - constErrorSpaceBottom);

					top -= 12;
					mBarErrors.setBounds(//
							rectErrors.x, //
							top, //
							rect.width - rectErrors.x, //
							rectBarErrors.height);

					top -= 8;
					getTextSource().setBounds(//
							rectErrors.x, //
							rectText.y, //
							rect.width - rectErrors.x - 30 - 70, //
							top - rectText.y);

					// onResize();
				}
			}
		});
		mBarErrors.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				Rectangle rect = mBarErrors.getBounds();
				mResizeBase = rect.y;
				mResizing = true;
			}

			@Override
			public void mouseUp(MouseEvent e) {
				mResizing = false;
				onResize();
			}
		});

		mBarLeftRight = new Composite(shlJoctoIde, SWT.NONE);
		mBarLeftRight.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
		mBarLeftRight.setBounds(175, 57, 5, 495);

		mTabFolder = new CTabFolder(shlJoctoIde, SWT.NONE);
		mTabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onTabSwitched(e);
			}
		});
		mTabFolder.setBounds(185, 88, 740, 337);
		/*
		 * mTabFolder.addMouseListener(new MouseListener() {
		 * 
		 * @Override public void mouseUp(MouseEvent arg0) { TabFolder curFolder =
		 * (TabFolder)arg0.widget; Point eventLocation = new Point(arg0.x, arg0.y);
		 * TabItem item = curFolder.getItem(eventLocation); if(item == null) return;
		 * 
		 * Image image = item.getImage();
		 * 
		 * // check if click is on image if( eventLocation.x >= item.getBounds().x +
		 * image.getBounds().x && eventLocation.x <= item.getBounds().x +
		 * image.getBounds().x + image.getBounds().width && eventLocation.y >=
		 * item.getBounds().y + image.getBounds().y && eventLocation.y <=
		 * item.getBounds().y + image.getBounds().y + image.getBounds().height) {
		 * System.out.println("Close tab"); item.dispose(); } else {
		 * System.out.println("Don't close tab"); } }
		 * 
		 * @Override public void mouseDown(MouseEvent arg0) { }
		 * 
		 * @Override public void mouseDoubleClick(MouseEvent arg0) { } });
		 */

		mBarLeftRight.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				if (mResizing) {
					System.out.println(String.format("x=%d", e.x));
					Rectangle rect = mBarLeftRight.getBounds();
					mBarLeftRight.setBounds(mResizeBase + e.x, rect.y, rect.width, rect.height);
					onResize();
				}
			}
		});
		mBarLeftRight.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				Rectangle rect = mBarLeftRight.getBounds();
				mResizeBase = rect.x;
				mResizing = true;
			}

			@Override
			public void mouseUp(MouseEvent e) {
				mResizing = false;
			}
		});

	}

	protected void onDoubleClickFileTree(SelectionEvent e) {
		TreeItem items[] = mListFiles.getSelection();
		boolean found = false;
		if (items.length == 1) {
			TreeItem sel = items[0];

			String fname = (String) sel.getData();
			if (fname != null) {
				for (CTabItem tabItem : mTabFolder.getItems()) {
					String tabFile = (String) tabItem.getData();
					if (tabFile != null) {
						if (tabFile.compareTo(fname) == 0) {
							found = true;
							mTabFolder.setSelection(tabItem);
						}
					}
					if (found)
						break;
				}
				if (!found)
					loadFile(fname);
			}

		}

	}

	protected void onTabSwitched(SelectionEvent e) {

		CTabItem item = (CTabItem) e.item;
		ActivateTab(item);

	}

	private void ActivateTab(CTabItem item) {
		if (item == null)
			return;
		if (item.getControl() == null)
			return;
		parseFile();

	}

	protected void onModifyText(ModifyEvent e) {
		mEditorDirty = true;
		StyledText ctext = getTextSource();
		if (ctext == null)
			return;
		String text = ctext.getText();
		saveUndo();
		if (mStackUndo.size() > 30)
			mStackUndo.remove(0);

	}

	protected void onCompileDisass() {
		CChip8Assembler assembler = new CChip8Assembler();
		assembler.mFolder = mFolder;
		assembler.assemble(getTextSource().getText(), mFilename);
		String errors = assembler.getErrors();
		mTextErrors.setText(errors);
		boolean ok = !assembler.mbError;
		if (ok) {
			CDialogDisassembler dlgEmu = new CDialogDisassembler(shlJoctoIde,
					SWT.TITLE + SWT.RESIZE + SWT.MIN + SWT.MAX);
			// dlgEmu.mDecoder.setMemory(assembler.getCode());
			// dlgEmu.mDecoder.setCodeSize(assembler.getCodeSize());
			dlgEmu.memory = assembler.getCode();
			dlgEmu.codesize = assembler.getCodeSize();
			for (CC8Label lbl : assembler.mLabels.values()) {
				dlgEmu.addLabel(lbl);
			}
			dlgEmu.setDebugSource(assembler.mDebugSource);
			dlgEmu.open();
		}

	}

	protected void onKeyReleasedEditor(KeyEvent e) {
		System.out.println(String.format("%x %x", e.keyCode, e.stateMask));
		if (e.stateMask == state_mask_completion && e.keyCode == key_completion)
			onEditAutocomplete();
		if (e.stateMask == state_mask_hyperjump && e.keyCode == key_hyperjump)
			onEditHyperjump();
		if (e.character == 13 && (e.stateMask & SWT.SHIFT) != 0) {
			if (mEditorDirty)
				onAutoformat();
		}
		if (e.stateMask == state_mask_forward) {
			if (e.keyCode == key_forward)
				onEditJumpForward();
			if (e.keyCode == key_back)
				onEditJumpBack();
		}

	}

	private void onEditJumpBack() {
		if (!mLineNumberStack.isEmpty()) {
			int pos = mLineNumberStack.pop();
			getTextSource().setSelection(pos, pos + 1);
			getTextSource().setCaretOffset(pos);
			;
		}

	}

	private void onEditJumpForward() {

	}

	private String wordUnderCursor(String text) {
		char c;
		int textPos = getTextSource().getCaretOffset();
		int begin = textPos - 1;
		while (begin > 0) {
			c = text.charAt(begin);
			if (Character.isSpace(c)) {
				begin++;
				break;
			}
			begin--;
		}
		int end = textPos;
		while (end < text.length()) {
			c = text.charAt(end);
			if (Character.isSpace(c)) {
				end--;
				break;
			}
			end++;
		}
		mWordBegin = begin;
		mWordEnd = end;
		if (end > begin)
			return text.substring(begin, end + 1);
		else
			return "";
	}

	private void onEditHyperjump() {
		String text = getTextSource().getText();
		String word = wordUnderCursor(text);
		CTokenizer tokenizer = new CTokenizer();
		CToken token = new CToken();
		tokenizer.start(text);
		while (tokenizer.hasData()) {
			tokenizer.getToken(token);
			if (token.token == Token.label) {
				if (token.literal.compareTo(word) == 0) {
					mLineNumberStack.push(getTextSource().getCaretOffset());
					getTextSource().setSelection(token.pos, token.pos);
					break;
				}
			}
		}

	}

	private void onEditAutocomplete() {
		String text = getTextSource().getText();
		String word = wordUnderCursor(text).toLowerCase();
		if (word == null)
			return;
		mListCompletion = new List(getTextSource(), SWT.BORDER);

		TreeSet<String> set = new TreeSet<>();
		CTokenizer tokenizer = new CTokenizer();
		CToken token = new CToken();
		tokenizer.start(text);
		while (tokenizer.hasData()) {
			token.literal = null;
			tokenizer.getToken(token);
			if (token.literal != null) {
				if (token.literal.toLowerCase().startsWith(word))
					set.add(token.literal);
			}
		}
		for (String str : set) {
			mListCompletion.add(str);
			Caret caret = getTextSource().getCaret();
			Point pt = caret.getLocation();
			mListCompletion.setBounds(pt.x, pt.y, 100, 150);
			mListCompletion.addKeyListener(new KeyListener() {

				@Override
				public void keyReleased(KeyEvent e) {
					if (e.keyCode == 13) {
						replaceSelection();
					}
				}

				@Override
				public void keyPressed(KeyEvent e) {
				}
			});

			mListCompletion.addMouseListener(new MouseListener() {

				@Override
				public void mouseUp(MouseEvent e) {
					replaceSelection();

				}

				@Override
				public void mouseDown(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseDoubleClick(MouseEvent e) {
					// TODO Auto-generated method stub

				}
			});
			mListCompletion.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// getTextSource().setSelection(mWordBegin, mWordEnd);
					// getTextSource().

					// mListCompletion.dispose();
				}
			});
			mListCompletion.setFocus();

		}

	}

	protected void replaceSelection() {
		if (mListCompletion == null)
			return;
		String[] sel = mListCompletion.getSelection();
		if (sel.length >= 1) {
			String newword = sel[0];
			getTextSource().replaceTextRange(mWordBegin, mWordEnd - mWordBegin + 1, newword);
		}
		mListCompletion.dispose();
		mListCompletion = null;

	}

	Runnable timer = new Runnable() {
		public void run() {
			if (getTextSource() != null) {

				int offset = getTextSource().getCaretOffset();
				String text = getTextSource().getText();
				int line = 1;
				int pos = 1;
				for (int i = 0; i < offset; i++) {
					char c = text.charAt(i);
					if (c == '\n') {
						pos = 1;
						line++;
					}
					pos++;

				}

				mLblStatus.setText(String.format("%d/%d", line, pos));
				if (mEditorDirty) {
					styleText(getTextSource().getText());
					parseFile();
					mEditorDirty = false;
				}
				display.timerExec(time, this);
			}

		}

	};
	private CSearchReplace mSerachReplace;
	private boolean mTextDirty;
	private boolean mUndoing = false;
	private double mSplitError = 0;

	private TreeMap<String, Integer> mLineNumbers = new TreeMap<>();

	protected void onEmulator() {
		CDialogEmulator dlg = new CDialogEmulator(shlJoctoIde, SWT.TITLE + SWT.RESIZE + SWT.MIN + SWT.MAX + SWT.CLOSE);
		dlg.open();

	}

	void createMenu() {

		mMainMenus = new CMainMenus(shlJoctoIde);
		mMainMenus.addMenu("&File").add("&New", new CCallback() {
			@Override
			public void callback() {
				onNewFile();
			}
		}).add("&Open", new CCallback() {
			@Override
			public void callback() {
				onOpenFile();
			}
		}).add("Open &Folder", new CCallback() {
			@Override
			public void callback() {
				onOpenFolder();
			}
		}).add("&Save", new CCallback() {
			@Override
			public void callback() {
				onSaveFile();
			}
		}).add("Save &As", new CCallback() {
			@Override
			public void callback() {
				onSaveAs();

			}
		}).add("&Options", new CCallback() {
			@Override
			public void callback() {
				OnOptions();

			}
		}).add("&Exit", new CCallback() {
			@Override
			public void callback() {

			}
		});

		mMainMenus.addMenu("&Edit").add("&Undo Ctrl-Z", SWT.MOD1 + 'Z', new CCallback() {
			@Override
			public void callback() {
				onUndo();
			}
		}).add("&Redo Ctrl-Y", SWT.MOD1 + 'Y', new CCallback() {
			@Override
			public void callback() {
				onRedo();
			}
		}).add("&Select All Ctrl-A", SWT.MOD1 + 'A', new CCallback() {
			@Override
			public void callback() {
				onSelectAll();
			}
		})

				.add("&Autoformat", 0, new CCallback() {
					@Override
					public void callback() {
						onAutoformat();
					}
				})

				.add("&Find CtrlF", SWT.MOD1 + 'F', new CCallback() {
					@Override
					public void callback() {
						onFind();
					}
				})

		;

		mMainMenus.addMenu("&Debugger").add("&Compile", new CCallback() {
			@Override
			public void callback() {
				onCompile();

			}
		}).add("&Run", new CCallback() {
			@Override
			public void callback() {

			}
		}).add("&Debug", new CCallback() {
			@Override
			public void callback() {

			}
		});

		mMainMenus.addMenu("&Tools").add("&Sprite Editor", new CCallback() {
			@Override
			public void callback() {

			}
		})
		.add("&Emulator", new CCallback() {
			@Override
			public void callback() {

			}
		})
		.add("&XOR Sprite Generator", new CCallback() {
			@Override
			public void callback() {
				CDialogXorSprite dlg = new CDialogXorSprite(shlJoctoIde, SWT.CLOSE | SWT.TITLE);
				dlg.open();
			}
		})
		.add("&Vector drawing", new CCallback() {
			@Override
			public void callback() {
				CDialogVectorDraw dlg = new CDialogVectorDraw(shlJoctoIde, SWT.CLOSE | SWT.TITLE | SWT.RESIZE);
				dlg.open();
			}
		})
		
		;

	}

	protected void onOpenFolder() {
		DirectoryDialog dialog = new DirectoryDialog(shlJoctoIde);
		String folder = dialog.open();
		if (folder != null) {
			mFolder = folder;
			loadFilesTree();
			setAutoload();
		}

	}

	protected void OnOptions() {
		// TODO Auto-generated method stub

	}

	protected void onAutoformat() {
		String text = getTextSource().getText();
		String[] lines = text.split("\n");
		int level = 0;
		Point selection = getTextSource().getSelection();
		int topIndex = getTextSource().getTopIndex();
		CTokens tokens = new CTokens();
		StringBuilder sb = new StringBuilder();
		CWordParser wordParser = new CWordParser();
		int iline = 0;
		int caret = getTextSource().getCaretOffset();
		int caretLine = 0;
		for (int pos = 0; pos < caret; pos++) {
			if (text.charAt(pos) == '\n')
				caretLine++;
		}
		for (String line : lines) {
			iline++;

			if (line.trim().length() == 0) {
				sb.append("\n");
				continue;
			}
			line = line.trim().replaceAll("\t", " ");
			if (line.charAt(0) == ';' || line.charAt(0) == '#') {
				sb.append(line + "\n");
				continue;
			}

			int lineLevel = level;
			wordParser.start(line);
			String word = wordParser.getWord();
			if (word.startsWith(":") || word.endsWith(":"))
				lineLevel = -1;
			int commentPos = -1;
			while (true) {
				word = word.toLowerCase();
				Token token = tokens.parse(word);
				if (word.startsWith(";") || word.startsWith("#")) {
					commentPos = wordParser.prevpos;
					break;
				}
				System.out.println("'" + word + "'");

				if (token != null) {
					switch (token) {
					case octobegin:
					case loop:
					case curlybracketopen:
						level++;
						break;
					case octoend:
					case again:
					case curlybracketclose:
						level--;
						lineLevel--;
						break;
					case octoelse:
						lineLevel--;
						break;
					}
				}
				if (!wordParser.hasData())
					break;
				word = wordParser.getWord();
			}

			String comment = "";
			if (commentPos != -1) {
				comment = line.substring(commentPos).trim();
				line = line.substring(0, commentPos).trim();
			}
			if (lineLevel >= 0)
				line = " ".repeat(12 + 2 * lineLevel) + line;
			if (commentPos != -1) {
				if (line.length() < 70)
					line = String.format("%-70s%s", line, comment);
				else
					line = String.format("%s\n%-70s%s", line, "", comment);
			}

			sb.append(line + "\n");
		}
		text = sb.toString();
		iline = 0;
		for (int pos = 0; pos < text.length(); pos++) {
			if (text.charAt(pos) == '\n') {
				iline++;
				if (iline == caretLine) {
					int i = pos + 1;
					while (i < text.length()) {
						if (text.charAt(i) == '\n') {
							caret = i;
							break;
						}
						i++;
					}
					break;
				}
			}
		}
		getTextSource().setText(sb.toString());
		parseFile();

		getTextSource().setSelection(selection);
		getTextSource().setTopIndex(topIndex);
		getTextSource().setCaretOffset(caret+1);

	}

	protected void onSelectAll() {
		// TODO Auto-generated method stub

	}

	protected void onRedo() {
		// TODO Auto-generated method stub

	}

	private void saveUndo() {
		if (!mUndoing) {
			String text = String.valueOf(getTextSource().getText());
			mStackUndo.push(text);
		}

	}

	protected void onUndo() {
		if (mStackUndo != null) {
			if (!mStackUndo.isEmpty()) {
				String text = mStackUndo.pop();
				mUndoing = true;
				Point selection = getTextSource().getSelection();
				getTextSource().setText(text);
				getTextSource().setSelection(selection);
				parseFile();

				mUndoing = false;
				mTextDirty = true;
			}
		}
	}

	protected void onFind() {
		mSerachReplace = new CSearchReplace();
		mSerachReplace.start(getTextSource().getText());
		CDialogFindReplace dlg = new CDialogFindReplace(shlJoctoIde, SWT.TITLE | SWT.CLOSE);
		dlg.mSerachReplace = mSerachReplace;
		dlg.mDialogIDE = this;
		dlg.open();

	}

	protected void onNewFile() {
		newFile();

	}

	protected void onTileEditor() {
		CDialogTileEditor dlg = new CDialogTileEditor(shlJoctoIde, getStyle());
		dlg.readSourcefile(getTextSource());
		dlg.open();

	}

	protected void onSpriteEditor() {
		CDialogSpriteEditor dlg = new CDialogSpriteEditor(shlJoctoIde, getStyle());
		dlg.readSourcefile(getTextSource());
		dlg.open();

	}

	protected void onDisassembler() {
		CDialogDisassembler dlg = new CDialogDisassembler(shlJoctoIde, SWT.TITLE + SWT.RESIZE + SWT.MIN + SWT.MAX);
		dlg.open();

	}

	protected void onCompile() {
		try {
			autosave();
			mFilename = getCurrentFilename();
			CChip8Assembler assembler = new CChip8Assembler();
			assembler.mFolder = mFolder;
			assembler.assemble(getTextSource().getText(), mFilename);
	
			String errors = assembler.getErrors();
			boolean ok = !assembler.mbError;
			errors += String.format("Code size = %d (%d remaining\n", assembler.getCodeSize(),
					4096 - 0x200 - assembler.getCodeSize());
			
			for (String fnname : assembler.mMapFunctionSize.keySet()) {
				Integer size = assembler.mMapFunctionSize.get(fnname);
				errors += String.format("function %s = %d bytes\n", fnname, size.intValue());
			}
			
			for (CMemoryStatistic stat : assembler.mMemoryStatistics) {
				errors += String.format("%s: %d (code: %d, data: %d)\n", stat.file, stat.sizeCode + stat.sizeData,
						stat.sizeCode, stat.sizeData);
			}
			mTextErrors.setText(errors);
			if (ok) {
				CDialogEmulator dlgEmu = new CDialogEmulator(shlJoctoIde, SWT.TITLE + SWT.RESIZE + SWT.MIN + SWT.MAX);
				dlgEmu.copyMemory(0x200, assembler.getCode(), assembler.getCodeSize());
				dlgEmu.mCPU.mDebugEntries = assembler.mDebugEntries;
				dlgEmu.startRunning = false;
				dlgEmu.mLabels = assembler.mLabels;
				dlgEmu.setDebugSource(assembler.mDebugSource);
				dlgEmu.open();
			}
		}
		catch(Exception ex) {
			mLblStatus.setText(ex.toString());
		
		}

	}

	private String getCurrentFilename() {
		CTabItem tabItem;
		int index = mTabFolder.getSelectionIndex();
		if (index == -1)
			return null;
		tabItem = mTabFolder.getItems()[index];
		String fname = (String) tabItem.getData();
		return fname.trim();
	}

	private void autosave() {
		String text = getTextSource().getText();
		Tools.writeTextFile("autosave.8o", text);

	}

	protected void onListLabelSelected(SelectionEvent e) {
		try {
			String[] selection = mListLabels.getSelection();
			if (selection.length == 1) {
				String label = selection[0];
				Integer line = mLineNumbers.get(label);
				if (line != null) {
					getTextSource().setCaretOffset(line.intValue());
					getTextSource().setSelection(line.intValue(), line.intValue() + label.length());
				}
			}

		} catch (Exception ex) {

		}

	}

	protected void onResize() {
		if (composite == null || mListLabels == null || getTextSource() == null || mTextErrors == null
				|| mLblStatus == null)
			return;
		Rectangle rect = shlJoctoIde.getBounds();
		Rectangle rectComposite = composite.getBounds();
		Rectangle rectList = mListLabels.getBounds();
		Rectangle rectText = mTabFolder.getBounds();
		Rectangle rectErrors = mTextErrors.getBounds();
		Rectangle rectStatus = mLblStatus.getBounds();
		Rectangle rectBarLeftRight = mBarLeftRight.getBounds();
		Rectangle rectBarFiles = mBarFiles.getBounds();
		Rectangle rectBarErrors = mBarErrors.getBounds();
		Rectangle RectListFiles = mListFiles.getBounds();

		int left = rectBarLeftRight.x + 5;
		int right = rectBarLeftRight.x + rectBarLeftRight.width + 5;
		int leftYSplit = rectBarFiles.y;
		int rightYSplit = rectBarErrors.y;

		composite.setBounds(//
				rectComposite.x, //
				rectComposite.y, //
				rect.width - 40, //
				rectComposite.height);

		mListFiles.setBounds(//
				RectListFiles.x, //
				RectListFiles.y, //
				left - RectListFiles.x - 10, //
				leftYSplit - RectListFiles.y - 10);

		mListLabels.setBounds(//
				rectList.x, //
				leftYSplit + 10, //
				left - rectList.x - 10, //
				rect.height - leftYSplit - 80);

		mLblStatus.setBounds(//
				rectStatus.x, //
				rect.height - 30 - 70, //
				rect.width - rectStatus.x, //
				30);
		mLblStatus.setVisible(true);

		int top = rect.height - rectErrors.height - constErrorSpaceBottom - rectStatus.height;
		mTextErrors.setBounds( //

				right, //
				top, //
				rect.width - right - 30, //
				rectErrors.height);

		top -= 8;

		mBarErrors.setBounds(//
				right, //
				top, //
				rect.width - right, //
				rectBarErrors.height);

		top -= 8;
		mTabFolder.setBounds(//
				right, //
				60, //
				rect.width - right - 30, //
				top - rectText.y);

		mBarLeftRight.setBounds(//
				rectBarLeftRight.x, //
				rectBarLeftRight.y, //
				rectBarLeftRight.width, //
				rect.height - rectBarLeftRight.y);

		mBarFiles.setBounds(//
				rectBarFiles.x, //
				rectBarFiles.y, //
				left - rectBarFiles.x, //
				rectBarFiles.height);

		double h = rect.height;
		double s = rectBarErrors.y;
		mSplitError = h / s;

	}

	protected void onSaveAs() {
		String[] filterExt = { "*.8o" };
		FileDialog fd = new FileDialog(shlJoctoIde, SWT.SAVE);
		fd.setText("Save chip8 program");
		fd.setFilterExtensions(filterExt);
		mFilename = fd.open();
		if (mFilename == null)
			return;
		CTabItem item = mTabFolder.getItems()[mTabFolder.getSelectionIndex()];
		File file = new File(mFilename);
		item.setData(file.getAbsolutePath());
		item.setText(file.getName());
		Tools.saveTextFile(getTextSource().getText(), mFilename);
		shlJoctoIde.setText("J-Octo IDE " + mFilename);
		setAutoload();

	}

	protected void onSaveFile() {
		try {
			CTabItem titem = mTabFolder.getItems()[mTabFolder.getSelectionIndex()];
			String filename = getCurrentFilename();
			StyledText stext = (StyledText) titem.getControl();

			if (filename == null) {
				String[] filterExt = { "*.8o" };
				FileDialog fd = new FileDialog(shlJoctoIde, SWT.SAVE);
				fd.setText("Save chip8 program");
				fd.setFilterExtensions(filterExt);
				mFilename = fd.open();
				if (mFilename == null)
					return;

			}
			String text = stext.getText();
			Tools.saveTextFile(text, filename);
			setAutoload();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void setAutoload() {
		StringBuilder sb = new StringBuilder();
		sb.append("folder=" + mFolder + "\n");
		for (CTabItem item : mTabFolder.getItems()) {
			String fname = (String) item.getData();
			if (fname != null)
				sb.append("file=" + fname + "\n");
		}

		Tools.writeTextFile("autoload.inf", sb.toString());
	}

	String getAutoload() {
		String autoload = Tools.loadTextFile("autoload.inf");
		return autoload;
	}

	void autoload() {
		String text = getAutoload();
		String lines[] = text.split("\n");
		for (String line : lines) {
			int p = line.indexOf('=');
			if (p != -1) {
				String name = line.substring(0, p);
				String fname = line.substring(p + 1);
				if (name.compareTo("folder") == 0) {
					mFolder = fname;
					loadFilesTree();
				} else {
					loadFile(fname);
				}
			} else
				loadFile(line);
		}
	}

	void loadFile(String fname) {
		String strSource = Tools.loadTextFile(fname.trim());
		if (strSource == null)
			return;
		File file = new File(fname);
		CTabItem item = new CTabItem(mTabFolder, SWT.CLOSE);
		mTabFolder.setSelection(item);
		// item.setImage(new
		// Image(shlJoctoIde.getDisplay(),"/JoctoIDE/src/main/java/gui/close.bmp"));
		item.setData(file.getAbsolutePath());
		item.setText(file.getName());
		StyledText text = createStyledText();
		item.setControl(text);
		StyledText stext = (StyledText) item.getControl();
		stext.setText(strSource);
	}

	void newFile() {
		CTabItem item = new CTabItem(mTabFolder, SWT.CLOSE);
		mTabFolder.setSelection(item);
		// item.setData(null);
		StyledText text = createStyledText();
		item.setControl(text);
		item.setText("new file");
	}

	private StyledText createStyledText() {
		StyledText text = new StyledText(mTabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				onModifyText(e);
			}
		});

		text.addLineStyleListener(mOctoLineStyler);

		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				onKeyReleasedEditor(e);
			}
		});
		text.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StyledText text = (StyledText) e.widget;
				Point pt = text.getSelection();
				mLblStatus.setText(String.format("%d/%d", pt.x, pt.y));
			}
		});
		text.setFont(SWTResourceManager.getFont("Courier New", 9, SWT.NORMAL));
		return text;

	}

	private void styleText(String text) {

	}

	private void setStyleRange(StyleRange range) {

		try {

			StyleRange oldRange = getTextSource().getStyleRangeAtOffset(range.start);
			if (oldRange != null) {
				oldRange.length = range.length;
				oldRange.foreground = range.foreground;
			} else {
				getTextSource().setStyleRange(range);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	protected void onOpenFile() {
		String[] filterExt = { "*.8o" };
		FileDialog fd = new FileDialog(shlJoctoIde, SWT.OPEN);
		fd.setText("Open chip8 program");
		fd.setFilterExtensions(filterExt);
		mFilename = fd.open();
		if (mFilename == null)
			return;
		loadFile(mFilename);
		setAutoload();

	}

	private void parseFile() {
		try {
			String text = getTextSource().getText();
			String lines[] = text.split("\n");
			StringBuilder sbLabels = new StringBuilder();
			char c;
			int pos, len;
			String word;
			ArrayList<String> list = new ArrayList<>();
			CTokenizer tokenizer = new CTokenizer();
			tokenizer.start(text);
			CToken token = new CToken();

			mLineNumbers.clear();
			while (tokenizer.hasData()) {
				tokenizer.getToken(token);
				switch (token.token) {
				case label:
					list.add(token.literal);
					mLineNumbers.put(token.literal, token.pos);
					sbLabels.append(token.literal + "\n");
					break;
				case macro:
				case dotTiles:
				case dotSprites:
				case dotTileset:
				case dotFunction:
					tokenizer.getToken(token);
					if (token.token == Token.literal) {
						list.add(token.literal);
						mLineNumbers.put(token.literal, token.pos);
						sbLabels.append(token.literal + "\n");
						break;
					}

				}

			}

			/*
			 * for (String line : lines) { line = line.trim(); if (line.startsWith(": ")) {
			 * int p = line.indexOf(' ', 2); if (p == -1) p = line.length(); String label =
			 * line.substring(1, p).trim(); list.add(label); sbLabels.append(label + "\n");
			 * } else { len = line.length(); word = ""; pos = 0; c = 0; while (pos < len) {
			 * c = line.charAt(pos++); if (!Character.isWhitespace(c)) break; } if (c != 0)
			 * { word = String.format("%c", c); while (pos < len) { c = line.charAt(pos++);
			 * if (!(c == '-' || c == ':' || c == '_' || Character.isAlphabetic(c) ||
			 * Character.isDigit(c))) break; word += c; } } if (word.endsWith(":")) { String
			 * label = word.substring(0, word.length() - 1); list.add(label);
			 * sbLabels.append(label + "\n"); } } }
			 */
			list.sort(new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return o1.toLowerCase().compareTo(o2.toLowerCase());
				}
			});
			String labels = sbLabels.toString();
			if (labels.compareTo(mStrLabels) != 0) {
				mStrLabels = labels;
				mListLabels.removeAll();
				for (String lbl : list)
					mListLabels.add(lbl);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
