package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
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
import org.eclipse.swt.widgets.Canvas;

public class CDialogSpriteEditor extends Dialog {

	protected Object result;
	protected Shell shell;
	private CSpriteCanvas mCanvas;
	private Combo mComboRows;	
	private Button mBtnShowAs8x8;
	private Button mBtnSprite8;
	private Button mBtnSprite16;
	private Button mBtnShowBitmaps;
	private CCanvasSpriteMatrix mCanvasSpriteView;
	private TreeMap<Integer, String> mLabels = new TreeMap<>();
	private Combo mComboTileset;
	public CResources mResources = new CResources();
	int[] mSpriteData = new int[16];
	//int spriteHeight = 0;			// 0 = 16x16
	//int mBasePos=0;
	private Text mTextHex;
	private boolean mParse = true;
	private CSpriteData mCurSprite;
	private CDialogSpriteEditor mSpriteEditor;
	
	public void readSourcefile(StyledText editor) {
		mResources = new CResources();
		mResources.readSourcecode(editor);
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
				mCanvas.spriteHeight = 0;
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
					mCanvas.spriteHeight = Integer.parseInt(mComboRows.getText());
				}
				catch(Exception e1) {
					mCanvas.spriteHeight = 12;
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
		
		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSaveToEditor();
			}
		});
		btnNewButton.setBounds(463, 216, 75, 25);
		btnNewButton.setText("Save");
		
		mCanvasSpriteView = new CCanvasSpriteMatrix(shell, SWT.BORDER);
		mCanvasSpriteView.setBounds(688, 30, 232, 312);
		mCanvasSpriteView.setBackground(null);
		mCanvasSpriteView.setTileData(new ICanvasSpriteMatrixData() {
			
			@Override
			public int getTileW() {
				return mCanvas.spriteHeight == 0 ? 16 : 8; 
			}
			
			@Override
			public int getTileH() {
				return mCanvas.spriteHeight == 0 ? 16 : mCanvas.spriteHeight; 
			}
			
			@Override
			public int[] getTileData() {
				return mSpriteData;
			}

			@Override
			public void onSpriteSelected(int pos) {
				mCanvas.mOffset = pos;
				mCanvas.redraw();
				updateTitle();
			}
		});
		
		
		
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

	protected void onSaveToEditor() {
		if (mCurSprite != null) {
			mCurSprite.setText(mTextHex.getText());
			if (mResources.save(mCurSprite) == false) {
				CDialogMessage dlg = new CDialogMessage(shell, SWT.TITLE);
				dlg.text = "Error saving icons\nPlase update source file\nmanually";
				dlg.open();
			}
			
		}
		// TODO Auto-generated method stub
		
	}

	protected void onComboSpriteSetSelected() {
		int index = mComboTileset.getSelectionIndex();
		if (index != -1) {
			CSpriteData data = mResources.mSprites.get(index);
			
			mCurSprite = data;
			if (data.h == 16) {
				mBtnSprite16.setSelection(true);
				mBtnSprite8.setSelection(false);
				mCanvas.spriteHeight = 0;
			} else {
				mBtnSprite8.setSelection(true);
				mBtnSprite16.setSelection(false);
				mCanvas.spriteHeight = data.h;
				mComboRows.setText(String.format("%d", data.h));

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
		for (int i=1;i<16;i++) {
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
		mSpriteEditor = this;
		shell = new Shell(getParent(), getStyle());
		shell.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				onResize();
			}
		});
		shell.setSize(947, 561);
		shell.setText(getText());
		
		mCanvas = new CSpriteCanvas(shell, SWT.NONE);
		mCanvas.mReadonly = false;
		mCanvas.setData(new CSpriteCanvasData() {
			
			@Override
			public void updateText() {
				mSpriteEditor.updateText();
				mCanvasSpriteView.redraw();
				
			}
			
			@Override
			public void setSpriteWord(int bytepos, int word) {
				mSpriteEditor.setSpriteWord(bytepos, word);
				
			}
			
			@Override
			public void setSpriteByte(int bytepos, int newbyte) {
				mSpriteEditor.setSpriteByte(bytepos, newbyte);
				
			}
			
			@Override
			public int getSpriteWord(int bytepos) {
				return mSpriteEditor.getSpriteWord(bytepos);
			}
			
			@Override
			public int getSpriteByte(int bytepos) {
				return mSpriteEditor.getSpriteByte(bytepos);
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
					mCanvas.spriteHeight = Integer.parseInt(mComboRows.getText());
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
		mTextHex.setBounds(10, 348, 910, 170);
		
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
		int rows = mCanvas.spriteHeight == 0 ? 16 : mCanvas.spriteHeight;
		int bytes = mCanvas.spriteHeight == 0 ? 2 : 1;
		int pos = addNewBytes(rows*bytes);
		for (int i=0;i<rows*bytes;i++) {
			mSpriteData[pos+i] = mSpriteData[mCanvas.mOffset+i];
		}
		mCanvas.mOffset = pos;
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
		int rows = mCanvas.spriteHeight == 0 ? 16 : mCanvas.spriteHeight;
		int bytes = mCanvas.spriteHeight == 0 ? 2 : 1;
		int pos = addNewBytes(rows*bytes);
		mCanvas.mOffset = pos;
		updateText();
		mCanvas.redraw();
		
	}

	protected void onShiftDown() {
		mCanvas.onShiftDown();
	}

	protected void onShiftUp() {
		mCanvas.onShiftUp();
	}

	protected void flipW() {
		mCanvas.flipW();
	}

	protected void flipH() {
		mCanvas.flipH();
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
		int ofs = mCanvas.spriteHeight == 0 ? 32 : mCanvas.spriteHeight;
		if (mCanvas.mOffset + ofs < mSpriteData.length) {
			mCanvas.mOffset += ofs;
		} else
			mCanvas.mOffset = 0;
		mCanvas.redraw();
		updateTitle();
		
	}

	private void updateTitle() {
		int size = mCanvas.spriteHeight == 0 ? 32 : mCanvas.spriteHeight;
		int ofs = mCanvas.spriteHeight == 0 ? 32 : mCanvas.spriteHeight;
		int sprite = mCanvas.mOffset / size;
		String label = mLabels.get(mCanvas.mOffset);
		if (label == null) label = "";
		shell.setText(String.format("Sprite %d/%d pos %d %s",sprite, mSpriteData.length/ofs, mCanvas.mOffset, label));
		
		
	}

	protected void prevSprite() {
		int ofs = mCanvas.spriteHeight == 0 ? 32 : mCanvas.spriteHeight;
		if (mCanvas.mOffset - ofs > 0) {
			mCanvas.mOffset -= ofs;
		} else {
			mCanvas.mOffset = mSpriteData.length-ofs;
		}
		mCanvas.redraw();
		updateTitle();
	}

	protected void shiftRight() {
		mCanvas.shiftRight();
	}

	protected void shiftLeft() {
		mCanvas.shiftLeft();
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
		try {
			return pos < mSpriteData.length ? mSpriteData[pos] : 0;
		} catch(Exception ex) {
			ex.printStackTrace();
			return 0;
		}
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
		int spheight = mCanvas.spriteHeight == 0 ? 16 : mCanvas.spriteHeight;
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
			if (mCanvas.spriteHeight == 0) {
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
					for (i=0;i<mCanvas.spriteHeight-1;i++) {
						int b = getSpriteByte(bytepos++);
						sb.append(String.format("  %02x\t#\t%s\n", b, hex8Bin(b)));
					}
					sb.append("\n");
				} else {
					for (i=0;i<mCanvas.spriteHeight-1;i++) {
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
			mCanvas.mOffset = 0;
			mCanvas.redraw();
			mCanvasSpriteView.redraw();
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

}
