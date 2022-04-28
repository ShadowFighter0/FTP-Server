package ServerPackage;
import java.io.*;
import java.net.*;

public class Server {
	
	public static void main(String args[]) 
	{
	  System.out.println("SERVER");
	  int port = 1400;
	  ServerSocket sServ;
	  Socket socket;
	  ConnectionHandler connectionHandler =  new ConnectionHandler();
	  BufferedReader reader;
	  PrintWriter writter;
		
	  String data = "";
	  int filePort = 1300;
	  
	  boolean end = false;
		
	  try {
		  
		// Create the socket
		sServ = new ServerSocket(port);		
		while(true)
		{
			// Accept a connection  and create the socket for the transmission with the client				
			System.out.println("Waiting for client to connect");
			socket = sServ.accept();
			System.out.println("New Client Accepted");
			
		    // Create input/output from the socket
		    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		    writter = new PrintWriter(socket.getOutputStream(), true);
			   
			while(!end) 
			{
				// Read the data sent by the client
			   data = reader.readLine();
			   data = data.toUpperCase();
			   System.out.println("Server receives: " + data);
			   
			   switch (data)
			   {
			   	 case "FILE":
				   //Confirmation message
				   writter.println("OK");
				   
				   try {
					   connectionHandler.ReceiveFileFromClient(reader, writter, filePort);
				   }
				   catch (Exception e) {
					   System.out.println("Something go wrong");
					   e.printStackTrace();
				   }
				   break;
				   
			   case "END":
				   end = true;
				   writter.println("OK");
				   System.out.println("Ending connection with current client");
			   break;
				   
			   default:
				   writter.println("unknown");
				   System.out.println("Didn't understand the command");
			   break;
			   }
			}
			// Close the socket
			socket.close();
			break;
		}
		// Close the server socket
		sServ.close();
	  } 
	  catch(Exception e) {
		System.out.println("Error: " + e);
		e.printStackTrace();
	    }
	}
	
}
