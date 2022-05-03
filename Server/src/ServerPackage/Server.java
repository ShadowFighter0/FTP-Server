package ServerPackage;

import java.io.*;
import java.net.*;

public class Server {

	
	static boolean end = false;

	public static void main(String args[]) {

		System.out.println("SERVER");

		int port = 21;
		ServerSocket sServ;
		Socket socket;

		SubConnection subConnection;

		BufferedReader reader;
		PrintWriter writter;
		
		String data = "";
		String path = "D:\\Redes\\Server";
		
		try {

			// Create the socket
			sServ = new ServerSocket(port);
			subConnection = new SubConnection();

			while (true) // The server will connect to a client
			{
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
				
				while(!end) // Current Connection
				{
					// Read the data sent by the client
					data = reader.readLine();

					System.out.println("Server receives: " + data);

					CommandSelector(data, subConnection, writter, path);

				}
				//Close the socket
				socket.close();
				
				break;
			}
			// Close the server socket
			sServ.close();
			subConnection = null;
			
		} 
		catch (Exception e) 
		{
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
	}

	private static void CommandSelector(String commandStr, SubConnection subConnection, PrintWriter writter, String path) 
	{
		
		String[] command = commandStr.split(" ");
		command[0] = command[0].toUpperCase();

		switch (command[0]) {

			case "PORT": //Connect pasive mode
				
				if (!subConnection.Connected)
				{				
					if (subConnection.ConnectActiveSubConnection(command[1]))
					{
						writter.println("200");
						System.out.println("The subConnection has been created with Active Mode");
					}
					else
					{
						writter.println("503"); //Command
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
				
				String message = ReadFolder(path, 0);
				subConnection.socketWritter.println(message);
				subConnection.socketWritter.println("END");
				break;
				
			case "PASV":
				
				break;
				
			case "STOR": //Store a file
				
				// Confirmation message
				writter.println("200"); //Ok Message
					
				try {	
			
					//subConnection.ReceiveFileFromClient(reader, writter);
			
				} catch (Exception e) {
					System.out.println("Something went wrong");					
				}	
				
				
				break;
	
			case "END": //End the current connection
				end = true;
				writter.println("OK");
				System.out.println("Ending connection with current client");
				break;
	
			default:
				writter.println("Unknown");
				System.out.println("Didn't understand the command");
				break;
		}
	}
	
	private static String ReadFolder(String path, int deepLevel)
	{
		String message = "";
		
		File folder = new File(path);
		
		for (File file : folder.listFiles()) 
		{
			for(int i = 0; i < deepLevel; i++)
			{
				message += "  ";
			}
			
			if (file.isDirectory())
			{
				message += "Folder " + ReadFolder(file.getPath(), deepLevel+1);
			}
			else
			{
				message += file.getName() + "\n";
			}
		}
		
		return message;
		
	}

}
