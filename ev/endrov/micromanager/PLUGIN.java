package endrov.micromanager;
import endrov.ev.EV;
import endrov.ev.PluginDef;
import endrov.micromanager.MicroManager;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Recording hardware";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		return EV.isLinux();
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
		return new Class[]{MicroManager.class};
		}
	}