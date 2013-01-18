/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.particleMeasure;

import java.io.PrintWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.flow.FlowType;
import endrov.imageset.EvChannel;
import endrov.imageset.EvStack;
import endrov.particleMeasure.ParticleMeasure.Well;
import endrov.particleMeasure.calc.MeasureProperty;
import endrov.particleMeasure.calc.ParticleMeasureCenterOfMass;
import endrov.particleMeasure.calc.ParticleMeasureCentroid;
import endrov.particleMeasure.calc.ParticleMeasureGeometricPCA;
import endrov.particleMeasure.calc.ParticleMeasureMaxIntensity;
import endrov.particleMeasure.calc.ParticleMeasureMeanIntensity;
import endrov.particleMeasure.calc.ParticleMeasureMedianIntensity;
import endrov.particleMeasure.calc.ParticleMeasureModalIntensity;
import endrov.particleMeasure.calc.ParticleMeasurePerimeter;
import endrov.particleMeasure.calc.ParticleMeasureSumIntensity;
import endrov.particleMeasure.calc.ParticleMeasureSurfaceArea;
import endrov.particleMeasure.calc.ParticleMeasureVolume;
import endrov.util.EvDecimal;
import endrov.util.ProgressHandle;

/**
 * Measurements of particles - identified regions in stacks.
 * Designed to handle channels by lazy evaluation of each stack.<br/>
 * 
 * There is by design no connection of the ID of one frame to the next,
 * this interpretation is done outside by the user.<br/>
 * 
 * This object can also be used as a quick property output for other things.
 * In that case the frame and ID should be set to 0 unless there is a good
 * reason for something else.<br/>
 * 
 * Output data can not normally be modified - a special filter could do it.
 * Filters act by lazily rewriting one measure to another.
 * 
 * TODO what about channel comparisons?
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class ParticleMeasure extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static final String metaType="ParticleMeasure";
	

		
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	
	/**
	 * Information about one well
	 */
	public static class Well 
		{
		private static final long serialVersionUID = 1L;
//		private Runnable calcInfo;
		
		HashMap<EvDecimal,Frame> frameInfo=new HashMap<EvDecimal, ParticleMeasure.Frame>();
		
		
		/**
		 * Get data for one frame. Evaluate if necessary
		 */
		public Map<Integer,ParticleMeasureParticle> getFrame(EvDecimal frame)
			{
			Frame info=frameInfo.get(frame);
			if(info!=null)
				{
				if(info.calcInfo!=null)
					{
					info.calcInfo.run();
					info.calcInfo=null;
					}
				return Collections.unmodifiableMap(info);
				}
			else
				return null;
			}

		public void setFrame(EvDecimal frame, Frame info)
			{
			frameInfo.put(frame, info);
			}

		/**
		 * Get which frames exist
		 */
		public SortedSet<EvDecimal> getFrames()
			{
			return Collections.unmodifiableSortedSet((SortedSet<EvDecimal>)frameInfo.keySet());
			}

		
		
		/**
		 * Get a new particle measure where particles and frames have been filtered.
		 * Will execute lazily.
		 */
		public Well filter(final Filter filter)
			{
			Well out=new Well();
			
			//Copy all the columns
//			out.columns.addAll(columns);
			
			//Copy all frames
			for(Map.Entry<EvDecimal, Frame> f:frameInfo.entrySet())
				if(filter.acceptFrame(f.getKey()))
					{
					//Create place-holder for frame
					final Frame oldInfo=f.getValue();
					final Frame newInfo=new Frame();
					out.frameInfo.put(f.getKey(), newInfo);

					//Filter need to execute lazily as well
					newInfo.calcInfo=new Runnable()
						{
						public void run()
							{
							//Execute calculation if not done already
							if(oldInfo.calcInfo!=null)
								{
								oldInfo.calcInfo.run();
								oldInfo.calcInfo=null;
								}

							//Filter particles
							for(int id:oldInfo.keySet())
								{
								ParticleMeasureParticle pInfo=oldInfo.get(id);
								if(filter.acceptParticle(id, pInfo))
									newInfo.put(id,pInfo);
								}
							}
						};
					}
			
			return out;
			}

		}

	
	/**
	 * Information about one frame - Just a list of particles, with a lazy evaluator
	 */
	public static class Frame extends HashMap<Integer,ParticleMeasureParticle>
		{
		private static final long serialVersionUID = 1L;
		public Runnable calcInfo;  //TODO this is bad!
		
		public HashMap<String, Object> getCreateParticle(int id)
			{
			ParticleMeasureParticle info=get(id);
			if(info==null)
				put(id,info=new ParticleMeasureParticle());
			return info.map;
			}
		}
	
	

	/**
	 * Data for one particle
	 */
	public static class ParticleMeasureParticle
		{
		HashMap<String, Object> map=new HashMap<String, Object>();
	
		/**
		 * Get value as double
		 */
		public Double getDouble(String s)
			{
			Object o=map.get(s);
			if(o instanceof String)
				return Double.parseDouble((String)o);
			else if(o instanceof Number)
				return ((Number)o).doubleValue();
			else if(o==null)
				return null;
			else
				throw new RuntimeException("Bad type: "+o.getClass());
			}
	
		/**
		 * Get value as integer
		 */
		public Integer getInt(String s)
			{
			Object o=map.get(s);
			if(o instanceof String)
				return Integer.parseInt((String)o);
			else if(o instanceof Number)
				return ((Number)o).intValue();
			else if(o==null)
				return null;
			else
				throw new RuntimeException("Bad type: "+o.getClass());
			}
	
		/**
		 * Get value as string
		 */
		public String getString(String s)
			{
			Object o=map.get(s);
			if(o==null)
				return null;
			else
				return o.toString();
			}
	
		/**
		 * Get raw object
		 */
		public Object getObject(String s)
			{
			return map.get(s);
			}
		}
	
	/******************************************************************************************************
	 *            Class: XML Reader and writer of this type of meta object                                *
	 *****************************************************************************************************/

	//TODO this barely qualifies as an object since it contains no data. is this fine?
	
	@Override
	public void loadMetadata(Element e)
		{
		//TODO
		}

	@Override
	public String saveMetadata(Element e)
		{
		//TODO
		return metaType;
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	/**
	 * 
	 */
	private TreeMap<String, Well> wellInfo=new TreeMap<String, Well>();

	
	public Well getCreateWell(String well)
		{
		Well info=wellInfo.get(well);
		if(info==null)
			wellInfo.put(well,info=new Well());
		return info;
		}
	
	public Well getWell(String wellName)
		{
		return wellInfo.get(wellName);
		}

	public void setWell(String wellName, Well well)
		{
		wellInfo.put(wellName, well);
		}

	
	/**
	 * Columns ie properties, for each particle
	 */
	private TreeSet<String> columns=new TreeSet<String>();




	public static final FlowType FLOWTYPE=new FlowType(ParticleMeasure.class);

	/**
	 * Empty measure
	 */
	public ParticleMeasure()
		{
		}

	
	
	
	
	
		
	
	/**
	 * Get which columns exist
	 */
	public SortedSet<String> getColumns()
		{
		return Collections.unmodifiableSortedSet(columns);
		}
	
	
	@Override
	public void buildMetamenu(JMenu menu, EvContainer parentObject)
		{
		}

	@Override
	public String getMetaTypeDesc()
		{
		return "Measured properties";
		}


	
	/**
	 * Write data as a CSV-style table
	 */
	public void saveCSV(Writer io, boolean addHeader, String fieldDelim)
		{
		System.out.println("field delim:"+fieldDelim+":");
		
		PrintWriter pw=new PrintWriter(io);
		
		Set<String> col=getColumns();
		
		//Add header
		if(addHeader)
			{
			pw.print("frame");
			pw.print(fieldDelim);
			pw.print("particle");
			for(String s:col)
				{
				pw.print(fieldDelim);
				pw.print(s);
				}
			pw.println();
			}

		//Write the data
		for(String wellName:wellInfo.keySet())
			{
			Well well=wellInfo.get(wellName);
			for(EvDecimal frame:well.getFrames())
				{
				for(Map.Entry<Integer, ParticleMeasureParticle> e:well.getFrame(frame).entrySet())
					{
					pw.print(wellName);
					pw.print(fieldDelim);
					pw.print(frame);
					pw.print(fieldDelim);
					pw.print(e.getKey());
					Map<String,Object> props=e.getValue().map;
					for(String columnName:col)
						{
						pw.print(fieldDelim);
						pw.print(props.get(columnName));
						}
					pw.println();
					}
				
				}
			}
		
		pw.flush();
		}
	
	
	/**
	 * Save data to SQL database
	 */
	public void saveSQL(Connection conn, String dataid, String tablename) throws SQLException
		{
		dropSQLtable(conn, dataid, tablename);

		//Clean up this dataid
		deleteFromSQLtable(conn, dataid, tablename);
		
		//Create table if needed. Make sure it has the right columns
		createSQLtable(conn, dataid, tablename);
		
		
		//Insert all data
		insertIntoSQLtable(conn, dataid, tablename);
		
		}

	/**
	 * Create the table
	 */
	public void createSQLtable(Connection conn, String dataid, String tablename) throws SQLException
		{
		StringBuffer createTable=new StringBuffer();
		createTable.append("create table "+tablename+" (");
		createTable.append("dataid TEXT, well TEXT, frame DECIMAL, particle INTEGER");
		for(String column:columns)
			createTable.append(", "+column+" DECIMAL"); //TODO types
		createTable.append(");");
		PreparedStatement stmCreateTable=conn.prepareStatement(createTable.toString());
		//for(String column:columns) //TODO also columns as ?
		stmCreateTable.execute();
		}

	/**
	 * Drop the entire table
	 */
	public void dropSQLtable(Connection conn, String dataid, String tablename) throws SQLException
		{
		StringBuffer dropTable=new StringBuffer();
		dropTable.append("drop table "+tablename+";");
		PreparedStatement stmDropTable=conn.prepareStatement(dropTable.toString());
		stmDropTable.execute();
		}

	/**
	 * Delete these values from the SQL table
	 */
	public void deleteFromSQLtable(Connection conn, String dataid, String tablename) throws SQLException
		{
		StringBuffer deleteTable=new StringBuffer();
		deleteTable.append("delete from "+tablename+" where dataid=?;");
		PreparedStatement stmDeleteTable=conn.prepareStatement(deleteTable.toString());
		stmDeleteTable.setString(1, dataid);
		stmDeleteTable.execute();
		}
		
	/**
	 * Insert values into table
	 */
	public void insertIntoSQLtable(Connection conn, String dataid, String tablename) throws SQLException
		{
		Set<String> col=getColumns();

		StringBuffer insert=new StringBuffer();
		insert.append("insert into "+tablename+" (");
		insert.append("dataid, well, frame, particle");
		for(String column:col)
			insert.append(","+column); //TODO types
		insert.append(") VALUES (");
		insert.append("?, ?, ?, ?");
		for(int i=0;i<col.size();i++)
			insert.append(",?");
		insert.append(");");
		
		System.out.println(insert);
		PreparedStatement stmInsertTable=conn.prepareStatement(insert.toString());
		
		stmInsertTable.setString(1, dataid);
		for(String wellName:wellInfo.keySet())
			{
			Well well=wellInfo.get(wellName);
			stmInsertTable.setString(2, wellName);
			for(EvDecimal frame:well.getFrames())
				{
				stmInsertTable.setBigDecimal(3, frame.toBigDecimal());			
				for(Map.Entry<Integer, ParticleMeasureParticle> e:well.getFrame(frame).entrySet())
					{
					int particleID=e.getKey();
					stmInsertTable.setInt(4, particleID);
					
					Map<String,Object> props=e.getValue().map;
					int colid=5;
					for(String columnName:col)
						{
						Object p=props.get(columnName);
						if(p instanceof Double)
							stmInsertTable.setDouble(colid, (Double)p);
						else if(p instanceof Integer)
							stmInsertTable.setInt(colid, (Integer)p);
						else
							stmInsertTable.setInt(colid, (Integer)(-1));
						colid++;
						}
					
					stmInsertTable.execute();
					}
				}
			}
		}

	
	
	/**
	 * Filter of particle measure data
	 * 
	 * TODO can use the same structure also for adding data!
	 */
	public static interface Filter
		{
		/**
		 * Accept a frame? if false then all particles will be discarded
		 */
		public boolean acceptFrame(EvDecimal frame);
		
		/**
		 * Accept a particle?
		 */
		public boolean acceptParticle(int id, ParticleMeasureParticle info);
		}
	
	
	
	
	@Override
	public EvObject cloneEvObject()
		{
		return cloneUsingSerialize();
		}
	

	public void addColumn(String s)
		{
		columns.add(s);
		}

	
	public ParticleMeasure filter(final Filter filter)
		{
		ParticleMeasure out=new ParticleMeasure();
		
		//Copy all the columns
		out.columns.addAll(columns);
		
		//Copy all wells
		for(Map.Entry<String, Well> f:wellInfo.entrySet())
			out.wellInfo.put(f.getKey(),f.getValue().filter(filter));
		
		return out;
		}
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,ParticleMeasure.class);
		
		MeasureProperty.registerMeasure("max value", new ParticleMeasureMaxIntensity());
		MeasureProperty.registerMeasure("sum value", new ParticleMeasureSumIntensity());
		MeasureProperty.registerMeasure("mean value", new ParticleMeasureMeanIntensity());
		MeasureProperty.registerMeasure("modal value", new ParticleMeasureModalIntensity());
		MeasureProperty.registerMeasure("median value", new ParticleMeasureMedianIntensity());
		
		MeasureProperty.registerMeasure("volume", new ParticleMeasureVolume());
		MeasureProperty.registerMeasure("center of mass", new ParticleMeasureCenterOfMass());
		MeasureProperty.registerMeasure("centroid", new ParticleMeasureCentroid());
		MeasureProperty.registerMeasure("surface area", new ParticleMeasureSurfaceArea());
		MeasureProperty.registerMeasure("perimeter", new ParticleMeasurePerimeter());
		MeasureProperty.registerMeasure("Geometric PCA", new ParticleMeasureGeometricPCA());
		}
	
	
	

	}
