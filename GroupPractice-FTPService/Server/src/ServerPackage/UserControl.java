package ServerPackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class UserControl {
	
	String filePath = "src\\ServerPackage\\users.txt";
	
	ArrayList<User> users;
	
	public UserControl ()
	{
		users = new ArrayList<User>();
		
		try {
			readAll();
		} catch (FileNotFoundException e) {
			System.out.println("Problem reading the user file");
			e.printStackTrace();
		}
	}
	
	public void readAll() throws FileNotFoundException
	{
		File userFile = new File(filePath);
		Scanner reader = new Scanner(userFile);
		
		while(reader.hasNext())
		{
			String name = reader.nextLine();
			
			String[] split = name.split(";");
			
			users.add(new User(split[0], split[1]));
		}
	}
	
	public User userExists(String username)
	{
		for (int i = 0 ; i < users.size(); i++)
		{
			if (users.get(i).getName().equals(username))
			{
				return users.get(i);
			}
		}	
			
		return null;
	}
}
