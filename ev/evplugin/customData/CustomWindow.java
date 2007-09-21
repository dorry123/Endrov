package evplugin.customData;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;


import evplugin.basicWindow.*;
import evplugin.ev.*;
import evplugin.metadata.*;
import org.jdom.*;

//TODO: auto-replicate down to metadata

/**
 * Adjust Frame-Time mapping
 * @author Johan Henriksson
 */
public class CustomWindow extends BasicWindow 
implements ActionListener, ChangeListener, ObjectCombo.comboFilterMetaObject, TreeSelectionListener, TableModelListener
	{
	static final long serialVersionUID=0;

	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new CustomBasic());
		
		EV.personalConfigLoaders.put("CustomWindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				try	{new CustomWindow(BasicWindow.getXMLbounds(e));}
				catch (Exception e1) {e1.printStackTrace();}
				}
			public void savePersonalConfig(Element e){}
			});
		
		}
	
	//GUI components
	private ObjectCombo objectCombo=new ObjectCombo(this, true);
	private CustomTreeModel treeModel=new CustomTreeModel();
	private JTree tree=new JTree(treeModel);
	private JPanel treeFields=new JPanel();
	
	private CustomTableModel tableModel=new CustomTableModel();
	private JTable table=new JTable(tableModel);
	
	private JButton btRemoveEntry=new JButton("Remove entry");
	private JButton btInsertEntry=new JButton("Insert entry");
	private JButton btRemoveColumn=new JButton("Remove column");
	private JButton btInsertColumn=new JButton("Insert column");
	
	
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowPersonalSettings(Element root)
		{
		Element e=new Element("CustomWindow");
		setXMLbounds(e);
		root.addContent(e);
		}

	

	/**
	 * Make a new window at default location
	 */
	public CustomWindow()
		{
		this(new Rectangle(100,100,1000,600));
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public CustomWindow(Rectangle bounds)
		{		
		objectCombo.addActionListener(this);
		tree.addTreeSelectionListener(this);

		btInsertColumn.addActionListener(this);
		btRemoveColumn.addActionListener(this);
		btInsertEntry.addActionListener(this);
		btRemoveEntry.addActionListener(this);
		
		JScrollPane treeScroll=new JScrollPane(tree);
		JPanel treePanel=new JPanel(new BorderLayout());
		treePanel.add(treeScroll,BorderLayout.CENTER);
		treePanel.add(treeFields,BorderLayout.SOUTH);

		
		JPanel tablePanel=new JPanel(new BorderLayout());
		JScrollPane tableScroll=new JScrollPane(table);
		JPanel tableBottom=new JPanel(new GridLayout(1,4));
		tableBottom.add(btInsertEntry);
		tableBottom.add(btRemoveEntry);
		tableBottom.add(btInsertColumn);
		tableBottom.add(btRemoveColumn);
		tablePanel.add(tableScroll,BorderLayout.CENTER);
		tablePanel.add(tableBottom,BorderLayout.SOUTH);
		
		setLayout(new BorderLayout());
		JTabbedPane tabs=new JTabbedPane();
		tabs.addTab("Tree", treePanel);
		tabs.addTab("Table", tablePanel);
		add(objectCombo, BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
		
		//Update GUI
		fillTreeAttributesPane((CustomTreeElement)treeModel.getRoot());
		
		//Window overall things
		setTitle(EV.programName+" Custom Data");
		pack();
		setVisible(true);
		setBounds(bounds);
		}

	/**
	 * For combo box - which meta objects to list
	 */
	public boolean comboFilterMetaObjectCallback(MetaObject ob)
		{
		return ob instanceof CustomObject;
		}
	/**
	 * Add special options for the combo box
	 */
	public ObjectCombo.Alternative[] comboAddAlternative(final ObjectCombo combo, final Metadata meta)
		{
		return new ObjectCombo.Alternative[]{};
		}

	
	/**
	 * Callback: selection in tree changed
	 */
	public void valueChanged(TreeSelectionEvent e2)
		{
		TreePath p=tree.getSelectionPath();
		if(p==null)
			{
			fillTreeAttributesPane(null);
			tableModel.setRoot(objectCombo.getObject(), null);
			}
		else
			{
			CustomTreeElement e=(CustomTreeElement)p.getLastPathComponent();
			fillTreeAttributesPane(e);
			tableModel.setRoot(objectCombo.getObject(), e.e);
			}
		}

	
	/**
	 * Update GUI: attribute pane for tree
	 */
	public void fillTreeAttributesPane(final CustomTreeElement e)
		{
		treeFields.removeAll();
		if(e!=null && e.e!=null)
			{
			java.util.List attr=e.e.getAttributes();
			treeFields.setLayout(new GridLayout(attr.size()+2,1));
			
			//The value
			//what about trimming?
			JPanel p2=new JPanel(new BorderLayout());
			JTextField cf=new JTextField(e.e.getText());
			p2.add(new JLabel("Value:"));
			p2.add(cf,BorderLayout.CENTER);
			treeFields.add(cf);
			
			//Every attribute
			for(Object o:attr)
				{
				final Attribute a=(Attribute)o;
				JPanel p=new JPanel(new BorderLayout());
				p.add(new JLabel(a.getName()+":"),BorderLayout.WEST);
				JTextField tf=new JTextField(a.getValue());
				p.add(tf,BorderLayout.CENTER);
				JButton bremove=new JButton("X");
				p.add(bremove, BorderLayout.EAST);
				treeFields.add(p);
				
				bremove.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e2)
						{
						TreePath p=e.getPath();
						e.e.removeAttribute(a.getName());
						treeModel.updateElement(e);
						tree.setSelectionPath(p);    //should not be needed
						}
					});
				}

			
			//Buttons below
			JPanel p3=new JPanel(new GridLayout(1,3));
			JButton bNewField=new JButton("Add field");
			JButton bNewChild=new JButton("Add child");
			JButton bRemove=new JButton("Remove element");
			p3.add(bNewChild);
			p3.add(bNewField);
			p3.add(bRemove);
			treeFields.add(p3);
			
			bNewChild.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e2)
					{
					String name=JOptionPane.showInputDialog("Name of child");
					if(name!=null)
						{
						TreePath p=e.getPath();
						treeModel.addChild(e, new Element(name));
						tree.setSelectionPath(p);    //should not be needed
						objectCombo.getObject().metaObjectModified=true;
						}
					}
				});
				
			bNewField.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e2)
					{
					String name=JOptionPane.showInputDialog("Name of field");
					if(name!=null)
						{
						e.e.setAttribute(name, "");
						TreePath p=e.getPath();
						treeModel.updateElement(e);
						tree.setSelectionPath(p);    //should not be needed
						objectCombo.getObject().metaObjectModified=true;
						}
					}
				});
			
			bRemove.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e2)
					{
					CustomTreeElement parent=e.parent;
					if(parent!=null)
						{
						parent.e.removeContent(e.e);
						treeModel.emitAllChanged();
						tree.setSelectionPath(parent.getPath());
						objectCombo.getObject().metaObjectModified=true;
						}
					}
				});
			
			}
		setVisible(true);
		}
	
	
	
	
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==objectCombo)
			{
			treeModel.setMetaObject((CustomObject)objectCombo.getObject());
			
			}
		else if(e.getSource()==btInsertEntry)
			{
			//TODO
			
			}
		else if(e.getSource()==btRemoveEntry)
			{
			int row=table.getSelectedRow();
			if(row!=-1)
				tableModel.removeRow(row);
			}
		else if(e.getSource()==btInsertColumn)
			{
			
			
			//TODO
			}
		else if(e.getSource()==btRemoveColumn)
			{
			int col=table.getSelectedColumn();
			if(col!=-1)
				tableModel.removeColumn(col);
			}
		
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
		{
		
//		fillGraphpart();
		}
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		objectCombo.updateObjectList();
		//copy list
		fillTreeAttributesPane((CustomTreeElement)treeModel.getRoot());
		}



	public void tableChanged(TableModelEvent e)
		{
		if(e.getType()==TableModelEvent.INSERT)
			{
			
			}
		else if(e.getType()==TableModelEvent.DELETE)
			{
			
			}
		else if(e.getType()==TableModelEvent.UPDATE)
			{
			
			}
		//TODO: update tree
		}
	
	
	
	
	}