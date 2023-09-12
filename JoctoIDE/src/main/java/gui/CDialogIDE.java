package gui;

import org.eclipse.swt.widgets.Dialog;
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
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wb.swt.SWTResourceManager;

import assembler.CChip8Assembler;
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

	protected Object result;
	protected Shell shlJoctoIde;
	private StyledText mTextSource;
	private String mFilename;
	private String mStrLabels = "";
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
	List mListFiles;
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
	protected boolean mResizing=false;
	protected int mResizeBase;

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

	private void modifyTitle() {
		shlJoctoIde.setText("J-Octo IDE -- " + mFilename);
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
	      Display display = getParent().getDisplay();
	     shlJoctoIde = new Shell(display);

		//shlJoctoIde = new Shell(getParent(), getStyle());
		//shlJoctoIde = getParent();
		shlJoctoIde.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				if (mSplitError != 0) {
					if (mBarErrors != null) {
						Rectangle rect = shlJoctoIde.getBounds();
						Rectangle rectSplit = mBarErrors.getBounds();
						double v = rect.height;
						v *= mSplitError;
						//mBarErrors.setBounds(rectSplit.x,(int) v, rectSplit.width, rectSplit.height);
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
		shlJoctoIde.setSize(931, 597);
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
		btnNewButton.setBounds(652, 10, 75, 25);
		btnNewButton.setText("Sprite Editor");

		Button btnNewButton_1 = new Button(composite, SWT.NONE);
		btnNewButton_1.setBounds(733, 10, 75, 25);
		btnNewButton_1.setText("Tile Editor");

		Button btnDisassembler = new Button(composite, SWT.NONE);
		btnDisassembler.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onDisassembler();
			}
		});
		btnDisassembler.setBounds(814, 10, 81, 25);
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
		btnEmulator.setBounds(571, 10, 75, 25);
		btnEmulator.setText("Emulator");

		Button btnCompileDisass = new Button(composite, SWT.NONE);
		btnCompileDisass.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onCompileDisass();
			}
		});
		btnCompileDisass.setText("Compile+Disass");
		btnCompileDisass.setBounds(253, 10, 97, 25);

		mTextSource = new StyledText(shlJoctoIde, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);

		mTextSource.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				onModifyText(e);
			}
		});

		mTextSource.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				onKeyReleasedEditor(e);
			}
		});
		mTextSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Point pt = mTextSource.getSelection();
				mLblStatus.setText(String.format("%d/%d", pt.x, pt.y));
			}
		});
		mTextSource.setFont(SWTResourceManager.getFont("Courier New", 9, SWT.NORMAL));
		mTextSource.setBounds(185, 57, 730, 337);

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
		mLblStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		mLblStatus.setBounds(179, 523, 533, 20);
		mLblStatus.setText("status");

		mListFiles = new List(shlJoctoIde, SWT.BORDER);
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
				mResizing=true;
			}
			@Override
			public void mouseUp(MouseEvent e) {
				mResizing=false;
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
					Rectangle rectText = mTextSource.getBounds();
					Rectangle rectBarErrors = mBarErrors.getBounds();
					Rectangle rect = shlJoctoIde.getBounds();
					int top = mResizeBase + e.y; 
					mTextErrors.setBounds( //
							rectErrors.x, //
							top,
							rect.width - rectErrors.x - 30, //
							rect.height-top-constErrorSpaceBottom);
					
					top -= 12;
					mBarErrors.setBounds(//
							rectErrors.x, //
							top, //
							rect.width-rectErrors.x, //
							rectBarErrors.height);

					top -= 8;
					mTextSource.setBounds(//
							rectErrors.x, //
							rectText.y, //
							rect.width - rectErrors.x - 30, //
							top-rectText.y);					
					
					//onResize();
				}
			}
		});
		mBarErrors.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseDown(MouseEvent e) {
				Rectangle rect = mBarErrors.getBounds();
				mResizeBase = rect.y;
				mResizing=true;
			}
			@Override
			public void mouseUp(MouseEvent e) {
				mResizing=false;
			}
		});
		

		mBarLeftRight = new Composite(shlJoctoIde, SWT.NONE);
		mBarLeftRight.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
		mBarLeftRight.setBounds(175, 57, 5, 495);
		
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
				mResizing=true;
			}
			@Override
			public void mouseUp(MouseEvent e) {
				mResizing=false;
			}
		});		

	}

	protected void onModifyText(ModifyEvent e) {
		String text = mTextSource.getText();
		saveUndo();
		if (mStackUndo.size() > 30)
			mStackUndo.remove(0);

	}

	protected void onCompileDisass() {
		CChip8Assembler assembler = new CChip8Assembler();
		assembler.assemble(mTextSource.getText(), mFilename);
		String errors = assembler.getErrors();
		mTextErrors.setText(errors);
		if (errors.trim().length() == 0) {
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
			mTextSource.setSelection(pos, pos);
		}

	}

	private void onEditJumpForward() {

	}

	private String wordUnderCursor(String text) {
		char c;
		int textPos = mTextSource.getCaretOffset();
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
		String text = mTextSource.getText();
		String word = wordUnderCursor(text);
		CTokenizer tokenizer = new CTokenizer();
		CToken token = new CToken();
		tokenizer.start(text);
		while (tokenizer.hasData()) {
			tokenizer.getToken(token);
			if (token.token == Token.label) {
				if (token.literal.compareTo(word) == 0) {
					mLineNumberStack.push(mTextSource.getCaretOffset());
					mTextSource.setSelection(token.pos, token.pos);
					break;
				}
			}
		}

	}

	private void onEditAutocomplete() {
		String text = mTextSource.getText();
		String word = wordUnderCursor(text).toLowerCase();
		if (word == null)
			return;
		mListCompletion = new List(mTextSource, SWT.BORDER);

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
			Caret caret = mTextSource.getCaret();
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
					// mTextSource.setSelection(mWordBegin, mWordEnd);
					// mTextSource.

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
			mTextSource.replaceTextRange(mWordBegin, mWordEnd - mWordBegin + 1, newword);
		}
		mListCompletion.dispose();
		mListCompletion = null;

	}

	Runnable timer = new Runnable() {
		public void run() {

			int offset = mTextSource.getCaretOffset();
			String text = mTextSource.getText();
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
			styleText(mTextSource.getText());
			parseFile();
			display.timerExec(time, this);

		}

	};
	private CSearchReplace mSerachReplace;
	private boolean mTextDirty;
	private boolean mUndoing = false;
	private double mSplitError=0;

	protected void onEmulator() {
		CDialogEmulator dlg = new CDialogEmulator(shlJoctoIde, SWT.TITLE + SWT.RESIZE + SWT.MIN + SWT.MAX);
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
		}).add("&Emulator", new CCallback() {
			@Override
			public void callback() {

			}
		});

	}

	
	protected void OnOptions() {
		// TODO Auto-generated method stub

	}

	protected void onAutoformat() {
		String[] lines = mTextSource.getText().split("\n");
		int level = 0;
		CTokens tokens = new CTokens();
		StringBuilder sb = new StringBuilder();
		CWordParser wordParser = new CWordParser();
		int iline = 0;
		int caret = mTextSource.getCaretOffset();
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
		mTextSource.setText(sb.toString());
		mTextSource.setSelection(caret, caret);

	}

	protected void onSelectAll() {
		// TODO Auto-generated method stub

	}

	protected void onRedo() {
		// TODO Auto-generated method stub

	}

	private void saveUndo() {
		if (!mUndoing) {
			String text = String.valueOf(mTextSource.getText());
			mStackUndo.push(text);
		}

	}

	protected void onUndo() {
		if (mStackUndo != null) {
			if (!mStackUndo.isEmpty()) {
				String text = mStackUndo.pop();
				mUndoing = true;
				mTextSource.setText(text);
				mUndoing = false;
				mTextDirty = true;
			}
		}
	}

	protected void onFind() {
		mSerachReplace = new CSearchReplace();
		mSerachReplace.start(mTextSource.getText());
		CDialogFindReplace dlg = new CDialogFindReplace(shlJoctoIde, SWT.TITLE | SWT.CLOSE);
		dlg.mSerachReplace = mSerachReplace;
		dlg.mTextSource = mTextSource;
		dlg.open();

	}

	protected void onNewFile() {
		onSaveFile();
		mFilename = null;
		mTextSource.setText("");

	}

	protected void onSpriteEditor() {
		CDialogSpriteEditor dlg = new CDialogSpriteEditor(shlJoctoIde, getStyle());
		dlg.readSourcefile(mTextSource.getText());
		dlg.open();

	}

	protected void onDisassembler() {
		CDialogDisassembler dlg = new CDialogDisassembler(shlJoctoIde, SWT.TITLE + SWT.RESIZE + SWT.MIN + SWT.MAX);
		dlg.open();

	}

	protected void onCompile() {
		autosave();
		CChip8Assembler assembler = new CChip8Assembler();
		assembler.assemble(mTextSource.getText(), mFilename);

		String errors = assembler.getErrors();
		boolean ok = errors.trim().length() == 0;
		errors += String.format("Code size = %d (%d remaining\n", assembler.getCodeSize(),
				4096 - 0x200 - assembler.getCodeSize());
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

	private void autosave() {
		String text = mTextSource.getText();
		Tools.writeTextFile("autosave.8o", text);

	}

	protected void onListLabelSelected(SelectionEvent e) {
		String[] selection = mListLabels.getSelection();
		if (selection.length == 1) {
			String label = ": " + selection[0];
			String text = mTextSource.getText();
			int p = text.indexOf(label);
			if (p == -1) {
				label = selection[0] + ":";
				p = text.indexOf(label);
			}
			if (p != -1) {
				mTextSource.setSelection(p, p);
			}
		}

	}

	protected void onResize() {
		if (composite == null || mListLabels == null || mTextSource == null || mTextErrors == null
				|| mLblStatus == null)
			return;
		Rectangle rect = shlJoctoIde.getBounds();
		Rectangle rectComposite = composite.getBounds();
		Rectangle rectList = mListLabels.getBounds();
		Rectangle rectText = mTextSource.getBounds();
		Rectangle rectErrors = mTextErrors.getBounds();
		Rectangle rectStatus = mLblStatus.getBounds();
		Rectangle rectBarLeftRight = mBarLeftRight.getBounds();
		Rectangle rectBarFiles = mBarFiles.getBounds();
		Rectangle rectBarErrors = mBarErrors.getBounds();
		Rectangle RectListFiles = mListFiles.getBounds();

		int left = rectBarLeftRight.x+5;
		int right = rectBarLeftRight.x + rectBarLeftRight.width+5;
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
				rect.height - rectStatus.height - 55, //
				rect.width - rectStatus.x - 10, //
				rectStatus.height);

		int top = rect.height - rectErrors.height-constErrorSpaceBottom;
		mTextErrors.setBounds( //
				right, //
				top,
				rect.width - right - 30, //
				rectErrors.height);

		top -= 12;
		mBarErrors.setBounds(//
				right, //
				top, //
				rect.width-right, //
				rectBarErrors.height);

		top -= 8;
		mTextSource.setBounds(//
				right, //
				rectText.y, //
				rect.width - right - 30, //
				top-rectText.y);

		mTextErrors.setBounds( //
				right, //
				rect.height - rectErrors.height-constErrorSpaceBottom,
				rect.width - right - 30, //
				rectErrors.height);
		
		mBarLeftRight.setBounds(//
				rectBarLeftRight.x, //
				rectBarLeftRight.y, //
				rectBarLeftRight.width, //
				rect.height-rectBarLeftRight.y);
		
		mBarFiles.setBounds(//
				rectBarFiles.x, //
				rectBarFiles.y, //
				left-rectBarFiles.x, //
				rectBarFiles.height);
		
		
		double h = rect.height;
		double s = rectBarErrors.y;
		mSplitError = h/s;

	}

	protected void onSaveAs() {
		String[] filterExt = { "*.8o" };
		FileDialog fd = new FileDialog(shlJoctoIde, SWT.SAVE);
		fd.setText("Save chip8 program");
		fd.setFilterExtensions(filterExt);
		mFilename = fd.open();
		if (mFilename == null)
			return;
		Tools.saveTextFile(mTextSource.getText(), mFilename);
		shlJoctoIde.setText("J-Octo IDE "+mFilename);
		setAutoload(mFilename);
		
	}

	protected void onSaveFile() {
		if (mFilename == null) {
			String[] filterExt = { "*.8o" };
			FileDialog fd = new FileDialog(shlJoctoIde, SWT.SAVE);
			fd.setText("Save chip8 program");
			fd.setFilterExtensions(filterExt);
			mFilename = fd.open();
			if (mFilename == null)
				return;

		}
		Tools.saveTextFile(mTextSource.getText(), mFilename);
		setAutoload(mFilename);

	}

	private void setAutoload(String filename) {
		File file = new File(filename);
		Tools.writeTextFile("autoload.inf", file.getAbsolutePath());
	}

	String getAutoload() {
		String autoload = Tools.loadTextFile("autoload.inf");
		return autoload;
	}

	void autoload() {
		String fname = getAutoload();
		if (fname != null) {
			fname = fname.trim();
			mFilename = fname;
			String text = Tools.loadTextFile(fname);
			if (text != null) {
				mTextSource.setText(text);
				styleText(text);
				modifyTitle();
				mStackUndo.removeAllElements();
				saveUndo();
			}
		}
	}

	private void styleText(String text) {
		ArrayList<StyleRange> styleRanges = new ArrayList<>();
		CTokenizer tokenizer = new CTokenizer();
		Color lime = shlJoctoIde.getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
		Color blue = shlJoctoIde.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE);
		Color green = shlJoctoIde.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
		tokenizer.start(text);
		CToken token = new CToken();
		StyleRange range;
		while (tokenizer.hasData()) {
			tokenizer.getToken(token);
			if (token.token == null)
				continue;
			switch (token.token) {
			case newline:
			case literal:
			case label:
				break;
			case comment:
				range = new StyleRange();
				range.start = token.pos - 1;
				range.length = token.literal.length();
				range.fontStyle = SWT.BOLD;
				range.foreground = green;
				styleRanges.add(range);
				break;

			case number:
			case string:
				range = new StyleRange();
				range.start = token.pos - 1;
				range.length = token.literal.length();
				range.fontStyle = SWT.BOLD;
				range.foreground = blue;
				styleRanges.add(range);
				break;
			default:
				range = new StyleRange();
				range.start = token.pos - 1;
				range.length = token.literal.length();
				range.fontStyle = SWT.BOLD;
				range.foreground = lime;
				styleRanges.add(range);
				break;
			}
		}
		StyleRange ranges[] = new StyleRange[styleRanges.size()];
		for (int i = 0; i < styleRanges.size(); i++)
			ranges[i] = styleRanges.get(i);
		mTextSource.setStyleRanges(ranges);

	}

	protected void onOpenFile() {
		String[] filterExt = { "*.8o" };
		FileDialog fd = new FileDialog(shlJoctoIde, SWT.OPEN);
		fd.setText("Open chip8 program");
		fd.setFilterExtensions(filterExt);
		mFilename = fd.open();
		if (mFilename == null)
			return;
		mTextSource.setText(Tools.loadTextFile(mFilename));
		styleText(mTextSource.getText());
		mStackUndo.removeAllElements();
		shlJoctoIde.setText("J-Octo IDE "+mFilename);
		saveUndo();
		Tools.saveTextFile(mTextSource.getText(), mFilename);


		parseFile();

	}

	private void parseFile() {
		String text = mTextSource.getText();
		String lines[] = text.split("\n");
		StringBuilder sbLabels = new StringBuilder();
		char c;
		int pos, len;
		String word;
		ArrayList<String> list = new ArrayList<>();
		for (String line : lines) {
			line = line.trim();
			if (line.startsWith(": ")) {
				int p = line.indexOf(' ', 2);
				if (p == -1)
					p = line.length();
				String label = line.substring(1, p).trim();
				list.add(label);
				sbLabels.append(label + "\n");
			} else {
				len = line.length();
				word = "";
				pos = 0;
				c = 0;
				while (pos < len) {
					c = line.charAt(pos++);
					if (!Character.isWhitespace(c))
						break;
				}
				if (c != 0) {
					word = String.format("%c", c);
					while (pos < len) {
						c = line.charAt(pos++);
						if (!(c == '-' || c == ':' || c == '_' || Character.isAlphabetic(c) || Character.isDigit(c)))
							break;
						word += c;
					}
				}
				if (word.endsWith(":")) {
					String label = word.substring(0, word.length() - 1);
					list.add(label);
					sbLabels.append(label + "\n");
				}
			}
		}
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

	}
}
