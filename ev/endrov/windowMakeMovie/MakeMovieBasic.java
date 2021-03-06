/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowMakeMovie;

import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;

import java.awt.event.*;

import javax.swing.JMenuItem;


/**
 * Extension to BasicWindow
 * 
 * @author Johan Henriksson
 */
public class MakeMovieBasic implements EvBasicWindowExtension
	{
	public void newBasicWindow(EvBasicWindow w)
		{
		w.addHook(this.getClass(),new Hook());
		}
	private class Hook implements EvBasicWindowHook, ActionListener
		{
		JMenuItem miMakeMovie=new JMenuItem("Make movie");
		JMenuItem miMakeMovieImw=new JMenuItem("Make movie from 2D viewers");
		
		public void createMenus(EvBasicWindow w)
			{
			miMakeMovie.addActionListener(this);
			miMakeMovieImw.addActionListener(this);
			w.addMenuOperation(miMakeMovie, null);
			w.addMenuOperation(miMakeMovieImw, null);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			if(e.getSource()==miMakeMovie)
				new MakeMovieWindow();
			else if(e.getSource()==miMakeMovieImw)
				MakeMovieWindow.createDialogFromImageWindows();
			}
		
		public void buildMenu(EvBasicWindow w){}
		}
	}
