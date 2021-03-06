/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowPlateAnalysis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.*;
import org.jdom.*;

import endrov.core.EvSQLConnection;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.EvPath;
import endrov.data.gui.EvDataGUI;
import endrov.flow.Flow;
import endrov.gui.EvSwingUtil;
import endrov.gui.FrameControl2D;
import endrov.gui.component.EvComboObject;
import endrov.gui.component.EvComboObjectOne;
import endrov.gui.component.JImageButton;
import endrov.gui.component.JSnapBackSlider;
import endrov.gui.icon.BasicIcon;
import endrov.gui.sql.EvDialogChooseSQL;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.ioBD.EvIODataBD;
import endrov.typeImageset.*;
import endrov.typeParticleMeasure.ParticleMeasure;
import endrov.typeParticleMeasure.ParticleMeasureIO;
import endrov.typeParticleMeasure.ParticleMeasure.Well;
import endrov.util.math.EvDecimal;
import endrov.windowPlateAnalysis.PlateAnalysisQueueWidget.WellRunnable;

/**
 * Plate window - For high-throughput analysis
 *
 * @author Johan Henriksson
 */
public class PlateWindow extends EvBasicWindow implements ChangeListener, ActionListener, PlateWindowView.Listener
	{	
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;
	
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowSavePersonalSettings(Element root)
		{
		frameControl.storeSettings(root);
		}
	public void windowLoadPersonalSettings(Element e)
		{
		frameControl.getSettings(e);
		}

	private static ImageIcon iconLabelBrightness=new ImageIcon(BasicIcon.class.getResource("labelBrightness.png"));
	private static ImageIcon iconLabelContrast=new ImageIcon(BasicIcon.class.getResource("labelContrast.png"));
	private static ImageIcon iconLabelFitRange=new ImageIcon(BasicIcon.class.getResource("labelFitRange.png"));

	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	private final EvComboObject comboData=new EvComboObject(new LinkedList<EvObject>(), true, true)
		{
		private static final long serialVersionUID = 1L;
		public boolean includeObject(EvContainer cont)
			{
			return true;
			}
		};
	private final EvComboObjectOne<ParticleMeasure> comboParticleMeasure=new EvComboObjectOne<ParticleMeasure>(new ParticleMeasure(), true, true);
	private final EvComboObjectOne<Flow> comboFlow=new EvComboObjectOne<Flow>(new Flow(), true, true);
	private JComboBox comboAttribute1=new JComboBox/*<String>*/();
	private JComboBox comboAttribute2=new JComboBox/*<String>*/();
	private JComboBox comboLayout=new JComboBox/*<String>*/();
	private JComboBox comboChannel=new JComboBox/*<String>*/();
	private JComboBox comboDisplay=new JComboBox/*<Object>*/(PlateWindowView.getAggrModes());
	private PlateAnalysisQueueWidget queue=new PlateAnalysisQueueWidget();
	
	private final FrameControl2D frameControl=new FrameControl2D(this, false, true);
	private PlateWindowView imagePanel=new PlateWindowView(this);	
	private ChannelWidget cw=new ChannelWidget();
	private JButton bAddToQueue=new JButton("Add to queue");
	
	private boolean disableDataChanged=false;

	private final JMenu menuPlateWindow=new JMenu("PlateWindow");
	private final JMenuItem miZoom=new JMenuItem("Zoom to fit");
	private final JMenuItem miExportCSV=new JMenuItem("Export as CSV");
	private final JMenuItem miImportCSV=new JMenuItem("Import from CSV");
	private final JMenuItem miExportSQL=new JMenuItem("Export to SQL");
	private final JMenuItem miReevaluate=new JMenuItem("Clear previous measure values");

	private final JMenu menuImageSize=new JMenu("Thumbnail size");
	private final JMenuItem miSize100=new JMenuItem("100px");
	private final JMenuItem miSize200=new JMenuItem("200px");
	private final JMenuItem miSize300=new JMenuItem("300px");
	private final JMenuItem miSize500=new JMenuItem("500px");
	private final JMenuItem miSizeOrig=new JMenuItem("Orig size");

	/**
	 * Update for right side bar
	 */
	private ComponentListener resizer=new ComponentListener()
		{
		public void componentResized(ComponentEvent arg0)
			{
			PlateWindow.this.revalidate();
			}
		public void componentShown(ComponentEvent arg0){}
		public void componentMoved(ComponentEvent arg0){}
		public void componentHidden(ComponentEvent arg0){}
		};
	
	/**
	 * Make a new window at given location
	 */
	public PlateWindow()
		{
		cw.updatePanelContrastBrightness();
		
		//Add listeners
		comboData.addActionListener(this);
		comboParticleMeasure.addActionListener(this);
		comboFlow.addActionListener(this);
		comboDisplay.addActionListener(this);
		comboAttribute1.addActionListener(this);
		comboAttribute2.addActionListener(this);
		comboLayout.addActionListener(this);
		comboChannel.addActionListener(this);
		bAddToQueue.addActionListener(this);
		
		addMainMenubarWindowSpecific(menuPlateWindow);

		//TODO right-click "open in image window"

		JComponent rightPanel=EvSwingUtil.layoutACB(
				EvSwingUtil.layoutCompactVertical(
						EvSwingUtil.withTitledBorder("Data location",
								EvSwingUtil.layoutCompactVertical(
										new JLabel("Images:"),
										comboData,
										new JLabel("Flow for computation:"),
										comboFlow,
										new JLabel("Measure values:"),
										comboParticleMeasure,
										bAddToQueue)),
						EvSwingUtil.withTitledBorder("Display",
								EvSwingUtil.layoutCompactVertical(
										new JLabel("Layout by:"),
										comboLayout,
										new JLabel("Channel:"),
										comboChannel,
										new JLabel("Primary attribute:"),
										comboAttribute1,
										new JLabel("Secondary attribute:"),
										comboAttribute2,
										new JLabel("Show as:"),
										comboDisplay)
										)),
				EvSwingUtil.withTitledBorder("Batch queue",	queue),
				null
				);
		JScrollPane scroll=new JScrollPane(rightPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		//Force update of layout whenever a subcomponent changes size (or the right bar will end up too small)
		comboData.addComponentListener(resizer);
		comboFlow.addComponentListener(resizer);
		comboChannel.addComponentListener(resizer);
		comboAttribute1.addComponentListener(resizer);
		comboAttribute2.addComponentListener(resizer);

		//Do the main layout
		setLayout(new BorderLayout());
		add(EvSwingUtil.layoutLCR(
				null, 
				imagePanel, 
				scroll),
				BorderLayout.CENTER);
		add(
				EvSwingUtil.layoutLCR(
					frameControl,
					cw,
					null),
				BorderLayout.SOUTH);

		//Misc
		attachDragAndDrop(imagePanel);
		packEvWindow();
		frameControl.setFrame(EvDecimal.ZERO);
		setBoundsEvWindow(500,400);
		setVisibleEvWindow(true);
		dataChangedEvent();
		}

	
	/**
	 * Rebuild ImageWindow menu
	 */
	private void buildMenu()
		{
		EvSwingUtil.tearDownMenu(menuPlateWindow);
		
			
		menuPlateWindow.add(miZoom);
		menuPlateWindow.add(miImportCSV);
		menuPlateWindow.add(miExportCSV);
		menuPlateWindow.add(miExportSQL);
		menuPlateWindow.add(miReevaluate);
		
		miZoom.addActionListener(this);
		miImportCSV.addActionListener(this);
		miExportCSV.addActionListener(this);
		miExportSQL.addActionListener(this);
		miReevaluate.addActionListener(this);
	//	miEvaluate.addActionListener(this);

		


		miSize100.addActionListener(this);
		miSize200.addActionListener(this);
		miSize300.addActionListener(this);
		miSize500.addActionListener(this);
		miSizeOrig.addActionListener(this);

		menuPlateWindow.add(menuImageSize);
		menuImageSize.add(miSize100);
		menuImageSize.add(miSize200);
		menuImageSize.add(miSize300);
		menuImageSize.add(miSize500);
		menuImageSize.add(miSizeOrig);
		}
	

	

	/**
	 * Get the zoom factor not including the binning
	 */
	public double getZoom()
		{
		return imagePanel.getZoom();
		}
	
	/**
	 * Set the zoom factor not including the binning 
	 */
	public void setZoom(double zoom)
		{
		imagePanel.setZoom(zoom);
		}
	
	/** Get rotation of image, in radians */
	public double getRotation()
		{
		return imagePanel.getRotation();
		}
	/** Set rotation of image, in radians */
	public void setRotation(double angle)
		{
		imagePanel.setRotation(angle);
		}

	
	
	/**
	 * One row of channel settings in the GUI
	 */
	public class ChannelWidget extends JPanel implements ActionListener, ChangeListener, JSnapBackSlider.SnapChangeListener
		{
		static final long serialVersionUID=0;
		
		private final JSnapBackSlider sliderContrast=new JSnapBackSlider(JSnapBackSlider.HORIZONTAL, -10000,10000);
		private final JSnapBackSlider sliderBrightness=new JSnapBackSlider(JSnapBackSlider.HORIZONTAL, -200,200);
		
		private final JImageButton bFitRange=new JImageButton(iconLabelFitRange,"Fit range");
		
		
		
		public ChannelWidget()
			{
			setLayout(new GridLayout(1,4));
		
			JPanel contrastPanel=new JPanel(new BorderLayout());
			contrastPanel.setBorder(BorderFactory.createEtchedBorder());
			contrastPanel.add(new JLabel(iconLabelContrast), BorderLayout.WEST);
			contrastPanel.add(sliderContrast,BorderLayout.CENTER);

			JPanel brightnessPanel=new JPanel(new BorderLayout());
			brightnessPanel.setBorder(BorderFactory.createEtchedBorder());
			brightnessPanel.add(new JLabel(iconLabelBrightness), BorderLayout.WEST);
			brightnessPanel.add(sliderBrightness,BorderLayout.CENTER);

			add(contrastPanel);
			add(EvSwingUtil.layoutLCR(
					null,
					brightnessPanel,
					EvSwingUtil.layoutEvenHorizontal(bFitRange)
					));

			bFitRange.addActionListener(this);
			
			sliderContrast.addSnapListener(this);
			sliderBrightness.addSnapListener(this);
			}
		

		
		private double brightness=0;
		private double contrast=0.1;

		public void slideChange(JSnapBackSlider source, int change)
			{
			if(source==sliderBrightness)
				{
				brightness+=change;
				}
			else if(source==sliderContrast)
				{
				contrast*=Math.pow(2,change/1000.0);
				}
			updatePanelContrastBrightness();
			}
	
		public void updatePanelContrastBrightness()
			{
			imagePanel.setContrastBrightness(contrast, brightness);
			}
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==bFitRange)
				fitRange();
			else
				imagePanel.redrawPanel();
			}
		
		public void fitRange()
			{
			PlateWindowView.ValueRange range=imagePanel.getIntensityRange();
			if(range!=null)
				{
				contrast=255.0/(range.max-range.min);
				brightness=-range.min*contrast;
				updatePanelContrastBrightness();
				}
			}
		
		public void stateChanged(ChangeEvent e)
			{
			imagePanel.redrawPanel();
			}	

		public void resetSettings()
			{
			brightness=1;
			contrast=1;
			}
		}	


	
	
	
	public void updateWindowTitle()
		{
		setTitleEvWindow("Plate Analysis");
		}
	
	
	

	private void updateAttrCombo()
		{
		List<String> alist=getParticleAttributes();
		updateCombo(comboAttribute1,alist);
		updateCombo(comboAttribute2,alist);
		imagePanel.setAggrMethod(comboDisplay.getSelectedItem(), 
				(String)comboAttribute1.getSelectedItem(),
				(String)comboAttribute2.getSelectedItem());
		
		}
	
	public void updateLayoutCombo()
		{
		List<String> llist=new ArrayList<String>();
		llist.add(PlateWindowView.layoutByWellID);
		llist.addAll(getWellAttributes());
		updateCombo(comboLayout,llist);
		imagePanel.setLayoutMethod((String)comboLayout.getSelectedItem());
		}
	
	/**
	 * Called whenever data has been updated
	 */
	public void dataChangedEvent()
		{
		if(!disableDataChanged)
			{
			disableDataChanged=true;
			buildMenu();

			comboData.updateList();
			comboParticleMeasure.updateList();
			comboFlow.updateList();
			updateChannelCombo();
			updateAttrCombo();
			updateLayoutCombo();
			
			updateWells();

			updateWindowTitle();
			
			disableDataChanged=false;
			}
		}

	
	private EvData fakeData=new EvData();
	
	/**
	 * Update which wells exist, and their content
	 */
	public void updateWells()
		{
		imagePanel.clearWells();
		EvContainer con=comboData.getSelectedObject();

		ParticleMeasure pm=getParticleMeasure();
		imagePanel.setParticleMeasure(pm);
		
		EvPath pathToFlow=comboFlow.getSelectedPath();
		imagePanel.setFlow(pathToFlow);
		
		
		//TODO different channel, z, time etc?

		
		HashSet<EvPath> wellPaths=new HashSet<EvPath>();
		if(con!=null)
			{
			Map<EvPath, EvChannel> m=con.getIdObjectsRecursive(EvChannel.class);

			//Add wells to panel, from images
			String curChannel=(String)comboChannel.getSelectedItem();
			if(curChannel==null)
				curChannel="";
			for(EvPath p:m.keySet())
				{
				String ch=p.getLeafName();
				if(ch.equals(curChannel))
					{
					EvPath path=p.getParent();
					wellPaths.add(path);
					imagePanel.addWell(path, m.get(p));
					}
				}
			
			}

		//Add remaining wells to panel, from PM
		if(pm!=null)
			{
			for(String s:pm.getWellNames())
				{
				EvPath path=EvPath.parse(fakeData, s);   //////////////// TODO THIS IS REALLY NASTY
				if(!wellPaths.contains(path))
					{
					wellPaths.add(path);
					imagePanel.addWell(path, null);
					}
				}
			}
		
		//Update panel
		imagePanel.layoutWells();
		}


	private void updateChannelCombo()
		{
		EvContainer con=comboData.getSelectedObject();
		if(con!=null)
			{
			Map<EvPath, EvChannel> m=con.getIdObjectsRecursive(EvChannel.class);
			
			//Update channel combo
			TreeSet<String> chans=new TreeSet<String>();
			for(EvPath p:m.keySet())
				chans.add(p.getLeafName());
			LinkedList<String> listchans=new LinkedList<String>();
			listchans.addAll(chans);
			updateCombo(comboChannel, listchans);
			}
		}

	/**
	 * Update the options of a combobox 
	 */
	private boolean updateCombo(JComboBox cb, List<String> alist)
		{
		//First check if anything has changed at all. If not, leave it be. This removes a lot of potential flicker
		if(!comboNeedsUpdate(cb, alist))
			{
			String item=(String)cb.getSelectedItem();
			cb.removeAllItems();
			for(String s:alist)
				cb.addItem(s);
			if(item!=null)
				{
				int index=alist.indexOf(item);
				if(index!=-1)
					cb.setSelectedIndex(index);
				}
			return true;
			}
		else
			return false;
		}
	
	/**
	 * Check if the combobox contains the given list of items
	 */
	private boolean comboNeedsUpdate(JComboBox cb, List<String> alist)
		{
		if(cb.getItemCount()!=alist.size())
			return false;
		for(int i=0;i<cb.getItemCount();i++)
			{
			if(!cb.getItemAt(i).equals(alist.get(i)))
				return false;
			}
		return true;
		}
	
	
	/**
	 * Get available particle attributes
	 */
	private List<String> getParticleAttributes()
		{
		LinkedList<String> list=new LinkedList<String>();
		ParticleMeasure pm=getParticleMeasure();
		if(pm!=null)
			{
			list.addAll(pm.getParticleColumns());
//			list.remove("source");   //TODO what is this?
			}
		return list;
		}

	/**
	 * Get available particle well attributes
	 */
	private List<String> getWellAttributes()
		{
		LinkedList<String> list=new LinkedList<String>();
		ParticleMeasure pm=getParticleMeasure();
		if(pm!=null)
			list.addAll(pm.getWellColumns());
		return list;
		}

	
	public ParticleMeasure getParticleMeasure()
		{
		return comboParticleMeasure.getSelectedObject();
		}
	
	
	/**
	 * Upon state changes, update the window
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
		{
		imagePanel.redrawPanel();
		}	
	
	
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==comboData || e.getSource()==comboChannel || e.getSource()==comboParticleMeasure || e.getSource()==comboDisplay)
			dataChangedEvent();
		else if(e.getSource()==comboFlow)        //In some of these cases, possible to do better?
			dataChangedEvent();
		else if(e.getSource()==comboAttribute1 || e.getSource()==comboAttribute2)
			{
			updateAttrCombo();
			}
		else if(e.getSource()==comboLayout)
			{
			updateLayoutCombo();
			}
		else if(e.getSource()==miZoom)
			imagePanel.zoomToFit();
		else if(e.getSource()==miSize100)
			imagePanel.setThumbnailImageSize(100);
		else if(e.getSource()==miSize200)
			imagePanel.setThumbnailImageSize(200);
		else if(e.getSource()==miSize300)
			imagePanel.setThumbnailImageSize(300);
		else if(e.getSource()==miSize500)
			imagePanel.setThumbnailImageSize(500);
		else if(e.getSource()==miSizeOrig)
			imagePanel.setThumbnailImageSize(null);
		else if(e.getSource()==miImportCSV)
			importCSV();
		else if(e.getSource()==miExportCSV)
			exportCSV();
		else if(e.getSource()==miExportSQL)
			exportSQL();
		else if(e.getSource()==miReevaluate)
			imagePanel.clearPM();
		else if(e.getSource()==bAddToQueue)
			addToQueue();
		}

	
	
	
	
	public void windowEventUserLoadedFile(EvData data)
		{
		dataChangedEvent();
		}

	public void windowFreeResources()
		{
		imagePanel.freeResources();
		}
	
		

	
	
	
	

	public EvDecimal getFrame()
		{
		return frameControl.getFrame();
		}


	public EvDecimal getZ()
		{
		return frameControl.getModelZ();
		}


	public void setFrame(EvDecimal frame)
		{
		frameControl.setFrame(frame);
		updateImagePanelFrameZ();
		}


	public void setZ(EvDecimal z)
		{
		frameControl.setZ(z);
		updateImagePanelFrameZ();
		}

	
	public void updateImagePanelFrameZ()
		{
		imagePanel.setFrameZ(getFrame(), getZ());
		}
	
	
	
	private File removeCSVending(File f)
		{
		String a1=".pm.particle.csv";
		String a2=".pm.frame.csv";
		String a3=".pm.well.csv";
				
		if(f.getName().endsWith(a1))
			return new File(f.getParentFile(), f.getName().substring(0,f.getName().length()-a1.length()));
		else if(f.getName().endsWith(a2))
			return new File(f.getParentFile(), f.getName().substring(0,f.getName().length()-a2.length()));
		else if(f.getName().endsWith(a3))
			return new File(f.getParentFile(), f.getName().substring(0,f.getName().length()-a3.length()));
		else
			return f;
		}
	
	/**
	 * Export data to CSV
	 */
	private void exportCSV()
		{
		ParticleMeasure pm=getParticleMeasure();
		if(pm!=null)
			{
			File f=openDialogSaveFile(""); 
			if(f!=null)
				{
				f=removeCSVending(f);
				File fPerParticle=new File(f.getParentFile(), f.getName()+".pm.particle.csv");
				File fPerFrame=new File(f.getParentFile(), f.getName()+".pm.frame.csv");
				File fPerWell=new File(f.getParentFile(), f.getName()+".pm.well.csv");
				
				try
					{
					FileWriter fwPerParticle=new FileWriter(fPerParticle);
					ParticleMeasureIO.writeCSVperparticle(pm, fwPerParticle, true, "\t", true);
					fwPerParticle.close();
					
					FileWriter fwPerFrame=new FileWriter(fPerFrame);
					ParticleMeasureIO.writeCSVperframe(pm, fwPerFrame, true, "\t", true);
					fwPerFrame.close();

					FileWriter fwPerWell=new FileWriter(fPerWell);
					ParticleMeasureIO.writeCSVperwell(pm, fwPerWell, true, "\t", true);
					fwPerWell.close();
					}
				catch (IOException e)
					{
					showErrorDialog("Error saving file: "+e.getMessage());
					EvLog.printError(e.getMessage(), e);
					}
				}
			}
		else
			showErrorDialog("No particle data is selected");
		}

	
	/**
	 * Import data from CSV
	 */
	private void importCSV()
		{
		ParticleMeasure pm=getParticleMeasure();
		if(pm!=null)
			{
			File f=openDialogOpenFile(); 
			if(f!=null)
				{
				try
					{
					FileReader reader=new FileReader(f);
					ParticleMeasureIO.readCSV(pm, reader, '\t');
					reader.close();
					}
				catch (IOException e)
					{
					showErrorDialog("Error reading file: "+e.getMessage());
					EvLog.printError(e.getMessage(), e);
					}
				dataChangedEvent();
				}
			}
		else
			showErrorDialog("No particle data is selected");
		}

	
	private void exportSQL()
		{
		ParticleMeasure pm=getParticleMeasure();
		if(pm!=null)
			{
			EvSQLConnection conn=EvDialogChooseSQL.openDialog();
			if(conn!=null)
				{
				String tablename=JOptionPane.showInputDialog("Name of table?");
				if(tablename!=null)
					{
					String dataid=JOptionPane.showInputDialog("Data id?");
					if(dataid!=null)
						{
						try
							{
							ParticleMeasureIO.createSQLtable(pm, conn, dataid, tablename);
							}
						catch (SQLException e){}
						try
							{
							ParticleMeasureIO.insertIntoSQLtable(pm, conn, dataid, tablename);
							}
						catch (SQLException e)
							{
							showErrorDialog("Could not insert into sql: "+e.getMessage());
							EvLog.printError(e);
							}
						}
					}
				
				}
			}
		else
			showErrorDialog("No particle data is selected");
		}
	

	/**
	 * Testing
	 * @param args
	 */
	public static void main(String[] args)
		{
		
		EvLog.addListener(new EvLogStdout());
	/*	
		EV.loadPlugins();
*/
		new PlateWindow();
		
//		EvData d=EvData.loadFile(new File("/media/753C-F3A6/20121001_plate1"));
		EvData d=new EvData();
		try
			{
			new EvIODataBD(d, new File("/win/images/20121001_plate1"));
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		
		ParticleMeasure pm=new ParticleMeasure();
		d.metaObject.put("pm",pm);

		for(String letter:new String[]{"B","C","D","E","F"})
			for(String num:new String[]{"02","03","04","05","06","07","08","09","10"})
				{
				String wellName="#<unnamed>/"+letter+num;
//				m.put("source", );

				ParticleMeasure.Well pmw=new Well();
				pm.setWell(wellName, pmw);
				
				ParticleMeasure.Frame fi=new ParticleMeasure.Frame();
				pmw.setFrame(EvDecimal.ZERO, fi);
//				pm.addColumn("source");
				pm.addParticleColumn("a");
				pm.addParticleColumn("b");

				int id=0;
				for(int i=0;i<100;i++)
					{
					ParticleMeasure.ColumnSet m=fi.getCreateParticle(id++);
					double r=Math.random();
					m.put("a", r);
					m.put("b", r+Math.random());
					}

				}
		
		EvDataGUI.registerOpenedData(d);
		}
	

	@Override
	public String windowHelpTopic()
		{
		return "High-throughput screenings with multi-well plates";
		}

	
	public void attributesMayHaveUpdated()
		{
		updateAttrCombo();
		updateLayoutCombo();
		}
	
	public WellRunnable getNextBackgroundTask()
		{
		return queue.getNextBackgroundTask();
		}
	

	private void addToQueue()
		{
		EvPath pathData=comboData.getSelectedPath();
		EvPath pathFlow=comboFlow.getSelectedPath();
		EvPath pathOutput=comboParticleMeasure.getSelectedPath();

		comboFlow.getSelectedPath();
		
		if(pathData!=null && pathFlow!=null && pathOutput!=null)
			{
			queue.addJob(pathData, pathFlow, pathOutput);
			imagePanel.startWorkerThread();
			}
		}

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	
	
	private static ImageIcon iconMultiwell=new ImageIcon(PlateWindow.class.getResource("multiwell.png"));
	
	public static void initPlugin() {}
	static
		{
		EvBasicWindow.addBasicWindowExtension(new EvBasicWindowExtension()
			{
			public void newBasicWindow(EvBasicWindow w)
				{
				w.addHook(this.getClass(),new Hook());
				}
			class Hook implements EvBasicWindowHook, ActionListener
				{
				public void createMenus(EvBasicWindow w)
					{
					JMenuItem mi=new JMenuItem("Plate analysis",iconMultiwell);
					mi.addActionListener(this);
					w.addMenuWindow(mi);
					}
				
				public void actionPerformed(ActionEvent e) 
					{
					new PlateWindow();
					}
				
				public void buildMenu(EvBasicWindow w){}
				}
			});
		}
	
	

	}

