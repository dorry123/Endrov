package endrov.flowBinaryMorph;


import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;

/**
 * Flow unit: create kernel by drawing one
 * @author Johan Henriksson
 *
 */
public class FlowUnitBinMorphDrawKernel2D extends FlowUnitBasic
	{
	public static final String showName="Binary Draw Kernel 2D";
	private static final String metaType="binaryDrawKernel2d";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitBinMorphDrawKernel2D.class, null,
				"Draw kernel for binary morphology"));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", BinMorphKernel.FLOWTYPE); //TODO same type as "image"
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		
		//TODO
		//TODO
		//TODO
		//TODO
		
		lastOutput.put("out", null);
		}

	
	}