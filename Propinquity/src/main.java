import java.util.*;


public class main {

	static int numGroups = 10; //of course these two variables are changable 
	static int numStudents = 10;
	
	public static void main(String[] args) {
		
		
		ArrayList<Student> students = new ArrayList<Student>();
		ArrayList<Group> groups = new ArrayList<Group>();
		for(int x = 0; x < numGroups; x++){
			groups.add(new Group());
		}
		
		for(int x = 0; x < numStudents; x++){
			students.add(new Student());
		}
		
		for(int x = 0; x < numGroups; x++){
			for(int y = 0; y<numStudents; y++){
				groups.get(x).recruitStudent(students.get(y));
			}
		}
		
		for(int x = 0; x < groups.size(); x++){
			//I would say a group has to have at least 3 members, however, for now I'm outputing all the groups to see data
		//	if(groups.get(x).getSize()>=3){
				groups.get(x).printStatement();
		//	}
		}
		
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		
		for(int x = 0; x < students.size(); x++){
			students.get(x).printStatement();
		}
		

	}

}
