package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Canvas;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import assembler.CToken;
import assembler.CTokenizer;
import assembler.Token;
import gui.CDialogVectorDraw.CDrawElement.Typ;

import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;

public class CDialogVectorDraw extends Dialog {

	public static class CDrawElement {
		static public enum Typ {
			NONE, POINT, LINE, RECT, CIRCLE, FILL, COLOR, HIRES, STOP, CLEAR, LORES
		}

		public Typ mTyp;
		public int x0, y0;
		public int x1=1, y1;

		/**
		 * @param iPoint
		 * @param x
		 */
		public void setPoint(int iPoint, int x, int y) {
			switch (iPoint) {
			case 0:
				x0 = x;
				y0 = y;
				break;
			case 1:
				x1 = x;
				y1 = y;
				break;
			}
		}

		CDrawElement() {
			this.mTyp = Typ.POINT;

		}

		CDrawElement(Typ typ) {
			this.mTyp = typ;
		}

		public int getNumPoints() {
			switch (mTyp) {
			case FILL:
				return 1;
			case POINT:
				return 1;
			case RECT:
			case CIRCLE:
			case LINE:
				return 2;
			}
			return 0;
		}
		
		public void drawSelection(PaintEvent e) {
			int num = getNumPoints();
			if (num >= 1) {
				drawRect(e, x0,y0);
			}
			if (num >= 2) {
				drawRect(e, x1, y1);
				
			}
		}

		private void drawRect(PaintEvent e, int x, int y) {
			e.gc.drawRectangle(x-2, y-2, 4, 4);
			
		}
		
		public boolean inRect(int x0, int y0, int x, int y) {
			if (x >= x0-2 && x <= x0+2 && y >= y0-2 && y <= x0+2) return true;
			return false;
		}
		
		int getSelectedPoint(int x, int y) {
			if (inRect(x0, y0, x, y)) return 0;
			if (inRect(x1, y1, x, y)) return 1;
			return -1;
		}

		public void onPaint(PaintEvent e) {
			switch (mTyp) {
			case POINT:
				e.gc.drawPoint(x0, y0);
				break;
			case LINE:
				if (x1 == -1) 
					e.gc.drawPoint(x0, y0);
				else
					e.gc.drawLine(x0, y0, x1, y1);
				break;
			case RECT:
				if (x1 == -1) 
					e.gc.drawPoint(x0, y0);
				else
					e.gc.drawRectangle(x0, y0, x1-x0, y1-y0);
				break;

				
			}
			

		}

		public String toString() {
			switch (mTyp) {
			case POINT:
				return String.format("Point %d %d", x0, y0);
			case LINE:
				return String.format("Line %d %d %d %d", x0, y0, x1, y1);
			case CIRCLE:
				return String.format("Circle %d %d %d", x0, y0, Math.abs(x0 - x1));
			case RECT:
				return String.format("Rectangle %d %d %d %d", x0, y0, x1, y1);
			case FILL:
				return String.format("Fill %d %d", x0, y0);
			case COLOR:
				return String.format("Color %d", x0);
			default:
				return mTyp.toString();
			}

		}

		public Point addCode(StringBuilder sb, Point lastPoint) {
			if (lastPoint == null) 
				lastPoint = new Point(-1, -1);
		
			switch(mTyp) {
			case POINT:
					sb.append(String.format("    TYP_POINT %d %d\n", x0, y0));
					return new Point(x0,x1);
			case LINE:
					if (lastPoint.x != x0 || lastPoint.y != y0) {
						sb.append(String.format("    TYP_POINT %d %d\n", x0, y0));
					}
					sb.append(String.format("    TYP_LINE %d %d\n", x1, y1));
					return new Point(x1, y1);
			case RECT:
				if (lastPoint.x != x0 || lastPoint.y != y0) {
					sb.append(String.format("    TYP_POINT %d %d\n", x0, y0));
				}
				sb.append(String.format("    TYP_RECT %d %d\n", x1, y1));
				return new Point(x1, y1);
			case CIRCLE:
				sb.append(String.format("    TYP_CIRCLE %d %d %d\n", x0, y0, x1-x0));
				return new Point(x0, y0);
			case FILL:
				sb.append(String.format("    TYP_FILL %d %d\n", x0, y0));
				return null;
			case COLOR:
				sb.append(String.format("    TYP_COLOR %d\n", x0));
				return null;
			case HIRES:
				sb.append(String.format("    TYP_HIRES\n"));
				return null;
			case LORES:
				sb.append(String.format("    TYP_LORES\n"));
				return null;
			case CLEAR:
				sb.append(String.format("    TYP_CLEAR\n"));
				return null;
			case STOP:
				sb.append(String.format("    TYP_STOP\n"));
				return null;
					
			}
			
			// TODO Auto-generated method stub
			return null;
		}

	}

	private CDrawElement mCurrentDrawElement = null;
	private int mCanvasW = 128;
	private int mCanvasH = 64;
	private int mColors = 2;
	private Composite mCanvasProperties;
	protected Object result;
	protected Shell shlVectorDraw;
	private Text mTextOutput;
	Canvas mCanvas;
	ArrayList<CDrawElement> mDrawElements = new ArrayList<>();
	protected int mnPoint=-1;
	private Transform transform;
	private float mScaleW = 0;
	private float mScaleH = 0;
	private List mListCommands;
	private CDrawElement mLastElem=null;;
	private Text mTexty0;
	private Text mTextx0;
	private Text mTextx1;
	private Text mTexty1;
	private boolean mnAutoPoint=true;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public CDialogVectorDraw(Shell parent, int style) {
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
		shlVectorDraw.open();
		shlVectorDraw.layout();
		parse(mTextOutput.getText());
		Display display = getParent().getDisplay();
		while (!shlVectorDraw.isDisposed()) {
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
		shlVectorDraw = new Shell(getParent(), getStyle());
		shlVectorDraw.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				onResize();
			}
		});
		shlVectorDraw.setSize(829, 523);
		shlVectorDraw.setText("Vector Draw");

		mCanvas = new Canvas(shlVectorDraw, SWT.BORDER);
		mCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				onPaint(e);
			}
		});
		mCanvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				onMouseUp(e);
			}
		});
		mCanvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				onMouseMove(e);
			}
		});
		mCanvas.setBounds(98, 36, 492, 309);

		Label lblResolution = new Label(shlVectorDraw, SWT.NONE);
		lblResolution.setBounds(10, 7, 55, 15);
		lblResolution.setText("Resolution");

		Combo mComboReslution = new Combo(shlVectorDraw, SWT.NONE);
		mComboReslution.setItems(new String[] { "64x32", "128x64", "256x192" });
		mComboReslution.setBounds(71, 4, 75, 23);
		mComboReslution.setText("128x64");

		Label lblColors = new Label(shlVectorDraw, SWT.NONE);
		lblColors.setBounds(152, 7, 34, 15);
		lblColors.setText("Colors");

		Combo mComboColor = new Combo(shlVectorDraw, SWT.NONE);
		mComboColor.setItems(new String[] { "2", "4", "256" });
		mComboColor.setBounds(192, 4, 55, 23);
		mComboColor.setText("2");

		mListCommands = new List(shlVectorDraw, SWT.BORDER);
		mListCommands.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setCurrentDrawElement(mDrawElements.get(mListCommands.getSelectionIndex()));
			}
		});
		mListCommands.setBounds(596, 36, 207, 377);

		Button btnPoint = new Button(shlVectorDraw, SWT.NONE);
		btnPoint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mCurrentDrawElement = new CDrawElement(Typ.POINT);
				mnPoint = 0;
				mnAutoPoint = true;
				mLastElem = mCurrentDrawElement;
			}
		});
		btnPoint.setBounds(10, 36, 75, 25);
		btnPoint.setText("Point");

		Button btnLine = new Button(shlVectorDraw, SWT.NONE);
		btnLine.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mCurrentDrawElement = new CDrawElement(Typ.LINE);
				mLastElem = mCurrentDrawElement;
				mnAutoPoint = true;
				mnPoint = 0;
			}
		});
		btnLine.setBounds(10, 67, 75, 25);
		btnLine.setText("Line");

		Button btnCircle = new Button(shlVectorDraw, SWT.NONE);
		btnCircle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mCurrentDrawElement = new CDrawElement(Typ.CIRCLE);
				mLastElem = mCurrentDrawElement;
				mnAutoPoint = true;
				mnPoint = 0;

			}
		});
		btnCircle.setBounds(10, 98, 75, 25);
		btnCircle.setText("Circle");

		Button btnRectangle = new Button(shlVectorDraw, SWT.NONE);
		btnRectangle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mCurrentDrawElement = new CDrawElement(Typ.RECT);
				mLastElem = mCurrentDrawElement;
				mnAutoPoint = true;
				mnPoint = 0;

			}
		});
		btnRectangle.setBounds(10, 129, 75, 25);
		btnRectangle.setText("Rectangle");

		Button btnFill = new Button(shlVectorDraw, SWT.NONE);
		btnFill.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mCurrentDrawElement = new CDrawElement(Typ.FILL);
				mLastElem = mCurrentDrawElement;
				mnAutoPoint = true;
				mnPoint = 0;

			}
		});
		btnFill.setBounds(10, 160, 75, 25);
		btnFill.setText("Fill");

		Button btnColor = new Button(shlVectorDraw, SWT.NONE);
		btnColor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mCurrentDrawElement = new CDrawElement(Typ.COLOR);
				mLastElem = mCurrentDrawElement;
				mnAutoPoint = true;
				mnPoint = 0;

			}
		});
		btnColor.setBounds(10, 191, 75, 25);
		btnColor.setText("Color");

		mTextOutput = new Text(shlVectorDraw, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		mTextOutput.setBounds(10, 357, 580, 124);
		
		Button btnParse = new Button(shlVectorDraw, SWT.NONE);
		btnParse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				parse(mTextOutput.getText());
			}
		});
		btnParse.setBounds(386, 2, 60, 25);
		btnParse.setText("Parse");
		
		Button btnRun = new Button(shlVectorDraw, SWT.NONE);
		btnRun.setBounds(452, 2, 60, 25);
		btnRun.setText("Run");
		
		Label lblLoad = new Label(shlVectorDraw, SWT.NONE);
		lblLoad.setBounds(253, 7, 34, 15);
		lblLoad.setText("Load");
		
		Combo combo = new Combo(shlVectorDraw, SWT.NONE);
		combo.setBounds(289, 4, 91, 23);
		
		mCanvasProperties = new Composite(shlVectorDraw, SWT.NONE);
		mCanvasProperties.setBounds(596, 417, 207, 64);
		
		mTextx0 = new Text(mCanvasProperties, SWT.BORDER);
		mTextx0.setBounds(48, 7, 40, 21);
		
		Label lblY = new Label(mCanvasProperties, SWT.NONE);
		lblY.setBounds(10, 10, 32, 15);
		lblY.setText("x/y");
		
		mTexty0 = new Text(mCanvasProperties, SWT.BORDER);
		mTexty0.setBounds(94, 7, 40, 21);
		
		Label lblXy = new Label(mCanvasProperties, SWT.NONE);
		lblXy.setText("x1/y1");
		lblXy.setBounds(10, 37, 32, 15);
		
		mTextx1 = new Text(mCanvasProperties, SWT.BORDER);
		mTextx1.setBounds(48, 34, 40, 21);
		
		mTexty1 = new Text(mCanvasProperties, SWT.BORDER);
		mTexty1.setBounds(94, 34, 40, 21);
		
		Button btnSave = new Button(mCanvasProperties, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (mCurrentDrawElement != null) {
					saveCurrentDrawElement();
					UpdateList();
					mCanvas.redraw();
				}
			}


		});
		btnSave.setBounds(140, 7, 57, 25);
		btnSave.setText("Save");
		
		Button btnDelete = new Button(mCanvasProperties, SWT.NONE);
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (mCurrentDrawElement != null) {
					mDrawElements.remove(mCurrentDrawElement);
					mCurrentDrawElement = null;
					UpdateList();
					mCanvas.redraw();
					
				}
			}
		});
		btnDelete.setBounds(140, 34, 57, 25);
		btnDelete.setText("Delete");

	}



	protected void setCurrentDrawElement(CDrawElement cDrawElement) {
		if (mCurrentDrawElement != null) {
			saveCurrentDrawElement();
		}
		mCurrentDrawElement = cDrawElement;
		updateProperties();
		mCanvas.redraw();
		mnPoint = -1;
	}



	private void updateProperties() {
		mTextx0.setText(int2str(mCurrentDrawElement.x0));
		mTexty0.setText(int2str(mCurrentDrawElement.y0));
		mTextx1.setText(int2str(mCurrentDrawElement.x1));
		mTexty1.setText(int2str(mCurrentDrawElement.y1));
	}

	private String int2str(int x0) {
		return String.format("%d", x0);
	}

	protected int readInt(Text text) {
		try {
			return Integer.parseInt(text.getText());
		}
		catch(Exception e) {
			return 0;
		}
	}

	protected void onPaint(PaintEvent e) {
		Rectangle bounds = mCanvas.getBounds();
		float scaleW = bounds.width / mCanvasW;
		float scaleH = bounds.height / mCanvasH;
		


		if (scaleW != mScaleW || scaleH != mScaleH) {
			mScaleW = scaleW;
			mScaleH = scaleH;
			if (transform != null) { // dispose of previous to prevent leaking of handles
				((Resource) transform).dispose();
			}

			transform = new Transform(e.gc.getDevice());
			transform.scale(scaleW, scaleH);
		}
		e.gc.setTransform(transform);
		
		for (CDrawElement elem: mDrawElements) {
			elem.onPaint(e);
		}

		if (mCurrentDrawElement != null) {
			mCurrentDrawElement.onPaint(e);
			mCurrentDrawElement.drawSelection(e);
		}

	}

	protected void onMouseUp(MouseEvent e) {
		Rectangle bounds = mCanvas.getBounds();
		float scaleW = bounds.width / mCanvasW;
		float scaleH = bounds.height / mCanvasH;
		float ptx = e.x / scaleW;
		float pty = e.y / scaleH;
		if (ptx > mCanvasW-1) ptx = mCanvasW-1;
		if (pty > mCanvasH-1) pty = mCanvasH-1;
		
		if (mCurrentDrawElement != null) {
			if (mnPoint == -1) {
				int pt = mCurrentDrawElement.getSelectedPoint((int)ptx, (int)pty);
				if (pt != -1) {
					mnPoint = pt;
					mnAutoPoint = false;
					return;
				}
			}
			if (mnPoint+1 >= mCurrentDrawElement.getNumPoints()) {
				mDrawElements.add(mCurrentDrawElement);
				mCurrentDrawElement = null;
				mnPoint = 0;
				if (mLastElem != null) {
					if (mLastElem.mTyp == Typ.POINT) {
						mCurrentDrawElement = new CDrawElement(Typ.POINT);
						mnAutoPoint = true;
					}
					else if (mLastElem.mTyp == Typ.LINE) {
						mCurrentDrawElement = new CDrawElement(Typ.LINE);
						mCurrentDrawElement.x0 = mLastElem.x1;
						mCurrentDrawElement.y0 = mLastElem.y1;
						mLastElem = mCurrentDrawElement;
						mnPoint=1;
						
					}
				}
				
				UpdateList();
			} else {
				if (mnAutoPoint)
					mnPoint++;
				else {
					UpdateList();
					updateProperties();
					mnPoint = -1;
				}
			}
		}

	}
	
	private void saveCurrentDrawElement() {
		mCurrentDrawElement.x0 = readInt(mTextx0);
		mCurrentDrawElement.y0 = readInt(mTexty0);
		mCurrentDrawElement.x1 = readInt(mTextx1);
		mCurrentDrawElement.y1 = readInt(mTexty1);
	}

	protected void onMouseMove(MouseEvent e) {
		Rectangle bounds = mCanvas.getBounds();
		float scaleW = bounds.width / mCanvasW;
		float scaleH = bounds.height / mCanvasH;
		float ptx = e.x / scaleW;
		float pty = e.y / scaleH;
		if (ptx > mCanvasW-1) ptx = mCanvasW-1;
		if (pty > mCanvasH-1) pty = mCanvasH-1;

		if (mCurrentDrawElement != null) {
			mCurrentDrawElement.setPoint(mnPoint, (int) ptx, (int) pty);
			mCanvas.redraw();
		}
	}
	
	protected void UpdateList() {
		int row=0;
		Point lastPoint = null;
		StringBuilder sb = new StringBuilder();
		if (mListCommands.getItemCount() > mDrawElements.size())
			mListCommands.removeAll();
		for (CDrawElement elem: mDrawElements) {
			if (mListCommands.getItemCount() > row) {
				mListCommands.setItem(row,  elem.toString());
			} else
				mListCommands.add(elem.toString());
			
			lastPoint = elem.addCode(sb, lastPoint);
			
			row++;
	
		}
		mTextOutput.setText(sb.toString());
		
		
	}
	
	protected void parse(String text) {
		mListCommands.removeAll();
		mDrawElements.clear();
		CTokenizer tokenizer = new CTokenizer();
		CToken token = new CToken();
		tokenizer.start(mTextOutput.getText());
		CDrawElement elem=null, newElem;
		int x=-1, y;
		int x1, y1;
		
		while (tokenizer.hasData()) {
			tokenizer.getToken(token);
			if (token.token == Token.literal) {
				if (token.literal.compareTo("TYP_HIRES") == 0) {
					mDrawElements.add(new CDrawElement(Typ.HIRES));
				} else if (token.literal.compareTo("TYP_STOP") == 0) {
					mDrawElements.add(new CDrawElement(Typ.STOP));
				} else if (token.literal.compareTo("TYP_CLEAR") == 0) {
					mDrawElements.add(new CDrawElement(Typ.CLEAR));
				} else if (token.literal.compareTo("TYP_LORES") == 0) {
					mDrawElements.add(new CDrawElement(Typ.LORES));
				} else if (token.literal.compareTo("TYP_POINT") == 0) {
					tokenizer.getToken(token);
					x = token.iliteral;
					tokenizer.getToken(token);
					y = token.iliteral;
					elem = new CDrawElement(Typ.POINT);
					elem.x0 = x;
					elem.y0 = y;
					mDrawElements.add(elem);
				} else if (token.literal.compareTo("TYP_LINE") == 0) {
					tokenizer.getToken(token);
					x = token.iliteral;
					tokenizer.getToken(token);
					y = token.iliteral;
					elem = getLastElem();
					if (elem.mTyp == Typ.POINT) {
						elem.mTyp = Typ.LINE;
						elem.x1 = x;
						elem.y1 = y;
					} else if (elem.mTyp == Typ.LINE || elem.mTyp == Typ.RECT) {
						newElem = new CDrawElement(Typ.LINE);
						newElem.x0 = elem.x1;
						newElem.y0 = elem.y1;
						newElem.y1 = y;
						newElem.x1 = x;
						mDrawElements.add(newElem);
					} else if (elem.mTyp == Typ.CIRCLE || elem.mTyp == Typ.FILL) {
						newElem = new CDrawElement(Typ.LINE);
						newElem.x0 = elem.x0;
						newElem.y0 = elem.y0;
						newElem.y1 = y;
						newElem.x1 = x;
						mDrawElements.add(newElem);
					}
				} else if (token.literal.compareTo("TYP_FILL") == 0) {
					tokenizer.getToken(token);
					x = token.iliteral;
					tokenizer.getToken(token);
					y = token.iliteral;
					elem = new CDrawElement(Typ.FILL);
					elem.x0 = x;
					elem.y0 = y;
					mDrawElements.add(elem);
					
				}
			}
		}
		UpdateList();
		mCanvas.redraw();
		
	}

	private CDrawElement getLastElem() {
		if (mDrawElements.size() == 0) return new CDrawElement(Typ.NONE);
		return mDrawElements.get(mDrawElements.size()-1);
	}
	
	protected void onResize() {
		if (mCanvas == null) return;
		Rectangle bounds = shlVectorDraw.getBounds();
		Rectangle rectCanvas = mCanvas.getBounds();
		Rectangle rectProperty = mCanvasProperties.getBounds();
		Rectangle rectText = mTextOutput.getBounds();
		Rectangle rectList = mListCommands.getBounds();
		int x1 = bounds.width-20;
		int y1 = bounds.height-40;
		mCanvasProperties.setBounds(x1 - rectProperty.width, y1 - rectProperty.height, rectProperty.width, rectProperty.height);
		mListCommands.setBounds(x1 - rectList.width, rectList.y, rectList.width, y1-rectText.height-40);
		mTextOutput.setBounds(rectText.x, y1-rectText.height, x1-rectProperty.width-20, rectText.height);
		mCanvas.setBounds(rectCanvas.x, rectCanvas.y, x1 - rectList.width-100, y1-rectText.height-40 );
		
	}
}
