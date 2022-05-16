package ServerPackage;

import java.io.*;
import java.net.*;

import ServerPackage.Settings.connectionMode;

public class Server {
	
	static boolean end = false;
	
	static String mainPath = "";
	static String subPath = "";
	public static String path()
	{
		return mainPath + subPath;
	}
	
	static Settings settings;
	
	static SubConnection subConnection;
	static UserControl users;

	static BufferedReader reader;
	static PrintWriter writter;
	
	public static void main(String args[]) {

		System.out.println("SERVER");	

		settings = new Settings();
		
		mainPath = settings.getMainPath();
		System.out.println(mainPath);
		
		ServerSocket sServ;
		Socket socket;
		
		String data = "";
		
		try {
			while (true) // The server will connect to a client
			{
				// Create the socket
				sServ = new ServerSocket(settings.getPort());
				subConnection = new SubConnection();
				users = new UserControl();
				// Accept a connection and create the socket for the transmission with the
				// client
				System.out.println("Waiting for client to connect");
				socket = sServ.accept();
				System.out.println("New Client Accepted");

				// Create input/output from the socket
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writter = new PrintWriter(socket.getOutputStream(), true);
				
				writter.flush();
				writter.println("220"); // 220 -> connection is established
				
				while (!end) // Current Connection
				{
					// Read the data sent by the client
					data = reader.readLine();

					System.out.println("Server receives: " + data);

					CommandSelector(data);
				}
				
				//Close the socket
				reader.close();
				writter.close();
				socket.close();
				sServ.close();
				
				end = false; 
				
			}			
		} 
		catch (IOException e) 
		{
			System.out.println(data);
			System.out.println(e.getClass());
			e.printStackTrace();
		}
	}

	private static void CommandSelector(String commandStr) throws IOException
	{
		String[] command = commandStr.split(" ");
		command[0] = command[0].toUpperCase();

		switch (command[0]) {

			case "CONN": //Connect active mode
				
				if (!subConnection.Connected)
				{				
					if (subConnection.ConnectActiveSubConnection(command[1]))
					{
						writter.println("200");
						System.out.println("The subConnection has been created with Active Mode");
					}
					else
					{
						writter.println("503");
						System.out.println("Error");
					}
				}
				else	
				{
					writter.println("503"); //Command
					System.out.println("SubConnection already running");
				}

				break;
				
			case "LIST":
					
				try {
					
					if (command.length > 1)
					{
						File f = new File(command[1]);
					}
					else
					{ 
						File f = new File(path());
					}
					
					writter.println("150"); //Opening data connection
					
					if (!AutoConnect(subConnection))
					{
						writter.println("425"); //Error opening data connection
						return;
					}
					else
					{
						writter.println("ok");
					}
					
					try {
						
						if (command.length > 1)
						{
							subConnection.SendListToClient(command[1]);
						}
						else
						{ 
							subConnection.SendListToClient(path());
						}
						
						writter.println("226"); //Connection closing
						
					}
					catch (Exception e) {		
						writter.println("451");
					}
				}
				catch (Exception e){
					
					if(e.getClass().equals(FileNotFoundException.class))
					{
						writter.println("550");
						System.out.println("Requested action not taken");
					}
					else
					{
						writter.println("450");
						System.out.println("Requested file action not taken");
					}
					break;
				}
				
				
				
				break;
				
			case "PASV":
				
				writter.println("" + settings.getSubConnectionPort());
				
				if (subConnection.StartPasiveSubConnection(settings.getSubConnectionPort()))
				{					
					writter.println("227");
				}
				
				break;
				
			case "STOR": //Store a file
				
				AutoConnect(subConnection);
				
				// Confirmation message
				writter.println("200"); //Ok Message
					
				try {	
			
					subConnection.ReceiveFileFromClient(path());
			
				} catch (Exception e) {
					System.out.println("Something went wrong");			
					e.printStackTrace();
				}	
				
				break;
				
				
			case "USER":
				
				String username = command[1];
				User user = users.userExists(username);

				writter.println("331");
				command = reader.readLine().split(" ");
				
				if (!command[0].toUpperCase().contains("PASS"))
				{
					writter.println("530");
					break;
				}
				
				if (user == null)
				{
					writter.println("530");
					break;
				}
				
				if(user.getPassword().equals(command[1]))
				{
					writter.println("230");
				}
				
				break;
				
				
			case "MKD":
				
				File folder = new File(path() + command[1]);
				
				if (folder.mkdir())
				{
					//Folder created
					writter.println("257");
				}
				else
				{
					//Eror creating a folder
					writter.println("550");
				}
				
				
				break;
				
			case "PWD":
				
				writter.println("\\"+subPath);
				break;
				
				
			case "RETR":
				
				try {
					File f = new File(path() + command[1]);
					
					if (f.exists())
					{
						writter.println("150");
					}
					else
					{
						writter.println("550");
					}					
					
					if (!AutoConnect(subConnection))
					{
						writter.println("425"); //Error opening data connection
						return;
					}
					else
					{
						writter.println("OK");
					}
					
					subConnection.SendFileToClient(path() + command[1]);
					
					writter.println("226");
					
				} catch (IOException e) {
					writter.println("451");
				}
				
				break;
				
			case "CWD":
				
				try 
				{
					if(!command[1].endsWith("\\"))
					{	
						command[1]+="\\";
					}
					
					File f = new File(path() + command[1]);
					
					if(!f.isDirectory())
					{
						writter.println("550");
					}
					else
					{
						subPath += command[1];
						
						writter.println("250");
					}					
					
				}catch(Exception e)	{			}
				
				break;
				
			case "CD":

				if (subPath.length() == 0)
				{
					writter.println("550");
					break;
				}
				
				for(int i = subPath.length()-2; i >= 0; i--)
				{
					
					if(subPath.charAt(i) == '\\')
					{
						subPath = subPath.substring(0,i+1);
						break;
					}
					
					if(i == 0)
					{
						subPath = "";
					}
				}
				
				writter.println("250");
				break;
				
			case "DELE":
				
				try {
				File file = new File(path() + command[1]);
				
				if (file.delete())
				{
					writter.println("250");
				}
				else
				{
					writter.println("450");
				}
				
				}catch(Exception e)
				{
					writter.println("550");
				}
				break;
				
			case "RMD":
				
				try {
					File file = new File (command[1]);
					
					if(file.isDirectory())
					{
						file = null;
						DeleteFolder(command[1]);
						writter.println("250");
					}
					else
					{
						writter.println("550");
					}
				}catch(Exception e)
				{
					writter.println("550");
				}
				break;
				
			case "RNFR":
				
				try {
					File from = new File(command[1]);
					
					writter.println("250");
					command = reader.readLine().split(" ");
					
					File to = new File(command[1]);
						
					if(from.renameTo(to))
					{
						writter.println("250");
					}
					else
					{
						writter.println("553");
					}
				}
				catch(Exception e)
				{
					writter.println("550");
				}
				
				break;
				
			case "QUIT": //End the current connection
					
				end = true;
				System.out.println("Ending connection with current client");
				writter.println("221");
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				break;
	
			default:
				writter.println("Unknown");
				System.out.println("Didn't understand the command");
				break;
		}
	}
	
	private static void DeleteFolder(String folderPath)
	{
		File folder = new File(folderPath);
		File[] folderContent = folder.listFiles();
		for(int i = 0; i < folderContent.length; i++)
		{
			if(folderContent[i].isFile())
			{
				folderContent[i].delete();
			}
			else
			{
				DeleteFolder(folderContent[i].getAbsolutePath());
						
				folderContent[i].delete();
			}
		}
		folder.delete();
	}
	
	private static boolean AutoConnect(SubConnection subConnection) throws IOException
	{
		if (!subConnection.Connected)
		{
			if (settings.getConnectionMode() == connectionMode.Pasive)
			{
				writter.flush();
				writter.println(settings.getSubConnectionPort());
				return subConnection.StartPasiveSubConnection(settings.getSubConnectionPort());
			}
			else
			{
				return subConnection.ConnectActiveSubConnection(reader.readLine());
			}
		}
		return true;
	}
}
