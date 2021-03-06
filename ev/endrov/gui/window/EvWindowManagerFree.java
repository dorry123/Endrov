/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.window;

import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.JFrame;

import endrov.core.EndrovCore;
import endrov.gui.icon.BasicIcon;
import endrov.starter.EvSystemUtil;

/**
 * Ev Window Manager: Free-floating windows
 * @author Johan Henriksson
 */
public class EvWindowManagerFree extends JFrame implements WindowListener, EvWindowManager
	{
	static final long serialVersionUID=0; 
	//this is not needed in later versions of java. just for OSX compatibility
	private static WeakHashMap<Window, Void> java15windowList=new WeakHashMap<Window, Void>();
	public static Collection<Window> get15Windows()
		{
		return java15windowList.keySet();
		}

	
	
	
	private WeakReference<EvBasicWindow> bw;
	private boolean shouldHaveBeenDisposed=false;
	
	
	private EvBasicWindow getBasicWindow()
		{
		return bw.get();
		}
	
	
	public EvWindowManagerFree(EvBasicWindow bw)
		{
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.bw=new WeakReference<EvBasicWindow>(bw);
		addWindowListener(this);
		add(bw);

		
    //int titleBarHeight = getInsets().top; //can be used to set the right icon
    //20x20 seems good on windows? or more?
		//16x16 on gnome, but in alt+tab larger. can supply larger image
		if(!EvSystemUtil.isMac())
			setIconImage(BasicIcon.iconEndrov.getImage());
		
		java15windowList.put(this,null);
		}
	
	public void setTitle(String title)
		{
		super.setTitle(EndrovCore.programName+" "+title+" ["+getBasicWindow().windowInstance+"]");
		}
	
	private static WeakReference<EvBasicWindow> lastActiveWindow=new WeakReference<EvBasicWindow>(null);
	
	public void windowClosing(WindowEvent e) 
		{
	
		}
	public void windowActivated(WindowEvent e)
		{
		lastActiveWindow=new WeakReference<EvBasicWindow>(getBasicWindow());
		}
	public void windowDeactivated(WindowEvent arg0)	{}
	public void windowDeiconified(WindowEvent arg0)	{}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0)
		{
		//Remove listeners manually just to be sure GC works smoothly
		//for(WindowListener list:getWindowListeners())
		//	removeWindowListener(list);
		String title=getTitle();
		shouldHaveBeenDisposed=true;
		
		getBasicWindow().freeResourcesBasic();
		//Closing has already invoked dispose()
		System.out.println("window closed: "+title);
		}

	
	public void setResizable(boolean b)
		{
		super.setResizable(b);
		}
	
	
	protected void finalize() throws Throwable
		{
		String title=getTitle();
		System.out.println("Finalize "+title);
		}

	
	public static class Manager implements EvBasicWindow.EvWindowManagerMaker
		{
		public EvWindowManager createWindow(EvBasicWindow bw)
			{
			EvWindowManager w=new EvWindowManagerFree(bw);
			return w;
			}
		
		/**
		 * Get a list of all windows
		 */
		public List<EvBasicWindow> getAllWindows()
			{
			LinkedList<EvBasicWindow> list=new LinkedList<EvBasicWindow>();
			for(Window w:get15Windows())
				if(w instanceof EvWindowManagerFree)
					{
					EvWindowManagerFree ww=(EvWindowManagerFree)w;
					EvBasicWindow bw=ww.getBasicWindow();
					if(!ww.shouldHaveBeenDisposed)
						list.add(bw);
					}
			return list;
			}

		public EvBasicWindow getFocusWindow()
			{
			return lastActiveWindow.get();
			}
		
		}

	@Override
	public void setBounds(Rectangle r)
		{
		super.setBounds(r);
		super.setLocationRelativeTo(null);
		}
	
	}
