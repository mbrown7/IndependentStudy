package edu.umw.cpsc.collegesim;
import java.util.*;


/**
 * This main() is currently being used for Maddie's propinquity stuff.
 * Morgan is using the Sim class.
 * @author ML
 */
public class Main {

	private static final int NUM_GROUPS = 10;
	private static final int NUM_STUDENTS = 10;
	
	public static void main(String[] args) {
		
		
		ArrayList<Person> students = new ArrayList<Person>();
		ArrayList<Group> groups = new ArrayList<Group>();
		for(int x = 0; x < NUM_GROUPS; x++){
			groups.add((new Group()).setID(x));
		}
		
		for(int x = 0; x < NUM_STUDENTS; x++){
			String name = Integer.toString(x);
			students.add((new Person(name)).setID(x));
			
		}
		
		for(int x = 0; x < NUM_GROUPS; x++){
			for(int y = 0; y<NUM_STUDENTS; y++){
				groups.get(x).recruitStudent(students.get(y));
			}
		}
		
		for(int x = 0; x < groups.size(); x++){
			//I would say a group has to have at least 3 members, however, for now I'm outputing all the groups to see data
		//	if(groups.get(x).getSize()>=3){
				System.out.println(groups.get(x));
		//	}
		}
		
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		
		for(int x = 0; x < students.size(); x++){
			students.get(x).printStatement();
		}
		

	}

}
