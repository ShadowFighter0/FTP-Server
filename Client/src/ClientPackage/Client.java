package ClientPackage;
import java.io.*;
import java.net.*;

public class Client {

		public static void main(String args[]) {
			
			System.out.println("CLIENT");
			
			int port = 1400;
			
			Socket sCon = null;
			
			//From the socket
			BufferedReader reader;
			PrintWriter writter;
			
			//From the keyboard
			BufferedReader inputKeyboard;
			
			String data;
			String result;
			
			try {
				
				boolean connected = false;
				
				while (!connected)
				{					
					try {
					// Connect to the server
					sCon = new Socket("localhost", port);
					
					//At this point we are connected
					connected = true;
					System.out.println("Succes conection");
					
					}catch (ConnectException e) 
					{
						//Error in connection. Trying again
						System.out.println("Server not found. Trying again in 2 secs");
						Thread.sleep(2000);
					}
				}
				
				reader = new BufferedReader(new InputStreamReader(sCon.getInputStream()));
				writter = new PrintWriter(sCon.getOutputStream(), true);

				inputKeyboard = new BufferedReader(new InputStreamReader(System.in));
				
				// Get the input/output from the socket			
				while(true)
				{					
					// Get text from the keyboard
					System.out.print("Write text (END to close the server): ");
					System.out.flush();
					data = inputKeyboard.readLine();
					data = data.toUpperCase();
						
					// Send data to the server
					writter.println(data);
					
					// Read answer from the server
					result = reader.readLine();
					result = result.toUpperCase();
					
					if(result.contains("OK"))
					{
						switch (data)
						{
							case "FILE":
								
								try {
								SendFileToServer(inputKeyboard,reader);
								}
								catch (Exception e) {
									System.out.println("Something has gone wrong");
									e.printStackTrace();
								}
								break;
							case "END":
								break;
								
							default:
								System.out.println("Not implented yet");							
						} 		
					}
					else if(result.equals("UNKNOWN"))
					{
						System.out.println("Command didn't recognised. Pls check the command");
						break;
					}
				}				
				// Close the connection
				sCon.close();
			}  catch(Exception e) {
				System.out.println("Error: " + e);		
				e.printStackTrace();
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
