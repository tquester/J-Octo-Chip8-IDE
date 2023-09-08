package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import spiteed.CResources;
import spiteed.CSpriteData;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Label;

public class CDialogSpriteEditor extends Dialog {

	protected Object result;
	protected Shell shell;
	Composite mCanvas;
	Combo mComboRows;	
	Button mBtnShowAs8x8;
	Button mBtnSprite8;
	Button mBtnSprite16;
	Button mBtnShowBitmaps;
	TreeMap<Integer, String> mLabels = new TreeMap<>();
	Combo mComboTileset;
	
	public CResources mResources = new CResources();
	
	int[] mSpriteData = new int[16];
	
	
	int spriteHeight = 0;			// 0 = 16x16
	int mBasePos=0;
	
	private Text mTextHex;
	private boolean mParse = true;
	private CSpriteData mCurSprite;
	
	public void readSourcefile(String text) {
		mResources = new CResources();
		mResources.readSourcecode(text);
	}

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CDialogSpriteEditor(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		fillCombo();
		updateText();
		/*
		mTextHex.setText(": miner1\r\n"
				+ "0x00 0xC0 0x07 0xC0 0x0F 0x80 0x06 0x80 0x07 0xC0 0x07 0x80 0x03 0x00 0x07 0x80 \r\n"
				+ "0x0F 0xC0 0x0F 0xC0 0x1E 0xE0 0x1F 0x60 0x07 0x80 0x0E 0xC0 0x0D 0xC0 0x0E 0xE0\r\n"
				+ ": miner2\r\n"
				+ "0x00 0xC0 0x07 0xC0 0x0F 0x80 0x06 0x80 0x07 0xC0 0x07 0x80 0x03 0x00 0x07 0x80\r\n"
				+ "0x0D 0xC0 0x0D 0xC0 0x0D 0xC0 0x0E 0xC0 0x07 0x80 0x03 0x00 0x03 0x00 0x03 0x80\r\n"
				+ ": miner3\r\n"
				+ "0x00 0xC0 0x07 0xC0 0x0F 0x80 0x06 0x80 0x07 0xC0 0x07 0x80 0x03 0x00 0x07 0x80 \r\n"
				+ "0x0F 0xC0 0x0F 0xC0 0x1E 0xE0 0x1F 0x60 0x07 0x80 0x0E 0xC0 0x0D 0xC0 0x0E 0xE0\r\n"
				+ ": miner4:\r\n"
				+ "0x00 0xC0 0x07 0xC0 0x0F 0x80 0x06 0x80 0x07 0xC0 0x07 0x80 0x03 0x00 0x07 0x80\r\n"
				+ "0x0F 0xC0 0x1F 0xE0 0x3F 0xF0 0x37 0xB0 0x07 0xC0 0x0E 0xD0 0x18 0x70 0x1C 0x60\r\n"
				+ ": miner5\r\n"
				+ "0x00 0xC0 0x07 0xC0 0x0F 0x80 0x06 0x80 0x07 0xC0 0x07 0x80 0x03 0x00 0x07 0x80\r\n"
				+ "0x0D 0xC0 0x0D 0xC0 0x0D 0xC0 0x0E 0xC0 0x07 0x80 0x03 0x00 0x03 0x00 0x03 0x80\r\n"
				+ "\r\n"
				+ ": minerr1\r\n"
				+ "0x03 0x00 0x03 0xe0 0x01 0xf0 0x01 0x60 0x03 0xe0 0x01 0xe0 0x00 0xc0 0x01 0xe0 \r\n"
				+ "0x03 0xf0 0x03 0xf0 0x07 0x78 0x06 0xf8 0x01 0xe0 0x03 0x70 0x03 0xb0 0x07 0x70 \r\n"
				+ ": minerr2\r\n"
				+ "0x03 0x00 0x03 0xe0 0x01 0xf0 0x01 0x60 0x03 0xe0 0x01 0xe0 0x00 0xc0 0x01 0xe0 \r\n"
				+ "0x03 0xb0 0x03 0xb0 0x03 0xb0 0x03 0x70 0x01 0xe0 0x00 0xc0 0x00 0xc0 0x01 0xc0 \r\n"
				+ ": minerr3\r\n"
				+ "0x03 0x00 0x03 0xe0 0x01 0xf0 0x01 0x60 0x03 0xe0 0x01 0xe0 0x00 0xc0 0x01 0xe0 \r\n"
				+ "0x03 0xf0 0x03 0xf0 0x07 0x78 0x06 0xf8 0x01 0xe0 0x03 0x70 0x03 0xb0 0x07 0x70 \r\n"
				+ ": minerr4\r\n"
				+ "0x03 0x00 0x03 0xe0 0x01 0xf0 0x01 0x60 0x03 0xe0 0x01 0xe0 0x00 0xc0 0x01 0xe0 \r\n"
				+ "0x03 0xf0 0x07 0xf8 0x0f 0xfc 0x0d 0xec 0x03 0xe0 0x0b 0x70 0x0e 0x18 0x06 0x38 \r\n"
				+ ": minerr5\r\n"
				+ "0x03 0x00 0x03 0xe0 0x01 0xf0 0x01 0x60 0x03 0xe0 0x01 0xe0 0x00 0xc0 0x01 0xe0 \r\n"
				+ "0x03 0xb0 0x03 0xb0 0x03 0xb0 0x03 0x70 0x01 0xe0 0x00 0xc0 0x00 0xc0 0x01 0xc0 \r\n"
				+ "");
				*/
		
		mBtnSprite16 = new Button(shell, SWT.RADIO);
		mBtnSprite16.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mBtnSprite8.setSelection(false);
				spriteHeight = 0;
				mBtnShowAs8x8.setVisible(true);
				mCanvas.redraw();
			}
		});
		mBtnSprite16.setBounds(463, 247, 90, 16);
		mBtnSprite16.setText("16x16 Sprite");
		
		mBtnSprite8 = new Button(shell, SWT.RADIO);
		mBtnSprite8.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mBtnSprite16.setSelection(false);
				mBtnShowAs8x8.setVisible(false);
				try {
					spriteHeight = Integer.parseInt(mComboRows.getText());
				}
				catch(Exception e1) {
					spriteHeight = 12;
					mComboRows.setText("12");
				}
				mCanvas.redraw();
			}
		});
		mBtnSprite8.setBounds(463, 269, 90, 16);
		mBtnSprite8.setText("8xn Sprite");
		
		Label lblTileset = new Label(shell, SWT.NONE);
		lblTileset.setBounds(463, 10, 55, 15);
		lblTileset.setText("Tileset");
		
		
		
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

	protected void onComboSpriteSetSelected() {
		int index = mComboTileset.getSelectionIndex();
		if (index != -1) {
			CSpriteData data = mResources.mSprites.get(index);
			mCurSprite = data;
			if (data.height == 16) {
				mBtnSprite16.setSelection(true);
				mBtnSprite8.setSelection(false);
				spriteHeight = 0;
			} else {
				mBtnSprite8.setSelection(true);
				mBtnSprite16.setSelection(false);
				mComboRows.setText(String.format("%s", data.height));
				spriteHeight = data.height;

			}
			mTextHex.setText(data.getText());
			
		}
		
		
	}

	protected void onResize() {
		if (mTextHex == null) return;
		Rectangle bounds = shell.getBounds();
		Rectangle rectText = mTextHex.getBounds();
		mTextHex.setBounds(rectText.x, rectText.y, bounds.width-40, bounds.height-40-rectText.y);
				
		
	}

	private void fillCombo() {
		for (int i=1;i<13;i++) {
			mComboRows.add(String.format("%d", i));
		}
		
		for (CSpriteData data: mResources.mSprites) {
			mComboTileset.add(data.toString());
		}
		
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				onResize();
			}
		});
		shell.setSize(714, 561);
		shell.setText(getText());
		
		mCanvas = new Composite(shell, SWT.NONE);
		mCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				paintSpriteEditor(e);
			}
		});
		mCanvas.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				onMouseDownCanvas(e);
				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		mCanvas.setBounds(10, 10, 437, 332);
		
		Button btnShiftLeft = new Button(shell, SWT.NONE);
		btnShiftLeft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shiftLeft();
			}
		});
		btnShiftLeft.setBounds(463, 64, 75, 25);
		btnShiftLeft.setText("Shift left");
		
		Button buttonShiftRight = new Button(shell, SWT.NONE);
		buttonShiftRight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shiftRight();
			}
		});
		buttonShiftRight.setText("Shift right");
		buttonShiftRight.setBounds(544, 64, 75, 25);
		
		Button buttonShiftUp = new Button(shell, SWT.NONE);
		buttonShiftUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onShiftUp();
			}
		});
		buttonShiftUp.setText("Shift up");
		buttonShiftUp.setBounds(463, 95, 75, 25);
		
		Button btnShiftDown = new Button(shell, SWT.NONE);
		btnShiftDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onShiftDown();
			}
		});
		btnShiftDown.setText("Shift down");
		btnShiftDown.setBounds(544, 95, 75, 25);
		
		Button btnFlipHorizontal = new Button(shell, SWT.NONE);
		btnFlipHorizontal.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				flipH();
			}
		});
		btnFlipHorizontal.setText("flip h");
		btnFlipHorizontal.setBounds(463, 126, 75, 25);
		
		Button btnFlipW = new Button(shell, SWT.NONE);
		btnFlipW.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				flipW();
			}
		});
		btnFlipW.setText("flip w");
		btnFlipW.setBounds(544, 126, 75, 25);
		
		mBtnShowAs8x8 = new Button(shell, SWT.CHECK);
		mBtnShowAs8x8.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateText();
			}
		});
		mBtnShowAs8x8.setBounds(463, 296, 212, 16);
		mBtnShowAs8x8.setText("Show 16x16 Sprite as 4*8x8 Sprites");

		
		mComboRows = new Combo(shell, SWT.NONE);
		mComboRows.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					spriteHeight = Integer.parseInt(mComboRows.getText());
					mCanvas.redraw();
				}
				catch(Exception e1) {
					
				}
			}
		});
		mComboRows.setBounds(559, 267, 54, 23);
		
		Button btnNext = new Button(shell, SWT.NONE);
		btnNext.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				prevSprite();
			}
		});
		btnNext.setBounds(463, 157, 75, 25);
		btnNext.setText("<<");
		
		Button btnNext_1 = new Button(shell, SWT.NONE);
		btnNext_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				nextSprite();
			}
		});
		btnNext_1.setText(">>");
		btnNext_1.setBounds(544, 157, 75, 25);
		
		Button btnNew = new Button(shell, SWT.NONE);
		btnNew.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onNewSprite();
			}
		});
		btnNew.setText("New");
		btnNew.setBounds(463, 188, 75, 25);
		
		Button btnCopy = new Button(shell, SWT.NONE);
		btnCopy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onCopySprite();
			}
		});
		btnCopy.setText("Copy");
		btnCopy.setBounds(544, 188, 75, 25);
		
		mTextHex = new Text(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		mTextHex.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				parseText(mTextHex.getText()+" ");
			}
		});
		mTextHex.setFont(SWTResourceManager.getFont("Courier New", 9, SWT.NORMAL));
		mTextHex.setBounds(10, 348, 677, 170);
		
		mBtnShowBitmaps = new Button(shell, SWT.CHECK);
		mBtnShowBitmaps.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateText();
			}
		});
		mBtnShowBitmaps.setBounds(463, 318, 93, 16);
		mBtnShowBitmaps.setText("Show Bitmaps");
		
		mComboTileset = new Combo(shell, SWT.NONE);
		mComboTileset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onComboSpriteSetSelected();
			}
		});
		mComboTileset.setBounds(465, 30, 210, 28);



	}

	protected void onCopySprite() {
		int rows = spriteHeight == 0 ? 16 : spriteHeight;
		int bytes = spriteHeight == 0 ? 2 : 1;
		int pos = addNewBytes(rows*bytes);
		for (int i=0;i<rows*bytes;i++) {
			mSpriteData[pos+i] = mSpriteData[mBasePos+i];
		}
		mBasePos = pos;
		updateText();
		mCanvas.redraw();
		
	}

	private int addNewBytes(int newBytes) {
		int r = mSpriteData.length;
		int temp[] = new int[r+newBytes];
		for (int i = 0;i<r;i++) {
			temp[i] = mSpriteData[i];
		}
		mSpriteData = temp;
		return r;
	}

	protected void onNewSprite() {
		int rows = spriteHeight == 0 ? 16 : spriteHeight;
		int bytes = spriteHeight == 0 ? 2 : 1;
		int pos = addNewBytes(rows*bytes);
		mBasePos = pos;
		updateText();
		mCanvas.redraw();
		
	}

	protected void onShiftDown() {
		int rows = spriteHeight == 0 ? 16 : spriteHeight;
		int bytes = spriteHeight == 0 ? 2 : 1;
		int pos1 = mBasePos+rows*bytes;
		int pos = pos1-bytes;
		pos1--;
		pos--;
		for (int i=0;i<rows-1;i++) {
			for (int j=0;j<bytes;j++) {
				mSpriteData[pos1--] = mSpriteData[pos--];
			}
			
			
		}
		for (int i=0;i<bytes;i++) mSpriteData[mBasePos+i] = 0;
		updateText();
		mCanvas.redraw();
		
	}

	protected void onShiftUp() {
		int rows = spriteHeight == 0 ? 16 : spriteHeight;
		int bytes = spriteHeight == 0 ? 2 : 1;
		int pos = mBasePos;
		int pos1 = mBasePos+bytes;
		for (int i=0;i<rows-1;i++) {
			for (int j=0;j<bytes;j++) {
				mSpriteData[pos++] = mSpriteData[pos1++];
			}
		}
		for (int i=0;i<bytes;i++) mSpriteData[pos++] = 0;
		updateText();
		mCanvas.redraw();
	}

	protected void flipW() {
		int rows = spriteHeight == 0 ? 16 : spriteHeight;
		int bytes = spriteHeight == 0 ? 2 : 1;
		int temp[] = new int[bytes];
		int pos = mBasePos;
		for (int i=0;i<rows;i++) {
			int d = bytes-1;
			for (int j=0;j<bytes;j++) {
				int v = mSpriteData[pos+j];
				v = reverse(v);
				temp[d] = v;
				d--;	
			}
			for (int j=0;j<bytes;j++) {
				mSpriteData[pos+j] = temp[j];
			}
			pos+=bytes;
		}
		mCanvas.redraw();
		updateText();
		
	}

	protected void flipH() {
		int rows = spriteHeight == 0 ? 16 : spriteHeight;
		int bytes = spriteHeight == 0 ? 2 : 1;
		int temp[] = new int[bytes];
		int pos = mBasePos;
		int j;
		int pos2 = mBasePos+bytes*(rows-1);
		for (int i=0;i<rows/2;i++) {
			for (j=0;j<bytes;j++) temp[j] = mSpriteData[pos+j];
			for (j=0;j<bytes;j++) mSpriteData[pos+j] = mSpriteData[pos2+j];
			for (j=0;j<bytes;j++) mSpriteData[pos2+j] = temp[j];
			pos+=bytes;
			pos2-=bytes;
			
		}
		mCanvas.redraw();
		updateText();
		
	}
	
	int reverse(int b) {
		int r =0;
		int bit = 1;
		for (int i=1;i<=8;i++) {
			int nextbit = (b & bit) != 0 ? 1 : 0;
			r = r+r+nextbit;
			bit = bit << 1;
		}
		
		return r;
	}

	protected void nextSprite() {
		int ofs = spriteHeight == 0 ? 32 : spriteHeight;
		if (mBasePos + ofs < mSpriteData.length) {
			mBasePos += ofs;
		} else
			mBasePos = 0;
		mCanvas.redraw();
		updateTitle();
		
	}

	private void updateTitle() {
		int size = spriteHeight == 0 ? 32 : spriteHeight;
		int ofs = spriteHeight == 0 ? 32 : spriteHeight;
		int sprite = mBasePos / size;
		String label = mLabels.get(mBasePos);
		if (label == null) label = "";
		shell.setText(String.format("Sprite %d/%d pos %d %s",sprite, mSpriteData.length/ofs, mBasePos, label));
		
		
	}

	protected void prevSprite() {
		int ofs = spriteHeight == 0 ? 32 : spriteHeight;
		if (mBasePos - ofs > 0) {
			mBasePos -= ofs;
		} else {
			mBasePos = mSpriteData.length-ofs;
		}
		mCanvas.redraw();
		updateTitle();
	}

	protected void shiftRight() {
		int i;
		int word;
		int pos = mBasePos;
		if (spriteHeight == 0) {
			for (i=0;i<16;i++) {
				word = getSpriteWord(pos);
				word >>= 1;
				setSpriteWord(pos, word);
				pos+=2;
			}
		} else {
			for (i=0;i<spriteHeight;i++) {
				word = getSpriteByte(pos);
				word >>= 1;
				setSpriteByte(pos,word);
				pos++;
			}
		}
		updateText();
		mCanvas.redraw();		
	}

	protected void shiftLeft() {
		int i;
		int word;
		int pos = mBasePos;
		if (spriteHeight == 0) {
			for (i=0;i<16;i++) {
				word = getSpriteWord(pos);
				word <<= 1;
				setSpriteWord(pos, word);
				pos+=2;
			}
		} else {
			for (i=0;i<spriteHeight;i++) {
				word = getSpriteByte(pos);
				word <<= 1;
				setSpriteByte(pos,word);
				pos++;
			}
		}
		updateText();
		mCanvas.redraw();
		
	}

	protected void onMouseDownCanvas(MouseEvent e) {
		Rectangle bounds = mCanvas.getBounds();
		int pos;
		int x = 0;
		int y = 0;
	    int width = spriteHeight == 0 ? 16 : 8;
	    int height = spriteHeight == 0 ? 16 : 12;
		int dx = bounds.width / width;
		int dy = bounds.height / height;
		int w = dx * 16;
		if (height > width) {
			dx = dy;
			w = dx * 8;
		}
		x = e.x / dx;
		y = e.y / dy;
		if (spriteHeight == 0) {
			pos = mBasePos + y * 2;
			if (x > 7) {
				pos++;
				x -= 8;
			}
			int mask = 0x80 >> x;
			setSpriteByte(pos, getSpriteByte(pos) ^ mask);
				 
		} else {
			pos = mBasePos + y;
			if (x <= 8) {
				int mask = 0x80 >> x;
				setSpriteByte(pos, getSpriteByte(pos) ^ mask);
			}
		}
		mCanvas.redraw();
		updateText();
		
		
		System.out.println(String.format("%d/%d", x,y));
	}
	
	private void setSpriteByte(int pos, int data) {
		if (mSpriteData.length < pos+1) {
			int newbytes[] = new int[pos+1];
			for (int i=0;i<mSpriteData.length;i++) 
				newbytes[i] = mSpriteData[i];
			mSpriteData = newbytes;
			
		}
		
		mSpriteData[pos] = data;
		
	}

	int getSpriteByte(int pos) {
		return pos < mSpriteData.length ? mSpriteData[pos] : 0;
	}
	
	int getSpriteWord(int pos) {
		int r = getSpriteByte(pos) << 8; 
		r |=  getSpriteByte(pos+1);
		return r;
	}
	
	void setSpriteWord(int pos, int word) {
		setSpriteByte(pos, word >> 8);
		setSpriteByte(pos+1, word & 0xff);
		
	}
	
	String createText() {
		StringBuilder sb = new StringBuilder();
		int spheight = spriteHeight == 0 ? 16 : spriteHeight;
		int isprite = 1;
		int i;
		int j;
		int bytepos = 0;
		while (bytepos < mSpriteData.length) {
			String label;
			label = mLabels.get(bytepos);
			if (label == null) 
				label = String.format("# Sprite %d", isprite++);
			else
				label = ": "+label;
					
			sb.append(label+"\n");
			if (spriteHeight == 0) {
				if (mBtnShowAs8x8.getSelection() == false) {
					if (mBtnShowBitmaps.getSelection()) {
						for (i=0;i<16;i++) {
							int left = getSpriteByte(bytepos++);
							int right = getSpriteByte(bytepos++);
							sb.append(String.format("  %02x %02x\t#\t%s\n",left,right,hex16Bin(left,right)));
						}
						
					} else {
						for (j=0;j<2;j++) {
							for (i=0;i<8;i++) {
								sb.append(toHex(getSpriteByte(bytepos++))+" ");
								sb.append(toHex(getSpriteByte(bytepos++)));
								if (i < 15) sb.append(" ");
							}
							sb.append("\n");
						}
					}
				}
				else {
					
					sb.append(sprite8from16(bytepos));
					sb.append("\t# sprite top left\n");
					sb.append(sprite8from16(bytepos+1));
					sb.append("\t# sprite top right\n");
					sb.append(sprite8from16(bytepos+16));
					sb.append("\t# sprite bottom left\n");
					sb.append(sprite8from16(bytepos+17));
					sb.append("\t# sprite bottom right\n");
					bytepos+=32;
					
				}
			} else {
				if (mBtnShowBitmaps.getSelection()) {
					for (i=0;i<spriteHeight-1;i++) {
						int b = getSpriteByte(bytepos++);
						sb.append(String.format("  %02x\t#\t%s\n", b, hex8Bin(b)));
					}
					sb.append("\n");
				} else {
					for (i=0;i<spriteHeight-1;i++) {
						sb.append(toHex(getSpriteByte(bytepos++))+" ");
					}
					sb.append(toHex(getSpriteByte(bytepos++))+"\n");
				}
				
			}
		}
		return sb.toString();
	}
	
	private Object hex16Bin(int left, int right) {
		int word = left*256+right;
		String r = Integer.toString(word, 2).replace('0', ' ').replace('1', '#');
		while (r.length() < 16) r = " "+r;
		return r;
	}
	
	private Object hex8Bin(int b) {
		String r = Integer.toString(b, 2).replace('0', ' ').replace('1', '#');
		while (r.length() < 8) r = " "+r;
		return r;
	}

	private Object sprite8from16(int pos) {
		String str = "";
		if (mBtnShowBitmaps.getSelection()) {
			for (int i=0;i<8;i++) {
				int b = getSpriteByte(pos++);
				pos++;
				str += String.format("  %02x\t#\t%s\n",b, hex8Bin(b));
			}
			
		}
		for (int i=0;i<8;i++) {
			if (i != 0) str += " ";
			str += toHex(getSpriteByte(pos));
			pos+=2;
		}
		
		return str;
	}


	void updateText() {
		mParse = false;
		String text = createText(); 
		if (mCurSprite != null)
			mCurSprite.setText(text);
		mTextHex.setText(text);
		mParse = true;
	}

	private String toHex(int spriteByte) {
		return String.format("0x%02x", spriteByte);
	}
	
	void parseText(String text) {
		String literal;
		if (mParse == false ) return;
		mLabels.clear();
		try {
			ArrayList<Integer> data = new ArrayList<>();
			char c;
			int pos = 0;
			int len = text.length();
			while (pos < len) {
				c = text.charAt(pos++);
				if (c == '\n' || c == '\r') continue;
				literal = "";
				while (pos < len) {
					if (!Character.isWhitespace(c)) break;
					c = text.charAt(pos++);
				}
				if (c == '#' || c == ';' || c == ':')  {
					int c1 = c;
					while (pos < len) {
						c = text.charAt(pos++);
						if (c == '\n' || c == '\r') break;
						literal += c;
					}
					if (c1 == ':') {
						mLabels.put(data.size(), literal);
					}
				
					continue;
				}
				literal = "";
				while (pos < len) {
					if (Character.isWhitespace(c)) break;
					if (c == '#' || c == ';' || c == ':') break;
					literal += c;
					c = text.charAt(pos++);
				}
				int newbyte=0;
				if (literal.length() > 0) {
					literal = literal.toLowerCase();
					if (literal.charAt(0) == '$') {
						newbyte = readHex(literal.substring(1));
					} else if (literal.startsWith("0x")) {
						newbyte = readHex(literal.substring(2));
					} else
						newbyte = readDez(literal);
					data.add(newbyte);
				}
			}
			mSpriteData = new int[data.size()];
			for (int i=0;i<data.size();i++) {
				mSpriteData[i] = data.get(i).intValue();
			}
			mBasePos = 0;
			mCanvas.redraw();
			updateTitle();
			//updateText();
			
		}
		catch(Exception e) {
			
		}
		
	}

	private int readDez(String literal) {
		int r =0;
		try {
			r = Integer.parseInt(literal, 10);
 		} catch(Exception e) {
 			
 		}
		return r;
	}

	private int readHex(String literal) {
		int r =0;
		try {
			r = Integer.parseInt(literal, 16);
 		} catch(Exception e) {
 			
 		}
		return r;
	}

	protected void paintSpriteEditor(PaintEvent e) {
		Rectangle bounds = mCanvas.getBounds();
		Display display = getParent().getDisplay();
		
	    Color white = display.getSystemColor(SWT.COLOR_WHITE);
	    Color black = display.getSystemColor(SWT.COLOR_BLACK);
	    Color gray1 = display.getSystemColor(SWT.COLOR_GRAY);
	    Color gray2 = display.getSystemColor(SWT.COLOR_DARK_GRAY);
	    Color red = display.getSystemColor(SWT.COLOR_RED);
		
	    int width = spriteHeight == 0 ? 16 : 8;
	    int height = spriteHeight == 0 ? 16 : 12;
		int x = 0;
		int y = 0;
		int dx = bounds.width / width;
		int dy = bounds.height / height;
		int w = dx * 16;
		if (height > width) {
			dx = dy;
			w = dx * 8;
		}
		int h = dy * height;
		for (int i = 0; i < width+1; i++) {
			if (i == 8) {
				e.gc.setForeground(black);
			} else {
				if ((i & 3) == 0) {
					e.gc.setForeground(gray2);
				} else
					e.gc.setForeground(gray1);
			}
			e.gc.drawLine(x, 0, x, h);
	
			x += dx;
		}
		for (int i = 0; i < height+1; i++) {
			if (i == 8) {
				e.gc.setForeground(black);
			} else {
				if ((i & 3) == 0) {
					e.gc.setForeground(gray2);
				} else
					e.gc.setForeground(gray1);
			}			
			e.gc.drawLine(0, y, w, y);
			y += dy;
		}
		int bytepos = mBasePos;
		
		y = 0;
		int spheight = spriteHeight == 0 ? 16 : spriteHeight;
		
		for (int iy = 0; iy < spheight; iy++) {
			x = 0;
			if (spriteHeight == 0) {
				int word = getSpriteWord(bytepos);
				bytepos += 2;
				int mask = 0x8000;
				for (int ix=0;ix<16;ix++) {
					if ((word & mask) == 0) {
						e.gc.setForeground(white);
						e.gc.setBackground(white);
					} else {
						e.gc.setForeground(black);
						e.gc.setBackground(black);
					}
						
					e.gc.fillRectangle(x+2, y+2, dx-4, dy-4);
					x += dx;
					mask >>= 1;
				}
			} else {
				int word = getSpriteByte(bytepos);
				bytepos += 1;
				int mask = 0x80;
				for (int ix=0;ix<8;ix++) {
					if ((word & mask) == 0) {
						e.gc.setForeground(white);
						e.gc.setBackground(white);
					} else {
						e.gc.setForeground(black);
						e.gc.setBackground(black);
					}
						
					e.gc.fillRectangle(x+2, y+2, dx-4, dy-4);
					x += dx;
					mask >>= 1;
				}
				
			}
			y += dy;
		}
		
		e.gc.setForeground(red);
		e.gc.setBackground(red);
		for (int iy = spheight+1; iy <= height;iy++) {
			x = 0;
			for (int ix = 0; ix < width; ix++) {
				e.gc.fillRectangle(x+2, y+2, dx-4, dy-4);
				x += dx;
			}
			y += dy;
		}
		
	}
}
/*
# Sprite 1
0x60 0x7c 0x3e 0x6e 0x3c 0x18 0x3c 0x7e 0x7f 0x3c 0x36 0x76
# Sprite 2
0x06 0x3e 0x7c 0x76 0x3c 0x18 0x3c 0x7c 0x7c 0x38 0x38 0x3c
# Sprite 3
0x06 0x3e 0x7c 0x76 0x3c 0x18 0x3c 0x7e 0x7f 0x18 0x1e 0x0e
# Sprite 4
0x06 0x3e 0x7c 0x76 0x3c 0x18 0x3c 0x7e 0x3f 0x78 0x66 0x66
# Sprite 5
0x60 0x7c 0x3e 0x6e 0x3c 0x18 0x3c 0x7e 0x7f 0x3c 0x36 0x76
# Sprite 6
0x60 0x7c 0x3e 0x6e 0x3c 0x18 0x3c 0x3e 0x3e 0x1c 0x1c 0x3c
# Sprite 7
0x60 0x7c 0x3e 0x6e 0x3c 0x18 0x3c 0x7e 0xfe 0x18 0x78 0x70
# Sprite 8
0x60 0x7c 0x3e 0x6e 0x3c 0x18 0x3c 0x7e 0xfc 0x1e 0x66 0x66
*/