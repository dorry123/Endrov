package endrov.flow.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.vecmath.Vector2d;

import org.jdom.Document;
import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.ev.EvLog;
import endrov.flow.*;
import endrov.util.EvSwingUtil;
import endrov.util.EvXmlUtil;
import endrov.util.Tuple;

/**
 * Panel showing flow
 * @author Johan Henriksson
 *
 */
public class FlowPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener//, KeyListener
	{
	static final long serialVersionUID=0;

	private static final int connPointSnapDistance=10;
	private static final int lineSnapDistance=10;

	private int cameraX, cameraY;
	private Flow flow=new Flow();
	private FlowExec flowExec=new FlowExec();
	private boolean enabled=false;
	
	private Map<Tuple<FlowUnit, String>, ConnPoint> connPoint=new HashMap<Tuple<FlowUnit,String>, ConnPoint>();
	private int mouseLastX, mouseLastY; /* In camera coordinates */
	private int mouseLastDragX=0, mouseLastDragY=0;
	private Set<FlowUnit> movingUnits=new HashSet<FlowUnit>();
	private DrawingConn drawingConn=null;

	private FlowUnit placingUnit=null;
	private FlowConn stickyConn=null;
	private Rectangle2D selectRect=null;
	
	public Set<FlowUnit> selectedUnits=new HashSet<FlowUnit>();

	/**
	 * Every unit can have an assigned visible component. it should be created for every
	 * flow and instance once only and will be stored here.
	 */
	private HashMap<FlowUnit, Component> unitComponent=new HashMap<FlowUnit, Component>();
	
	
	/**
	 * Constructor
	 */
	public FlowPanel()
		{
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
		setEnabled(true);
		setFocusable(true);
/*		addKeyListener(this);*/
		setLayout(null);
		ToolTipManager.sharedInstance().registerComponent(this);
		}

	/**
	 * Set which flow to edit
	 */
	public void setFlow(Flow flow, EvData data, EvContainer parent, EvPath path)
		{
		enabled=flow!=null;
		if(flow==null)
			flow=new Flow();
		if(flow!=this.flow || data!=flowExec.getData() || parent!=flowExec.getParent() || !path.equals(flowExec.getPath()))
			{
			flowExec=new FlowExec();
			flowExec.setData(data);
			flowExec.setParent(parent);
			flowExec.setPath(path);
			}
		this.flow = flow;
		unitComponent.clear();
		removeAll();
		}

	/**
	 * Get currently edited flow
	 */
	public Flow getFlow()
		{
		return flow;
		}

	public FlowExec getFlowExec()
		{
		return flowExec;
		}
	
	/**
	 * Assign a flow unit to be placed
	 */
	public void setUnitToPlace(FlowUnit u)
		{
		if(enabled)
			placingUnit=u;
		}
	
	
	/**
	 * Call whenever component is panned, an object is added or removed
	 */
	private void doFlowSwingLayout()
		{
		if(flow!=null)
			{
			HashSet<FlowUnit> allUnit=new HashSet<FlowUnit>(flow.units);
			if(placingUnit!=null)
				allUnit.add(placingUnit);
			
			//Add units
			Set<FlowUnit> toAdd=new HashSet<FlowUnit>(allUnit);
			toAdd.removeAll(unitComponent.keySet());
			for(FlowUnit u:toAdd)
				getComponentForUnit(u);
	
			//Remove units
			Set<FlowUnit> toRemove=new HashSet<FlowUnit>(unitComponent.keySet());
			toRemove.removeAll(allUnit);
			for(FlowUnit u:toRemove)
				{
				Component c=unitComponent.get(u);
				if(c!=null)
					{
					remove(c);
					unitComponent.remove(u);
					}
				}
			
			//Set position and size of all components
			for(FlowUnit unit:allUnit)
				setUnitSize(unit);
			}
		
		
		}

	
	private void setUnitSize(FlowUnit unit)
		{
		Component c=unitComponent.get(unit);
		if(c!=null)
			{
			//offset?
			Dimension dim=c.getPreferredSize();
			Dimension dimMin=c.getMinimumSize();
			if(dim.width<dimMin.width)   dim.width=dimMin.width;
			if(dim.height<dimMin.height) dim.height=dimMin.height;
				
			c.setSize(dim);
			if(unit==placingUnit)
				c.setLocation(0, -dim.height-1000);
			else
				c.setLocation(unit.x-cameraX+unit.getGUIcomponentOffsetX(), unit.y-cameraY+unit.getGUIcomponentOffsetY());
			c.validate();
			}
		}
	
	/**
	 * Get component assigned to unit. Make sure it is there before you invoke this function
	 */
	private Component getComponentForUnit(FlowUnit u)
		{
		if(unitComponent.containsKey(u))
			return unitComponent.get(u);
		else
			{
			Component c=u.getGUIcomponent(this);
			unitComponent.put(u,c);
			if(c!=null)
				add(c);
			setUnitSize(u);
			return c;
			}
		}
	
	/**
	 * Render everything
	 */
	protected void paintComponent(Graphics g)
		{
		g.setColor(Color.WHITE);
		g.fillRect(0,0,getWidth(),getHeight());
		
		doFlowSwingLayout();

		if(flow!=null)
			{
			Graphics2D g2=(Graphics2D)g;
			g2.translate(-cameraX, -cameraY);
			
			//hm. clean up map of connection points?
			
			
			//Draw all units
			for(FlowUnit u:getFlow().units)
				u.paint(g2, this, unitComponent.get(u));
			
			//All connection points should now be in the list
			//Draw connection arrows
			connSegments.clear();
			LinkedList<FlowConn> delconn=new LinkedList<FlowConn>();
			for(FlowConn conn:getFlow().conns)
				{
				ConnPoint pFrom=connPoint.get(new Tuple<FlowUnit, String>(conn.fromUnit, conn.fromArg));
				ConnPoint pTo=connPoint.get(new Tuple<FlowUnit, String>(conn.toUnit, conn.toArg));
				if(pFrom==null || pTo==null)
					{
					EvLog.printError("Bad line, removing: "+conn,null);
					delconn.add(conn);
					}
				else
					{
					Vector2d vFrom=pFrom.pos;
					Vector2d vTo=pTo.pos;
					drawConnLine(g,vFrom,vTo,conn);
					}
				}
			getFlow().conns.removeAll(delconn);
			
			
			if(drawingConn!=null)
				{
				ConnPoint p=connPoint.get(new Tuple<FlowUnit, String>(drawingConn.t.fst(), drawingConn.t.snd()));
				Vector2d vFrom=p.pos;
				Vector2d vTo=drawingConn.toPoint;
				if(!p.isFrom)
					{
					Vector2d v=vFrom;
					vFrom=vTo;
					vTo=v;
					}
				drawConnLine(g,vFrom,vTo,null);
				}
			
			if(placingUnit!=null)
				{
				placingUnit.paint(g2,this,unitComponent.get(placingUnit));
				}
			
			//so, do NOT add 
			
			
			
			
			if(selectRect!=null)
				{
				g.setColor(Color.MAGENTA);
				g.drawRect((int)selectRect.getX(), (int)selectRect.getY(), 
						(int)selectRect.getWidth(), (int)selectRect.getHeight());
				}
			
			g2.translate(cameraX, cameraY);
			}
		}
	
	
	

	/**
	 * Move camera
	 */
	public void pan(int dx, int dy)
		{
		cameraX-=dx;
		cameraY-=dy;
		repaint();
		}
	

	public void mouseWheelMoved(MouseWheelEvent e){}

	public void mouseDragged(MouseEvent e)
		{
		int dx=(e.getX()-mouseLastDragX);
		int dy=(e.getY()-mouseLastDragY);
		mouseLastX=e.getX();
		mouseLastY=e.getY();
		
		//Update shape of selection rectangle
		if(selectRect!=null)
			{
			int x=(int)selectRect.getX();
			int y=(int)selectRect.getY();
			selectRect=new Rectangle(x,y,e.getX()+cameraX-x,e.getY()+cameraY-y);
			repaint();
			}
		
		//Pan
		if(SwingUtilities.isRightMouseButton(e))
			pan(dx,dy);

		//Move held unit
		if(!movingUnits.isEmpty() && SwingUtilities.isLeftMouseButton(e))
			{
			Set<FlowUnit> tomove=new HashSet<FlowUnit>();
			for(FlowUnit u:movingUnits)
				tomove.addAll(u.getSubUnits(getFlow()));

			for(FlowUnit u:tomove)
				{
				u.x+=dx;
				u.y+=dy;
				}
			repaint();
			}
		
		
		
		//Update shape of connection currently drawed
		if(drawingConn!=null)
			{
			int mx=e.getX()+cameraX;
			int my=e.getY()+cameraY;
			drawingConn.toPoint=new Vector2d(mx,my);
			repaint();
			}
		
		
		mouseLastDragX=e.getX();
		mouseLastDragY=e.getY();
		setToolTipText(null);
		}


	public void mouseMoved(MouseEvent e)
		{
		mouseLastX=e.getX();
		mouseLastY=e.getY();
		if(flow!=null)
			{
			Tuple<Vector2d, FlowConn> hoverSegment=getHoverSegment();

			
			
			//Update current position of the unit to be placed
			if(placingUnit!=null)
				{
				if(hoverSegment!=null)
					{
					placingUnit.x=(int)hoverSegment.fst().x;//+cameraX;
					placingUnit.y=(int)hoverSegment.fst().y;//+cameraY;
					stickyConn=hoverSegment.snd();
					}
				else
					{
					placingUnit.x=mouseLastX+cameraX;
					placingUnit.y=mouseLastY+cameraY;
					stickyConn=null;
					}
				Dimension dim=placingUnit.getBoundingBox(getComponentForUnit(placingUnit), flow);
				placingUnit.x-=dim.width/2;
				placingUnit.y-=dim.height/2;
				repaint();
				}

			int mx=e.getX()+cameraX;
			int my=e.getY()+cameraY;
			Tuple<Tuple<FlowUnit,String>,ConnPoint> tt=findHoverConnPoint(mx, my);

			
			if(tt!=null)
				{
				Tuple<FlowUnit,String> t=tt.fst();
				//Tell the mandated type if hovering a connection point
				FlowUnit hoverUnit=t.fst();
				Map<String,FlowType> types=hoverUnit.getTypesIn(flow);
				if(!types.containsKey(t.snd()))
					types=hoverUnit.getTypesOut(flow);
				if(types.containsKey(t.snd()))
					{
					FlowType type=types.get(t.snd());
					if(type!=null)
						setToolTipText("Should be "+type.toString());
					}
				else
					System.out.println("Error: Non-existing connection");
				}
			else if(hoverSegment!=null)
				{
				//Tell the type and value if hovering a segment
				FlowConn conn=hoverSegment.snd();
				Object output=flowExec.getLastOutput(conn.fromUnit).get(conn.fromArg);
				String typeString="";
				if(output!=null)
					typeString="("+output.getClass().getSimpleName()+": "+output+")";
				setToolTipText(""+conn.fromArg+" - "+conn.toArg+ " "+typeString);
				}
			else
				setToolTipText(null);
		
			}
		}


	/**
	 * Mouse button clicked
	 */
	public void mouseClicked(MouseEvent e)
		{
		final int mx=e.getX()+cameraX;
		final int my=e.getY()+cameraY;
		if(placingUnit!=null)
			{
			if(SwingUtilities.isLeftMouseButton(e))
				{
				getFlow().units.add(placingUnit);
				
				if(stickyConn!=null && !placingUnit.getTypesIn(flow).isEmpty() && !placingUnit.getTypesOut(flow).isEmpty())
					{
					String argin=placingUnit.getTypesIn(flow).keySet().iterator().next();
					String argout=placingUnit.getTypesOut(flow).keySet().iterator().next();

					getFlow().conns.remove(stickyConn);
					getFlow().conns.add(new FlowConn(stickyConn.fromUnit,stickyConn.fromArg,placingUnit,argin));
					getFlow().conns.add(new FlowConn(placingUnit,argout,stickyConn.toUnit,stickyConn.toArg));
					
					stickyConn=null;
					System.out.println("Placed sticky");
					}
				
				
				}
			placingUnit=null;
			repaint();
			}
		else
			{
			boolean hitAnything=false;
			
			for(final FlowUnit u:getFlow().units)
				if(!hitAnything && u.mouseHoverMoveRegion(mx,my,unitComponent.get(u), flow))
					{
					if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==1)
						{
						if(!selectedUnits.contains(u))
							{
							selectedUnits.clear();
							selectedUnits.add(u);
							repaint();
							}
						hitAnything=true;
						}
					else if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2)
						{
						u.editDialog();
						repaint();
						hitAnything=true;
						}
					else if(SwingUtilities.isRightMouseButton(e))
						{
						JPopupMenu popup = new JPopupMenu();
						
						JMenuItem itEval=new JMenuItem("Evaluate");
						itEval.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e)
								{
								try
									{
	//								u.evaluate(flow);
	
									u.updateTopBottom(getFlow(),flowExec);
	
									System.out.println(flowExec);
//									System.out.println(u.lastOutput);
									}
								catch (Exception e1)
									{
									e1.printStackTrace();
									}
								
								
								}
						});
	
						//TODO potential space leaks?
						JMenuItem itRemove=new JMenuItem("Remove unit");
						itRemove.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e)
								{
								getFlow().removeUnit(u);
								repaint();
								}
						});
	
						
						popup.add(itEval);
						popup.add(itRemove);
						popup.show(this,e.getX(),e.getY());
						hitAnything=true;
						}
					}

			
			
			if(!hitAnything && SwingUtilities.isRightMouseButton(e))
				{
				JPopupMenu popup = new JPopupMenu();
				
				////// Right-click on connection point
				final Tuple<Tuple<FlowUnit,String>,ConnPoint> tt=findHoverConnPoint(mx, my);
				if(tt!=null)
					{
					final Tuple<FlowUnit,String> t=tt.fst();
					final ConnPoint connpoint=tt.snd();
					Map<String,FlowType> typesIn=t.fst().getTypesIn(flow);
					Map<String,FlowType> typesOut=t.fst().getTypesOut(flow);
					
					boolean connected=false;
					for(FlowConn conn:flow.conns)
						if(conn.toUnit==t.fst() && conn.toArg.equals(t.snd()))
							connected=true;
					
					//Input connection
					if(typesIn.containsKey(t.snd()) && !connected)
						{
						FlowType type=typesIn.get(t.snd());
						Collection<FlowUnitDeclaration> suggestUnits=FlowType.getSuggestCreateUnitInput(type);
						for(final FlowUnitDeclaration decl:suggestUnits)
							{
							JMenuItem mi=new JMenuItem("Create "+decl.name);
							mi.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e)
								{
								FlowUnit newu=decl.createInstance();
								String newuArg=newu.getTypesOut(flow).keySet().iterator().next();

								FlowConn newconn=new FlowConn(newu,newuArg,t.fst(),t.snd());
								flow.conns.add(newconn);
								flow.units.add(newu);

								Dimension dim=newu.getBoundingBox(getComponentForUnit(newu), flow);
								newu.x=(int)connpoint.pos.x-dim.width-50;
								newu.y=(int)connpoint.pos.y-dim.height/2;
								
								FlowPanel.this.repaint();
								}
							});
							popup.add(mi);
							}
						}
					
					//Output connection
					//Converters etc, could easily have them here too
					if(typesOut.containsKey(t.snd()))
						{
						FlowType type=typesOut.get(t.snd());
						Collection<FlowUnitDeclaration> suggestUnits=FlowType.getSuggestCreateUnitOutput(type);
						for(final FlowUnitDeclaration decl:suggestUnits)
							{
							JMenuItem mi=new JMenuItem("Create "+decl.name);
							mi.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e)
								{
								FlowUnit newu=decl.createInstance();
								String newuArg=newu.getTypesIn(flow).keySet().iterator().next();

								FlowConn newconn=new FlowConn(t.fst(),t.snd(),newu,newuArg);
								flow.conns.add(newconn);
								flow.units.add(newu);

								Dimension dim=newu.getBoundingBox(getComponentForUnit(newu), flow);
								newu.x=(int)connpoint.pos.x+50;
								newu.y=(int)connpoint.pos.y-dim.height/2;
								
								FlowPanel.this.repaint();
								}
							});
							popup.add(mi);
							}
						}
					
					
					
					}
				
				
				
				/////// Right-click on segment (connection)
				final Tuple<Vector2d,FlowConn> seg=getHoverSegment();
				if(seg!=null)
					{
					JMenuItem itRemove=new JMenuItem("Remove connection");
					itRemove.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e)
							{
							if(getFlow()==null || seg==null)
								System.out.println("trouble "+getFlow()+"  "+seg);
							getFlow().conns.remove(seg.snd());
							repaint();
							}
					});
					popup.add(itRemove);
					}

				if(popup.getComponentCount()>0)
					popup.show(this,e.getX(),e.getY());
				
				}
			}
		
		}


	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}

	

	/**
	 * Mouse button pressed
	 */
	public void mousePressed(MouseEvent e)
		{
		mouseLastDragX=e.getX();
		mouseLastDragY=e.getY();
		int mx=e.getX()+cameraX;
		int my=e.getY()+cameraY;

		if(placingUnit!=null)
			{
			
			
			}
		else
			{
			boolean found=false;
			
			//Find connection point
			if(!found && SwingUtilities.isLeftMouseButton(e))
				{
				Tuple<Tuple<FlowUnit,String>,ConnPoint>	t=findHoverConnPoint(mx, my);
				if(t!=null)
					{
					drawingConn=new DrawingConn();
					drawingConn.t=t.fst();
					drawingConn.toPoint=new Vector2d(mx,my);
					found=true;
					}
				}
			
			//Find component the user is trying to click. TODO: containers?
			if(!found && SwingUtilities.isLeftMouseButton(e))
				for(FlowUnit u:getFlow().units)
					if(u.mouseHoverMoveRegion(mx,my,unitComponent.get(u), flow))
						{
						if(selectedUnits.contains(u))
							movingUnits.addAll(selectedUnits);
						else
							movingUnits.add(u);
						found=true;
						break;
						}
			
			if(!found && SwingUtilities.isLeftMouseButton(e))
				{
				selectRect=new Rectangle(e.getX()+cameraX,e.getY()+cameraY,0,0);
				repaint();
				}
			
			}
		
		}

	/**
	 * Mouse button released
	 */
	public void mouseReleased(MouseEvent e)
		{
		movingUnits.clear();
		if(selectRect!=null && SwingUtilities.isLeftMouseButton(e))
			{
			selectedUnits.clear();
			for(FlowUnit u:getFlow().units)
				{
				Point p=u.getMidPos(unitComponent.get(u),flow);
				if(p.x>selectRect.getX() && p.y>selectRect.getY() && p.x<selectRect.getMaxX() && p.y<selectRect.getMaxY())
					selectedUnits.add(u);
				}
			selectRect=null;
			repaint();
			}
		else if(drawingConn!=null && SwingUtilities.isLeftMouseButton(e))
			{
			int mx=e.getX()+cameraX;
			int my=e.getY()+cameraY;
			Tuple<Tuple<FlowUnit,String>,ConnPoint> tt=findHoverConnPoint(mx, my);
			Tuple<FlowUnit,String> v=drawingConn.t;
			
			if(tt!=null)
				{
				Tuple<FlowUnit,String> t=tt.fst();
				
				if(connPoint.get(v).isFrom != connPoint.get(t).isFrom)
					{
					//Correct order
					if(!connPoint.get(v).isFrom)
						{
						Tuple<FlowUnit,String> temp=t;
						t=v;
						v=temp;
						}

					//Add if it doesn't exist, otherwise remove
					FlowConn nc=new FlowConn(v.fst(),v.snd(), t.fst(), t.snd());
					boolean exists=false;
					for(FlowConn c:getFlow().conns)
						if(c.equals(nc))
							{
							exists=true;
							getFlow().conns.remove(c);
							break;
							}
					if(!exists)
						getFlow().conns.add(nc);
					}
				}
			drawingConn=null;
			repaint();
			}
		
		}
	
	
	
	/******************************************************************************************************
	 *                               Operations on flow                                                   *
	 *****************************************************************************************************/

	/**
	 * Align selected units as a vertical stack
	 */
	public void alignVert(Set<FlowUnit> sel)
		{
		List<FlowUnit> order=new LinkedList<FlowUnit>();
		Integer maxh=null;
		for(FlowUnit u:sel)
			if(maxh==null || u.getBoundingBox(unitComponent.get(u), flow).height>maxh)
				maxh=u.getBoundingBox(unitComponent.get(u), flow).height;
		order.addAll(sel);
		Collections.sort(order, new Comparator<FlowUnit>(){
			public int compare(FlowUnit a, FlowUnit b){return new Integer(a.y).compareTo(new Integer(b.y));}});
		FlowUnit fu=order.iterator().next();
		int starty=fu.getMidPos(unitComponent.get(fu),flow).y;
		for(int i=0;i<order.size();i++)
			{
			FlowUnit au=order.get(i);
			int cy=au.getMidPos(unitComponent.get(au),flow).y;
			int ny=starty+i*(int)(maxh*1.3);
			order.get(i).y+=ny-cy;
			}
		repaint();
		}
	
	/**
	 * Align selected units to have the same right side
	 */
	public void alignRight(Set<FlowUnit> sel)
		{
		Map<FlowUnit,Double> xmap=new HashMap<FlowUnit, Double>();
		Double totmax=null;
		
		for(Map.Entry<Tuple<FlowUnit, String>, ConnPoint> e:connPoint.entrySet())
			if(selectedUnits.contains(e.getKey().fst()))
				{
				FlowUnit u=e.getKey().fst();
				String arg=e.getKey().snd();
				//TODO improve
				//Problematic assumption: all components have to the right, or otherwise to the left
				//If there is no connector at all then it will fail totally
				if(u.getTypesOut(flow).keySet().contains(arg) || u.getTypesIn(flow).keySet().contains(arg))
					{
					ConnPoint p=e.getValue();
					Double maxx=xmap.get(u);
					if(maxx==null || p.pos.x>maxx)
						{
						maxx=p.pos.x;
						xmap.put(u,maxx);
						if(totmax==null || totmax<p.pos.x)
							totmax=p.pos.x;
						}
					}
				
				}
		
		for(FlowUnit u:sel)
			{
			int diff=(int)(xmap.get(u)-totmax);
			u.x-=diff;
			}
		repaint();
		}
	

	
	
	/******************************************************************************************************
	 *                               Connecting points                                                    *
	 *****************************************************************************************************/

	private static class DrawingConn
		{
		Tuple<FlowUnit,String> t;
		Vector2d toPoint;
		}

	private static class ConnPoint
		{
		Vector2d pos;
		boolean isFrom;
		}
	
	/**
	 * Draw a connection point on the left side of a flow unit
	 */
	public void drawConnPointLeft(Graphics g,FlowUnit unit, String arg, int x, int y)
		{
		g.setColor(Color.BLACK);
		g.fillRect(x-5, y-2, 5, 5);
		ConnPoint p=new ConnPoint();
		p.pos=new Vector2d(x-2,y);
		p.isFrom=false;
		connPoint.put(new Tuple<FlowUnit, String>(unit,arg), p);
		
		boolean connected=false;
		for(FlowConn conn:getFlow().conns)
			if(conn.toUnit==unit && arg.equals(conn.toArg))
				connected=true;
		if(!connected)
			{
			int fw=g.getFontMetrics().stringWidth(arg);
			int fh=g.getFontMetrics().getAscent();
			g.drawString(arg, x-fw-5, y-2+fh/2);
			}
		}
	
	/**
	 * Draw a connection point on the right side of a flow unit
	 */
	public void drawConnPointRight(Graphics g,FlowUnit unit, String arg, int x, int y)
		{
		g.setColor(Color.BLACK);
		g.fillRect(x, y-2, 5, 5);
		ConnPoint p=new ConnPoint();
		p.pos=new Vector2d(x+2,y);
		p.isFrom=true;
		connPoint.put(new Tuple<FlowUnit, String>(unit,arg), p);
		
		boolean connected=false;
		for(FlowConn conn:getFlow().conns)
			if(conn.fromUnit==unit && arg.equals(conn.fromArg))
				connected=true;
		if(!connected)
			{
			int fh=g.getFontMetrics().getAscent();
			g.drawString(arg, x+5, y-2+fh/2);
			}
		}

	/**
	 * Find connection point near given coordinate
	 */
	private Tuple<Tuple<FlowUnit,String>,ConnPoint> findHoverConnPoint(int mx, int my)
		{
		int sq=connPointSnapDistance*connPointSnapDistance;
		for(Map.Entry<Tuple<FlowUnit, String>, ConnPoint> entry:connPoint.entrySet())
			{
			Vector2d diff=new Vector2d(mx,my);
			diff.sub(entry.getValue().pos);
			if(diff.lengthSquared()<sq)
				return Tuple.make(entry.getKey(),entry.getValue());
			}
		return null;
		}
	

	/******************************************************************************************************
	 *                               Connecting lines                                                     *
	 *****************************************************************************************************/
	private List<ConnLineSegment> connSegments=new LinkedList<ConnLineSegment>();
	private abstract class ConnLineSegment
		{
		public FlowConn c;
		public abstract Tuple<Vector2d,Integer> hitLine(int x, int y);
		}
	
	
	/**
	 * Horizontal line segment
	 */
	private class ConnLineSegmentH extends ConnLineSegment
		{
		public int x1,x2,y; //Word coordinates
		public ConnLineSegmentH(Graphics g, int x1, int x2, int y, FlowConn c)
			{
			//Reorder x for fast comparison
			if(x1>x2)
				{
				this.x1=x2;
				this.x2=x1;
				}
			else
				{
				this.x1=x1;
				this.x2=x2;
				}
			this.y=y;
			this.c=c;
			g.drawLine(x1,y,x2,y);
			if(c!=null)
				connSegments.add(this);
			}
		public Tuple<Vector2d,Integer> hitLine(int mx, int my)
			{
			int dist=Math.abs(my-y);
			if(mx>=x1 && mx<=x2)
				return Tuple.make(new Vector2d(mx,this.y), dist);
			else
				return null;
			}
		}
	
	/**
	 * Vertical line segment
	 */
	private class ConnLineSegmentV extends ConnLineSegment
		{
		public int x,y1,y2; //Word coordinates
		public ConnLineSegmentV(Graphics g, int x, int y1, int y2, FlowConn c)
			{
			//Reorder y for fast comparison
			if(y1>y2)
				{
				this.y1=y2;
				this.y2=y1;
				}
			else
				{
				this.y1=y1;
				this.y2=y2;
				}
			this.x=x;
			this.c=c;
			g.drawLine(x,y1,x,y2);
			if(c!=null)
				connSegments.add(this);
			}		
		public Tuple<Vector2d,Integer> hitLine(int mx, int my)
			{
			int dist=Math.abs(mx-x);
			if(my>=y1 && my<=y2)
				return Tuple.make(new Vector2d(x,my), dist);
			else
				return null;
			}
		}

	/**
	 * Get line segment the mouse currently hovers
	 */
	private Tuple<Vector2d,FlowConn> getHoverSegment()
		{
		Integer closestDist=null;
		ConnLineSegment closestSeg=null;
		Vector2d closestProj=null;
		int mx=mouseLastX+cameraX;
		int my=mouseLastY+cameraY;
		for(ConnLineSegment seg:connSegments)
			{
			Tuple<Vector2d,Integer> hit=seg.hitLine(mx, my);
			if(hit!=null && (closestDist==null || hit.snd()<closestDist))
				{
				closestProj=hit.fst();
				closestDist=hit.snd();
				closestSeg=seg;
				}
			}
		if(closestDist!=null && closestDist<lineSnapDistance)
			return Tuple.make(closestProj,closestSeg.c);
		else
			return null;
		}
	
	/**
	 * Draw connecting line between two points
	 */
	public void drawConnLine(Graphics g,Vector2d vFrom, Vector2d vTo, FlowConn c)
		{
		int spacing=15;

		int midy=(int)(vFrom.y+vTo.y)/2;
		int x1=(int)(vFrom.x+vTo.x)/2;
		int x2=x1;
		if(vFrom.x>vTo.x)
			{
			if(x1<vFrom.x+spacing) x1=(int)vFrom.x+spacing;
			if(x2>vTo.x-spacing) x2=(int)vTo.x-spacing;
			}
		
		new ConnLineSegmentH(g, (int)vFrom.x, x1, (int)vFrom.y,c);
		new ConnLineSegmentV(g, x1, (int)vFrom.y, midy,c);
		new ConnLineSegmentH(g,x1,x2,midy,c);
		new ConnLineSegmentV(g,x2,(int)vTo.y,midy,c);
		new ConnLineSegmentH(g,(int)vTo.x,x2,(int)vTo.y,c);
		}



	/**
	 * Copy current selection to clipboard
	 */
	public void copy()
		{
		if(enabled)
			try
				{
				Element root=new Element("temp");
				Document doc=new Document(root);
				flow.saveMetadata(root, selectedUnits);
				EvSwingUtil.setClipBoardString(EvXmlUtil.xmlToString(doc));
				}
			catch (Exception e)
				{
				EvLog.printError("Error copying flow", e);
				}
		}

	/**
	 * Paste from clipboard
	 */
	public void paste()
		{
		if(enabled)
			{
			try
				{
				String cp=EvSwingUtil.getClipBoardString();
				Element root=EvXmlUtil.stringToXml(cp);
				if(root.getName().equals(Flow.metaType))
					{
					Flow n=new Flow();
					n.loadMetadata(root);
					flow.pasteFrom(n);
					selectedUnits.clear();
					selectedUnits.addAll(n.units);

					//Put pasted units in the center of the screen
					int avx=0;
					int avy=0;
					for(FlowUnit u:selectedUnits)
						{
						avx+=u.x;
						avy+=u.y;
						}
					avx/=selectedUnits.size();
					avy/=selectedUnits.size();
					
					int midsx=cameraX+getWidth()/2;
					int midsy=cameraY+getHeight()/2;
					int dx=midsx-avx;
					int dy=midsy-avy;
					for(FlowUnit u:selectedUnits)
						{
						u.x+=dx;
						u.y+=dy;
						}
					repaint();
					}
				}
			catch (Exception e)
				{
				EvLog.printError("Error pasting flow", e);
				}
			}
		}
	


	}
