package ClientPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

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
			Connected = false;
		}
		
	}
	
	
}
