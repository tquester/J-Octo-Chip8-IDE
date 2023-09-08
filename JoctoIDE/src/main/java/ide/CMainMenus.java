package ide;

import java.util.ArrayList;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

public class CMainMenus {
	Menu mBar;
	public Shell mShell;
	public CMainMenus(Shell shell) {
		mShell = shell;
		mBar = 	new Menu(shell, SWT.BAR);
		mShell.setMenuBar(mBar);
	}
	
	public TreeMap<String, CMainMenu> mMainMenu = new TreeMap<>();
	
	public CMainMenu addMenu(String name) {
		CMainMenu menu = new CMainMenu(mBar, mShell, name);
		mMainMenu.put(name, menu);
		return menu;
	}

}
