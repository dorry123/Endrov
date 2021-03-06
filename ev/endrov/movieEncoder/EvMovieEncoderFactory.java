/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.movieEncoder;

import java.io.File;
import java.util.List;
import java.util.Vector;

/**
 * Movie encoders
 * @author Johan Henriksson
 *
 */
public abstract class EvMovieEncoderFactory
	{
	public static Vector<EvMovieEncoderFactory> makers=new Vector<EvMovieEncoderFactory>();
	
	/** Also implement toString. or just tostring? */
	public abstract String getName();
	
	/**
	 * File rename is allowed to make it fit format
	 */
	public abstract EvMovieEncoder getInstance(File path, int w, int h, String quality) throws Exception;

	/**
	 * Get a list of associated quality levels
	 */
	public abstract List<String> getQualities();
	
	/**
	 * Get the default quality. Should be the same pointer as in qualities list
	 * OR NOT
	 */
	public abstract String getDefaultQuality();
	
	
	/**
	 * Get maker given name
	 */
	public static EvMovieEncoderFactory getFactory(String s)
		{
		for(EvMovieEncoderFactory f:makers)
			{
			if(f.getName().equals(s))
				return f;
			}
		return null;
		}
	}
