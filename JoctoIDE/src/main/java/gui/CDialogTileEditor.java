package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;

import java.io.File;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import assembler.CToken;
import assembler.CTokenizer;
import assembler.Token;
import ide.CCallback;
import ide.CMainMenus;
import spiteed.CResources;
import spiteed.CSpriteData;
import spiteed.CTileData;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.wb.swt.SWTResourceManager;

public class CDialogTileEditor extends Dialog {

	protected Object result;
	protected Shell shell;
	private Text mTextIcons;
	private Text mTextTiles;
	private Combo mComboTileset;
	private Combo mComboSpriteSize;
	private Combo mComboZoom;
	private Button mBtnRLE;
	private TabFolder tabFolder;
	private int mTilesetIndex = -1;
	private int[] tilesetBytes;
	Canvas mCanvasTilePicture;
	Canvas mCanvasTileset;
	Canvas mCanvasPreview;
	ScrolledComposite mScrolledComponent;
	private int[] mTileData = new int[64 * 64];
	public CResources mResources = new CResources();
	private boolean mParse;
	int mSpriteSizeBytes = 0;
	ArrayList<SpriteLocation> mListSpriteLocations = new ArrayList<>();
	private SpriteLocation mCurrentSprite = null;
	private int mSpriteSelected = -1;
	private int mSpritePointed = -1;
	private int mSpriteSelectedX = -1;
	private int mSpriteSelectedY = -1;
	private int mTileSizeW, mTileSizeH;
	private String mTileSetLabel = "";

	Combo mComboW;
	Combo mComboH;
	protected int mNTilesW;
	protected int mNTilesH;
	private int mScreenW, mScreenH;
	protected boolean mTilesUpdate = false;
	private Combo mComboTileMap;
	private CSpriteData mCurrentTileMap = null;
	private CMainMenus mMainMenus;
	private CSpriteData mSpriteSet = null;

	public void readSourcefile(StyledText editor) {
		mResources = new CResources();
		mResources.readSourcecode(editor);
	}

	class SpriteLocation {
		public SpriteLocation(int x2, int y2, int w2, int h2, int index) {
			x = x2;
			y = y2;
			w = w2;
			h = h2;
			ofs = index;
		}

		int x, y, w, h; // screen coordinates in tiles window
		int ofs; // byte offset in tilesetBytes;
	}

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public CDialogTileEditor(Shell parent, int style) {
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
		shell.open();
		fillCombo();
		updateText();
		calcScreenDim();
		createMenu();

		shell.layout();
		mCanvasTilePicture.redraw();
		mCanvasTileset.redraw();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private void fillCombo() {
		for (int i = 1; i < 16; i++) {
			mComboSpriteSize.add(String.format("8x%d", i));
		}
		mTileSizeW = 8;
		mTileSizeH = 8;
		for (int i = 4; i < 32; i++) {
			String s = String.format("%d", i);
			mComboW.add(s);
			mComboH.add(s);
			if (i < 8)
				mComboZoom.add(s);
		}
		mComboZoom.setText("4");
		mComboW.setText("16");
		mComboH.setText("8");
		mComboSpriteSize.add("16x16");
		mComboSpriteSize.setText("8x8");

		for (CSpriteData data : mResources.mSprites) {
			mComboTileset.add(data.toString());
		}
		for (CSpriteData data : mResources.mTilesets) {
			mComboTileMap.add(data.toString());
		}

	}

	void updateText() {
		mParse = false;
		String text = createTilesText();
		mTextIcons.setText(text);
		;
		mParse = true;
	}

	String createTilesText() {

		return "";
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
		shell.setSize(979, 673);
		shell.setText(getText());

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBounds(10, 0, 942, 43);

		Label lblWidth = new Label(composite, SWT.NONE);
		lblWidth.setBounds(10, 18, 40, 15);
		lblWidth.setText("Width");

		Label lblHeight = new Label(composite, SWT.NONE);
		lblHeight.setBounds(96, 18, 40, 15);
		lblHeight.setText("Height");

		Label lblTiles = new Label(composite, SWT.NONE);
		lblTiles.setBounds(370, 18, 35, 18);
		lblTiles.setText("Tiles");

		Label lblSprites = new Label(composite, SWT.NONE);
		lblSprites.setBounds(188, 18, 40, 15);
		lblSprites.setText("Sprites");

		mComboSpriteSize = new Combo(composite, SWT.NONE);
		mComboSpriteSize.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				mTileSizeW = getTileW();
				mTileSizeH = getTileH();
				if (mCanvasTilePicture != null)
					mCanvasTilePicture.redraw();
				if (mCanvasTileset != null)
					mCanvasTileset.redraw();
			}
		});
		mComboSpriteSize.setBounds(228, 14, 55, 23);

		Label lblSpriteSet = new Label(composite, SWT.NONE);
		lblSpriteSet.setBounds(504, 18, 55, 15);
		lblSpriteSet.setText("Sprite set");

		mComboTileset = new Combo(composite, SWT.NONE);
		mComboTileset.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				onModifyComboTileset(e);
			}
		});
		mComboTileset.setBounds(565, 15, 100, 23);

		mComboW = new Combo(composite, SWT.NONE);
		mComboW.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				calcScreenDim();
				mCanvasTilePicture.redraw();
			}
		});
		mComboW.setBounds(56, 15, 40, 23);

		mComboH = new Combo(composite, SWT.NONE);
		mComboH.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				calcScreenDim();
				mCanvasTilePicture.redraw();
			}
		});
		mComboH.setBounds(142, 15, 40, 23);

		mComboTileMap = new Combo(composite, SWT.NONE);
		mComboTileMap.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				onModifyComboTileMap(e);
			}
		});
		mComboTileMap.setBounds(408, 15, 92, 28);

		Label lblZoom = new Label(composite, SWT.NONE);
		lblZoom.setBounds(289, 18, 40, 15);
		lblZoom.setText("Zoom");

		mComboZoom = new Combo(composite, SWT.NONE);
		mComboZoom.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (mCanvasTilePicture != null) {
					calcScreenDim();
					mCanvasTilePicture.redraw();
				}

			}
		});
		mComboZoom.setBounds(324, 15, 40, 23);
		mComboZoom.setText("8");

		mBtnRLE = new Button(composite, SWT.CHECK);
		mBtnRLE.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				writeTileset();
			}
		});
		mBtnRLE.setBounds(825, 17, 107, 16);
		mBtnRLE.setText("RLE Compress");

		Button btnSave = new Button(composite, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSave();
			}
		});
		btnSave.setBounds(751, 12, 55, 25);
		btnSave.setText("Save");

		tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.setBounds(10, 453, 952, 172);

		TabItem tbtmIcons = new TabItem(tabFolder, SWT.NONE);
		tbtmIcons.setText("Icons");

		mTextIcons = new Text(tabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		mTextIcons.setFont(SWTResourceManager.getFont("Courier New", 9, SWT.NORMAL));
		mTextIcons.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				onUpdateEditSprites();
			}
		});
		tbtmIcons.setControl(mTextIcons);

		TabItem tbtmTiles = new TabItem(tabFolder, SWT.NONE);
		tbtmTiles.setText("Tiles");

		mTextTiles = new Text(tabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		mTextTiles.setFont(SWTResourceManager.getFont("Courier New", 9, SWT.NORMAL));
		mTextTiles.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (mTilesUpdate == false) {
					onUpdateEditTiles();
				}
			}
		});
		tbtmTiles.setControl(mTextTiles);

		mCanvasTileset = new Canvas(shell, SWT.BORDER);
		mCanvasTileset.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				onMouseUpTile(e);
			}
		});
		mCanvasTileset.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				onMouseMoveTile(e);
			}
		});
		mCanvasTileset.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				onPaintTileSet(e);
			}
		});
		mCanvasTileset.setBounds(638, 128, 310, 319);

		mCanvasPreview = new Canvas(shell, SWT.BORDER);
		mCanvasPreview.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				onRepaintCanvasPreview(e);
			}
		});
		mCanvasPreview.setBounds(643, 58, 64, 64);

		mScrolledComponent = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		mScrolledComponent.setBounds(10, 49, 622, 398);
		mScrolledComponent.setExpandHorizontal(true);
		mScrolledComponent.setExpandVertical(true);

		mCanvasTilePicture = new Canvas(mScrolledComponent, SWT.NONE);
		// mScrolledComponent.setMinSize(mCanvasTilePicture.computeSize(SWT.DEFAULT,
		// SWT.DEFAULT));
		// mCanvasTilePicture.setBackground(shell.getDisplay().getSystemColor(
		// SWT.COLOR_WIDGET_LIGHT_SHADOW ) );
		mCanvasTilePicture.setSize(2000, 3000);
		mScrolledComponent.setMinSize(2000, 3000);
		mScrolledComponent.setContent(mCanvasTilePicture);
		mScrolledComponent.setExpandHorizontal(true);
		mScrolledComponent.setExpandVertical(true);

		mCanvasTilePicture.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				onMouseUpPicture(e);
			}
		});
		mCanvasTilePicture.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				onMouseMovePicture(e);
			}
		});
		mCanvasTilePicture.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				onPaintTilePicture(e);
			}
		});

	}

	private void createMenu() {

		mMainMenus = new CMainMenus(shell);
		mMainMenus.addMenu("&File")

				.add("&Exit", new CCallback() {
					@Override
					public void callback() {
						shell.close();
					}
				}).add("&Load image", new CCallback() {
					@Override
					public void callback() {
						onLoadImage();
					}
				})

		;

	}

	protected void onLoadImage() {
		String[] filterExt = { "*.gif", "*.jpg", "*.bmp", "*.png" };
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		fd.setText("Open Image");
		fd.setFilterExtensions(filterExt);
		String filename = fd.open();
		ImageData imageData = new ImageData(filename);
		File file = new File(filename);
		// pureWhiteIdeaImageData.transparentPixel =
		// pureWhiteIdeaImageData.palette.getPixel(new RGB(255,255,255));
		final Image image = new Image(shell.getDisplay(), imageData);
		int width = imageData.width;
		int height = imageData.height;
		int depth = imageData.depth;
		RGB rgb[] = imageData.getRGBs();
		if (rgb == null) {
			return;
		}
		int sprite[] = new int[8];
		int pixel;
		int nextsprite=1;
		String strTileset = "";
		String strSpriteSet = "";
		TreeMap<String, Integer> mapSprites = new TreeMap<>();
		for (int y = 0; y < height; y += 8) {
			String match = "";
			for (int x = 0; x < width; x += 8) {
				match="";
				for (int irow = 0; irow < 8; irow++) {
					int spbyte = 0;
					for (int ibit = 0; ibit < 8; ibit++) {
						try {
							pixel = imageData.getPixel(x + ibit, y + irow);
						} catch(Exception ex) {
							pixel = 1;
						}
						RGB rgbpix = rgb[pixel];
						if (rgbpix.red > 128 || rgbpix.blue > 128 || rgbpix.green > 128)
							pixel = 0;
						else
							pixel = 1;
						spbyte += spbyte;
						spbyte += pixel;
					}
					sprite[irow] = spbyte;
					match += String.format("0x%02x ", spbyte & 0xff) ;
				} // irow
				boolean allnull=true;
				for (int i=0;i<8;i++) {
					if (sprite[i] != 0) {allnull = false; break; }
				}
				Integer isprite;
				if (allnull) {
					isprite = 0;
				} else
				{
					isprite =  mapSprites.get(match);
					if (isprite == null) {
						isprite = nextsprite;
						nextsprite++;
						mapSprites.put(match, isprite);
						strSpriteSet+=match.trim()+"\n";
					}
				}
				strTileset += String.format("0x%02x ", (isprite.intValue() & 0xff));
			}
			strTileset += "\n";
		}
		String name = file.getName().replaceAll("\\.", "_");
		mTextTiles.setText(strTileset);
		mTextIcons.setText(strSpriteSet);
		mCurrentTileMap = new CSpriteData(new CToken());
		mCurrentTileMap.name = name+"_tiles";
		mCurrentTileMap.w = width / 8;
		mCurrentTileMap.h = height / 8;
		mCurrentTileMap.sb = null;
		mCurrentTileMap.isNew = true;
		mSpriteSet  = new CSpriteData(new CToken());
		mSpriteSet.name = name + "_sprite";
		mSpriteSet.isNew = true;
		mSpriteSet.w = 8;
		mSpriteSet.h = 8;
		mSpriteSet.sb = null;
		mSpriteSet.text = strSpriteSet;
		mComboH.setText(String.format("%d", mCurrentTileMap.h));
		mComboW.setText(String.format("%d", mCurrentTileMap.w));
		
		onUpdateEditSprites();
		onUpdateEditTiles();

	}

	private void onUpdateEditSprites() {
		CSpriteData data;
		if (mTilesetIndex != -1) {
		 data = mResources.mSprites.get(mTilesetIndex);
		} else {
			if (mSpriteSet == null)
				data = new CSpriteData(new CToken());
			else
				data = mSpriteSet;
		}
		tilesetBytes = data.parse(data.getText());
		
	}

	protected void onSave() {
		if (mCurrentTileMap != null) {
			mCurrentTileMap.text = mTextTiles.getText();
			if (mResources.saveTileset(mCurrentTileMap) == false) {
				CDialogMessage dlg = new CDialogMessage(shell, SWT.TITLE);
				dlg.text = "Can not save tileset\nPlease update source\nmanually!";
				dlg.open();
			}
		}
		if (mSpriteSet != null) {
			if (!mResources.save(mSpriteSet)) {
				CDialogMessage dlg = new CDialogMessage(shell, SWT.TITLE);
				dlg.text = "Can not save sprites\nPlease update source\nmanually!";
				dlg.open();
			}
		}

	}

	protected void onModifyComboTileset(ModifyEvent e) {
		try {
			mTilesetIndex = mComboTileset.getSelectionIndex();
			CSpriteData data = mResources.mSprites.get(mTilesetIndex);
			mTextIcons.setText(data.getText());
			tilesetBytes = data.parse(data.getText());

			if (mCanvasTilePicture != null)
				mCanvasTilePicture.redraw();
			if (mCanvasTileset != null)
				mCanvasTileset.redraw();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	protected void onModifyComboTileMap(ModifyEvent e) {
		try {
			mTilesetIndex = mComboTileMap.getSelectionIndex();
			CSpriteData data = mResources.mTilesets.get(mTilesetIndex);
			mCurrentTileMap = data;
			mTextTiles.setText(data.getText());
			// tilesetBytes = data.parse(data.getText());

			if (mCanvasTilePicture != null)
				mCanvasTilePicture.redraw();
			if (mCanvasTileset != null)
				mCanvasTileset.redraw();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	protected void onUpdateEditTiles() {
		CTokenizer tokenizer = new CTokenizer();
		CToken token = new CToken();
		String text = mTextTiles.getText();
		tokenizer.start(text);
		int nRows = getTilesetH();
		int nColumns = getTilesetW();
		int pos = 0;
		int posInLine = 0;
		int i, n, count, b;
		for (i = 0; i < mTileData.length; i++)
			mTileData[i] = 0;
		while (tokenizer.hasData()) {
			tokenizer.getToken(token);
			if (token.token == Token.number) {
				n = token.iliteral;
				switch (n) {
				case 0xff:
					while (posInLine < nColumns) {
						mTileData[pos++] = 0;
						posInLine++;
					}
					posInLine = 0;
					break;
				case 0xfe:
					count = nextNumber(tokenizer, token);
					for (i = 0; i < count; i++) {
						mTileData[pos++] = 0;
						posInLine++;
					}
					if (posInLine == nColumns)
						posInLine = 0;
					break;
				case 0xfd:
					count = nextNumber(tokenizer, token);
					b = nextNumber(tokenizer, token);
					for (i = 0; i < count; i++) {
						mTileData[pos++] = b;
						posInLine++;
					}
					if (posInLine == nColumns)
						posInLine = 0;
					break;
				default:
					mTileData[pos++] = n;
					posInLine++;
					if (posInLine == nColumns)
						posInLine = 0;
				}
			}

		}
		mCanvasTilePicture.redraw();
		// TODO Auto-generated method stub

	}

	private int getTilesetW() {
		try {
			return Integer.parseInt(mComboW.getText());
		} catch (Exception ex) {

		}
		return 8;
	}

	private int getTilesetH() {
		try {
			return Integer.parseInt(mComboH.getText());
		} catch (Exception ex) {

		}
		return 8;
	}

	private int nextNumber(CTokenizer tokenizer, CToken token) {
		int r = 0;
		while (tokenizer.hasData()) {
			tokenizer.getToken(token);
			if (token.token == Token.number) {
				r = token.iliteral;
				break;
			}
		}
		return r;
	}

	protected void onResize() {
		if (tabFolder == null)
			return;
		Rectangle rect = shell.getBounds();
		Rectangle rectTab = tabFolder.getBounds();
		tabFolder.setBounds(rectTab.x, rectTab.y, rect.width - rectTab.x - 20, rect.height - rectTab.y - 40);

	}

	protected void calcScreenDim() {
		try {
			int faktor = Integer.parseInt(mComboZoom.getText());
			mNTilesH = Integer.parseInt(mComboH.getText());
			mNTilesW = Integer.parseInt(mComboW.getText());
			int tileh = getTileH();
			int tilew = getTileW();
			Rectangle rect = mCanvasTilePicture.getBounds();

			int w = tilew * mNTilesW * faktor;
			int h = tileh * mNTilesH * faktor;
			mScreenW = w;
			mScreenH = h;
			mCanvasTilePicture.setSize(w, h);
			mScrolledComponent.setMinSize(w + 10, h + 10);

			// mCanvasTilePicture.setBounds(0,0,w,h);
			// mCanvasTilePicture.setSize(mCanvasTilePicture.computeSize(SWT.DEFAULT,
			// SWT.DEFAULT));
			// mScrolledComponent.setMinSize(mCanvasTilePicture.computeSize(SWT.DEFAULT,
			// SWT.DEFAULT));
		} catch (Exception ex) {

		}

	}

	protected void onMouseMovePicture(MouseEvent e) {
		try {

			int tilesW = Integer.parseInt(mComboW.getText());
			int tilesH = Integer.parseInt(mComboH.getText());
			int x = 0;
			int y = 0;
			int w = mScreenW / tilesW;
			int h = mScreenH / tilesH;

			if (mSpriteSelected != -1) {
				int selx = e.x / w;
				int sely = e.y / h;
				if (selx < mNTilesW && sely < mNTilesH) {
					// System.out.println(String.format("selx=%d sely=%d", selx, sely));
					if (selx != mSpriteSelectedX || sely != mSpriteSelectedY) {
						mSpriteSelectedX = selx;
						mSpriteSelectedY = sely;
						mCanvasTilePicture.redraw();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	protected void onMouseUpPicture(MouseEvent e) {
		if (mSpriteSelected != -1) {
			int tile = mSpriteSelected / mSpriteSizeBytes;
			if (e.button != 1)
				tile = -1;
			putTile(mSpriteSelectedX, mSpriteSelectedY, tile + 1);
			writeTileset();
		}

	}

	protected void onPaintTilePicture(PaintEvent e) {
		try {
			Display display = getParent().getDisplay();

			Color gray = display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
			e.gc.setForeground(gray);
			Rectangle rect = mCanvasTilePicture.getBounds();
			int tileh = getTileH();
			int tilew = getTileW();
			int tilesW = parseInt(mComboW.getText());
			int tilesH = parseInt(mComboH.getText());
			int x = 0;
			int y = 0;
			int w = mScreenW / tilesW;
			int h = mScreenH / tilesH;
			for (int iy = 0; iy <= tilesH; iy++) {
				e.gc.drawLine(0, y, rect.width, y);
				y += h;

			}
			for (int ix = 0; ix <= tilesW; ix++) {
				e.gc.drawLine(x, 0, x, rect.height);
				x += w;

			}
			e.gc.drawLine(rect.width, 0, rect.width, rect.height);
			e.gc.drawLine(0, rect.height, rect.width, rect.height);
			e.gc.setBackground(gray);
			e.gc.fillRectangle(mScreenW, 0, rect.width, rect.height);
			e.gc.fillRectangle(0, mScreenH, rect.width, rect.height);
			y = 0;
			for (int iy = 0; iy < mNTilesH; iy++) {
				x = 0;
				for (int ix = 0; ix < mNTilesW; ix++) {
					int tile = getTile(ix, iy);
					if (tile != 0) {
						int adr = (tile - 1) * mSpriteSizeBytes;
						drawSpriteWhite(e.gc, adr, ix * w, iy * h, tilew, tileh, w, h);
					}
				}
			}
			if (mSpritePointed != -1) {
				drawSprite(e.gc, mSpriteSelected, mSpriteSelectedX * w, mSpriteSelectedY * h, tilew, tileh, w, h);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private int parseInt(String text) {
		try {
			return Integer.parseInt(text);
		} catch (Exception ex) {

		}
		return 1;
	}

	protected void onRepaintCanvasPreview(PaintEvent e) {
		if (mCurrentSprite == null)
			return;
		Rectangle rect = mCanvasPreview.getBounds();
		int tileh = getTileH();
		int tilew = getTileW();

		drawSprite(e.gc, mCurrentSprite.ofs, 0, 0, tilew, tileh, rect.width, rect.height);

	}

	protected void onMouseMoveTile(MouseEvent e) {
		Rectangle rect = mCanvasTileset.getBounds();
		int x = e.x;
		int y = e.y;
		int ofs = -1;

		for (SpriteLocation loc : mListSpriteLocations) {
			if (loc.x <= x && loc.x + loc.w > x && loc.y <= y && loc.y + loc.h > y) {
				ofs = loc.ofs;
				if (ofs != mSpritePointed) {
					mSpritePointed = ofs;
					mCanvasTileset.redraw();
				}
				mCurrentSprite = loc;
				mCanvasPreview.redraw();

				break;
			}
		}

	}

	protected void onMouseUpTile(MouseEvent e) {
		mSpriteSelected = mSpritePointed;
		mCanvasTileset.redraw();

	}

	protected void onPaintTileSet(PaintEvent e) {
		int faktor = 4;
		int tileh = getTileH();
		int tilew = getTileW();
		int x = 0;
		int y = 0;
		Rectangle rect = mCanvasTileset.getBounds();
		int w = tilew * faktor + 4;
		int h = tileh * faktor + 4;
		int spriteByteW = tileh;
		mSpriteSizeBytes = spriteByteW;
		if (tilew == 16)
			spriteByteW *= 2;
		int index = 0;
		if (tilesetBytes == null)
			return;
		mListSpriteLocations.clear();
		for (index = 0; index < tilesetBytes.length; index += spriteByteW) {
			mListSpriteLocations.add(new SpriteLocation(x, y, w, h, index));
			drawSprite(e.gc, index, x, y, tilew, tileh, tilew * faktor, tileh * faktor);
			// index += spriteByteW;
			x += w;
			if (x + w > rect.width) {
				x = 0;
				y += h;
			}
		}

	}

	private void drawSpriteWhite(GC gc, int index, int x, int y, int w, int h, int canvasw, int canvash) {
		drawSpriteSub(gc, index, x, y, w, h, canvasw, canvash, false);
	}

	private void drawSprite(GC gc, int index, int x, int y, int w, int h, int canvasw, int canvash) {
		drawSpriteSub(gc, index, x, y, w, h, canvasw, canvash, true);
	}

	private void drawSpriteSub(GC gc, int index, int x, int y, int w, int h, int canvasw, int canvash, boolean color) {
		int iy;
		Display display = getParent().getDisplay();
		Color white = display.getSystemColor(SWT.COLOR_WHITE);
		Color gray = display.getSystemColor(SWT.COLOR_GRAY);
		Color blue = display.getSystemColor(SWT.COLOR_BLUE);
		Color black = display.getSystemColor(SWT.COLOR_BLACK);
		Color whiteCol = white;
		int pw = canvasw / w;
		int ph = canvash / h;
		int b, mask;
		if (color) {
			if (index == mSpritePointed)
				whiteCol = gray;
			if (index == mSpriteSelected)
				whiteCol = blue;
		}
		int pos = 0;
		if (w == 8) {
			// 8xn Sprite
			int sy = y;

			for (iy = 0; iy < h; iy++) {
				int sx = x;
				if (index + pos >= tilesetBytes.length)
					break;
				b = tilesetBytes[index + pos];
				pos++;
				mask = 0x80;
				for (int bit = 0; bit < 8; bit++) {
					if ((b & mask) != 0)
						gc.setBackground(black);
					else
						gc.setBackground(whiteCol);
					gc.fillRectangle(sx, sy, pw, ph);
					sx += pw;
					mask /= 2;
				}
				sy += ph;
			}
		} else {

		}

	}

	private int getTileW() {
		String str = mComboSpriteSize.getText();
		int p = str.indexOf('x');
		if (p == -1)
			return 16;
		int h = Integer.parseInt(str.substring(0, p));
		return h;
	}

	private int getTileH() {
		String str = mComboSpriteSize.getText();
		int p = str.indexOf('x');
		if (p == -1)
			return 16;
		int h = Integer.parseInt(str.substring(p + 1));
		return h;
	}

	private void putTile(int x, int y, int tile) {
		int adr = y * mNTilesW + x;
		if (adr < mTileData.length)
			mTileData[adr] = tile;

	}

	private int getTile(int x, int y) {
		int adr = y * mNTilesW + x;
		if (adr < mTileData.length)
			return mTileData[adr];
		else
			return 0;
	}

	private void writeTileset() {
		mTilesUpdate = true;
		if (mBtnRLE.getSelection())
			mTextTiles.setText(tilesetToStringRLE());
		else
			mTextTiles.setText(tilesetToString());
		mTilesUpdate = false;
	}

	private String tilesetToString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("#tileset %dx%d", mNTilesW, mNTilesH));
		if (mTileSetLabel != null) {
			if (!mTileSetLabel.isEmpty())
				sb.append(": " + mTileSetLabel);
		}
		int pos = 0;
		for (int y = 0; y < mNTilesH; y++) {
			sb.append("\n");
			for (int x = 0; x < mNTilesW; x++) {
				if (x != 0)
					sb.append(" ");
				sb.append(String.format("0x%02x", mTileData[pos++]));
			}

		}
		sb.append("\n#end\n");
		return sb.toString();
	}

	private String tilesetToStringRLE() {
		int line[] = new int[mNTilesW];
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("#tileset %dx%d", mNTilesW, mNTilesH));
		if (mTileSetLabel != null) {
			if (!mTileSetLabel.isEmpty())
				sb.append(": " + mTileSetLabel);
		}
		int pos = 0;
		for (int y = 0; y < mNTilesH; y++) {
			sb.append("\n");
			for (int x = 0; x < mNTilesW; x++) {
				line[x] = mTileData[pos++];
			}
			int len = rle(line);
			for (int x = 0; x < len; x++) {
				if (x != 0)
					sb.append(" ");
				sb.append(String.format("0x%02x", line[x]));
			}

		}
		sb.append("\n#end\n");
		return sb.toString();
	}

	private int rle(int[] line) {
		int src = 0;
		int dest = 0;
		int count;
		int b;
		int b1;
		int i;

		while (src < line.length) {
			b = line[src++];
			count = 1;
			for (i = src; i < line.length; i++) {
				b1 = line[i];
				if (b1 != b)
					break;
				count++;
			}
			if ((count > 2 && b == 0) || count > 3) {
				if (b == 0) {
					if (i == line.length) {
						line[dest++] = 0xff;
					} else {
						line[dest++] = 0xfe;
						line[dest++] = count;
					}
				} else {
					line[dest++] = 0xfd;
					line[dest++] = count;
					line[dest++] = b;
				}
				src += count - 1;
			} else
				line[dest++] = b;
		}

		return dest;

	}
}
