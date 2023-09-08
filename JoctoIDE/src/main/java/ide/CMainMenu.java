package ide;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;



public class CMainMenu {
	public MenuItem mHeader;
	public Menu mMenu;
	public ArrayList<CMenuEntry> mEntries = new ArrayList<>();
	
	public CMainMenu(Menu menuBar, Shell shell, String name) {
		mHeader = new MenuItem(menuBar, SWT.CASCADE);
		mHeader.setText(name);
		mMenu = new Menu(shell, SWT.DROP_DOWN);
		mHeader.setMenu(mMenu);

	}

	public CMainMenu add(String title, Integer accelerator, CCallback callback) {
		mEntries.add(new CMenuEntry(mMenu, title, accelerator, callback));
		
		return this;
		
	}

	public CMainMenu add(String title, CCallback callback) {
		mEntries.add(new CMenuEntry(mMenu, title, callback));
		
		return this;
		
	}


}