package endrov.flow.std.math;


import java.util.Map;

import endrov.basicWindow.FlowExec;
import endrov.flow.Flow;
import endrov.flow.FlowUnitDeclaration;

/**
 * Flow unit: /
 * @author Johan Henriksson
 *
 */
public class FlowUnitDiv extends FlowUnitMathBinop
	{
	private static final String metaType="div";
	
	public FlowUnitDiv()
		{
		super("A/B",metaType);
		}
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Math","/",metaType,FlowUnitDiv.class, null));
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		if(a instanceof Double)
			lastOutput.put("C", ((Double)a)/toDouble(b));
		else
			throw new Exception("Unsupported numerical type "+a.getClass());
		}

	
	}
