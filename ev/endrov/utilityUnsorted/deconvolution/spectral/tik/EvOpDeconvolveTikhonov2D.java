/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.utilityUnsorted.deconvolution.spectral.tik;

import endrov.typeImageset.EvPixels;
import endrov.utilityUnsorted.deconvolution.Deconvolver2D;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralPaddingType;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralResizingType;

/**
 * Deconvolution in 2D using tikhonov
 * @author Johan Henriksson
 *
 */
public class EvOpDeconvolveTikhonov2D extends Deconvolver2D
	{
	private final EvPixels imPSF;
	private final SpectralResizingType resizing;
	private final double regParam;
	private final double threshold;
	private final SpectralPaddingType padding;
	
	 public EvOpDeconvolveTikhonov2D(EvPixels imPSF, SpectralResizingType resizing,double regParam, double threshold, SpectralPaddingType padding) 
		 {
		 this.imPSF=imPSF;
		 this.resizing=resizing;
		 this.regParam=regParam;
		 this.threshold=threshold;
		 this.padding=padding;
		 }
   
	protected EvPixels internalDeconvolve(EvPixels ipB)
		{
		if(padding.equals(SpectralPaddingType.PERIODIC))
			{
			DoublePeriodicTikhonov2D d=new DoublePeriodicTikhonov2D(ipB, imPSF, resizing, regParam, threshold);
			return d.internalDeconvolve();
			}
		else
			{
			DoubleReflexiveTikhonov2D d=new DoubleReflexiveTikhonov2D(ipB, imPSF, resizing, regParam, threshold);
			return d.internalDeconvolve();
			}
		}
	
	
	}
