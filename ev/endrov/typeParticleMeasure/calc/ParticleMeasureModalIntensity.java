/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeParticleMeasure.calc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import endrov.typeImageset.EvStack;
import endrov.typeParticleMeasure.ParticleMeasure;
import endrov.util.ProgressHandle;
import endrov.util.collection.EvListUtil;

/**
 * Measure: median intensity
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureModalIntensity implements MeasurePropertyType 
	{
	private static String propertyName="modalI";
	
	
	public void analyze(ProgressHandle progh, EvStack stackValue, EvStack stackMask, ParticleMeasure.Frame info)
		{
		HashMap<Integer,ArrayList<Double>> entryList=new HashMap<Integer, ArrayList<Double>>();
		//TODO: a special map for this case could speed up plenty.
		//also: only accept integer IDs? this would speed up hashing and indexing.
		//can be made even faster as a non-hash

		//Find entries
		for(int az=0;az<stackValue.getDepth();az++)
			{
			double[] arrValue=stackValue.getPlane(az).getPixels(progh).convertToDouble(true).getArrayDouble();
			int[] arrID=stackMask.getPlane(az).getPixels(progh).convertToInt(true).getArrayInt();
			
			for(int i=0;i<arrValue.length;i++)
				{
				double v=arrValue[i];
				int id=arrID[i];
	
				if(id!=0)
					{
					ArrayList<Double> entries=entryList.get(id);
					if(entries==null)
						entryList.put(id, entries=new ArrayList<Double>());
					entries.add(v);
					}
				
				}
			
			
			}
		
		//Write into particles
		for(int id:entryList.keySet())
			{
			ParticleMeasure.ColumnSet p=info.getCreateParticle(id);
			ArrayList<Double> entries=entryList.get(id);
			double[] arr=EvListUtil.toDoubleArray(entries);
			double median=EvListUtil.findPercentileDouble(arr,0.5);
			p.put(propertyName, median);
			}
		
		}

	public String getDesc()
		{
		return "Median intensity of any pixel";
		}

	public Set<String> getColumns()
		{
		return Collections.singleton(propertyName);
		}

	
	
	
	}
