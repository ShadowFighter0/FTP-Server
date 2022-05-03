package ClientPackage;

import java.io.*;
import java.net.*;

public class Client {

	public static void main(String args[]) 
	{
		
		boolean end = false;
		int port = 21;

		Socket sCon = null;
		
		// From the socket
		BufferedReader reader;
		PrintWriter writter;

		// From the keyboard
		BufferedReader inputKeyboard;
		
		
		SubConnection subConnection;
		
		
		System.out.println("CLIENT");

		String data;

		try {
			
			//Wait for a server 
			sCon = WaitForConnection(port);

			//Create writters and readers
			reader = new BufferedReader(new InputStreamReader(sCon.getInputStream()));
			writter = new PrintWriter(sCon.getOutputStream(), true);
			inputKeyboard = new BufferedReader(new InputStreamReader(System.in));			

			//Create SubConnection

			subConnection = new SubConnection();

			data = reader.readLine();
			
			if (data.contains("220")) //220 -> Ok 
			{
				System.out.println("Conection Success");
			}

			// Get the input/output from the socket
			while (!end) 
			{
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
					CommandSelector(command,subConnection, reader);
				}
			}
			
			// Close the connection
			sCon.close();
			
		} catch (Exception e) {
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
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
				Thread.sleep(2000);
			}
		}
	}
	
	private static boolean CommandChecker(String[] command, SubConnection subConnection)
	{
		switch (command[0])
		{
			case "PORT":
				
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
				}
				catch (Exception e) {
					System.out.println("Wrong Command Format: Port is not a number");
					return false;
				}
				
				return true;
				
				
			case "LIST":
				
				if (!subConnection.Connected)
				{
					System.out.println("There is no sub Connection created.");
					return false;
				}
				
				return true;
				
				
			case "STOR":
				
				return true;
			case "END":
				
				return true;

				
			default: 
				return false;
		}
	}

	private static void CommandSelector(String[] command, SubConnection subConnection, BufferedReader reader) 
	{
		switch (command[0]) {
		
		case "PORT":
			
			try {
				// Start connection
				subConnection.StartActiveSubConnection(command[1]);
				
				Thread.sleep(100);
				String data = reader.readLine();
				
				// Get server response
				if (data.equals("200"))
					System.out.println("200 -> SubConnection Running");
				else if (data.equals("503"))
					System.out.println("503 -> SubConnection Error");
				else
					System.out.println(data + " -> Non recognise code");
				
				
			}catch (Exception e)
			{
				System.out.println("Something went wrong");
			}
			
			break;
			
		case "LIST":
			
			String message;
			try {

				while (true) 
				{
					message = subConnection.socketReader.readLine();
					
					if (message.equals("END")) 
					{
						break;
					} 
					else
					{
						System.out.println(message);
					}
				}
			}
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
			
			break;
			
		case "PASV":
			
			break;

		case "STOR":

			try {
				//SendFileToServer(inputKeyboard, reader);
			} catch (Exception e) {
				System.out.println("Something has gone wrong");
				e.printStackTrace();
			}

			break;
			
		case "END":
			
				//end = true;
			
			break;
				
		default:
			System.out.println("Not implented yet");
		}
	}

	private static void SendFileToServer(BufferedReader inputKeyboard, BufferedReader mainSocketReader)
			throws Exception {

		System.out.println("Enter the path:");

		String path = inputKeyboard.readLine();

		int port = Integer.parseInt(mainSocketReader.readLine());

		Socket sCon = new Socket("localhost", port);
		System.out.println("Connected to new Port: " + port);

		BufferedReader socketReader = new BufferedReader(new InputStreamReader(sCon.getInputStream()));
		PrintWriter socketWritter = new PrintWriter(sCon.getOutputStream(), true);

		// Create File and array
		File file = new File(path);
		byte[] bytes = new byte[(int) file.length()];

		// Convert a File into byte[]
		FileInputStream fileConverter = new FileInputStream(file);
		System.out.println(fileConverter.read(bytes, 0, bytes.length));

		// Create printer for bytes into the socket
		DataOutputStream bytePrinter = new DataOutputStream(sCon.getOutputStream());

		// Send the filename
		socketWritter.flush();
		socketWritter.println(file.getName());

		// Send length
		socketWritter.flush();
		socketWritter.println(bytes.length);

		// Send bytes
		Thread.sleep(100);

		System.out.println("Sending Bytes");
		bytePrinter.flush();
		bytePrinter.write(bytes);

		System.out.println("Upload Complete. Waiting for server confirmation");

		String data = mainSocketReader.readLine();

		if (data.equals("OK"))
			System.out.println("Transfer Status: Ok");
		else
			System.out.println("Transfer Status: Error");

		fileConverter.close();
		fileConverter = null;

		bytePrinter.close();
		bytePrinter = null;

		socketReader.close();
		socketReader = null;

		socketWritter.close();
		socketWritter = null;

		sCon.close();
		sCon = null;
	}

}
