package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Canvas;

import java.util.ArrayList;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import assembler.CToken;
import assembler.CTokenizer;
import assembler.Token;

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

interface CDrawElementContainer {
	void addBytes(int bytes);
}
public class CDialogVectorDraw extends Dialog implements CDrawElementContainer {

	CDrawElementContainer getContainer() { return this; }
	
	static public enum Typ {
		NONE, POINT, LINE, RECT, CIRCLE, FILL, COLOR, HIRES, STOP, CLEAR, LORES, TEXT, ERASELINE, XORLINE, ERASERECT,
		XORRECT, ERASECIRCLE, XORCIRCLE, POLYLINE, POLYPOINT
	}

	static public enum Mode {
		XOR, DRAW, ERASE
	}

	static public Object AllTokens[][] = { { "TYP_NONE", Typ.NONE }, { "TYP_POINT", Typ.POINT },
			{ "TYP_LINE", Typ.LINE }, { "TYP_RECT", Typ.RECT }, { "TYP_CIRCLE", Typ.CIRCLE }, { "TYP_FILL", Typ.FILL },
			{ "TYP_COLOR", Typ.COLOR }, { "TYP_HIRES", Typ.HIRES }, { "TYP_STOP", Typ.STOP },
			{ "TYP_CLEAR", Typ.CLEAR }, { "TYP_LORES", Typ.LORES }, { "TYP_TEXT", Typ.TEXT },
			{ "TYP_ERASELINE", Typ.ERASELINE }, { "TYP_XORLINE", Typ.XORLINE }, { "TYP_XORRECT", Typ.XORRECT },
			{ "TYP_ERASECIRCLE", Typ.ERASECIRCLE }, { "TYP_XORCIRCLE", Typ.XORCIRCLE, "TYP_POLYLINE", Typ.POLYLINE, 
				"TYP_POLYPOINT", Typ.POLYPOINT}

	};

	TreeMap<String, Typ> mMapTypToInt = null;
	public int mBytes;

	public static class CDrawElement {

		public Typ mTyp;
		public Mode mMode;
		public int x0, y0;
		public int x1 = 1, y1;
		protected String mString;
		private int mBytes;
		private CDrawElementContainer mParent;
		public ArrayList<Point> mPolypoints = null;
		public static String mAlphabet = "#|ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.!?,'-+/*= ;";
		
		public CDrawElement clone() {
			CDrawElement result = new CDrawElement(mParent, mTyp);
			result.x0 = x0;
			result.x1 = x1;
			result.y0 = y0;
			result.y1 = y1;
			result.mString = mString;
			return result;
		}

		boolean addPolypoint(CDrawElement elem) {
			boolean result=false;
			if (elem.mTyp == Typ.LINE && (mTyp == Typ.LINE || mTyp == Typ.POLYLINE)) {
				Point pt = lastPoint();
				if (pt.x == elem.x0 && pt.y == elem.y0) {
					addPolypoint(elem.x1, elem.y1);
					return true;
				}
				
			}
			if (elem.mTyp == Typ.POINT && (mTyp == Typ.POINT || mTyp == Typ.POLYPOINT)) {
					addPolypoint(elem.x0, elem.y0);
					return true;
			}
			return false;
		}
		
		private Point lastPoint() {
			if (mPolypoints == null) return new Point(x1,y1);
			else return mPolypoints.get(mPolypoints.size()-1);
		}

		private void addPolypoint(int x, int y) {
			if (mPolypoints == null) {
				mPolypoints = new ArrayList<>();
				if (mTyp == Typ.LINE) mTyp = Typ.POLYLINE;
				if (mTyp == Typ.POINT) mTyp = Typ.POLYPOINT;
			}
			mPolypoints.add(new Point(x, y));
		}

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

		CDrawElement(CDrawElementContainer parent) {
			mParent = parent;
			this.mTyp = Typ.POINT;

		}

		CDrawElement(CDrawElementContainer parent, Typ typ) {
			mParent = parent;
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
				drawRect(e, x0, y0);
			}
			if (num >= 2) {
				drawRect(e, x1, y1);

			}
		}

		private void drawRect(PaintEvent e, int x, int y) {
			e.gc.drawRectangle(x - 2, y - 2, 4, 4);

		}

		public boolean inRect(int x0, int y0, int x, int y) {
			if (x >= x0 - 2 && x <= x0 + 2 && y >= y0 - 2 && y <= x0 + 2)
				return true;
			return false;
		}

		int getSelectedPoint(int x, int y) {
			if (inRect(x0, y0, x, y))
				return 0;
			if (inRect(x1, y1, x, y))
				return 1;
			return -1;
		}

		public void onPaint(PaintEvent e) {
			int radius;
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
					e.gc.drawRectangle(x0, y0, x1 - x0, y1 - y0);
				break;
			case CIRCLE:
				radius = x1-x0;
				if (x1 == -1)
					e.gc.drawPoint(x0, y0);
				else
					e.gc.drawOval(x0-radius/2, y0-radius/2	, x1-x0, x1-x0);
				break;
			case TEXT:
				if (mString != null) {
					FontData fd = new FontData();
					fd.setName("Courier new");
					fd.setHeight(8);
					e.gc.setFont(new Font(e.gc.getDevice(), fd));
					int x = x0;
					int y = y0;
					String[] lines = mString.split("\n");
					for (String line: lines) {
						e.gc.drawText(line, x, y);
						y += 8;
					}
				
				}
			}
				

		}

		public String toString() {
			switch (mTyp) {
			case POINT:
				return String.format("Point %d %d", x0, y0);
			case LINE:
				return String.format("Line %d %d %d %d", x0, y0, x1, y1);
			case CIRCLE:
				return String.format("Circle %d %d %d", x0, y0, Math.abs(x0 - x1)/2);
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

			switch (mTyp) {
			case POINT:
				sb.append(String.format("    TYP_POINT %d %d\n", x0, y0));
				mParent.addBytes(3);
				return new Point(x0, x1);
			case POLYPOINT:
				sb.append(String.format("    TYP_POLYPOINT %d ",mPolypoints.size()+1));
				mParent.addBytes(4);
				sb.append(String.format("%d %d ", (byte)(x0 & 0xff), (byte)(y0 & 0xff)));
				for (Point pt : mPolypoints) {
					sb.append(String.format("%d %d ", (byte)(pt.x & 0xff), (byte)(pt.y & 0xff)));
					mParent.addBytes(2);
				}
				sb.append("\n");
				break;
			case POLYLINE:
				sb.append(String.format("    TYP_POLYLINE %d ",mPolypoints.size()+1));
				mParent.addBytes(4);
				sb.append(String.format("%d %d ", (byte)(x1 & 0xff), (byte)(y1 & 0xff)));
				for (Point pt : mPolypoints) {
					sb.append(String.format("%d %d ", (byte)(pt.x & 0xff), (byte)(pt.y & 0xff)));
					mParent.addBytes(2);
				}
				sb.append("\n");
				break;
				
			case LINE:
				if (lastPoint.x != x0 || lastPoint.y != y0) {
					sb.append(String.format("    TYP_POINT %d %d\n", x0, y0));
					mParent.addBytes(3);
				}
				sb.append(String.format("    TYP_LINE %d %d\n", x1, y1));
				mParent.addBytes(3);
				return new Point(x1, y1);
			case ERASELINE:
				if (lastPoint.x != x0 || lastPoint.y != y0) {
					sb.append(String.format("    TYP_POINT %d %d\n", x0, y0));
					mParent.addBytes(3);
				}
				mParent.addBytes(3);
				sb.append(String.format("    TYP_ERASELINE %d %d\n", x1, y1));
				return new Point(x1, y1);
			case XORLINE:
				if (lastPoint.x != x0 || lastPoint.y != y0) {
					sb.append(String.format("    TYP_POINT %d %d\n", x0, y0));
					mParent.addBytes(3);
				}
				mParent.addBytes(3);
				sb.append(String.format("    TYP_XORLINE %d %d\n", x1, y1));
				return new Point(x1, y1);
			case RECT:
				if (lastPoint.x != x0 || lastPoint.y != y0) {
					sb.append(String.format("    TYP_POINT %d %d\n", x0, y0));
					mParent.addBytes(3);
				}
				mParent.addBytes(3);
				sb.append(String.format("    TYP_RECT %d %d\n", x1, y1));
				return new Point(x1, y1);
			case XORRECT:
				if (lastPoint.x != x0 || lastPoint.y != y0) {
					sb.append(String.format("    TYP_POINT %d %d\n", x0, y0));
					mParent.addBytes(3);
				}
				mParent.addBytes(3);
				sb.append(String.format("    TYP_XORRECT %d %d\n", x1, y1));
				return new Point(x1, y1);
			case ERASERECT:
				if (lastPoint.x != x0 || lastPoint.y != y0) {
					sb.append(String.format("    TYP_POINT %d %d\n", x0, y0));
					mParent.addBytes(3);
				}
				mParent.addBytes(3);
				sb.append(String.format("    TYP_ERASERECT %d %d\n", x1, y1));
				return new Point(x1, y1);
			case CIRCLE:
				sb.append(String.format("    TYP_CIRCLE %d %d %d\n", x0, y0, Math.abs(x1 - x0)/2));
				mParent.addBytes(4);
				return new Point(x0, y0);
			case XORCIRCLE:
				sb.append(String.format("    TYP_XORCIRCLE %d %d %d\n", x0, y0, x1 - x0));
				mParent.addBytes(4);
				return new Point(x0, y0);
			case ERASECIRCLE:
				sb.append(String.format("    TYP_ERASECIRCLE %d %d %d\n", x0, y0, x1 - x0));
				mParent.addBytes(4);
				return new Point(x0, y0);
			case FILL:
				sb.append(String.format("    TYP_FILL %d %d\n", x0, y0));
				mParent.addBytes(1);
				return null;
			case COLOR:
				sb.append(String.format("    TYP_COLOR %d\n", x0));
				mParent.addBytes(2);
				return null;
			case HIRES:
				sb.append(String.format("    TYP_HIRES\n"));
				mParent.addBytes(1);
				return null;
			case LORES:
				sb.append(String.format("    TYP_LORES\n"));
				mParent.addBytes(1);
				return null;
			case CLEAR:
				sb.append(String.format("    TYP_CLEAR\n"));
				mParent.addBytes(1);
				return null;
			case STOP:
				sb.append(String.format("    TYP_STOP\n"));
				mParent.addBytes(1);
				return null;
			case TEXT:
			{
				String comment = "";
				sb.append(String.format("    TYP_TEXT %d %d ", x0, y0-4));
				mParent.addBytes(3);
				String data = "";
				for (int i=0;i<mString.length();i++) {
					char c = mString.charAt(i);
					if (c == 10) continue;
					if (c == 13) {
						data += "0x00 ";
						mParent.addBytes(1);
					} else {
						int idx = mAlphabet.indexOf(c);
						if (idx != -1) {
							data += String.format("0x%02x ", idx);
							mParent.addBytes(1);
							comment += c;
						}
					}
					
				}
				data += "0x00";
				mParent.addBytes(1);
				sb.append(data+"\t# "+ comment+"\n");
			}
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
	protected int mnPoint = -1;
	private Transform transform;
	private float mScaleW = 0;
	private float mScaleH = 0;
	private List mListCommands;
	private CDrawElement mLastElem = null;;
	private Text mTexty0;
	private Text mTextx0;
	private Text mTextx1;
	private Text mTexty1;
	private boolean mnAutoPoint = true;
	private Label mLblSize;
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
		mListCommands.setBounds(596, 36, 207, 351);

		Button btnPoint = new Button(shlVectorDraw, SWT.NONE);
		btnPoint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mCurrentDrawElement = new CDrawElement(getContainer(), Typ.POINT);
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
				mCurrentDrawElement = new CDrawElement(getContainer(), Typ.LINE);
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
				mCurrentDrawElement = new CDrawElement(getContainer(), Typ.CIRCLE);
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
				mCurrentDrawElement = new CDrawElement(getContainer(), Typ.RECT);
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
				mCurrentDrawElement = new CDrawElement(getContainer(), Typ.FILL);
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
				mCurrentDrawElement = new CDrawElement(getContainer(), Typ.COLOR);
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
		mCanvasProperties.setBounds(596, 393, 207, 88);

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

		Button btnNewButton = new Button(mCanvasProperties, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (mCurrentDrawElement != null) {
					
					CDialogTextBox dlg = new CDialogTextBox(shlVectorDraw, SWT.TITLE);
					dlg.mString = mCurrentDrawElement.mString;
					dlg.open();
					mCurrentDrawElement.mString = dlg.mString;
				}
			}
		});
		btnNewButton.setBounds(140, 61, 57, 25);
		btnNewButton.setText("Text");

		Combo mComboText = new Combo(mCanvasProperties, SWT.NONE);
		mComboText.setBounds(10, 58, 124, 23);

		Button btnText = new Button(shlVectorDraw, SWT.NONE);
		btnText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mCurrentDrawElement = new CDrawElement(getContainer(), Typ.TEXT);
				mCurrentDrawElement.mString = "Text";
			
				mLastElem = mCurrentDrawElement;
				mnAutoPoint = true;
				
				mnPoint = 0;
			}
		});
		btnText.setBounds(10, 222, 75, 25);
		btnText.setText("Text");
		
		Label lblSize = new Label(shlVectorDraw, SWT.NONE);
		lblSize.setBounds(10, 307, 55, 15);
		lblSize.setText("Size");
		
		mLblSize = new Label(shlVectorDraw, SWT.NONE);
		mLblSize.setBounds(10, 328, 55, 15);
		mLblSize.setText("0 Bytes");

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
		} catch (Exception e) {
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

		for (CDrawElement elem : mDrawElements) {
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
		if (ptx > mCanvasW - 1)
			ptx = mCanvasW - 1;
		if (pty > mCanvasH - 1)
			pty = mCanvasH - 1;

		if (mCurrentDrawElement != null) {
			if (mnPoint == -1) {
				int pt = mCurrentDrawElement.getSelectedPoint((int) ptx, (int) pty);
				if (pt != -1) {
					mnPoint = pt;
					mnAutoPoint = false;
					return;
				}
			}
			if (mnPoint + 1 >= mCurrentDrawElement.getNumPoints()) {
				mDrawElements.add(mCurrentDrawElement);
				mCurrentDrawElement = null;
				mnPoint = 0;
				if (mLastElem != null) {
					if (mLastElem.mTyp == Typ.POINT) {
						mCurrentDrawElement = new CDrawElement(this, Typ.POINT);
						mnAutoPoint = true;
					} else if (mLastElem.mTyp == Typ.LINE) {
						mCurrentDrawElement = new CDrawElement(this, Typ.LINE);
						mCurrentDrawElement.x0 = mLastElem.x1;
						mCurrentDrawElement.y0 = mLastElem.y1;
						mLastElem = mCurrentDrawElement;
						mnPoint = 1;

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
		if (ptx > mCanvasW - 1)
			ptx = mCanvasW - 1;
		if (pty > mCanvasH - 1)
			pty = mCanvasH - 1;

		if (mCurrentDrawElement != null) {
			mCurrentDrawElement.setPoint(mnPoint, (int) ptx, (int) pty);
			mCanvas.redraw();
		}
	}

	protected void UpdateList() {
		int row = 0;
		Point lastPoint = null;
		StringBuilder sb = new StringBuilder();
		mBytes = 0;
		ArrayList<CDrawElement> newList = new ArrayList<>();
		CDrawElement prev = null;
		if (mListCommands.getItemCount() > mDrawElements.size())
		mListCommands.removeAll();		
		for (CDrawElement elem: mDrawElements) {
			if (mListCommands.getItemCount() > row) {
				mListCommands.setItem(row, elem.toString());
			} else
				mListCommands.add(elem.toString());
			row++;
			CDrawElement newElem = elem.clone();
			if (prev == null) {
				prev = newElem;
				newList.add(newElem);
			} else {
				if (prev.addPolypoint(newElem))	continue;
				prev = newElem;
				newList.add(newElem);
			}
		}
		
		

		for (CDrawElement elem : newList) {

			lastPoint = elem.addCode(sb, lastPoint);

			

		}
		mLblSize.setText(String.format("%d bytes", mBytes));
		mTextOutput.setText(sb.toString());

	}

	protected void parse(String text) {
		initMapTyp();
		mListCommands.removeAll();
		mDrawElements.clear();
		CTokenizer tokenizer = new CTokenizer();
		CToken token = new CToken();
		tokenizer.start(mTextOutput.getText());
		CDrawElement newElem;
		int x=-1, y;
		int x1, y1;
		int count;
		
		while (tokenizer.hasData()) {
			tokenizer.getToken(token);
			if (token.token == Token.literal) {
				Typ typ = mMapTypToInt.get(token.literal);
				if (typ == null) continue;
				
				switch(typ) {
					case HIRES:
						mDrawElements.add(new CDrawElement(this, Typ.HIRES));
						break;
					case STOP:
						mDrawElements.add(new CDrawElement(this, Typ.STOP));
						break;
					case CLEAR:
						mDrawElements.add(new CDrawElement(this, Typ.CLEAR));
						break;
					case LORES:
						mDrawElements.add(new CDrawElement(this, Typ.LORES));
						break;
					case POINT:
						tokenizer.getToken(token);
						x = token.iliteral;
						tokenizer.getToken(token);
						y = token.iliteral;
						newElem = new CDrawElement(this, Typ.POINT);
						newElem.x0 = x;
						newElem.y0 = y;
						mDrawElements.add(newElem);
						break;
					case LINE:
					case XORLINE:
					case ERASELINE:
						newElem = new CDrawElement(this, Typ.LINE);
						setx1y1(tokenizer, token, newElem);
						newElem = setx2y2(newElem);
						mDrawElements.add(newElem);
						switch(typ) {
							case LINE: newElem.mMode = Mode.DRAW; break;
							case XORLINE: newElem.mMode = Mode.XOR; break;
							case ERASELINE: newElem.mMode = Mode.ERASE; break;
						}
						break;
					case POLYLINE:
						count = nextNumber(tokenizer, token);
						for (int i=0;i<count;i++) {
							newElem = new CDrawElement(this, Typ.LINE);
							setx1y1(tokenizer, token, newElem);
							newElem = setx2y2(newElem);
							mDrawElements.add(newElem);							
						}
					case POLYPOINT:
						count = nextNumber(tokenizer, token);
						for (int i=0;i<count;i++) {
							newElem = new CDrawElement(this, Typ.POINT);
							setx1y1(tokenizer, token, newElem);
							mDrawElements.add(newElem);							
						}
						
						
					case RECT:
					case XORRECT:
					case ERASERECT:
						newElem = new CDrawElement(this, Typ.RECT);
						setx1y1(tokenizer, token, newElem);
						newElem = setx2y2(newElem);
						mDrawElements.add(newElem);
						switch(typ) {
							case RECT: newElem.mMode = Mode.DRAW; break;
							case XORRECT: newElem.mMode = Mode.XOR; break;
							case ERASERECT: newElem.mMode = Mode.ERASE; break;
						}
						break;

					case CIRCLE:
					case XORCIRCLE:
					case ERASECIRCLE:
						newElem = new CDrawElement(this, Typ.CIRCLE);
						setx1y1(tokenizer, token, newElem);
						mDrawElements.add(newElem);
						tokenizer.getToken(token);
						newElem.x1 = token.iliteral;

						switch(typ) {
							case CIRCLE: newElem.mMode = Mode.DRAW; break;
							case XORCIRCLE: newElem.mMode = Mode.XOR; break;
							case ERASECIRCLE: newElem.mMode = Mode.ERASE; break;
						}
						break;
						
					case FILL:
						tokenizer.getToken(token);
						x = token.iliteral;
						tokenizer.getToken(token);
						y = token.iliteral;
						newElem = new CDrawElement(this, Typ.FILL);
						newElem.x0 = x;
						newElem.y0 = y;
						mDrawElements.add(newElem);
						break;
					case TEXT:
						newElem = new CDrawElement(this, Typ.TEXT);
						mDrawElements.add(newElem);
						tokenizer.getToken(token);
						newElem.x0 = token.iliteral;
						tokenizer.getToken(token);
						newElem.y0 = token.iliteral-4;
						newElem.mString = "";
						while (tokenizer.hasData()) {
							tokenizer.getToken(token);
							if (token.token == Token.comment || token.token == Token.newline) continue;
							if (token.token == Token.number) {
								if (token.iliteral == 0) break;
							}
							if (token.iliteral < CDrawElement.mAlphabet.length()) {
								newElem.mString += CDrawElement.mAlphabet.charAt(token.iliteral);
							
							}
							
						}
						break;
					
				}
						
			
			}
		}
		UpdateList();
		mCanvas.redraw();
		
	}

	private int nextNumber(CTokenizer tokenizer, CToken token) {
		while (tokenizer.hasData()) {
			tokenizer.getToken(token);
			if (token.token == Token.number) return token.iliteral;
		}
		return 0;
	}

	private CDrawElement setx2y2(CDrawElement newElem) {
		
		CDrawElement elem = getLastElem();
		if (elem.mTyp == Typ.POINT) {
//			elem.mTyp = Typ.LINE;
			newElem.y1 = newElem.y0;
			newElem.x1 = newElem.x0;
			newElem.x0 = elem.x0;
			newElem.y0 = elem.y0;
		} else if (elem.mTyp == Typ.LINE || elem.mTyp == Typ.RECT) {
			newElem.y1 = newElem.y0;
			newElem.x1 = newElem.x0;
			newElem.x0 = elem.x1;
			newElem.y0 = elem.y1;
		} else if (elem.mTyp == Typ.CIRCLE || elem.mTyp == Typ.FILL) {
			newElem = new CDrawElement(this, Typ.LINE);
			newElem.y1 = newElem.y0;
			newElem.x1 = newElem.x0;
			newElem.x0 = elem.x0;
			newElem.y0 = elem.y0;
		}
		return newElem;
	}

	private void setx1y1(CTokenizer tokenizer, CToken token, CDrawElement elem) {
		
		elem.x0= nextNumber(tokenizer, token);
		elem.y0 = nextNumber(tokenizer, token);
	}

	private void initMapTyp() {
		if (mMapTypToInt == null) {
			mMapTypToInt = new TreeMap<>();
			for (Object[] token : AllTokens) {
				mMapTypToInt.put((String) token[0], (Typ) token[1]);
			}
		}

	}

	private CDrawElement getLastElem() {
		if (mDrawElements.size() == 0)
			return new CDrawElement(this, Typ.NONE);
		return mDrawElements.get(mDrawElements.size() - 1);
	}

	protected void onResize() {
		if (mCanvas == null)
			return;
		Rectangle bounds = shlVectorDraw.getBounds();
		Rectangle rectCanvas = mCanvas.getBounds();
		Rectangle rectProperty = mCanvasProperties.getBounds();
		Rectangle rectText = mTextOutput.getBounds();
		Rectangle rectList = mListCommands.getBounds();
		int x1 = bounds.width - 20;
		int y1 = bounds.height - 40;
		mCanvasProperties.setBounds(x1 - rectProperty.width, y1 - rectProperty.height, rectProperty.width,
				rectProperty.height);
		mListCommands.setBounds(x1 - rectList.width, rectList.y, rectList.width, y1 - rectText.height - 40);
		mTextOutput.setBounds(rectText.x, y1 - rectText.height, x1 - rectProperty.width - 20, rectText.height);
		mCanvas.setBounds(rectCanvas.x, rectCanvas.y, x1 - rectList.width - 100, y1 - rectText.height - 40);

	}

	@Override
	public void addBytes(int bytes) {
		mBytes+=bytes;
		
	}
}
