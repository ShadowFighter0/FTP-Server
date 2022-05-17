package ClientPackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Settings {

	static String settingspath = "src\\ClientPackage\\settings.txt";
	
	private int port;
	private int subConnectionPort;
	private String mainPath;
	
	enum connectionMode {Active, Pasive};
	private connectionMode mode;
	
	public Settings() {
		
		try {
		
			File settingFile = new File(settingspath);
			Scanner reader = new Scanner(settingFile);
			System.out.println(settingFile);
			
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
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public String getPath()
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