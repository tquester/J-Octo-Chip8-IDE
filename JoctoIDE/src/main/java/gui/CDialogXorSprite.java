package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Canvas;

import java.util.ArrayList;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

import assembler.CToken;
import spiteed.CSpriteData;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class CDialogXorSprite extends Dialog {

	CSpriteCanvas mCanvasXOR;
	CSpriteCanvas mCanvasSprite1;
	CSpriteCanvas mCanvasSprite2;
	protected Object result;
	protected Shell shlCreateXorSprite;
	private Text mText;
	private Text mTextXOR;
	CToken mToken = new CToken();
	int[] mSpriteData = new int[64];
	int[] mXORData = null;
	TreeMap<Integer, String> mLabels = new TreeMap<>();

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CDialogXorSprite(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlCreateXorSprite.open();
		shlCreateXorSprite.layout();
		Display display = getParent().getDisplay();
		while (!shlCreateXorSprite.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	
	void parseText(String text, boolean mParse) {
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
			mCanvasSprite1.redraw();
			mCanvasSprite2.redraw();
			mCanvasXOR.redraw();
			mTextXOR.setText(createText(mCanvasXOR));
			
		}
		catch(Exception e) {
			
		}
		
	}

	private int readHex(String literal) {
		int r =0;
		try {
			r = Integer.parseInt(literal, 16);
 		} catch(Exception e) {
 			
 		}
		return r;
	}
	
	private int readDez(String literal) {
		int r =0;
		try {
			r = Integer.parseInt(literal, 10);
 		} catch(Exception e) {
 			
 		}
		return r;
	}
	
	String createText(CSpriteCanvas canvas) {
		String result = null;
		int size = canvas.spriteHeight == 0 ? 32 : canvas.spriteHeight;
		for (int i=0;i<size;i++) {
			int b = canvas.getSpriteByte(i);
			String s = String.format("0x%02x", b);
			if (result == null) 
				result = s;
			else 
				result += " "+s;
		}
		
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		
		shlCreateXorSprite = new Shell(getParent(), getStyle());
		shlCreateXorSprite.setSize(1020, 607);
		shlCreateXorSprite.setText("Create XOR Sprite");
		
		mCanvasSprite1 = new CSpriteCanvas(shlCreateXorSprite, SWT.NONE);
		mCanvasSprite1.setBounds(10, 26, 320, 320);
		mCanvasSprite1.mReadonly = false;
		mCanvasSprite1.setData(new CSpriteCanvasData() {
			
			@Override
			public void updateText() {
				onUpdateText();
				
			}
			
			@Override
			public void setSpriteByte(int bytepos, int newbyte) {
				if (mSpriteData != null) {
					if (mSpriteData.length >= bytepos) {
						mSpriteData[bytepos] = newbyte; 
						System.out.println(String.format("write data[%d] = %x", bytepos, newbyte));
					}
				}
				
			}
			
			@Override
			public int getSpriteWord(int bytepos) {
				int result=0;
				if (mSpriteData != null) {
					if (mSpriteData.length >= bytepos+1) {
						 result = mSpriteData[bytepos] * 256 + mSpriteData[bytepos+1];
					}
				}
				return result;
			}
			
			@Override
			public int getSpriteByte(int bytepos) {
				int result=0;
				if (mSpriteData != null) {
					if (mSpriteData.length >= bytepos+1) {
						 result = mSpriteData[bytepos];
						 System.out.println(String.format("read data[%d] = %x", bytepos, result));
					}
				}
				return result;
			}

			@Override
			public void setSpriteWord(int bytepos, int word) {
				if (mSpriteData != null) {
					if (mSpriteData.length >= bytepos+1) {
						mSpriteData[bytepos] = (byte)(word >> 8);
						mSpriteData[bytepos+1] = (byte)(word & 0xff);
					}
				}
				
			}
		});
		
		
		mCanvasSprite2 = new CSpriteCanvas(shlCreateXorSprite, SWT.NONE);
		mCanvasSprite2.setBounds(336, 26, 320, 320);
		mCanvasSprite2.mReadonly = false;
		mCanvasSprite2.setData(new CSpriteCanvasData() {
			
			@Override
			public void updateText() {
				onUpdateText();
				
			}
			
			@Override
			public void setSpriteByte(int bytepos, int newbyte) {
				bytepos += getOfs();
				if (mSpriteData != null) {
					if (mSpriteData.length >= bytepos) {
						mSpriteData[bytepos] = newbyte; 
					}
				}
				
			}
			
			private int getOfs() {
				return mCanvasSprite2.spriteHeight == 0 ? 32 : mCanvasSprite2.spriteHeight;
			}

			@Override
			public int getSpriteWord(int bytepos) {
				bytepos += getOfs();
				int result=0;
				if (mSpriteData != null) {
					if (mSpriteData.length >= bytepos+1) {
						 result = mSpriteData[bytepos] * 256 + mSpriteData[bytepos+1];
					}
				}
				return result;
			}
			
			@Override
			public int getSpriteByte(int bytepos) {
				bytepos += getOfs();
				int result=0;
				if (mSpriteData != null) {
					if (mSpriteData.length >= bytepos+1) {
						 result = mSpriteData[bytepos];
					}
				}
				return result;
			}

			@Override
			public void setSpriteWord(int bytepos, int word) {
				bytepos += getOfs();
				int result=0;
				if (mSpriteData != null) {
					if (mSpriteData.length >= bytepos+1) {
						mSpriteData[bytepos] = (byte)(word >> 8);
						mSpriteData[bytepos+1] = (byte)(word & 0xff);
					}
				}
				
			}

		});
		
		mCanvasXOR = new CSpriteCanvas(shlCreateXorSprite, SWT.NONE);
		mCanvasXOR.setBounds(662, 26, 320, 320);
		mCanvasXOR.setData(new CSpriteCanvasData() {
			
			@Override
			public void updateText() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setSpriteByte(int bytepos, int newbyte) {
				
			}
			
			private int getOfs() {
				return mCanvasSprite2.spriteHeight == 0 ? 32 : mCanvasSprite2.spriteHeight;
			}

			@Override
			public int getSpriteWord(int bytepos) {
				int bytepos2 = bytepos + getOfs();
				int result=0;
				if (mSpriteData != null) {
					if (mSpriteData.length >= bytepos2+1) {
						 int word = mSpriteData[bytepos2] * 256 + mSpriteData[bytepos+12];
						 result = mSpriteData[bytepos] * 256 + mSpriteData[bytepos+1];
						 result ^= word;
					}
				}
				return result;
			}
			
			@Override
			public int getSpriteByte(int bytepos) {
				int bytepos2 = bytepos + getOfs();
				int result=0;
				if (mSpriteData != null) {
					if (mSpriteData.length >= bytepos2+1) {
						 result = mSpriteData[bytepos] ^ mSpriteData[bytepos2];
					}
				}
				return result;
			}

			@Override
			public void setSpriteWord(int bytepos, int word) {
				// TODO Auto-generated method stub
				
			}

		});
		
		mText = new Text(shlCreateXorSprite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		mText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				parseText(mText.getText(), true);
			}
		});
		mText.setBounds(10, 373, 981, 73);
		
		mTextXOR = new Text(shlCreateXorSprite, SWT.BORDER | SWT.MULTI);
		mTextXOR.setBounds(10, 469, 971, 27);
		
		Button btnShiftLeft = new Button(shlCreateXorSprite, SWT.NONE);
		btnShiftLeft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mCanvasSprite2.shiftLeft();
			}
		});
		btnShiftLeft.setBounds(501, 516, 75, 25);
		btnShiftLeft.setText("Shift left");
		
		Button btnShiftRight = new Button(shlCreateXorSprite, SWT.NONE);
		btnShiftRight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mCanvasSprite2.shiftRight();
			}
		});
		btnShiftRight.setBounds(582, 516, 75, 25);
		btnShiftRight.setText("Shift right");
		
		Button btnShiftUp = new Button(shlCreateXorSprite, SWT.NONE);
		btnShiftUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mCanvasSprite2.onShiftUp();
			}
		});
		btnShiftUp.setBounds(663, 516, 75, 25);
		btnShiftUp.setText("Shift up");
		
		Button btnShiftDown = new Button(shlCreateXorSprite, SWT.NONE);
		btnShiftDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mCanvasSprite2.onShiftDown();
			}
		});
		btnShiftDown.setBounds(744, 516, 75, 25);
		btnShiftDown.setText("Shift down");
		
		Button btnMirrorH = new Button(shlCreateXorSprite, SWT.NONE);
		btnMirrorH.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mCanvasSprite2.flipH();
			}
		});
		btnMirrorH.setBounds(826, 516, 75, 25);
		btnMirrorH.setText("Flip H");
		
		Button btnFlipW = new Button(shlCreateXorSprite, SWT.NONE);
		btnFlipW.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mCanvasSprite2.flipW();
			}
		});
		btnFlipW.setBounds(907, 516, 75, 25);
		btnFlipW.setText("Flip W");
		
		Label lblSprite = new Label(shlCreateXorSprite, SWT.NONE);
		lblSprite.setBounds(10, 5, 55, 15);
		lblSprite.setText("Sprite 1");
		
		Label lblSprite_1 = new Label(shlCreateXorSprite, SWT.NONE);
		lblSprite_1.setBounds(336, 5, 55, 15);
		lblSprite_1.setText("Sprite 2");
		
		Label lblXorSprite = new Label(shlCreateXorSprite, SWT.NONE);
		lblXorSprite.setBounds(664, 5, 55, 15);
		lblXorSprite.setText("Xor Sprite");
		
		Label lblNewLabel = new Label(shlCreateXorSprite, SWT.NONE);
		lblNewLabel.setBounds(10, 352, 89, 15);
		lblNewLabel.setText("Sprite 1 / Sprite 2");
		
		Label lblXorSprite_1 = new Label(shlCreateXorSprite, SWT.NONE);
		lblXorSprite_1.setBounds(10, 452, 89, 15);
		lblXorSprite_1.setText("XOR Sprite");
		
		Label lblHeight = new Label(shlCreateXorSprite, SWT.NONE);
		lblHeight.setBounds(10, 516, 55, 15);
		lblHeight.setText("Height");
		
		Combo combo = new Combo(shlCreateXorSprite, SWT.NONE);
		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					String str = combo.getText();
					int height = Integer.parseInt(str);
					mCanvasSprite1.setHeight(height);
					mCanvasSprite2.setHeight(height);
					mCanvasXOR.setHeight(height);
				}
				catch(Exception ex) {
					ex.printStackTrace();
					
				}
			}
		});
		combo.setItems(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "0"});
		combo.setBounds(71, 512, 91, 23);
		combo.setText("8");

	}

	protected void onUpdateText() {
		String text = createText(mCanvasSprite1) + "\r\n" + createText(mCanvasSprite2);
		String xorText = createText(mCanvasXOR);
		mText.setText(text);
		mTextXOR.setText(xorText);
		
		
	}
}
