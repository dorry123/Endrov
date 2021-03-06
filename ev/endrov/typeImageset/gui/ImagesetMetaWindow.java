/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeImageset.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.jdom.*;

import endrov.core.*;
import endrov.core.log.EvLog;
import endrov.data.*;
import endrov.gui.component.EvComboObjectOne;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.typeImageset.EvChannel;
import endrov.typeImageset.Imageset;


/**
 * Meta data window for imageset
 * @author Johan Henriksson
 */
public class ImagesetMetaWindow extends EvBasicWindow implements ActionListener, DocumentListener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	
	private EvComboObjectOne<Imageset> metaCombo=new EvComboObjectOne<Imageset>(new Imageset(),false,true);
	
	
	
	private Vector<ChannelTab> channels=new Vector<ChannelTab>();

	private JTabbedPane tabs=null;
	private JTextField commonSlicespacing=new JTextField();
	private JTextField commonObjective=new JTextField();
	private JTextField commonNA=new JTextField();
	private JTextField commonOptivar=new JTextField();
	private JTextField commonCampix=new JTextField();
	private JTextField commonTimestep=new JTextField();
	private JTextField commonSample=new JTextField();
	private JTextArea  commonDescript=new JTextArea();
	private JTextField commonCalcResX=new JTextField();
	private JTextField commonCalcResY=new JTextField();
	private JTextField commonCalcResZ=new JTextField();
	private JTextField commonManResX=new JTextField();
	private JTextField commonManResY=new JTextField();
	private JTextField commonManResZ=new JTextField();
	private JTextField commonResX=new JTextField();
	private JTextField commonResY=new JTextField();
	private JTextField commonResZ=new JTextField();
	
	private boolean updatingFields=false;
		
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowSavePersonalSettings(Element root)
		{
		}
	@Override
	public void windowLoadPersonalSettings(Element e)
		{
		}


	
	/**
	 * Make a new window at some specific location
	 */
	public ImagesetMetaWindow()
		{
		//Listeners
		metaCombo.addActionListener(this);

		commonSlicespacing.getDocument().addDocumentListener(this);
		commonObjective.getDocument().addDocumentListener(this);
		commonNA.getDocument().addDocumentListener(this);
		commonOptivar.getDocument().addDocumentListener(this);
		commonCampix.getDocument().addDocumentListener(this);
		commonTimestep.getDocument().addDocumentListener(this);
		commonSample.getDocument().addDocumentListener(this);
		commonDescript.getDocument().addDocumentListener(this);
		commonManResX.getDocument().addDocumentListener(this);
		commonManResY.getDocument().addDocumentListener(this);
		commonManResZ.getDocument().addDocumentListener(this);
		
		//GUI overall layout
		tabs=new JTabbedPane();
		setLayout(new BorderLayout());
		add(metaCombo,BorderLayout.SOUTH);
		add(tabs, BorderLayout.CENTER);

		//Create tabs		
		readFromMetadata();
		
		//Show
		setTitleEvWindow("Imageset metadata");
		packEvWindow();
		setBoundsEvWindow(new Rectangle(100,100,500,500));
		setVisibleEvWindow(true);
		}
	
	
	private void readFromMetadata()
		{
		Imageset rec=metaCombo.getSelectedObject();
		updatingFields=true;
		
		//Or just remember current tab?
		int currentIndex=tabs.getSelectedIndex();

		//Remove all old tabs
		tabs.removeAll();
		channels.removeAllElements();

		//Create new tabs
		if(rec!=null)
			{
			//Create common tab
			createCommon();		
	
			/*
			commonManResX.setText(""+rec.resX);
			commonManResY.setText(""+rec.resY);
			commonManResZ.setText(""+rec.resZ);
			
			commonSlicespacing.setText(""+rec.metaSlicespacing);
			commonObjective.setText(""+rec.metaObjective);
			commonNA.setText(""+rec.metaNA);
			commonOptivar.setText(""+rec.metaOptivar);
			commonCampix.setText(""+rec.metaCampix);
			commonTimestep.setText(""+rec.metaTimestep);
			*/
			commonSample.setText(rec.getSampleID());
			commonDescript.setText(rec.getDescription());
			
			//Add channel tabs
			for(String channelName:rec.getChannels().keySet())
				{
				//System.out.println("meta123: "+channelName);
				tabs.add(channelName, new ChannelTab(this,channelName,rec.getChannel(channelName)));
				}
			if(currentIndex>=0 && currentIndex<tabs.getComponentCount())
				tabs.setSelectedIndex(currentIndex);
			
			for(ChannelTab t:channels)
				{
				EvChannel cm=rec.getChannel(t.channelName);
				t.iDispX.setText(""+cm.defaultDisp.x);
				t.iDispY.setText(""+cm.defaultDisp.y);			
				t.iBinning.setText(""+cm.chBinning);
				t.iCompression.setText(""+cm.compression);
				t.iOther.setText(cm.metaOther.get("evother"));
				}
			}
		updatingFields=false;
		updateRes();
		}
	
	/**
	 * Create common metadata tab
	 */
	private void createCommon()
		{
		int cury=0;
		Insets ins=new Insets(0, 0, 0, 0);
		JPanel p=new JPanel();
		tabs.add("Common", p);
		p.setLayout(new GridBagLayout());
		add3Fast(p, new JLabel("Slicespacing"),            commonSlicespacing, new JLabel("[um/slice]"),cury++);
		add3Fast(p, new JLabel("Objective magnification"), commonObjective,    new JLabel("[x]"),       cury++);
		add3Fast(p, new JLabel("NA"),                      commonNA,           new JLabel("[]"),        cury++);
		add3Fast(p, new JLabel("Optivar"),           commonOptivar,         new JLabel("[x]"),       cury++);
		add3Fast(p, new JLabel("Camera pixel size"), commonCampix,          new JLabel("[um/px]"),   cury++);
		add3Fast(p, new JLabel("Time step"),         commonTimestep,        new JLabel("[s]"),       cury++);
		add3Fast(p, new JLabel("Sample"),            commonSample,          new JLabel(""),          cury++);
	//	add3Fast(p, new JLabel(""), , new JLabel(""),0);
		
		JPanel p1=makeResolutionStrip("Calculated resolution [px/um]", commonCalcResX, commonCalcResY, commonCalcResZ);
		p.add(p1, new GridBagConstraints(0,cury++,3,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,ins,0,0));		
		JPanel p2=makeResolutionStrip("Override of resolution [px/um]", commonManResX, commonManResY, commonManResZ);
		p.add(p2, new GridBagConstraints(0,cury++,3,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,ins,0,0));
		JPanel p3=makeResolutionStrip("Final resolution [px/um]", commonResX, commonResY, commonResZ);
		p.add(p3, new GridBagConstraints(0,cury++,3,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,ins,0,0));
		p.add(new JLabel("Description"),new GridBagConstraints(0,cury++,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,ins,0,0));
		
		JScrollPane scrollPane = new JScrollPane(commonDescript, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		p.add(scrollPane,	new GridBagConstraints(0,cury++,3,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,ins,0,0));

		commonSlicespacing.setPreferredSize(new Dimension(400,22));
		scrollPane.setPreferredSize(new Dimension(0,100));

		commonCalcResX.setEditable(false);
		commonCalcResY.setEditable(false);
		commonCalcResZ.setEditable(false);
		commonResX.setEditable(false);
		commonResY.setEditable(false);
		commonResZ.setEditable(false);
		}
	
	
	
	
	
	/**
	 * Update calculated resolution
	 */
	private void updateRes()
		{
		if(!updatingFields)
			{
			updatingFields=true;
			//Imageset rec=metaCombo.getSelectedObjectNotNull();
		
			/*
			double calcResX=rec.metaObjective*rec.metaOptivar/rec.metaCampix; //[]*[]/[um/px]
			double calcResY=calcResX;
			double calcResZ=1.0/rec.metaSlicespacing;
			commonCalcResX.setText(""+calcResX);
			commonCalcResY.setText(""+calcResY);
			commonCalcResZ.setText(""+calcResZ);
	
			if(commonCalcResX.getText().equals(commonManResX.getText()))	commonManResX.setText("");
			if(commonCalcResY.getText().equals(commonManResY.getText()))	commonManResY.setText("");
			if(commonCalcResZ.getText().equals(commonManResZ.getText()))	commonManResZ.setText("");
			//java.lang.IllegalStateException: above TODO
			double manResX=parseDoubleOr0(commonManResX.getText());
			double manResY=parseDoubleOr0(commonManResY.getText());
			double manResZ=parseDoubleOr0(commonManResZ.getText());
	
			if(manResX==0) rec.resX=calcResX; else rec.resX=manResX;
			if(manResY==0) rec.resY=calcResY; else rec.resY=manResY;
			if(manResZ==0) rec.resZ=calcResZ; else rec.resZ=manResZ;
			
			
			commonResX.setText(""+rec.resX);
			commonResY.setText(""+rec.resY);
			commonResZ.setText(""+rec.resZ);
	*/
			updatingFields=false;
			}
		}
	
	/**
	 * Parse a string as double. Return 0 if it fails
	 */
	/*
	private double parseDoubleOr0(String s)
		{
		try {return Double.parseDouble(s);}
		catch(Exception e) {return 0;}
		}*/
	
	
	
	/**
	 * Tab for a channel
	 */
	private class ChannelTab extends JPanel
		{
		static final long serialVersionUID=0;
		
		public String channelName;
		public JTextField iDispX=new JTextField();
		public JTextField iDispY=new JTextField();
		public JTextField iBinning=new JTextField();
		public JTextField iCompression=new JTextField();
		public JTextArea iOther=new JTextArea();
		
		public ChannelTab(ImagesetMetaWindow w, String cname, EvChannel c)
			{
			int cury=0;
			tabs.add(cname, this);
			channelName=cname;
			channels.add(this);
			setLayout(new GridBagLayout());
			iDispX.setPreferredSize(new Dimension(300,24));
			JScrollPane scrollPane = new JScrollPane(iOther, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setPreferredSize(new Dimension(600,300));
			add3Fast(this, new JLabel("Displacement X"), iDispX,   new JLabel("[px]"),cury++);
			add3Fast(this, new JLabel("Displacement Y"), iDispY,   new JLabel("[px]"),cury++);
			add3Fast(this, new JLabel("Binning"),        iBinning, new JLabel("[x]"), cury++);
			add3Fast(this, new JLabel("Compression"),    iCompression, new JLabel("[0-100]"), cury++);
			Insets ins=new Insets(0, 0, 0, 0);
			add(scrollPane, new GridBagConstraints(0,cury++,3,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,ins,0,0));
			
			iDispX.getDocument().addDocumentListener(w);
			iDispY.getDocument().addDocumentListener(w);
			iBinning.getDocument().addDocumentListener(w);
			iCompression.getDocument().addDocumentListener(w);
			iOther.getDocument().addDocumentListener(w);
			}
		
		
		}


	/**
	 * Add a c1-c2-c3 in gridbag cointainer c
	 */
	private static void add3Fast(Container c, Component in1, Component in2, Component in3, int y)
		{
		Insets ins=new Insets(0, 0, 0, 0);
		c.add(in1, new GridBagConstraints(0,y,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.NONE,ins,0,0));
		c.add(in2, new GridBagConstraints(1,y,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,ins,0,0));
		c.add(in3, new GridBagConstraints(2,y,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.NONE,ins,0,0));
		}
	
	/**
	 * Create a XYZ resolution GUI component out of 3 existing components
	 */
	private static JPanel makeResolutionStrip(String title, JTextField x, JTextField y, JTextField z)
		{
		JPanel p=new JPanel(new GridBagLayout());
		p.setBorder(new TitledBorder(title));
		Insets ins=new Insets(0, 0, 0, 0);
		p.add(new JLabel("X:"), new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,ins,0,0));
		p.add(new JLabel("Y:"), new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,ins,0,0));
		p.add(new JLabel("Z:"), new GridBagConstraints(4,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,ins,0,0));
		p.add(x, new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,ins,0,0));
		p.add(y, new GridBagConstraints(3,0,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,ins,0,0));
		p.add(z, new GridBagConstraints(5,0,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,ins,0,0));
		x.setPreferredSize(new Dimension(200,22));
		y.setPreferredSize(new Dimension(200,22));
		z.setPreferredSize(new Dimension(200,22));
		return p;
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==metaCombo)
			readFromMetadata();
		}

	
	/**
	 * Take fields, make meta out of them
	 *
	 */
	public void fieldsToMeta()
		{
		Imageset rec=metaCombo.getSelectedObject();
//		Imageset.castNull(metaCombo.getObject());
		if(rec!=null && !updatingFields)
			{
			try
				{
				updateRes();
				/*
				rec.resX=Double.parseDouble(commonResX.getText());
				rec.resY=Double.parseDouble(commonResY.getText());
				rec.resZ=Double.parseDouble(commonResZ.getText());

				rec.metaSlicespacing=Double.parseDouble(commonSlicespacing.getText());
				rec.metaObjective=Double.parseDouble(commonObjective.getText());
				rec.metaNA=Double.parseDouble(commonNA.getText());
				rec.metaOptivar=Double.parseDouble(commonOptivar.getText());
				rec.metaCampix=Double.parseDouble(commonCampix.getText());
				rec.metaTimestep=Double.parseDouble(commonTimestep.getText());
				*/
				rec.setSampleID(commonSample.getText());
				rec.setDescription(commonDescript.getText());
				
				for(ChannelTab t:channels)
					{
					EvChannel ch=rec.getChannel(t.channelName);
					
					ch.defaultDisp.x=Double.parseDouble(t.iDispX.getText());
					ch.defaultDisp.y=Double.parseDouble(t.iDispY.getText());
					ch.chBinning=Integer.parseInt(t.iBinning.getText());
					ch.compression=Integer.parseInt(t.iCompression.getText());
					ch.metaOther.put("evother",t.iOther.getText());
					}
				
				EvBasicWindow.updateWindows(this);
				rec.setMetadataModified();
				}
			catch (NumberFormatException e)
				{
				EvLog.printError("NF exception in fieldsToMeta",e);
				}
			}
		}

	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		metaCombo.updateList();
		readFromMetadata();
		}

	public void insertUpdate(DocumentEvent e) {fieldsToMeta();}
	public void removeUpdate(DocumentEvent e) {fieldsToMeta();}
	public void changedUpdate(DocumentEvent e) {fieldsToMeta();}
	
	public void windowEventUserLoadedFile(EvData data){}
	public void windowFreeResources(){}

	@Override
	public String windowHelpTopic()
		{
		return null;
		}

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		/******************************************************************************************************
		 *                               BasicWindow extension                                                *
		 *****************************************************************************************************/
		EvBasicWindow.addBasicWindowExtension(new EvBasicWindowExtension()
			{
			public void newBasicWindow(final EvBasicWindow w)
				{
				w.addHook(this.getClass(),new EvBasicWindowHook()
					{
					public void createMenus(EvBasicWindow w)
						{
						JMenuItem miImagesetMeta=new JMenuItem("Imageset metadata",new ImageIcon(getClass().getResource("gnomeMetaWindow.png")));
						w.addMenuWindow(miImagesetMeta);
						miImagesetMeta.addActionListener(new ActionListener()
							{public void actionPerformed(ActionEvent e){new ImagesetMetaWindow();}});
						buildMenu(w);
						}

					public void buildMenu(EvBasicWindow w)
						{
						}
					});
				}
			});
		

		/******************************************************************************************************
		 *                               Personal Config                                                      *
		 *****************************************************************************************************/
		EndrovCore.addPersonalConfigLoader("lastImagesetPath",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				EvBasicWindow.setLastDataPath(new File(e.getAttributeValue("path")));
				}
			public void savePersonalConfig(Element root)
				{
				try
					{
					Element e=new Element("lastImagesetPath");
					e.setAttribute("path",EvBasicWindow.getLastDataPath().getPath());
					root.addContent(e);
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				}
			});

		
		/******************************************************************************************************
		 *                               Data menu extension                                                  *
		 *****************************************************************************************************/
		/*
		EvDataMenu.extensions.add(new DataMenuExtension()
			{
			public void buildOpen(JMenu menu)
				{
				
				}
			public void buildSave(JMenu menu, final EvData meta)
				{*/
				//TODO recode reload
				/*
				if(meta instanceof OstImageset)
					{
					final Imageset rec=(Imageset)meta;
					final JMenuItem miReload=new JMenuItem("Reload");
					ActionListener listener=new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							if(e.getSource()==miOpenDatadir)
								EV.openExternal(rec.datadir());
							else if(e.getSource()==miReload)
								{
								OstImageset ost=(OstImageset)rec;
								ost.invalidateDatabaseCache();
								ost.buildDatabase();
								BasicWindow.updateWindows();
								}
							}
						};
					miOpenDatadir.addActionListener(listener);
					miReload.addActionListener(listener);
					menu.add(miOpenDatadir);					
					menu.add(miReload);					
					}
					*/
				//TODO Replace with more general Save as
				/*
				else if(meta instanceof Imageset)
					{
//					final Imageset rec=(Imageset)meta;
					final JMenuItem miExportOST=new JMenuItem("Export to OST");
					ActionListener listener=new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							if(e.getSource()==miExportOST)
								{
								//Should maybe be registered instead?
								new SaveOSTDialog((Imageset)meta);
								}
							}
						};
					miExportOST.addActionListener(listener);
					menu.add(miExportOST);
					}*/
			/*	}
			});*/
		}

	}
