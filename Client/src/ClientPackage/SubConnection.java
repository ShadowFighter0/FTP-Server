package ClientPackage;

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

//The subConnection for active mode 
public class SubConnection
{
	///CLIENT
	ServerSocket sServ;
	Socket socket;
	
	BufferedReader socketReader;
	PrintWriter socketWritter; 
	
	boolean Connected = false;
	
	public void StartActiveSubConnection (String port)
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
	}
	
	public void ConnectPasiveSubConnection (String port)
	{
		try {
			//Connect to the provide port
			socket = new Socket("localhost", Integer.parseInt(port));
			
			//Create reader and writter
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			socketWritter = new PrintWriter(socket.getOutputStream(), true);
			
			Connected = true;
			
		}catch(Exception e)
		{
			System.out.println("Something went wrong creating the sub connection.");
			e.printStackTrace();
			Connected = false;
		}
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
			
			Connected = true;
		}
	}
	
	public void ReceiveList() throws IOException
	{
		String message = "";
		while (true) 
		{
			message = socketReader.readLine();
			
			if (message.equals("END")) 
			{
				break;
			} 
			else
			{
				System.out.println(message);
			}
		}
		
		CloseConnection();
	}
	
	public void SendFileToServer(String path) throws IOException
	{
		// Create File and array
		File file = new File(path);
		byte[] bytes = new byte[(int) file.length()];

		// Convert a File into byte[]
		FileInputStream fileConverter = new FileInputStream(file);
		fileConverter.read(bytes);
		
		// Create printer for bytes into the socket
		DataOutputStream bytePrinter = new DataOutputStream(socket.getOutputStream());

		// Send the filename
		socketWritter.flush();
		socketWritter.println(file.getName());

		// Send length
		socketWritter.flush();
		socketWritter.println(bytes.length);

		socketReader.readLine();

		System.out.println("Sending Bytes");
		bytePrinter.flush();
		bytePrinter.write(bytes);

		System.out.println("Upload Complete. Waiting for server confirmation");

		String data = socketReader.readLine();

		if (data.equals("OK"))
			System.out.println("Transfer Status: Ok");
		else
			System.out.println("Transfer Status: Error");

		fileConverter.close();
		bytePrinter = null;
		
		CloseConnection();
	}
	
	public void ReceiveFileFromServer(String path) throws IOException
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

}
