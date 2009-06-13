package endrov.unsortedImageFilters.imageMath;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * A*b+c
 * @author Johan Henriksson
 *
 */
public class EvOpImageAxpy extends EvOpSlice1
	{
	private Number b;
	private Number c;
	public EvOpImageAxpy(Number b, Number c)
		{
		this.b = b;
		this.c = c;
		}
	public EvPixels exec1(EvPixels... p)
		{
		return EvOpImageAxpy.axpy(p[0], b, c);
		}
	/**
	 * A*b+c
	 */
	static EvPixels axpy(EvPixels a, Number b, Number c)
		{
		if(b instanceof Integer && c instanceof Integer)
			return axpy(a,b.intValue(),c.intValue());
		else
			return axpy(a,b.doubleValue(),c.doubleValue());
		}
	/**
	 * A*b+c
	 */
	static EvPixels axpy(EvPixels a, double b, double c)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_DOUBLE, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		double[] aPixels=a.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]*b+c;
		
		return out;
		}
	/**
	 * A*b+c
	 */
	static EvPixels axpy(EvPixels a, int b, int c)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_INT, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]*b+c;
		
		return out;
		}
	}