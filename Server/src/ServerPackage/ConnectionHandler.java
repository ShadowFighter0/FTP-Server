package ServerPackage;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionHandler 
{

	public static void ReceiveFileFromClient(BufferedReader mainSocketReader, PrintWriter mainSocketWritter, int port) throws Exception
	{
		//Send the client the new port for the sub connection
		System.out.println("Sending Port " + port + " to client");
		
		mainSocketWritter.flush();
		mainSocketWritter.println(""+port);
		mainSocketWritter.flush();
				
		//Create socket
	    ServerSocket sServ = new ServerSocket(port);
		Socket socket = sServ.accept();
		System.out.println("Connected to port " + port);
			
		//Create Writters and readers
		BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter socketWritter = new PrintWriter(socket.getOutputStream(), true);
		
		//Read name file
		System.out.print("Waiting for name file");
		String nameFile = socketReader.readLine();
		System.out.println("  |  NameFile received:" + nameFile);
		//Read byte Length
		System.out.print("Waiting fot File Lenght");
		String lengthStr = socketReader.readLine();
		System.out.println("  |  File Lenght received: " + lengthStr);
		
		byte[] bytes = new byte[Integer.parseInt(lengthStr)];
		
		//Create Handler 
		DataInputStream byteReader = new DataInputStream(socket.getInputStream());
		FileOutputStream byteToFileConverter = new FileOutputStream("D:\\Redes\\Server\\"+nameFile);  
		
		System.out.println("Waiting for bytes");
	 	byteReader.read(bytes, 0, bytes.length);
		
		System.out.println("Bytes received. Creating File");
		byteToFileConverter.flush();
		byteToFileConverter.write(bytes);
		System.out.println("File Created");
		   
		mainSocketWritter.flush();
		mainSocketWritter.println("OK");
		
		byteReader.close();
		byteReader = null;
		
		byteToFileConverter.close();
		byteToFileConverter = null;
		
		socketReader.close();
		socketReader = null;
		
		socketWritter.close();
		socketWritter = null;

		socket.close();
		socket = null;

		sServ.close();
		sServ = null;
		
		
		
		
		
	}
}
