/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.math;

import endrov.flow.EvOpSlice1;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.util.ProgressHandle;

/**
 * min(A,b)
 */
public class EvOpImageMinScalar extends EvOpSlice1
	{
	private final Number b;
	
	public EvOpImageMinScalar(Number b)
		{
		this.b = b;
		}

	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return apply(p[0],b);
		}
	
	public static EvPixels apply(EvPixels a, Number bVal)
		{
		if(a.getType()==EvPixelsType.INT && bVal instanceof Integer)
			{
			//Should use the common higher type here
			a=a.getReadOnly(EvPixelsType.INT);
			int b=bVal.intValue();
			
			int w=a.getWidth();
			int h=a.getHeight();
			EvPixels out=new EvPixels(a.getType(),w,h);
			int[] aPixels=a.getArrayInt();
			int[] outPixels=out.getArrayInt();
			
			for(int i=0;i<aPixels.length;i++)
				outPixels[i]=aPixels[i]<b ? aPixels[i] : b;
			
			return out;
			}
		else
			{
			//Should use the common higher type here
			a=a.getReadOnly(EvPixelsType.DOUBLE);
			double b=bVal.doubleValue();
			
			int w=a.getWidth();
			int h=a.getHeight();
			EvPixels out=new EvPixels(a.getType(),w,h);
			double[] aPixels=a.getArrayDouble();
			double[] outPixels=out.getArrayDouble();
			
			for(int i=0;i<aPixels.length;i++)
				outPixels[i]=aPixels[i]<b ? aPixels[i] : b;
			
			return out;			
			}
		}
	
	}