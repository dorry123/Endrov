package endrov.shell;

import javax.swing.JMenu;

import org.jdom.DataConversionException;
import org.jdom.Element;

import endrov.data.*;
import endrov.imageWindow.*;
import endrov.keyBinding.KeyBinding;
import endrov.modelWindow.*;

/**
 * Shell metadata
 * @author Johan Henriksson
 */
public class Shell extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static final String metaType="shell";
	
	public static final int KEY_TRANSLATE=KeyBinding.register(new KeyBinding("Shell","Translate",'z'));
	public static final int KEY_SETZ=KeyBinding.register(new KeyBinding("Shell","Set Z",'x'));
	public static final int KEY_ROTATE=KeyBinding.register(new KeyBinding("Shell","Rotate",'c'));

	
	public static void initPlugin() {}
	static
		{
		ImageWindow.addImageWindowExtension(new ImageWindowExtension()
			{
			public void newImageWindow(ImageWindow w)
				{
				ShellImageRenderer r=new ShellImageRenderer(w);
				w.imageWindowTools.add(new ShellImageToolMakeShell(w,r));
				w.imageWindowRenderers.add(r);
				}
			});
		
		ModelWindow.modelWindowExtensions.add(new ModelWindowExtension()
			{
			public void newModelWindow(ModelWindow w)
				{
				w.modelWindowHooks.add(new ShellModelHook(w));
				}
			});
		
		EvData.extensions.put(metaType,new EvObjectType()
			{
			public EvObject extractObjects(Element e)
				{
				Shell shell=new Shell();
				try
					{
					shell.midx=e.getAttribute("x").getDoubleValue();
					shell.midy=e.getAttribute("y").getDoubleValue();
					shell.midz=e.getAttribute("z").getDoubleValue();
					shell.major=e.getAttribute("major").getDoubleValue();
					shell.minor=e.getAttribute("minor").getDoubleValue();
					shell.angle=e.getAttribute("angle").getDoubleValue();
					shell.angleinside=e.getAttribute("angleinside").getDoubleValue();
					}
				catch (DataConversionException e1)
					{
					e1.printStackTrace();
					}
				return shell;
				}
			});
		
		}
	

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public double major, minor;
	public double midx, midy, midz;
	public double angle;        //This is the rotation of the shell around z axis
	public double angleinside; //This is the rotation of the embryo inside shell around major axis
	
	
	/**
	 * Save to XML
	 */
	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		
		e.setAttribute("x", ""+midx);
		e.setAttribute("y", ""+midy);
		e.setAttribute("z", ""+midz);

		e.setAttribute("major", ""+major);
		e.setAttribute("minor", ""+minor);

		e.setAttribute("angle", ""+angle);
		e.setAttribute("angleinside", ""+angleinside);
		}
	
	
	/**
	 * Desciption of data
	 */
	public String getMetaTypeDesc()
		{
		return "Shell";
		}
	
	/** Additions to the object-specific menu */
	public void buildMetamenu(JMenu menu)
		{
		}

	
	}