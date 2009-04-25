package endrov.unsortedImageFilters;

import endrov.imageset.EvPixels;

/**
 * Convolving operations
 * @author Johan Henriksson
 *
 */
public class Convolve
	{

	/**
	 * Convolve: in (*) kernel
	 * 
	 * kernel and in assumed 0 outside area
	 * 
	 * Complexity O(w*h*kw*kh)
	 * 
	 * TODO test
	 * 
	 * @LEARNED need to specialize over multiple input formats
	 * @LEARNED when calling functions to be inlined, these need specialized versions too with tests already done by caller 
	 * 
	 */
	public static EvPixels convolve(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		kernel=kernel.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		int kw=kernel.getWidth();
		int kh=kernel.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] inPixels=in.getArrayInt();
		int[] kernelPixels=in.getArrayInt();
		int[] outPixels=out.getArrayInt();
		

		//TODO probably need to support int x double, double x double
		
		//TODO specialize border calculations to avoid branching?
		
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				{
				//Find bounds for integration
				int fromx=Math.max(-kcx,-ax);
				int tox=Math.min(kw-kcx,w-(kw-kcx));
				int fromy=Math.max(-kcy,-ay);
				int toy=Math.min(kh-kcy,h-(kh-kcy));

				//Convolve
				int sum=0;
				for(int kx=fromx;kx<tox;kx++)
					for(int ky=fromy;ky<toy;ky++)
						sum+=inPixels[in.getPixelIndex(ax+kx, ay+ky)]*kernelPixels[kernel.getPixelIndex(kcx+kx, kcy+ky)];
				
				outPixels[out.getPixelIndex(ax, ay)]=sum;
				}
		
		return out;
		}
	
	}