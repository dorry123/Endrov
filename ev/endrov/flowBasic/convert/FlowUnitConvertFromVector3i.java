/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.convert;


import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.util.math.Vector3i;

/**
 * Flow unit: Convert from Vector3i
 * @author Johan Henriksson
 *
 */
public class FlowUnitConvertFromVector3i extends FlowUnitBasic
	{
	public static final String showName="From Vector3i";
	private static final String metaType="channelConvertFromVector3i";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitConvertFromVector3i.class, CategoryInfo.icon,
		"Convert from Vector3i");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitOutput(Vector3i.class, decl);
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return CategoryInfo.icon;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("in", FlowType.TVECTOR3I);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("x", FlowType.TINTEGER); 
		types.put("y", FlowType.TINTEGER); 
		types.put("z", FlowType.TINTEGER); 
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		Vector3i in=(Vector3i)flow.getInputValue(this, exec, "in");
		
		lastOutput.put("x", in.x);
		lastOutput.put("y", in.y);
		lastOutput.put("z", in.z);
		}

	public String getHelpArticle()
		{
		return "Misc flow operations";
		}

	}
