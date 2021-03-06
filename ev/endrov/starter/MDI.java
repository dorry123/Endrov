/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.starter;


import endrov.core.*;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvSplashScreen;
import endrov.gui.window.EvWindowManagerMDI;
import endrov.windowViewer2D.*;

import java.io.*;

//http://lopica.sourceforge.net/faq.html#nosandbox
//System.setSecurityManager(null)

/**
 * Start graphical user interface, one big window using MDI.
 * Experimental, totally unsupported
 * @author Johan Henriksson
 */
public class MDI
	{
	/**
	 * Entry point
	 * @param args Command line arguments
	 */
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());

		EvSplashScreen ss=null;
		if(EvSplashScreen.isSplashEnabled())
			ss=new EvSplashScreen();
		
		//if(!PluginInfo.storedInJar())
			{
			String javalib=System.getProperty("java.library.path");
			File javalibfile=new File(javalib);
			EvLog.printLog("Loading native libraries from "+javalibfile.getAbsolutePath());
			}
		
		try
			{
			EndrovCore.loadPlugins();
			EvBasicWindow.windowManager=new EvWindowManagerMDI.Manager();
//			BasicWindowExitLast.integrate();
			EndrovCore.loadPersonalConfig();		
			if(EvBasicWindow.getWindowList().size()==0)
				{
				//Make sure at least one window is open
				new Viewer2DWindow();
				}
			if(ss!=null)
				ss.dispose();
			}
		catch (Exception e)
			{
			EvLog.printError("EVGUI", e);
			}
		
		//Help memory debugging; remove dead objects
		System.gc();
		}
	}
