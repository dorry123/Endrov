/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowLineage;
import endrov.core.EvPluginDefinition;

public class PLUGIN extends EvPluginDefinition
	{
	public String getPluginName()
		{
		return "Lineage viewer";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		return true;
		}
	
	public String cite()
		{
		return "";
		}
	
	public String[] requires()
		{
		return new String[]{};
		}
	
	public Class<?>[] getInitClasses()
		{
		return new Class[]{LineageWindow.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
