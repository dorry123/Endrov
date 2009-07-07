package endrov.flowBasic.math;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * A / b
 * 
 * @author Johan Henriksson
 */
public class EvOpImageDivScalar extends EvOpSlice1
	{
	private Number b;

	public EvOpImageDivScalar(Number b)
		{
		this.b = b;
		}

	public EvPixels exec1(EvPixels... p)
		{
		return EvOpImageDivScalar.div(p[0], b);
		}

	static EvPixels div(EvPixels a, Number bVal)
		{
		if(a.getType()==EvPixelsType.INT && bVal instanceof Integer)
			{
			// Should use the common higher type here
			a = a.getReadOnly(EvPixelsType.INT);
			int b=bVal.intValue();
			
			int w = a.getWidth();
			int h = a.getHeight();
			EvPixels out = new EvPixels(a.getType(), w, h);
			int[] aPixels = a.getArrayInt();
			int[] outPixels = out.getArrayInt();
	
			for (int i = 0; i<aPixels.length; i++)
				outPixels[i] = aPixels[i]/b;
	
			return out;
			}
		else
			{
			// Should use the common higher type here
			a = a.getReadOnly(EvPixelsType.DOUBLE);
			double b=bVal.doubleValue();
			
			int w = a.getWidth();
			int h = a.getHeight();
			EvPixels out = new EvPixels(a.getType(), w, h);
			double[] aPixels = a.getArrayDouble(); 
			double[] outPixels = out.getArrayDouble();
	
			for (int i = 0; i<aPixels.length; i++)
				outPixels[i] = aPixels[i]/b;
	
			return out;
			}
		}
	}