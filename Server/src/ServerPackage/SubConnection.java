package ServerPackage;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class SubConnection
{
	//SERVER
	ServerSocket sServ;
	Socket socket;
	
	BufferedReader socketReader;
	PrintWriter socketWritter;
	
	public boolean Connected = false;
	
	public boolean StartPasiveSubConnection (String port)
	{
		try {
			sServ = new ServerSocket(Integer.parseInt(port));
			//Wait until the server connects
			socket = sServ.accept();
			
			//Create reader and writter
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			socketWritter = new PrintWriter(socket.getOutputStream(), true);
		
			Connected = true;
		}
		catch (Exception e) {
			System.out.println("Something went wrong creating the sub connection.");
			Connected = false;
		}
		
		return Connected;
	}
	
	
	public boolean ConnectActiveSubConnection (String port)
	{
		try
		{
			//Connect to the provide port
			socket = new Socket("localhost", Integer.parseInt(port));
			
			//Create reader and writter
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			socketWritter = new PrintWriter(socket.getOutputStream(), true);
			
			Connected = true;
		}
		catch (Exception e) {
			System.out.println("Something went wrong creating the sub connection.");
			Connected = false;
		}
		
		return Connected;
	}
	

	public void ReceiveFileFromClient(BufferedReader mainSocketReader, PrintWriter mainSocketWritter, int port) throws Exception
	{		
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
		
		Connected = false;
	}
}
