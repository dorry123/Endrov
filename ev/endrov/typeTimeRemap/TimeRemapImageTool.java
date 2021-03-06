/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeTimeRemap;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import endrov.data.EvContainer;
import endrov.gui.window.EvBasicWindow;
import endrov.util.math.EvDecimal;
import endrov.windowViewer2D.*;

/**
 * Image Window Tool: Associate time with current frame
 * @author Johan Henriksson
 */
public class TimeRemapImageTool implements Viewer2DTool
	{
	private final Viewer2DInterface w;
	public TimeRemapImageTool(Viewer2DInterface w)
		{
		this.w=w;
		}
	
	/*
	public boolean isToggleable()
		{
		return false;
		}
	public String toolCaption()
		{
		return "Frametime/Set current";
		}
	public boolean enabled()
		{
		return true;
		}
		*/
	public JMenuItem getMenuItem()
		{
		JMenuItem mi=new JMenuItem("Frametime/Set current");
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){mouseClicked(null, null);}
		});
		return mi;
		}
	
	public void mouseClicked(MouseEvent e, Component invoker)
		{
		String frametimes=JOptionPane.showInputDialog("What is the current time?");
		if(frametimes!=null)
			{
			EvContainer rec=w.getRootObject();
			for(TimeRemap f:rec.getObjects(TimeRemap.class))
				{
				EvDecimal frametime=new EvDecimal(frametimes);
				f.add(w.getFrame(), frametime); 
				EvBasicWindow.updateWindows();
				}
			}
		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e)	{}
	public void mouseMoved(MouseEvent e, int dx, int dy) {}
	public void mouseExited(MouseEvent e) {}
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void paintComponent(Graphics g) {}
	public void deselected() {}

	}
