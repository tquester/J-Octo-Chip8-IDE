package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.wb.swt.SWTResourceManager;

import assembler.CDebugEntries;
import disass.C8DisassEmitter;
import disass.CC8Decoder;
import disass.CC8Label;
import emulator.C8DebugSource;
import emulator.C8DebugSourceLine;
import emulator.Chip8CPU;
import emulator.IEmulator;
import emulator.IEmulatorCallback;
import ide.CCallback;
import ide.CMainMenus;

import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class CDialogEmulator extends Dialog implements IEmulator {

	final static int time = 16;
	static final int key_completion = 0x40000;

	protected Object result;
	protected Shell shlChipsuperChipxoChip;
	Label registerLabels[] = new Label[19];
	Label registerLabelTitles[] = new Label[19];
	String titles[] = { "V0", "V1", "V2", "V3", "V4", "V5", "V6", "V7", "V8", "V9", "VA", "VB", "VC", "VD", "VE", "VF",
			"W", "Snd", "I" };

	Composite mCompositeRegister;
	Composite mCanvasScreen;
	Chip8CPU mCPU = new Chip8CPU();
	Label mLblDisass;
	Label mLblData;
	List mListSource;
	TabFolder mTabFolder;
	Combo mComboSpeed;
	public boolean startRunning = false;

	private String mChip8Filename;
	private Color colors[];
	int bytesRead;
	Display display;
	CC8Decoder mDisassembler = new CC8Decoder();
	C8DisassEmitter mDisassEmitter = new C8DisassEmitter();

	private Listener mKeyUpFilter;

	private Listener mKeyDownFIlter;
	private CMainMenus mMainMenus;

	public void run() {
		mCPU.run();
	}

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public CDialogEmulator(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
		mDisassembler.setMemory(mCPU.getMemory());
		mDisassembler.emitter = mDisassEmitter;

	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlChipsuperChipxoChip.open();
		shlChipsuperChipxoChip.layout();
		createRegisterLabels();
		createMenu();
		display = getParent().getDisplay();
		mKeyUpFilter = new Listener() {

			@Override
			public void handleEvent(Event event) {
				onKeyUp(event.character, event.keyCode);

			}
		};

		mKeyDownFIlter = new Listener() {

			@Override
			public void handleEvent(Event event) {
				System.out.println(String.format("event: %d", event.type));
				onKeyDown(event.character, event.keyCode);

			}
		};

		display.addFilter(SWT.KeyUp, mKeyUpFilter);
		display.addFilter(SWT.KeyDown, mKeyDownFIlter);

		colors = new Color[4];
		colors[0] = display.getSystemColor(SWT.COLOR_YELLOW);
		colors[1] = display.getSystemColor(SWT.COLOR_BLACK);
		colors[2] = display.getSystemColor(SWT.COLOR_RED);
		colors[3] = display.getSystemColor(SWT.COLOR_GREEN);
		for (int i = 5; i < 100; i += 5)
			mComboSpeed.add(Integer.toString(i));

		createTimer();
		initDoubleBuffer();

		if (mDebugSource != null) {
			disassAndInitDebugger(null);
			// initListDebugSource();
			displayDebug();
		}
		if (startRunning)
			run();
		mCPU.gpu.mIEmulator = this;
		while (!shlChipsuperChipxoChip.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private void createMenu() {

		mMainMenus = new CMainMenus(shlChipsuperChipxoChip);
		mMainMenus.addMenu("&File").add("&Exit", new CCallback() {
			@Override
			public void callback() {
				shlChipsuperChipxoChip.close();
			}
		});

		mMainMenus.addMenu("&Debug").add("&Run\tF5", SWT.F5, new CCallback() {

			@Override
			public void callback() {
				onRun();
			}
		}).add("&Step into\tF11", SWT.F11, new CCallback() {

			@Override
			public void callback() {
				onStepInto();
			}
		}).add("&Step over\tF10", SWT.F10, new CCallback() {

			@Override
			public void callback() {
				onStepOver();
			}
		})

		;

	}

	protected void onKeyUp(char character, int keyCode) {
		/*
		 * if (keyCode == 0x1000013) onStepInto(); if (keyCode == 0x1000014)
		 * onStepOver(); if (keyCode == 0x100000e) onRun();
		 */
		// System.out.println(String.format("%x - %d", keyCode,(int) character));
		mCPU.addRemoveKey(character);
		// displayDebug();

	}

	protected void onKeyDown(char character, int keyCode) {
		mCPU.addKey(character);

	}

	private void initDoubleBuffer() {
		if (mCanvasScreen != null) {
			Rectangle bounds = mCanvasScreen.getBounds();
			mCPU.gpu.initImage(display, bounds.width, bounds.height);
		}

	}

	Runnable timer = new Runnable() {
		private boolean mTimerStopped;

		public void run() {

			if (mTimerStop == false) {
				if (mCPU.gpu.dirty) {
					mCPU.gpu.dirty = false;

					mCanvasScreen.redraw();
				}

				mCPU.timerTick();
				display.timerExec(time, this);
			}

		}

	};

	private boolean mTimerStop;

	private C8DebugSource mDebugSource;

	protected boolean mDisposed = false;

//	public TreeMap<String, Integer> mMapLabels = null;

	public TreeMap<String, CC8Label> mLabels;
	private Text mTextLog;
	private String mLastLogLine = "";

	void createTimer() {
		mTimerStop = false;
		display.timerExec(time, timer);
	}

	private void createRegisterLabels() {
		int x = 10;
		int y = 10;
		int h = 21;
		int w1 = 25;
		int w2 = mCompositeRegister.getBounds().width - 30;
		int pos = 0;
		for (String lbl : titles) {
			registerLabelTitles[pos] = new Label(mCompositeRegister, SWT.NONE);
			registerLabels[pos] = new Label(mCompositeRegister, SWT.NONE);
			registerLabelTitles[pos].setBounds(x, y, w1, h);
			registerLabelTitles[pos].setText(lbl);
			registerLabels[pos].setText("$00");
			registerLabels[pos].setBounds(x + 30, y, w2, h);
			y += h + 2;
			pos++;
		}

	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlChipsuperChipxoChip = new Shell(getParent(), getStyle());
		shlChipsuperChipxoChip.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				onClose();
			}
		});
		shlChipsuperChipxoChip.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				onKeyPressed(e);

			}

			@Override
			public void keyPressed(KeyEvent e) {
				onKeyReleased(e);

			}
		});

		shlChipsuperChipxoChip.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				onResize();
			}
		});
		shlChipsuperChipxoChip.setSize(886, 520);
		shlChipsuperChipxoChip.setText("Chip8/Super Chip8/XO Chip8");
		shlChipsuperChipxoChip.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				shlChipsuperChipxoChip.setFocus();

			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub

			}

		});

		mCanvasScreen = new Canvas(shlChipsuperChipxoChip, SWT.NO_BACKGROUND);
		mCanvasScreen.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				onRepaintScreen(e);
			}
		});
		mCanvasScreen.setBounds(10, 71, 576, 278);

		mCompositeRegister = new Composite(shlChipsuperChipxoChip, SWT.BORDER);
		mCompositeRegister.setBounds(737, 10, 125, 465);

		Composite composite_2 = new Composite(shlChipsuperChipxoChip, SWT.NONE);
		composite_2.setBounds(10, 10, 576, 40);

		Button btnOpenChip = new Button(composite_2, SWT.NONE);
		btnOpenChip.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onOpenChip8();
			}
		});
		btnOpenChip.setBounds(0, 10, 139, 25);
		btnOpenChip.setText("open chip8");

		Button btnStart = new Button(composite_2, SWT.FLAT);
		btnStart.setToolTipText("F6");
		btnStart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onRun();
			}
		});

		btnStart.setImage(SWTResourceManager.getImage(CDialogEmulator.class, "/disass/play.png"));
		btnStart.setBounds(150, 4, 40, 32);

		Button btnPause = new Button(composite_2, SWT.NONE);
		btnPause.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
			}
		});
		btnPause.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onPause();
			}
		});
		btnPause.setImage(SWTResourceManager.getImage(CDialogEmulator.class, "/disass/pause.png"));
		btnPause.setBounds(195, 4, 40, 32);

		Button btnStop = new Button(composite_2, SWT.NONE);
		btnStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onStop();
			}
		});
		btnStop.setImage(SWTResourceManager.getImage(CDialogEmulator.class, "/disass/stop.png"));
		btnStop.setBounds(240, 4, 40, 32);

		Button btnStepInto = new Button(composite_2, SWT.NONE);
		btnStepInto.setToolTipText("F10");
		btnStepInto.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onStepInto();
			}
		});
		btnStepInto.setImage(SWTResourceManager.getImage(CDialogEmulator.class, "/disass/stepinto.png"));
		btnStepInto.setBounds(285, 4, 40, 32);

		Button btnStepOver = new Button(composite_2, SWT.NONE);
		btnStepOver.setToolTipText("F11");
		btnStepOver.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onStepOver();
			}
		});
		btnStepOver.setImage(SWTResourceManager.getImage(CDialogEmulator.class, "/disass/stepover.png"));
		btnStepOver.setBounds(330, 4, 40, 32);

		Button btnBreakpoint = new Button(composite_2, SWT.NONE);
		btnBreakpoint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setClearBreakpoint();
			}
		});
		btnBreakpoint.setImage(SWTResourceManager.getImage(CDialogEmulator.class, "/disass/breakpoint.png"));
		btnBreakpoint.setBounds(375, 4, 40, 32);

		Label lblSpeed = new Label(composite_2, SWT.NONE);
		lblSpeed.setBounds(420, 10, 44, 15);
		lblSpeed.setText("Speed");

		mComboSpeed = new Combo(composite_2, SWT.NONE);
		mComboSpeed.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSpeedSelected();
			}
		});
		mComboSpeed.setBounds(465, 5, 51, 23);

		mLblDisass = new Label(shlChipsuperChipxoChip, SWT.NONE);
		mLblDisass.setText("200 00 00 NOP");
		mLblDisass.setBounds(10, 353, 576, 15);

		mLblData = new Label(shlChipsuperChipxoChip, SWT.BORDER | SWT.WRAP);
		mLblData.setFont(SWTResourceManager.getFont("Courier New", 9, SWT.NORMAL));
		mLblData.setBounds(650, 10, 137, 339);
		mLblData.setText("0000 00 ");

		mTabFolder = new TabFolder(shlChipsuperChipxoChip, SWT.NONE);
		mTabFolder.setBounds(10, 374, 721, 101);

		TabItem tbtmDisassembler = new TabItem(mTabFolder, SWT.NONE);
		tbtmDisassembler.setText("Disassembler");

		mListSource = new List(mTabFolder, SWT.BORDER | SWT.V_SCROLL);
		tbtmDisassembler.setControl(mListSource);
		mListSource.setFont(SWTResourceManager.getFont("Courier New", 9, SWT.NORMAL));

		TabItem tbtmLog = new TabItem(mTabFolder, SWT.NONE);
		tbtmLog.setText("Log");

		mTextLog = new Text(mTabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		tbtmLog.setControl(mTextLog);

	}

	protected void onSpeedSelected() {
		try {
			int speed = Integer.parseInt(mComboSpeed.getText());
			mCPU.setSpeed(speed);
		} catch (Exception e) {

		}

	}

	protected void onClose() {
		mDisposed = true;
		mCPU.gpu.mIEmulator = null;
		onStop();
		stopTimer();
		mCPU.gpu.mIEmulator = null;

		display.removeFilter(SWT.KeyDown, mKeyUpFilter);
		display.removeFilter(SWT.KeyUp, mKeyDownFIlter);

	}

	protected void setClearBreakpoint() {
		try {
			int line = mListSource.getSelectionIndex();
			int pc = mDebugSource.getPCForLine(line);
			if (pc != -1) {
				mCPU.setBreakpoint(pc);
				boolean breakpoint = mCPU.isBreakpoint(pc);

				String text = mListSource.getItem(line);
				text = (breakpoint ? "*" : " ") + text.substring(1);
				mListSource.setItem(line, text);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	protected void onKeyReleased(KeyEvent e) {
		System.out.println(String.format("%c %x", e.character, e.keyCode));

	}

	protected void onKeyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	protected void onResize() {
		if (mListSource == null)
			return;
		if (mCompositeRegister == null)
			return;
		if (mLblData == null)
			return;
		Rectangle rect = shlChipsuperChipxoChip.getBounds();
		Rectangle rectList = mTabFolder.getBounds();
		Rectangle rectData = mLblData.getBounds();
		Rectangle rectRegisters = mCompositeRegister.getBounds();
		mCompositeRegister.setBounds(rect.width - rectRegisters.width - 10, rectRegisters.y, rectRegisters.width,
				rect.height - rectRegisters.y - 40);
		mTabFolder.setBounds(rectList.x, rectList.y, rect.width - rectRegisters.width - rectList.x - 20,
				rect.height - rectList.y - 60);
		mLblData.setBounds(rectData.x, rectData.y, rect.width - rectRegisters.width - 20, rectData.height);

	}

	protected void stopTimer() {
		mTimerStop = true;

	}

	protected void onStepOver() {
		mCPU.stepOver();
		displayDebug();
	}

	{
		// TODO Auto-generated method stub

	}

	protected void onStepInto() {
		try {
			mCPU.cpuTick();
			displayDebug();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void onStop() {
		try {
			mCPU.reset();
			mCPU.gpu.cls();
			if (!mDisposed) {
				mCanvasScreen.redraw();
				displayDebug();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	protected void onPause() {
		mCPU.stop();
		displayDebug();

	}

	protected void onRun() {
		onStepInto();
		run();

	}

	protected void onOpenChip8() {
		mCPU.stop();
		String[] filterExt = { "*.??8", "*.ch8", "*.sc8" };
		FileDialog fd = new FileDialog(shlChipsuperChipxoChip, SWT.OPEN);
		fd.setText("Open chip8 program");
		fd.setFilterExtensions(filterExt);
		mChip8Filename = fd.open();
		shlChipsuperChipxoChip.setText(mChip8Filename);
		loadGame(mChip8Filename);
		displayDebug();
	}

	private void displayDebug() {
		int i;
		String strValue;
		int value;
		for (i = 0; i <= 15; i++) {
			value = mCPU.vx[i] & 0xff;
			strValue = String.format("$%02x %d", value, value);
			registerLabels[i].setText(strValue);
		}
		value = mCPU.regDelay;
		registerLabels[16].setText(String.format("$%02x %d", value, value));
		value = mCPU.regSound;
		registerLabels[17].setText(String.format("$%02x %d", value, value));
		value = mCPU.regI;
		registerLabels[18].setText(String.format("$%04x %d", value, value));
		String command = mDisassEmitter.disassLine(mCPU.getMemory(), mCPU.pc);
		mLblDisass.setText(command);
		if (mDebugSource != null) {
			int line = mDebugSource.getLineForCode(mCPU.pc);
			if (line != -1) {
				mListSource.setSelection(line);
			}
		}
		StringBuilder sb = new StringBuilder();
		int adr = mCPU.regI;
		String key;
		for (i = 0; i < 20; i++) {
			byte data = (byte) (mCPU.getMemory()[adr++] & 0xff);
			if (i <= 15) {
				key = String.format("Key %01x=%d", i, mCPU.keyPressed[i]);
			} else
				key = "";

			String str = String.format("%04x %02x %04d %s %s\n", adr, data, data, int2bin(data), key);
			sb.append(str);
		}
		mLblData.setText(sb.toString());

	}

	private Object int2bin(int data) {
		String s = "";
		int mask = 0x80;
		int i;
		for (i = 0; i < 8; i++) {
			if ((data & mask) == 0)
				s += " ";
			else
				s += String.format("%c", 0x2588);
			mask >>= 1;
		}
		return s;
	}

	int loadGame(String filename) {
		bytesRead = 0;
		try (InputStream inputStream = new FileInputStream(filename);) {
			long fileSize = new File(filename).length();
			byte gameBytes[] = new byte[(int) fileSize];
			bytesRead = inputStream.read(gameBytes);
			inputStream.close();
			int source = 0;
			int target = 0x200;
			for (int i = 0; i < bytesRead; i++) {
				mCPU.memory[target + i] = gameBytes[source + i];
			}
			mCPU.gpu.cls();
			disassAndInitDebugger(filename);
			return (int) bytesRead;

		} catch (IOException ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	private void disassAndInitDebugger(String filename) {

		C8DebugSource sourceHints = mDebugSource;
		mDisassEmitter.createDebugSource();
		if (filename != null) {
			mDisassembler.loadHints(filename + ".hints");
		} else {
			if (mLabels != null)
				mDisassembler.setAssemblerLabels(mLabels);
		}
		mDisassembler.start(mCPU.getMemory(), bytesRead);
		mDebugSource = mDisassEmitter.getDebugSource(sourceHints);

		if (mDebugSource != null)
			initListDebugSource();

	}

	private void initListDebugSource() {
		mListSource.removeAll();
		for (C8DebugSourceLine line : mDebugSource) {
			mListSource.add(" " + line.line);
		}

	}

	protected void onRepaintScreen(PaintEvent e) {
		if (mCPU.gpu.mImage != null) {
			try {
				mCPU.gpu.waitSemaphore();
				mCPU.gpu.setSemaphore(true);
				e.gc.drawImage(mCPU.gpu.mImage, 0, 0);
				e.gc.dispose();
				mCPU.gpu.setSemaphore(false);
			} catch (Exception ex) {

			}
		}
		/*
		 * Chip8GPU gpu = mCPU.gpu; int chip8Width = gpu.width; int chip8Height =
		 * gpu.height; Rectangle bounds = mCanvasScreen.getBounds(); int tileWidth =
		 * bounds.width / chip8Width; int tileHeight = bounds.height / chip8Height; int
		 * x=0; int y=0; if (colors == null) return;
		 * 
		 * 
		 * int adr=0; y=0; for (int iy=0;iy<gpu.height;iy++) { x = 0; for (int
		 * ix=0;ix<gpu.width;ix++) { int screenByte = gpu.mScreen[adr+ix];
		 * e.gc.setBackground(colors[screenByte & 0x03]); e.gc.fillRectangle(x, y,
		 * tileWidth, tileHeight); x+=tileWidth; } adr += gpu.width; y += tileHeight; }
		 */
	}

	@Override
	public void updateScreen() {
		shlChipsuperChipxoChip.getDisplay().asyncExec(new Runnable() {
			public void run() {
				mCanvasScreen.redraw();
			}
		});

	}

	public void copyMemory(int start, byte[] source, int size) {
		int ptr = start;
		for (int i = 0; i < size; i++) {
			mCPU.memory[ptr] = source[ptr];
			ptr++;
		}
		bytesRead = size;

	}

	@Override
	public void notifyStop() {
		shlChipsuperChipxoChip.getDisplay().asyncExec(new Runnable() {
			public void run() {
				displayDebug();
			}
		});
	}

	public void setDebugSource(C8DebugSource debugSource) {
		mDebugSource = debugSource;

	}

	@Override
	public void log(String text) {
		if (text.compareTo(mLastLogLine) == 0)
			return;
		mLastLogLine = text;
		shlChipsuperChipxoChip.getDisplay().asyncExec(new Runnable() {
			public void run() {
				String edit = mTextLog.getText();
				edit += text + "\r\n";
				mTextLog.setText(edit);
				mTextLog.setSelection(edit.length() - 1);
			}
		});

	}
}
