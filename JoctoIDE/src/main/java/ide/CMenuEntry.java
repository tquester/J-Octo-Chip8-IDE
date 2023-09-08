package ide;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;


public class CMenuEntry {
	CCallback callback;
	MenuItem menuItem;

	public CMenuEntry(Menu menu, String title, final CCallback callback) {
		init(menu, title, callback, null);
	}
	public CMenuEntry(Menu menu, String title, Integer accelerator, final CCallback callback) {
		init(menu, title, callback, accelerator);
		
		
	}

	public void init(Menu menu, String title, final CCallback callback, Integer accelerator) {
		
		this.callback = callback;
		menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText(title);
		if (accelerator != null) 
			menuItem.setAccelerator(accelerator.intValue());
		menuItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				callback.callback();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				callback.callback();
			}
		});
		
	}

}