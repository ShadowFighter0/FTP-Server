package ServerPackage;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SubConnection
{
	//SERVER
	ServerSocket sServ;
	Socket socket;
	
	BufferedReader socketReader;
	PrintWriter socketWritter;
	
	public boolean Connected = false;

		
	public boolean StartPasiveSubConnection (int port)
	{
		try {
			
			//Wait until the server connects
			sServ = new ServerSocket(port);
			socket = sServ.accept();
			
			//Create reader and writter
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			socketWritter = new PrintWriter(socket.getOutputStream(), true);
		
			Connected = true;
		}
		catch (Exception e) {
			System.out.println("Something went wrong creating the sub connection.");
			e.printStackTrace();
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
	
	private void CloseConnection()
	{
		try {
			socket.close();
			socket = null;
			
			socketReader.close();
			socketReader = null;
			
			socketWritter.close();
			socketWritter = null;
			
			if(sServ != null)
			{
				sServ.close();
				sServ = null;
			}		
			
			Connected = false;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void SendListToClient(String path)
	{		
		socketWritter.println(ReadFolder(path, 0));
		
		socketWritter.println("END");
		CloseConnection();
	}
	
	private String ReadFolder(String path, int deepLevel)
	{
		String message = "";
		
		File folder = new File(path);
		for (File file : folder.listFiles()) 
		{
			for(int i = 0; i < deepLevel; i++)
			{
				if ( i == deepLevel-1)
				{
					message += ">";					
				}
				else
				{
					message += " ";
				}
			}
			
			if (file.isDirectory())
			{
				message += file.getName() + "\n";
				message += ReadFolder(file.getPath(), deepLevel+1);
			}
			else
			{
				message += file.getName() + "\n";
			}
			file = null;
		}
		folder = null;
		return message;

	}
	

	public void ReceiveFileFromClient(String path) throws IOException
	{
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
		FileOutputStream byteToFileConverter = new FileOutputStream(path+nameFile);  
		
		socketWritter.println("Ok");
		
		System.out.println("Waiting for bytes");
	 	byteReader.read(bytes, 0, bytes.length);
		
		System.out.println("Bytes received. Creating File");
		byteToFileConverter.flush();
		byteToFileConverter.write(bytes);
		System.out.println("File Created");
		   
		socketWritter.flush();
		socketWritter.println("OK");
		
		byteReader.close();
		byteReader = null;
		
		byteToFileConverter.close();
		byteToFileConverter = null;
		
		CloseConnection();
	}
	
	public void SendFileToClient(String path) throws IOException
	{
		// Create File and array
		File file = new File(path);
		
		byte[] bytes = new byte[(int) file.length()];

		// Convert a File into byte[]
		FileInputStream fileConverter = new FileInputStream(file);
		System.out.println(fileConverter.read(bytes, 0, bytes.length));

		// Create printer for bytes into the socket
		DataOutputStream bytePrinter = new DataOutputStream(socket.getOutputStream());

		// Send the filename
		socketWritter.flush();
		socketWritter.println(file.getName());

		// Send length
		socketWritter.flush();
		socketWritter.println(bytes.length);

		// Send bytes
		socketReader.readLine();

		System.out.println("Sending Bytes");
		bytePrinter.flush();
		bytePrinter.write(bytes);

		System.out.println("Upload Complete. Waiting for client confirmation");

		String data = socketReader.readLine();

		if (data.equals("OK"))
			System.out.println("Transfer Status: Ok");
		else
			System.out.println("Transfer Status: Error");

		fileConverter.close();
		fileConverter = null;

		bytePrinter.close();
		bytePrinter = null;
		
		CloseConnection();
	}
}
