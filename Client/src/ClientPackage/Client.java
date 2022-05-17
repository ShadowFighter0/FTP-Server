package ClientPackage;

import java.io.*;
import java.net.*;

import ClientPackage.Settings.connectionMode;

public class Client {

	static boolean end = false;
	static Settings settings;
	
	static BufferedReader reader;
	static PrintWriter writter;
			// From the keyboard
	static BufferedReader inputKeyboard;
	static SubConnection subConnection;

	public static void main(String args[]) 
	{
		Socket sCon = null;
		
		System.out.println("CLIENT");

		settings = new Settings();
		
		String data;

		try {
			
			//Wait for a server 
			sCon = WaitForConnection(settings.getPort());

			//Create writters and readers
			reader = new BufferedReader(new InputStreamReader(sCon.getInputStream()));
			writter = new PrintWriter(sCon.getOutputStream(), true);
			inputKeyboard = new BufferedReader(new InputStreamReader(System.in));			

			//Create SubConnection
			subConnection = new SubConnection();

			if (reader.readLine().contains("220")) //220 -> Ok 
			{
				System.out.println("Conection Success");
			}

			// Get the input/output from the socket
			while (!end) 
			{
				try {
					// Get command from the keyboard
					System.out.print("Write command (END to close the server): ");
					data = inputKeyboard.readLine();			
					
					String[] command = data.split(" ");
					command[0] = command[0].toUpperCase();
					
					//Command Security
					if (CommandChecker(command, subConnection))
					{
						// Send data to the server
						writter.println(data);
										
						//Apply the command
						CommandSelector(command,subConnection);
					}
				}
				catch(Exception e)
				{
					System.out.println("Something failed in the command");
				}
			}
			
			// Close the connection
			sCon.close();
			
			System.out.println("Shutting down the client");
			
		} catch (Exception e) {		}
	}
	
	private static Socket WaitForConnection(int port) throws InterruptedException
	{
		while (true) 
		{
			try 
			{
				// Connect to the server
				return new Socket("localhost", port);				

			} catch (Exception e) {

				// Error in connection. Trying again
				System.out.println("Server not found. Trying again in 2 secs.");
				Thread.sleep(1000);
			}
		}
	}
	
	private static boolean CommandChecker(String[] command, SubConnection subConnection)
	{
		switch (command[0])
		{
			case "CONN":
				
				if (subConnection.Connected)
				{
					System.out.println("There is already a subconnection running");
					return false;
				}
				
				if (command.length != 2)
				{
					System.out.println("Wrong Command Format: Port Missing");
					return false;
				}
				
				try 
				{
					Integer.parseInt(command[1]);
					return true;
				}
				catch (Exception e) {
					System.out.println("Wrong Command Format: Port is not a number");
					return false;
				}
				
				
			case "LIST":
				
				if (command.length > 2)
				{
					System.out.println("Wrong Command Format: ");
					return false;
				}
				
				return true;
				
				
			case "STOR":
				
				if (command.length < 2)
					return false;
				
				try {
					File file  = new File(command[1]);
					
					if(!file.isFile())
					{
						System.out.println("The File does not exist");
						return false;
					}
					return true;
					
				}catch (Exception e)
				{
					
					System.out.println("The File does not exist");
					return false;
				}
				
			case "RETR":
				
				if (command.length != 2)
					return false;

				return true;
				
			case "PASV":
				
				if (subConnection.Connected)
				{
					System.out.println("There is already a subconnection running");
					return false;
				}
				
				return true;
			case "PWD":
				
				return true;
				
			case "CWD":
				
				if (command.length != 2)	return false;
				
				return true;
				
			case "CD":
				return true;
			
				
			case "USER":
				
				if(command.length != 2) return false;
				
				return true;
				
			case "MKD":
				
				if(command.length != 2) return false;
				
				return true;
				
			case "DELE":
				
				if(command.length != 2) return false;
				
				return true;
				
			case "RMD":
				if(command.length != 2) return false;
				
				return true;
				
			case "RNFR":
				if(command.length != 2) return false;
				
				return true;
				
				
			case "QUIT":
				
				return true;

				
			default: 
				System.out.println("No command detected. Please try again");
				return false;
		}
	}

	private static void CommandSelector(String[] command, SubConnection subConnection) throws IOException 
	{
		String message;
		
		switch (command[0]) {
		
		case "CONN":
			
			try {
				if(!subConnection.Connected)
				{
					// Start connection
					subConnection.StartActiveSubConnection(command[1]);
				}
				else
				{

					System.out.println("SubConnection already running");
				}
				
				
				message = reader.readLine();
				
				// Get server response
				if (message.equals("200"))
					System.out.println("The subConnection has been created with Active Mode");
				else if (message.equals("503"))
					System.out.println("SubConnection Error");
				else
					System.out.println(message + " -> Non recognise code");
				
				
			}catch (Exception e)
			{
				System.out.println("Something went wrong");
			}
			
			break;
			
		case "LIST":
			
			try {
				message = reader.readLine();
				
				if (message.contains("550"))
				{
					System.out.println("Error: File unavailable");
					return;
				}
				else if (message.contains("450"))
				{
					System.out.println("Error: Requested file action not taken");
					return;
				}
				else if (message.contains("150"))
				{
					System.out.println("File status okay");
					AutoConnect(subConnection);

					message = reader.readLine();
					if (message.contains("425"))
					{
						System.out.println("Error: Can't open data connection");
						return;
					}
					else if (message.contains("451"))
					{
						System.out.println("Error: Requested action aborted");
						return;
					}
					
					subConnection.ReceiveList();
					
					if(reader.readLine().contains("226"))
					{
						System.out.println("Requested action successful");
					}
					else
					{
						System.out.println("Error: Error during receiving list");
					}
				}
			}
			catch (IOException e1){			}
			
			break;
			
		case "PASV":
			
				message = reader.readLine();
				
				if(!subConnection.Connected)
				{
					subConnection.ConnectPasiveSubConnection(message);
					
					if (reader.readLine().equals("227"))
						System.out.println("Connection created with pasive mode");
				}
				else
				{
					System.out.println("SubConnection already created");
				}
				

			break;

		case "STOR":

			try {
				
				AutoConnect(subConnection);
				
				
				subConnection.SendFileToServer(command[1]);
				if(reader.readLine().contains("200"))
				{
					System.out.println("Transfer completed");
				}
				
			} catch (Exception e) {
				System.out.println("Something has gone wrong");
				e.printStackTrace();
			}

			break;
			
		case "RETR":
			
				message = reader.readLine();
				
				if (message.contains("550"))
				{
					System.out.println("File unavailable");
					return;
				}
				else if(message.contains("150"))
				{
					System.out.println("File status okay");
				}
				
				AutoConnect(subConnection);

				message = reader.readLine();
				
				if (message.contains("425"))
				{
					System.out.println("Can't open data connection");
					return;
				}
				else if (message.contains("451"))
				{
					System.out.println("Requested action aborted");
					return;
				}
				
				try {
					subConnection.ReceiveFileFromServer(settings.getPath());
					
					message = reader.readLine();
					
					if (message.contains("226"))
					{
						System.out.println("Requested action successful");
						return;
					}
					else if (message.contains("450")) 
					{
						
					}
				}
				catch (Exception e)
				{
					
				}				
			
			break;
			
		case "PWD":
			
			try {
				
				System.out.println(reader.readLine());
			
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			break;
			
		case "CWD":
			try {
				
				message = reader.readLine();
				
				if(message.equals("250"))
				{
					System.out.println("Path changed correctly");
				}
				else if(message.equals("550"))
				{
					System.out.println("Fail changing the path");
				}
				
			} catch (IOException e1) {			}	
				
			break;
			
		case "CD":
				message = reader.readLine();
				if(message.equals("250"))
				{
					System.out.println("Path changed correctly");
				}
				else if(message.equals("550"))
				{
					System.out.println("You are currently in the root folder");
				}
			break;
			
		case "USER":
			
			try {
				if(reader.readLine().contains("331"))
				{
					String passwordCommand = "";
					boolean check = false;
					while(!check)
					{
						System.out.println("Please entre the password (PASS + <space> + password)");
						passwordCommand = inputKeyboard.readLine();
						
						String[] passwordCommandCheck = passwordCommand.split(" ");
						
						if(passwordCommandCheck.length == 2)
						{
							check = true;
						}
						else {
							System.out.println("Invalid command format.");
						}
					}
					
					writter.println(passwordCommand);
					
					message = reader.readLine();
					
					if (message.contains("230"))
					{
						System.out.println("You are now logged");
					}
					else if(message.contains("530"))
					{
						System.out.println("Log in error. Please try again");
					}
				}
				else {
					System.out.println("Log in error. Please try again");
				}
				
			} catch (IOException e) {	}
			
			break;
			
		case "MKD":
			
			try {
				message = reader.readLine();
				
				if (message.contains("257")) 
				{
					System.out.println("Folder created");
				}
				else
				{
					System.out.println("Error creating folder");
				}
				
			} catch (IOException e1) {
				System.out.println("Error creating a folder.");
			}
			
			break;
			
		case "DELE":
			
			try {
				message = reader.readLine();
				
				if (message.contains("250")) 
				{
					System.out.println("File removed");
				}
				else
				{
					System.out.println("Error removing file");
				}
				
			} catch (IOException e1) {
				System.out.println("Error removing file.");
			}
			
			break;
			
		case "RMD":
			
			message = reader.readLine();
			
			if(message.contains("250"))
			{
				System.out.println("Requested action successful");
			}
			else
			{
				System.out.println("Requested action not taken");
			}
			break;
			
		case "RNFR":
			
			try {
				message = reader.readLine();
				if (message.contains("350"))
				{
					System.out.println("Please introduce the new name (RNTO + Name)");
					String code = inputKeyboard.readLine();
					writter.println(code);
					
					message = reader.readLine();
					
					if(message.contains("250"))
					{
						System.out.println("Rename file action succed");
					}
					else
					{
						System.out.println("Error Rename file");
					}
				}
				else
				{
					System.out.println("Error renaming the file");
				}
			}catch(Exception e)
			{
				
			}
			
			break;
			
		case "QUIT":
			try {
				String code = reader.readLine();
				
				if (code.equals("221"))
				{
					System.out.println("Ending connection with server");

					end = true;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
				
		default:
			System.out.println("Not implented yet");
		}
	}
	
	private static void AutoConnect(SubConnection subConnection) throws IOException
	{
		if (!subConnection.Connected)
		{
			if (settings.getConnectionMode() == connectionMode.Pasive)
			{
				subConnection.ConnectPasiveSubConnection(reader.readLine());
			}
			else
			{
				writter.flush();
				writter.println(settings.getSubConnectionPort());
				subConnection.StartActiveSubConnection(""+settings.getSubConnectionPort());
			}
		}
	}
}
