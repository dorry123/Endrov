/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.liveWindow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;


/**
 * Calculate histogram for display only. Hence the histogram is binned to fit on screen. 
 * @author Johan Henriksson
 *
 */
public class LiveHistogramView extends JPanel
	{
	private static final long serialVersionUID = 1L;

	protected int histoRangeMin=0;
	protected int histoRangeMax=255;

	protected int showRangeMin=0;
	protected int showRangeMax=255;
	
	private EvPixels currentImage;
	private BufferedImage cachedImage=null; //cached image

	private int height=50;
	
	protected boolean showCDF=false;

	public void setShowCDF(boolean b)
		{
		showCDF=b;
		cachedImage=null;
		repaint();
		}
	
	/**
	 * Set pixels to calculate histogram from. #bits determines maximum range
	 */
	public void setImage(EvPixels p, int numBits)
		{
		cachedImage=null;
		
		currentImage=p;
		
		showRangeMax=2<<numBits-1;
		repaint();
		}

	@Override
	public Dimension getMinimumSize()
		{
		return new Dimension(1, height);
		}
	
	@Override
	public Dimension getPreferredSize()
		{
		return new Dimension(1, height);
		}
	
	/**
	 * Calculate the histogram
	 * @param p Pixels intensities
	 * @return Bins
	 */
	private int[] calculateHistogram(int[] p)
		{
		int screenWidth=getWidth();
		int[] bins=new int[screenWidth];
		
		//Figure out min range
		int min=Integer.MAX_VALUE;
		for(int v:p)
			if(v<min)
				min=v;
		histoRangeMin=min;
		
		if(min>0)
			min=0;
		showRangeMin=min;
		
		//Figure out max range
		int max=Integer.MIN_VALUE;
		for(int v:p)
			if(v>max)
				max=v;
		histoRangeMax=max;
		
		if(max<256)
			max=256;
		else if(max<1024)
			max=1023;
		else if(max<4096)
			max=4096;
		else if(max<65535)
			max=65536;
		else
			max=(1<<32)-1;
		showRangeMax=max;
		
		//Calculate histogram
		for(int v:p)
			{
			if(v<0)
				v=0;
			
			int i=(v-showRangeMin)*screenWidth/(showRangeMax-showRangeMin);
			bins[i]++;
			}

		//Option: Show CDF rather than PDF
		if(showCDF)
			{
			int totalSum=0;
			for(int i=0;i<bins.length;i++)
				{
				totalSum+=bins[i];
				bins[i]=totalSum;
				}
			}
		
		//Normalize according to largest peak found
		int maxH=1;
		for(int i=0;i<bins.length;i++)
			if(bins[i]>maxH)
				maxH=bins[i];
		for(int i=0;i<bins.length;i++)
			bins[i]=bins[i]*height/maxH;
		
		return bins;
		}
	
	/**
	 * Render bins onto image
	 */
	private void renderBins(Graphics g, int[] bins)
		{
		g.setColor(Color.BLACK);
		for(int ax=0;ax<bins.length;ax++)
			g.drawLine(ax, height, ax, height-bins[ax]);
		}
	
	/**
	 * Generate image of histogram
	 */
	private void makeImage()
		{
		//Only int values will be fast for now. no floating point
		currentImage=currentImage.convertToInt(true); 

		BufferedImage bim=cachedImage=new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_BYTE_GRAY);
		Graphics g2=bim.getGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());
		if(currentImage.getType()==EvPixelsType.INT)
			renderBins(g2,calculateHistogram(currentImage.getArrayInt()));
		}
	
	

	/**
	 * Transform to screen coordinates
	 */
	public int toScreenX(int x)
		{
		int screenWidth=getWidth();
		return x*screenWidth/showRangeMax;
		}

	/**
	 * Transform to world coordinates
	 */
	public int toWorldX(int x)
		{
		int screenWidth=getWidth();
		return x*showRangeMax/screenWidth;
		}

	
	@Override
	protected void paintComponent(Graphics g)
		{
		//Recalculate histogram if component size changes
		if(cachedImage!=null && getWidth()!=cachedImage.getWidth())
			cachedImage=null;
		
		if(currentImage!=null)
			{
			if(cachedImage==null)
				makeImage();
			
			//Image is opaque, no need to clear background
			g.drawImage(cachedImage, 0, 0, null);
			}
		else
			{
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());
			}
		}
	
	}
