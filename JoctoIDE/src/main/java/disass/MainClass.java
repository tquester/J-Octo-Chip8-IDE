package disass;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import assembler.CTokenizer;
import gui.CDialogIDE;

public class MainClass {

	
	public static void main(String[] args) {
		
		
	      Display display = new Display();
	        Shell shell = new Shell(display);
	        shell.setText("Chip8 Disassembler");
	        shell.setImage(SWTResourceManager.getImage(MainClass.class,"disass.gif"));
	        shell.setLayout(new FillLayout(SWT.VERTICAL));
	        shell.setBounds(0,0,10,10);
	        shell.open();
	        CDialogIDE mainDialog = new CDialogIDE(shell, SWT.TITLE + SWT.MAX + SWT.RESIZE + SWT.CLOSE);
	        mainDialog.open();
	        
	        /*
		System.out.println("usuage in=chipfile out=textfile disass hex\n");
		
		CC8Decoder decoder = new CC8Decoder();
		CParameter para = new CParameter(args);
		String filename = para.getParam("in");
		String outfile = para.getParam("out");
		boolean disassFormat = para.isCmd("disass");
		boolean hex = para.isCmd("hex");
		decoder.start(filename, outfile, disassFormat, hex);
		*/

	}

	private static char[] expr(CTokenizer tokenizer) {
		
		// TODO Auto-generated method stub
		
		return null;
	}

}
