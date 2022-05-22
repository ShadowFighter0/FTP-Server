package ServerPackage;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	
	int sizeOfPacket;
	
	public boolean Connected = false;

	public SubConnection(int sizeOfPacket)
	{
		this.sizeOfPacket = sizeOfPacket;
	}
		
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
	
	public void CloseConnection()
	{
		try {
			
			if(socket != null)
			{				
				socket.close();
				socket = null;
			}
			
			if(socketReader!= null)
			{
				socketReader.close();
				socketReader = null;				
			}
			
			if (socketWritter != null)
			{
				socketWritter.close();
				socketWritter = null;				
			}
			
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
	
	public void SendListToClient(String saveFilePath, String path)
	{
		try {
			String message = ReadFolder(path, 0);

			File listFile = new File(saveFilePath+"list.txt");
			PrintWriter writter = new PrintWriter(listFile);
			
			writter.printf(message);
			
			writter.close();
			listFile = null;
			
			SendFileToClient(saveFilePath+"list.txt");
			
			listFile = new File(saveFilePath+"list.txt");
			listFile.delete();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		//Read and send name file
		String nameFile = socketReader.readLine();
		
		//Read and send byte Length
		String lengthStr = socketReader.readLine();
		int fileLength = Integer.parseInt(lengthStr);
		
		//Read and send number packages
		String packagesStr = socketReader.readLine();
		int packages = Integer.parseInt(packagesStr);
		
		//Prepare array of bytes to store the content
		byte[] bytes = new byte[fileLength];
		
		//Create Handlers
		DataInputStream byteReader = new DataInputStream(socket.getInputStream());
		FileOutputStream byteToFileConverter = new FileOutputStream(path+nameFile);  
		
		//Send prepare to server
		socketWritter.println("Ok");
			 	
		for (int i = 0; i < packages; i++)
		{	
			//Create auxiliary array to store the packet
			byte aux [] = new byte [sizeOfPacket];
			
			//Calculate the size of the packet
			int size = (fileLength - ((i)*sizeOfPacket));
			size = size > sizeOfPacket? sizeOfPacket:size;
			
			//Read the packet form the socket
			byteReader.read(aux, 0, size);
			
			//Transfer the packet info into the file array
			for (int j = 0 ; j < size; j++)
			{
				bytes[(i*sizeOfPacket)+j] = aux[j];
			}
		}
		
		//read end confirmation from server
		socketReader.readLine();
		
		//Write the bite array into the file
		byteToFileConverter.write(bytes);
		System.out.println("File Created");
		   
		//Send confirmation to the server
		socketWritter.flush();
		socketWritter.println("OK");
		
		//Close Handlers
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
		byte[] bytes = new byte[(int)file.length()];

		// Convert a File into byte[]
		FileInputStream fileConverter = new FileInputStream(file);		
		// Create printer for bytes into the socket
		DataOutputStream bytePrinter = new DataOutputStream(socket.getOutputStream());

		// Send the filename
		socketWritter.flush();
		socketWritter.println(file.getName());

		// Send length
		socketWritter.flush();
		socketWritter.println(""+file.length());
		
		//Calculate and Send number of Packages
		int packages = (int)file.length() / sizeOfPacket;
		packages += ((int)file.length() % sizeOfPacket) == 0 ? 0:1; //Add a packet if there are still bytes
		
		socketWritter.flush();
		socketWritter.println(""+packages);

		socketReader.readLine(); //OK from server

		fileConverter.read(bytes);

		for (int i = 0 ; i < packages; i++)
		{			
			//Create aux array for stor the packet
			byte[] aux = new byte[sizeOfPacket];

			//Calculate the size of the packet
			int size = ((int)file.length() - ((i)*sizeOfPacket));
			size = size > sizeOfPacket? sizeOfPacket:size;
			
			//Fill the packet
			for (int j = 0 ; j < size; j++)
			{
				aux[j] = bytes[(i*sizeOfPacket)+j];
			}
			
			//Send the packet over socket
			bytePrinter.flush();
			bytePrinter.write(aux);
		}
		//End the sending of packets
		socketWritter.println("END");
		
		//Confirmation from client 
		String data = socketReader.readLine();

		if (data.equals("OK"))
			System.out.println("File Transmited Properly");
		else
			System.out.println("Transfer Status: Error");

		//Close Sender
		fileConverter.close();
		bytePrinter = null;
		
		
		CloseConnection();
	}
	
}
