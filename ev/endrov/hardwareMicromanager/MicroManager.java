/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareMicromanager;

import java.io.File;
import java.util.*;

import mmcorej.CMMCore;
import mmcorej.Configuration;
import mmcorej.DeviceType;
import mmcorej.MMEventCallback;
import mmcorej.PropertyBlock;
import mmcorej.PropertyPair;
import mmcorej.PropertySetting;

import org.jdom.Element;
import org.micromanager.conf2.ConfiguratorDlg2;

import endrov.hardware.*;
import endrov.recording.ResolutionManager;
import endrov.recording.ResolutionManager.ResolutionState;
import endrov.starter.EvSystemUtil;

/**
 * Micromanager hardware interface
 * @author Johan Henriksson
 *
 */
public class MicroManager extends EvDeviceProvider implements EvDevice
	{
	
	
	public class CoreEventCallback extends MMEventCallback {
		//TODO whole bunch of methods here to override
	}
	
	CMMCore core;
	private static CoreEventCallback cb_ ;
	File configFile;
	
	private EvDeviceObserver.DeviceListener coreListener=new EvDeviceObserver.DeviceListener()
		{
		public void devicePropertyChange(Object source, EvDevice dev)
			{
			EvCoreDevice evcore=EvHardware.getCoreDevice();
			if(dev==evcore)
				{
				try
					{
					String setAutofocus=trySetEvCoreDevice(evcore, "AutoFocus");
					if(setAutofocus!=null)
						core.setAutoFocusDevice(setAutofocus);
					
					String setCamera=trySetEvCoreDevice(evcore, "Camera");
					if(setCamera!=null)
						core.setCameraDevice(setCamera);
					
					String setFocus=trySetEvCoreDevice(evcore, "Focus");  //ZStage
					if(setFocus!=null)
						core.setFocusDevice(setFocus);
					
					String setSLM=trySetEvCoreDevice(evcore, "SLM");
					if(setSLM!=null)
						core.setSLMDevice(setSLM);
					
					String setShutter=trySetEvCoreDevice(evcore, "Shutter");
					if(setShutter!=null)
						core.setShutterDevice(setShutter);
					
					String setXYStage=trySetEvCoreDevice(evcore, "XYStage");
					if(setXYStage!=null)
						core.setXYStageDevice(setXYStage);

					boolean setAutoshutter=evcore.getPropertyValueBoolean("AutoShutter");
					if(!core.getAutoShutter()==setAutoshutter)
						core.setAutoShutter(setAutoshutter);
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				
				}
			}
	};
		
	/**
	 * Returns the device to use according to the endrov core. Returns null if there is no need to set it
	 */
	private String trySetEvCoreDevice(EvCoreDevice evcore, String devicePropertyName) throws Exception
		{
		String device=evcore.getPropertyValue(devicePropertyName);
		String setDevice="";
		if(!device.equals(""))
			{
			EvDevicePath devpath=new EvDevicePath(device);
			if(devpath.path.length==2 && devpath.path[0].equals("um"))//core.getDeviceType(devpath.path[1])==deviceType)
				setDevice=devpath.path[1];
			}
		
		//Switch device if needed
		String currentDevice=core.getProperty("Core", devicePropertyName);
		if(!currentDevice.equals(setDevice))
			return setDevice;
		return null;
		}
	

	/**
	 * Constructor
	 */
	public MicroManager()
		{
		try
			{

			
			core=new CMMCore();

			cb_= new CoreEventCallback();
      core.registerCallback(cb_);

			//core.enableStderrLog(true);
			core.enableDebugLog(false);
			
			File fMMconfig1=new File(EvSystemUtil.getGlobalConfigEndrovDir(),"MMConfig.cfg");
			File fMMconfig=fMMconfig1;
			fMMconfig.getParentFile().mkdirs();
			if(!fMMconfig.exists())
				fMMconfig=new File("MMConfig.cfg");
			if(!fMMconfig.exists())
				{
				System.out.println("No config file found ("+fMMconfig1+" nor "+fMMconfig+")");
				configFile=new File(EvSystemUtil.getGlobalConfigEndrovDir(),"MMConfig.cfg");
				configFile.createNewFile();
				return;
				}
			System.out.println("Micro-manager version "+core.getAPIVersionInfo()+" loading config "+fMMconfig.getAbsolutePath());
			
			configFile=fMMconfig;
			core.loadSystemConfiguration(fMMconfig.getPath());

			
			populateProviderFromMMCore();
			
			//Listen for core device selection of devices
			EvHardware.getCoreDevice().event.addWeakListener(coreListener);
			}
		catch (Throwable e) 
			{
			e.printStackTrace();
			System.out.println("err:"+e.getMessage());
			}
		
		
		}
	
	
	private TreeSet<String> isXY;
	private TreeSet<String> isStage;
	private TreeSet<String> isShutter;
	private TreeSet<String> isSerial;
	private TreeSet<String> isAutoFocus;
	private TreeSet<String> isCamera;
	private TreeSet<String> isState;
	private TreeSet<String> isSLM;
	
	private void populateProviderFromMMCore() throws Exception
		{

		//Update list devices
		System.out.println("Device status:");
		for (String device:MMutil.getLoadedDevices(core))
			{
			System.out.println("device: "+device);
			for(Map.Entry<String, String> prop:MMutil.getPropMap(core,device).entrySet())
				{
				System.out.print(" " + prop.getKey() + " = " + prop.getValue());
				System.out.println("  "+MMutil.convVector(core.getAllowedPropertyValues(device, prop.getKey())));
				}
			}

		//Micro-manager has a defunct getDeviceType(), this is a work-around
		//or is it? I think they have a different notion of filter
		//Collection<String> isMagnifier=MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.MagnifierDevice));
		isXY=new TreeSet<String>(MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.XYStageDevice)));
		isStage=new TreeSet<String>(MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.StageDevice)));
		isShutter=new TreeSet<String>(MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.ShutterDevice)));
		isSerial=new TreeSet<String>(MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.SerialDevice)));
		isAutoFocus=new TreeSet<String>(MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.AutoFocusDevice)));
		isCamera=new TreeSet<String>(MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.CameraDevice)));
		isState=new TreeSet<String>(MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.StateDevice)));
		isSLM=new TreeSet<String>(MMutil.convVector(core.getLoadedDevicesOfType(DeviceType.SLMDevice)));
		
		
		//Register all devices
		for(String devName:MMutil.convVector(core.getLoadedDevices()))
			{
			//Device fundamentals
			EvDevice adp;
			if(isCamera.contains(devName))
				adp=new MMCamera(this,devName);
			//else if(isMagnifier.contains(devName))
				//adp=new MMMagnifier(this,devName);
			else if(isXY.contains(devName))
				adp=new MMStage(this,devName,true);
			else if(isStage.contains(devName))
				adp=new MMStage(this,devName,false);
			else if(isShutter.contains(devName))
				adp=new MMShutter(this,devName);
			else if(isAutoFocus.contains(devName))
				adp=new MMAutoFocus(this,devName);
			else if(isSLM.contains(devName))
				adp=new MMSpatialLightModulator(this,devName);
			else if(isSerial.contains(devName))
				adp=new MMSerial(this,devName);
			else if(isState.contains(devName))
				adp=new MMState(this,devName);
			else
				adp=new MMDeviceAdapter(this,devName);
			//System.out.println(devName+"---"+adp+" "+adp.getDescName()+" ???? "+core.getDeviceType(devName));
			
			//Exclude the core device
			if(!devName.equals("Core"))
				hw.put(devName,adp);
			}
		
		
		/**
		 * Read property blocks
		 * 
		 * Currently not used
		 */
		for(String blockName:MMutil.convVector(core.getAvailablePropertyBlocks()))
			{
			System.out.println("block: "+blockName);
			PropertyBlock b=core.getPropertyBlockData(blockName);
			HashMap<String, String> prop=new HashMap<String, String>(); 
			try
				{
				for(int i=0;i<b.size();i++)
					{
					PropertyPair pair=b.getPair(i);
					prop.put(pair.getPropertyName(), pair.getPropertyValue());
					}
				}
			catch (Exception e)
				{
				e.printStackTrace();
				System.out.println("This should never happen");
				}

			System.out.println("Property blocks: "+prop);
			}
		
		
		/**
		 * Convert micromanager config groups into endrov config groups.
		 * These groups cannot control non-micromanager devices
		 * 
		 */
		for(String configGroupName:core.getAvailableConfigGroups())
			{
			EvHardwareConfigGroup hwg=new EvHardwareConfigGroup();
			for(String configGroupStateName:core.getAvailableConfigs(configGroupName))
				{
				mmcorej.Configuration config=core.getConfigState(configGroupName, configGroupStateName);
				convertConfigState(hwg, configGroupStateName, config);
				}
			EvHardwareConfigGroup.putConfigGroup(configGroupName, hwg);
			}
		
		/**
		 * Convert micromanager pixel size configurations
		 */
		for(String pixelSizeName:core.getAvailablePixelSizeConfigs())
			{
			Configuration conf=core.getPixelSizeConfigData(pixelSizeName);
			double umRes=core.getPixelSizeUmByID(pixelSizeName);

			//Assign resolution to the first available camera
			if(!isCamera.isEmpty())
				{
				EvDevicePath campath=mmDeviceToEndrov(isCamera.iterator().next());
			
				ResolutionState resState=new ResolutionState();
				resState.state=convertConfigState(conf);
				resState.cameraRes=new ResolutionManager.Resolution(umRes, umRes);
				
				ResolutionManager.getCreateResolutionStatesMap(campath).put(pixelSizeName, resState);
				}
			}

		}
	
	
	private static void convertConfigState(EvHardwareConfigGroup hwg, String configGroupStateName, Configuration conf) throws Exception
		{
		EvHardwareConfigGroup.State state=convertConfigState(conf);
		hwg.propsToInclude.addAll(state.propMap.keySet());
		hwg.putState(configGroupStateName,state);
		}
	
	
	private static EvHardwareConfigGroup.State convertConfigState(Configuration conf) throws Exception
		{
		EvHardwareConfigGroup.State state=new EvHardwareConfigGroup.State();
		for(int i=0;i<conf.size();i++)
			{
			PropertySetting umSetting=conf.getSetting(i);
			String propName=umSetting.getPropertyName();
			String value=umSetting.getPropertyValue();
			String mmDevName=umSetting.getDeviceLabel();
			EvDevicePropPath evPropPath=mmDevicePropToEndrov(mmDevName, propName);
			state.propMap.put(evPropPath, value);
			}
		return state;
		}
		
	
	private static EvDevicePath mmDeviceToEndrov(String mmDevName)
		{
		return new EvDevicePath(new String[]{"um",mmDevName});
		}
	
	private static EvDevicePropPath mmDevicePropToEndrov(String mmDevName, String propName)
		{
		return new EvDevicePropPath(mmDeviceToEndrov(mmDevName), propName);
		}
	
	
	public Set<EvDevice> autodetect()
		{
		return null;
		}

	public void getConfig(Element root)
		{
		}

	public void setConfig(Element root)
		{
		}
	
	public List<String> provides()
		{
		List<String> list=new LinkedList<String>();

		
		
		return list;
		}
	public EvDevice newProvided(String s)
		{
		return null;
		}


	public String getDescName()
		{
		return "Micro-manager";
		}


	public SortedMap<String, String> getPropertyMap()
		{
		return new TreeMap<String, String>();
		}


	public SortedMap<String, DevicePropertyType> getPropertyTypes()
		{
		return new TreeMap<String, DevicePropertyType>();
		}


	public String getPropertyValue(String prop)
		{
		return null;
		}


	public Boolean getPropertyValueBoolean(String prop)
		{
		return null;
		}


	public void setPropertyValue(String prop, boolean value)
		{
		}


	public void setPropertyValue(String prop, String value)
		{
		}
	
	
	public boolean hasConfigureDialog(){return true;}
	public void openConfigureDialog()
		{
		ConfiguratorDlg2 dlg=new ConfiguratorDlg2(core,configFile.getAbsolutePath());
		dlg.setVisible(true);
		try
			{
			populateProviderFromMMCore();
			EvHardware.updateAvailableDevices();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	
	
	public EvDeviceObserver event=new EvDeviceObserver();
	
	public void addDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.addWeakListener(listener);
		}
	
	
	public void removeDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.remove(listener);
		}

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvHardware.getRoot().hw.put("um", new MicroManager());
		
		}

	}
