package ServerPackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Settings {

	static String settingspath = "src\\ServerPackage\\settings.txt";
	
	private String mainPath;
	private int port;
	private int subConnectionPort;
	private int packetSize;
	
	enum connectionMode {Active, Pasive};
	private connectionMode mode;
	
	public Settings() {
		
		try {
		
			File settingFile = new File(settingspath);
			Scanner reader = new Scanner(settingFile);
			
			while (reader.hasNextLine())
			{
				String[] settings = reader.nextLine().split(":");

				switch (settings[0])
				{
					case "MAIN_PORT":
						
						port = Integer.parseInt(settings[1]);
						
						break;
						
					case "SUBCONNECTION_PORT":
						
						subConnectionPort = Integer.parseInt(settings[1]);
						
						break;
						
					case "CONNECTION_MODE":
						
						if (settings[1].contains("PASIVE"))
						{
							mode = connectionMode.Pasive;
						}
						else
						{
							mode = connectionMode.Active;
						}
						
						break;
						
					case "MAIN_PATH":
						
						mainPath = settings[1] + ":";
						for(int i = 2; i < settings.length; i++)
						{
							mainPath += settings[i];					
						}
						
						if(!mainPath.endsWith("\\"))
						{
							mainPath+="\\";
						}
						
						break;
						
					case "PACKETSIZE":
						
						try {
							packetSize = Integer.parseInt(settings[1]);
						}
						catch(Exception e)
						{
							System.out.println("Unable to read PACKETSIZE property from settings file. Please fix the problem and restart.");
						}
						break;
				}
			}
		} catch (Exception e) {		}		
	}
	
	public int getPacketSize()
	{
		return packetSize;
	}
	
	public String getMainPath()
	{
		return mainPath;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public int getSubConnectionPort()
	{
		return subConnectionPort;
	}
	
	public connectionMode getConnectionMode()
	{
		return mode;
	}
	
	@Override 
	public String toString()
	{
		return "[port: "+ port + ", subconnection: " + subConnectionPort + ", connectionMode: " + mode.toString() +"]";
	}
}