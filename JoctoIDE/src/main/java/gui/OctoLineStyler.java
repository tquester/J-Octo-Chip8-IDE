package gui;

// see http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/JavaSourcecodeViewer.htm
import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import assembler.CToken;
import assembler.CTokenizer;
import assembler.Token;

import org.eclipse.swt.widgets.Control;

public class OctoLineStyler implements LineStyleListener {

	CTokenizer tokenizer = new CTokenizer();
	Vector blockComments = new Vector();

	private Color mColorWord;

	private Color mColorString;

	private Color mColorKeyword;

	private Color mColorNumber;

	private Color mColorComment;
	private Color mColorLabel;

	public OctoLineStyler() {
		initializeColors();
		tokenizer.deliverWhite = false;
		// scanner = new JavaScanner();
	}

	Color getColor(Token token) {
		if (token == null)
			return mColorWord;
		switch (token) {
		case comment:
			return mColorComment;
		case string:
			return mColorString;
		case number:
			return mColorNumber;
		case label:
			return mColorLabel;
		case literal:
		case whitespace:
		case newline:
			return mColorWord;

		default:
			return mColorKeyword;
		}

	}


	void initializeColors() {
		Display display = Display.getDefault();
		mColorWord = display.getSystemColor(SWT.COLOR_BLACK);
		mColorString = display.getSystemColor(SWT.COLOR_BLUE);
		mColorKeyword = display.getSystemColor(SWT.COLOR_DARK_RED);
		mColorNumber = display.getSystemColor(SWT.COLOR_DARK_BLUE);
		mColorComment = display.getSystemColor(SWT.COLOR_DARK_GREEN);
		mColorLabel = display.getSystemColor(SWT.COLOR_DARK_GRAY);

	}

	void disposeColors() {
	}

	/**
	 * Event.detail line start offset (input) Event.text line text (input)
	 * LineStyleEvent.styles Enumeration of StyleRanges, need to be in order.
	 * (output) LineStyleEvent.background line background color (output)
	 */
	public void lineGetStyle(LineStyleEvent event) {
		try {
		Vector styles = new Vector();
		CToken token = new CToken();
		StyleRange lastStyle;

		Color defaultFgColor = ((Control) event.widget).getForeground();
		tokenizer.start(event.lineText);

		while (tokenizer.hasData()) {
			tokenizer.getToken(token);
			if (token.token == Token.literal)
				continue;
			if (token.token != Token.whitespace) {
				Color color = getColor(token.token);
				// Only create a style if the token color is different than the
				// widget's default foreground color and the token's style is
				// not
				// bold. Keywords are bolded.
				if ((!color.equals(defaultFgColor))) {
					StyleRange style = new StyleRange(token.pos - 1 + event.lineOffset, token.length(), color, null);
					if (isKeyword(token)) {
						style.fontStyle = SWT.BOLD;
					}
					if (styles.isEmpty()) {
						styles.addElement(style);
					} else {
						// Merge similar styles. Doing so will improve
						// performance.
						lastStyle = (StyleRange) styles.lastElement();
						if (lastStyle.similarTo(style) && (lastStyle.start + lastStyle.length == style.start)) {
							lastStyle.length += style.length;
						} else {
							styles.addElement(style);
						}
					}
				}
			} else if ((!styles.isEmpty()) && ((lastStyle = (StyleRange) styles.lastElement()).fontStyle == SWT.BOLD)) {
				int start = token.pos + event.lineOffset;
				lastStyle = (StyleRange) styles.lastElement();
				// A font style of SWT.BOLD implies that the last style
				// represents a java keyword.
				if (lastStyle.start + lastStyle.length == start) {
					// Have the white space take on the style before it to
					// minimize the number of style ranges created and the
					// number of font style changes during rendering.
					lastStyle.length += token.length();
				}
			}
		}
		event.styles = new StyleRange[styles.size()];
		styles.copyInto(event.styles);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	private boolean isKeyword(CToken token) {
		if (token.token == null)
			return false;
		switch (token.token) {
		case comment:
		case whitespace:
		case newline:
		case number:
		case literal:
		case label:
			return false;
		default:
			return true;

		}

	}



}
