/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeBookmark;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.data.gui.EvDataGUI;
import endrov.gui.EvSwingUtil;
import endrov.gui.undo.UndoOpPutObject;
import endrov.windowViewer2D.Viewer2DWindow;
import endrov.windowViewer2D.Viewer2DWindowExtension;
import endrov.windowViewer2D.Viewer2DTool;

/**
 * Bookmark integration with image window
 * @author Johan Henriksson
 *
 */
public class BookmarkImageWindowHook implements Viewer2DWindowExtension
	{
	public void newImageWindow(final Viewer2DWindow w)
		{
		w.addImageWindowTool(new Viewer2DTool(){
			JMenu miBookmark=new JMenu("Bookmarks");
			JMenuItem miAddBookmark=new JMenuItem("Add bookmark");
			
			//At the moment, can expect menu rebuild signal from outside
			public JMenuItem getMenuItem()
				{
				EvSwingUtil.tearDownMenu(miBookmark);
				miBookmark.add(miAddBookmark);
				miAddBookmark.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e)
						{
						EvContainer container=w.getSelectedData();
						String name=Bookmark.addBookmarkDialog(w, container);
						if(name!=null)
							{
							Bookmark b=new Bookmark();
							b.frame=w.getFrame();
							b.z=w.getZ();
							new UndoOpPutObject("Add bookmark "+name, b, container, name);
							}
						}
					});
				miBookmark.addSeparator();
				
				for(EvData data:EvDataGUI.openedData)
					{
					JMenu miData=new JMenu(data.getMetadataName());
					for(Map.Entry<EvPath, Bookmark> e:data.getIdObjectsRecursive(Bookmark.class).entrySet())
						{
						JMenuItem miGoto=new JMenuItem("=> "+e.getKey());
						miData.add(miGoto);
						final Bookmark m=e.getValue();
						
						miGoto.addActionListener(new ActionListener()
							{
							public void actionPerformed(ActionEvent e)
								{
								//if(w.frameControl!=null)
									//{
								w.setFrame(m.frame);
								if(m.z!=null)
									w.setZ(m.z);
									//}
								w.repaint();
								}
							});
						}
					if(miData.getItemCount()>0)
						miBookmark.add(miData);				
					}
				
				
				
				return miBookmark;
				}

			public void keyPressed(KeyEvent e)
				{
				}

			public void keyReleased(KeyEvent e)
				{
				}

			public void mouseClicked(MouseEvent e, Component invoker)
				{
				}

			public void mouseDragged(MouseEvent e, int dx, int dy)
				{
				}

			public void mouseExited(MouseEvent e)
				{
				}

			public void mouseMoved(MouseEvent e, int dx, int dy)
				{
				}

			public void mousePressed(MouseEvent e)
				{
				}

			public void mouseReleased(MouseEvent e)
				{
				}

			public void paintComponent(Graphics g)
				{
				}

			public void deselected()
				{
				}
			
			
		
		});
		}
	}

	