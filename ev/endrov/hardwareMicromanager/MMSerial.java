/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareMicromanager;

import endrov.hardware.HWSerial;

//could preload list of properties

//mm virtual property: state. map to setstate


/**
 * Micro manager serial interface
 * @author Johan Henriksson
 *
 */
public class MMSerial extends MMDeviceAdapter implements HWSerial
	{
		
	public MMSerial(MicroManager mm, String mmDeviceName)
		{
		super(mm,mmDeviceName);
		}
	
	
	
	/*
	//void 	setSerialPortCommand (const char *deviceLabel, const char *command, const char *term) throw (CMMError)
	std::string 	getSerialPortAnswer (const char *deviceLabel, const char *term) throw (CMMError)
	void 	writeToSerialPort (const char *deviceLabel, const std::vector< char > &data) throw (CMMError)
	std::vector< char > 	readFromSerialPort (const char *deviceLabel) throw (CMMError)
*/
	
	public String nonblockingRead()
		{
		try
			{
			return MMutil.convVector(mm.core.readFromSerialPort(mmDeviceName));
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return "";
			}
		}
	public String readUntilTerminal(String term)
		{
		try
			{
			return mm.core.getSerialPortAnswer(mmDeviceName, term);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return "";
			}
		}
	public void writePort(String s)
		{
		try
			{
			mm.core.writeToSerialPort(mmDeviceName, MMutil.convString(s));
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	
	
	}
